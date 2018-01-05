package bdapi.DAO;

import bdapi.models.*;
import bdapi.models.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
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

@Service
@Transactional
public class ThreadDAO {
    private static final ThreadMapper THREAD_MAPPER = new ThreadMapper();
    private static final VoteMapper VOTE_MAPPER = new VoteMapper();
    private static final PostMapper POST_MAPPER = new PostMapper();
    private final JdbcTemplate jdbc;

    @Autowired
    public ThreadDAO(JdbcTemplate jdbc) {this.jdbc = jdbc;}

    public Integer create(Thread thread, Forum forum, User user) {
        Object[] object;
        String SQL__id = "insert into \"threads\" (author, message, slug, title, forum, forumid, votes, created) VALUES(?,?,?,?,?,?,?, ?) returning id";
        object = new Object[]{thread.getAuthor(), thread.getMessage(), thread.getSlug(), thread.getTitle(), thread.getForum(), forum.getId(), thread.getVotes(), thread.getCreated()};
        try {
            thread.setId(jdbc.queryForObject(SQL__id, object, Integer.class));
        } catch (DuplicateKeyException e) {
            return 409;
        }
        jdbc.update("INSERT INTO users_forum (forumid, nickname, fullname, email, about) VALUES (?,?,?,?,?) ON CONFLICT (forumid, nickname) DO NOTHING", forum.getId(), user.getNickname(), user.getFullname(), user.getEmail(), user.getAbout());

        final String SQL_UP_FORUM = "UPDATE \"forums\" SET threads = threads + 1 WHERE slug::citext = ?::citext";
        jdbc.update(SQL_UP_FORUM, thread.getForum());
        return 201;
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

    public List<Thread> getThreads (Forum forum, Integer limit, String since, Boolean flag) {
        String SQL = "select * from \"threads\" where forumid = ?";
        List<Object> obj = new ArrayList<>();
        obj.add(forum.getId());
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

    public void insert_or_update_Vote(Vote vote, Thread thread) {
            String SQL = "insert into \"votes\" (nickname, voice, threadid) VALUES(?, ?, ?)";
            jdbc.update(SQL, vote.getNickname(), vote.getVoice(), thread.getId());
    }

    public void updateVote(Vote vote, Thread thread) {
        String SQL = "update \"votes\" set voice = ? where nickname::citext = ?::citext and threadid = ?";
        jdbc.update(SQL, vote.getVoice(), vote.getNickname(), thread.getId());
    }

    public Vote getVotebyVote(Vote vote, Thread thread) {
        try {
            String SQL = "SELECT * FROM \"votes\" WHERE nickname::CITEXT = ?::CITEXT and threadid = ?";
            return jdbc.queryForObject(SQL, VOTE_MAPPER, vote.getNickname(), thread.getId());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }


    public List<Post> getPosts(long threadId, Integer limit, Integer since, String sort, Boolean desc) {
        List<Object> myObj = new ArrayList<>();
        String asc = desc ? "<" : ">";
        String myStr = "";
        if (sort == null)
            sort = "flat";
        switch (sort) {
            case "flat":
                myStr = "select * from posts where thread = ? ";
                myObj.add(threadId);
                if (since != null) {
                    myStr += " and id " + asc + "?";
                    myObj.add(since);
                }
                myStr += " order by created ";
                if (desc != null && desc) {
                    myStr += " desc, id desc";
                } else {
                    myStr += ", id";
                }
                if (limit != null) {
                    myStr += " limit ? ";
                    myObj.add(limit);
                }
                break;

            case "tree":
                myStr = "select * from posts where thread = ? ";
                myObj.add(threadId);
                if (since != null) {
                    myStr += " and path" + asc +" (select path from posts where id = ?) ";
                    myObj.add(since);
                }
                myStr += " order by path ";
                if (desc != null && desc) {
                    myStr += " desc";
                }
                if (limit != null) {
                    myStr += " limit ? ";
                    myObj.add(limit);
                }
                break;
            case "parent_tree":
                myStr += "select p.* from posts as p join";
                if (since != null) {
                    myStr += " (select id from posts where thread = ? and parent = 0 and path" + asc + " (select path from posts where id = ?) order by id " + ((desc != null && desc) ? " desc " : "") + " limit ? ) as z on z.id = p.path[1]";
                    myObj.add(threadId);
                    myObj.add(since);
                    myObj.add(limit);
                } else if (limit != null) {
                        myStr += "(select id from posts where thread = ? and parent = 0 order by id " + ((desc != null && desc) ? " desc ": "") + " limit ?) as z on z.id = p.path[1]";
                        myObj.add(threadId);
                        myObj.add(limit);
                    }
                    myStr += " order by path ";
                    if (desc != null && desc) {
                        myStr += " desc ";
                    }
                    break;
            }
        return jdbc.query(myStr, myObj.toArray(), POST_MAPPER);
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
            thread.setId(resultSet.getInt("id"));
            thread.setForumid(resultSet.getInt("forumid"));
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

    private static final class VoteMapper implements RowMapper<Vote> {
        public Vote mapRow(ResultSet resultSet, int i) throws SQLException {
            Vote vote = new Vote();
            vote.setNickname(resultSet.getString("nickname"));
            vote.setVoice(resultSet.getInt("voice"));
            vote.setId(resultSet.getInt("id"));
            return vote;
        }
    }
}
