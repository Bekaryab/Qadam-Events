@file:OptIn(ExperimentalMaterial3Api::class)

package com.bekaryab.qadamevents.ui.events.create

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bekaryab.qadamevents.R
import com.bekaryab.qadamevents.ui.auth.BorderGray
import com.bekaryab.qadamevents.ui.auth.Muted
import com.bekaryab.qadamevents.ui.auth.Purple
import com.bekaryab.qadamevents.ui.auth.SubmitButton
import com.bekaryab.qadamevents.ui.events.components.rememberContentImageBitmap
import com.bekaryab.qadamevents.ui.theme.AlumniSansSc

private val ScreenBackground = Color(0xFFDDF3F5)
private val CreateNavy = Color(0xFF2E3768)
private val CoverPlaceholder = Color(0xFFE3E7EB)
private val FieldShape = RoundedCornerShape(13.dp)

/** Compact Create Event form. All rules live in [CreateEventViewModel]. */
@Composable
fun CreateEventScreen(
    viewModel: CreateEventViewModel,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val coverPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Session-only access is enough; persistence is best-effort.
            }
            viewModel.onCoverPicked(uri.toString())
        }
    }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            viewModel.consumeSuccess()
            onSaved()
        }
    }

    var categoryOpen by remember { mutableStateOf(false) }
    var dateDialogOpen by remember { mutableStateOf(false) }
    var startDialogOpen by remember { mutableStateOf(false) }
    var endDialogOpen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .imePadding()
    ) {
        Text(
            text = "CREATE EVENT",
            fontFamily = AlumniSansSc,
            fontWeight = FontWeight.Black,
            fontSize = 26.sp,
            color = CreateNavy,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CoverPicker(
                coverUri = state.coverUri,
                onPick = {
                    coverPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )

            FormTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = "Event title",
                placeholder = "KBTU Hackathon 2026",
                error = viewModel.titleError(),
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )

            FormTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                label = "Description",
                placeholder = "What is this event about?",
                error = viewModel.descriptionError(),
                singleLine = false,
                minLines = 4
            )

            ExposedDropdownMenuBox(
                expanded = categoryOpen,
                onExpandedChange = { categoryOpen = it }
            ) {
                OutlinedTextField(
                    value = state.category,
                    onValueChange = {},
                    readOnly = true,
                    label = { FieldLabel("Category") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryOpen)
                    },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    singleLine = true,
                    shape = FieldShape,
                    colors = formColors()
                )
                ExposedDropdownMenu(
                    expanded = categoryOpen,
                    onDismissRequest = { categoryOpen = false }
                ) {
                    EventCategories.forEach { category ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = category,
                                    fontFamily = AlumniSansSc,
                                    fontSize = 14.sp
                                )
                            },
                            onClick = {
                                viewModel.onCategoryChange(category)
                                categoryOpen = false
                            }
                        )
                    }
                }
            }

            PickerField(
                label = "Date",
                value = state.dateMillis?.let(CreateEventViewModel::formatDate),
                placeholder = "Select date",
                iconRes = R.drawable.ic_schedule,
                error = viewModel.dateError(),
                onClick = { dateDialogOpen = true }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PickerField(
                    label = "Start time",
                    value = state.startMinutes?.let(CreateEventViewModel::formatMinutes),
                    placeholder = "--:--",
                    iconRes = R.drawable.ic_schedule,
                    error = viewModel.startError(),
                    onClick = { startDialogOpen = true },
                    modifier = Modifier.weight(1f)
                )
                PickerField(
                    label = "End time",
                    value = state.endMinutes?.let(CreateEventViewModel::formatMinutes),
                    placeholder = "--:--",
                    iconRes = R.drawable.ic_schedule,
                    error = viewModel.endError(),
                    onClick = { endDialogOpen = true },
                    modifier = Modifier.weight(1f)
                )
            }

            FormTextField(
                value = state.location,
                onValueChange = viewModel::onLocationChange,
                label = "Location",
                placeholder = "Almaty, KBTU",
                error = viewModel.locationError(),
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            )

            if (viewModel.organizerEmail.isNotBlank()) {
                Text(
                    text = "Organizer · ${viewModel.organizerEmail}",
                    fontFamily = AlumniSansSc,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = Muted,
                    maxLines = 1
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            val formError = state.errorMessage
            if (formError != null) {
                Text(
                    text = formError,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                    fontFamily = AlumniSansSc,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }
            SubmitButton(
                onClick = viewModel::save,
                isLoading = state.isSaving,
                label = "SAVE EVENT",
                fontSize = 22.sp,
                letterSpacing = 4.sp
            )
        }
    }

    if (dateDialogOpen) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.dateMillis
                ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { dateDialogOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dateDialogOpen = false
                        viewModel.onDatePicked(pickerState.selectedDateMillis)
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { dateDialogOpen = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (startDialogOpen) {
        TimePickerDialog(
            title = "Start time",
            initialMinutes = state.startMinutes,
            onDismiss = { startDialogOpen = false },
            onConfirm = {
                startDialogOpen = false
                viewModel.onStartPicked(it)
            }
        )
    }

    if (endDialogOpen) {
        TimePickerDialog(
            title = "End time",
            initialMinutes = state.endMinutes,
            onDismiss = { endDialogOpen = false },
            onConfirm = {
                endDialogOpen = false
                viewModel.onEndPicked(it)
            }
        )
    }
}

@Composable
private fun CoverPicker(
    coverUri: String?,
    onPick: () -> Unit
) {
    val bitmap = rememberContentImageBitmap(coverUri)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CoverPlaceholder)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp))
            .clickable(onClick = onPick)
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Event cover preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Text(
                text = "TAP TO REPLACE",
                fontFamily = AlumniSansSc,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(id = R.drawable.tab_create_plus),
                    contentDescription = null,
                    tint = CreateNavy,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "ADD COVER IMAGE",
                    fontFamily = AlumniSansSc,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp,
                    color = CreateNavy
                )
                Text(
                    text = "Tap to choose a photo",
                    fontFamily = AlumniSansSc,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = Muted
                )
            }
        }
    }
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    error: String?,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { FieldLabel(label) },
            placeholder = {
                Text(
                    text = placeholder,
                    fontFamily = AlumniSansSc,
                    fontSize = 13.sp,
                    color = Muted
                )
            },
            singleLine = singleLine,
            minLines = minLines,
            isError = error != null,
            shape = FieldShape,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            colors = formColors()
        )
        if (error != null) {
            Text(
                text = error,
                color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                fontFamily = AlumniSansSc,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontFamily = AlumniSansSc,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
}

@Composable
private fun formColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedBorderColor = Purple,
    unfocusedBorderColor = BorderGray,
    focusedLabelColor = Purple,
    unfocusedLabelColor = Muted
)

@Composable
private fun PickerField(
    label: String,
    value: String?,
    placeholder: String,
    iconRes: Int,
    error: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Color.White)
                .border(
                    1.dp,
                    if (error != null) {
                        androidx.compose.material3.MaterialTheme.colorScheme.error
                    } else {
                        BorderGray
                    },
                    RoundedCornerShape(13.dp)
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = Muted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = label,
                        fontFamily = AlumniSansSc,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = Muted
                    )
                    Text(
                        text = value ?: placeholder,
                        fontFamily = AlumniSansSc,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = if (value != null) Color.Black else Muted,
                        maxLines = 1
                    )
                }
            }
        }
        if (error != null) {
            Text(
                text = error,
                color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                fontFamily = AlumniSansSc,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun TimePickerDialog(
    title: String,
    initialMinutes: Int?,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val pickerState = rememberTimePickerState(
        initialHour = (initialMinutes ?: 12 * 60) / 60,
        initialMinute = (initialMinutes ?: 12 * 60) % 60,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontFamily = AlumniSansSc,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            androidx.compose.material3.TimePicker(state = pickerState)
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(pickerState.hour * 60 + pickerState.minute) }
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
