package com.solomondev.mlbstats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MlbstatsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MlbstatsApplication.class, args);
	}

}
