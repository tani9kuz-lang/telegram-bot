package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final Pattern pattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)");
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private NotificationTaskRepository notificationTaskRepository;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);

            if (update.message() != null && update.message().text() != null) {
                String messageText = update.message().text();
                Long chatId = update.message().chat().id();

                if (messageText.equals("/start")) {
                    SendMessage welcomeMessage = new SendMessage(chatId,
                            "Привет! Я бот-напоминалка.\nОтправь мне сообщение в формате:\n01.01.2022 20:00 Сделать домашнюю работу");
                    telegramBot.execute(welcomeMessage);
                    return;
                }

                Matcher matcher = pattern.matcher(messageText);
                if (matcher.matches()) {
                    String dateTimeStr = matcher.group(1);
                    String taskText = matcher.group(3);

                    try {
                        LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);

                        NotificationTask notificationTask = new NotificationTask();
                        notificationTask.setChatId(chatId);
                        notificationTask.setText(taskText);
                        notificationTask.setSendDateTime(dateTime);

                        notificationTaskRepository.save(notificationTask);

                        SendMessage successMessage = new SendMessage(chatId, "Напоминание успешно сохранено!");
                        telegramBot.execute(successMessage);

                        logger.info("Успешно сохранено напоминание для чата {}", chatId);

                    } catch (Exception e) {
                        logger.error("Ошибка при сохранении напоминания", e);
                        SendMessage errorMessage = new SendMessage(chatId, "Произошла ошибка при обработке даты. Попробуйте еще раз.");
                        telegramBot.execute(errorMessage);
                    }
                }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
