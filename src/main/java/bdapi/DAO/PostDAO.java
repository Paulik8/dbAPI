package bdapi.DAO;

import bdapi.models.*;
import bdapi.models.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
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

    public void insertUser(Forum forum, User user) {
        jdbc.update("INSERT INTO users_forum (forumid, nickname, fullname, email, about) VALUES (?,?,?,?,?) ON CONFLICT (forumid, nickname) DO NOTHING", forum.getId(), user.getNickname(), user.getFullname(), user.getEmail(), user.getAbout());
    }

    public int create (List<Post> posts, Thread thread) throws SQLException {
        Connection con = jdbc.getDataSource().getConnection();
             PreparedStatement pst = con.prepareStatement(
                     "INSERT INTO \"posts\" (id, author, forum, isedited, message, thread, parent, created, path)" +
                             " VALUES(?,?,?,?,?,?,?,?::TIMESTAMP,?)");
            for (Post post : posts) {
                ArrayList arrObj;
                Post parentPost = getPostbyId((int) post.getParent());
                Post check = getChild(post.getParent());
                if ((parentPost == null || check == null) &&
                        post.getParent() != 0 || (check != null && check.getThread() != post.getThread())) {
                    return 409;
                }
                post.setCreated(posts.get(0).getCreated());
                Long ids = jdbc.queryForObject("SELECT nextval('posts_id_seq')", Long.class);
                if (post.getParent() == 0) {
                    ArrayList arr = new ArrayList<>(Arrays.asList(ids));
                    arrObj = arr;
                } else {
                    ArrayList arr = new ArrayList<>(Arrays.asList(parentPost.getPath()));
                    arr.add(ids);
                    arrObj = arr;
                }

                post.setId(ids);
                post.setPath(arrObj.toArray());
                ArrayList finalArrObj = arrObj;
                pst.setLong(1, post.getId());
                pst.setString(2, post.getAuthor());
                pst.setString(3, post.getForum());
                pst.setBoolean(4, post.getIsEdited());
                pst.setString(5, post.getMessage());
                pst.setLong(6, post.getThread());
                pst.setLong(7, post.getParent());
                pst.setString(8, post.getCreated());
                pst.setArray(9, con.createArrayOf("int", finalArrObj.toArray()));

                pst.addBatch();
            }
            pst.executeBatch();
            con.close();

            final String SQL_posts = "UPDATE \"forums\" SET posts = posts + ? WHERE slug::CITEXT = ?::CITEXT";
            jdbc.update(SQL_posts, posts.size(), thread.getForum());

            return 201;
    };

    @Transactional
    public Post getDetails(Integer id) {
        try {
            String SQL = "SELECT * FROM \"posts\" WHERE id = ?";
            return jdbc.queryForObject(SQL, POST_MAPPER, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Transactional
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


    @Transactional
    public Post getPostbyId(Integer id) {
        try {
            String SQL = "SELECT * FROM \"posts\" WHERE id = ?";
            return jdbc.queryForObject(SQL, POST_MAPPER, id);
        } catch (DataAccessException e) {
            return null;
        }
    }

    @Transactional
    public Post getChild(Long parent) {
        try {
            String SQL = "select * from \"posts\" where id = ?";
            return jdbc.queryForObject(SQL, POST_MAPPER, parent);
        } catch (DataAccessException e) {
            return null;
        }
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
            post.setPath((Object[])path.getArray());
            Timestamp created = resultSet.getTimestamp("created");
            SimpleDateFormat format = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            post.setCreated(format.format(created));
            post.setId(resultSet.getLong("id"));
            return post;
        }
    }
}
