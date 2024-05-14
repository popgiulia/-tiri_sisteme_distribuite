package org.example.Client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.paho.client.mqttv3.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class Client implements MqttCallback {
    private String broker;
    private List<Broker> brokerList;
    private String id;      // Client id
    private final int qos;        // Quality of Service (QoS)
    private MqttClient mqttClient; // obiect pentru comunicarea cu broker-ul
    private boolean reconnecting;
    private boolean connected;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private NewsList newsList;

    private Queue<News> orderedNewsQueue = new LinkedList<>();

    private final ExecutorService executorServiceStiri = Executors.newCachedThreadPool();

    private List<String> subscribedTopics = new ArrayList<>();

    public static int i = 0;

    public Client() {
        brokerList = new ArrayList<>();
        Broker broker1 = new Broker("tcp://192.168.37.140:1883", false);
        Broker broker2 = new Broker("tcp://192.168.37.140:1884", false);
        brokerList.add(broker1);
        brokerList.add(broker2);


        this.qos = 2;
        newsList = new NewsList();
        reconnecting = false;
        connected = false;
    }

    public void setId(String newId) {
        this.id = newId;
    }

    /**
     * Functie pentru generarea unui id
     *
     * @param caleFisier calea catre fisierul in care se va scrie id-ul generat
     * @return id-ul generat
     */
    public String generareID(String caleFisier) {
        // atentie la id negativ
        String timestamp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
        Random random = new Random();
        int randomNumber = random.nextInt(Integer.MAX_VALUE);

        //System.out.println(timestamp + "   " + randomNumber);
        // Se face hash la cele două variabile
        int rezultatHash = Math.abs(Objects.hash(timestamp, randomNumber));
        // se scrie in fisier id-ul
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(caleFisier, true))) {
            System.out.println("Hash pentru variabilele " + timestamp + " și " + randomNumber + ": " + rezultatHash);
            writer.write(String.valueOf(rezultatHash));
            return String.valueOf(rezultatHash);
            //System.out.println("Mesajul a fost scris cu succes în fișier.");
        } catch (IOException e) {
            System.err.println("Eroare la scrierea în fișier a id-ului: " + e.getMessage());
            return String.valueOf(0);
        }
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
                        this.writeToLogFile("Conectare cu SUCCES la broker-ul " + myBroker.getIpBroker() + " #############################");

                        // abonare automata pentru fiecare client
                        this.subscribe("stergere");
                        return; // iesim din bucla for atunci cand ne-am conectat la broker-ul curent
                    } catch (MqttException e) {
                        connected = false;
                        myBroker.setRunning(false);
                        this.writeToLogFile("Conectare cu SUCCES la broker-ul " + myBroker.getIpBroker());
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

    // se apeleaza atunci cand scriem in consola 20
    public void disconnectFromBroker() throws MqttException {
        mqttClient.disconnect();
        System.out.println("Deconectat de la broker\n");
    }


    public void subscribe(String topic) throws MqttException {
        this.mqttClient.subscribe(topic);
        subscribedTopics.add(topic);
        writeToLogFile("Abonare la topicul [" + topic + "]");
    }

    public void unsubscribe(String topic) throws MqttException {
        this.mqttClient.unsubscribe(topic);
        subscribedTopics.remove(topic);
        writeToLogFile("Dezabonare de la topicul [" + topic + "]");
    }

    private void renewSubscriptions() throws MqttException {
        for (String topic : subscribedTopics) {
            mqttClient.subscribe(topic);
        }
    }


    @Override
    public void connectionLost(Throwable cause) {
        connected = false;
        System.out.println("\nConexiune pierdută cu broker-ul\n");
        writeToLogFile("Conexiune pierdută cu broker-ul [" + this.broker + "]");
        startReconnectThread();
    }

    private void startReconnectThread() {
        Thread connectThread = new Thread(() -> {
            if (!connected) {
                while (true) {
                    // încercăm să ne conectăm la unul din brokeri
                    for (Broker myBroker : brokerList) {
                        try {
                            mqttClient = new MqttClient(myBroker.getIpBroker(), id, null);
                            mqttClient.setCallback(this);
                            MqttConnectOptions connOpts = new MqttConnectOptions();
                            connOpts.setCleanSession(true);

                            // Conectare la broker
                            System.out.println("Conectare la broker: " + myBroker.getIpBroker());
                            writeToLogFile("Se incearca reconectarea la broker-ul: " + this.broker );
                            mqttClient.connect(connOpts);
                            System.out.println("ID-ul acestui client este: " + id);
                            System.out.println("Conectare cu SUCCES!!!");
                            writeToLogFile("Conectare cu SUCCES la broker-ul: " + this.broker );
                            myBroker.setRunning(true);
                            this.broker = myBroker.getIpBroker();
                            connected = true;

                            // Reînnoiește abonările pe noul broker
                            renewSubscriptions();
                            return;
                        } catch (MqttException e) {
                            myBroker.setRunning(false);
                            connected = false;
                            System.out.println("Conectarea la broker-ul " + myBroker.getIpBroker() + " a eșuat");
                            System.out.println("Se încearcă conectarea la un alt broker\n");
                            writeToLogFile("Conectarea la broker-ul " + myBroker.getIpBroker() + " a eșuat");
                        }
                    }
                    // Așteptăm 5 secunde, după care încercăm o nouă rundă de conectare
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

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        //Procesează știrea într-un alt fir de execuție
        Thread messageArrivedThread = new Thread(() -> {
            String payload = new String(message.getPayload());
            processNews(payload);
        });
        messageArrivedThread.start();
    }

    public synchronized void processNews(String payload) {
        // Convertim JSON-ul în obiectul News
        News news = deserializeNews(payload);
        if (news.getTopic().equals("stergere")) {
            this.stergeStire(news);
        } else {
            // orice alta stire care are alt topic se va adauga in lista de stiri
            // Afișăm știrea pe ecran
            System.out.println("\nReceived News:");
            System.out.println("ID: " + news.getId());
            System.out.println("Title: " + news.getTitle());
            System.out.println("Content: " + news.getContent());
            System.out.println("Topic: " + news.getTopic());
            System.out.println();

            writeToLogFile("S-a primit o stire cu topicul [" + news.getTopic() + "]");
            newsList.addNews(news);
        }
    }

    /**
     * Functie care se apeleaza atunci cand primim o stire pe topicul "stergere", o astfel de stire
     * va avea la continut id-ul stirii care trebuie stearsa
     *
     * @param news
     */
    public void stergeStire(News news) {
        String idForDelete = news.getContent();
        int rezultat = newsList.deleteNewsById(idForDelete);//stergere din lista de stiri
        if (rezultat == 1) {
            System.out.println("\nStirea cu id-ul " + idForDelete + " a fost stearsa cu succes");
            writeToLogFile("\nStirea cu id-ul " + idForDelete + " a fost stearsa cu succes");

        } else {
            System.out.println("\nStirea cu id-ul " + idForDelete + " nu a fost gasita local");
            writeToLogFile("\nStirea cu id-ul " + idForDelete + " nu a fost gasita local");

        }
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
    public void addNewsMenu(Topics topicsList) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Introdu titlul stirii: ");
        String myTitle = scanner.nextLine();

        System.out.print("Introdu continutul stirii: ");
        String myContent = scanner.nextLine();

        String myTopic;
        do {
            System.out.print("Introdu topicul stirii: ");
            myTopic = scanner.nextLine();
        } while (!topicsList.existsTopic(myTopic));


        News myNews = new News(this.id, myTitle, myContent, myTopic);
        this.newsList.addNews(myNews);   // adaugarea stirii in lista
        publishNews(myNews);             // publicare stire
    }

    public void sendMultipleNews() throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Introdu topicul stirii: ");
        String myTopic = scanner.nextLine();

        List<News> newsList = Collections.synchronizedList(new ArrayList<>());


        for (int i = 0; i < 1000; i++) {
            // Creează o știre fictivă pentru a o trimite
            News news = new News(this.id, "Titlu " + i, "Conținut " + i, myTopic);
            newsList.add(news);
        }

        // Sortează știrile după idCounter
        // newsList.sort(Comparator.comparing(news -> Integer.parseInt(news.getId().split(":")[1])));

        for (News news : newsList) {
            try {
                publishNews(news).get();  // Așteaptă finalizarea publicării știrii
                //Thread.sleep(0);  // Se poate ajusta perioada de așteptare între știri, dacă este necesar
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        System.out.println("S-au trimis 1000 de știri pe topicul " + myTopic);
    }

    public void sendNewsToDeleteNews(News newsForDelete) {
        String idStireDeSters = newsForDelete.getId();
        String idParts[] = idStireDeSters.split(":");
        if (!idParts[0].equals(this.id)) {
            System.out.println("Aceasta stire nu poate fi stearsa, deoarece nu este scrisa de dumneavoastra!");
        } else {
            News newsForAll = new News(this.id, "Stergere stire", idStireDeSters, "stergere");
            publishNews(newsForAll);
        }
    }

    /**
     * Functie care publica o stire
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
            // processNews(payload);   daca vrei sa iti afiseze si tie stirea cand o trimiti chiar daca nu esti abonat
            System.out.println("Stire publicata cu succes " + i);
            i++;
            future.complete(null);
            writeToLogFile("Publicare stire cu topicul [" + topic + "]");
        } catch (MqttException e) {
            System.out.println("Stirea nu a putut fi publicata");
            writeToLogFile("Stirea cu topicul [" + topic + "] nu a putut fi publicata." + e.getMessage());
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
                                "3. Dezabonare de la un topic\n" +
                                "4. Vizualizare lista stiri\n" +
                                "5. Vizualizare detalii stire\n" +
                                "6. Adauga stire\n" +
                                "7. Adauga 1000 de stiri\n" +
                                "8. Stergere stire\n" +
                                "20. Exit\n"
                );
                System.out.print("Optiunea mea este: ");
                int choice = scanner.nextInt();
                switch (choice) {

                    case 1:// Afiseaza toate topicurile
                        topics.printAllTopics();
                        break;

                    case 2:// Abonare la un topic
                        System.out.print("Introdu numele topicului la care doresti sa te abonezi(ex:crypto):");
                        String newTopic = scanner.next();
                        if (topics.existsTopic(newTopic)) {
                            System.out.print("Abonare cu succes\n");
                            c.subscribe(newTopic);
                        } else {
                            System.out.print("Nu exista acest topic\n");
                        }
                        break;

                    case 3:// Dezabonare de la un topic
                        System.out.print("Introdu numele topicului la care doresti sa te dezabonezi(ex:crypto):");
                        String myTopic = scanner.next();
                        if (topics.existsTopic(myTopic)) {
                            c.unsubscribe(myTopic);
                            System.out.print("Dezabonare cu succes\n");
                        } else {
                            System.out.print("Nu exista acest topic\n");
                        }
                        break;

                    case 4://Vizualizare lista stiri
                        System.out.println("Lista de stiri:");
                        this.newsList.printAllNews();
                        break;

                    case 5://Vizualizare detalii stire
                        System.out.print("Introdu indexul stirii:");
                        String myStringIndex = scanner.next();
                        int index = Integer.parseInt(myStringIndex);
                        newsList.printNewsWithIndex(index);
                        break;

                    case 6:// adauga stire
                        addNewsMenu(topics);
                        break;

                    case 7:// adauga 1000 de stiri
                        sendMultipleNews();
                        break;

                    case 8:// stergere stire
                        System.out.print("Introdu indexul stirii pe care doresti sa il stergi:");
                        String myStringIndex2 = scanner.next();
                        int index2 = Integer.parseInt(myStringIndex2);
                        News newsForDelete = newsList.getNewsWithIndex(index2);// identificare stire dupa index
                        c.sendNewsToDeleteNews(newsForDelete);
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

    /**
     * Functie pentru gestiunea fisierului "id.txt"
     */
    public void manageIdFile() {
        String caleFisier = "./src/main/java/org/example/id.txt";
        File fisier = new File(caleFisier);
        try {
            // Verifica dacă fișierul există
            if (!fisier.exists()) {
                // Încearcă să creeze fișierul
                if (fisier.createNewFile()) {
                    // se intra in acest if atunci cand se porneste programul prima data pe o anumita masina
                    System.out.println("Fișierul id.txt a fost creat cu succes.");
                    String idGenerat = this.generareID(caleFisier);
                    this.setId(idGenerat);
                } else {
                    System.out.println("Nu s-a putut crea fișierul id.txt.");
                }
            } else {
                System.out.println("Fișierul id.txt există deja.");
                if (fisier.length() > 0) {
                    // verificam daca exista ceva scris in fisier
                    System.out.println("Fișierul id.txt nu este gol.");
                    System.out.println("Fișierul id.txt contine urmatoarele linii:");

                    FileReader fileReader = new FileReader(fisier);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);

                    // Citirea și afișarea conținutului fișierului
                    String linie;
                    int contorLinie = 0;
                    while ((linie = bufferedReader.readLine()) != null) {
                        System.out.println(linie);
                        if (contorLinie == 0) {
                            this.setId(linie);
                        }
                        contorLinie++;
                        // atentie la mai multe linii
                    }

                    // Închiderea fluxului de citire
                    bufferedReader.close();

                } else {
                    // fisierul este gol
                    System.out.println("Fișierul este gol");
                    // se genereaza id-ul
                    String idGenerat = this.generareID(caleFisier);
                    this.setId(idGenerat);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
