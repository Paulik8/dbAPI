package bdapi.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PostFull {
    private Thread thread;
    private User author;
    private Forum forum;
    private Post post;

    @JsonCreator
    public PostFull(
            @JsonProperty("thread") Thread thread,
            @JsonProperty("author") User user,
            @JsonProperty("post") Post post,
            @JsonProperty("forum") Forum forum

    ) {
        this.author = user;
        this.thread = thread;
        this.post = post;
        this.forum = forum;
    }

    public PostFull() {

    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Forum getForum() {
        return forum;
    }

    public void setForum(Forum forum) {
        this.forum = forum;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}
