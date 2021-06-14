package dorox.app;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@ComponentScan(basePackages = {"org.n3r.idworker","dorox.app"})
public class BTAApplication {
    public static void main(String[] args) {
        SpringApplication.run(BTAApplication.class, args);
    }

}
