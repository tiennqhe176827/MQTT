package Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mqtt_getdata.R
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import io.reactivex.plugins.RxJavaPlugins

class InputServerInfor : AppCompatActivity() {

    private lateinit var editClientId: EditText
    private lateinit var editServerName: EditText
    private lateinit var editPort: EditText
    private lateinit var editUsername: EditText
    private lateinit var editPassword: EditText
    private lateinit var btnSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_input_server_infor)

        // RxJava error handler (important!)
        RxJavaPlugins.setErrorHandler { e ->
            Log.e("RxJava", "Unhandled RxJava error", e)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        editClientId = findViewById(R.id.editClientId)
        editServerName = findViewById(R.id.editServerName)
        editPort = findViewById(R.id.editPort)
        editUsername = findViewById(R.id.editUsername)
        editPassword = findViewById(R.id.editPassword)
        btnSubmit = findViewById(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
//            val clientId = "ESPClient_ee19"
//            val server = "13af0e06adda4c148393286447903573.s1.eu.hivemq.cloud"
//            val port = 8883
            val username = "tiennqhe176827"
            val password = "Tiennqhe176827"

//            val mqttClient = MqttClient.builder()
//                .useMqttVersion5()
//                .identifier(clientId)
//                .serverHost(server)
//                .serverPort(port)
//                .sslWithDefaultConfig()
//                .buildAsync()
            val mqttClient = MqttConnectionManager.mqttClient

            try {
                mqttClient.connectWith()
                    .simpleAuth()
                    .username(username)
                    .password(password.toByteArray())
                    .applySimpleAuth()
                    .send()
                    .whenComplete { _, throwable ->
                        if (throwable == null) {
                            Log.d("MQTT", "✅ Connected to MQTT")

                            mqttClient.subscribeWith()
                                .topicFilter("esp8266/dht11")
                                .send()
                                .whenComplete { _, subError ->
                                    if (subError == null) {
                                        Log.d("MQTT", "✅ Subscribed to topic")

                                        mqttClient.publishes(MqttGlobalPublishFilter.ALL) { publish ->
                                            val payload = publish.payload.orElse(null)
                                                ?.let { bufferOriginal ->
                                                    val buffer = bufferOriginal.duplicate()
                                                    val bytes = ByteArray(buffer.remaining())
                                                    buffer.get(bytes)
                                                    String(bytes, Charsets.UTF_8)
                                                } ?: return@publishes

                                            Log.d("MQTT", "📥 Received: $payload")
                                            val jsonObject = org.json.JSONObject(payload)
                                            val humidity = jsonObject.getDouble("humidity")
                                            val temperature = jsonObject.getDouble("temperature")

                                            MqttDataRepository.temperature.postValue(temperature)
                                            MqttDataRepository.humidity.postValue(humidity)


                                        }
                                    } else {
                                        Log.e("MQTT", "❌ Failed to subscribe", subError)
                                    }
                                }
                            val intent = Intent(this, ControllerDevice::class.java)
                            startActivity(intent)
                            finish()

                        } else {
                            Log.e("MQTT", "❌ MQTT connection failed", throwable)
                        }
                    }
            } catch (e: Exception) {
                Toast.makeText(this, "Connection failed: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("nqt", "Connection failed", e)
            }
        }
    }
}