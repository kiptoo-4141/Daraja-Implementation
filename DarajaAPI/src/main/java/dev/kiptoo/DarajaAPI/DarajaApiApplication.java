package dev.kiptoo.DarajaAPI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class DarajaApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(DarajaApiApplication.class, args);
	}
}