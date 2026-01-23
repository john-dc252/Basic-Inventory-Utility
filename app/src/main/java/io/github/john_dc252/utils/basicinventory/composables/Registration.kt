package io.github.john_dc252.utils.basicinventory.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import io.github.john_dc252.utils.basicinventory.BookInfo

@Composable
fun RegistrationForm(
    savedState: SavedStateHandle,
    onRegistrationRequest: (BookInfo) -> Unit,
    onScan: () -> Unit,
    onDismiss: () -> Unit
) {
    val scanStatus = savedState.get<ScanStatus>(SCAN_STATUS_STATE_KEY)
    val isbnInput = rememberTextFieldState(initialText = "")
    val titleInput = rememberTextFieldState(initialText = "")
    val authorInput = rememberTextFieldState(initialText = "")
    var isBusy by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(scanStatus) {
        if (scanStatus == null) {
            return@LaunchedEffect
        }

        if (scanStatus is ScanStatus.Success) {
            isbnInput.edit {
                this.replace(0, this.length, scanStatus.code)
            }
        }

        isBusy = false
        savedState.remove<ScanStatus>(SCAN_STATUS_STATE_KEY)
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Card(modifier = Modifier.padding(32.dp)) {
            Column(
                modifier = Modifier.run {
                    fillMaxWidth()
                        .padding(16.dp)
                },
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row {
                    Text(text = "Registration")
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextField(
                        modifier = Modifier.weight(2f),
                        state = isbnInput,
                        label = { Text("ISBN") },
                    )
                    Button(
                        enabled = !isBusy,
                        onClick = {
                            isBusy = true
                            onScan()
                        },
                    ) {
                        Text(
                            if (isBusy) {
                                "Please wait"
                            } else {
                                "Scan"
                            }
                        )
                    }
                }
                Row {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        state = titleInput,
                        label = { Text("Title") },
                    )
                }
                Row {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        state = authorInput,
                        label = { Text("Author") },
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                        FilledTonalButton(
                            onClick = {
                                onRegistrationRequest(
                                    BookInfo(
                                        isbnInput.text.toString(),
                                        titleInput.text.toString(),
                                        authorInput.text.toString()
                                    )
                                )
                            }
                        ) {
                            Text("Register")
                        }
                    }
                }
            }
        }
    }
}
