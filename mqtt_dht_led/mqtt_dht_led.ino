#include <ESP8266WiFi.h>
#include "DHTesp.h"
#include <ArduinoJson.h>
#include <PubSubClient.h>
#include <WiFiClientSecure.h>

#define DHTpin 2
#define PIN_LED D1

DHTesp dht;

const char* ssid = "realme C53";   
const char* password = "11111111"; 
const char* mqtt_server = "13af0e06adda4c148393286447903573.s1.eu.hivemq.cloud";
const int mqtt_port = 8883;
const char* mqtt_username = "tiennqhe176827";
const char* mqtt_password = "Tiennqhe176827"; 

WiFiClientSecure espClient;
PubSubClient client(espClient);

unsigned long lastMsg = 0;
#define MSG_BUFFER_SIZE (50)
char msg[MSG_BUFFER_SIZE];

void setup_wifi() {
  delay(10);
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    Serial.println(WiFi.status());
    delay(500);
    // Serial.println("connect");
  }
  randomSeed(micros());
  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}

void reconnect() {
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    String clientID = "ESPClient-";
    clientID += String(random(0xffff), HEX);
     Serial.println("CliendId: " + clientID);
    if (client.connect(clientID.c_str(), mqtt_username, mqtt_password)) {
      Serial.println("connected");
      client.subscribe("esp8266/client");
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      delay(5000);
    }
  }
}
//callback nhan tin nhan
void callback(char* topic, byte* payload, unsigned int length) {
  String incommingMessage = "";
  for (int i = 0; i < length; i++) incommingMessage += (char)payload[i];
  Serial.println("Massage arived [" + String(topic) + "]" + incommingMessage);

 if (incommingMessage.equalsIgnoreCase( "On") ) {
    digitalWrite(PIN_LED, HIGH);
    Serial.println("LED turned ON");
  } else if (incommingMessage . equalsIgnoreCase( "Off")) {
    digitalWrite(PIN_LED, LOW);
    Serial.println("LED turned OFF");
  }


}
//gui tin nhan
void publishMessage(const char* topic, String payload, boolean retained) {
  if (client.publish(topic, payload.c_str(), true))
    Serial.println("Message published [" + String(topic) + "]: " + payload);
}


void setup() {
  Serial.begin(115200);
  while (!Serial) delay(1);

  dht.setup(DHTpin, DHTesp::DHT11);

  setup_wifi();
  espClient.setInsecure();
  client.setServer(mqtt_server, mqtt_port);
  client.setCallback(callback);

  pinMode(PIN_LED, OUTPUT);
  digitalWrite(PIN_LED, HIGH);


}
unsigned long timeUpdata = millis();
void loop() {
  if (!client.connected()) {
    reconnect();
  }
  client.loop();

  if (millis() - timeUpdata > 5000) {
    delay(dht.getMinimumSamplingPeriod());
    float h = dht.getHumidity();
    float t = dht.getTemperature();
    DynamicJsonDocument doc(1024);
    doc["humidity"] = h;
    doc["temperature"] = t;
    char mqtt_message[128];
    serializeJson(doc, mqtt_message);
    publishMessage("esp8266/dht11", mqtt_message, true);
    timeUpdata = millis();
  }
}
