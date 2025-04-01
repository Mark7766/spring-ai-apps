package com.sandy.mcp.ollama.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Component
@Slf4j
public class McpServerTool {

    @Tool(name = "getCurrentDateTime",description = "查询用户所在时区的当前时间")
    String getCurrentDateTime() {
        log.info("getCurrentDateTime:{}",new Date());
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }

    @Tool(name = "setAlarm",description = "将用户闹钟设置为给定的时间，时间格式为 ISO-8601")
    public void setAlarm(String time) {
        log.info("setAlarm:{}",time);
        LocalDateTime alarmTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        log.info("Alarm set for {}", alarmTime);
    }
}
