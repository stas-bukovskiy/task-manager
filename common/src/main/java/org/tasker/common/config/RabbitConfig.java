package org.tasker.common.config;


import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.rabbitmq.*;

@Configuration
@Profile("!test")
public class RabbitConfig {
	
	@Bean
	Mono<Connection> connectionMono(@Value("${spring.rabbitmq.host}") String host,
									@Value("${spring.rabbitmq.port}") int port,
									@Value("${spring.rabbitmq.username}") String username,
									@Value("${spring.rabbitmq.password}") String password) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.useNio();
		connectionFactory.setHost(host);
		connectionFactory.setPort(port);
		connectionFactory.setUsername(username);
		connectionFactory.setPassword(password);
        return Mono.fromCallable(() -> connectionFactory.newConnection("reactor-rabbit")).cache();
    }

	@Bean
	public SenderOptions senderOptions(Mono<Connection> connectionMono) {
		return new SenderOptions()
			.connectionMono(connectionMono)
            .resourceManagementScheduler(Schedulers.boundedElastic());
	} 

	@Bean
	public Sender sender(SenderOptions senderOptions) {
		return RabbitFlux.createSender(senderOptions);
	}

	@Bean
	public ReceiverOptions receiverOptions(Mono<Connection> connectionMono) {
		return new ReceiverOptions()
				.connectionMono(connectionMono);
	}


	@Bean
	Receiver receiver(ReceiverOptions receiverOptions) {
		return RabbitFlux.createReceiver(receiverOptions);
	}

}