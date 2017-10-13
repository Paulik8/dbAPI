package bdapi.DAO;

import bdapi.models.Forum;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ForumDAO {
    private static final ForumMapper FORUM_MAPPER = new ForumMapper();

    JdbcTemplate jdbc;

    public void createForum(Forum forum) {
        String SQL = "insert into forum (slug, title, user) VALUES (?, ?, ?);";
        jdbc.update(SQL, forum.getSlug(), forum.getTitle(), forum.getUser());
    }

    public Forum getForumbySlug(String slug) {
        String SQL = "select * from forum where slug::citext = ?::citext";
        return jdbc.queryForObject(SQL, FORUM_MAPPER, slug);
    }

    private static final class ForumMapper implements RowMapper<Forum> {
        public Forum mapRow(ResultSet resultSet, int i) throws SQLException {
            Forum forum = new Forum();
            String slug = resultSet.getString("slug");
            String title = resultSet.getString("title");
            String user = resultSet.getString("user");
            Long posts = resultSet.getLong("posts");
            Integer threads = resultSet.getInt("threads");
            return forum;
        }
    }
}
