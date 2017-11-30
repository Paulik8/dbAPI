package bdapi.DAO;

import bdapi.models.Forum;
import bdapi.models.Post;
import bdapi.models.Thread;
import bdapi.models.Vote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

@Service
@Transactional
public class ThreadDAO {
    private static final ThreadMapper THREAD_MAPPER = new ThreadMapper();
    private static final VoteMapper VOTE_MAPPER = new VoteMapper();
    private static final PostMapper POST_MAPPER = new PostMapper();
    private final JdbcTemplate jdbc;

    @Autowired
    public ThreadDAO(JdbcTemplate jdbc) {this.jdbc = jdbc;}

//    public void createThread (Thread thread) {
//        final String SQL = "insert into \"threads\" (author, created, message, title) VALUES(?,?,?,?)";
//        jdbc.update(SQL, thread.getAuthor(), thread.getCreated(), thread.getMessage(), thread.getTitle());
//
//    }

    public void create(Thread thread) {
        Object[] object;
        String SQL__id = "insert into \"threads\" (author, message, slug, title, forum, votes, created) VALUES(?,?,?,?,?,?,?) returning id";
        object = new Object[]{thread.getAuthor(), thread.getMessage(), thread.getSlug(), thread.getTitle(), thread.getForum(), thread.getVotes(), thread.getCreated()};
        thread.setId(jdbc.queryForObject(SQL__id, object, Integer.class));

        final String SQL_UP_FORUM = "UPDATE \"forums\" SET threads = threads + 1 WHERE slug::citext = ?::citext";
        jdbc.update(SQL_UP_FORUM, thread.getForum());
    }

    public void change(Thread thread) {
        String SQL = "update \"threads\" set message = ?, title = ? where slug::citext = ?::citext";// + where
        jdbc.update(SQL, thread.getMessage(), thread.getTitle(), thread.getSlug());
    }


