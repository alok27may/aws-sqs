package com.kumaral.awssqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageQueueService {
    @Value("${app.config.message.queue.topic}")
    private String messageQueueTopic;
    private final AmazonSQS amazonSQSClient;

    public void createMessageQueue() {
        log.info("Creating message queue on AWS SQS");
        CreateQueueRequest request = new CreateQueueRequest();
        request.setQueueName(messageQueueTopic);
        try {
            CreateQueueResult queue = amazonSQSClient.createQueue(request);
            log.info("Create Queue Response {}", queue.getQueueUrl());
        } catch (QueueNameExistsException e) {
            log.error("Queue Name Exists {}", e.getErrorMessage());
        }
    }

    public void createFIFOMessageQueue() {
        Map<String, String> queueAttributes = new HashMap<>();
        queueAttributes.put(QueueAttributeName.FifoQueue.toString(), "true");
        queueAttributes.put(QueueAttributeName.ContentBasedDeduplication.toString(), "true");

        log.info("Creating message queue on AWS SQS");
        CreateQueueRequest request = new CreateQueueRequest();
        request.setQueueName(messageQueueTopic);
        request.setAttributes(queueAttributes);

        try {
            CreateQueueResult queue = amazonSQSClient.createQueue(request);
            log.info("Create Queue Response {}", queue.getQueueUrl());
        } catch (QueueNameExistsException e) {
            log.error("Queue Name Exists {}", e.getErrorMessage());
        }
    }

    public void publishExpense(CreateExpenseDto createExpenseDto) {
        try {
            GetQueueUrlResult queueUrl = amazonSQSClient.getQueueUrl(messageQueueTopic);
            log.info("Reading SQS Queue done: URL {}", queueUrl.getQueueUrl());
            amazonSQSClient.sendMessage(queueUrl.getQueueUrl(), createExpenseDto.getType() + ":" + createExpenseDto.getAmount());
        } catch (QueueDoesNotExistException | InvalidMessageContentsException e) {
            log.error("Queue does not exist {}", e.getMessage());
        }
    }

    @Scheduled(fixedDelay = 5000) // executes on every 5 second gap.
    public void receiveMessages() {
        try {
            String queueUrl = amazonSQSClient.getQueueUrl(messageQueueTopic).getQueueUrl();
            log.info("Reading SQS Queue done: URL {}", queueUrl);
            ReceiveMessageResult receiveMessageResult = amazonSQSClient.receiveMessage(queueUrl);
            if (!receiveMessageResult.getMessages().isEmpty()) {
                Message message = receiveMessageResult.getMessages().get(0);
                log.info("Incoming Message From SQS {}", message.getMessageId());
                log.info("Message Body {}", message.getBody());
                processInvoice(message.getBody());
                amazonSQSClient.deleteMessage(queueUrl, message.getReceiptHandle());
            }
        } catch (QueueDoesNotExistException e) {
            log.error("Queue does not exist {}", e.getMessage());
        }
    }

    private void processInvoice(String body) {
        log.info("Processing invoice generation and sending invoice emails from here.." + body);
    }

}
