import java.util.*;
import java.io.*;
import java.net.*;

/**
 * The backend of the server system. It contains
 * the data that is to be persistent for all 
 * Client Proxy instances, i.e. the users 
 * known by the server, the logins and the posts.
 *
 * @version %H%, %I%
 */
public class Server {
    private Set<Account> knownUsers = new TreeSet<Account>();
    private Set<Login> knownLogins = new TreeSet<Login>();
    private List<Post> posts = new LinkedList<Post>();

    /**
     * The main function of the server. Begin by trying to
     * open a new server socket, instantiate a new server
     * and begin listening for connections.
     *
     * @param args the port for the socket will be specified
     * at the first of the arguments, defaults to 8080 if 
     * there are none
     */
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

    /**
     * Get the account associated with a specified Id
     *
     * @param userId the unique id of an account
     * @return an account with the specified userId if one exists, else <code>null</code>
     */
    public Account getAccountFor(String userId) {
        for (Account a : this.knownUsers)
            if (a.getUserId().equals(userId)) return a;

        return null;
    }

    /**
     * Get the login associated with a specified Id
     *
     * @param userId the unique id of an account
     * @return a login for an account with the specified Id if one exists, else <code>null</code>
     */
    public Login getLoginFor(String userId) {
	for (Login l : this.knownLogins){
	    if (l.getAccount().getUserId().equals(userId)) return l;}

	return null;
    }

    /**
     * Add an account to the server
     */
    public synchronized void addAccount(Account a) {
        this.knownUsers.add(a);
    }

    /**
     * Add an account's login on the server
     */
    public synchronized void addLogin(Login l) {
	this.knownLogins.add(l);
    }

    /**
     * Remove an account from the server
     */
    public synchronized void removeAccount(Account a) {
        this.knownUsers.remove(a);
    }

    /**
     * Remove an accounts login from the server
     */
    public synchronized void removeLogin(Login l) {
	this.knownLogins.remove(l);
    }

    /**
     * Get a set of all known accounts stored on the server
     */
    public synchronized Set<Account> getAccounts() {
        return new TreeSet<Account>(this.knownUsers);
    }

    /**
     * Get a list of all posts stored on the server
     */
    public synchronized List<Post> getPosts() {
        return new ArrayList<Post>(this.posts);
    }

    /**
     * Get a list of posts that are new to a specified account
     *
     * @param account the account to get posts for
     * @return a list of the posts that have been made since the last time the account got posts
     */
    public synchronized List<Post> getNewPosts(Account account) {
	int since = account.getPostAtLastSync();
	account.setPostAtLastSync(this.posts.size());

	return new ArrayList<Post>(this.posts.subList(since, this.posts.size()));
    }

    /**
     * Get a list of posts that are new to a specified account, and are also made by friends of the account
     *
     * @param account the account to get posts for
     * @return a list of posts that have been made by the account's friends since it last got posts
     */
    public synchronized List<Post> getNewFriendPosts(Account account) {
	List<Post> result = new ArrayList<Post>();
        
	for (Post p : this.getNewPosts(account)) {
	    if (account.isFriendsWith(p.getPoster())) result.add(p);
	}

	return result;
    }

    /**
     * Add a post to the server
     */
    public synchronized void addPost(Post p) {
        this.posts.add(p);
    }

    /**
     * @version %I%, %G%
     *
     * The proxy which handles incoming messages from
     * the {@link Client} and provides it with requested
     * information from the {@link Server}.
     */
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

	/**
	 * Attempt to establish a new connection with a client
	 *
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @param socket the socket which the server listens for connections on
	 * @param server the server which the client proxy is acting as an interface for
	 */
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
	    this.server.removeLogin(a.getUserId());
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

	/**
	 * Continuously try to handle any incoming messages from clients
	 */
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
