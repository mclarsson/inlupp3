
/** Logout event. */
public class Logout implements java.io.Serializable {
    private Account account;

    /**
     * Constructor
     *
     * @param a Account that is logging out.
     */
    public Logout(Account a) {
        this.account = a;
    }

    /** Get the account that is login out. */
    public Account getAccount() {
        return this.account;
    }
}
