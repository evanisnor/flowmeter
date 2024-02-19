package com.evanisnor.flowmeter.features.home

import com.evanisnor.flowmeter.features.flowtimesession.ui.SessionContent
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize

@Parcelize
data object HomeScreen : Screen {

  data class State(
    val sessionContent: SessionContent,
    val eventSink: (Event) -> Unit
  ) : CircuitUiState {

  }

  interface Event : CircuitUiEvent
}
