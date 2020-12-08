package com.somegame.faculty

import com.somegame.requiredIdParameter
import com.somegame.responseExceptions.NotFoundException
import faculty.GET_ALL_FACULTIES_ENDPOINT
import faculty.GET_LEADING_FACULTY
import faculty.GET_SINGLE_FACULTY_ENDPOINT
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject

fun Routing.faculties() {
    val facultyRepository: FacultyRepository by inject()
    val facultyPointsManager: FacultyPointsManager by inject()

    get(GET_ALL_FACULTIES_ENDPOINT) {
        val faculties = facultyRepository.getAllFaculties()
        call.respond(faculties.map { it.publicData() })
    }

    get(GET_SINGLE_FACULTY_ENDPOINT) {
        val facultyId = call.requiredIdParameter()
        val faculty = facultyRepository.getFacultyById(facultyId)
            ?: throw NotFoundException("Faculty with id=$facultyId not found")
        call.respond(faculty.publicData())
    }

    get(GET_LEADING_FACULTY) {
        val faculty = facultyPointsManager.getLeadingFaculty()
        call.respond(faculty.publicData())
    }
}
