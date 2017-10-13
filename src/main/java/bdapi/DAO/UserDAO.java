package bdapi.DAO;

import bdapi.models.User;
import com.sun.org.apache.xerces.internal.impl.xpath.XPath;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDAO {
    JdbcTemplate jdbc;
    private static final UserMapper USER_MAPPER = new UserMapper();

    public void createUser(User user) {
        final String SQL = "insert into user (nickname, fullname, email, about) VALUES (?, ?, ?, ?)";
        jdbc.update(SQL, user.getNickname(), user.getFullname(), user.getEmail(), user.getAbout());
    }

    public User getUserbyNickname(String nickname) {
        final String SQL = "select * from user where nickname::citext = ?::citext";
        return jdbc.queryForObject(SQL, USER_MAPPER, nickname);
    }

    public void changeUser(User user) {
        final String SQL = "update user set" + "fullname = COALESCE(?, fullname)," + "email = COALESCE(?, email)," + "about = COALESCE(?, about)" + "where nickname::citext = ?::citext";
        jdbc.update(SQL, user.getFullname(), user.getEmail(), user.getAbout(), user.getNickname());
    }

    public List<User> getAlreadyUser(User user) {
        final String SQL = "select * from user where nickname::citext = ?::citext or email::citext = ?::citext";
        return jdbc.query(SQL, USER_MAPPER, user.getNickname(), user.getEmail());
    }

    private static final class UserMapper implements RowMapper<User> {
        public User mapRow(ResultSet resultSet, int i) throws SQLException {
            User user = new User();
            String username = resultSet.getString("nickname");
            String fullname = resultSet.getString("fullname");
            String email = resultSet.getString("email");
            String about = resultSet.getString("about");
            return user;
        }
    }
}
