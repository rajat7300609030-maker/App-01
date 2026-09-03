package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SavedPage
import com.example.ui.theme.SleekOutlineVariant
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSurfaceContainer

private val PRESET_CATEGORIES = listOf("General", "Portal", "Admissions", "Academics", "Campus", "Contact")

@Composable
fun AddEditBookmarkDialog(
    initialBookmark: SavedPage?,
    currentUrl: String,
    currentTitle: String,
    onDismiss: () -> Unit,
    onSave: (title: String, url: String, category: String, notes: String, isPinned: Boolean) -> Unit,
    onUpdate: (SavedPage) -> Unit
) {
    val isEditMode = initialBookmark != null

    var title by remember {
        mutableStateOf(initialBookmark?.title ?: currentTitle.ifBlank { "Education Hills" })
    }
    var url by remember {
        mutableStateOf(initialBookmark?.url ?: currentUrl)
    }
    var category by remember {
        mutableStateOf(initialBookmark?.category ?: "General")
    }
    var notes by remember {
        mutableStateOf(initialBookmark?.notes ?: "")
    }
    var isPinned by remember {
        mutableStateOf(initialBookmark?.isPinned ?: false)
    }

    var titleError by remember { mutableStateOf(false) }
    var urlError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = if (isEditMode) "Edit Saved Item" else "Save to Resource Hub",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = { Text("Title") },
                    isError = titleError,
                    supportingText = if (titleError) {
                        { Text("Title cannot be empty") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_title_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SleekSurfaceContainer,
                        unfocusedContainerColor = SleekSurfaceContainer,
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekOutlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // URL Field
                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                        if (it.isNotBlank()) urlError = false
                    },
                    label = { Text("URL Address") },
                    isError = urlError,
                    supportingText = if (urlError) {
                        { Text("URL cannot be empty") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_url_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SleekSurfaceContainer,
                        unfocusedContainerColor = SleekSurfaceContainer,
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekOutlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category selection
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PRESET_CATEGORIES.forEach { cat ->
                        val isSelected = category == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .then(
                                    if (isSelected) {
                                        Modifier.background(SleekPrimaryContainer)
                                    } else {
                                        Modifier
                                            .background(Color.Transparent)
                                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(20.dp))
                                    }
                                )
                                .clickable { category = cat }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notes Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Personal Notes (Optional)") },
                    placeholder = { Text("Add notes or details...") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_notes_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SleekSurfaceContainer,
                        unfocusedContainerColor = SleekSurfaceContainer,
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekOutlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Pinned switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Pin to top",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Text(
                            text = "Keep at the top of your saved list",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isPinned,
                        onCheckedChange = { isPinned = it },
                        modifier = Modifier.testTag("dialog_pin_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SleekPrimary
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@Button
                    }
                    if (url.isBlank()) {
                        urlError = true
                        return@Button
                    }
                    val validUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        "https://$url"
                    } else {
                        url
                    }

                    if (isEditMode && initialBookmark != null) {
                        onUpdate(
                            initialBookmark.copy(
                                title = title.trim(),
                                url = validUrl.trim(),
                                category = category,
                                notes = notes.trim(),
                                isPinned = isPinned
                            )
                        )
                    } else {
                        onSave(title.trim(), validUrl.trim(), category, notes.trim(), isPinned)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SleekPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("dialog_save_button")
            ) {
                Text(if (isEditMode) "Save Changes" else "Save Bookmark")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SleekOutlineVariant)
            ) {
                Text("Cancel")
            }
        }
    )
}

