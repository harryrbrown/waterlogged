package com.hrb116.waterlogged.mobile

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    private val TAG = "MobileMainActivity"
    private val permissions = setOf(
        androidx.health.connect.client.permission.HealthPermission.getReadPermission(androidx.health.connect.client.records.HydrationRecord::class),
        androidx.health.connect.client.permission.HealthPermission.getWritePermission(androidx.health.connect.client.records.HydrationRecord::class)
    )

    private var permissionsGrantedState = mutableStateOf<Boolean?>(null)

    private val requestPermissions = registerForActivityResult(
        androidx.health.connect.client.PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        if (granted.containsAll(permissions)) {
            Log.d(TAG, "Health Connect permissions granted")
            permissionsGrantedState.value = true
        } else {
            Log.d(TAG, "Health Connect permissions denied")
            permissionsGrantedState.value = false
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkPermissions()
        
        // Force advertise the phone app capability to connected watch nodes
        lifecycleScope.launch {
            try {
                Wearable.getCapabilityClient(this@MainActivity)
                    .addLocalCapability("waterlogged_phone_app")
                    .await()
                Log.d(TAG, "Successfully advertised waterlogged_phone_app capability")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to advertise capability", e)
            }
        }

        setContent {
            val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
            val colorScheme = if (isDarkTheme) {
                darkColorScheme(
                    primary = Color(0xFF3DBCDC),
                    onPrimary = Color(0xFF003258),
                    primaryContainer = Color(0xFF00497D),
                    onPrimaryContainer = Color(0xFFD1E4FF),
                    secondary = Color(0xFFBBC7DB),
                    onSecondary = Color(0xFF253140),
                    secondaryContainer = Color(0xFF3B4858),
                    onSecondaryContainer = Color(0xFFD6E3F7)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF3DBCDC),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFD1E4FF),
                    onPrimaryContainer = Color(0xFF001D36),
                    secondary = Color(0xFF535F70),
                    onSecondary = Color.White,
                    secondaryContainer = Color(0xFFD6E3F7),
                    onSecondaryContainer = Color(0xFF101C2B)
                )
            }

            MaterialTheme(colorScheme = colorScheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    var selectedTab by remember { mutableStateOf("settings") }
                    
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text("Waterlogged") },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        },
                        bottomBar = {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Text("⚙️") },
                                    label = { Text("Settings") },
                                    selected = selectedTab == "settings",
                                    onClick = { selectedTab = "settings" }
                                )
                                NavigationBarItem(
                                    icon = { Text("ℹ️") },
                                    label = { Text("Guide") },
                                    selected = selectedTab == "guide",
                                    onClick = { selectedTab = "guide" }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                            if (permissionsGrantedState.value == false) {
                                Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "⚠️ Health Connect permissions are denied. Waterlogged cannot sync your data. Please enable them in your phone settings.",
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                if (selectedTab == "settings") {
                                    SettingsScreen(
                                        onSaveSettings = { glass, bottle, largeBottle, goal ->
                                            syncSettingsToWatch(glass, bottle, largeBottle, goal)
                                        }
                                    )
                                } else {
                                    GuideScreen()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkPermissions() {
        if (HealthConnectClient.getSdkStatus(this) == HealthConnectClient.SDK_AVAILABLE) {
            val healthConnectClient = HealthConnectClient.getOrCreate(this)
            lifecycleScope.launch {
                val granted = healthConnectClient.permissionController.getGrantedPermissions()
                if (granted.containsAll(permissions)) {
                    permissionsGrantedState.value = true
                } else {
                    permissionsGrantedState.value = false
                    requestPermissions.launch(permissions)
                }
            }
        } else {
            permissionsGrantedState.value = false
        }
    }

    private fun syncSettingsToWatch(glass: String, bottle: String, largeBottle: String, goal: String) {
        lifecycleScope.launch {
            try {
                val putDataMapReq = PutDataMapRequest.create("/settings")
                putDataMapReq.dataMap.putString("glass_size", glass)
                putDataMapReq.dataMap.putString("bottle_size", bottle)
                putDataMapReq.dataMap.putString("large_bottle_size", largeBottle)
                putDataMapReq.dataMap.putString("daily_goal", goal)
                putDataMapReq.dataMap.putLong("timestamp", System.currentTimeMillis())

                val request = putDataMapReq.asPutDataRequest()
                request.setUrgent()
                Wearable.getDataClient(this@MainActivity).putDataItem(request).await()
                Toast.makeText(this@MainActivity, "Settings synced to watch!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync settings", e)
                Toast.makeText(this@MainActivity, "Failed to sync", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun SettingsScreen(onSaveSettings: (String, String, String, String) -> Unit) {
    var glassSize by remember { mutableStateOf("250") }
    var bottleSize by remember { mutableStateOf("500") }
    var largeBottleSize by remember { mutableStateOf("750") }
    var dailyGoal by remember { mutableStateOf("2000") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Waterlogged Companion", style = MaterialTheme.typography.headlineSmall)
        
        Text(text = "Configure the water presets that appear on your watch.")

        OutlinedTextField(
            value = glassSize,
            onValueChange = { glassSize = it },
            label = { Text("Glass Size (ml)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = bottleSize,
            onValueChange = { bottleSize = it },
            label = { Text("Bottle Size (ml)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = largeBottleSize,
            onValueChange = { largeBottleSize = it },
            label = { Text("Large Bottle Size (ml)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        OutlinedTextField(
            value = dailyGoal,
            onValueChange = { dailyGoal = it },
            label = { Text("Daily Goal (ml)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { onSaveSettings(glassSize, bottleSize, largeBottleSize, dailyGoal) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sync Settings to Watch")
        }
    }
}

@Composable
fun GuideScreen() {
    rememberScrollState().let { scrollState ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = androidx.compose.ui.res.stringResource(id = com.hrb116.waterlogged.R.string.guide_title), style = MaterialTheme.typography.headlineSmall)
            
            Text(text = androidx.compose.ui.res.stringResource(id = com.hrb116.waterlogged.R.string.guide_intro))
            
            Text(text = androidx.compose.ui.res.stringResource(id = com.hrb116.waterlogged.R.string.guide_step1), style = MaterialTheme.typography.titleMedium)
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.hrb116.waterlogged.R.drawable.step1_health_connect),
                contentDescription = "Health Connect Permissions Screenshot",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            )

            Text(text = androidx.compose.ui.res.stringResource(id = com.hrb116.waterlogged.R.string.guide_step2), style = MaterialTheme.typography.titleMedium)
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.hrb116.waterlogged.R.drawable.step2_present),
                contentDescription = "Partner Apps Connected Screenshot",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            )
            
            Text(text = androidx.compose.ui.res.stringResource(id = com.hrb116.waterlogged.R.string.guide_step3), style = MaterialTheme.typography.titleMedium)
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.hrb116.waterlogged.R.drawable.step2_not_present),
                contentDescription = "Sync Apps Button Screenshot",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.hrb116.waterlogged.R.drawable.step3_consent),
                contentDescription = "Consent Forms Screenshot",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            )

            Text(text = androidx.compose.ui.res.stringResource(id = com.hrb116.waterlogged.R.string.guide_step4), style = MaterialTheme.typography.titleMedium)
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.hrb116.waterlogged.R.drawable.step4_nutrition1),
                contentDescription = "Nutrition Settings Screenshot 1",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.hrb116.waterlogged.R.drawable.step4_nutrition2),
                contentDescription = "Nutrition Settings Screenshot 2",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.hrb116.waterlogged.R.drawable.step4_nutrition3),
                contentDescription = "Nutrition Settings Screenshot 3",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
