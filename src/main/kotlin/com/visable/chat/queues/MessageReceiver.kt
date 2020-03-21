package com.visable.chat.queues

import com.visable.chat.entities.Message
import com.visable.chat.repositories.MessageRepository
import com.visable.chat.repositories.UserRepository
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Configuration
class MessageQueueConfig {

    @Bean
    fun messageReceiver(): Queue {
        return Queue("messageReceiver")
    }
}

@Service
@RabbitListener(queues = ["messageReceiver"])
class Receiver(private val messageRepository: MessageRepository) {
    @RabbitHandler
    fun receive(message: Message) {
        messageRepository.save(message)
    }
}