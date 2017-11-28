package bdapi.Controller;

import bdapi.DAO.UserDAO;
import bdapi.models.Message;
import bdapi.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(path = "/api/user")

public class UserController {
        private final UserDAO userDAO;

        @Autowired
        UserController(UserDAO userDAO) {
            this.userDAO = userDAO;
        }

        @PostMapping(path = "/{nickname}/create")
        public ResponseEntity create(@PathVariable(name = "nickname")final String nick,
                                     @RequestBody User user) {
            user.setNickname(nick);

            try {
                userDAO.createUser(user);

            } catch (DuplicateKeyException e) {
                List<User> duplicate = userDAO.getAlreadyUser(user);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(duplicate);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        }

        @GetMapping(path = "/{nickname}/profile")
        public ResponseEntity getUserProfile(@PathVariable(name = "nickname") String nick) {
            //User user;
//            try {
//                user = userDAO.getUserbyNickname(nick);
//            } catch (EmptyResultDataAccessException e) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Cant find User"));
//            }
            if (userDAO.getUserbyNickname(nick) == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Cant find user"));
            };
            return ResponseEntity.ok(userDAO.getUserbyNickname(nick));
        }

         @PostMapping(path = "/{nickname}/profile")
        public ResponseEntity setUserProfile(@PathVariable(name = "nickname") String nick,
                                         @RequestBody User body) {
            User user;
            body.setNickname(nick);
//            try {
//                userDAO.getUserbyNickname(nick);
//            } catch (DataAccessException e) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Cant find user"));
//            }
             if (userDAO.getUserbyNickname(nick) == null) {
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Cant find user"));
             }
            try {
                userDAO.changeUser(body);
            } catch (DuplicateKeyException e) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new Message("Conflict user"));
            }
//            catch (DataAccessException e) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Cant find user"));
//            }
             return ResponseEntity.ok(userDAO.getUserbyNickname(nick));
        }
}
