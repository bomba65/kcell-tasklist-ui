package kz.kcell.flow;

import javassist.tools.rmi.ObjectNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.util.List;

@Log
@Service
@RequiredArgsConstructor
public class UserTaskHistoryService {
    private final UserTaskHistoryRepository userTaskHistoryRepository;

    public List<UserTaskHistoryOutputDto> getUserTaskHistoryByTaskId(String taskId) throws Exception {
        List<UserTaskHistory> userTaskHistoryList = userTaskHistoryRepository.findByTaskId(taskId);
        if (userTaskHistoryList.size() == 0) {
            throw new ObjectNotFoundException("History for user task with task_id_ = " + taskId + " not found");
        }

        return UserTaskHistoryOutputDto.fromUserTaskHistoryOutputs(userTaskHistoryList);
    }

}
