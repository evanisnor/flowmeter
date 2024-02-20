package com.evanisnor.flowmeter.features.settings

import androidx.compose.runtime.Composable
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.features.settings.SettingsScreen.State
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.presenter.Presenter
import kotlinx.collections.immutable.persistentListOf
import javax.inject.Inject

@CircuitInject(SettingsScreen::class, AppScope::class)
class SettingsPresenter @Inject constructor() : Presenter<State> {
  @Composable
  override fun present(): State {
    return State(
      settingsItems = persistentListOf(),
      eventSink = {},
    )
  }
}
