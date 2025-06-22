package Activity



import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttClientState
import com.hivemq.client.mqtt.MqttGlobalPublishFilter

object MqttConnectionManager {
    var mqttClient = MqttClient.builder()
        .useMqttVersion5()
        .identifier("ESPClient_ee19")
        .serverHost("13af0e06adda4c148393286447903573.s1.eu.hivemq.cloud")
        .serverPort(8883)
        .sslWithDefaultConfig()
        .buildAsync()
}
