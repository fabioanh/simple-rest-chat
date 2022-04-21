package com.visable.chat.config

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class MessagingConfig {
    @Bean
    fun queue() = Queue(MessagingConstants.QUEUE)

    @Bean
    fun exchange(): TopicExchange = TopicExchange(MessagingConstants.EXCHANGE)

    @Bean
    fun binding(queue: Queue?, exchange: TopicExchange): Binding =
        BindingBuilder.bind(queue).to(exchange).with(MessagingConstants.ROUTING_KEY)

    @Bean
    fun converter(): MessageConverter =
        Jackson2JsonMessageConverter(jacksonObjectMapper().registerModule(JavaTimeModule()))

    @Bean
    fun template(connectionFactory: ConnectionFactory): AmqpTemplate {
        val rabbitTemplate = RabbitTemplate(connectionFactory)
        rabbitTemplate.messageConverter = converter()
        return rabbitTemplate
    }
}