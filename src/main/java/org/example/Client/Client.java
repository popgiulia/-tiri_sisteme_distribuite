package org.example.Client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.paho.client.mqttv3.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

import static org.eclipse.paho.client.mqttv3.MqttClient.generateClientId;

public class Client implements MqttCallback {
    private String broker;
    private List<Broker> brokerList;
    private final String id;      // Client id
    private final int qos;        // Quality of Service (QoS)
    private MqttClient mqttClient; // obiect pentru comunicarea cu broker-ul
    private boolean reconnecting;
    private boolean connected;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private NewsList newsList;

    public Client() {
        brokerList = new ArrayList<>();
        Broker broker1 = new Broker("tcp://localhost:1883", false);
        Broker broker2 = new Broker("tcp://localhost:1884", false);
        brokerList.add(broker1);
        brokerList.add(broker2);


        this.id = generateClientId();
        this.qos = 2;
        newsList = new NewsList();
        reconnecting = false;
        connected = false;
    }

    public void connectToBroker3() {
        Future<?> connectFuture = executorService.submit(() -> {
            while (true) {
                // incercam sa ne conetcam la unul din brokeri
                for (Broker myBroker : brokerList) {
                    try {
                        mqttClient = new MqttClient(myBroker.getIpBroker(), id, null);
                        mqttClient.setCallback(this);
                        MqttConnectOptions connOpts = new MqttConnectOptions();
                        connOpts.setCleanSession(true);

                        // Conectare la broker
                        System.out.println("Conectare la broker : " + myBroker.getIpBroker());
                        mqttClient.connect(connOpts);
                        System.out.println("ID-ul acestui client este: " + id);
                        myBroker.setRunning(true);
                        this.broker = myBroker.getIpBroker();
                        connected = true;
                        return; // iesim din bucla for atunci cand ne-am conectat la broker-ul curent
                    } catch (MqttException e) {
                        connected = false;
                        myBroker.setRunning(false);
                        System.out.println("Conectarea la broker-ul " + myBroker.getIpBroker() + " a eșuat");
                        System.out.println("Se incearca conectarea la un alt broker");
                    }
                }
                // Așteaptăm 5 secunde, dupa care incercam o noua runda de conectare
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });

        try {
            connectFuture.get(); // Așteaptă finalizarea firului de execuție
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    // nu o mai folosim
    public void connectToBroker2() {

        try {
            mqttClient = new MqttClient(brokerList.get(0).getIpBroker(), id, null);
            mqttClient.setCallback(this);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            // Conectare la broker
            System.out.println("Conectare la broker 1: " + broker);
            mqttClient.connect(connOpts);
            System.out.println("ID-ul acestui client este: " + id);

        } catch (MqttException e) {
            System.out.println("Conectarea la broker-ul 1 a eșuat");
            try {
                mqttClient = new MqttClient(brokerList.get(1).getIpBroker(), id, null);
                mqttClient.setCallback(this);
                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);

                // Conectare la broker
                System.out.println("Conectare la broker 2: " + broker);
                mqttClient.connect(connOpts);
                System.out.println("ID-ul acestui client este: " + id);
            } catch (MqttSecurityException ex) {
                throw new RuntimeException(ex);
            } catch (MqttException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    // nu o mai folosim
    public void connectToBroker() {
        Future<Void> connectFuture = executorService.submit(() -> {
            while (true) {
                try {
                    mqttClient = new MqttClient(broker, id, null);
                    mqttClient.setCallback(this);
                    MqttConnectOptions connOpts = new MqttConnectOptions();
                    connOpts.setCleanSession(true);

                    // Conectare la broker
                    System.out.println("Conectare la broker: " + broker);
                    mqttClient.connect(connOpts);
                    System.out.println("ID-ul acestui client este: " + id);
                    return null; // Ieșim din firul de execuție dacă conectarea a reușit
                } catch (MqttException e) {
                    System.out.println("Conectarea a eșuat. Așteptăm 5 secunde înainte de a încerca din nou.");
                    try {
                        Thread.sleep(5000); // Așteaptă 5 secunde înainte de a încerca din nou
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        try {
            connectFuture.get(); // Așteaptă finalizarea firului de execuție
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void disconnectFromBroker() throws MqttException {
        mqttClient.disconnect();
        System.out.println("Deconectat de la broker\n");
    }

    public void subscribe(String topic) throws MqttException {
        this.mqttClient.subscribe(topic);
    }

    @Override
    public void connectionLost(Throwable cause) {
        connected = false;
        System.out.println("\nConexiune pierdută cu broker-ul\n");
        startReconnectThread();

    }

    // nu merge bine functia
    private void startReconnectThread() {

        Thread connectThread = new Thread(() -> {
            if (!connected) {
                while (true) {
                    // incercam sa ne conetcam la unul din brokeri
                    for (Broker myBroker : brokerList) {
                        try {
                            mqttClient = new MqttClient(myBroker.getIpBroker(), id, null);
                            mqttClient.setCallback(this);
                            MqttConnectOptions connOpts = new MqttConnectOptions();
                            connOpts.setCleanSession(true);

                            // Conectare la broker
                            System.out.println("Conectare la broker : " + myBroker.getIpBroker());
                            mqttClient.connect(connOpts);
                            System.out.println("ID-ul acestui client este: " + id);
                            System.out.println("Connectare cu SUCCES!!!");
                            myBroker.setRunning(true);
                            this.broker = myBroker.getIpBroker();
                            connected = true;
                            return;
                        } catch (MqttException e) {
                            myBroker.setRunning(false);
                            connected = false;
                            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                            System.out.println("Conectarea la broker-ul " + myBroker.getIpBroker() + " a eșuat");
                            System.out.println("Se incearca conectarea la un alt broker\n");
                        }
                    }
                    // Așteaptăm 5 secunde, dupa care incercam o noua runda de conectare
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        connectThread.start();
    }

    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload());

        // Convertim JSON-ul în obiectul News
        News news = deserializeNews(payload);

        // Afișăm știrea pe ecran
        System.out.println("\nReceived News:");
        System.out.println("ID: " + news.getId());
        System.out.println("Title: " + news.getTitle());
        System.out.println("Content: " + news.getContent());
        System.out.println("Topic: " + news.getTopic());
        System.out.println();

        newsList.addNews(news);

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Aici puteți implementa acțiuni suplimentare după livrarea unui mesaj
    }

    private News deserializeNews(String json) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, News.class);
    }


    /**
     * Functie care scrie in fisierul de log-uri un anumit mesaj
     *
     * @param mesaj Mesajul care va fi scris in fisier-ul de log-uri
     *              In fata mesajului se va scrie dateTime-ul in care s-a scris acel mesaj
     */
    public void writeToLogFile(String mesaj) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("./src/main/java/org/example/logs.txt", true))) {
            String timestamp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());

            // Construire mesaj cu timestamp
            String mesajFinal = "[" + timestamp + "] " + mesaj;
            writer.write(mesajFinal);
            writer.newLine();
            //System.out.println("Mesajul a fost scris cu succes în fișier.");
        } catch (IOException e) {
            //System.err.println("Eroare la scrierea în fișier: " + e.getMessage());
        }
    }

    /**
     * Functie care adauga o stire (obiect News) intr-o lista de stiri (obiect NewsList)
     * Componentele stirii vor fi citite de la tastatura
     * La sfarsit noua stire va fi publicata (trimisa catre broker)
     */
    public void addNewsMenu() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Introdu titlul stirii: ");
        String myTitle = scanner.nextLine();

        System.out.print("Introdu continutul stirii: ");
        String myContent = scanner.nextLine();

        System.out.print("Introdu topicul stirii: ");
        String myTopic = scanner.nextLine();


        News myNews = new News(this.id, myTitle, myContent, myTopic);
        this.newsList.addNews(myNews);   // adaugarea stirii in lista
        publishNews(myNews);             // publicare stire
    }


    /**
     * Functie care publica o stire
     *
     * @param news stirea care va fi publicata
     * @return
     */
    public Future<Void> publishNews(News news) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        String topic = news.getTopic(); // topicul stirii
        String payload = news.toJson(); // Convertim obiectul News la format JSON

        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(this.qos);
        try {
            mqttClient.publish(topic, message); // publicare
            System.out.println("Stire publicata cu succes");
            future.complete(null);
        } catch (MqttException e) {
            System.out.println("Stirea nu a putut fi publicata");
            future.completeExceptionally(e);
        }
        return future;
    }


    public void startUserInputThread(Client c, Topics topics) {
        executorService.submit(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                System.out.println(
                        "1. Afiseaza toate topic-urile\n" +
                                "2. Abonare la un topic\n" +
                                "3. Adauga o stire\n" +
                                "4. Vizualizare lista stiri\n" +
                                "5. Vizualizare detalii stire\n" +
                                "20. Exit\n"
                );
                System.out.print("Optiunea mea este: ");
                int choice = scanner.nextInt();
                switch (choice) {

                    case 1:
                        topics.printAllTopics();
                        break;
                    case 2:
                        System.out.print("Introdu numele topicului la care doresti sa te abonezi(ex:crypto):");
                        String newTopic = scanner.next();
                        if (topics.existsTopic(newTopic)) {
                            System.out.print("Abonare cu succes\n");
                            c.subscribe(newTopic);
                        } else {
                            System.out.print("Nu exista acest topic\n");
                        }
                        break;

                    case 3:
                        addNewsMenu();
                        break;

                    case 4:
                        System.out.println("Lista de stiri:");
                        this.newsList.printAllNews();
                        break;

                    case 5:
                        System.out.print("Introdu indexul stirii:");
                        String myStringIndex = scanner.next();
                        int index = Integer.parseInt(myStringIndex);
                        newsList.printNewsWithIndex(index);
                        break;

                    case 20:
                        try {
                            c.disconnectFromBroker();
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Programul s-a inchis cu succes!");
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Alegere invalidă!");
                }
            }
        });
    }

    public static void main(String[] args) throws MqttException {
        Client c = new Client();
        Topics topics = new Topics();
        topics.addNewTopic("test");
        topics.addNewTopic("crypto");
        topics.addNewTopic("ai");
        topics.addNewTopic("blockchain");
        topics.addNewTopic("vremea");


        //c.writeToLogFile("Acesta este un mesaj de test");
        c.connectToBroker3();
        c.startUserInputThread(c, topics);
    }
}
