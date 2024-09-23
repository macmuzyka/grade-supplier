package com.gradesupplier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = {"com.schoolmodel.model"})
public class GradeSupplierApplication {

    public static void main(String[] args) {
        SpringApplication.run(GradeSupplierApplication.class, args);
    }

}
