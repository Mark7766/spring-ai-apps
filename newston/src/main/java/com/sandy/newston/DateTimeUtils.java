package com.sandy.newston;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    /**
     *  Get Current Time (YY-mm-dd HH)。
     *
     * @return formatted time
     */
    public static String getCurrentTime() {
        // Get Current Time
        LocalDateTime now = LocalDateTime.now();

        // Define the DateTimeFormatter
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH");

        // Format the time
        return now.format(formatter);
    }
}