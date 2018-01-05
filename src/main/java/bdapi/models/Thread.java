package bdapi.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;
import java.util.Date;

public class Thread {
    private String author;
    //private String created;
    private String forum;
    private int id;
    private int forumid;
    private String message;
    private String slug;
    private String title;
    private int votes;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date created;

    public int getForumid() {
        return forumid;
    }

    public void setForumid(int forumid) {
        this.forumid = forumid;
    }

    @JsonCreator
        public Thread(
            @JsonProperty("author") String author,
            @JsonProperty("created") Date created,
            @JsonProperty("forum") String forum,
            @JsonProperty("id") int id,
            @JsonProperty("forumid") int forumid,
            @JsonProperty("message") String message,
            @JsonProperty("slug") String slug,
            @JsonProperty("title") String title,
            @JsonProperty("votes") int votes
    ) {
        this.author = author;
        //this.created = created;
//        if (created == null) {
//            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//            this.created = timestamp.toInstant().toString();
//        } else {
//            this.created = created;
//
//        }
//        if (created == null)
//            this.created = null;
//        else
//            this.created = created.toInstant().toString();
        this.forum = forum;
        this.id = id;

        this.forumid = forumid;
        this.message = message;
        this.slug = slug;
        this.title = title;
        this.votes = votes;
        this.created = created;
    }

    public Thread() {
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date  getCreated() {
        return created;
    }

    public void setCreated(Date  created)
    //{
//        if (created == null)
//            this.created = null;
//        else
//            this.created = created.toInstant().toString();
//    }
    {
        this.created = created;
    }

    public String getForum() {
        return forum;
    }

    public void setForum(String forum) {
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
