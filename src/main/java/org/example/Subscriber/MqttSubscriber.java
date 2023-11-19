package org.example.Subscriber;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttSubscriber {

    public static void main(String[] args) {
        String broker = "tcp://localhost:1883"; // adresa și portul Mosquitto
        String clientId = "Subscriber";
        String topic = "test/topic";
        int qos = 0;

        try {
            MqttClient client = new MqttClient(broker, clientId, new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            System.out.println("Conectare la broker: " + broker);
            client.connect(connOpts);
            System.out.println("Conectat la broker");

            System.out.println("Abonare la topic: " + topic);
            client.subscribe(topic, qos);

            // Definirea unui ascultător pentru mesajele primite
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("Conexiune pierdută");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    System.out.println("Mesaj primit de la topic: " + topic);
                    System.out.println("Conținut: " + new String(message.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Aici putem implementa acțiuni suplimentare după livrarea unui mesaj
                }
            });

            // Așteptare pentru a permite primirea mesajelor
            Thread.sleep(500000);

            client.disconnect();
            System.out.println("Deconectat");
        } catch (MqttException | InterruptedException me) {
            me.printStackTrace();
        }
    }
}
