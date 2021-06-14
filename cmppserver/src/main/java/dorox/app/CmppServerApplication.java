package dorox.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "org.n3r.idworker","dorox.app"})
public class CmppServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(CmppServerApplication.class, args);
	}
	

}
