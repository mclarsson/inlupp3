import java.util.Set;
import java.util.TreeSet;
import java.io.Serializable;

/**
 * @version %I%, %G%
 * 
 * An account containing information about the user. 
 * Instances of the class can be found in {@link Client}
 * and {@link ClientProxy}, where it is used to represent 
 * the currently logged in user. A list of accounts can 
 * also be found in Client and {@link Server}, to show which 
 * accounts are known. Since accounts are passed between 
 * the server and the client, the password is stored 
 * elsewhere to ensure security.
 */

public class Account implements Serializable, Comparable<Account> {
    private int postsAtLastSync;
    private String name;
    private String userId;
    private Set<Account> friends = new TreeSet<Account>();
    private Set<Account> ignoredFriends = new TreeSet<Account>();

    /**
     * Instantiate an account based only on ID
     *
     * @param userId the uniquely identifying string for the account
     */
    public Account(String userId) {
        this.userId   = userId;
    }

    /**
     * Instantiate an account based on an ID and a username
     *
     * @param userId the uniquely identifying string for the account
     * @param name the displayed name of the account's user
     */
    public Account(String userId, String name) {
        this(userId);
        this.name = name;
    }

    /**
     * Get the displayed name associated with the owner of the account
     *
     * @return the displayed name of the account's user
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name to be displayed by the account
     * 
     * @param name the displayed name of the account's user
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the ID associated with the account
     * 
     * @return the uniquely identifying string for the account
     */
    public String getUserId() {
        return this.userId;
    }

    /**
     * Set a new ID for the account
     *
     * @param userId the uniquely identifying string for the account
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Get the amount of posts the user of the account was aware of
     * the last time the Client synchronized with the Server.
     *
     * @return the amount of posts the account knew of at the last sync
     */
    public int getPostAtLastSync() {
	return this.postsAtLastSync;
    }

    
    /**
     * Set the amount of posts the user of the account is aware of.
     * Should be done when the Client synchronizes with the Server.
     *
     * @param posts the amount of posts the account knows of at the current sync
     */
    public void setPostAtLastSync(int posts) {
	this.postsAtLastSync = posts;
    }

    public void addFriend(Account a) {
        this.friends.add(a);
    }

    public void removeFriend(Account a) {
        this.friends.remove(a);
    }

    public void ignoreFriend(Account a) {
        if (this.isFriendsWith(a)) this.ignoredFriends.add(a);
    }

    public void unIgnoreFriend(Account a) {
        if (this.isFriendsWith(a)) this.ignoredFriends.remove(a);
    }

    public boolean isFriendsWith(Account a) {
        return this.friends.contains(a);
    }

    public boolean isCurrentlyIgnoring(Account a) {
        return this.ignoredFriends.contains(a);
    }

    public int compareTo(Account a) {
        return a.userId.compareTo(this.userId);
    }

    public boolean hasFriends() {
        return this.friends.size() > 0;
    }

    public boolean hasIgnoredFriends() {
        return this.ignoredFriends.size() > 0;
    }

    public Account[] getFriends() {
        return (Account[]) this.friends.toArray(new Account[0]);
    }

    public Account[] getIgnoredFriends() {
        return (Account[]) this.ignoredFriends.toArray(new Account[0]);
    }

    public boolean equals(Object o) {
        if (o instanceof Account) {
            return ((Account) o).userId.equals(this.userId);
        } else {
            return false;
        }
    }
}
