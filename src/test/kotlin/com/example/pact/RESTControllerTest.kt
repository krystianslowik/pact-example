package com.example.pact

import au.com.dius.pact.provider.junit5.HttpTestTarget
import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider
import au.com.dius.pact.provider.junitsupport.Provider
import au.com.dius.pact.provider.junitsupport.loader.PactBroker
import au.com.dius.pact.provider.junitsupport.State
import au.com.dius.pact.provider.junitsupport.StateChangeAction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@Provider("pact-example")
@PactBroker(url = "http://jrse.firewall-gateway.com:9292")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ProjectProviderPactTest {

    @Autowired
    lateinit var projectRepository: ProjectRepository

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider::class)
    fun pactVerificationTestTemplate(context: PactVerificationContext) {
        context.verifyInteraction()
    }

    @BeforeEach
    fun before(context: PactVerificationContext) {
        context.target = HttpTestTarget("localhost", 8080, "/")
    }

    @State("project with ID 123 exists")
    fun setupProject123() {
        // Insert or update the project in the repository to set up this state
        projectRepository.save(Project(id = "123", name = "Agile Transformation", status = "Active"))
    }

    @State("no projects exist", "TEARDOWN")
    fun clearProjects() {
        // Remove all projects from the repository
        projectRepository.deleteAll()
    }
}