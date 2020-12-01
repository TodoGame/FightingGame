package faculty

import com.somegame.SimpleKtorTest
import com.somegame.TestUtils.addJsonContentHeader
import com.somegame.TestUtils.addJwtHeader
import com.somegame.faculty.faculties
import com.somegame.faculty.publicData
import testFaculty1
import testFaculty2
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class FacultiesRoutingKtTest : SimpleKtorTest() {
    private fun withApp(block: TestApplicationEngine.() -> Unit) = withBaseApp({
        routing {
            faculties()
        }
    }) {
        block()
    }

    @Test
    fun `get all faculties should respond with all faculties`() = withApp {
        handleRequest {
            uri = GET_ALL_FACULTIES_ENDPOINT
            method = HttpMethod.Get
            addJsonContentHeader()
            addJwtHeader("user1")
        }.apply {
            assert(requestHandled) { "Request not handled" }
            val faculties = response.content?.let { Json.decodeFromString<List<FacultyData>>(it) }
            assertEquals(listOf(testFaculty1.publicData(), testFaculty2.publicData()), faculties)
        }
    }

    @Test
    fun `get faculty by id=1 should respond with first faculty`() = withApp {
        handleRequest {
            uri = "$GET_SINGLE_FACULTY_ENDPOINT?id=1"
            method = HttpMethod.Get
            addJsonContentHeader()
            addJwtHeader("user1")
        }.apply {
            assert(requestHandled) { "Request not handled" }
            val faculty = response.content?.let { Json.decodeFromString<FacultyData>(it) }
            assertEquals(testFaculty1.publicData(), faculty)
        }
    }

    @Test
    fun `get faculty by id=1 should respond with second faculty`() = withApp {
        handleRequest {
            uri = "$GET_SINGLE_FACULTY_ENDPOINT?id=2"
            method = HttpMethod.Get
            addJsonContentHeader()
            addJwtHeader("user1")
        }.apply {
            assert(requestHandled) { "Request not handled" }
            val faculty = response.content?.let { Json.decodeFromString<FacultyData>(it) }
            assertEquals(testFaculty2.publicData(), faculty)
        }
    }

    @Test
    fun `get faculty should respond with 404 for id=3`() = withApp {
        handleRequest {
            uri = "$GET_SINGLE_FACULTY_ENDPOINT?id=3"
            method = HttpMethod.Get
            addJsonContentHeader()
            addJwtHeader("user1")
        }.apply {
            assert(requestHandled) { "Request not handled" }
            assertEquals(HttpStatusCode.NotFound, response.status())
        }
    }

    @Test
    fun `get faculty should respond with BadRequest for id=stringValue`() = withApp {
        handleRequest {
            uri = "$GET_SINGLE_FACULTY_ENDPOINT?id=stringValue"
            method = HttpMethod.Get
            addJsonContentHeader()
            addJwtHeader("user1")
        }.apply {
            assert(requestHandled) { "Request not handled" }
            assertEquals(HttpStatusCode.BadRequest, response.status())
        }
    }
}