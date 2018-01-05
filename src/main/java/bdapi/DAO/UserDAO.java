package bdapi.DAO;

import bdapi.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class UserDAO{
    private final JdbcTemplate jdbc;

    @Autowired
    public UserDAO(JdbcTemplate jdbc){
        this.jdbc = jdbc;
    }

    private static final UserMapper USER_MAPPER = new UserMapper();

    public void createUser(final User user) {
        final String SQL = "insert into users (nickname, fullname, email, about) VALUES (?, ?, ?, ?)";
        jdbc.update(SQL, user.getNickname(), user.getFullname(), user.getEmail(), user.getAbout());
    }

    public User getUserbyNickname(String nickname) {
        try {
            final String SQL = "select * from users where nickname::citext = ?::citext";
            return jdbc.queryForObject(SQL, USER_MAPPER, nickname);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<User> getUsers (Integer id, Integer limit, String since, Boolean desc) {
        try {
            List<Object> obj = new ArrayList<>();
            //String SQL =  "select u.* from users u join users_forum  uf on uf.forum = ? and uf.nick = u.nickname";
            String SQL = "select uf.nickname, uf.fullname, uf.email, uf.about from users_forum uf where uf.forumid = ?";
            obj.add(id);
            if (since != null) {
                if (desc != null && desc) {
                    SQL +=  " and uf.nickname::citext < ?::citext ";
                } else {
                    SQL +=  " and uf.nickname::citext > ?::citext ";
                }
                obj.add(since);
            }

            SQL += " order by uf.nickname ";
            if (desc != null && desc) {
                SQL += " desc ";
            }
            if (limit != null) {
                SQL +=  " LIMIT ?";
                obj.add(limit);
            }
            SQL += ";";
            return jdbc.query(SQL, obj.toArray(), USER_MAPPER);

        } catch (DataAccessException e) {
            return null;
        }
    }

    public void changeUser(User user) {
        final String SQL = "update \"users\" set" + " fullname = COALESCE(?, fullname)," + "email = COALESCE(?, email)," + "about = COALESCE(?, about)" + "where nickname::citext = ?::citext";
        jdbc.update(SQL, user.getFullname(), user.getEmail(), user.getAbout(), user.getNickname());
    }

    public List<User> getAlreadyUser(final User user) {
        final String SQL = "select * from users where LOWER(nickname) = LOWER(?) OR LOWER(email) = LOWER(?)";
        return jdbc.query(SQL, USER_MAPPER, user.getNickname(), user.getEmail());
    }

private static final class UserMapper implements RowMapper<User> {
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        final User user = new User();
        user.setNickname(rs.getString("nickname"));
        user.setFullname(rs.getString("fullname"));
        user.setEmail(rs.getString("email"));
        user.setAbout(rs.getString("about"));
        return user;
    }
}
}
