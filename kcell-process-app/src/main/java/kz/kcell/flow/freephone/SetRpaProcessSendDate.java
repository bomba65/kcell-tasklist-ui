package kz.kcell.flow.freephone;

import kz.kcell.flow.calendar.ProductionCalendar;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@Log
public class SetRpaProcessSendDate implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 6);
        calendar.add(Calendar.DATE,1);
        while(!ProductionCalendar.checkWorkingDay(calendar.getTime())){
            calendar.add(Calendar.DATE,1);
        }
        calendar.set(Calendar.HOUR_OF_DAY,8);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND,0);
        calendar.add(Calendar.HOUR_OF_DAY, -6);
        execution.setVariable("rpa_params_send_process_date", calendar.getTime());
    }
}
