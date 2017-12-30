package bdapi.DAO;

import bdapi.models.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class PostDAO {
    private final JdbcTemplate jdbc;
    private static final PostMapper POST_MAPPER = new PostMapper();

    @Autowired
    public PostDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }


    public void create (List<Post> posts) {
        Object[] object;
        String SQL = "insert into \"posts\" (author, forum, isedited, message, thread, parent, created) VALUES(?,?,?,?,?,?,?::timestamp) returning id";
        for (Post post : posts) {
            Post parentPost = getPostbyId((int)post.getParent());
            post.setCreated(posts.get(0).getCreated());
            object = new Object[] {post.getAuthor(), post.getForum(), post.getIsEdited(), post.getMessage(), post.getThread(), post.getParent(), post.getCreated()};
            post.setId(jdbc.queryForObject(SQL, object, Long.class));
            //post.setId(jdbc.queryForObject(SQL, POST_MAPPER, post.getAuthor(), post.getForum(), post.getIsEdited(), post.getMessage(), post.getThread(), post.getParent(), post.getCreated()));
            final String SQL_posts = "update \"forums\" set posts = posts + 1 WHERE slug::citext = ?::citext";
            jdbc.update(SQL_posts, post.getForum());
            setPath(parentPost, post);
        }
    };

    public Post getDetails(Integer id) {
        try {
            String SQL = "SELECT * FROM \"posts\" WHERE id = ?";
            return jdbc.queryForObject(SQL, POST_MAPPER, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    //@Transactional
    public void setPath(Post parentPost, Post childPost) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement pst = con.prepareStatement(
                    "update posts set" +
                            "  path = ? " +
                            "where id = ?",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            if (childPost.getParent() == 0) {
                ArrayList arr = new ArrayList<Object>(Arrays.asList(childPost.getId()));
                pst.setArray(1, con.createArrayOf("int", arr.toArray()));
            } else {
                if (parentPost.getPath() == null) {
                    ArrayList arr = new ArrayList<Object>(Arrays.asList(0));
                    arr.add(childPost.getId());
                    pst.setArray(1, con.createArrayOf("int", arr.toArray()));
                } else {
                    ArrayList arr = new ArrayList<Object>(Arrays.asList(parentPost.getPath()));
                    arr.add(childPost.getId());
                    pst.setArray(1, con.createArrayOf("int", arr.toArray()));
                }
            }
            pst.setLong(2, childPost.getId());
            return pst;
        }, keyHolder);

        //List<Object> obj = new ArrayList<>();
//        Array arr;
//        Object[] obj;
//        if (childPost.getParent() == 0) {
//            //obj.add(childPost.getId());
//            obj = new Object[] {childPost.getId()};
//        } else {
//            //obj.add(parentPost.getPath());
//            //obj.add(childPost.getId());
//            obj = new Object[] {parentPost.getPath(), childPost.getId()};
//        }
//        String SQL = "update \"posts\" set path = ? where id = ?";
//        jdbc.update(SQL,  obj, childPost.getId());
    }
    //@Transactional
    public void changeMessage(Integer id, String message) {
        String SQL;
        if (message == null) {
            SQL = "update \"posts\" set isedited = true where id = ?";
            jdbc.update(SQL, id);
        }
        else {
            SQL = "update \"posts\" set message = ?, isedited = true where id = ?";
            jdbc.update(SQL, message, id);
        }
    }

    public List<Post> getAuthorByNickname(String nickname) {
        String SQL = "select * from \"posts\" where author::citext = ?::citext";
        return jdbc.query(SQL, POST_MAPPER, nickname);
    }

    public Post getPostbyId(Integer id) {
        try {
            String SQL = "SELECT * FROM \"posts\" WHERE id = ?";
            return jdbc.queryForObject(SQL, POST_MAPPER, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Post getFinishPostbyId(Integer id) {
        try {
            String SQL = "SELECT * FROM \"posts\" WHERE id = ?";
            return jdbc.queryForObject(SQL, POST_MAPPER, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Post getChild(Long parent) {
        try {
            String SQL = "select * from \"posts\" where id = ?";
            return jdbc.queryForObject(SQL, POST_MAPPER, parent);
        } catch (DataAccessException e) {
            return null;
        }
    }

    public List<Post> getPostByForum (String slug) {
        String SQL = "select * from \"posts\" where forum::citext = ?::citext";
        return jdbc.query(SQL,POST_MAPPER, slug);
    }


    private static final class PostMapper implements RowMapper<Post> {
        public Post mapRow(ResultSet resultSet, int i) throws SQLException {
            Post post = new Post();
            post.setAuthor(resultSet.getString("author"));
            post.setForum(resultSet.getString("forum"));
            post.setIsEdited(resultSet.getBoolean("isedited"));
            post.setMessage(resultSet.getString("message"));
            post.setThread(resultSet.getLong("thread"));
            post.setParent(resultSet.getLong("parent"));
            Array path = resultSet.getArray("path");
            //post.setPath((Object[]) resultSet.getObject("path"));
            post.setPath((Object[])path.getArray());
            Timestamp created = resultSet.getTimestamp("created");
            SimpleDateFormat format = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            //format.setTimeZone(TimeZone.getTimeZone("UTC"));
            post.setCreated(format.format(created));
           // post.setCreated(resultSet.getString("created"));
            post.setId(resultSet.getLong("id"));
            return post;
        }
    }
}
