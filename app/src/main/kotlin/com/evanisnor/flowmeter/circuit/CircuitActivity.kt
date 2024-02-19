package com.evanisnor.flowmeter.circuit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.evanisnor.flowmeter.features.flowsession.domain.FlowTimePresenter
import com.evanisnor.flowmeter.features.flowsession.domain.FlowTimePresenterFactory
import com.evanisnor.flowmeter.features.flowsession.ui.FlowTimeUiFactory
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals


abstract class CircuitActivity : ComponentActivity() {

  private val circuit: Circuit = Circuit.Builder()
    .addPresenterFactory(FlowTimePresenterFactory { FlowTimePresenter() })
    .addUiFactory(FlowTimeUiFactory())
    .build()

  @Composable
  abstract fun Content()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      CircuitCompositionLocals(circuit = circuit) {
        Content()
      }
    }
  }
}
