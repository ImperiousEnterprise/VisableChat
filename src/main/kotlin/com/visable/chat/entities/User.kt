package com.visable.chat.entities


import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.NotBlank


@Entity
@Table(name = "users")
class User( @Column(name = "nickname", unique = true, nullable = false)
            @field:NotBlank(message = "error.nickname.notblank")
            val nickname: String? = null): AbstractJpaPersistable<Long>()

