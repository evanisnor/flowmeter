@file:OptIn(ExperimentalMaterial3Api::class)

package com.evanisnor.flowmeter.features.home

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.DateRange
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.features.flowtimesession.ui.SessionCompleteUi
import com.evanisnor.flowmeter.features.flowtimesession.ui.SessionContent.SessionComplete
import com.evanisnor.flowmeter.features.flowtimesession.ui.SessionContent.SessionInProgress
import com.evanisnor.flowmeter.features.flowtimesession.ui.SessionContent.StartNew
import com.evanisnor.flowmeter.features.flowtimesession.ui.SessionInProgressUi
import com.evanisnor.flowmeter.features.flowtimesession.ui.StartNewUi
import com.evanisnor.flowmeter.features.home.HomeScreen.State
import com.evanisnor.flowmeter.ui.theme.FlowmeterTheme
import com.slack.circuit.codegen.annotations.CircuitInject

@CircuitInject(HomeScreen::class, AppScope::class)
@Composable
fun HomeUi(state: State, modifier: Modifier = Modifier) {
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
      when (state.sessionContent) {
        is StartNew -> StartNewUi(state = state.sessionContent, modifier = screenModifier)
        is SessionComplete -> SessionCompleteUi(state = state.sessionContent, modifier = screenModifier)
        is SessionInProgress -> SessionInProgressUi(state = state.sessionContent, modifier = screenModifier)
      }
    }
  }
}
