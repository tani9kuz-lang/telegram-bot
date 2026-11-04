package pro.sky.telegrambot;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TelegramBotApplication {
	public static void main(String[] args) {
		org.springframework.boot.SpringApplication.run(TelegramBotApplication.class, args);
	}
}
