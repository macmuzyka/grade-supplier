package com.gradesupplier.service;

import com.gradesupplier.StudentRepository;

import com.schoolmodel.model.GradeRaw;
import com.schoolmodel.model.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Random;

@Service
public class SupplyingService {
    private static List<String> subjects = List.of("Math", "History", "Art", "English");
    private static List<Integer> grades = List.of(1, 2, 3, 4, 5);
    private RestTemplate rt = new RestTemplate();
    private final KafkaTemplate<String, GradeRaw> kafkaTemplate;
    private static final Logger log = LoggerFactory.getLogger(SupplyingService.class);

    private final StudentRepository studentRepository;

    public SupplyingService(KafkaTemplate<String, GradeRaw> kafkaTemplate, StudentRepository studentRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.studentRepository = studentRepository;
    }


//    @Scheduled(cron = "0 */1 * ? * *")
//    public void scheduledSupplier() {

//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Content-Type", "application/json");
//        HttpEntity<GradeRaw> requestEntity = new HttpEntity<>(grade, headers);
//        String url = "http://localhost:9000/school/add-raw-grade";
//        ResponseEntity<String> response = rt.exchange(
//                url,
//                HttpMethod.POST,
//                requestEntity,
//                String.class
//        );
//
//        if (response.getStatusCode().is2xxSuccessful()) {
//            log.info("Request was successful: " + response.getBody());
//        } else {
//            log.error("Request failed with status: " + response.getStatusCode());
//        }
//    }

    @Scheduled(cron = "*/15 * * * * *")
    public void scheduledSupplier() {
        int i = 0;
        log.info("[Executing scheduled task]");
        List<String> studentCodes = studentRepository.findAll().stream().map(Student::getCode).toList();
        int codesCount = studentCodes.size();
        log.debug("Number of codes found: {}", codesCount);
        Random random = new Random();

        int randomGrade = grades.get(random.nextInt(grades.size() - 1));

        String randomSubject = subjects.get(random.nextInt(subjects.size() - 1));
        String randomStudentCode = studentCodes.get(random.nextInt(codesCount));
        log.info("Random grade: {}\nRandom Subject: {}\nRandom code: {}", randomGrade, randomSubject, randomStudentCode);
        GradeRaw grade = new GradeRaw(randomGrade,
                randomSubject,
                randomStudentCode
        );
        sendMessage(grade);
    }


    public void sendMessage(GradeRaw grade) {
        kafkaTemplate.send("grade-supplier", grade);
    }
}
