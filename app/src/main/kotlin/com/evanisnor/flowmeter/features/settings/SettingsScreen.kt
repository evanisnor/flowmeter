package com.evanisnor.flowmeter.features.settings

import androidx.compose.ui.graphics.vector.ImageVector
import com.evanisnor.flowmeter.system.RingtoneSystem
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.parcelize.Parcelize

typealias FieldId = String

@Parcelize
data object SettingsScreen : Screen {

  data class State(
    val settingsItems: ImmutableList<SettingsListViewData>,
    val overlayState: SettingsOverlay.State? = null,
    val eventSink: (Event) -> Unit,
  ) : CircuitUiState

  sealed interface Event : CircuitUiEvent {
    data object NavigateBack : Event
    data class FieldSelected(val field: FieldId) : Event
    data class OverlayResult(val result: SettingsOverlay.OverlayResult) : Event
  }
}

object SettingsOverlay {
  sealed interface State {
    val field: FieldId

    data class SoundPickerState(
      override val field: FieldId,
      val availableSounds: List<RingtoneSystem.RingtoneSound>,
      val currentSound: RingtoneSystem.RingtoneSound
    ) : State
    data class InformationState(override val field: FieldId) : State
  }

  sealed interface OverlayResult {
    data class SelectSound(val field: FieldId, val sound: RingtoneSystem.RingtoneSound) : OverlayResult
    data object Dismiss : OverlayResult
  }
}

sealed interface SettingsListViewData {

  data class GroupHeading(
    val icon: ImageVector,
    val label: String,
  ) : SettingsListViewData

  data class Setting(
    val field: FieldId,
    val label: String,
    val currentValue: String,
  ) : SettingsListViewData

  data class Toggle(
    val field: FieldId,
    val label: String,
    val currentValue: Boolean,
  ) : SettingsListViewData

  data class DisplayValue(val label: String, val value: String) : SettingsListViewData
  data class MoreInformation(
    val field: FieldId,
    val label: String,
  ) : SettingsListViewData

  data object Divider : SettingsListViewData
}
