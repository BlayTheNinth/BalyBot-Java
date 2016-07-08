package net.blay09.balybot.script.binding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateBinding {

    public String getTimeZoneName(String timeZone) {
        TimeZone object = TimeZone.getTimeZone(timeZone);
        if(object != null) {
            return object.getDisplayName();
        }
        return null;
    }

    public String format(long utc, String format, String timeZone) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
        return dateFormat.format(new Date(utc));
    }

}
