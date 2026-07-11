package com.example.reportservice.service;

import com.example.reportservice.model.Report;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.time.Instant;
import java.util.List;

@Service
public class ReportSqsProducer {

    private static final Logger log = LoggerFactory.getLogger(ReportSqsProducer.class);

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final String queueUrl;

    public ReportSqsProducer(
        SqsClient sqsClient,
        ObjectMapper objectMapper,
        @Value("${aws.sqs.queue-url:}") String queueUrl
    ) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.queueUrl = queueUrl;
    }

    public void sendReportCreatedMessage(Report report) {
        if (queueUrl == null || queueUrl.isBlank()) {
            log.warn("AWS_SQS_QUEUE_URL is empty. Skipping SQS publish for reportId={}", report.getId());
            return;
        }

        try {
            String messageBody = objectMapper.writeValueAsString(ReportCreatedMessage.from(report));
            SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(messageBody)
                .build();

            SendMessageResponse response = sqsClient.sendMessage(request);
            log.info(
                "SQS message sent for reportId={} messageId={} queueUrl={}",
                report.getId(),
                response.messageId(),
                queueUrl
            );
        } catch (JsonProcessingException exception) {
            log.error("Failed to serialize SQS message for reportId={}", report.getId(), exception);
            throw new IllegalStateException("Could not serialize report message for SQS", exception);
        } catch (Exception exception) {
            log.error("Failed to send SQS message for reportId={} queueUrl={}", report.getId(), queueUrl, exception);
            throw exception;
        }
    }

    private record ReportCreatedMessage(
        Long reportId,
        String type,
        String description,
        Instant createdAt,
        Double lat,
        Double lng,
        List<String> imageUrls
    ) {
        private static ReportCreatedMessage from(Report report) {
            return new ReportCreatedMessage(
                report.getId(),
                report.getType() != null ? report.getType().name() : null,
                report.getDescription(),
                report.getCreatedAt(),
                report.getLat(),
                report.getLng(),
                report.getImages().stream().map(image -> image.getImageUrl()).toList()
            );
        }
    }
}
