package io.github.john_dc252.utils.basicinventory

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.john_dc252.utils.basicinventory.composables.CameraScanner
import io.github.john_dc252.utils.basicinventory.composables.Home
import io.github.john_dc252.utils.basicinventory.composables.IdCheckView
import io.github.john_dc252.utils.basicinventory.composables.RegistrationForm
import io.github.john_dc252.utils.basicinventory.composables.SCAN_STATUS_STATE_KEY
import io.github.john_dc252.utils.basicinventory.composables.ScanStatus
import io.github.john_dc252.utils.basicinventory.ui.theme.BasicInventoryUtilityTheme
import kotlinx.serialization.Serializable

@Serializable
internal object HomePage

@Serializable
internal object CameraScannerPage

@Serializable
internal object RegistrationPage

@Serializable
internal object IdCheckPage

class MainActivity : ComponentActivity() {

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
}

@Composable
fun Main(modifier: Modifier = Modifier) {
    Column(
        modifier,
        verticalArrangement = Arrangement.Center
    ) {
        val navController = rememberNavController()
        val mainActivity = LocalActivity.current as MainActivity

        NavHost(
            navController = navController,
            startDestination = HomePage,
        ) {
            composable<HomePage> {
                Home { navController.navigate(route = it) }
            }
            composable<CameraScannerPage>(
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { it }) },
            ) {
                val context = LocalActivity.current
                CameraScanner(
                    onCodeCaptured = { isbn ->
                        Toast.makeText(context, "ISBN: $isbn", Toast.LENGTH_LONG).show()
                        navController.previousBackStackEntry?.savedStateHandle[SCAN_STATUS_STATE_KEY] =
                            ScanStatus.Success(isbn)
                        navController.navigateUp()
                    },
                    onDismiss = { navController.navigateUp() },
                )
            }
            composable<RegistrationPage>(
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
            ) { stackEntry ->
                RegistrationForm(
                    savedState = stackEntry.savedStateHandle,
                    onRegistrationRequest = { bookInfo ->
                        mainActivity.registerBook(bookInfo)
                        navController.navigate(route = HomePage)
                    },
                    onScan = {
                        stackEntry.savedStateHandle[SCAN_STATUS_STATE_KEY] = ScanStatus.Requested
                        navController.navigate(route = CameraScannerPage)
                    },
                    onDismiss = { navController.navigateUp() }
                )
            }
            composable<IdCheckPage>(
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
            ) { stackEntry ->
                IdCheckView(
                    savedState = stackEntry.savedStateHandle,
                    onScan = {
                        stackEntry.savedStateHandle[SCAN_STATUS_STATE_KEY] = ScanStatus.Requested
                        navController.navigate(route = CameraScannerPage)
                    },
                    onDismiss = { navController.navigateUp() },
                )
            }
        }
    }
}

data class BookInfo(val isbn: String, val title: String, val author: String)

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BasicInventoryUtilityTheme {
        Main()
    }
}