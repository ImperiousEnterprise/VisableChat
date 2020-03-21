package com.visable.chat.util

import org.junit.After
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.io.File

abstract class SingletonDBAndQueue(){
    companion object {
        val environment: DockerCompose = ContainerMP.instance.apply {
            start()
        }
    }

}

object ContainerMP {
    val instance by lazy { startMongoContainer() }

    private fun startMongoContainer() = DockerCompose("src/test/resources/compose-test.yml")
            .withExposedService("postgres", 5432)
            .withExposedService("rabbitmq", 5672)
}

class DockerCompose(fileUrl: String): DockerComposeContainer<DockerCompose>(File(fileUrl))