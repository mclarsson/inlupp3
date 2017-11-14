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
     * Constructor.
     *
     * @param userId Unique string for identifying user.
     */
    public Account(String userId) {
        this.userId   = userId;
    }

    /**
     * Constructor.
     *
     * @param userId Unique string for identifying user.
     * @param name Name of user.
     */
    public Account(String userId, String name) {
        this(userId);
        this.name = name;
    }

    /** Get name of user.  */
    public String getName() {
        return this.name;
    }

    /** Set name of user.  */
    public void setName(String name) {
        this.name = name;
    }

    /** Get unique ID of user.  */
    public String getUserId() {
        return this.userId;
    }

    /** Set unique ID of user.  */
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

    /** Add account to list of friends. */
    public void addFriend(Account a) {
        this.friends.add(a);
    }

    /** Remove account from friends list.  */
    public void removeFriend(Account a) {
        this.friends.remove(a);
    }

    /** Add friend to list of ignored friends, meaning that no posts from that user will appear when displaying posts.  */
    public void ignoreFriend(Account a) {
        if (this.isFriendsWith(a)) this.ignoredFriends.add(a);
    }

    /** Remove friend from list of ignored friends.  */
    public void unIgnoreFriend(Account a) {
        if (this.isFriendsWith(a)) this.ignoredFriends.remove(a);
    }

    /** Check if account is in list of friends.  */
    public boolean isFriendsWith(Account a) {
        return this.friends.contains(a);
    }

    /** Check if an account is being ignored. */
    public boolean isCurrentlyIgnoring(Account a) {
        return this.ignoredFriends.contains(a);
    }

    /** Compare accounts ID to this ID. */
    public int compareTo(Account a) {
        return a.userId.compareTo(this.userId);
    }

    /** Check if the user currently have any users in friends list. */
    public boolean hasFriends() {
        return this.friends.size() > 0;
    }

    /** Check if any friends are being ignored. */
    public boolean hasIgnoredFriends() {
        return this.ignoredFriends.size() > 0;
    }

    /** Get all current friends in array. */
    public Account[] getFriends() {
        return (Account[]) this.friends.toArray(new Account[0]);
    }

    /** Get all currently ignored friends in array. */
    public Account[] getIgnoredFriends() {
        return (Account[]) this.ignoredFriends.toArray(new Account[0]);
    }

    /** Check if an account is equivalent to this (user id is the same). */
    public boolean equals(Object o) {
        if (o instanceof Account) {
            return ((Account) o).userId.equals(this.userId);
        } else {
            return false;
        }
    }
}
