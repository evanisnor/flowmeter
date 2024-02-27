package com.evanisnor.flowmeter.features.settings.ui

import androidx.compose.runtime.Composable
import com.evanisnor.flowmeter.features.settings.SettingsOverlay.OverlayResult
import com.evanisnor.flowmeter.features.settings.SettingsOverlay.State
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.overlay.OverlayEffect

@Composable
fun SettingsOverlayUi(
  state: State?,
  onResult: (OverlayResult) -> Unit,
) {
  check(state != null) { return }

  ContentWithOverlays {
    OverlayEffect { host ->
      val overlay =
        when (state) {
          is State.InformationState -> InformationOverlay(state)
          is State.SoundPickerState -> SoundPickerOverlay(state)
        }
      val result = host.show(overlay)
      onResult(result)
    }
  }
}
