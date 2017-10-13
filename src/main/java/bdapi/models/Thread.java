package bdapi.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Thread {
    private String author;
    private String created;
    private String forum;
    private int id;
    private String message;
    private String slug;
    private String title;
    private int votes;

    @JsonCreator
    public Thread(
            @JsonProperty("author") String author,
            @JsonProperty("created") String created,
            @JsonProperty("forum") String forum,
            @JsonProperty("id") int id,
            @JsonProperty("message") String message,
            @JsonProperty("slug") String slug,
            @JsonProperty("title") String title,
            @JsonProperty("votes") int votes
    ) {
        this.author = author;
        this.created = created;
        this.forum = forum;
        this.id = id;
        this.message = message;
        this.slug = slug;
        this.title = title;
        this.votes = votes;
    }

    public Thread() {
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getForum() {
        return forum;
    }

    public void setForum() {
        this.forum = forum;
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }
}
