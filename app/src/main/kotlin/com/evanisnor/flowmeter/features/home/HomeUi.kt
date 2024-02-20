package com.evanisnor.flowmeter.features.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.DateRange
import androidx.compose.material.icons.twotone.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.evanisnor.flowmeter.FeatureFlags
import com.evanisnor.flowmeter.R
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionComplete
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.SessionInProgress
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.StartNew
import com.evanisnor.flowmeter.features.flowtimesession.SessionContent.TakingABreak
import com.evanisnor.flowmeter.features.flowtimesession.ui.SessionCompleteUi
import com.evanisnor.flowmeter.features.flowtimesession.ui.SessionInProgressUi
import com.evanisnor.flowmeter.features.flowtimesession.ui.StartNewUi
import com.evanisnor.flowmeter.features.flowtimesession.ui.TakingABreakUi
import com.evanisnor.flowmeter.features.home.HomeScreen.State
import com.evanisnor.flowmeter.features.home.HomeScreen.Event.OpenSettings
import com.evanisnor.flowmeter.ui.theme.FlowmeterTheme
import com.slack.circuit.codegen.annotations.CircuitInject

@CircuitInject(HomeScreen::class, AppScope::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeUi(state: State, modifier: Modifier = Modifier) {
  FlowmeterTheme {
    Scaffold(
      topBar = {
        TopAppBar(
          title = { AppTitle() },
          actions = {
            if (FeatureFlags.SETTINGS) {
              IconButton(onClick = { state.eventSink(OpenSettings) }) {
                Icon(
                  imageVector = Icons.TwoTone.Settings,
                  contentDescription = stringResource(R.string.screen_settings)
                )
              }
            }
          }
        )
      },
    ) { innerPadding ->
      val screenModifier = modifier
        .padding(innerPadding)
        .fillMaxSize()

      AnimatedContent(
        targetState = state,
        transitionSpec = {
          val duration =
            if (initialState.sessionContent::class == targetState.sessionContent::class) {
              // Do not animate if the screen is recomposing session content
              // It will appear to flash with every clock tick
              0
            } else {
              1000
            }
          fadeIn(
            animationSpec = tween(duration)
          ) togetherWith fadeOut(
            animationSpec = tween(duration)
          )
        },
        label = "Home UI"
      ) { targetState ->
        when (targetState.sessionContent) {
          is StartNew -> StartNewUi(state = targetState.sessionContent, modifier = screenModifier)
          is SessionComplete -> SessionCompleteUi(
            state = targetState.sessionContent,
            modifier = screenModifier
          )
          is SessionInProgress -> SessionInProgressUi(
            state = targetState.sessionContent,
            modifier = screenModifier
          )
          is TakingABreak -> TakingABreakUi(
            state = targetState.sessionContent,
            modifier = screenModifier
          )
        }
      }
    }
  }
}

@Composable
private fun AppTitle(modifier: Modifier = Modifier) {
  Row {
    Icon(
      modifier = modifier.padding(horizontal = 4.dp),
      imageVector = Icons.TwoTone.DateRange,
      contentDescription = null
    )
    Text(buildAnnotatedString {
      append("flow")
      withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
        append("meter")
      }
    })
  }
}

@PreviewLightDark
@Composable
private fun HomeUiPreview() {
  FlowmeterTheme {
    Surface {
      HomeUi(state = State(
        sessionContent = StartNew(eventSink = {}),
        eventSink = {}
      ))
    }
  }
}
