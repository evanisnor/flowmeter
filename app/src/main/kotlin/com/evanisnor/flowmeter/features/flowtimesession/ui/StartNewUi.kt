package com.evanisnor.flowmeter.features.flowtimesession.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.StartNew
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionEvent.NewSession

@Composable
fun StartNewUi(state: StartNew, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center
  ) {
    NewSessionButton(
      onClick = { state.eventSink(NewSession) },
    )
  }
}

@PreviewLightDark
@Composable
private fun StartNewPreview() {
  StartNewUi(state = StartNew(eventSink = {}))
}
