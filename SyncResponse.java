import java.io.Serializable;
import java.util.Set;
import java.util.List;

/**
 * @version %I%, %G%
 * 
 * A message used by the {@link ClientProxy} to provide
 * the {@link Client} with the data requested by the Client
 * with a {@link SyncRequest}. The message stores a list of
 * users and posts that can be extracted via provided methods
 * by the Client.
 */

public class SyncResponse implements Serializable {
    private Set<Account> users;
    private List<Post> posts;

    /**
     * Initialize a new sync response.
     * 
     * @param users a set of the users the server knows about
     * @param posts a list of posts made to the server
     */
    public SyncResponse(Set<Account> users, List<Post> posts) {
        this.users = users;
        this.posts = posts;
    }
    
    /** 
     * Get the provided posts.
     */
    public List<Post> getPosts() {
        return this.posts;
    }

    /**
     * Get the known users.
     */
    public Set<Account> getUsers() {
        return this.users;
    }
}
