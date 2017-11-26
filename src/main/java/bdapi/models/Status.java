package bdapi.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Status {
    private Integer forum;
    private Integer post;
    private Integer thread;
    private Integer user;

    @JsonCreator
    public Status(
            @JsonProperty("forum") Integer forum,
            @JsonProperty("post") Integer post,
            @JsonProperty("thread") Integer thread,
            @JsonProperty("user") Integer user
    ) {
        this.forum = forum;
        this.post = post;
        this.thread = thread;
        this.user = user;
    }


    public Status() {

    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public int getForum() {
        return forum;
    }

    public void setForum(int forum) {
        this.forum = forum;
    }

    public int getThread() {
        return thread;
    }

    public void setThread(int thread) {
        this.thread = thread;
    }

    public int getPost() {
        return post;
    }

    public void setPost(int post) {
        this.post = post;
    }
}
