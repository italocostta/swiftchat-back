package com.pd.swiftchat.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PROCESSO_TRAMITE_QUEUE = "processoTramiteQueue";
    public static final String PROCESSO_INDEFERIDO_QUEUE = "processoIndeferidoQueue";
    public static final String PROCESSO_DEFERIDO_QUEUE = "processoDeferidoQueue";

    @Bean
    public Queue tramiteQueue() {
        return new Queue(PROCESSO_TRAMITE_QUEUE, true);
    }

    @Bean
    public Queue indeferidoQueue() {
        return new Queue(PROCESSO_INDEFERIDO_QUEUE, true);
    }

    @Bean
    public Queue deferidoQueue() {
        return new Queue(PROCESSO_DEFERIDO_QUEUE, true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange("processoExchange");
    }

    @Bean
    public Binding tramiteBinding(Queue tramiteQueue, TopicExchange exchange) {
        return BindingBuilder.bind(tramiteQueue).to(exchange).with(PROCESSO_TRAMITE_QUEUE);
    }

    @Bean
    public Binding indeferidoBinding(Queue indeferidoQueue, TopicExchange exchange) {
        return BindingBuilder.bind(indeferidoQueue).to(exchange).with(PROCESSO_INDEFERIDO_QUEUE);
    }

    @Bean
    public Binding deferidoBinding(Queue deferidoQueue, TopicExchange exchange) {
        return BindingBuilder.bind(deferidoQueue).to(exchange).with(PROCESSO_DEFERIDO_QUEUE);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
