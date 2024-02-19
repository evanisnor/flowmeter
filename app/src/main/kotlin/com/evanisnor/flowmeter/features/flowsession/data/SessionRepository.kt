package com.evanisnor.flowmeter.features.flowsession.data

import com.evanisnor.flowmeter.AppScope
import com.evanisnor.flowmeter.Database
import com.evanisnor.flowmeter.features.flowsession.data.SessionRepository.Session
import com.squareup.anvil.annotations.ContributesBinding
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Repository for storing and accessing [Session] data
 */
interface SessionRepository {

  data class Session(
    val startedOn: LocalDateTime,
    val completedOn: LocalDateTime,
    val duration: Duration,
  )

  fun addSession(session: Session)

  fun getSessions(startingOn: LocalDateTime, endingOn: LocalDateTime): Collection<Session>

}

/**
 * Database implementation for [SessionRepository]
 */
@ContributesBinding(SessionRepository::class, AppScope::class)
class SessionRepositoryDatabase @Inject constructor(
  database: Database,
  private val zoneOffset: ZoneOffset,
) : SessionRepository {

  private val sessions = database.sessionsQueries

  override fun addSession(session: Session) {
    sessions.insert(
      startedTimestampSec = session.startedOn.toEpochSecond(zoneOffset),
      completedTimestampSec = session.completedOn.toEpochSecond(zoneOffset),
      durationSec = session.duration.inWholeSeconds,
    )
  }

  override fun getSessions(
    startingOn: LocalDateTime,
    endingOn: LocalDateTime,
  ): Collection<Session> =
    sessions.selectBetween(
      start = startingOn.toEpochSecond(zoneOffset),
      end = endingOn.toEpochSecond(zoneOffset)
    ).executeAsList().map {
      Session(
        startedOn = LocalDateTime.ofEpochSecond(it.startedTimestampSec, 0, zoneOffset),
        completedOn = LocalDateTime.ofEpochSecond(it.completedTimestampSec, 0, zoneOffset),
        duration = it.durationSec.seconds,
      )
    }

}
