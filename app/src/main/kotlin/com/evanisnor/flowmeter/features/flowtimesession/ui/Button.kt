package com.evanisnor.flowmeter.features.flowtimesession.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlin.time.Duration


@Composable
fun NewSessionButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
  Button(
    modifier = modifier,
    text = AnnotatedString("Start a new session"),
    icon = Icons.Filled.PlayArrow,
    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
    textColor = MaterialTheme.colorScheme.onPrimaryContainer,
    onClick = onClick,
  )
}

@Composable
fun StopButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
  Button(
    modifier = modifier,
    text = AnnotatedString("Stop"),
    icon = Icons.Filled.Close,
    backgroundColor = MaterialTheme.colorScheme.errorContainer,
    textColor = MaterialTheme.colorScheme.onErrorContainer,
    onClick = onClick,
  )
}

@Composable
fun TakeABreakButton(duration: Duration, onClick: () -> Unit, modifier: Modifier = Modifier) {
  Button(
    modifier = modifier,
    text = buildAnnotatedString {
      append("Take a ")
      withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
        append(duration.inWholeMinutes.toString())
      }
      append(" minute break")
    },
    icon = Icons.Filled.Favorite,
    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
    textColor = MaterialTheme.colorScheme.onSecondaryContainer,
    onClick = onClick,
  )
}

@Composable
private fun Button(
  text: AnnotatedString,
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
