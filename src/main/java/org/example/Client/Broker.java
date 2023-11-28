package org.example.Client;

import org.eclipse.paho.client.mqttv3.MqttClient;

public class Broker {
    private String ipBroker; // ip + port

    private boolean isRunning;

    public Broker(String ipBroker, boolean isRunning) {
        this.ipBroker = ipBroker;
        this.isRunning = isRunning;
    }

    public String getIpBroker() {
        return ipBroker;
    }

    public void setIpBroker(String ipBroker) {
        this.ipBroker = ipBroker;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }
}
