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
        body.setForum(forum.getSlug());
        Integer res = threadDAO.create(body, forum, user);
        if (res == 409) {
            thread = threadDAO.getThreadBySlug(body.getSlug());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(thread);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping(path = "/{slug}/threads")
    public ResponseEntity getForum(@PathVariable(name="slug") String slug,
                                   @RequestParam(name="limit", required = false) Integer limit,
                                   @RequestParam(name="since", required = false) String since,
                                   @RequestParam(name="desc", required = false) Boolean desc) {
        List<Thread> thread;
        Forum forum;
        forum = forumDAO.getForumbySlug(slug);
        if (forum == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("cant find user"));
        }
        thread = threadDAO.getThreads(forum, limit, since, desc);
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
        users = userDAO.getUsers(forum.getId(), limit, since, desc);
        if (users == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("cant find users"));
        }
        return ResponseEntity.ok(users);
    }
}
