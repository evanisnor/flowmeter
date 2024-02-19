package com.evanisnor.flowmeter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.evanisnor.flowmeter.di.ActivityKey
import com.evanisnor.flowmeter.di.AnvilInjector
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.di.InjectWith
import com.evanisnor.flowmeter.features.home.FlowTimeScreen
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.CircuitContent
import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.MembersInjector
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Inject

@InjectWith(AppScope::class)
class MainActivity : ComponentActivity() {

  @Inject
  lateinit var circuit: Circuit

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (application as FlowmeterApp).inject(this)
    enableEdgeToEdge()
    setContent {
      CircuitCompositionLocals(circuit = circuit) {
        CircuitContent(FlowTimeScreen)
      }
    }
  }

}

class MainActivityAnvilInjector @Inject constructor(
  override val injector: MembersInjector<MainActivity>,
) : AnvilInjector<MainActivity>

@Module
@ContributesTo(AppScope::class)
interface MainActivityAnvilInjectorBinder {
  @IntoMap
  @Binds
  @ActivityKey(MainActivity::class)
  fun bind(mainActivityAnvilInjector: MainActivityAnvilInjector) : AnvilInjector<*>
}
