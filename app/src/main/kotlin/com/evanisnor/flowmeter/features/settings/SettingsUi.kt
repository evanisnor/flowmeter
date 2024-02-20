@file:OptIn(ExperimentalMaterial3Api::class)

package com.evanisnor.flowmeter.features.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.evanisnor.flowmeter.R
import com.evanisnor.flowmeter.di.AppScope
import com.evanisnor.flowmeter.features.settings.SettingsListViewData.DisplayValue
import com.evanisnor.flowmeter.features.settings.SettingsListViewData.GroupHeading
import com.evanisnor.flowmeter.features.settings.SettingsListViewData.MoreInformation
import com.evanisnor.flowmeter.features.settings.SettingsListViewData.Setting
import com.evanisnor.flowmeter.features.settings.SettingsListViewData.Divider
import com.evanisnor.flowmeter.features.settings.SettingsScreen.Event.NavigateBack
import com.evanisnor.flowmeter.features.settings.SettingsScreen.State
import com.evanisnor.flowmeter.ui.theme.FlowmeterTheme
import com.slack.circuit.codegen.annotations.CircuitInject
import kotlinx.collections.immutable.persistentListOf


@CircuitInject(SettingsScreen::class, AppScope::class)
@Composable
fun SettingsUi(state: State, modifier: Modifier = Modifier) {
  FlowmeterTheme {
    Scaffold(
      topBar = {
        TopBar(onNavigateBack = { state.eventSink(NavigateBack) })
      },
    ) { padding ->
      SettingsList(
        modifier = modifier
          .padding(padding)
          .consumeWindowInsets(padding)
          .fillMaxWidth(),
        state = state,
      )
    }
  }
}

@Composable
private fun TopBar(onNavigateBack: () -> Unit) {
  TopAppBar(
    navigationIcon = {
      IconButton(onClick = onNavigateBack) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = stringResource(R.string.screen_settings_back)
        )
      }
    },
    title = {  },
  )
}

@Composable
private fun SettingsList(state: State, modifier: Modifier = Modifier) {
  LazyColumn(modifier = modifier) {
    item {
      ColumnItem {
        Text(
          modifier = Modifier.padding(vertical = 16.dp),
          text = stringResource(R.string.screen_settings),
          style = MaterialTheme.typography.headlineLarge,
        )
      }
    }
    items(items = state.settingsItems) {
      when (it) {
        is GroupHeading -> ColumnItem { GroupHeadingItem(it) }
        is Setting -> ColumnItem { SettingItem(it) }
        is DisplayValue -> ColumnItem {  DisplayValueItem(it)}
        is MoreInformation -> ColumnItem {  MoreInformationItem(it) }
        is Divider -> HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
      }
    }
  }
}

/**
 * For consistent styling of items in the LazyColumn
 */
@Composable
private fun ColumnItem(content : @Composable () -> Unit) {
  Box(modifier = Modifier
    .defaultMinSize(minHeight = 52.dp)
    .padding(horizontal = 16.dp)
    .fillMaxWidth(),
    contentAlignment = Alignment.CenterStart,
    ) {
    content()
  }
}

@Composable
private fun GroupHeadingItem(groupHeading: GroupHeading, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = groupHeading.label,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.primary,
    )
  }
}

@Composable
private fun SettingItem(setting: Setting, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.fillMaxWidth(),
  ) {
    Text(
      text = setting.label,
      style = MaterialTheme.typography.labelLarge,
    )
    Text(
      text = setting.currentValue,
      style = MaterialTheme.typography.bodySmall,
    )
  }
}

@Composable
private fun DisplayValueItem(displayValue: DisplayValue, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.fillMaxWidth(),
  ) {
    Text(
      text = displayValue.label,
      style = MaterialTheme.typography.labelLarge,
    )
    Text(
      text = displayValue.value,
      style = MaterialTheme.typography.bodySmall,
    )
  }
}

@Composable
private fun MoreInformationItem(moreInformation: MoreInformation, modifier: Modifier = Modifier) {
  Text(
    modifier = modifier.fillMaxWidth(),
    text = moreInformation.label,
    style = MaterialTheme.typography.labelLarge,
  )
}

@PreviewLightDark
@Composable
private fun SettingsUiPreview() {
  SettingsUi(state = State(
    settingsItems = persistentListOf(
      GroupHeading(icon = Icons.Filled.Notifications, label = "Notifications"),
      Setting(
        label = "Sound One",
        currentValue = "Ringtone 1"
      ),
      Setting(
        label = "Sound Two",
        currentValue = "Ringtone 2"
      ),
      Divider,
      GroupHeading(icon = Icons.Filled.Info, label = "Information"),
      DisplayValue(
        label = "App version",
        value = "0.1.0",
      ),
      MoreInformation("Privacy Policy"),
      MoreInformation("Open Source Attribution"),
    ),
    eventSink = {}
  ))
}
