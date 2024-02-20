@file:OptIn(ExperimentalMaterial3Api::class)

package com.evanisnor.flowmeter.features.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
    title = { Text(stringResource(R.string.screen_settings).lowercase()) },
  )
}

@Composable
private fun SettingsList(state: State, modifier: Modifier = Modifier) {
  LazyColumn(modifier = modifier) {
    items(items = state.settingsItems) {
      Box(
        modifier = Modifier.defaultMinSize(minHeight = 48.dp),
        contentAlignment = Alignment.CenterStart
      ) {
        val itemModifier = Modifier.fillMaxWidth()
        when (it) {
          is GroupHeading -> GroupHeadingItem(it, modifier = itemModifier)
          is Setting -> SettingItem(it, modifier = itemModifier)
          is DisplayValue -> DisplayValueItem(it, modifier = itemModifier)
          is MoreInformation -> MoreInformationItem(it, modifier = itemModifier)
        }
      }
    }
  }
}

@Composable
private fun GroupHeadingItem(groupHeading: GroupHeading, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = groupHeading.icon,
      contentDescription = null,
    )
    Text(
      modifier = modifier.padding(horizontal = 16.dp),
      text = groupHeading.label,
      style = MaterialTheme.typography.titleMedium,
    )
  }
}

@Composable
private fun SettingItem(setting: Setting, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
  ) {
    Text(
      text = setting.label,
      style = MaterialTheme.typography.labelLarge,
    )
    Text(
      text = setting.description,
      style = MaterialTheme.typography.bodySmall,
    )
  }
}

@Composable
private fun DisplayValueItem(displayValue: DisplayValue, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
    modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
        description = "Change a sound made by the app",
        currentValue = "Ringtone 1"
      ),
      Setting(
        label = "Sound Two",
        description = "Change a sound made by the app",
        currentValue = "Ringtone 2"
      ),
      GroupHeading(icon = Icons.Filled.Info, label = "Information"),
      DisplayValue(
        label = "App version",
        value = "0.1.0",
      ),
      MoreInformation("Open Source Attribution")
    ),
    eventSink = {}
  ))
}
