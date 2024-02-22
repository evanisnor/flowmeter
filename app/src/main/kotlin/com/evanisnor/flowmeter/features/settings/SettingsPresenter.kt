package com.evanisnor.flowmeter.features.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import com.evanisnor.flowmeter.R
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.features.settings.SettingsListViewData.DisplayValue
import com.evanisnor.flowmeter.features.settings.SettingsListViewData.Divider
import com.evanisnor.flowmeter.features.settings.SettingsListViewData.GroupHeading
import com.evanisnor.flowmeter.features.settings.SettingsListViewData.MoreInformation
import com.evanisnor.flowmeter.features.settings.SettingsListViewData.Setting
import com.evanisnor.flowmeter.features.settings.SettingsOverlay.OverlayResult.Dismiss
import com.evanisnor.flowmeter.features.settings.SettingsOverlay.OverlayResult.SelectSound
import com.evanisnor.flowmeter.features.settings.SettingsOverlay.State.InformationState
import com.evanisnor.flowmeter.features.settings.SettingsOverlay.State.SoundPickerState
import com.evanisnor.flowmeter.features.settings.SettingsScreen.Event
import com.evanisnor.flowmeter.features.settings.SettingsScreen.Event.FieldSelected
import com.evanisnor.flowmeter.features.settings.SettingsScreen.Event.NavigateBack
import com.evanisnor.flowmeter.features.settings.SettingsScreen.State
import com.evanisnor.flowmeter.features.settings.data.SettingsRepository
import com.evanisnor.flowmeter.system.RingtoneSystem
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

private const val SETTING_SESSION_START_SOUND = "session_start_sound"
private const val SETTING_BREAK_IS_OVER_SOUND = "break_is_over_sound"
private const val INFO_PRIVACY_POLICY = "privacy_policy"
private const val INFO_OPEN_SOURCE_ATTRIBUTION = "open_source_attribution"

class SettingsPresenter @AssistedInject constructor(
  @Assisted private val navigator: Navigator,
  private val ringtoneSystem: RingtoneSystem,
  private val settingsRepository: SettingsRepository,
) : Presenter<State> {
  @Composable
  override fun present(): State {
    val scope = rememberCoroutineScope()
    val overlay = rememberRetained { mutableStateOf<SettingsOverlay.State?>(null) }
    val availableSounds =
      rememberRetained { mutableStateOf(emptyList<RingtoneSystem.RingtoneSound>()) }

    val sessionStartSound = rememberRetained { mutableStateOf(ringtoneSystem.getDefaultSound()) }
    val breakIsOverSound = rememberRetained { mutableStateOf(ringtoneSystem.getDefaultSound()) }
    val appVersion = "0.1.0"

    LaunchedEffect(Unit) {
      availableSounds.value = ringtoneSystem.getSounds()
      sessionStartSound.value = settingsRepository.getSessionStartSound()
      breakIsOverSound.value = settingsRepository.getBreakIsOverSound()
    }


    val overlayResultSink: (SettingsOverlay.OverlayResult) -> Unit = { result ->
      when (result) {
        is Dismiss -> overlay.value = null
        is SelectSound -> {
          when(result.field) {
            SETTING_SESSION_START_SOUND -> {
              sessionStartSound.value = result.sound
              scope.launch { settingsRepository.saveSessionStartSound(result.sound) }
            }
            SETTING_BREAK_IS_OVER_SOUND -> {
              breakIsOverSound.value = result.sound
              scope.launch { settingsRepository.saveBreakIsOverSound(result.sound) }
            }
          }
          overlay.value = null
        }
      }
    }

    val eventSink: (Event) -> Unit = { event ->
      when (event) {
        is NavigateBack -> navigator.pop()
        is FieldSelected -> {
          when (event.field) {
            SETTING_SESSION_START_SOUND ->
              overlay.value = SoundPickerState(
                field = event.field,
                availableSounds = availableSounds.value,
                currentSound = sessionStartSound.value,
              )
            SETTING_BREAK_IS_OVER_SOUND ->
              overlay.value = SoundPickerState(
                field = event.field,
                availableSounds = availableSounds.value,
                currentSound = breakIsOverSound.value,
              )
            INFO_PRIVACY_POLICY -> overlay.value =
              InformationState(event.field)
            INFO_OPEN_SOURCE_ATTRIBUTION -> overlay.value =
              InformationState(event.field)
          }
        }
        is Event.OverlayResult -> {
          overlayResultSink(event.result)
        }
      }
    }


    return buildState(
      overlay = overlay.value,
      sessionStartSound = sessionStartSound.value,
      breakIsOverSound = breakIsOverSound.value,
      appVersion = appVersion,
      eventSink = eventSink,
    )
  }

  @Composable
  private fun buildState(
    overlay: SettingsOverlay.State?,
    sessionStartSound: RingtoneSystem.RingtoneSound,
    breakIsOverSound: RingtoneSystem.RingtoneSound,
    appVersion: String,
    eventSink: (Event) -> Unit,
  ): State = State(
    settingsItems = persistentListOf(
      GroupHeading(
        icon = Icons.Filled.Notifications,
        label = stringResource(R.string.settings_group_notifications)
      ),
      Setting(
        field = SETTING_SESSION_START_SOUND,
        label = stringResource(R.string.settings_notification_session_start),
        currentValue = sessionStartSound.name
      ),
      Setting(
        field = SETTING_BREAK_IS_OVER_SOUND,
        label = stringResource(R.string.settings_notification_break_is_over),
        currentValue = breakIsOverSound.name
      ),
      Divider,
      GroupHeading(
        icon = Icons.Filled.Info,
        label = stringResource(R.string.settings_group_information)
      ),
      DisplayValue(
        label = stringResource(R.string.settings_information_app_version),
        value = appVersion,
      ),
      MoreInformation(
        field = INFO_PRIVACY_POLICY,
        label = stringResource(R.string.settings_information_privacy_policy)
      ),
      MoreInformation(
        field = INFO_OPEN_SOURCE_ATTRIBUTION,
        label = stringResource(R.string.settings_information_open_source_attribution)
      ),
    ),
    overlayState = overlay,
    eventSink = eventSink,
  )

  @CircuitInject(SettingsScreen::class, AppScope::class)
  @AssistedFactory
  fun interface Factory {
    fun create(navigator: Navigator): SettingsPresenter
  }
}