    public Thread getThreadBySlug (String slug) {
        try {
            String SQL = "SELECT * FROM \"threads\" WHERE slug::CITEXT = ?::CITEXT";
            return jdbc.queryForObject(SQL, THREAD_MAPPER, slug);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    public void createVote(Thread thread, Integer voice) {
        String SQL = "update \"threads\" set votes = votes + ? where id = ?";
        jdbc.update(SQL, voice, thread.getId());
    }

    public Thread getThreadById (Integer id) {
        try {
            String SQL = "select * from \"threads\" where id = ?";
            return jdbc.queryForObject(SQL, THREAD_MAPPER, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Thread> getThreadByForum (String forum) {
        String SQL = "select * from \"threads\" where forum::citext = ?::citext";
        return jdbc.query(SQL, THREAD_MAPPER, forum);
    }

    public List<Thread> getThreads (Forum forum, Integer limit, String since, Boolean flag) {
        String SQL = "select * from \"threads\" where forum::citext = ?::citext";
        List<Object> obj = new ArrayList<>();
        String slug = forum.getSlug();
        obj.add(slug);
        if (since != null) {
            if (flag != null && flag) {
                SQL += " AND created";
                SQL += " <= ?::timestamptz";
            }
            else {
                SQL += " AND created";
                SQL += " >= ?::timestamptz";
            }
            obj.add(since);
        }
        SQL += " order by created";
        if (flag != null && flag)
            SQL += " desc";
        if (limit != null) {
            SQL += " LIMIT ?";
            obj.add(limit);
        }

        return jdbc.query(SQL, THREAD_MAPPER, obj.toArray());
    }

    public void insertVote(Vote vote) {
        String SQL = "insert into \"votes\" (nickname, voice) VALUES(?, ?)";
        jdbc.update(SQL, vote.getNickname(), vote.getVoice());
    }

    public void updateVote(Vote vote) {
        String SQL = "update \"votes\" set voice = ? where nickname::citext = ?::citext";
        jdbc.update(SQL, vote.getVoice(), vote.getNickname());
    }

    public Vote getVotebyVote(Vote vote) {
        try {
            String SQL = "SELECT * FROM \"votes\" WHERE nickname::CITEXT = ?::CITEXT";
            return jdbc.queryForObject(SQL, VOTE_MAPPER, vote.getNickname());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Vote getVote(Vote vote) {
        String SQL = "select * from \"votes\" where nickname::citext = ?::citext";
        return jdbc.queryForObject(SQL, VOTE_MAPPER, vote.getNickname());
    }

    public List<Post> getPosts(long threadId, Integer limit, Integer since, String sort, Boolean desc) {
        List<Object> myObj = new ArrayList<>();
        if (sort == null || sort.equals("flat")) {
            String myStr = "select * from posts where thread = ?";
            myObj.add(threadId);
            if (since != null) {
                if (desc != null && desc) {
                    myStr += " and id < ?";
                } else {
                    myStr += " and id > ?";
                }
                myObj.add(since);
            }
            myStr += " order by created ";
            if (desc != null && desc) {
                myStr += " desc, id desc ";
            } else {
                myStr += ", id";
            }
            if (limit != null) {
                myStr += " limit ? ";
                myObj.add(limit);
            }

            List<Post> result = jdbc.query(myStr
                    , myObj.toArray(), POST_MAPPER);
            return result;
        } else if (sort.equals("tree")) {
            String myStr = "select * from posts where thread = ?";
            myObj.add(threadId);
            if (since != null) {
                if (desc != null && desc) {
                    myStr += " and path < (select path from posts where id = ?) ";
                } else {
                    myStr += " and path > (select path from posts where id = ?) ";
                }
                myObj.add(since);
            }
            myStr += " order by path ";
            if (desc != null && desc) {
                myStr += " desc, id desc ";
            }
            if (limit != null) {
                myStr += " limit ? ";
                myObj.add(limit);
            }

            List<Post> result = jdbc.query(myStr
                    , myObj.toArray(), POST_MAPPER);
            return result;

        } else {
            //WORKING HERE
            String myStr = "select * from posts where thread = ? ";
            myObj.add(threadId);
            if (since != null) {
                if (desc != null && desc) {
                    myStr += " and path[1] = ANY (select id from posts where parent = 0 and path < (select path from posts where id = ?) and thread = ? order by id desc limit ? ) ";

                } else {
                    myStr += " and path[1] = ANY (select id from posts where parent = 0 and path > (select path from posts where id = ?) and thread = ? order by id limit ? ) ";
                }
                myObj.add(since);
                myObj.add(threadId);
                myObj.add(limit);
            } else if (limit != null) {
                if (desc != null && desc) {
                    myStr += " and path[1] = ANY (select id  from posts where parent = 0 and thread = ? order by id desc limit ? ) ";
                } else {
                    myStr += " and path[1] = ANY (select id  from posts where parent = 0 and thread = ? order by id limit ? ) ";
                }
                myObj.add(threadId);
                myObj.add(limit);
            }
            myStr += " order by path ";
            if (desc != null && desc) {
                myStr += " desc ";
            }
            List<Post> result = jdbc.query(myStr
                    , myObj.toArray(), POST_MAPPER);
            return result;
        }


    }


    private static final class ThreadMapper implements RowMapper<Thread> {
        public Thread mapRow(ResultSet resultSet, int i) throws SQLException {
            Thread thread = new Thread();
            thread.setAuthor(resultSet.getString("author"));
            thread.setForum(resultSet.getString("forum"));
            thread.setTitle(resultSet.getString("title"));
            thread.setMessage(resultSet.getString("message"));
            thread.setSlug(resultSet.getString("slug"));
            thread.setCreated(resultSet.getTimestamp("created"));
//            Timestamp created = resultSet.getTimestamp("created");
//            SimpleDateFormat format = new SimpleDateFormat(
//                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
//            format.setTimeZone(TimeZone.getTimeZone("UTC"));//здесь надо, а в пост нет????????
//            thread.setCreated(format.format(created));
            //thread.setCreated(resultSet.getString("created"));
            thread.setId(resultSet.getInt("id"));
            thread.setVotes(resultSet.getInt("votes"));
            return thread;
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
            //post.setPath((Object[])resultSet.getObject("path"));
            Array path = resultSet.getArray("path");
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

    private static final class VoteMapper implements RowMapper<Vote> {
        public Vote mapRow(ResultSet resultSet, int i) throws SQLException {
            Vote vote = new Vote();
            vote.setNickname(resultSet.getString("nickname"));
            vote.setVoice(resultSet.getInt("voice"));
            return vote;
        }
    }
}
