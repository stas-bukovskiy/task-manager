package org.tasker.task.input.event;


import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tasker.common.models.commands.*;
import org.tasker.common.models.queries.*;
import reactor.core.publisher.Flux;
import reactor.rabbitmq.*;

@Slf4j
@Service
@Getter
public final class TaskMessagingSpecs {

    private final String requestQueueName;
    private final String requestExchangeName;
    private final String responseExchangeName;
    private final Sender sender;
    private final Receiver receiver;

    public TaskMessagingSpecs(Sender sender, Receiver receiver,
                              @Value("${communication.task.request-queue}") String requestQueueName,
                              @Value("${communication.task.request-exchange}") String requestExchangeName,
                              @Value("${communication.task.response-exchange}") String responseExchangeName) {
        this.requestQueueName = requestQueueName;
        this.requestExchangeName = requestExchangeName;
        this.responseExchangeName = responseExchangeName;
        this.sender = sender;
        this.receiver = receiver;
    }

    @PostConstruct
    public void init() {
        // TODO: add sender and retriever bean retrieving
        log.debug("Queue initializing...");

        var queue = this.requestExchangeSpec();
        var exchange = this.requestQueueSpec();
        sender.declareExchange(queue)
                .then(sender.declareQueue(exchange))
                .thenMany(Flux.just(GetStatisticQuery.QUERY_NAME, CreateBoardCommand.COMMAND_NAME,
                                ReviewInvitationCommand.COMMAND_NAME, GetBoardQuery.QUERY_NAME, GetBoardsQuery.QUERY_NAME,
                                DeleteBoardCommand.COMMAND_NAME, DeleteInvitationCommand.COMMAND_NAME,
                                DeleteMemberCommand.COMMAND_NAME, UpdateBoardCommand.COMMAND_NAME,
                                InviteUsersCommand.COMMAND_NAME, AddAssigneeCommand.COMMAND_NAME,
                                GetTasksQuery.QUERY_NAME, GetTaskQuery.QUERY_NAME,
                                CreateTaskCommand.COMMAND_NAME, DeleteAssigneeCommand.COMMAND_NAME,
                                DeleteTaskCommand.COMMAND_NAME, UpdateTaskInfoCommand.COMMAND_NAME,
                                UpdateTaskStatusCommand.COMMAND_NAME)
                        .map(this::requestBindingSpecs)
                        .flatMap(sender::bind))
                .doOnError(ex -> log.error("Error while initializing command queue: {}", ex.getMessage()))
                .subscribe();
    }

    public QueueSpecification requestQueueSpec() {
        return QueueSpecification.queue(requestQueueName)
                .durable(true)
                .exclusive(false)
                .autoDelete(false);
    }

    public ExchangeSpecification requestExchangeSpec() {
        return ExchangeSpecification.exchange(requestExchangeName)
                .type("direct")
                .durable(true)
                .autoDelete(false);
    }

    public BindingSpecification requestBindingSpecs(String routingKey) {
        return BindingSpecification.binding(requestExchangeName, routingKey, requestQueueName);
    }
}
