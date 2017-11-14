import java.io.Serializable;

/**
 * A message used by a Client to notify the 
 * Client Proxy that the logged in user has  
 * added a friend. The message stores the added  
 * friend's account upon instantiation and provides 
 * a method for the server to get said account.
 *
 * @version %I%, %G%
 */

public class AddFriend implements Serializable {
    private Account friendToBeAdded;

    /**
     * Initialize a new message regarding friend addition.
     *
     * @param friendToBeAdded the account of the user
     * who is to be befriended.
     */
    public AddFriend(Account friendToBeAdded) {
        this.friendToBeAdded = friendToBeAdded;
    }

    /**
     * Get a reference to the account of the user who
     * the message indicates has been added.
     *
     * @return the account of the added friend.
     */
    public Account getFriend() {
        return this.friendToBeAdded;
    }
}
