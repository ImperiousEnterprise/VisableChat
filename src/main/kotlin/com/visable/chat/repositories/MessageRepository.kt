package com.visable.chat.repositories

import com.visable.chat.entities.Message
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MessageRepository : JpaRepository<Message, Long> {
    fun findByRecipient(to:Long, sort : Sort):List<Message>
    fun findBySender(from:Long, sort : Sort):List<Message>
    fun findByRecipientAndSender(to: Long, from: Long, sort : Sort): List<Message>
}