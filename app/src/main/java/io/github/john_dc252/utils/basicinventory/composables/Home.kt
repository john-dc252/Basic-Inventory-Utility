package io.github.john_dc252.utils.basicinventory.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.john_dc252.utils.basicinventory.IdCheckPage
import io.github.john_dc252.utils.basicinventory.RegistrationPage

@Composable
fun Home(onActionSelected: (Any) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Card {
            Column(
                modifier = Modifier.padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Home",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                )
                RegistryActions(onActionSelected)
            }
        }
    }
}

@Composable
fun RegistryActions(onActionSelected: (Any) -> Unit) {
    var isBusy by rememberSaveable { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxWidth()) {
        if (isBusy) {
            Text(
                text = "Scanning...",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Button(
                onClick = {
                    onActionSelected(RegistrationPage)
                }
            ) {
                Text("Register")
            }
            Button(
                onClick = {
                    onActionSelected(IdCheckPage)
                }
            ) {
                Text("Identify ISBN")
            }
        }
    }
}
