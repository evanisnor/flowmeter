package com.evanisnor.flowmeter.features.flowsession.domain

import androidx.compose.runtime.Composable
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen.Event
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen.State
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.presenter.Presenter
import javax.inject.Inject

@CircuitInject(FlowTimeScreen::class, AppScope::class)
class FlowTimePresenter @Inject constructor(
  private val contentPresenter: FlowTimeSessionContentPresenter
) : Presenter<State> {

  @Composable
  override fun present(): State {
    val eventSink: (Event) -> Unit = { _ -> }

    return State(
      sessionContent = contentPresenter.present(),
      eventSink = eventSink
    )
  }

}
