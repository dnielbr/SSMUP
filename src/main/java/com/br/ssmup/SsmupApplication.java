package com.br.ssmup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SsmupApplication {

	public static void main(String[] args) {
		SpringApplication.run(SsmupApplication.class, args);
	}

}
