package com.evanisnor.flowmeter

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.evanisnor.flowmeter.circuit.CircuitActivity
import com.evanisnor.flowmeter.ui.theme.FlowmeterTheme

class MainActivity : CircuitActivity() {

  @Composable
  override fun Content() {
    FlowmeterTheme {
      Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Greeting(
          name = "Android",
          modifier = Modifier.padding(innerPadding)
        )
      }
    }
  }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(
    text = "Hello $name!",
    modifier = modifier
  )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  FlowmeterTheme {
    Greeting("Android")
  }
}
