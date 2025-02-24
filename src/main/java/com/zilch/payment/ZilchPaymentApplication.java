package com.zilch.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class ZilchPaymentApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZilchPaymentApplication.class, args);
	}

}
