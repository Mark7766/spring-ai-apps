# 这个应用是做什么的
这是一个展示Spring AI Function Calling的Demo，大模型可以根据提问，调用不同的API，解决大模型没有的最新知识问题，可以实现大模型自动处理一些任务
# Ollama 安装
[Download and install Ollama](https://ollama.com/download)
# 安装千问模型
ollama pull qwen2.5
# 启动
IDE spring-boot:run -DskipTest
# 访问 http://localhost:8081/chat.html
- 填写今天星期几
会调用
```
@Tool(description = "查询用户所在时区的当前时间")
  String getCurrentDateTime() {
  System.out.println("getCurrentDateTime");
  return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
  }
```

- 闹钟设置为10点
```
@Tool(description = "将用户闹钟设置为给定的时间，时间格式为 ISO-8601")
public void setAlarm(String time) {
System.out.println("setAlarm");
LocalDateTime alarmTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
System.out.println("Alarm set for " + alarmTime);
}
```