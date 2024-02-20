package com.evanisnor.flowmeter.features.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import com.evanisnor.flowmeter.R
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.features.settings.SettingsScreen.State
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.collections.immutable.persistentListOf

class SettingsPresenter @AssistedInject constructor(
  @Assisted private val navigator: Navigator,
) : Presenter<State> {
  @Composable
  override fun present(): State {
    val sessionStartSound = rememberRetained { mutableStateOf("Ringtone 1") }
    val breakIsOverSound = rememberRetained { mutableStateOf("Ringtone 2") }
    val appVersion = "0.1.0"

    val eventSink: (SettingsScreen.Event) -> Unit = { event ->
      when (event) {
        is SettingsScreen.Event.NavigateBack -> navigator.pop()
        is SettingsScreen.Event.FieldSelected -> {}
      }
    }

    return State(
      settingsItems = persistentListOf(
        SettingsListViewData.GroupHeading(
          icon = Icons.Filled.Notifications,
          label = stringResource(R.string.settings_group_notifications)
        ),
        SettingsListViewData.Setting(
          field = SettingsScreen.Field.SessionStartSound,
          label = stringResource(R.string.settings_notification_session_start),
          currentValue = sessionStartSound.value
        ),
        SettingsListViewData.Setting(
          field = SettingsScreen.Field.BreakIsOverSound,
          label = stringResource(R.string.settings_notification_break_is_over),
          currentValue = breakIsOverSound.value
        ),
        SettingsListViewData.Divider,
        SettingsListViewData.GroupHeading(
          icon = Icons.Filled.Info,
          label = stringResource(R.string.settings_group_information)
        ),
        SettingsListViewData.DisplayValue(
          label = stringResource(R.string.settings_information_app_version),
          value = appVersion,
        ),
        SettingsListViewData.MoreInformation(
          field = SettingsScreen.Field.PrivacyPolicy,
          label = stringResource(R.string.settings_information_privacy_policy)
        ),
        SettingsListViewData.MoreInformation(
          field = SettingsScreen.Field.OpenSourceAttribution,
          label = stringResource(R.string.settings_information_open_source_attribution)
        ),
      ),
      eventSink = eventSink,
    )
  }


  @CircuitInject(SettingsScreen::class, AppScope::class)
  @AssistedFactory
  fun interface Factory {
    fun create(navigator: Navigator): SettingsPresenter
  }
}
