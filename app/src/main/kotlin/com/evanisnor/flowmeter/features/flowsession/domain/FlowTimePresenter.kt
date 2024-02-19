package com.evanisnor.flowmeter.features.flowsession.domain

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen.State
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen.State.StartNew
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen.Event
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen.Event.EndSession
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen.Event.NewSession
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.produceRetainedState
import com.slack.circuit.runtime.presenter.Presenter
import javax.inject.Inject

@CircuitInject(FlowTimeScreen::class, AppScope::class)
class FlowTimePresenter @Inject constructor() : Presenter<State> {

  @Composable
  override fun present(): State {
    val eventSink : (Event) -> Unit = { event ->
      when (event) {
        is NewSession -> {}
        is EndSession -> {}
      }
    }

    val state by produceRetainedState(initialValue = StartNew(eventSink)) {
    }
    return state
  }

}
