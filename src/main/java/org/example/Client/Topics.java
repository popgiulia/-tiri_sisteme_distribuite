package org.example.Client;

import java.util.ArrayList;
import java.util.List;


public class Topics {
    private List<String> topicsList; //

    public Topics() {
        this.topicsList = new ArrayList<>();
    }

    public boolean existsTopic(String myTopic) {
        return topicsList.contains(myTopic);
    }

    public void clearAllTopics() {
        topicsList.clear();
    }

    public void printAllTopics() {
        int i = 1;
        System.out.println("Topic-urile disponibile sunt următoarele:");
        for (String topic : topicsList) {
            System.out.println(i + ". " + topic);
            i++;
        }
        System.out.println("\n");
    }

    public void deleteTopic(String topic) {
        if (!existsTopic(topic)) {
            System.out.println("Topicul " + topic + " nu exista");
        } else {
            topicsList.remove(topic);
        }
    }

    public void addNewTopic(String newTopic) {
        topicsList.add(newTopic);
    }
}
