package com.sandy.tools.ollama;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
@Component
public class DateTimeTools {

    @Tool(description = "查询用户所在时区的当前时间")
    String getCurrentDateTime() {
        System.out.println("getCurrentDateTime");
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }

    @Tool(description = "将用户闹钟设置为给定的时间，时间格式为 ISO-8601")
    public void setAlarm(String time) {
        System.out.println("setAlarm");
        LocalDateTime alarmTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        System.out.println("Alarm set for " + alarmTime);
    }
}
