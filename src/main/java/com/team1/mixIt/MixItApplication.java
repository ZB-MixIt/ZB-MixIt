package com.team1.mixIt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class MixItApplication {

	public static void main(String[] args) {
		SpringApplication.run(MixItApplication.class, args);
	}

}
