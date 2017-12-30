package bdapi.Controller;

import bdapi.DAO.PostDAO;
import bdapi.DAO.ThreadDAO;
import bdapi.DAO.UserDAO;
import bdapi.models.*;
import bdapi.models.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/thread")
public class ThreadController {
    private final ThreadDAO threadDAO;
    private final PostDAO postDAO;
    private final UserDAO userDAO;

    @Autowired
    public ThreadController(ThreadDAO threadDAO, PostDAO postDAO, UserDAO userDAO) {
        this.threadDAO = threadDAO;
        this.postDAO = postDAO;
        this.userDAO = userDAO;
    }

    public Thread CheckIdOrSlug (String slug_or_id) {
        Thread thread;
        if (slug_or_id.matches("\\d+")) {
            Integer id = Integer.parseInt(slug_or_id);
            thread = threadDAO.getThreadById(id);
        } else {
            thread = threadDAO.getThreadBySlug(slug_or_id);
        }
        return thread;
    }

    @PostMapping(path = "/{slug_or_id}/create")
    public ResponseEntity createPost(@PathVariable(name = "slug_or_id") String slug,
                                     @RequestBody List<Post> posts) {

        Thread thread;
        List<Post> newPosts;
            thread = CheckIdOrSlug(slug);
            if (thread == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("not found"));
        for (Post post : posts) {
            post.setForum(thread.getForum());
            post.setThread(thread.getId());
//            System.out.println(thread.getId());
//                    System.out.println(post.getThread());
            //if (postDAO.getAuthorByNickname(post.getAuthor()).size() == 0) {
            if (userDAO.getUserbyNickname(post.getAuthor()) == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("not found this author"));
            }
        }

        for (Post post : posts) {
            Post checkPost = postDAO.getPostbyId((int)post.getParent());
            Post check = postDAO.getChild(post.getParent());
            if ((checkPost == null || check == null) &&
                    post.getParent() != 0 || (check != null && check.getThread() != post.getThread())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new Message("conflict"));
            }
        }
        postDAO.create(posts);
        return ResponseEntity.status(HttpStatus.CREATED).body(posts);
    }

    @GetMapping(path = "{slug_or_id}/details")
    public ResponseEntity getThread(@PathVariable (name="slug_or_id") String slug_or_id) {
        Thread thread;
            thread = CheckIdOrSlug(slug_or_id);
            if (thread == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("cant find thread"));
            }
        return ResponseEntity.ok(thread);
    }

    @PostMapping(path = "{slug_or_id}/details")
    public ResponseEntity changePost (@PathVariable(name="slug_or_id") String slug_or_id,
                                      @RequestBody Thread body) {

        Thread thread;
            thread = CheckIdOrSlug(slug_or_id);
            if (thread == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("cant find thread"));
        if (body.getMessage() != null) {
            thread.setMessage(body.getMessage());
        }
        if (body.getTitle() != null) {
            thread.setTitle(body.getTitle());
        }
        threadDAO.change(thread);
        thread = CheckIdOrSlug(slug_or_id);
        return ResponseEntity.ok(thread);
    }


    @PostMapping(path = "/{slug_or_id}/vote")
    public ResponseEntity setVote(@PathVariable(name = "slug_or_id") String slug_or_id,
                                  @RequestBody Vote vote) {

        Thread thread;
        Integer flag = null;
        Boolean bool = null;
        thread = CheckIdOrSlug(slug_or_id);
        if (thread == null || userDAO.getUserbyNickname(vote.getNickname()) == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("cant find thread"));

        Vote checkVote = threadDAO.getVotebyVote(vote, thread);//голосовал ли он ранее

        if (checkVote != null && (vote.getVoice()).equals(checkVote.getVoice())) {
            return ResponseEntity.ok(threadDAO.getThreadById((int)thread.getId()));
        }
        if (checkVote == null) {
            flag = 1;
            bool = true;
        }
        if (checkVote != null && !(vote.getVoice().equals(checkVote.getVoice()))) {
            flag = 2;
            bool = false;
        }
            threadDAO.createVote(thread, vote.getVoice() * flag);//прибавление голоса в ветке или уменьшение
            threadDAO.insert_or_update_Vote(vote, thread, bool);
            //flag = 0;//не голосовал

//        if (checkVote != null && !(vote.getVoice().equals(checkVote.getVoice()))) {
//            flag = 2;
//            threadDAO.createVote(thread, vote.getVoice() * flag);//прибавление голоса в ветке или уменьшение
//            threadDAO.updateVote(vote, thread);
//           // flag = vote.getVoice();//да голосовал
//        }

        //Vote getvote = threadDAO.getVote(vote);
        //if (getvote.getVoice() != 0)

        return ResponseEntity.ok(CheckIdOrSlug(slug_or_id));
    }

    @GetMapping(path = "/{slug_or_id}/posts")
    public ResponseEntity getPosts(@PathVariable("slug_or_id") String slug_or_id,
                                   @RequestParam(value = "limit", required = false) Integer limit,
                                   @RequestParam(value = "sort", required = false) String sort,
                                   @RequestParam(value = "desc", required = false) boolean desc,
                                   @RequestParam(value = "since", required = false) Integer since
                                    ) {
        Thread thread;
        thread = CheckIdOrSlug(slug_or_id);
        if (thread == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("cant find thread"));
        }
        return ResponseEntity.ok(threadDAO.getPosts(thread.getId(), limit, since, sort, desc));
    }

}
