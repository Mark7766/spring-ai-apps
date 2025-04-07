package com.sandy.tools.ollama;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
@Slf4j
@Component
public class DateTimeTools {

    @Tool(description = "查询用户所在时区的当前时间")
    String getCurrentDateTime() {
        log.info("getCurrentDateTime");
        String currentDateTime = LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
        log.info("getCurrentDateTime:{}",currentDateTime);
        return currentDateTime;
    }

    @Tool(description = "将用户闹钟设置为给定的时间，时间格式为 ISO-8601")
    public String setAlarm(String time) {
        log.info("setAlarm with time: {}", time);
        LocalDateTime alarmTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        String result = "Alarm set for " + alarmTime + ".";
        log.info(result);
        return result;
    }
}
