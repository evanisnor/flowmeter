package com.evanisnor.flowmeter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.evanisnor.flowmeter.di.ActivityKey
import com.evanisnor.flowmeter.di.AnvilInjector
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.di.InjectWith
import com.evanisnor.flowmeter.features.home.HomeScreen
import com.evanisnor.flowmeter.system.NotificationSystem
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
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

  @Inject
  lateinit var notificationSystem: NotificationSystem

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (application as FlowmeterApp).inject(this)

    // Notification Post Permission
    if (!notificationSystem.isNotificationPermissionGranted()) {
      notificationSystem.registerForPermissionResult(this)
    }

    enableEdgeToEdge()
    setContent {
      CircuitCompositionLocals(circuit) {
        val backStack = rememberSaveableBackStack(HomeScreen)
        val navigator = rememberCircuitNavigator(backStack)
        NavigableCircuitContent(navigator, backStack)
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
