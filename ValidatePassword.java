public class ValidatePassword implements java.io.Serializable {
    private Login login;

    public ValidatePassword(Login l) {
	this.login = l;
    }

    public Login getLogin(){
	return this.login;
    }
}
