package dorox.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"dorox.app"})
public class SgipPortApplication {
	public static void main(String[] args) {
		SpringApplication.run(SgipPortApplication.class, args);
	}
	


}
