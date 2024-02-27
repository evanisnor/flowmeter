package com.evanisnor.flowmeter.ui.circuit

import com.evanisnor.flowmeter.di.AppScope
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides

@Module
@ContributesTo(AppScope::class)
class CircuitModule {
  @JvmSuppressWildcards
  @Provides
  fun circuit(
    presenterFactories: Set<Presenter.Factory>,
    uiFactories: Set<Ui.Factory>,
  ): Circuit =
    Circuit.Builder()
      .addPresenterFactories(presenterFactories)
      .addUiFactories(uiFactories)
      .build()
}
