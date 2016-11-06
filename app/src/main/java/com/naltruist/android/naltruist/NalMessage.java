package com.naltruist.android.naltruist;

/**
 * Created by viveknarra on 10/29/16.
 */

public class NalMessage {
    private String id;
    private String text;
    private String name;
    private String photoUrl;

    public NalMessage() {

    }

    public NalMessage(String text, String name, String photoUrl) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
