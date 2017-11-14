import java.io.Serializable;

/** Remove friend event. */
public class RemoveFriend implements Serializable {
    private Account friendToBeRemoved;

    /**
     * Constructor
     *
     * @param friendToBeRemoved Account of friend to remove.
     */
    public RemoveFriend(Account friendToBeRemoved) {
        this.friendToBeRemoved = friendToBeRemoved;
    }

    /** Get which account is to be unfriended. */
    public Account getFriend() {
        return this.friendToBeRemoved;
    }
}
