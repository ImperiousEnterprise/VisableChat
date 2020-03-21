package com.visable.chat.entities

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
@Table(name = "messages")
class Message(
        @Column(name = "sender_id", nullable = false)
        var sender:Long? = null,
        @Column(name = "receiver_id", nullable = false)
        @field:NotNull(message = "error.recipient.notnull")
        val recipient:Long? = null,
        @Column(name = "message", nullable = false)
        @field:NotBlank(message = "error.message.notblank")
        val message: String? = null): AbstractJpaPersistable<Long>()