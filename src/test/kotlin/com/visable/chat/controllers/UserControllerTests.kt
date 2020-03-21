package com.visable.chat.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import com.visable.chat.entities.User
import com.visable.chat.util.DockerCompose
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.ClassRule
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner
import org.junit.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.post
import com.fasterxml.jackson.databind.ObjectMapper
import com.visable.chat.entities.Message
import com.visable.chat.util.SingletonDBAndQueue
import org.apache.commons.collections.MultiMap
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.AbstractMessageSource
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.util.LinkedMultiValueMap
import org.springframework.transaction.annotation.Transactional
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.*
import com.visable.chat.controllers.RestErrorHandler.ValidationError


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner::class)
class UserControllerTests : SingletonDBAndQueue(){

    @Autowired
    private lateinit var controller:UserController

    @Autowired
    private lateinit var mapper: ObjectMapper

    @Autowired
    private lateinit var messageSource: ReloadableResourceBundleMessageSource

    @Autowired
    lateinit var restTemplate: TestRestTemplate


    @After
    fun cleanDB(){
        controller.userRepository.deleteAll()
        controller.messageRepository.deleteAll()
    }
    @Test
    fun contextLoads(){
        assertThat(controller).isNotNull()
    }

    @Test
    fun createUniqueUser(){
        // create headers
        var headers : HttpHeaders = HttpHeaders()
        // set `content-type` header
        headers.contentType = MediaType.APPLICATION_JSON


        // create a post object
        val u = User("testUser")

        // build the request
        val entity: HttpEntity<User> = HttpEntity<User>(u, headers);
        val result: ResponseEntity<User> = restTemplate.postForEntity("/users", entity, User::class)
        assertThat(result.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(result.body!!.nickname).isEqualTo("testUser")
    }

   @Test
    fun createDuplicateUser(){

       // create headers
       var headers : HttpHeaders = HttpHeaders()
       // set `content-type` header
       headers.contentType = MediaType.APPLICATION_JSON

       // build the request
       val saveFirstUser: ResponseEntity<User> = restTemplate.postForEntity("/users",
               HttpEntity<User>(User("testUser"), headers),
               User::class)
       assertThat(saveFirstUser.statusCode).isEqualTo(HttpStatus.CREATED)
       assertThat(saveFirstUser.body!!.nickname).isEqualTo("testUser")


       val error: ResponseEntity<ValidationError> = restTemplate.postForEntity("/users",
               HttpEntity<User>(User("testUser"), headers),
               User::class)
       assertThat(error.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
       val errorMessage = messageSource.getMessage("error.nickname.duplicate", null, LocaleContextHolder.getLocale())
       assertThat(error.body!!.error).isEqualTo(errorMessage)
    }

       @Test
       fun getUserIdBasedOnNickname(){
           var headers : HttpHeaders = HttpHeaders()
           // set `content-type` header
           headers.contentType = MediaType.APPLICATION_JSON

           val user: ResponseEntity<User> = restTemplate.postForEntity("/users",
                   HttpEntity<User>(User("testUser1"), headers),
                   User::class)
           assertThat(user.statusCode).isEqualTo(HttpStatus.CREATED)
           // build the request
           val returnedUser: ResponseEntity<User> = restTemplate.getForEntity("/users/"+user.body!!.nickname, User::class)
           assertThat(returnedUser.statusCode).isEqualTo(HttpStatus.OK)
           assertThat(returnedUser.body!!.nickname).isEqualTo(user.body!!.nickname)
       }

       @Test
       fun getNonExistantUserIdBasedOnNickname(){
           val error: ResponseEntity<ValidationError> = restTemplate.getForEntity("/users/IDONTNOW", ValidationError::class)
           assertThat(error.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
           val errorMessage = messageSource.getMessage("error.user.notexist", null, LocaleContextHolder.getLocale())
           assertThat(error.body!!.error).isEqualTo(errorMessage)
       }

       @Test
       fun sendMessageToAnotherUser(){
           var headers : HttpHeaders = HttpHeaders()
           // set `content-type` header
           headers.contentType = MediaType.APPLICATION_JSON

           val user1: ResponseEntity<User> = restTemplate.postForEntity("/users",
                   HttpEntity<User>(User("testUser1"), headers),
                   User::class)

           assertThat(user1.statusCode).isEqualTo(HttpStatus.CREATED)
           val user2: ResponseEntity<User> = restTemplate.postForEntity("/users",
                   HttpEntity<User>(User("testUser2"), headers),
                   User::class)
           assertThat(user2.statusCode).isEqualTo(HttpStatus.CREATED)

           val message: ResponseEntity<Message> = restTemplate.postForEntity("/users/"+user1.body!!.getId()+"/messages",
                   HttpEntity<Message>(Message(null,user2.body!!.getId(),"Hello World"), headers),
                   Message::class)
           assertThat(message.statusCode).isEqualTo(HttpStatus.ACCEPTED)
       }

       @Test
       fun sendMessageToSelf(){
           var headers : HttpHeaders = HttpHeaders()
           // set `content-type` header
           headers.contentType = MediaType.APPLICATION_JSON

           val user1: ResponseEntity<User> = restTemplate.postForEntity("/users",
                   HttpEntity<User>(User("testUser"), headers),
                   User::class)

           assertThat(user1.statusCode).isEqualTo(HttpStatus.CREATED)
           val message: ResponseEntity<ValidationError> = restTemplate.postForEntity("/users/"+user1.body!!.getId()+"/messages",
                   HttpEntity<Message>(Message(null,user1.body!!.getId(),"Hello World"), headers),
                   Message::class)
           assertThat(message.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
           val errorMessage = messageSource.getMessage("error.message.selfsend", null, LocaleContextHolder.getLocale())
           assertThat(message.body!!.error).isEqualTo(errorMessage)
       }

       @Test
       fun sendMessageFromNonExistantUser(){
           var headers : HttpHeaders = HttpHeaders()
           // set `content-type` header
           headers.contentType = MediaType.APPLICATION_JSON

           val message: ResponseEntity<ValidationError> = restTemplate.postForEntity("/users/1000/messages",
                   HttpEntity<Message>(Message(null,1,"Hello World"), headers),
                   Message::class)
           assertThat(message.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
           val errorMessage = messageSource.getMessage("error.sender.notexist", null, LocaleContextHolder.getLocale())
           assertThat(message.body!!.error).isEqualTo(errorMessage)
       }

       @Test
       fun sendMessageToNonExistantUser(){
           var headers : HttpHeaders = HttpHeaders()
           // set `content-type` header
           headers.contentType = MediaType.APPLICATION_JSON

           val user1: ResponseEntity<User> = restTemplate.postForEntity("/users",
                   HttpEntity<User>(User("testUser"), headers),
                   User::class)
           assertThat(user1.statusCode).isEqualTo(HttpStatus.CREATED)
           val message: ResponseEntity<ValidationError> = restTemplate.postForEntity("/users/"+user1.body!!.getId()+"/messages",
                   HttpEntity<Message>(Message(null,1000,"Hello World"), headers),
                   Message::class)
           assertThat(message.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
           val errorMessage = messageSource.getMessage("error.recipient.notexist", null, LocaleContextHolder.getLocale())
           assertThat(message.body!!.error).isEqualTo(errorMessage)
       }

       @Test
       fun getAllRecievedMessagesForUser(){
           var headers : HttpHeaders = HttpHeaders()
           // set `content-type` header
           headers.contentType = MediaType.APPLICATION_JSON

           val user1: ResponseEntity<User> = restTemplate.postForEntity("/users",
                   HttpEntity<User>(User("testUser"), headers),
                   User::class)
           assertThat(user1.statusCode).isEqualTo(HttpStatus.CREATED)

           val user2: ResponseEntity<User> = restTemplate.postForEntity("/users",
                   HttpEntity<User>(User("testUser1"), headers),
                   User::class)
           assertThat(user2.statusCode).isEqualTo(HttpStatus.CREATED)

           val message: ResponseEntity<Message> = restTemplate.postForEntity("/users/"+user1.body!!.getId()+"/messages",
                   HttpEntity<Message>(Message(null,user2.body!!.getId(),"Hello World"), headers),
                   Message::class)
           assertThat(message.statusCode).isEqualTo(HttpStatus.ACCEPTED)

           val message1: ResponseEntity<Message> = restTemplate.postForEntity("/users/"+user1.body!!.getId()+"/messages",
                   HttpEntity<Message>(Message(null,user2.body!!.getId(),"Hello Test"), headers),
                   Message::class)
           assertThat(message1.statusCode).isEqualTo(HttpStatus.ACCEPTED)

           val messageList: ResponseEntity<List<LinkedHashMap<String,Any>>> = restTemplate.getForEntity("/users/"+user2.body!!.getId()+"/messages", Message::class)

           assertThat(messageList.statusCode).isEqualTo(HttpStatus.OK)

           for(m in messageList.body!!){
               assertThat(m.get("sender")).isEqualTo(user1.body!!.getId()!!.toInt())
               assertThat(m.get("recipient")).isEqualTo(user2.body!!.getId()!!.toInt())
           }
       }

       @Test
       fun getAllRecievedMessagesFromSpecificUser(){
           var headers : HttpHeaders = HttpHeaders()
           // set `content-type` header
           headers.contentType = MediaType.APPLICATION_JSON

           val user1: ResponseEntity<User> = restTemplate.postForEntity("/users",
                   HttpEntity<User>(User("testUser"), headers),
                   User::class)
           assertThat(user1.statusCode).isEqualTo(HttpStatus.CREATED)

           val user2: ResponseEntity<User> = restTemplate.postForEntity("/users",
                   HttpEntity<User>(User("testUser1"), headers),
                   User::class)
           assertThat(user2.statusCode).isEqualTo(HttpStatus.CREATED)

           val user3: ResponseEntity<User> = restTemplate.postForEntity("/users",
                   HttpEntity<User>(User("testUser2"), headers),
                   User::class)
           assertThat(user3.statusCode).isEqualTo(HttpStatus.CREATED)

           val message1: ResponseEntity<Message> = restTemplate.postForEntity("/users/"+user1.body!!.getId()+"/messages",
                   HttpEntity<Message>(Message(null,user2.body!!.getId(),"Hello World"), headers),
                   Message::class)
           assertThat(message1.statusCode).isEqualTo(HttpStatus.ACCEPTED)

           val message2: ResponseEntity<Message> = restTemplate.postForEntity("/users/"+user1.body!!.getId()+"/messages",
                   HttpEntity<Message>(Message(null,user2.body!!.getId(),"Hello Test"), headers),
                   Message::class)
           assertThat(message2.statusCode).isEqualTo(HttpStatus.ACCEPTED)

           val message3: ResponseEntity<Message> = restTemplate.postForEntity("/users/"+user3.body!!.getId()+"/messages",
                   HttpEntity<Message>(Message(null,user2.body!!.getId(),"Hello Visable"), headers),
                   Message::class)
           assertThat(message3.statusCode).isEqualTo(HttpStatus.ACCEPTED)

           val messageList: ResponseEntity<List<LinkedHashMap<String,Any>>> =
                   restTemplate.getForEntity("/users/"+user2.body!!.getId()+"/messages?from="+user1.body!!.getId(), Message::class)

           assertThat(messageList.statusCode).isEqualTo(HttpStatus.OK)

           for(m in messageList.body!!){
               assertThat(m.get("sender")).isEqualTo(user1.body!!.getId()!!.toInt())
               assertThat(m.get("recipient")).isEqualTo(user2.body!!.getId()!!.toInt())
           }
       }

        @Test
        fun getAllSentMessagesForAUser(){
            var headers : HttpHeaders = HttpHeaders()
            // set `content-type` header
            headers.contentType = MediaType.APPLICATION_JSON

            val user1: ResponseEntity<User> = restTemplate.postForEntity("/users",
                    HttpEntity<User>(User("testUser"), headers),
                    User::class)
            assertThat(user1.statusCode).isEqualTo(HttpStatus.CREATED)

            val user2: ResponseEntity<User> = restTemplate.postForEntity("/users",
                    HttpEntity<User>(User("testUser1"), headers),
                    User::class)
            assertThat(user2.statusCode).isEqualTo(HttpStatus.CREATED)

            val user3: ResponseEntity<User> = restTemplate.postForEntity("/users",
                    HttpEntity<User>(User("testUser2"), headers),
                    User::class)
            assertThat(user3.statusCode).isEqualTo(HttpStatus.CREATED)

            val message1: ResponseEntity<Message> = restTemplate.postForEntity("/users/"+user1.body!!.getId()+"/messages",
                    HttpEntity<Message>(Message(null,user2.body!!.getId(),"Hello World"), headers),
                    Message::class)
            assertThat(message1.statusCode).isEqualTo(HttpStatus.ACCEPTED)

            val message2: ResponseEntity<Message> = restTemplate.postForEntity("/users/"+user1.body!!.getId()+"/messages",
                    HttpEntity<Message>(Message(null,user2.body!!.getId(),"Hello Test"), headers),
                    Message::class)
            assertThat(message2.statusCode).isEqualTo(HttpStatus.ACCEPTED)

            val message3: ResponseEntity<Message> = restTemplate.postForEntity("/users/"+user3.body!!.getId()+"/messages",
                    HttpEntity<Message>(Message(null,user2.body!!.getId(),"Hello Visable"), headers),
                    Message::class)
            assertThat(message3.statusCode).isEqualTo(HttpStatus.ACCEPTED)

            val messageList: ResponseEntity<List<LinkedHashMap<String,Any>>> =
                    restTemplate.getForEntity("/users/"+user1.body!!.getId()+"/messages?sent=true", Message::class)

            assertThat(messageList.statusCode).isEqualTo(HttpStatus.OK)
            assertThat(messageList.body!!.size).isEqualTo(2)

            for(m in messageList.body!!){
                assertThat(m.get("sender")).isEqualTo(user1.body!!.getId()!!.toInt())
                assertThat(m.get("recipient")).isEqualTo(user2.body!!.getId()!!.toInt())
            }
        }

    @Test
    fun getAllSentMessagesToASpecificUser(){
        var headers : HttpHeaders = HttpHeaders()
        // set `content-type` header
        headers.contentType = MediaType.APPLICATION_JSON

        val user1: ResponseEntity<User> = restTemplate.postForEntity("/users",
                HttpEntity<User>(User("testUser"), headers),
                User::class)
        assertThat(user1.statusCode).isEqualTo(HttpStatus.CREATED)

        val user2: ResponseEntity<User> = restTemplate.postForEntity("/users",
                HttpEntity<User>(User("testUser1"), headers),
                User::class)
        assertThat(user2.statusCode).isEqualTo(HttpStatus.CREATED)

        val user3: ResponseEntity<User> = restTemplate.postForEntity("/users",
                HttpEntity<User>(User("testUser2"), headers),
                User::class)
        assertThat(user3.statusCode).isEqualTo(HttpStatus.CREATED)

        val message1: ResponseEntity<Message> = restTemplate.postForEntity("/users/"+user1.body!!.getId()+"/messages",
                HttpEntity<Message>(Message(null,user2.body!!.getId(),"Hello World"), headers),
                Message::class)
        assertThat(message1.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        val message2: ResponseEntity<Message> = restTemplate.postForEntity("/users/"+user1.body!!.getId()+"/messages",
                HttpEntity<Message>(Message(null,user3.body!!.getId(),"Hello Test"), headers),
                Message::class)
        assertThat(message2.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        val message3: ResponseEntity<Message> = restTemplate.postForEntity("/users/"+user3.body!!.getId()+"/messages",
                HttpEntity<Message>(Message(null,user1.body!!.getId(),"Hello Visable"), headers),
                Message::class)
        assertThat(message3.statusCode).isEqualTo(HttpStatus.ACCEPTED)

        val messageList: ResponseEntity<List<LinkedHashMap<String,Any>>> =
                restTemplate.getForEntity("/users/"+user1.body!!.getId()+"/messages?sent=true&to="+user2.body!!.getId(), Message::class)

        assertThat(messageList.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(messageList.body!!.size).isEqualTo(1)

        for(m in messageList.body!!){
            assertThat(m.get("sender")).isEqualTo(user1.body!!.getId()!!.toInt())
            assertThat(m.get("recipient")).isEqualTo(user2.body!!.getId()!!.toInt())
        }
    }

   }