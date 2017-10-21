package bdapi.Controller;

import bdapi.DAO.UserDAO;
import bdapi.models.Message;
import bdapi.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
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
        public ResponseEntity create(@PathVariable(name = "nickname") final String nickname,
                                     @RequestBody User newUser) {
            newUser.setNickname(nickname);

            try {
                userDAO.createUser(newUser);
            } catch (DuplicateKeyException e) {
                List<User> duplicates = userDAO.getAlreadyUser(newUser);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(duplicates);
            } catch (DataAccessException e) {
                e.printStackTrace();
                return ResponseEntity.notFound().build();
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
        }
//        public ResponseEntity<?> create(@PathVariable(name = "nickname")final String nick,
//                                     @RequestBody User user) {
//            user.setNickname(nick);
//
//            try {
//                userDAO.createUser(user);
//                return ResponseEntity.status(HttpStatus.CREATED).body(user);
//            }
//                catch (DuplicateKeyException e) {
//                List<User> duplicate = userDAO.getAlreadyUser(user);
//                return ResponseEntity.status(HttpStatus.CONFLICT).body(duplicate);
//            }
//            catch (Exception e) {
//                e.printStackTrace();
//                return ResponseEntity.notFound().build();
//            }
////                User u1 = userDAO.getUserbyNickname(nick);
////                System.out.println(u1);
////                User u2 = userDAO.getUserbyEmail(user.getEmail());
////                System.out.println(u2);
////                if (u1 == null) {
////                    User[] usrs = {u2};
////                    return ResponseEntity.status(HttpStatus.CONFLICT).body(usrs);
////                }
////                if (u2 == null || u2.getEmail().equals(u1.getEmail())) {
////                    User[] usrs = {u1};
////                    return ResponseEntity.status(HttpStatus.CONFLICT).body(usrs);
////                }
////                if (!u2.getEmail().equals(u1.getEmail())) {
////                    User[] usrs = {u1, u2};
////                    return ResponseEntity.status(HttpStatus.CONFLICT).body(usrs);
////                }
////            }
////            return ResponseEntity.status(HttpStatus.CREATED).body(user);
//        }

        @GetMapping(path = "/{nickname}/profile")
        public ResponseEntity getUserProfile(@PathVariable(name = "nickname") String nick) {
            User user;
            try {
                user = userDAO.getUserbyNickname(nick);
            } catch (EmptyResultDataAccessException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Cant find User"));
            }
            return ResponseEntity.ok(user);
        }

         @PostMapping(path = "/{nickname}/profile")
        public ResponseEntity setUserProfile(@PathVariable(name = "nickname") String nick,
                                         @RequestBody User body) {
            body.setNickname(nick);
            try {
                userDAO.changeUser(body);
            } catch (EmptyResultDataAccessException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Cant find user"));
            } catch (DuplicateKeyException e) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new Message("Conflict user"));
            }
            return ResponseEntity.ok(body);
        }
}
