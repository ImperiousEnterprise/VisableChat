package com.visable.chat.repositories

import com.visable.chat.entities.Message
import com.visable.chat.util.DockerCompose
import com.visable.chat.util.SingletonDBAndQueue
import org.junit.ClassRule
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Sort
import org.springframework.test.context.junit4.SpringRunner
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.After

@SpringBootTest
@RunWith(SpringRunner::class)
class MessageRepositoryIntegrationTests: SingletonDBAndQueue() {

    companion object {
        private val Message1 = Message(1,2,"Hello")
        private val Message2 = Message(2,3,"World")
        private val Message3 = Message(3,2,"Macho")
    }

    @Autowired
    lateinit var messageRepository: MessageRepository

    @After
    fun cleanUpDB(){
        messageRepository.deleteAll()
    }
    @Test
    fun givenMessageEntity_whenInsertWithSave_ThenMessageIsPersisted() {
        messageRepository.save(Message1)
        assertThat(Message1.getId()).isNotNull()
    }

    @Test
    fun givenMessageEntity_whenInsertWithSaveAndFlush_ThenMessageIsPersisted() {
        messageRepository.saveAndFlush(Message2)
        assertThat(Message2.getId()).isNotNull()
    }

    @Test
    fun findReciepientByID_IN_ASC_ORDER(){
        messageRepository.saveAll(mutableListOf<Message>(Message(1,2,"Hello"),Message(3,2,"Macho")))
        val ap: List<Message> =  messageRepository.findByRecipient(2, Sort.by("createdAt").ascending())
        assertThat(ap.size).isEqualTo(2)
        val dif = ap[0].createdAt!!.time - ap[1].createdAt!!.time
        assert(dif < 0)
    }
    @Test
    fun findReciepientByID_IN_DESC_ORDER(){
        messageRepository.saveAll(mutableListOf<Message>(Message1,Message3))
        val ap: List<Message> =  messageRepository.findByRecipient(2, Sort.by("createdAt").descending())
        assertThat(ap.size).isEqualTo(2)
        val dif = ap[0].createdAt!!.time - ap[1].createdAt!!.time
        assert(dif > 0)
    }
    @Test
    fun findNonExistentReciepientByID(){
        val ap: List<Message> =  messageRepository.findByRecipient(0, Sort.by("createdAt").ascending())
        assertThat(ap.size).isEqualTo(0)
    }
    @Test
    fun findSenderByID_IN_ASC_ORDER(){
        messageRepository.saveAll(mutableListOf<Message>(
                Message(1,2,"Hello")
                ,Message(1,3,"Talk")))
        val ap: List<Message> =  messageRepository.findBySender(1, Sort.by("createdAt").ascending())
        assertThat(ap.size).isEqualTo(2)
        val dif = ap[0].createdAt!!.time - ap[1].createdAt!!.time
        assert(dif < 0)
    }
    @Test
    fun findSenderByID_IN_DESC_ORDER(){
        messageRepository.saveAll(mutableListOf<Message>(Message1,Message2,Message(1,3,"Talk")))
        val ap: List<Message> =  messageRepository.findBySender(1, Sort.by("createdAt").descending())
        assertThat(ap.size).isEqualTo(2)
        val dif = ap[0].createdAt!!.time - ap[1].createdAt!!.time
        assert(dif > 0)
    }
    @Test
    fun findNonExistentSenderByID(){
        val ap: List<Message> =  messageRepository.findBySender(0, Sort.by("createdAt").ascending())
        assertThat(ap.size).isEqualTo(0)
    }
    @Test
    fun findSenderAndRecipientByID_IN_ASC_ORDER(){
        messageRepository.saveAll(mutableListOf<Message>(Message1,
                Message(2,1,"I"),
                Message(2,1,"Heart"),
                Message(2,1,"Radio"),
                Message(1,2,"Talk")))
        val ap: List<Message> =  messageRepository.findByRecipientAndSender(1, 2,Sort.by("createdAt").ascending())
        assertThat(ap.size).isEqualTo(3)
        for(x in 1..(ap.size-1)){
            var dif = ap[x-1].createdAt!!.time - ap[x].createdAt!!.time
            assert(dif < 0)
        }
    }
    @Test
    fun findSenderAndRecipientByID_IN_DESC_ORDER(){
        messageRepository.saveAll(mutableListOf<Message>(Message1,
                Message(2,1,"I"),
                Message(2,1,"Heart"),
                Message(2,1,"Radio"),
                Message(1,2,"Talk")))
        val ap: List<Message> =  messageRepository.findByRecipientAndSender(1, 2,Sort.by("createdAt").descending())
        assertThat(ap.size).isEqualTo(3)
        for(x in 1..(ap.size-1)){
            var dif = ap[x-1].createdAt!!.time - ap[x].createdAt!!.time
            assert(dif > 0)
        }
    }

}
