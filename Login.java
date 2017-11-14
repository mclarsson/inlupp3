/**
 * @version %I%, %G%
 * 
 * An association between an {@link Account} and 
 * its password. It is used both in {@link Server}
 * to keep track of the valid passwords of different
 * accounts, and as a message sent by {@link Client} 
 * to notify the {@link ClientProxy} that a user is 
 * attempting to log in.
 */

public class Login implements java.io.Serializable, Comparable<Login> {
    private Account account;
    private String password;

    /**
     * Initialize a new association between an account and a password
     *
     * @param a the account of the login
     * @param p the password used for the account
     */
    public Login(Account a, String p) {
        this.account = a;
	this.password = p;
    }

    /**
     * Get the login account.
     */
    public Account getAccount() {
        return this.account;
    }

    /**
     * Get the valid password of the login's account.
     */
    public String getPassword() {
	return this.password;
    }

    /**
     * Compare the login to another login.
     *
     * @return the result of comparing the logins' accounts
     */
    public int compareTo(Login l) {
	return this.account.compareTo(l.account);
    }

    /**
     * Check if the login is for the same account as another login
     *
     * @return <code>true</code> if the accounts of the logins are equal
     */
    public boolean equals(Object o) {
	if (o instanceof Login) {
	    return ((Login) o).account.equals(this.account);
	} else {
	    return false;
	}
    }
}
