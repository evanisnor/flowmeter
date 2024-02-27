@file:OptIn(ExperimentalMaterial3Api::class)

package com.evanisnor.flowmeter.features.settings

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Switch
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
import com.evanisnor.flowmeter.features.settings.SettingsListViewData.Divider
import com.evanisnor.flowmeter.features.settings.SettingsListViewData.GroupHeading
import com.evanisnor.flowmeter.features.settings.SettingsListViewData.MoreInformation
import com.evanisnor.flowmeter.features.settings.SettingsListViewData.Setting
import com.evanisnor.flowmeter.features.settings.SettingsListViewData.Toggle
import com.evanisnor.flowmeter.features.settings.SettingsScreen.Event.NavigateBack
import com.evanisnor.flowmeter.features.settings.SettingsScreen.State
import com.evanisnor.flowmeter.features.settings.ui.SettingsOverlayUi
import com.evanisnor.flowmeter.ui.theme.FlowmeterTheme
import com.slack.circuit.codegen.annotations.CircuitInject
import kotlinx.collections.immutable.persistentListOf

@CircuitInject(SettingsScreen::class, AppScope::class)
@Composable
fun SettingsUi(
  state: State,
  modifier: Modifier = Modifier,
) {
  FlowmeterTheme {
    Scaffold(
      topBar = {
        TopBar(onNavigateBack = { state.eventSink(NavigateBack) })
      },
    ) { padding ->
      SettingsList(
        modifier =
          modifier
            .padding(padding)
            .consumeWindowInsets(padding)
            .fillMaxWidth(),
        state = state,
        onFieldSelected = { field ->
          state.eventSink(SettingsScreen.Event.FieldSelected(field))
        },
      )

      SettingsOverlayUi(
        state = state.overlayState,
        onResult = { state.eventSink(SettingsScreen.Event.OverlayResult(it)) },
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
          contentDescription = stringResource(R.string.screen_settings_back),
        )
      }
    },
    title = { },
  )
}

@Composable
private fun SettingsList(
  state: State,
  onFieldSelected: (FieldId) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(modifier = modifier) {
    item {
      ColumnItem {
        Text(
          modifier = Modifier.padding(top = 32.dp, bottom = 16.dp),
          text = stringResource(R.string.screen_settings),
          style = MaterialTheme.typography.headlineLarge,
        )
      }
    }
    items(items = state.settingsItems) {
      when (it) {
        is Divider -> HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        is GroupHeading -> ColumnItem { GroupHeadingItem(it) }
        is DisplayValue -> ColumnItem { DisplayValueItem(it) }
        is Setting ->
          ColumnItem(
            modifier = Modifier.clickable { onFieldSelected(it.field) },
          ) {
            SettingItem(it)
          }
        is Toggle ->
          ColumnItem(
            modifier = Modifier.clickable { onFieldSelected(it.field) },
          ) {
            ToggleItem(it, onCheckedChange = { _ -> onFieldSelected(it.field) })
          }
        is MoreInformation ->
          ColumnItem(
            modifier = Modifier.clickable { onFieldSelected(it.field) },
          ) {
            MoreInformationItem(it)
          }
      }
    }
  }
}

/**
 * For consistent styling of items in the LazyColumn
 */
@Composable
private fun ColumnItem(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Box(
    modifier =
      modifier
        .defaultMinSize(minHeight = 52.dp)
        .padding(horizontal = 16.dp)
        .fillMaxWidth(),
    contentAlignment = Alignment.CenterStart,
  ) {
    content()
  }
}

@Composable
private fun GroupHeadingItem(
  groupHeading: GroupHeading,
  modifier: Modifier = Modifier,
) {
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
private fun SettingItem(
  setting: Setting,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth(),
  ) {
    Text(
      text = setting.label,
      style = MaterialTheme.typography.bodyMedium,
    )
    Text(
      text = setting.currentValue,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun ToggleItem(
  toggle: Toggle,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = toggle.label,
      style = MaterialTheme.typography.bodyMedium,
    )
    Switch(
      checked = toggle.currentValue,
      onCheckedChange = onCheckedChange,
    )
  }
}

@Composable
private fun DisplayValueItem(
  displayValue: DisplayValue,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth(),
  ) {
    Text(
      text = displayValue.label,
      style = MaterialTheme.typography.bodyMedium,
    )
    Text(
      text = displayValue.value,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun MoreInformationItem(
  moreInformation: MoreInformation,
  modifier: Modifier = Modifier,
) {
  Text(
    modifier = modifier.fillMaxWidth(),
    text = moreInformation.label,
    style = MaterialTheme.typography.bodyMedium,
  )
}

@PreviewLightDark
@Composable
private fun SettingsUiPreview() {
  SettingsUi(
    state =
      State(
        settingsItems =
          persistentListOf(
            GroupHeading(icon = Icons.Filled.Notifications, label = "Notifications"),
            Setting(
              field = "",
              label = "Sound one",
              currentValue = "Ringtone 1",
            ),
            Setting(
              field = "",
              label = "Sound two",
              currentValue = "Ringtone 2",
            ),
            Toggle(
              field = "",
              label = "Vibrate?",
              currentValue = true,
            ),
            Divider,
            GroupHeading(icon = Icons.Filled.Info, label = "Information"),
            DisplayValue(
              label = "App version",
              value = "0.1.0",
            ),
            MoreInformation(
              field = "",
              label = "Privacy policy",
            ),
            MoreInformation(
              field = "",
              label = "Open source attribution",
            ),
          ),
        eventSink = {},
      ),
  )
}
