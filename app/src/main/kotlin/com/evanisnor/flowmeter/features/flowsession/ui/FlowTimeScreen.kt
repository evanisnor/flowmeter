package com.evanisnor.flowmeter.features.flowsession.ui

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize
import kotlin.time.Duration

@Parcelize
data object FlowTimeScreen : Screen {

  data class State(
    val content: Content,
    val eventSink: (Event) -> Unit
  ) : CircuitUiState {

    sealed interface Content {
      val eventSink : (SessionEvent) -> Unit

      data class StartNew(
        override val eventSink: (SessionEvent) -> Unit,
      ): Content

      data class SessionInProgress(
        val duration: String,
        override val eventSink: (SessionEvent) -> Unit,
      ) : Content

      data class SessionComplete(
        val duration: String,
        val breakRecommendation: Duration,
        override val eventSink: (SessionEvent) -> Unit,
      ) : Content


      interface SessionEvent : CircuitUiEvent {
        data object NewSession : SessionEvent
        data object EndSession : SessionEvent
      }
    }
  }

  interface Event : CircuitUiEvent
}
