package com.sandy.text.tosql.service;

import com.sandy.text.tosql.config.TextToSqlCfg;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.util.json.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class HtmlService {
    private static final int TOP_K = 10;
    @Autowired
    private ChatModel chatModel; // 注入聊天模型
    @Autowired
    private TextToSqlCfg textToSqlCfg;
    @Autowired
    private ResourceLoader resourceLoader;


    public String generateHtml(String question, List<Map<String, Object>> data) {
        try {

            Resource resource = resourceLoader.getResource("classpath:template.html");
            // 读取 JSON 文件内容到字符串
            String template = null;
            try {
                template = new String(toByteArray(resource.getInputStream()), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            PromptTemplate promptTemplate = new PromptTemplate("""
                    ### 目标
                    基于给定的title、data和html模版，生成html，将html模版里的$title替换为给定的title，基于data生成echarts脚本，并将html模版里的$generateScript替换为生成的echarts脚本,要求只返回html部分，并且去掉```html和```，将html部分生成html文件后，可以被浏览器正常访问
                    ### 给定的title为
                    {title}
                    ### 给定的data为
                    {data}
                    ### 给定的模版为
                    {template}
                    """);

            Prompt prompt = promptTemplate.create(Map.of("title", question,"data", JsonParser.toJson(data), "template", template));
            log.info("prompt:{}", JsonParser.toJson(prompt));
            String content = ChatClient.builder(chatModel) .build().prompt(prompt).call().content();
            content=null!=content?content.replaceAll("```html","").replaceAll("```",""):null;
            String directory = "./generated/";
            checkDirectory(directory);

            String fileName = UUID.randomUUID().toString() + ".html";
            File file = new File(directory+fileName);
            // 写入文件
            try (FileWriter writer = new FileWriter(file)) {
                String[] lines = content.split("\\r?\\n"); // 将字符串按行拆分，兼容不同换行符
                boolean startWriting = false; // 标记是否开始写入
                for (String line : lines) {
                    // 检查是否到达开始写入的行
                    if (line.trim().startsWith("<!DOCTYPE html>")) {
                        startWriting = true;
                    }
                    // 如果已经开始写入，则写入当前行
                    if (startWriting) {
                        writer.write(line);
                        writer.write(System.lineSeparator()); // 添加换行符
                        // 检查是否到达结束行
                        if (line.trim().endsWith("</html>")) {
                            break; // 遇到 </html> 后结束
                        }
                    }
                }
            }
            log.info("generate html fileName:{},file:{}",fileName,content);
            return "http://"+textToSqlCfg.getHost()+":"+textToSqlCfg.getPort()+"/"+fileName;
        }catch (Exception e){
            log.error("Failed to generate EchartsJson: {}", e.getMessage());
        }
        return null;
    }
    private void checkDirectory(String directory) {
        // Create File object
        File dir = new File(directory);
        // Check if directory exists
        if (!dir.exists()) {
            // If it doesn't exist, try to create the directory
            boolean created = dir.mkdirs();
            if (created) {
                log.info("Directory created successfully: {}", directory);
            } else {
                log.info("Failed to create directory: {}", directory);
            }
        } else {
            log.info("Directory already exists: {}", directory);
        }
    }


    private byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try(InputStream in = input){
            IOUtils.copy(in, output);
        }
        return output.toByteArray();
    }

    public String generateHtmlTable( List<Map<String, Object>> data, int displaySize) {
        StringBuilder html = new StringBuilder();
        String fileName = UUID.randomUUID().toString() + ".csv";
        String csvFilePath = "./generated/"+ fileName;
        String  address="http://"+textToSqlCfg.getHost()+":"+textToSqlCfg.getPort()+"/"+fileName;

        // 确保目录存在
        checkDirectory("./generated/");

        // 生成 CSV 文件（包含全部数据）
        generateCsvFile(data, csvFilePath);

        // 开始表格
        html.append("<table border='1' style='border-collapse: collapse; width: 100%;'>\n");

        // 如果数组不为空，生成表头
        if (!data.isEmpty()) {
            Map<String, Object> firstRow = data.get(0);
            html.append("  <thead>\n    <tr>\n");
            for (String key : firstRow.keySet()) {
                html.append("      <th>").append(key).append("</th>\n");
            }
            html.append("    </tr>\n  </thead>\n");

            // 生成表格内容，最多显示 10 条
            html.append("  <tbody>\n");
            int displayLimit = Math.min(data.size(), displaySize); // 最多 50 条
            for (int i = 0; i < displayLimit; i++) {
                Map<String, Object> row = data.get(i);
                html.append("    <tr>\n");
                for (String key : firstRow.keySet()) {
                    Object value = row.get(key);
                    html.append("      <td>").append(value != null ? value.toString() : "").append("</td>\n");
                }
                html.append("    </tr>\n");
            }
            html.append("  </tbody>\n");
        }

        // 结束表格
        html.append("</table>\n");
        html.append("<a href=\""+address+"\" download>下载数据</a>\n");
        String tableHtml = html.toString();
        log.info("It generates a tableHtml:\n{}",tableHtml);
        return tableHtml;
    }

    private void generateCsvFile(List<Map<String, Object>> data, String filePath) {
        try (FileWriter writer = new FileWriter(filePath, StandardCharsets.UTF_8)) {
            if (data.isEmpty()) {
                return;
            }
            // 写入 BOM
            writer.write("\uFEFF");
            // 写入表头
            Map<String, Object> firstRow = data.get(0);
            StringBuilder header = new StringBuilder();
            for (String key : firstRow.keySet()) {
                header.append("\"").append(key.replace("\"", "\"\"")).append("\",");
            }
            header.setLength(header.length() - 1); // 移除最后一个逗号
            writer.write(header.toString());

            // 写入全部数据
            for (int i = 0; i < data.size(); i++) {
                Map<String, Object> row = data.get(i);
                StringBuilder line = new StringBuilder();
                for (String key : firstRow.keySet()) {
                    Object value = row.get(key);
                    String valueStr = value != null ? value.toString() : "";
                    line.append("\"").append(valueStr.replace("\"", "\"\"")).append("\",");
                }
                line.setLength(line.length() - 1); // 移除最后一个逗号
                writer.write("\n"+line.toString() );
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
