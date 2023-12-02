package org.example.Client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.concurrent.atomic.AtomicInteger;

public class News implements Comparable<News>{
    private static final AtomicInteger idCounter = new AtomicInteger(0);

    private String id;
    private String title;
    private String content;
    private String topic;

    public News(String authorID, String title, String content, String topic) {
        this.id = authorID + ":" + idCounter.getAndIncrement();
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

    public int compareTo(News other) {
        int thisIdCounter = Integer.parseInt(this.id.split(":")[1]);
        int otherIdCounter = Integer.parseInt(other.id.split(":")[1]);
        return Integer.compare(thisIdCounter, otherIdCounter);
    }
}
