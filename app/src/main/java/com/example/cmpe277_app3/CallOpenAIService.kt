package com.example.cmpe277_app3

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject


fun extractMessageContent(jsonString: String): String {
    val jsonObject = JSONObject(jsonString)
    val firstChoice = jsonObject.getJSONArray("choices").optJSONObject(0)
    val messageContent = firstChoice?.optJSONObject("message")?.optString("content")
    if (!messageContent.isNullOrBlank()) {
        return messageContent
    }
    return "Unknown"
}

class CallOpenAIService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread {
            val url = URL("https://api.openai.com/v1/chat/completions")
            val httpURLConnection = url.openConnection() as HttpURLConnection

            val prompt = intent?.extras?.getString("prompt").toString()

            Log.d("CallOpenAIService", "prompt = $prompt")
            try {
                httpURLConnection.requestMethod = "POST"
                httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                httpURLConnection.setRequestProperty("Authorization", "Bearer {SECRET}")
                httpURLConnection.doOutput = true
                httpURLConnection.doInput = true

                val jsonInputString = """
                      {
                           "model": "gpt-3.5-turbo",
                            "messages": [
                                {
                                    "role": "user",
                                    "content": "$prompt"
                                }
                            ],
                            "temperature": 1,
                            "max_tokens": 256,
                            "top_p": 1,
                            "frequency_penalty": 0,
                            "presence_penalty": 0
                      }
                    """.trimIndent()

                httpURLConnection.outputStream.use { os ->
                    BufferedWriter(OutputStreamWriter(os, "UTF-8")).use { writer ->
                        writer.write(jsonInputString)
                        writer.flush()
                    }
                }

                val responseCode = httpURLConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = httpURLConnection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("CallOpenAIService", "Response: $response")
                    Log.d("CallOpenAIService", "extractMessageContent: ${extractMessageContent(response)}")
                    Intent("cmpe277.app3.UPDATE_ACTIVITY").also {
                        it.putExtra("response", extractMessageContent(response))
                        sendBroadcast(it)
                    }
                } else {
                    Log.d("CallOpenAIService", "Response: ERROR $responseCode")

                }
            } finally {
                httpURLConnection.disconnect()
            }
        }.start()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

//    private val binder = LocalBinder()
//
//    inner class LocalBinder : Binder() {
//        fun getService(): CallOpenAIService = this@CallOpenAIService
//    }
//
//    override fun onBind(intent: Intent): IBinder {
//        Log.d("CallOpenAIService", "onBind ${SharedData.prompt}")
//        return binder
//    }
//
//    override fun onUnbind(intent: Intent?): Boolean {
//        Log.d("CallOpenAIService", "onUnbind")
//        return super.onUnbind(intent)
//    }
}