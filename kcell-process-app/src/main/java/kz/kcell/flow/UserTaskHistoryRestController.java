package kz.kcell.flow;

import lombok.extern.java.Log;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.bind.annotation.RestController;

@Log
@RestController
@RequiredArgsConstructor
public class UserTaskHistoryRestController {
    private final UserTaskHistoryService userTaskHistoryService;

//    @RequestMapping(value = "/{taskId}", method = RequestMethod.GET)
//    public List<UserTaskHistoryOutputDto> getUserTaskHistory(@PathVariable("taskId") String taskId) throws Exception {
//        log.info("111");
//        log.info(taskId);
//        return userTaskHistoryService.getUserTaskHistoryByTaskId(taskId);
//    }

    @GetMapping(path = "/user-task-history/{taskId}")
    @Transactional
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public List<UserTaskHistoryOutputDto> getUserTaskHistory(@PathVariable("taskId") String taskId) throws Exception {
        log.info("111");
        log.info(taskId);
        return userTaskHistoryService.getUserTaskHistoryByTaskId(taskId);
    }


}
