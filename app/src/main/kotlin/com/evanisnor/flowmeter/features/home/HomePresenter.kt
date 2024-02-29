package com.evanisnor.flowmeter.features.home

import androidx.compose.runtime.Composable
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.features.flowtimesession.SessionContentPresenter
import com.evanisnor.flowmeter.features.home.HomeScreen.Event
import com.evanisnor.flowmeter.features.home.HomeScreen.State
import com.evanisnor.flowmeter.features.settings.SettingsScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class HomePresenter
@AssistedInject
constructor(
  @Assisted private val navigator: Navigator,
  private val contentPresenter: SessionContentPresenter,
) : Presenter<State> {
  @Composable
  override fun present(): State {
    val eventSink: (Event) -> Unit = { event ->
      when (event) {
        is Event.OpenSettings -> navigator.goTo(SettingsScreen)
      }
    }

    return State(
      sessionContent = contentPresenter.present(),
      eventSink = eventSink,
    )
  }

  @CircuitInject(HomeScreen::class, AppScope::class)
  @AssistedFactory
  fun interface Factory {
    fun create(navigator: Navigator): HomePresenter
  }
}
