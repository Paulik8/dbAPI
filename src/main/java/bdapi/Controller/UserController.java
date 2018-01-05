package bdapi.Controller;

import bdapi.DAO.UserDAO;
import bdapi.models.Message;
import bdapi.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            User user = userDAO.getUserbyNickname(nick);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Cant find user"));
            };
            return ResponseEntity.ok(user);
        }

         @PostMapping(path = "/{nickname}/profile")
        public ResponseEntity setUserProfile(@PathVariable(name = "nickname") String nick,
                                         @RequestBody User body) {
            Integer res;
            body.setNickname(nick);
            User user = userDAO.getUserbyNickname(nick);
             if (user == null) {
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Cant find user"));
             }
            try {
                res = userDAO.changeUser(body);
            } catch (DuplicateKeyException e) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new Message("Conflict user"));
            }
            if (res == 202) {
                 return ResponseEntity.ok(user);
            }
             return ResponseEntity.ok(userDAO.getUserbyNickname(nick));
        }
}
