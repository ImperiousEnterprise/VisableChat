package com.visable.chat.repositories

import com.visable.chat.entities.User
import com.visable.chat.util.DockerCompose
import com.visable.chat.util.SingletonDBAndQueue
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.After
import org.junit.ClassRule


import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@SpringBootTest
@RunWith(SpringRunner::class)
class UserRepositoryIntegrationTests: SingletonDBAndQueue() {

    companion object {
        private val User1 = User("John")
        private val User2 = User("Alice")
    }

    @Autowired
    lateinit var userRepository: UserRepository

    @After
    fun cleanUPDB(){
        userRepository.deleteAll()
    }

    @Test
    fun givenUserEntity_whenInsertWithSave_ThenUserIsPersisted() {
        userRepository.save(User1)
        assertThat(User1).isNotNull()
    }

    @Test
    fun givenUserEntity_whenInsertWithSaveAndFlush_ThenUserIsPersisted() {
        userRepository.saveAndFlush(User2)
        assertThat(User2).isNotNull()
    }

    @Test
    fun givenUserNickname_Successfully_find_User() {
        val savedUser :User = userRepository.save(User1)
        val u :User = userRepository.findByNickname(User1.nickname!!)
        assertThat(u).isNotNull()
        assertThat(u).isEqualTo(savedUser)
    }

    @Test
    fun givenUserId_CheckIfExists() {
        val u : User = userRepository.save(User1)
        val res : Boolean = userRepository.existsById(u.getId()!!)
        assertThat(res).isTrue()
    }

    @Test
    fun givenNonExistantUserId_CheckIfExists() {
        userRepository.save(User1)
        val res : Boolean = userRepository.existsById(5)
        assertThat(res).isFalse()
    }

}