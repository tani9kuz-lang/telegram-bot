package pro.sky.telegrambot.scheduler;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class NotificationTaskScheduler {

    private final Logger logger = LoggerFactory.getLogger(NotificationTaskScheduler.class);

    @Autowired
    private NotificationTaskRepository notificationTaskRepository;

    @Autowired
    private TelegramBot telegramBot;

    @Scheduled(cron = "0 0/1 * * * *")
    public void runNotificationChecks() {

        LocalDateTime currentMinute = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        logger.info("Поиск напоминаний на время: {}", currentMinute);

        List<NotificationTask> tasks = notificationTaskRepository.findAllBySendDateTime(currentMinute);

        if (!tasks.isEmpty()) {
            logger.info("Найдено напоминаний к отправке: {}", tasks.size());

            tasks.forEach(task -> {
                SendMessage message = new SendMessage(task.getChatId(), "Напоминание: " + task.getText());

                SendResponse response = telegramBot.execute(message);

                if (response.isOk()) {
                    logger.info("Уведомление с ID {} успешно отправлено пользователю в чат {}", task.getId(), task.getChatId());
                } else {
                    logger.error("Не удалось отправить уведомление ID {}. Код ошибки: {}", task.getId(), response.errorCode());
                }
            });
        }
    }
}
