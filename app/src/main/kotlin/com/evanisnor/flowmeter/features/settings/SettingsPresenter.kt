package com.evanisnor.flowmeter.features.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import com.evanisnor.flowmeter.BuildConfig
import com.evanisnor.flowmeter.R
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.features.settings.SettingsListViewData.DisplayValue
import com.evanisnor.flowmeter.features.settings.SettingsListViewData.Divider
import com.evanisnor.flowmeter.features.settings.SettingsListViewData.GroupHeading
import com.evanisnor.flowmeter.features.settings.SettingsListViewData.MoreInformation
import com.evanisnor.flowmeter.features.settings.SettingsListViewData.Setting
import com.evanisnor.flowmeter.features.settings.SettingsListViewData.Toggle
import com.evanisnor.flowmeter.features.settings.SettingsOverlay.OverlayResult.Dismiss
import com.evanisnor.flowmeter.features.settings.SettingsOverlay.OverlayResult.SelectSound
import com.evanisnor.flowmeter.features.settings.SettingsOverlay.State.InformationState
import com.evanisnor.flowmeter.features.settings.SettingsOverlay.State.SoundPickerState
import com.evanisnor.flowmeter.features.settings.SettingsScreen.Event
import com.evanisnor.flowmeter.features.settings.SettingsScreen.Event.FieldSelected
import com.evanisnor.flowmeter.features.settings.SettingsScreen.Event.NavigateBack
import com.evanisnor.flowmeter.features.settings.SettingsScreen.State
import com.evanisnor.flowmeter.features.settings.data.SettingsRepository
import com.evanisnor.flowmeter.system.MarkdownReader
import com.evanisnor.flowmeter.system.MediaPlayerSystem
import com.evanisnor.flowmeter.system.NotificationChannelSystem
import com.evanisnor.flowmeter.system.NotificationChannelSystem.NotificationChannel.BreakIsOverNotificationChannel
import com.evanisnor.flowmeter.system.NotificationChannelSystem.NotificationChannelSettings
import com.evanisnor.flowmeter.system.RingtoneSystem
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch

private const val SETTING_SESSION_START_SOUND = "session_start_sound"
private const val SETTING_BREAK_IS_OVER_SOUND = "break_is_over_sound"
private const val SETTING_BREAK_IS_OVER_VIBRATE = "break_is_over_vibrate"
private const val INFO_PRIVACY_POLICY = "privacy_policy"
private const val DEBUG_ENABLE_QUICK_BREAKS = "debug_enable_quick_breaks"

