package com.example.cmpe277_app3

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cmpe277_app3.ui.theme.CMPE277App3Theme

class MainActivity : ComponentActivity() {

    private var prompt by mutableStateOf("")
    private var responseText by mutableStateOf("")

    private var isProcessing by mutableStateOf(false)

    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val data = intent?.getStringExtra("response")
            Log.d("CallOpenAIService", "response = $data")
            responseText = data.toString()
            isProcessing = false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CMPE277App3Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(text = "ChatGPT API Tester", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = prompt,
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 5,
                                    onValueChange = { newText ->
                                        prompt = newText
                                    },
                                    label = {
                                        Text(text = "Prompt")
                                    })
                                OutlinedTextField(
                                    value = responseText,
                                    onValueChange = {},
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 5,
                                    maxLines = 5,
                                    readOnly = true,
                                    label = {
                                        Text(text = "Response")
                                    })
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    if(!isProcessing){
                                        Button(onClick = {
                                            val intent = Intent(this@MainActivity, CallOpenAIService::class.java).apply {
                                                putExtra("prompt", prompt)
                                            }
                                            startService(intent)
                                            isProcessing = true
//                                            SharedData.prompt = prompt
//                                            Intent(this@MainActivity, CallOpenAIService::class.java).also { intent ->
//                                                bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
//
//                                            }
                                        }) {
                                            Text(text = "Send")
                                        }
                                    }else{
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.width(20.dp),
                                                color = MaterialTheme.colorScheme.secondary,
                                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                            )
                                            Button(onClick = {
                                                isProcessing = false
                                            }) { Text(text = "Cancel") }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        val filter = IntentFilter("cmpe277.app3.UPDATE_ACTIVITY")
        registerReceiver(updateReceiver, filter, RECEIVER_EXPORTED)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(updateReceiver)
    }

}

@Composable
@Preview(showBackground = true)
fun GreetingPreview() {
    CMPE277App3Theme { }
}