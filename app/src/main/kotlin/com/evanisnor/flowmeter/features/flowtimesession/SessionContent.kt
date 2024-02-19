package com.evanisnor.flowmeter.features.flowtimesession

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import kotlin.time.Duration

sealed interface SessionContent : CircuitUiState {
  val eventSink : (SessionEvent) -> Unit

  data class StartNew(
    override val eventSink: (SessionEvent) -> Unit,
  ): SessionContent

  data class SessionInProgress(
    val duration: String,
    override val eventSink: (SessionEvent) -> Unit,
  ) : SessionContent

  data class SessionComplete(
    val duration: String,
    val breakRecommendation: Duration,
    override val eventSink: (SessionEvent) -> Unit,
  ) : SessionContent


  interface SessionEvent : CircuitUiEvent {
    data object NewSession : SessionEvent
    data object EndSession : SessionEvent
  }
}
