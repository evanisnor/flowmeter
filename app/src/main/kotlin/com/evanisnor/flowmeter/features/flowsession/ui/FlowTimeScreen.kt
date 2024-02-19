package com.evanisnor.flowmeter.features.flowsession.ui

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize
import kotlin.time.Duration

@Parcelize
data object FlowTimeScreen : Screen {

  sealed interface State : CircuitUiState {
    val eventSink: (Event) -> Unit

    data class StartNew(
      override val eventSink: (Event) -> Unit,
    ) : State

    data class SessionInProgress(
      val duration: String,
      override val eventSink: (Event) -> Unit,
    ) : State

    data class SessionComplete(
      val duration: String,
      val breakRecommendation: Duration,
      override val eventSink: (Event) -> Unit,
    ) : State
  }

  interface Event : CircuitUiEvent {
    data object NewSession : Event
    data object EndSession : Event
  }
}
