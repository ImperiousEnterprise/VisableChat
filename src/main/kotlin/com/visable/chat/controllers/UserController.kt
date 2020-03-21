package com.visable.chat.controllers

import com.visable.chat.entities.Message
import com.visable.chat.entities.User
import com.visable.chat.repositories.MessageRepository
import com.visable.chat.repositories.UserRepository
import org.springframework.amqp.core.Queue;
import org.springframework.web.bind.annotation.*
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.boot.origin.TextResourceOrigin
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Sort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.validation.FieldError
import org.springframework.validation.annotation.Validated
import java.lang.Exception
import javax.validation.Valid

@RestController
class UserController(val userRepository: UserRepository,
                     val messageRepository: MessageRepository,
                     val queue:Queue,
                     val rabbitTemplate: RabbitTemplate){

    @PostMapping("/users", consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    @ResponseBody
    fun createUser(@Valid @RequestBody user: User): ResponseEntity<User> {
        var newUser: User? = null
        kotlin.runCatching {
            userRepository.save(user)
        }.onSuccess {
            newUser = it
        }.onFailure {
            throw Exception("error.nickname.duplicate")
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(newUser)
    }

    @GetMapping("/users/{username}")
    fun getUserId(@PathVariable(value = "username") username: String): ResponseEntity<User> {
        var user:User? = null
        kotlin.runCatching {
            userRepository.findByNickname(username)
        }.onSuccess {
            user = it
        }.onFailure {
            throw NotExistException("error.user.notexist")
        }
        return ResponseEntity.status(HttpStatus.OK).body(user)
    }

    @PostMapping("/users/{id}/messages", consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun postMessage(@PathVariable(value = "id") userId: Long, @Valid @RequestBody message: Message):ResponseEntity<String> {
        message.sender = userId

        if(message.sender!! == message.recipient!!){
            throw SelfMessageException("error.message.selfsend")
        }

        if(!userRepository.existsById(message.sender!!)){
            throw NotExistException("error.sender.notexist")
        }

        if(!userRepository.existsById(message.recipient!!)){
            throw NotExistException("error.recipient.notexist")
        }

        rabbitTemplate.convertAndSend("messageReceiver",message)

        return ResponseEntity.status(HttpStatus.ACCEPTED).build()
    }

    @GetMapping("/users/{id}/messages")
    fun getMessages(@PathVariable(value = "id") userId: Long,
                    @RequestParam from:Long? = null,
                    @RequestParam to:Long? = null,
                    @RequestParam username:Long? = null,
                    @RequestParam(defaultValue="asc") sort:String,
                    @RequestParam(defaultValue = "false") sent:Boolean ) : ResponseEntity<List<Message>>{

        if(!userRepository.existsById(userId)){
            throw NotExistException("error.user.notexist")
        }

        if(from != null && to != null){
            throw Exception("error.message.onlyone")
        }

        val sortDirection: Sort = when(sort.toLowerCase()){
           "desc" -> Sort.by("createdAt").descending()
            else -> Sort.by("createdAt").ascending()
        }

        val messages: List<Message>

        if(sent){
            messages = when(to != null){
                true -> messageRepository.findByRecipientAndSender(to, userId, sortDirection)
                false -> messageRepository.findBySender(userId,sortDirection)
            }
        }else{
            messages = when(from != null){
               true -> messageRepository.findByRecipientAndSender(userId,from, sortDirection)
               false -> messageRepository.findByRecipient(userId, sortDirection)
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(messages)

    }
}