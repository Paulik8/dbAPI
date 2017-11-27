package bdapi.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

public class Post {
    private String author;
    private String created;
    private String forum;
    private long id;
    private boolean isEdited;
    private String message;
    private long thread;

    public Object[] getPath() {
        return path;
    }

    public void setPath(Object[] path) {
        this.path = path;
    }

    private long parent = 0;
    private Object[] path;

    @JsonCreator
    public Post(@JsonProperty("author") String author,
                @JsonProperty("created") String created,
                @JsonProperty("forum") String forum,
                @JsonProperty("id") long id,
                @JsonProperty("isEdited") boolean isEdited,
                @JsonProperty("message") String message,
                @JsonProperty("thread") long thread,
                @JsonProperty("parent") long parent) {

        this.author = author;
        //this.created = created;
        if (created == null) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            this.created = timestamp.toInstant().toString();
        } else {
            this.created = created;

        }
        this.forum = forum;
        this.id = id;
        this.isEdited = isEdited;
        this.message = message;
        this.thread = thread;
        this.parent = parent;
    }

    public Post() {

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

    public long getThread() {
        return thread;
    }

    public void setThread(long thread) {
        this.thread = thread;
    }

    public long  getParent() {
        return parent;
    }

    public void setParent (long  parent) {
        this.parent = parent;
    }


}
