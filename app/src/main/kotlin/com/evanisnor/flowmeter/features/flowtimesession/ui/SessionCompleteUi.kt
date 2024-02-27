package com.evanisnor.flowmeter.features.flowtimesession.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionComplete
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionEvent.NewSession
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionEvent.TakeABreak
import com.evanisnor.flowmeter.ui.theme.FlowmeterTheme
import kotlin.time.Duration.Companion.minutes

@Composable
fun SessionCompleteUi(
  state: SessionComplete,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center,
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        modifier = Modifier.width(300.dp),
        textAlign = TextAlign.Center,
        text = "You were in the zone for",
        style = MaterialTheme.typography.bodyMedium,
      )
      Text(
        modifier = Modifier.width(300.dp),
        textAlign = TextAlign.Center,
        text =
          buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
              append(state.duration)
            }
          },
        style = MaterialTheme.typography.headlineMedium,
      )

      TakeABreakButton(
        duration = state.breakRecommendation,
        onClick = { state.eventSink(TakeABreak(state.breakRecommendation)) },
      )
      NewSessionButton(
        onClick = { state.eventSink(NewSession) },
      )
    }
  }
}

@PreviewScreenSizes
@PreviewLightDark
@Composable
private fun SessionCompletePreview() {
  FlowmeterTheme {
    Surface {
      Scaffold { padding ->
        SessionCompleteUi(
          modifier =
            Modifier
              .padding(padding)
              .fillMaxSize(),
          state =
            SessionComplete(
              duration = "1 hour and 24 minutes",
              breakRecommendation = 10.minutes,
              eventSink = {},
            ),
        )
      }
    }
  }
}
