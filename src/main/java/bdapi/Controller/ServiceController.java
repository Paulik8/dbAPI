package bdapi.Controller;

import bdapi.DAO.ServiceDAO;
import bdapi.models.Message;
import bdapi.models.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(path = "/api/service")
public class ServiceController {

    private ServiceDAO serviceDAO;

    public ServiceController(ServiceDAO serviceDAO) {
        this.serviceDAO = serviceDAO;
    }

    @GetMapping(path = "/status")
    public ResponseEntity getCount() {
        Status status = new Status();

        if (serviceDAO.getCountForums() == 0) {
            status.setForum(0);
        } else {
            status.setForum(serviceDAO.getCountForums());
        }

        if (serviceDAO.getCountUsers() == 0) {
            status.setUser(0);
        } else {
            status.setUser(serviceDAO.getCountUsers());
        }

        if (serviceDAO.getCountThreads() == 0) {
            status.setThread(0);
        } else {
            status.setThread(serviceDAO.getCountThreads());
        }

        if (serviceDAO.getCountPosts() == 0) {
            status.setPost(0);
        } else {
            status.setPost(serviceDAO.getCountPosts());
        }

        return ResponseEntity.ok(status);
    }

    @PostMapping(path = "/clear")
    public ResponseEntity clear() {
        serviceDAO.clear();
        return ResponseEntity.ok(new Message("clear successfully"));
    }
}
