package Activity

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mqtt_getdata.R
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttClientState
import org.w3c.dom.Text

class ControllerDevice : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_controller_device)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var nd: TextView
        var da: TextView
        var switchButton: Button
        nd = findViewById(R.id.Nhietdo)
        da = findViewById(R.id.Doam)
        switchButton = findViewById(R.id.btnOnOff)


        MqttDataRepository.temperature.observe(this) { temp ->
            nd.text = "Nhiệt độ: $temp°C"
        }

        MqttDataRepository.humidity.observe(this) { humi ->
            da.text = "Độ ẩm: $humi%"
        }

        val mqttClient = MqttConnectionManager.mqttClient

        var isOn = false

        switchButton.setOnClickListener {
            if (mqttClient.state == MqttClientState.CONNECTED) {
                mqttClient.publishWith().topic("esp8266/client")
                    .payload((if (!isOn) "on" else "off").toByteArray())
                    .send()
                
                isOn = !isOn
            }
        }

    }
}