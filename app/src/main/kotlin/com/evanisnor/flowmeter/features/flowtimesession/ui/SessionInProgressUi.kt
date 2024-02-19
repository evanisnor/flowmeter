package com.evanisnor.flowmeter.features.flowtimesession.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionEvent.EndSession
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionInProgress
import com.evanisnor.flowmeter.ui.theme.FlowmeterTheme


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
      StopButton(
        onClick = { state.eventSink(EndSession) },
      )
    }
  }
}

@PreviewScreenSizes
@PreviewLightDark
@Composable
private fun SessionInProgressPreview() {
  FlowmeterTheme {
    Surface {
      Scaffold { padding ->
        SessionInProgressUi(
          modifier = Modifier
            .padding(padding)
            .fillMaxSize(),
          state = SessionInProgress(
            duration = "1:10:13",
            eventSink = {},
          )
        )
      }
    }
  }
}
