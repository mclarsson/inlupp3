public class Login implements java.io.Serializable, Comparable<Login> {
    private Account account;
    private String password;
    
    public Login(Account a, String p) {
        this.account = a;
	this.password = p;
    }

    public Account getAccount() {
        return this.account;
    }

    public String getPassword() {
	return this.password;
    }

    public int compareTo(Login l) {
	return this.account.compareTo(l.account);
    }

    public boolean equals(Object o) {
	if (o instanceof Login) {
	    return ((Login) o).account.equals(this.account);
	} else {
	    return false;
	}
    }
}
