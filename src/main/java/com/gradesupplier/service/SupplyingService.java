package com.gradesupplier.service;

import com.gradesupplier.StudentRepository;

import com.schoolmodel.model.entity.GradeDto;
import com.schoolmodel.model.entity.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Service
public class SupplyingService {
    @Value("#{'${available.subjects}'.split(',')}")
    private List<String> subjects;
    @Value("#{'${available.grades}'.split(',')}")
    private List<Integer> grades;
    private final KafkaTemplate<String, GradeDto> kafkaTemplate;
    private static final Logger log = LoggerFactory.getLogger(SupplyingService.class);
    private final StudentRepository studentRepository;
    private final Random randomizer = new Random();

    public SupplyingService(KafkaTemplate<String, GradeDto> kafkaTemplate, StudentRepository studentRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.studentRepository = studentRepository;
    }

    @Scheduled(cron = "*/60 * * * * *")
    public void scheduledSupplier() {
        log.info("[Executing scheduled task]");
        List<String> studentCodes = studentRepository.findAll().stream().map(Student::getCode).toList();
        if (studentCodes.isEmpty()) {
            throw new IllegalArgumentException("No students found in database thus grade will not be sent!");
        }
        log.debug("Number of codes found: {}", studentCodes.size());
        String randomStudentCode = getRandomStudentCode(studentCodes);
        sendRandomGradeToEachStudentsSubject(randomStudentCode);

    }

    private void sendRandomGradeToEachStudentsSubject(String randomStudentCode) {
        for (String subject : subjects) {
            int randomGrade = getRandomGrade();
            log.info("[Random grade: {} Current Subject: {} Random code: {}]", randomGrade, subject, randomStudentCode);

            GradeDto grade = new GradeDto(randomGrade,
                    subject,
                    randomStudentCode
            );
            sendViaKafka(grade);
        }
    }

    private String getRandomStudentCode(List<String> studentCodes) {
        return studentCodes.get(randomizer.nextInt(studentCodes.size()));
    }

    private int getRandomGrade() {
        return grades.get(randomizer.nextInt(grades.size()));
    }

    public void sendViaKafka(GradeDto grade) {
        String topic = grade.getSubject().toLowerCase() + "-grade-supplier";
        CompletableFuture<SendResult<String, GradeDto>> result = kafkaTemplate.send(topic, grade);
    }
}
