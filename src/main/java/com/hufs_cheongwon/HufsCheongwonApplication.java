package com.hufs_cheongwon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class HufsCheongwonApplication {

    public static void main(String[] args) {
        SpringApplication.run(HufsCheongwonApplication.class, args);
    }

}
