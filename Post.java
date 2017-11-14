import java.io.Serializable;

/** Implementation of a post made by user. */
public class Post implements Serializable {
    private int globalPostId;
    private Account poster;
    private String content;

    /** Get the account that made the post. */
    public Account getPoster() {
        return this.poster;
    }

    /**
     * Constructor
     *
     * @param globalPostId
     * @param poster Account of user that made the post.
     * @param content Content of post.
     */
    public Post(int globalPostId, Account poster, String content) {
        this.globalPostId = globalPostId;
        this.poster       = poster;
        this.content      = content;
    }

    /** Get string represantion of post to display to user. */
    public String render() {
        return "{" + this.poster.getName() + "} says:\n" + this.content + "\n";
    }
}
