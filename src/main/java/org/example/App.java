package org.example;


import org.example.Client.Client;
import org.example.Client.Topics;

public class App
{
    public static void main(String[] args){
        Client c = new Client();
        c.manageIdFile();

        Topics topics = new Topics();
        topics.addNewTopic("stergere");
        topics.addNewTopic("test");
        topics.addNewTopic("crypto");
        topics.addNewTopic("ai");
        topics.addNewTopic("blockchain");
        topics.addNewTopic("vremea");



        c.connectToBroker3();
        //c.writeToLogFile("Acesta este un mesaj de test");
        c.startUserInputThread(c, topics);
    }
}
