/**
 * A message used by a Client to notify the 
 * Client Proxy that it wishes to validate a 
 * password. The message contains a Login, 
 * which can be used by the Client Proxy to verify 
 * that the equivalent Login on the Server 
 * contains the same password as the one provided.
 *
 * @version %I%, %G%
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
