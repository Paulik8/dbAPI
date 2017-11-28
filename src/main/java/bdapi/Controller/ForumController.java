package bdapi.Controller;

import bdapi.DAO.ForumDAO;
import bdapi.DAO.PostDAO;
import bdapi.DAO.ThreadDAO;
import bdapi.DAO.UserDAO;
import bdapi.models.*;
import bdapi.models.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/forum")

public class ForumController {

    private final ForumDAO forumDAO;
    private final UserDAO userDAO;
    private final ThreadDAO threadDAO;
    private final PostDAO postDAO;

    @Autowired
    ForumController(UserDAO userDAO, ForumDAO forumDAO, ThreadDAO threadDAO, PostDAO postDAO) {
        this.forumDAO = forumDAO;
        this.userDAO = userDAO;
        this.threadDAO = threadDAO;
        this.postDAO = postDAO;
    }

    @PostMapping(path = "/create")
    public ResponseEntity create(@RequestBody Forum forum) {
        User user;
        user = userDAO.getUserbyNickname(forum.getUser());
//        try {
//            user = userDAO.getUserbyNickname(forum.getUser());
//        } catch (DataAccessException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Cant find user"));
//        }
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Cant find user"));
        }
        try {
            forum.setUser(user.getNickname());
            forumDAO.createForum(forum);
        }
        catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(forumDAO.getForumbySlug(forum.getSlug()));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(forum);
    }

    @GetMapping(path = "/{slug}/details")
    public ResponseEntity getForum(@PathVariable(name = "slug") String ForumSlug) {
        Forum forum;
            forum = forumDAO.getForumbySlug(ForumSlug);
            if (forum ==null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("cant find forum"));
            }
        return ResponseEntity.ok(forum);
    }

    @PostMapping(path = "/{slug}/create")
    public ResponseEntity createSlug(@PathVariable(name = "slug") String ForumSlug,
                                     @RequestBody Thread body) {
        User user;
        Forum forum;
        Thread thread;
            user = userDAO.getUserbyNickname(body.getAuthor());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("cant find author or forum"));
            }
            forum = forumDAO.getForumbySlug(ForumSlug);
            if (forum == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("cant find author or forum"));
            }
        try {
            body.setForum(forum.getSlug());
            threadDAO.create(body);
        }   catch (DuplicateKeyException e) {
            thread = threadDAO.getThreadBySlug(body.getSlug());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(thread);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping(path = "/{slug}/threads")
    public ResponseEntity getForum(@PathVariable(name="slug") String slug,
                                   //@RequestBody int limit,
                                   //@RequestBody String since,
                                   //@RequestBody boolean desc) {
                                   @RequestParam(name="limit", required = false) Integer limit,
                                   @RequestParam(name="since", required = false) String since,
                                   @RequestParam(name="desc", required = false) Boolean desc) {
        List<Thread> thread;
        Forum forum;
        forum = forumDAO.getForumbySlug(slug);
            //forum.setSlug(slug);
        if (forum == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("cant find user"));
        }
//        try {
//            thread = threadDAO.getThreads(slug, limit, since, desc);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.notFound().build();
//        }
        thread = threadDAO.getThreads(forum, limit, since, desc);
        //String ForumSlug = forum.getSlug();

        return ResponseEntity.ok(thread);

    }

    @GetMapping(path="{slug}/users")
    public ResponseEntity getUsers (@PathVariable(name="slug") String slug,
                                    @RequestParam(value = "limit", required = false, defaultValue = "100") Integer limit,
                                    @RequestParam(value = "since", required = false) String since,
                                    @RequestParam(value = "desc", required = false, defaultValue = "false") Boolean desc) {
        Forum forum;
        List<User> users;
        forum = forumDAO.getForumbySlug(slug);
        if (forum == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("cant find forum"));
        users = userDAO.getUsers(forum.getSlug(), limit, since, desc);
        if (users == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("cant find users"));
        }
//        threads = threadDAO.getThreadByForum(forum);//Лист веток форума
//        for (Thread thread: threads) {
//            ThreadUser = thread.getAuthor();// может быть добавить условие if thread.getSlug != null
//            if (!ThreadUsers.contains(userDAO.getUserbyNickname(ThreadUser))) {
//                ThreadUsers.add(userDAO.getUserbyNickname(ThreadUser));//массив пользователей у которых есть ветка
//            }
//        }
//        posts = postDAO.getPostByForum(forum);//Лист постов форума
//        for (Post post : posts) {
//            PostUser = post.getAuthor();
//            if (!PostUsers.contains(userDAO.getUserbyNickname(PostUser))) {
//                PostUsers.add(userDAO.getUserbyNickname(PostUser));//массив пользователей у которых есть пост на форуме
//            }
//        }
//
//        for (User user : ThreadUsers) {
//            if (!PostUsers.contains(user)) {
//                PostUsers.add(user);
//            }
//        }
        return ResponseEntity.ok(users);
    }
}
