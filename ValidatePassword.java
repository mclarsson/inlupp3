/**
 * @version %I%, %G%
 * 
 * A message used by a {@link Client} to notify the 
 * {@link ClientProxy} that it wishes to validate a 
 * password. The message contains a {@link Login}, 
 * which can be used by the ClientProxy to verify 
 * that the equivalent Login on the {@link Server} 
 * contains the same password as the one provided.
 */

public class ValidatePassword implements java.io.Serializable {
    private Login login;

    /** Initialize a new message regarding password validation.
     * 
     * @param l the login to be validated
     */
    public ValidatePassword(Login l) {
	this.login = l;
    }

    /** Get the login to be validated.
     */
    public Login getLogin(){
	return this.login;
    }
}
