package com.evanisnor.flowmeter.features.flowtimesession.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.evanisnor.flowmeter.R
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.TakingABreak
import com.evanisnor.flowmeter.features.home.isLandscape
import com.evanisnor.flowmeter.ui.theme.FlowmeterTheme
import kotlin.time.Duration.Companion.minutes


@Composable
fun TakingABreakUi(state: TakingABreak, modifier: Modifier = Modifier) {
  BoxWithConstraints(
    modifier = modifier,
    contentAlignment = Alignment.Center
  ) {
    if (isLandscape()) {
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        BreakImage(modifier = Modifier.weight(1f))
        BreakSessionControls(state = state, modifier = Modifier.weight(1f))
      }
    } else {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        BreakImage(modifier = Modifier.weight(1f))
        BreakSessionControls(state = state, modifier = Modifier.weight(1f))
      }

    }
  }
}

@Composable
private fun BreakImage(modifier: Modifier = Modifier) {
  // Image: Woman Taking Coffee Break While Work From Home
  // Author zakazix https://pngtree.com/zakazix_12990890
  // Source: https://pngtree.com/freepng/women-taking-coffee-break-while-work-from-home-illustration-concept_5386884.html
  Image(
    modifier = modifier.padding(horizontal = 24.dp),
    painter = painterResource(id = R.drawable.woman_taking_coffee_break),
    contentDescription = null
  )

}

@Composable
private fun BreakSessionControls(state: TakingABreak, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "Taking a ${state.breakRecommendation.inWholeMinutes} minute break",
      style = MaterialTheme.typography.bodyLarge
    )
    Text(
      text = state.duration,
      style = MaterialTheme.typography.displayLarge,
      color = if (state.isBreakLongerThanRecommended) {
        MaterialTheme.colorScheme.error
      } else {
        Color.Unspecified
      }
    )
    StopButton(
      onClick = { state.eventSink(SessionContent.SessionEvent.EndBreak) },
    )
    NewSessionButton(
      onClick = { state.eventSink(SessionContent.SessionEvent.NewSession) },
    )
  }
}


@PreviewScreenSizes
@PreviewLightDark
@Composable
private fun TakingABreakPreview() {
  FlowmeterTheme {
    Surface {
      Scaffold { padding ->
        TakingABreakUi(
          modifier = Modifier
            .padding(padding)
            .fillMaxSize(),
          state = TakingABreak(
            duration = "1:10:13",
            breakRecommendation = 10.minutes,
            isBreakLongerThanRecommended = true,
            eventSink = {},
          )
        )
      }
    }
  }
}
