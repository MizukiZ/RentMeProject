package Helper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class timeFormat {

    private Calendar calendar;
    private String formatData;

    public  timeFormat(String timeStampA){
        calendar = GregorianCalendar.getInstance();

        Long timeStamp = Long.valueOf(timeStampA);
        calendar.setTimeInMillis(timeStamp);
    }


    public String postedOnForm(){

        SimpleDateFormat dataFormat = new SimpleDateFormat("dd-MM-yyyy",Locale.US);
        formatData = dataFormat.format(calendar.getTime());

     return formatData;
    }
}
