import com.somegame.faculty.Faculty
import com.somegame.faculty.FacultyRepository
import io.mockk.every
import io.mockk.mockk

val testFaculty1 = createMockFaculty(1, "MM")
val testFaculty2 = createMockFaculty(2, "BadFaculty")

fun createMockFaculty(id: Int, name: String): Faculty {
    val faculty = mockk<Faculty>()
    every { faculty.getId() } returns id
    every { faculty.name } returns name
    return faculty
}

fun createMockFacultyRepository(): FacultyRepository {
    val copiedFaculties = listOf(testFaculty1, testFaculty2)

    val facultyRepository = mockk<FacultyRepository>()

    every { facultyRepository.getAllFaculties() } returns copiedFaculties
    every { facultyRepository.getFacultyById(any()) } answers {
        copiedFaculties.find { it.getId() == firstArg() }
    }

    return facultyRepository
}
