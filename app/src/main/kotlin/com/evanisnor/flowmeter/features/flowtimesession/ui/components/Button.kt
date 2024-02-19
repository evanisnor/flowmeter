package com.evanisnor.flowmeter.features.flowtimesession.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


@Composable
fun NewSessionButton(onClick: () -> Unit) {
  Button(
    text = "Start a new session",
    icon = Icons.Filled.PlayArrow,
    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
    textColor = MaterialTheme.colorScheme.onPrimaryContainer,
    onClick = onClick,
  )
}

@Composable
fun StopSessionButton(onClick: () -> Unit) {
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
