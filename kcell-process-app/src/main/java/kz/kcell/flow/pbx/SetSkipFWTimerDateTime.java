package kz.kcell.flow.pbx;


import kz.kcell.flow.calendar.ProductionCalendar;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

import java.util.Calendar;
import java.util.Date;

public class SetSkipFWTimerDateTime implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        Date today = Calendar.getInstance().getTime();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY,3);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        Date nineAMTime = cal.getTime();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 6);
        calendar.add(Calendar.DATE, today.after(nineAMTime) ? 0 : 1);


        while(!ProductionCalendar.checkWorkingDay(calendar.getTime())){
            calendar.add(Calendar.DATE,1);
        }
        calendar.set(Calendar.HOUR_OF_DAY,9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND,0);
        calendar.add(Calendar.HOUR_OF_DAY, -6);
        execution.setVariable("skipFWTimerDateTime", calendar.getTime());
    }
}
