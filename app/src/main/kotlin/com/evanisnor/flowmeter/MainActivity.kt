package com.evanisnor.flowmeter

import androidx.compose.runtime.Composable
import com.evanisnor.flowmeter.circuit.CircuitActivity
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeScreen
import com.slack.circuit.foundation.CircuitContent

class MainActivity : CircuitActivity() {

  @Composable
  override fun Content() {
    CircuitContent(FlowTimeScreen)
  }

}
