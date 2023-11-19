package org.example.Publisher;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttPublisher {

    public static void main(String[] args) {
        String broker = "tcp://localhost:1883"; // adresa și portul Mosquitto
        String clientId = "Publisher";
        String topic = "test/topic";
        String content = "Mesaj de la Publisher Test123";
        int qos = 2;

        try {
            MqttClient client = new MqttClient(broker, clientId, new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            System.out.println("Conectare la broker: " + broker);
            client.connect(connOpts);
            System.out.println("Conectat la broker");

            System.out.println("Publicare mesaj la topic: " + topic);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            client.publish(topic, message);

            System.out.println("Mesaj publicat cu succes");

            client.disconnect();
            System.out.println("Deconectat");
        } catch (MqttException me) {
            me.printStackTrace();
        }
    }
}
