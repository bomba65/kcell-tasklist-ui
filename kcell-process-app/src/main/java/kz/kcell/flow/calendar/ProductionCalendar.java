package kz.kcell.flow.calendar;
import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ProductionCalendar {
    private static List holidaysForYear;
    private static List holidays;
    private static List weekendWorking;


    public ProductionCalendar(){
        holidaysForYear = Arrays.asList("07/01/2017", "20/03/2017", "07/07/2017", "01/09/2017", "07/01/2018", "09/03/2018", "30/04/2018",
            "08/05/2018", "21/08/2018", "31/08/2018", "31/12/2018", "07/01/2019", "10/05/2019", "07/01/2020", "03/01/2020",
            "08/05/2020", "31/07/2020", "18/12/2020");
        holidays = Arrays.asList("01/01", "02/01", "8/03", "21/03", "22/03", "23/03", "01/05", "07/05", "09/05", "06/07",
            "30/08", "01/12", "16/12", "17/12");
        weekendWorking = Arrays.asList("18/03/2017", "01/07/2017", "03/03/2018", "28/04/2018", "05/05/2018", "25/08/2018", "29/12/2018",
            "04/05/2019", "05/01/2020","11/05/2020", "20/12/2020");
    }
    public static Boolean checkWorkingDay(Date date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        boolean result = true;
        calendar.setTime(date);
        String formatDate = dateFormat.format(calendar.getTime());
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if(dayOfWeek == 7 || dayOfWeek == 1){
            result = false;
        }
        if(result){
            return !holidaysForYear.contains(formatDate) && !holidays.contains(formatDate.substring(0,5));
        }else{
            return weekendWorking.contains(formatDate);
        }
    }
}
