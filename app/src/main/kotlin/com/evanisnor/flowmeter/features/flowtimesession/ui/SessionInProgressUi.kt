package com.evanisnor.flowmeter.features.flowtimesession.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionInProgress
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionEvent.EndSession


@Composable
fun SessionInProgressUi(state: SessionInProgress, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = state.duration,
        style = MaterialTheme.typography.displayLarge
      )
      StopSessionButton(
        onClick = { state.eventSink(EndSession) },
      )
    }
  }
}

@PreviewLightDark
@Composable
private fun SessionInProgressPreview() {
  SessionInProgressUi(
    state = SessionInProgress(
      duration = "1:10:13",
      eventSink = {},
    )
  )
}
