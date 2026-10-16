package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Answer;
import com.example.demo.Question;
import com.example.demo.User;
import com.example.demo.repository.AnswerRepository;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final AnswerRepository answerRepository;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final boolean emailEnabled;
    private final String fromAddress;

    public EmailNotificationService(
            AnswerRepository answerRepository,
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.notifications.email.enabled:false}") boolean emailEnabled,
            @Value("${app.notifications.email.from:}") String fromAddress
    ) {
        this.answerRepository = answerRepository;
        this.mailSenderProvider = mailSenderProvider;
        this.emailEnabled = emailEnabled;
        this.fromAddress = fromAddress;
    }

    @Transactional(readOnly = true)
    public void notifyQuestionOwnerAboutAnswer(String answerId, String questionId, String answererId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));

        Question question = answer.getQuestion();
        if (question == null || question.getUser() == null) {
            log.info("Skipping answer notification. Question owner is unavailable. answerId={} questionId={}",
                    answerId, questionId);
            return;
        }

        User questionOwner = question.getUser();
        if (questionOwner.getId() != null && questionOwner.getId().equals(answererId)) {
            log.info("Skipping answer notification. Question owner answered their own question. answerId={} questionId={}",
                    answerId, questionId);
            return;
        }

        String recipient = questionOwner.getEmail();
        if (recipient == null || recipient.isBlank()) {
            log.info("Skipping answer notification. Question owner has no email. questionId={}", questionId);
            return;
        }

        String subject = "New answer on your question";
        String body = "Your question \"" + question.getTitle() + "\" received a new answer.";

        if (emailEnabled) {
            sendEmail(recipient, subject, body, answerId, questionId);
            return;
        }

        log.info("Mock email notification: to={} subject={} body={} answerId={} questionId={}",
                recipient, subject, body, answerId, questionId);
    }

    private void sendEmail(String recipient, String subject, String body, String answerId, String questionId) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Real email is enabled, but JavaMailSender is not configured. to={} answerId={} questionId={}",
                    recipient, answerId, questionId);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        if (fromAddress != null && !fromAddress.isBlank()) {
            message.setFrom(fromAddress);
        }
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
            log.info("Email notification sent: to={} subject={} answerId={} questionId={}",
                    recipient, subject, answerId, questionId);
        } catch (MailException ex) {
            log.warn("Email notification failed: to={} answerId={} questionId={}",
                    recipient, answerId, questionId, ex);
        }
    }
}
