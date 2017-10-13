package bdapi.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Forum {
    private long posts;
    private String slug;
    private int threads;
    private String title;
    private String user;

    @JsonCreator
    public Forum(@JsonProperty("posts") int posts,
                 @JsonProperty("slug") String slug,
                 @JsonProperty("threads") int threads,
                 @JsonProperty("user") String user,
                 @JsonProperty("title") String title) {
        this.posts = posts;
        this.slug = slug;
        this.threads = threads;
        this.user = user;
        this.title = title;
    }

    public Forum() {

    }

    public long getPosts() {
        return posts;
    }

    public void setPosts(long posts) {
        this.posts = posts;
    }

    public String  getSlug() {
        return slug;
    }

    public void setSlug (String  slug) {
        this.slug = slug;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public String  getTitle() {
        return title;
    }

    public void setTitle (String  title) {
        this.title = title;
    }

    public String  getUser() {
        return user;
    }

    public void setUser(String  user) {
        this.user = user;
    }

}
