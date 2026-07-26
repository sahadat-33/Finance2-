import re

file_path = "app/src/main/java/com/example/ui/PinScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# Add imports
if "androidx.biometric.BiometricManager" not in content:
    content = content.replace(
        "import androidx.compose.foundation.gestures.detectTapGestures",
        """import androidx.compose.foundation.gestures.detectTapGestures
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.compose.ui.platform.LocalContext
import android.content.Context"""
    )

content = content.replace(
    "fun PinScreen(onVerify: (String) -> Boolean) {",
    "fun PinScreen(onVerify: (String) -> Boolean, onUnlocked: () -> Unit = {}) {"
)

content = content.replace(
    '''    var pinValue by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }''',
    '''    var pinValue by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val sharedPrefs = context.getSharedPreferences("FinanceTrackerPrefs", Context.MODE_PRIVATE)
    val biometricEnabled = sharedPrefs.getBoolean("biometric_enabled", false)
    val canUseBiometric = remember {
        biometricEnabled && BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun launchBiometric() {
        val act = activity ?: return
        val executor = ContextCompat.getMainExecutor(context)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onUnlocked()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // User dismissed or hardware error — PIN entry remains visible, no action needed.
            }
            override fun onAuthenticationFailed() {
                // Not recognized — user can retry via button or just use PIN.
            }
        }
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Finance Tracker")
            .setSubtitle("Use fingerprint or face to unlock")
            .setNegativeButtonText("Use PIN instead")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
        BiometricPrompt(act, executor, callback).authenticate(promptInfo)
    }

    LaunchedEffect(Unit) {
        if (canUseBiometric) {
            launchBiometric()
        }
    }'''
)

content = content.replace(
    '''textStyle = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            )
        }
    }
}''',
    '''textStyle = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            )
            
            if (canUseBiometric) {
                Spacer(Modifier.height(20.dp))
                TextButton(
                    onClick = { launchBiometric() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Biometric unlock",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Use Fingerprint / Face ID")
                }
            }
        }
    }
}'''
)

with open(file_path, "w") as f:
    f.write(content)