class SettingsPresenter
@AssistedInject
constructor(
  @Assisted private val navigator: Navigator,
  private val ringtoneSystem: RingtoneSystem,
  private val settingsRepository: SettingsRepository,
  private val mediaPlayerSystem: MediaPlayerSystem,
  private val notificationChannelSystem: NotificationChannelSystem,
  private val markdownReader: MarkdownReader,
) : Presenter<State> {
  @Composable
  override fun present(): State {
    val scope = rememberCoroutineScope()
    val overlay = rememberRetained { mutableStateOf<SettingsOverlay.State?>(null) }
    val availableSounds =
      rememberRetained { mutableStateOf(emptyList<RingtoneSystem.RingtoneSound>()) }

    val sessionStartSound = rememberRetained { mutableStateOf(ringtoneSystem.getDefaultSound()) }
    val breakIsOver =
      rememberRetained {
        mutableStateOf(
          NotificationChannelSettings(
            sound = ringtoneSystem.getDefaultSound(),
            vibrate = true,
          ),
        )
      }
    val debugEnableQuickBreaks = rememberRetained { mutableStateOf(false) }

    LaunchedEffect(Unit) {
      availableSounds.value = ringtoneSystem.getSounds()
      sessionStartSound.value = settingsRepository.getSessionStartSound()
      breakIsOver.value =
        NotificationChannelSettings(
          sound = settingsRepository.getBreakIsOverSound(),
          vibrate = settingsRepository.getBreakIsOverVibrate(),
        )
      debugEnableQuickBreaks.value = settingsRepository.getDebugQuickBreaks()
    }

    val overlayResultSink: (SettingsOverlay.OverlayResult) -> Unit = { result ->
      when (result) {
        is Dismiss -> overlay.value = null
        is SelectSound -> {
          mediaPlayerSystem.play(result.sound)
          when (result.field) {
            SETTING_SESSION_START_SOUND -> {
              sessionStartSound.value = result.sound
              scope.launch { settingsRepository.saveSessionStartSound(result.sound) }
            }
            SETTING_BREAK_IS_OVER_SOUND -> {
              breakIsOver.value = breakIsOver.value.copy(sound = result.sound)
              scope.launch {
                settingsRepository.saveBreakIsOverSound(result.sound)
                notificationChannelSystem.createNotificationChannel(
                  BreakIsOverNotificationChannel,
                  settings = breakIsOver.value,
                )
              }
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
              overlay.value =
                SoundPickerState(
                  field = event.field,
                  availableSounds = availableSounds.value,
                  currentSound = sessionStartSound.value,
                )
            SETTING_BREAK_IS_OVER_SOUND ->
              overlay.value =
                SoundPickerState(
                  field = event.field,
                  availableSounds = availableSounds.value,
                  currentSound = breakIsOver.value.sound,
                )
            SETTING_BREAK_IS_OVER_VIBRATE -> {
              val vibrate = !breakIsOver.value.vibrate
              breakIsOver.value = breakIsOver.value.copy(vibrate = vibrate)
              scope.launch {
                settingsRepository.saveBreakIsOverVibrate(vibrate)
                notificationChannelSystem.createNotificationChannel(
                  BreakIsOverNotificationChannel,
                  settings = breakIsOver.value,
                )
              }
            }
            INFO_PRIVACY_POLICY ->
              overlay.value =
                InformationState(event.field, markdownReader.read(R.raw.privacy))
            DEBUG_ENABLE_QUICK_BREAKS -> {
              debugEnableQuickBreaks.value = !debugEnableQuickBreaks.value
              scope.launch {
                settingsRepository.saveDebugQuickBreaks(debugEnableQuickBreaks.value)
              }
            }
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
      breakIsOverSound = breakIsOver.value.sound,
      breakIsOverVibrate = breakIsOver.value.vibrate,
      debugEnableQuickBreaks = debugEnableQuickBreaks.value,
      eventSink = eventSink,
    )
  }

  @Composable
  private fun buildState(
    overlay: SettingsOverlay.State?,
    sessionStartSound: RingtoneSystem.RingtoneSound,
    breakIsOverSound: RingtoneSystem.RingtoneSound,
    breakIsOverVibrate: Boolean,
    debugEnableQuickBreaks: Boolean,
    eventSink: (Event) -> Unit,
  ): State = State(
    settingsItems =
    mutableListOf(
      GroupHeading(
        icon = Icons.Filled.Notifications,
        label = stringResource(R.string.settings_group_notifications),
      ),
      Setting(
        field = SETTING_SESSION_START_SOUND,
        label = stringResource(R.string.settings_notification_session_start),
        currentValue = sessionStartSound.name,
      ),
      Setting(
        field = SETTING_BREAK_IS_OVER_SOUND,
        label = stringResource(R.string.settings_notification_break_is_over),
        currentValue = breakIsOverSound.name,
      ),
      Toggle(
        field = SETTING_BREAK_IS_OVER_VIBRATE,
        label = stringResource(R.string.settings_notification_break_is_over_vibrate),
        currentValue = breakIsOverVibrate,
      ),
      Divider,
      GroupHeading(
        icon = Icons.Filled.Info,
        label = stringResource(R.string.settings_group_information),
      ),
      DisplayValue(
        label = stringResource(R.string.settings_information_app_version),
        value = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
      ),
      MoreInformation(
        field = INFO_PRIVACY_POLICY,
        label = stringResource(R.string.settings_information_privacy_policy),
      ),
    ).apply {
      if (BuildConfig.DEBUG) {
        // DEBUG MENU
        listOf(
          Divider,
          GroupHeading(
            icon = Icons.Filled.Build,
            label = stringResource(R.string.settings_group_debug),
          ),
          Toggle(
            field = DEBUG_ENABLE_QUICK_BREAKS,
            label = stringResource(R.string.settings_debug_enable_quick_breaks),
            currentValue = debugEnableQuickBreaks,
          ),
        ).let { addAll(it) }
      }
    }.toPersistentList(),
    overlayState = overlay,
    eventSink = eventSink,
  )

  @CircuitInject(SettingsScreen::class, AppScope::class)
  @AssistedFactory
  fun interface Factory {
    fun create(navigator: Navigator): SettingsPresenter
  }
}
