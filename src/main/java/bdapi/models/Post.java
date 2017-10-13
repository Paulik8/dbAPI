package bdapi.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Post {
    private String author;
    private String created;
    private String forum;
    private long id;
    private boolean isEdited;
    private String message;
    private int thread;
    private long parent = 0;

    @JsonCreator
    public Post(@JsonProperty("author") String author,
                @JsonProperty("created") String created,
                @JsonProperty("forum") String forum,
                @JsonProperty("id") long id,
                @JsonProperty("isEdited") boolean isEdited,
                @JsonProperty("message") String message,
                @JsonProperty("thread") int thread,
                @JsonProperty("parent") long parent) {

        this.author = author;
        this.created = created;
        this.forum = forum;
        this.id = id;
        this.isEdited = isEdited;
        this.message = message;
        this.thread = thread;
        this.parent = parent;
    }

    public String  getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String  getCreated() {
        return created;
    }

    public void setCreated(String  created) {
        this.created = created;
    }

    public String  getForum() {
        return forum;
    }

    public void setForum(String  forum) {
        this.forum = forum;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean getIsEdited() {
        return isEdited;
    }

    public void setIsEdited(boolean isEdited) {
        this.isEdited = isEdited;
    }

    public String  getMessage() {
        return message;
    }

    public void setMessage (String  message) {
        this.message = message;
    }

    public int getThread() {
        return thread;
    }

    public void setThread(int thread) {
        this.thread = thread;
    }

    public long  getParent() {
        return parent;
    }

    public void setParent (long  parent) {
        this.parent = parent;
    }


}
