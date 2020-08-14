package kz.kcell.flow;

import kz.kcell.flow.UserTaskHistory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserTaskHistoryOutputDto {
    public Date assigned_date;
    public String assignee;
    public String operation;
    public String operation_responsible;


    public static UserTaskHistoryOutputDto fromUserTaskHistoryOutput(UserTaskHistory userTaskHistory) {

        UserTaskHistoryOutputDto dto = new UserTaskHistoryOutputDto();
        dto.assigned_date = userTaskHistory.getAssigned_date();
        dto.assignee = userTaskHistory.getAssignee();
        dto.operation = userTaskHistory.getOperation();
        dto.operation_responsible = userTaskHistory.getOperation_responsible();

        return dto;
    }

    public static List<UserTaskHistoryOutputDto> fromUserTaskHistoryOutputs(List<UserTaskHistory> userTaskHistory) {
        List<UserTaskHistoryOutputDto> UserTaskHistoryOutputDtos = new ArrayList<>();
        for (UserTaskHistory userTaskHistoryElement : userTaskHistory) {
            UserTaskHistoryOutputDtos.add(fromUserTaskHistoryOutput(userTaskHistoryElement));
        }

        return UserTaskHistoryOutputDtos;
    }

}
