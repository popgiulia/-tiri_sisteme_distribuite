package org.example.Client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class News {
    static int contor = 0;
    private String id;  // se genereaza automat in constructor
    private String title;
    private String content;
    private String topic;

    public News( String authorID, String title, String content, String topic) {
        this.id = authorID;
        this.id = this.id +":"+ contor;       // id-ul va avea forma "idAutor:123"
        contor++;
        this.title = title;
        this.content = content;
        this.topic = topic;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
    public String toJson() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
    }
}
