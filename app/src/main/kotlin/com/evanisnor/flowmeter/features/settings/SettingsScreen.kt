package com.evanisnor.flowmeter.features.settings

import androidx.compose.ui.graphics.vector.ImageVector
import com.evanisnor.flowmeter.features.settings.SettingsScreen.Field
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.parcelize.Parcelize


@Parcelize
data object SettingsScreen : Screen {

  data class State(
    val settingsItems: ImmutableList<SettingsListViewData>,
    val overlay: SettingsOverlay? = null,
    val eventSink: (Event) -> Unit,
  ) : CircuitUiState

  enum class Field {
    SessionStartSound,
    BreakIsOverSound,
    PrivacyPolicy,
    OpenSourceAttribution,
  }

  sealed interface Event : CircuitUiEvent {
    data object NavigateBack : Event
    data class FieldSelected(val field: Field) : Event
  }

}

sealed interface SettingsListViewData {

  data class GroupHeading(
    val icon: ImageVector,
    val label: String,
  ) : SettingsListViewData

  data class Setting(
    val field: Field,
    val label: String,
    val currentValue: String,
  ) : SettingsListViewData

  data class DisplayValue(val label: String, val value: String) : SettingsListViewData
  data class MoreInformation(
    val field: Field,
    val label: String,
  ) : SettingsListViewData

  data object Divider : SettingsListViewData
}

sealed interface SettingsOverlay
