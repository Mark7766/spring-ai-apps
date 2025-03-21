package com.sandy.newston;

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class HackerNewsProcessor{
    @Autowired
    private StoryProcessor storyProcessor;
    @Autowired
    private EmailSender emailSender;
    @Scheduled(cron = "${sandy.newston.schedule}")
    public void process() {
        log.info("HackerNewsProcessor starts to process news.");
        try {
            DatabaseHelper.initDB();
            Class.forName("org.sqlite.JDBC");
            log.info("Database is inited.");
            List<String> topStories = storyProcessor.getTopStories();
            log.info("topStories:{}",topStories.toString());
            //<storyId,detail> 过滤掉已经看过的Story
            List<JsonObject> newStoris=getNewStorie(topStories);
            log.info("newStoris:{}",newStoris.toString());
            if(newStoris.isEmpty()){
                log.info("No new story found.");
                return;
            }
            //筛选出感兴趣的Story
            List<JsonObject> favouriteStories=storyProcessor.filterSoftwareTitles(newStoris);
            //对Story进行内容总结
            List<String> summaries = summary(favouriteStories);
            //记录本次Story总结
            recordDateSummaries(summaries);
            if (!summaries.isEmpty()) {
                emailSender.sendEmail("Hacker News "+DateTimeUtils.getCurrentTime(), String.join("\n", summaries));
            }

        } catch (Exception e) {
            log.info("Main process failed: " + e.getMessage(),e);
        }
        log.info("HackerNewsProcessor has processed news completely.");
    }

    private void recordDateSummaries(List<String> summaries) throws IOException {
        String filename = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".txt";
        try (FileWriter writer = new FileWriter(filename, true)) {
            for (String summary : summaries) {
                writer.write(summary + "\n");
            }
        }
    }

    private List<String> summary(List<JsonObject> favouriteStories) {
        List<String> summaries = new ArrayList<>();
        favouriteStories.forEach(detail->{
            log.info("summaries detail:{}",detail.toString());
            if(detail.has("text")){
                String title = detail.get("title").getAsString();
                String create_date = toDateStr(detail.get("time").getAsLong());
                int score = detail.get("score").getAsInt();
                int descendants = detail.get("descendants").getAsInt();
                String story_type = detail.get("type").getAsString();
                String text = detail.get("text").getAsString();
                String url =!detail.has("url")?"NO URL":detail.get("url").getAsString();
                String title_zh = null;
                try {
                    title_zh = storyProcessor.translateToChinese(title);
                    String summary = storyProcessor.generateSummary(text);
                    summaries.add(create_date+":"+score+":"+descendants+":"+story_type+"\n"+title+"\n"+title_zh+"\n"+summary+"\n"+url+"\n-----------------------------\n");
                } catch (IOException e) {
                    log.error("Translation has error[{}]",e.getMessage(),e);
                }
            }
        });
        return summaries;
    }

    private List<JsonObject> getNewStorie(List<String> topStories) throws SQLException, IOException {
        List<JsonObject> newStoris = new ArrayList<>();
        ZonedDateTime threeDaysAgo = ZonedDateTime.now(ZoneOffset.UTC).minusDays(3);
        for (String storyId : topStories.subList(0, Math.min(50, topStories.size()))) {
            if (!DatabaseHelper.keyExists(storyId)) {
                DatabaseHelper.setKey(storyId);
                JsonObject detail = storyProcessor.getStoryDetails(storyId);
                long time = detail.get("time").getAsLong();
                Instant instant = Instant.ofEpochSecond(time);
                if (instant.isAfter(threeDaysAgo.toInstant())) {
                    newStoris.add(detail);
                }
            }
        }
        return newStoris;
    }

    //Unix 时间戳（秒）
public static String toDateStr(long unixTimestamp ) {
    Instant instant = Instant.ofEpochSecond(unixTimestamp); // 转换为 Instant
    // 转换为北京时间（UTC+8）
    LocalDateTime beijingTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    // 格式化输出
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String formattedTime = beijingTime.format(formatter);
    return  formattedTime;
}
}
