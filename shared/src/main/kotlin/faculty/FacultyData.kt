package faculty

import kotlinx.serialization.Serializable

@Serializable
data class FacultyData(val id: Int, val name: String, val points: Int = 0)

enum class FixedFaculties(val id: Int, val facultyName: String) {
    MATMECH(1, "MatMech"),
    PMPU(2, "PMPU"),
}
