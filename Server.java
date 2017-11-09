import java.util.*;
import java.io.*;
import java.net.*;

public class Server {
    private Set<Account> knownUsers = new TreeSet<Account>();
    private Set<Login> knownLogins = new TreeSet<Login>();
    private List<Post> posts = new LinkedList<Post>();

    public static void main(String[] args) {
        try {
            ServerSocket socket = new ServerSocket(args.length > 0 ? Integer.parseInt(args[0]) : 8080);
            Server server = new Server();

            while (true) {
                System.out.println("!! Server listening for connections: " + socket.getInetAddress() + ":" + socket.getLocalPort());
                Socket clientConnection = socket.accept();
                System.out.println("!! Server got a connection from: " + clientConnection.getInetAddress() + ":" + clientConnection.getPort());
                try {
                    ClientProxy.attemptEstablishConnection(clientConnection, server);
                } catch (RuntimeException e) {
                    System.err.println(e.getMessage());
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
    }

    public Account getAccountFor(String userId) {
        for (Account a : this.knownUsers)
            if (a.getUserId().equals(userId)) return a;

        return null;
    }

    public Login getLoginFor(String userId) {
	for (Login l : this.knownLogins){
	    if (l.getAccount().getUserId().equals(userId)) return l;}

	return null;
    }

    public synchronized void addAccount(Account a) {
        this.knownUsers.add(a);
    }

    public synchronized void addLogin(Login l) {
	this.knownLogins.add(l);
    }

    public synchronized void removeAccount(Account a) {
        this.knownUsers.remove(a);
    }

    public synchronized void removeLogin(Login l) {
	this.knownLogins.remove(l);
    }

    public synchronized Set<Account> getAccounts() {
        return new TreeSet<Account>(this.knownUsers);
    }

    public synchronized List<Post> getPosts() {
        return new ArrayList<Post>(this.posts);
    }

    public synchronized List<Post> getNewPosts(Account account) {
	int since = account.getPostAtLastSync();
	account.setPostAtLastSync(this.posts.size());

	return new ArrayList<Post>(this.posts.subList(since, this.posts.size()));
    }

    public synchronized List<Post> getNewFriendPosts(Account account) {
	ArrayList<Post> result = new ArrayList<Post>();

	for (Post p : this.getNewPosts(account)) {
	    if (account.isFriendsWith(p.getPoster())) result.add(p);
	}

	return result;
    }

    public synchronized void addPost(Post p) {
        this.posts.add(p);
    }

    static class ClientProxy extends Thread {
        private Account account;
        private Socket socket;
        private Server server;
        private ObjectOutputStream outgoing;
        private ObjectInputStream incoming;

        private ClientProxy(Account account, Socket socket, Server server, ObjectInputStream incoming) throws IOException {
            this.account = account;
            this.server  = server;
            this.socket  = socket;
            this.incoming = incoming;
            this.outgoing = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("<< Account");
            this.outgoing.writeObject(account);
            this.outgoing.flush();
        }

        public static void attemptEstablishConnection(Socket socket, Server server) throws IOException, ClassNotFoundException {
            ObjectInputStream incoming = new ObjectInputStream(socket.getInputStream());
            Object handShake = incoming.readObject();
	    
            if (handShake instanceof Login) {
                Account account = ((Login) handShake).getAccount();
                Account knownAccount = server.getAccountFor(account.getUserId());
		String password = ((Login) handShake).getPassword();
		
                if (knownAccount == null) {
                    server.addAccount(account);
		    server.addLogin(new Login(account, password));
                    new ClientProxy(account, socket, server, incoming).start();
                } else {
		    String knownPassword = server.getLoginFor(account.getUserId()).getPassword();

                    if (knownPassword.equals(password) == false) throw new RuntimeException("Wrong password");
                    new ClientProxy(knownAccount, socket, server, incoming).start();
                }
            } else {
                System.err.println("!! Bad connection attempt from: " + socket.getInetAddress() + ":" + socket.getPort());
            }
        }

        private int globalPostIdCounter = 0;
        // The synchronised keyword is required on all methods which may
        // be called in parallel on the server from multiple clients at
        // the same time.
        private synchronized int getUniqueGlobalPostId() {
            return ++this.globalPostIdCounter;
        }

        private void logout(Account a) {
            this.server.removeAccount(a);
            System.out.println("!! " + a.getUserId() + " left the building");
            try {
                this.outgoing.close();
                this.incoming.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        private void postMessage(String msg) {
            this.server.addPost(new Post(this.getUniqueGlobalPostId(), this.account, msg));
        }

        private void addFriend(Account a) {
            this.account.addFriend(a);
            a.addFriend(this.account);
        }

        private void removeFriend(Account a) {
            this.account.removeFriend(a);
            a.removeFriend(this.account);
        }

	private void validatePassword(Login login) {
            try {
                System.out.println("<< ValidationResponse");
		Login validLogin = server.getLoginFor(login.getAccount().getUserId());

		if (login.equals(validLogin) && login.getPassword().equals(validLogin.getPassword())){
		    this.outgoing.writeObject(true);
		} else {
		    this.outgoing.writeObject(false);
		}
                this.outgoing.flush();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
	}

        private void updateAccount(Account old, Login neu) {
            server.removeAccount(old);
	    server.removeLogin(server.getLoginFor(old.getUserId()));
            server.addAccount(neu.getAccount());
	    server.addLogin(neu);
        }

        private void sync() {
            try {
                System.out.println("<< SyncResponse");
                this.outgoing.
                writeObject(new SyncResponse(new HashSet<Account>(this.server.getAccounts()),
                                             new LinkedList<Post>(this.server.getNewFriendPosts(this.account))));
                this.outgoing.flush();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        public void run() {
            try {
                while (true) {
                    Object o = this.incoming.readObject();
                    System.err.println(">> Received: " + o.getClass().getName());
                    // o instanceof Account checks if o is an account
                    // (Account) o type casts o into an Account so that it can be used as one
                    if (o instanceof Login) {
                        this.updateAccount(this.account, (Login) o);
                    } else if (o instanceof ValidatePassword) {
			this.validatePassword(((ValidatePassword) o).getLogin());
		    } else if (o instanceof PostMessage) {
                        this.postMessage(((PostMessage) o).getMsg());
                    } else if (o instanceof AddFriend) {
                        this.addFriend(((AddFriend) o).getFriend());
                    } else if (o instanceof RemoveFriend) {
                        this.removeFriend(((RemoveFriend) o).getFriend());
                    } else if (o instanceof SyncRequest) {
                        this.sync();
                    } else if (o instanceof Logout) {
                        this.logout(((Logout) o).getAccount());
                        return;
                    }
                }
            } catch (Exception e) {
                // BAD Practise. Never catch "Exception"s. Too general.
                e.printStackTrace();
            }
        }
    }
}
