package bdapi.DAO;

import bdapi.models.Message;
import bdapi.models.Thread;
import bdapi.models.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.crypto.Data;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class PostDAO {
    private final JdbcTemplate jdbc;
    private final UserDAO userDAO;
    private static final PostMapper POST_MAPPER = new PostMapper();

    @Autowired
    public PostDAO(JdbcTemplate jdbc, UserDAO userDAO) {
        this.jdbc = jdbc;
        this.userDAO = userDAO;
    }


    public int create (List<Post> posts, Thread thread) throws SQLException {
        //GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        //Object[] object;
        //Integer ind = 0;
        //Long result;
        try (Connection con = jdbc.getDataSource().getConnection();
             //PreparedStatement pst;
//        //jdbc.update(con -> {
             PreparedStatement pst = con.prepareStatement(
                     "INSERT INTO \"posts\" (id, author, forum, isedited, message, thread, parent, created, path)" +
                             " VALUES(?,?,?,?,?,?,?,?::TIMESTAMP,?)")) {

            //String SQL = "insert into \"posts\" (author, forum, isedited, message, thread, parent, created, id, path) VALUES(?,?,?,?,?,?,?::timestamp,?, ?)";
            //Long ids = jdbc.queryForObject("SELECT nextval('posts_id_seq')", Long.class);
            //System.out.print(ids + "ku" + "\n");
            for (Post post : posts) {
                //result = ids + ind;
                ArrayList arrObj = new ArrayList<Object>();
                post.setForum(thread.getForum());
                post.setThread(thread.getId());
//            System.out.println(thread.getId());
//                    System.out.println(post.getThread());
                //if (postDAO.getAuthorByNickname(post.getAuthor()).size() == 0) {
                if (userDAO.getUserbyNickname(post.getAuthor()) == null) {
                    return 404;
                }
                Post parentPost = getPostbyId((int) post.getParent());
                Post check = getChild(post.getParent());
                if ((parentPost == null || check == null) &&
                        post.getParent() != 0 || (check != null && check.getThread() != post.getThread())) {
                    return 409;
                }
                //Post parentPost = getPostbyId((int)post.getParent());
                post.setCreated(posts.get(0).getCreated());
//            //
                //Long idx = jdbc.queryForObject("SELECT nextval('posts_id_seq')", Long.class);
                //List<Long> ids = jdbc.query("select nextval('posts_id_seq') from generate_series(1, ?)", new Object[]{posts.size()}, (rs, rowNum) -> rs.getLong(1));
                //post.setId(idx);
                Long ids = jdbc.queryForObject("SELECT nextval('posts_id_seq')", Long.class);
                if (post.getParent() == 0) {
                    //Long id = jdbc.queryForObject("select nextval('posts_id_seq')", Long.class);
                    ArrayList arr = new ArrayList<>(Arrays.asList(ids));
                    //arr.add(ids.get(i));
                    arrObj = arr;
                    //System.out.print(ids + "ids" + "\n");
                    //pst.setArray(1, con.createArrayOf("int", arr.toArray()));
                } else {
                    ArrayList arr = new ArrayList<>(Arrays.asList(parentPost.getPath()));
                    //arr.add(parentPost.getPath());
                    //System.out.print(ids + "ids" + "\n");
                    arr.add(ids);
                    arrObj = arr;
                    //pst.setArray(1, con.createArrayOf("int", arr.toArray()));
                }

                post.setId(ids);
                post.setPath(arrObj.toArray());
                ArrayList finalArrObj = arrObj;
                //Integer finalInd = ind;
                //Long finalResult = result;
//            jdbc.batchUpdate(SQL, new BatchPreparedStatementSetter() {
//                @Override
//                public void setValues(PreparedStatement pst, int i) throws SQLException {
//                    System.out.print(finalResult+"final"+"\n");
//
//                    pst.setString(1, post.getAuthor());
//                    pst.setString(2, post.getForum());
//                    pst.setBoolean(3, post.getIsEdited());
//                    pst.setString(4, post.getMessage());
//                    pst.setLong(5, post.getThread());
//                    pst.setLong(6, post.getParent());
//                    pst.setString(7, post.getCreated());
//                    pst.setLong(8, finalResult);
//                    pst.setArray(9, con.createArrayOf("int", finalArrObj.toArray()));
//
//                }
//
//                @Override
//                public int getBatchSize() {
//                    return posts.size();
//                }
//            });
//                jdbc.update(con -> {
//                    PreparedStatement pst = con.prepareStatement(
//                            "INSERT INTO posts (author, forum, isedited, message, thread, parent, created, path)" +
//                                    " VALUES(?,?,?,?,?,?,?::TIMESTAMP,?) RETURNING id");
                pst.setLong(1, post.getId());
                pst.setString(2, post.getAuthor());
                pst.setString(3, post.getForum());
                pst.setBoolean(4, post.getIsEdited());
                pst.setString(5, post.getMessage());
                pst.setLong(6, post.getThread());
                pst.setLong(7, post.getParent());
                pst.setString(8, post.getCreated());
                pst.setArray(9, con.createArrayOf("int", finalArrObj.toArray()));
                // return pst;
                // });

            pst.addBatch();


                //post.setPath(arrObj);
                //object = new Object[] {post.getAuthor(), post.getForum(), post.getIsEdited(), post.getMessage(), post.getThread(), post.getParent(), post.getCreated()};
                //post.setId(jdbc.queryForObject(SQL, object, Long.class));
                //post.setId(jdbc.queryForObject(SQL, POST_MAPPER, post.getAuthor(), post.getForum(), post.getIsEdited(), post.getMessage(), post.getThread(), post.getParent(), post.getCreated()));

                //setPath(parentPost, post);
                //ind++;
            }
            final String SQL_posts = "UPDATE \"forums\" SET posts = posts + ? WHERE slug::CITEXT = ?::CITEXT";
            jdbc.update(SQL_posts, posts.size(), thread.getForum());
            pst.executeBatch();
            con.close();
            return 201;
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
    private void setPath(Post parentPost, Post childPost) {
        jdbc.update(con -> {
            PreparedStatement pst = con.prepareStatement(
                    "update posts set" +
                            "  path = ? " +
                            "where id = ?");
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
        });

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


    public Post getPostbyId(Integer id) {
        try {
            String SQL = "SELECT * FROM \"posts\" WHERE id = ?";
            return jdbc.queryForObject(SQL, POST_MAPPER, id);
        } catch (DataAccessException e) {
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
