import java.io.Serializable;

/**
 * @version %I%, %G%
 * 
 * A message used by a {@link Client} to notify the 
 * {@link ClientProxy} that the logged in user has  
 * made a new post. The class stores the message and 
 * provides a method for getting a reference to it.
 */

public class PostMessage implements Serializable {
    private String msg;

    /**
     * Initialize a new message regarding a new message being posted.
     *
     * @param msg the message that has been posted.
     */
    public PostMessage(String msg) {
        this.msg = msg;
    }

    /**
     * Get the posted message.
     */
    public String getMsg() {
        return msg;
    }
}
