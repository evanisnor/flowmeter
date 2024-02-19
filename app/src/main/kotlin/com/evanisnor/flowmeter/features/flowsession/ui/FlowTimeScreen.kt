package com.evanisnor.flowmeter.features.flowsession.ui

import kotlin.time.Duration

object FlowTimeScreen {

  sealed interface State {
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

  interface Event {
    data object NewSession : Event
    data object EndSession : Event
  }
}
