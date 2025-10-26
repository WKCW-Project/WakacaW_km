package org.wakacaw.km

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.wakacaw.km.core.Sysfs
import org.wakacaw.km.core.RootShell
import org.wakacaw.km.profile.Profiles
import org.wakacaw.km.ui.theme.WakacawTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WakacawTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    KernelManagerUI()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KernelManagerUI() {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val rooted by remember { mutableStateOf(RootShell.isRooted()) }

    var fastCharge by remember { mutableStateOf(false) }
    var tcp by remember { mutableStateOf("bbr") }
    var governor by remember { mutableStateOf("schedutil") }
    var io by remember { mutableStateOf("mq-deadline") }

    LaunchedEffect(Unit) {
        Sysfs.FAST_CHARGE?.let { fastCharge = RootShell.read(it)?.trim() == "1" }
        RootShell.read(Sysfs.TCP_CONGESTION)?.trim()?.let { tcp = it }
        Sysfs.CPU_GOVERNOR?.let { governor = RootShell.read(it)?.trim() ?: governor }
        Sysfs.IO_SCHEDULER?.let {
            val raw = RootShell.read(it)?.trim().orEmpty()
            // schedulers like: "mq-deadline [kyber] bfq"
            val active = raw.split(" ").firstOrNull { s -> s.startsWith("[") }?.trim('[', ']')
            if (!active.isNullOrBlank()) io = active
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("WakacaW Kernel Manager") },
                actions = {
                    AssistChip(
                        onClick = {},
                        label = { Text(if (rooted) "Root: OK" else "Root: Missing") }
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                FeatureCard(
                    title = "Fast Charge",
                    description = "Enable higher charging current (device dependent).",
                    toggle = fastCharge
                ) { fastCharge = it }
            }
            item { FeatureInput("TCP Algorithm", tcp) { tcp = it } }
            item { FeatureInput("CPU Governor", governor) { governor = it } }
            item { FeatureInput("I/O Scheduler", io) { io = it } }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            val map = mapOf(
                                "fast_charge" to if (fastCharge) "1" else "0",
                                "tcp" to tcp,
                                "cpu_gov" to governor,
                                "io_sched" to io
                            )
                            val ok = Profiles.saveAndApply(map)
                            scope.launch {
                                snackbarHostState.showSnackbar(if (ok) "Settings applied & saved" else "Apply failed")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Apply & Save") }

                    OutlinedButton(
                        onClick = {
                            val ok = Profiles.applyCurrent()
                            scope.launch { snackbarHostState.showSnackbar(if (ok) "Reapplied last profile" else "No saved profile") }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Reapply") }
                }
            }
        }
    }
}

@Composable
fun FeatureCard(title: String, description: String, toggle: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodySmall)
            }
            Switch(checked = toggle, onCheckedChange = onToggle)
        }
    }
}

@Composable
fun FeatureInput(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(14.dp)
    )
}
