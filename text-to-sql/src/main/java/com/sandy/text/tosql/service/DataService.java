package com.sandy.text.tosql.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.util.json.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DataService {
    private static final int TABLE_SIZE = 20;
    @Autowired
    private ChatModel chatModel; // 注入聊天模型
    @Autowired
    private DbService dbService;
    @Autowired
    private HtmlService htmlService;

    public void execute(String question, ResponseBodyEmitter emitter) throws IOException {
        doSend(emitter,"收到您的问题，正快马加鞭进行解决<br>");
        String content = ChatClient.create(chatModel)
                .prompt(getPrompt(question))
                .call()
                .content();
        log.info("method:{}",content);
        assert content != null;
        if(content.contains("detect")){
            detect(question,emitter);
        }else if(content.contains("predict")){
            predict(question,emitter);
        }else if(content.contains("query")){
            query(question,emitter);
        }else{
            emitter.send("无法进行相关操作");
        }

    }

    public void query(String question, ResponseBodyEmitter emitter) throws IOException {
        doSend(emitter,"数据查询<br>生成SQL中...");
        String sql = dbService.generateSql(question);
        doSend(emitter,"<br>生成SQL：" + sql+"<br>查询数据中...<br>");
        List<Map<String, Object>> data = dbService.executeSql(sql);
        doSend(emitter,"<br>查询到数据：" + htmlService.generateHtmlTable(data,TABLE_SIZE)+"<br>生成图表中...");
        String htmlfile = htmlService.generateHtml(question, data);
        doSend(emitter,"<br><iframe src=\"" + htmlfile + "\" width=\"800px\" height=\"500px\" frameborder=\"0\" title=\"" + question + "\"></iframe>");
    }
    public void predict(String question, ResponseBodyEmitter emitter) throws IOException {
        doSend(emitter,"数据预测<br>生成SQL中...");
        String sql = (dbService.generateSql(question));
        doSend(emitter,"生成SQL：" + sql+"<br>查询数据中...\"<br>");
        List<Map<String, Object>> data=dbService.executeSql(sql);
        doSend(emitter,"查询到数据：" + htmlService.generateHtmlTable(data,TABLE_SIZE)+"<br>进行数据预测...");
        String anomalyData=doPredict(question,data);
        doSend(emitter,"预测数据如下：<br>"+ anomalyData.replace("\n","<br>"));
    }

    public void detect(String question, ResponseBodyEmitter emitter) throws IOException {
        doSend(emitter,"异常检测<br>生成SQL中...");
        String sql = (dbService.generateSql(question));
        doSend(emitter,"生成SQL：" + sql+"<br>查询数据中...\"<br>");
        List<Map<String, Object>> data=dbService.executeSql(sql);
        doSend(emitter,"查询到数据：" + htmlService.generateHtmlTable(data,TABLE_SIZE)+"<br>进行数据异常检测...");
        String anomalyData=doDetect(question,data);
        doSend(emitter,"异常数据如下：<br>"+ anomalyData.replace("\n","<br>"));
    }
    private String doDetect(String question, List<Map<String, Object>> data) {
        PromptTemplate promptTemplate = new PromptTemplate("""
                    ### 角色
                    你是一个数据异常检测算法专家，擅长各种数据异常检测算法的设计、开发与选型
                    ### 目标
                    检测出异常数据
                    ### 方法
                    基于给定问题和数据，同时注意按步长频率的数据是否有整条记录缺失，进行数据异常检测，将异常数据筛选出来，并说明异常原因
                    ### 给定的问题为
                    {question}
                    ### 给定的数据为
                    {data}
                    """);
        Prompt prompt = promptTemplate.create(Map.of("question", question,"data", JsonParser.toJson(data)));
        log.info("doDetect prompt:{}", JsonParser.toJson(prompt));
        return ChatClient.builder(chatModel) .build().prompt(prompt).call().content();
    }
    private String doPredict(String question, List<Map<String, Object>> data) {
        PromptTemplate promptTemplate = new PromptTemplate("""
                    ### 角色
                    你是一个数据预测算法专家，擅长各种数据预测算法的设计、开发与选型
                    ### 目标
                    预测未来的数据
                    ### 方法
                    基于给定问题和数据，进行数据预测，并说明预测算法和依据
                    ### 给定的问题为
                    {question}
                    ### 给定的数据为
                    {data}
                    """);
        Prompt prompt = promptTemplate.create(Map.of("question", question,"data", JsonParser.toJson(data)));
        log.info("doPredict prompt:{}", JsonParser.toJson(prompt));
        return ChatClient.builder(chatModel) .build().prompt(prompt).call().content();
    }


    private void doSend(ResponseBodyEmitter emitter, String msg) throws IOException {
        log.info("send:{}",msg);
        emitter.send(msg);
    }

    private Prompt getPrompt(String question) {
        PromptTemplate promptTemplate = new PromptTemplate("""
                    ### 角色
                    你是一个前端数据专家，擅长数据分析、数据异常检测、查询数据等
                    ### 目标
                    判断这个问题是查询数据，还是异常数据检测
                    ### 方法
                    基于用户的问题，分析问题是查询数据，还是异常数据检测，还是预测数据
                    ### 给定的问题为
                    {question}
                    ### 输出格式
                    如果是查询数据，query，如果是异常数据检测，detect，如果是预测数据，返回predict，如果涉及多个，请用|分隔
                    query|detect，query|predict等，如果都不是，请直接返回UnKnown
                    """);
        Prompt prompt = promptTemplate.create(Map.of("question", question));
        log.info("prompt:{}", JsonParser.toJson(prompt));
        return  prompt;
    }
}
