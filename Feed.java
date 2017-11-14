import java.util.List;
import java.util.LinkedList;

/** Keeps tracks of posts.  */
public class Feed {
    private List<Post> posts = new LinkedList<Post>();

    /**
     * Pushes posts to beginning of list of posts.
     *
     * @param post Post to add.
     */
    public void addPost(Post post) {
        posts.add(0, post);
    }

    /**
     * Get string representation of all posts to display to the user.
     * Posts from friends that are being ignored will not be displayed.
     *
     * @param loggedInUser User that is currently logged into client.
     * @return Rendered post in string form.
     */
    public String renderAll(Account loggedInUser) {
        return this.render(posts.size(), loggedInUser);
    }

    /**
     * Get string represantation of a set amount of posts
     * in order to display it to the user. Posts from friends that
     * are being ignored will not be displayed. Newer posts will
     * be rendered first.
     *
     * @param n How many posts to render.
     * @return Rendered post in string form.
     */
    public String renderLatest(int n, Account loggedInUser) {
        return this.render(n, loggedInUser);
    }

    private String render(int n, Account loggedInUser) {
        String result = "";

        for (Post p : this.posts) {
	    // Don't render posts from ignored friends
            if (!loggedInUser.isCurrentlyIgnoring(p.getPoster())) {
		result = result + p.render();
	    }
            if (--n < 0) break;
        }

        return result;
    }
}
