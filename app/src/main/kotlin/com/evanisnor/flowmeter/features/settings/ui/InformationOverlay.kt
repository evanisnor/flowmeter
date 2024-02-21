package com.evanisnor.flowmeter.features.settings.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.evanisnor.flowmeter.features.settings.SettingsOverlay
import com.evanisnor.flowmeter.features.settings.SettingsOverlay.OverlayResult
import com.evanisnor.flowmeter.features.settings.SettingsOverlay.OverlayResult.Dismiss
import com.slack.circuit.overlay.Overlay
import com.slack.circuit.overlay.OverlayNavigator

class InformationOverlay(
  private val state : SettingsOverlay.State.InformationState,
) : Overlay<OverlayResult> {

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content(navigator: OverlayNavigator<OverlayResult>) {
    ModalBottomSheet(onDismissRequest = { navigator.finish(Dismiss) }) {
      Text("$state")
    }
  }

}
