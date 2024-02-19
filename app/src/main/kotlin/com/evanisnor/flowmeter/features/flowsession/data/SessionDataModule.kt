package com.evanisnor.flowmeter.features.flowsession.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.Database
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
@ContributesTo(AppScope::class)
class SessionDataModule {

  @Provides
  @Singleton
  fun sqlDriver(context: Context): SqlDriver = AndroidSqliteDriver(
    Database.Schema, context, "sessions.db"
  )

  @Provides
  fun database(sqlDriver: SqlDriver): Database = Database(sqlDriver)

}
