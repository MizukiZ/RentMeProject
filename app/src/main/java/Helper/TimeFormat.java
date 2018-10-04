package Helper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class TimeFormat {

    private Calendar calendar;
    private String formatData;

    public TimeFormat(String timeStampA){
        calendar = GregorianCalendar.getInstance();

        Long timeStamp = Long.valueOf(timeStampA);
        calendar.setTimeInMillis(timeStamp);
    }


    public String postedOnForm(){

        SimpleDateFormat dataFormat = new SimpleDateFormat("dd-MM-yyyy",Locale.US);
        formatData = dataFormat.format(calendar.getTime());

     return formatData;
    }

    public String messageForm(){

        SimpleDateFormat dataFormat = new SimpleDateFormat("HH:mm dd/MM ",Locale.US);
        formatData = dataFormat.format(calendar.getTime());

        return formatData;
    }
}
