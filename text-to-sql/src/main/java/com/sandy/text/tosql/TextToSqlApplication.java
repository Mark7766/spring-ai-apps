package com.sandy.text.tosql;

import com.sandy.text.tosql.initdata.IotDataService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class TextToSqlApplication {

	public static void main(String[] args) {
		ApplicationContext context =SpringApplication.run(TextToSqlApplication.class, args);
//		IotDataService service = context.getBean(IotDataService.class);
//		service.generateIotData();
	}

}
