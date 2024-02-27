package com.evanisnor.flowmeter.features.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.evanisnor.flowmeter.features.settings.SettingsOverlay.OverlayResult
import com.evanisnor.flowmeter.features.settings.SettingsOverlay.OverlayResult.Dismiss
import com.evanisnor.flowmeter.features.settings.SettingsOverlay.State.SoundPickerState
import com.slack.circuit.overlay.Overlay
import com.slack.circuit.overlay.OverlayNavigator

class SoundPickerOverlay(
  private val state: SoundPickerState,
) : Overlay<OverlayResult> {
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content(navigator: OverlayNavigator<OverlayResult>) {
    val modalBottomSheetState =
      rememberModalBottomSheetState(
        skipPartiallyExpanded = state.availableSounds.size > 10,
      )
    val lazyListState = rememberLazyListState()

    LaunchedEffect(state.currentSound) {
      lazyListState.animateScrollToItem(state.availableSounds.indexOf(state.currentSound))
    }

    ModalBottomSheet(
      sheetState = modalBottomSheetState,
      onDismissRequest = { navigator.finish(Dismiss) },
    ) {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = lazyListState,
      ) {
        items(state.availableSounds) { sound ->
          Row(
            modifier =
              Modifier
                .defaultMinSize(minHeight = 48.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { navigator.finish(OverlayResult.SelectSound(state.field, sound)) },
            verticalAlignment = Alignment.CenterVertically,
          ) {
            RadioButton(
              selected = state.currentSound == sound,
              onClick = null,
            )
            Text(
              modifier = Modifier.padding(start = 8.dp),
              text = sound.name,
              style = MaterialTheme.typography.bodyMedium,
            )
          }
        }
      }
    }
  }
}
