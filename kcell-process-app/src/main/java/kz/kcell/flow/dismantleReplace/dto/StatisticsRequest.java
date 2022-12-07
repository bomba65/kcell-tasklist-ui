package kz.kcell.flow.dismantleReplace.dto;

import lombok.Value;

import java.util.Date;

@Value
public class StatisticsRequest {
    String processInstanceId;
    String businessKey;
    String siteName;
    Date endTime;
}
