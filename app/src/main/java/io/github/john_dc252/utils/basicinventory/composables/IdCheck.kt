package io.github.john_dc252.utils.basicinventory.composables

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import io.github.john_dc252.utils.basicinventory.MainActivity

@Composable
fun IdCheckView(savedState: SavedStateHandle, onScan: () -> Unit, onDismiss: () -> Unit) {
    val mainActivity = LocalActivity.current as MainActivity
    var scannedIsbn by rememberSaveable { mutableStateOf("") }
    val scannedBookInfo = mainActivity.getBookInfo(scannedIsbn)
    val scanStatus = savedState.get<ScanStatus>(SCAN_STATUS_STATE_KEY)
    var isBusy by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(scanStatus) {
        if (scanStatus == null) {
            return@LaunchedEffect
        }

        if (scanStatus is ScanStatus.Success) {
            scannedIsbn = scanStatus.code
        }

        isBusy = false
        savedState.remove<ScanStatus>(SCAN_STATUS_STATE_KEY)
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    OutlinedButton(onClick = { onDismiss() }) { Text("Back") }
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

                if (scannedBookInfo == null) {
                    Text("ISBN: $scannedIsbn")
                    Text("No Matches")
                } else {
                    Column {
                        Text("ISBN: ${scannedBookInfo.isbn}")
                        Text("Title: ${scannedBookInfo.title}")
                        Text("Author: ${scannedBookInfo.author}")
                    }
                }
            }
        }
    }
}
