package com.evanisnor.flowmeter.features.flowtimesession.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionEvent.NewSession
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.StartNew
import com.evanisnor.flowmeter.ui.theme.FlowmeterTheme

@Composable
fun StartNewUi(
  state: StartNew,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center,
  ) {
    NewSessionButton(
      onClick = { state.eventSink(NewSession) },
    )
  }
}

@PreviewScreenSizes
@PreviewLightDark
@Composable
private fun StartNewPreview() {
  FlowmeterTheme {
    Surface {
      Scaffold { padding ->
        StartNewUi(
          modifier =
            Modifier
              .padding(padding)
              .fillMaxSize(),
          state = StartNew(eventSink = {}),
        )
      }
    }
  }
}
