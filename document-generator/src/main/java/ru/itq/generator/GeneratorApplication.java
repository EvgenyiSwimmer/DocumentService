package ru.itq.generator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType; // <-- добавили
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.itq.generator.config.GeneratorProperties;

@SpringBootApplication
@EnableConfigurationProperties(GeneratorProperties.class)
public class GeneratorApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(GeneratorApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE); /// НЕ поднимаем web-сервер
        app.run(args); /// запускаем контекст, выполняем Runner и выходим
    }
}