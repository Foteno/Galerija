package lt.insoft.gallery.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@EnableJpaRepositories("lt.insoft.gallery")
@EntityScan("lt.insoft")
@ComponentScan({"lt.insoft.gallery"})
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
