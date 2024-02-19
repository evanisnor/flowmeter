package com.evanisnor.flowmeter.di

import dagger.MembersInjector

interface AnvilInjector<T> {
  val injector: MembersInjector<T>

  fun inject(target: T) {
    injector.injectMembers(target)
  }
}
