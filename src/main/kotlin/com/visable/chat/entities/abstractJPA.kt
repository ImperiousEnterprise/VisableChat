package com.visable.chat.entities

import org.hibernate.annotations.CreationTimestamp
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.domain.Persistable
import org.springframework.data.util.ProxyUtils
import java.io.Serializable
import java.util.*
import javax.persistence.*

@MappedSuperclass
abstract class AbstractJpaPersistable<T : Serializable>: Serializable {

    companion object {
        private val serialVersionUID = -5554308939380869754L
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private var id: T? = null

    @Column(name = "createdAt", nullable = false, updatable = false)
    @CreationTimestamp
    val createdAt: Date? = null

    fun getId(): T? {
        return id
    }

    override fun equals(other: Any?): Boolean {
        other ?: return false

        if (this === other) return true

        if (javaClass != ProxyUtils.getUserClass(other)) return false

        other as AbstractJpaPersistable<*>

        return if (null == this.getId()) false else this.getId() == other.getId()
    }

    override fun hashCode(): Int {
        return 31
    }

    override fun toString() = "Entity of type ${this.javaClass.name} with id: $id"

}