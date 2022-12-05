package kz.kcell.flow.aftersales;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@Service("FinalStageStartDate")
@lombok.extern.java.Log

public class FinalStageStartDate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 5);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);

        if (dayOfWeek > 2 && dayOfWeek < 6) {
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth + 5 - dayOfWeek);
        } else if (dayOfWeek < 3) {
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth + 2 - dayOfWeek);
        } else {
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth + 9 - dayOfWeek);
        }

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        String startDate = formatter.format(c.getTime());

        delegateExecution.setVariable("startDate", startDate);
    }


}
