package org.rostyslav.forewa;

import org.springframework.boot.SpringApplication;

public class TestForewaApplication {

    public static void main(String[] args) {
        SpringApplication.from(ForewaApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
