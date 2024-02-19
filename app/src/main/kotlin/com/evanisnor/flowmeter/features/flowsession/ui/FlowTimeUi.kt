@file:OptIn(ExperimentalMaterial3Api::class)

package com.evanisnor.flowmeter.features.flowsession.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.twotone.DateRange
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen.State.Content.SessionEvent.EndSession
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen.State.Content.SessionEvent.NewSession
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen.State
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen.State.Content.SessionComplete
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen.State.Content.SessionInProgress
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen.State.Content.StartNew
import com.evanisnor.flowmeter.ui.theme.FlowmeterTheme
import com.slack.circuit.codegen.annotations.CircuitInject
import kotlin.time.Duration.Companion.minutes

@CircuitInject(FlowTimeScreen::class, AppScope::class)
@Composable
fun FlowTimeUi(state: State, modifier: Modifier = Modifier) {
  FlowmeterTheme {
    Scaffold(
      topBar = {
        TopAppBar(title = {
          Row {
            Icon(
              modifier = Modifier.padding(horizontal = 4.dp),
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
        })
      }
    ) { innerPadding ->
      val screenModifier = modifier
        .padding(innerPadding)
        .fillMaxSize()
      when (state.content) {
        is StartNew -> StartNewUi(state = state.content, modifier = screenModifier)
        is SessionComplete -> SessionCompleteUi(state = state.content, modifier = screenModifier)
        is SessionInProgress -> SessionInProgressUi(state = state.content, modifier = screenModifier)
      }
    }
  }
}

// region Start New
@Composable
private fun StartNewUi(state: StartNew, modifier: Modifier = Modifier) {
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

// endregion

// region In Progress

@Composable
private fun SessionInProgressUi(state: SessionInProgress, modifier: Modifier = Modifier) {
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

// endregion

// region Complete

@Composable
private fun SessionCompleteUi(state: SessionComplete, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        modifier = Modifier.width(300.dp),
        textAlign = TextAlign.Center,
        text = "You were in the zone for",
        style = MaterialTheme.typography.bodyMedium
      )
      Text(
        modifier = Modifier.width(300.dp),
        textAlign = TextAlign.Center,
        text = buildAnnotatedString {
          withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(state.duration)
          }
        },
        style = MaterialTheme.typography.headlineMedium
      )
      Text(
        modifier = Modifier.width(300.dp),
        textAlign = TextAlign.Center,
        text = buildAnnotatedString {
          append("Take a ")
          withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(state.breakRecommendation.inWholeMinutes.toString())
          }
          append(" minute break")
        },
        style = MaterialTheme.typography.bodyMedium
      )
      NewSessionButton(
        onClick = { state.eventSink(NewSession) },
      )
    }
  }
}

@PreviewLightDark
@Composable
private fun SessionCompletePreview() {
  SessionCompleteUi(
    state = SessionComplete(
      duration = "1 hour and 24 minutes",
      breakRecommendation = 10.minutes,
      eventSink = {},
    )
  )
}

// endregion

@Composable
private fun NewSessionButton(onClick: () -> Unit) {
  Button(
    text = "Start a new session",
    icon = Icons.Filled.PlayArrow,
    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
    textColor = MaterialTheme.colorScheme.onPrimaryContainer,
    onClick = onClick,
  )
}

@Composable
private fun StopSessionButton(onClick: () -> Unit) {
  Button(
    text = "Stop",
    icon = Icons.Filled.Close,
    backgroundColor = MaterialTheme.colorScheme.errorContainer,
    textColor = MaterialTheme.colorScheme.onErrorContainer,
    onClick = onClick,
  )
}

@Composable
private fun Button(
  text: String,
  icon: ImageVector,
  backgroundColor: Color,
  textColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  TextButton(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(backgroundColor),
    onClick = onClick,
  ) {
    Icon(
      imageVector = icon,
      tint = textColor,
      contentDescription = null,
      modifier = Modifier.padding(end = 8.dp)
    )
    Text(
      text = text,
      color = textColor,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(end = 8.dp),
    )
  }
}
