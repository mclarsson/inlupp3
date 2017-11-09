import java.util.List;
import java.util.LinkedList;

public class Feed {
    private List<Post> posts = new LinkedList<Post>();

    public void addPost(Post post) {
        posts.add(0, post);
    }

    public String renderAll(Account current) {
        return this.render(posts.size(), current);
    }

    public String renderLatest(int n, Account current) {
        return this.render(n, current);
    }

    private String render(int n, Account current) {
        String result = "";

        for (Post p : this.posts) {
	    // Don't render posts from ignored friends
            if (!current.isCurrentlyIgnoring(p.getPoster())) {
		result = result + p.render();
	    }
            if (--n < 0) break;
        }

        return result;
    }
}
