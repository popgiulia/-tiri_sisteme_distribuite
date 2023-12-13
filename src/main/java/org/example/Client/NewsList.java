package org.example.Client;

import java.util.ArrayList;
import java.util.Comparator;

public class NewsList {
    private ArrayList<News> newsList;

    public NewsList() {
        this.newsList = new ArrayList<>();
    }

    public void addNews(News myNews) {
        newsList.add(myNews);
    }
    public int deleteNewsById(String id){
        for(News news: newsList){
            if(news.getId().equals(id)){
                newsList.remove(news);
                return 1; // stire stearsa cu succes
            }
        }
        return 0; // stirea cu acest topic nu exista
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
            System.out.println(i+". " + "["+news.getId() + "]. " +  ", Titlu:" +news.getTitle()+ " " + ", Topic:" + news.getTopic());
            i++;
        }
    }
    public void printNewsWithIndex(int index){
        int i = 0;
        for (News news : newsList) {
            if(i == index-1){
                System.out.println("############################################");
                System.out.println("Index: " + index);
                System.out.println("ID:["+news.getId() + "]");
                System.out.println("Titlu: "+news.getTitle());
                System.out.println("Topic: " + news.getTopic());
                System.out.println("Continut: " + news.getContent());// de facut: sa nu se afiseze mai mult de x caractere pe un rand
                System.out.println("############################################");
                break;
            }
            i++;
        }
    }
    public News getNewsWithIndex(int index){
        int i = 0;
        for (News news : newsList) {
            if(i == index-1){
                return news;
            }
            i++;
        }
        return null;
    }
}
