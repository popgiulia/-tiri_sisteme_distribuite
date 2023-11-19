package org.example.Client;

import java.util.ArrayList;

public class NewsList {
    private ArrayList<News> newsList;

    public NewsList() {
        this.newsList = new ArrayList<>();
    }

    public void addNews(News myNews) {
        newsList.add(myNews);
    }

    public void getAllNewsByTopic(String topic) {
        for (News news : newsList) {
            if (news.getTopic().equals(topic)) {
                System.out.println("[" + news.getId() + "] " + news.getTitle() + " " + news.getTopic());
            }
        }
    }

    public void printAllNews() {
        int i = 1;
        for (News news : newsList) {
            System.out.println(i+". " + "["+news.getId() + "]. " + news.getTitle()+ " " + news.getTopic());
            i++;
        }
    }
    public void printNewsWithIndex(int index){
        int i = 0;
        for (News news : newsList) {
            if(i == index-1){
                System.out.println("############################################");
                System.out.println("Index: " + index+". " + "["+news.getId() + "]. " + news.getTitle()+ " " + news.getTopic());
                System.out.println("ID:["+news.getId() + "]");
                System.out.println("Topic: " + news.getTopic());
                System.out.println("Continut: " + news.getContent());
                System.out.println("############################################");
                break;
            }
            i++;
        }
    }
}