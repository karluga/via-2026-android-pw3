package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.DataStoreManager
import com.example.myapplication.data.ThemeOptions
import com.example.myapplication.ui.theme.RepositoryG2Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val dataStoreManager = remember { DataStoreManager(context) }
            val themeMode by dataStoreManager.themeModeFlow.collectAsState(initial = ThemeOptions.DEFAULT)

            LaunchedEffect(themeMode) {
                val nightMode = when (themeMode) {
                    ThemeOptions.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                    ThemeOptions.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                AppCompatDelegate.setDefaultNightMode(nightMode)
            }

            RepositoryG2Theme(themeMode = themeMode) {
                MainScreen(dataStoreManager = dataStoreManager, currentThemeMode = themeMode)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(dataStoreManager: DataStoreManager, currentThemeMode: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val savedTextState by dataStoreManager.savedTextFlow.collectAsState(initial = null)
    var inputText by rememberSaveable { mutableStateOf("") }
    var isTextLoadedFromDataStore by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(savedTextState) {
        if (!isTextLoadedFromDataStore && savedTextState != null) {
            inputText = savedTextState ?: ""
            isTextLoadedFromDataStore = true
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "RepositoryG2",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            ThemeDropdownMenu(
                selectedTheme = currentThemeMode,
                onThemeSelected = { newTheme ->
                    coroutineScope.launch {
                        dataStoreManager.saveThemeMode(newTheme)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Input text") },
                placeholder = { Text("Enter text to save") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        dataStoreManager.saveText(inputText)
                        Toast.makeText(context, "Saved to DataStore", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Save", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    val intent = Intent(context, SecondActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Go to 2-nd", fontSize = 16.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeDropdownMenu(
    selectedTheme: String,
    onThemeSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(ThemeOptions.DEFAULT, ThemeOptions.DARK, ThemeOptions.LIGHT)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedTheme,
            onValueChange = {},
            readOnly = true,
            label = { Text("Theme") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        expanded = false
                        onThemeSelected(option)
                    }
                )
            }
        }
    }
}
