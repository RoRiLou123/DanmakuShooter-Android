package io.github.acedroidx.danmaku.ui.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.acedroidx.danmaku.data.home.DanmakuConfig
import io.github.acedroidx.danmaku.model.DanmakuShootMode
import io.github.acedroidx.danmaku.ui.theme.AppTheme

object EditDanmakuProfile {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Profile(profile: DanmakuConfig, onChange: ((DanmakuConfig) -> Unit)) {
        var expanded by remember { mutableStateOf(false) }
        AppTheme {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        label = { Text(text = "房间号") },
                        value = profile.roomid.toString(),
                        onValueChange = {
                            it.toIntOrNull()
                                ?.let { it1 -> profile.copy(roomid = it1) }
                                ?.let { it2 -> onChange(it2) }
                        }
                    )
                }
                OutlinedTextField(
                    label = { Text(text = "弹幕内容") },
                    value = profile.msg,
                    onValueChange = { onChange(profile.copy(msg = it)) })
                OutlinedTextField(
                    label = { Text(text = "发送间隔") },
                    value = profile.interval.toString(),
                    onValueChange = {
                        it.toIntOrNull()
                            ?.let { it1 -> profile.copy(interval = it1) }
                            ?.let { it2 -> onChange(it2) }
                    })
                val shootModes = DanmakuShootMode.values()
                // We want to react on tap/press on TextField to show menu
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                ) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor(),
                        readOnly = true,
                        value = profile.shootMode.desc,
                        onValueChange = {},
                        label = { Text("发送模式") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        // colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        shootModes.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption.desc) },
                                onClick = {
                                    onChange(profile.copy(shootMode = selectionOption))
                                    expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
            }
        }
    }
}