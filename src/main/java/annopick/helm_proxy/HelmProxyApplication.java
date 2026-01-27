package annopick.helm_proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HelmProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(HelmProxyApplication.class, args);
	}

}
