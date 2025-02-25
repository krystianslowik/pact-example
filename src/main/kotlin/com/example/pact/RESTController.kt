package com.example.pact

import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.beans.factory.annotation.Autowired

@Entity
data class Project(
    @Id
    val id: String = "",
    val name: String = "",
    val status: String = ""
)

@RestController
@RequestMapping("/projects")
class ProjectController(@Autowired val projectRepository: ProjectRepository) {

    @GetMapping("/{id}")
    fun getProject(@PathVariable id: String): ResponseEntity<Project> {
        val projectOpt = projectRepository.findById(id)
        return if (projectOpt.isPresent) {
            ResponseEntity.ok(projectOpt.get())
        } else {
            ResponseEntity.notFound().build()
        }
    }
}