package bdapi.DAO;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServiceDAO {

    private final JdbcTemplate jdbc;

    public ServiceDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int getCountUsers() {
        try {
            String SQL = "SELECT count(*) FROM \"users\"";
            return jdbc.queryForObject(SQL, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    public int getCountForums() {
        try {
            String SQL = "SELECT count(*) FROM \"forums\"";
            return jdbc.queryForObject(SQL, Integer.class);
        }
        catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }


    public int getCountThreads() {
        try {
            String SQL = "SELECT count(*) FROM \"threads\"";
            return jdbc.queryForObject(SQL, Integer.class);
        }
        catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    public int getCountPosts() {
        try {
            String SQL = "SELECT count(*) FROM \"posts\"";
            return jdbc.queryForObject(SQL, Integer.class);
        }
        catch (Exception e) {
            return 0;
        }
    }

    public int getCountUsersForum() {
        try {
            String SQL = "SELECT count(*) FROM \"users_forum\"";
            return jdbc.queryForObject(SQL, Integer.class);
        }
        catch (Exception e) {
            return 0;
        }
    }

    public void clear() {
        String SQL = "truncate table users, forums, threads, posts, votes, users_forum";
        jdbc.execute(SQL);
    }

}
