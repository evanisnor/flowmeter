package com.evanisnor.flowmeter.features.home

import androidx.compose.runtime.Composable
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.features.flowtimesession.SessionContentPresenter
import com.evanisnor.flowmeter.features.home.HomeScreen.Event
import com.evanisnor.flowmeter.features.home.HomeScreen.State
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.presenter.Presenter
import javax.inject.Inject

@CircuitInject(HomeScreen::class, AppScope::class)
class HomePresenter @Inject constructor(
  private val contentPresenter: SessionContentPresenter
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
