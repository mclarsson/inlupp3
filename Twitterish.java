import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Main class for client.
 */
public class Twitterish {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java Twitterish <server_ip> <port_number>");
        } else {
            try {
                new Client(args[0], Integer.parseInt(args[1])).start();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Something went wrong. Please debug this error.");
            }
        }
    }
    private static class Client {
        private Account loggedInUser;
        private Set<Account> knownUsers = new TreeSet<Account>();
	private Feed feed = new Feed();

        private ObjectOutputStream out;
        private ObjectInputStream in;

        private String serverIp;
        private String myIp;
        private int port;

	/**
	 * Constructor.
	 *
	 * @param serverIp IP adress of server.
	 * @param port Which port to connect to server through.
	 */
        public Client(String serverIp, int port) {
            this.serverIp = serverIp;
            this.port = port;
        }

        private void newAccount(Account account) {
            this.knownUsers.add(account);
        }

        private void newPost(Post post) {
            if (this.loggedInUser.isFriendsWith(post.getPoster()) &&
                !this.loggedInUser.isCurrentlyIgnoring(post.getPoster())) {
                feed.addPost(post);
            } else {
                // Ignore post
            }
        }
        private void sendMessage(Object o) {
            try {
                this.outgoing.writeObject(o);
                this.outgoing.flush();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        private Object receiveMessage() {
            try {
                Object o = this.incoming.readObject();
                String s = o == null ? "<null>" : o.getClass().toString();
                System.out.printf("Received %s message\n",  s);
                return o;
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
            return null;
        }

        private void postMessage() {
            System.out.println("Write your message on a single line: ");

            String msg = System.console().readLine();
            sendMessage(new PostMessage(msg));

            System.out.println("Message sent");
        }

        private void printEnumeratedChoices(Account[] choices) {
            for (int i = 0; i < choices.length; ++i) {
                System.out.println(i + "\t" + choices[i].getName());
            }
        }

        private Account chooseAccount(Account[] knownUsers){
            printEnumeratedChoices(knownUsers);
            String choiceString;
            
            while (true) {
	        choiceString = System.console().readLine();

                try {
                    int choice = Integer.parseInt(choiceString);
                    if (choice >= 0 && choice < knownUsers.length) return knownUsers[choice];
                } catch (NumberFormatException e) {
		    System.out.println("Invalid input \'" + choiceString + "\'");
		}
            }
        }
        
        private void addFriend() {
            if (this.knownUsers.size() == 0) {
                System.out.println("You seem to be alone in the universe, at this moment.");
                return;
            }

            System.out.println("Who to befriend?");

            Account[] knownUsers = (Account[]) this.knownUsers.toArray(new Account[0]);
            Arrays.sort(knownUsers);


            Account friend = chooseAccount(knownUsers);

            sendMessage(new AddFriend(friend));
            this.loggedInUser.addFriend(friend);

            System.out.println("Befriended " + friend.getName());
        }

        private void removeFriend() {
            if (this.loggedInUser.hasFriends() == false) {
                System.out.println("You don't have anyone to unfriend. Try to make a few friends first.");
                return;
            }

            System.out.println("Who to unfriend?");
            Account[] friends = this.loggedInUser.getFriends();

            Account friend = chooseAccount(friends);

            sendMessage(new RemoveFriend(friend));
            this.loggedInUser.removeFriend(friend);

            System.out.println("Unfriended " + friend.getName());
        }

        private void ignoreFriend() {
            if (this.loggedInUser.hasFriends() == false) {
                System.out.println("You don't have anyone to ignore. Try to make a few friends first.");
                return;
            }

            System.out.println("Who to ignore?");
            Account[] friends = this.loggedInUser.getFriends();

            Account friend = chooseAccount(friends);

            this.loggedInUser.ignoreFriend(friend);

            System.out.println("Ignored " + friend.getName());
        }

	private void unIgnoreFriend() {
            if (this.loggedInUser.hasIgnoredFriends() == false) {
                System.out.println("You're not ignoring anyone.");
                return;
            }

            System.out.println("Who to unignore?");
            Account[] friends = this.loggedInUser.getIgnoredFriends();

            Account friend = chooseAccount(friends);

            this.loggedInUser.unIgnoreFriend(friend);

            System.out.println("Unignored " + friend.getName());
        }

        private void quit() {
            System.out.println("Logging out...");
            this.sendMessage(new Logout(this.loggedInUser));
        }

        private void editAccount() {
            System.out.print("Enter your password: ");
            String password = new String(System.console().readPassword());


            System.out.println("Validating password...");
	    this.sendMessage(new ValidatePassword(new Login(this.loggedInUser, password)));
            boolean validPassword = (Boolean) receiveMessage();

            if (validPassword) {
                System.out.print("Update your password: ");
                password = new String(System.console().readPassword());

                System.out.print("Enter your user name: ");
                String name = System.console().readLine();

                String userid = this.loggedInUser.getUserId();
		this.sendMessage(new Login(new Account(userid, name), password));
            } else {
                System.out.println("Wrong password!");
            }
        }

        private void listFriends() {
            if (this.loggedInUser.hasFriends()) {
                Account[] friends = this.loggedInUser.getFriends();
                this.printEnumeratedChoices(friends);
            } else {
                System.out.println("Sorry, but you don't seem to have any friends.");
            }
        }

        private void syncWithServer() {
            this.sendMessage(new SyncRequest());
            Object o = this.receiveMessage();

            if (o instanceof SyncResponse) {

		Set<Account> newUsers = new TreeSet<Account>();

		for (Account syncUser : (((SyncResponse) o).getUsers())) {
		    boolean userAlreadyKnown = false;
		    for (Account knownUser : this.knownUsers) {
			if (syncUser.getUserId().equals(knownUser.getUserId())) {
			    knownUser.setName(syncUser.getName());
			    userAlreadyKnown = true;
			}
		    }

		    if (!userAlreadyKnown) {
			newUsers.add(syncUser);
		    }
		}

		this.knownUsers.addAll(newUsers);

                // TODO
                // Only print the posts that I am interested in

                // TODO
                // Use the feed object for this

                for (Post p : ((SyncResponse) o).getPosts()) {
		    feed.addPost(p);
		}

            } else {
                System.out.println("Error: expected sync response, got " + o.getClass());
            }
        }

	private void updateFeed() {
	    String o = feed.renderAll(this.loggedInUser);
	    System.out.println(o);
	}

        private ObjectOutputStream outgoing;
        private ObjectInputStream incoming;

        private void loginOrCreateUser() throws IOException, UnknownHostException {
            Socket socket = new Socket(this.serverIp, port);
            this.outgoing = new ObjectOutputStream(socket.getOutputStream());

            System.out.print("Enter your user id (email address): ");
            String userid = System.console().readLine();
            System.out.print("Set your password: ");
            String password = new String(System.console().readPassword());
            System.out.print("Enter your user name: ");
            String name = System.console().readLine();

            assert(userid.length() > 0);
            assert(password.length() > 0);
            assert(name.length() > 0);

            System.out.println("Logging in new user " + userid + "...");
            outgoing.writeObject(new Login(new Account(userid, name), password));

            this.outgoing = outgoing;
            incoming = new ObjectInputStream(socket.getInputStream());
            Account a = (Account) receiveMessage();
            this.loggedInUser = a;
        }

        private void displaySplashScreen() {
            System.out.println("");
            System.out.println("");
            System.out.println("  _______       _ _   _            _     _      ");
            System.out.println(" |__   __|     (_) | | |          (_)   | |     ");
            System.out.println("    | |_      ___| |_| |_ ___ _ __ _ ___| |__   ");
            System.out.println("    | \\ \\ /\\ / / | __| __/ _ \\ '__| / __| '_ \\  ");
            System.out.println("    | |\\ V  V /| | |_| ||  __/ |  | \\__ \\ | | | ");
            System.out.println("    |_| \\_/\\_/ |_|\\__|\\__\\___|_|  |_|___/_| |_| ");
            System.out.println("");
        }

        private boolean action() {
            System.out.println("Actions:");
            System.out.print("[P]ost message     |  ");
            System.out.print("[S]ync with server |  ");
            System.out.print("[U]pdate feed      |  ");
            System.out.print("[A]dd friend       |  ");
            System.out.print("[R]emove friend    |  ");
            System.out.println();
            System.out.print("[I]gnore friend    |  ");
	    System.out.print("U[n]ignore friend  |  ");
            System.out.print("[L]ist friends     |  ");
            System.out.print("[E]dit account     |  ");
            System.out.print("[Q]uit");
            System.out.println();

            String input = System.console().readLine().toLowerCase();

            if (input.length() != 1) {
                System.out.println("Invalid input");
                return true;
            }

            switch (input.charAt(0)) {
            case 'p':
                this.postMessage();
                return true;
            case 's':
                this.syncWithServer();
                return true;
	    case 'u':
		this.updateFeed();
		return true;
            case 'a':
                this.addFriend();
                return true;
            case 'r':
                this.removeFriend();
                return true;
            case 'i':
                this.ignoreFriend();
                return true;
	    case 'n':
                this.unIgnoreFriend();
                return true;
            case 'e':
                this.editAccount();
                return true;
            case 'l':
                this.listFriends();
                return true;
            case 'q':
                this.quit();
                return false;
            }

            return true;
        }

	/**
	 * Starts up the client, continuing until commanded to stop.
	 */
        public void start() throws IOException, UnknownHostException {
            this.displaySplashScreen();
            this.loginOrCreateUser();
            while (this.action())
                ;
        }
    }
}
