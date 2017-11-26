package bdapi.DAO;

import bdapi.models.Forum;
import bdapi.models.Thread;
import bdapi.models.Vote;
import org.apache.maven.settings.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.StyledEditorKit;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
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
        String SQL__id = "insert into \"threads\" (author, message, slug, title, forum, votes, created) VALUES(?,?,?,?,?,?,?::TIMESTAMP) returning id";
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

    private static final class ThreadMapper implements RowMapper<Thread> {
        public Thread mapRow(ResultSet resultSet, int i) throws SQLException {
            Thread thread = new Thread();
            thread.setAuthor(resultSet.getString("author"));
            thread.setForum(resultSet.getString("forum"));
            thread.setTitle(resultSet.getString("title"));
            thread.setMessage(resultSet.getString("message"));
            thread.setSlug(resultSet.getString("slug"));
            Timestamp created = resultSet.getTimestamp("created");
            SimpleDateFormat format = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));//здесь надо, а в пост нет????????
            thread.setCreated(format.format(created));
            //thread.setCreated(resultSet.getString("created"));
            thread.setId(resultSet.getInt("id"));
            thread.setVotes(resultSet.getInt("votes"));
            return thread;
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
