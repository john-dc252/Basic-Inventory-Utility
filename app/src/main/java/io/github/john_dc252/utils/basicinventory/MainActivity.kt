package io.github.john_dc252.utils.basicinventory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import io.github.john_dc252.utils.basicinventory.ui.theme.BasicInventoryUtilityTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable

@Serializable
private object HomePage;

@Serializable
private object RegistrationPage;

@Serializable
private object IdCheckPage;

class MainActivity : ComponentActivity() {

    private val scannerOptions = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_EAN_13,
        )
        .allowManualInput()
        .enableAutoZoom()
        .build()
    private val bookRegistry = mutableMapOf<String, BookInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BasicInventoryUtilityTheme {
                Scaffold(
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.background)
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.displayCutout)
                ) { innerPadding ->
                    Main(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    fun registerBook(bookInfo: BookInfo) {
        bookRegistry[bookInfo.isbn] = bookInfo
    }

    fun getBookInfo(isbn: String): BookInfo? {
        return bookRegistry[isbn]
    }

    suspend fun scanCode(): Barcode {
        val scanner = GmsBarcodeScanning.getClient(this, scannerOptions)
        return scanner.startScan().await()
    }
}

@Composable
fun Main(modifier: Modifier = Modifier) {
    Column(
        modifier,
        verticalArrangement = Arrangement.Center
    ) {
        val navController = rememberNavController()
        val mainActivity = LocalContext.current as MainActivity

        NavHost(
            navController = navController,
            startDestination = HomePage,
        ) {
            composable<HomePage> {
                Home { navController.navigate(route = it) }
            }
            composable<RegistrationPage>(
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
            ) {
                RegistrationForm(
                    onRegistrationRequest = { bookInfo ->
                        mainActivity.registerBook(bookInfo)
                        navController.navigate(route = HomePage)
                    },
                    onDismiss = { navController.popBackStack() }
                )
            }
            composable<IdCheckPage>(
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
            ) {
                IdCheckView { navController.popBackStack() }
            }
        }
    }
}

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

data class BookInfo(val isbn: String, val title: String, val author: String)

@Composable
fun RegistrationForm(onRegistrationRequest: (BookInfo) -> Unit, onDismiss: () -> Unit) {
    val mainActivity = LocalContext.current as MainActivity
    val isbnInput = rememberTextFieldState(initialText = "")
    val titleInput = rememberTextFieldState(initialText = "")
    val authorInput = rememberTextFieldState(initialText = "")
    val asyncScope = rememberCoroutineScope()
    var isBusy by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Card(modifier = Modifier.padding(32.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                        onClick = {
                            asyncScope.launch {
                                isbnInput.edit {
                                    replace(
                                        0,
                                        length,
                                        mainActivity.scanCode().rawValue ?: ""
                                    )
                                }
                            }
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

@Composable
fun IdCheckView(onDismiss: () -> Unit) {
    val mainActivity = LocalContext.current as MainActivity
    var scannedIsbn by rememberSaveable { mutableStateOf("") }
    val asyncScope = rememberCoroutineScope()
    var isBusy by rememberSaveable { mutableStateOf(false) }
    val scannedBookInfo = mainActivity.getBookInfo(scannedIsbn)

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
                            asyncScope.launch {
                                scannedIsbn =
                                    mainActivity.scanCode().rawValue ?: ""
                            }
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BasicInventoryUtilityTheme {
        Main()
    }
}