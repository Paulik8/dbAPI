package bdapi.Controller;

import bdapi.DAO.ForumDAO;
import bdapi.DAO.PostDAO;
import bdapi.DAO.ThreadDAO;
import bdapi.DAO.UserDAO;
import bdapi.models.Forum;
import bdapi.models.Message;
import bdapi.models.Post;
import bdapi.models.PostFull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path ="/api/post")
public class PostController {
    final private PostDAO postDAO;
    final private UserDAO userDAO;
    final private ThreadDAO threadDAO;
    final private ForumDAO forumDAO;

    public PostController(UserDAO userDAO, PostDAO postDAO, ThreadDAO threadDAO, ForumDAO forumDAO) {
        this.userDAO = userDAO;
        this.postDAO = postDAO;
        this.threadDAO = threadDAO;
        this.forumDAO = forumDAO;
    }

    @GetMapping(path="/{id}/details")
    public ResponseEntity getDetails(@PathVariable(name="id") Integer id,
                                     @RequestParam(name="related", required = false) List<String> related) {
        Post post = postDAO.getDetails(id);
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("cant find post"));
        }
        PostFull postFull = new PostFull();
        postFull.setPost(post);

        if (related != null) {
            for (String item : related) {

                switch (item) {
                    case "user":
                        postFull.setAuthor(userDAO.getUserbyNickname(post.getAuthor()));
                        break;
                    case "thread":
                        postFull.setThread(threadDAO.getThreadById((int)(post.getThread())));
                        break;
                    case "forum":
                        postFull.setForum(forumDAO.getForumbySlug(post.getForum()));
                        break;
                    default:
                        return ResponseEntity.notFound().build();
                }
            }
        }
        return ResponseEntity.ok(postFull);
    }

    @PostMapping(path="/{id}/details")
    public ResponseEntity setDetails(@PathVariable(name="id") Integer id,
                                     @RequestBody Post body) {
        Post post = postDAO.getPostbyId(id);
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("cant find post"));
        }
        if (body.getMessage() == null || body.getMessage().equals(post.getMessage())) {
            return ResponseEntity.ok(post);
        }
        postDAO.changeMessage((int)post.getId(), body.getMessage());
        return ResponseEntity.ok(postDAO.getFinishPostbyId(id));
    }
}
