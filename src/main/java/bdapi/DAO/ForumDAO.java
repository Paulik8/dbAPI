package bdapi.DAO;

import bdapi.models.Forum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;

@Service
@Transactional
public class ForumDAO {
    private static final ForumMapper FORUM_MAPPER = new ForumMapper();
    private final JdbcTemplate jdbc;

    @Autowired
    public ForumDAO(JdbcTemplate jdbc) {this.jdbc = jdbc;}

    public void createForum(Forum forum) {
        String SQL = "insert into \"forums\" (slug, title, creator) VALUES (?, ?, ?);";
        jdbc.update(SQL, forum.getSlug(), forum.getTitle(), forum.getUser());
    }

    public Forum getForumbySlug(String slug) {
        try {
            String SQL = "select * from \"forums\" where slug::citext = ?::citext";
            return jdbc.queryForObject(SQL, FORUM_MAPPER, slug);
        } catch (DataAccessException e) {
            return null;
        }
    }

    private static final class ForumMapper implements RowMapper<Forum> {
        public Forum mapRow(ResultSet resultSet, int i) throws SQLException {
            Forum forum = new Forum();
            forum.setId(resultSet.getInt("id"));
            forum.setSlug(resultSet.getString("slug"));
            forum.setTitle(resultSet.getString("title"));
            forum.setUser(resultSet.getString("creator"));
            forum.setPosts(resultSet.getLong("posts"));
            forum.setThreads(resultSet.getInt("threads"));
            return forum;
        }
    }
}
