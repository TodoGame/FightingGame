package com.somegame.faculty

import com.somegame.user.User
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.slf4j.LoggerFactory
import subscription.FacultyPointsUpdate
import subscription.LeadingFacultyUpdate

class FacultyPointsManager : KoinComponent {
    companion object {
        const val WINNING_FACULTY_PRIZE = 5
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    private val facultyRepository: FacultyRepository by inject()

    private var leadingFaculty = facultyRepository.getLeadingFaculty() ?: throw IllegalStateException("No faculties found")

    private val mutex = Mutex()
    private val allFacultiesListeners = mutableSetOf<suspend (FacultyPointsUpdate) -> Unit>()

    private val leadingFacultyMutex = Mutex()
    private val leadingFacultyListeners = mutableSetOf<suspend (LeadingFacultyUpdate) -> Unit>()

    suspend fun onFacultyMemberWin(user: User) {
        val faculty = user.loadFaculty()
        val id = faculty.getId()
        faculty.givePoints(WINNING_FACULTY_PRIZE)
        val pointsUpdate = FacultyPointsUpdate(id, faculty.points, user.username)
        notifyAllFacultiesListeners(pointsUpdate)
        if (faculty == leadingFaculty) {
            notifyLeadingFacultyListeners(LeadingFacultyUpdate(id, faculty.points))
        } else if (faculty.points > leadingFaculty.points) {
            leadingFaculty = faculty
            notifyLeadingFacultyListeners(LeadingFacultyUpdate(id, faculty.points))
        }
    }

    suspend fun subscribeOnAllFacultiesPointsUpdates(listener: suspend (FacultyPointsUpdate) -> Unit) = mutex.withLock {
        logger.info("Added a subscription to all faculties points updates")
        allFacultiesListeners.add(listener)
    }

    suspend fun unsubscribeFromAllFacultiesPointsUpdates(listener: suspend (FacultyPointsUpdate) -> Unit) =
        mutex.withLock {
            logger.info("Removed a subscription from all faculties points updates")
            allFacultiesListeners.remove(listener)
        }

    private suspend fun notifyAllFacultiesListeners(facultyPointsUpdate: FacultyPointsUpdate) = mutex.withLock {
        logger.info("Notifying listeners to all faculty points updates")
        for (listener in allFacultiesListeners) {
            listener(facultyPointsUpdate)
        }
    }

    suspend fun subscribeOnLeadingFacultyUpdates(listener: suspend (LeadingFacultyUpdate) -> Unit) =
        leadingFacultyMutex.withLock {
            logger.info("Added a subscription to leading faculty updates")
            leadingFacultyListeners.add(listener)
        }

    suspend fun unsubscribeFromLeadingFacultyUpdates(listener: suspend (LeadingFacultyUpdate) -> Unit) =
        leadingFacultyMutex.withLock {
            logger.info("Removed a subscription from leading faculty updates")
            leadingFacultyListeners.remove(listener)
        }

    private suspend fun notifyLeadingFacultyListeners(update: LeadingFacultyUpdate) = leadingFacultyMutex.withLock {
        logger.info("Notifying listeners to leading faculty updates")
        for (listener in leadingFacultyListeners) {
            listener(update)
        }
    }
}
