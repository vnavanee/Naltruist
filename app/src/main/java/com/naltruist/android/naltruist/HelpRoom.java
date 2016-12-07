package com.naltruist.android.naltruist;

import android.location.Location;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by viveknarra on 11/6/16.
 */

public class HelpRoom {

    private String helprequestor;
    private String title;
    private String location;



    public HelpRoom() {

    }

    public HelpRoom(String helprequestor, String title, String location) {
        this.helprequestor = helprequestor;
        this.title = title;
        this.location = location;

    }

    public String getHelprequestor() {
        return helprequestor;
    }

    public void setHelprequestor(String helprequestor) {
        this.helprequestor = helprequestor;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Exclude
    public Map<String, Object> roomMap() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("helprequestor", helprequestor);
        result.put("location", location);
        result.put("title", title);


        return result;
    }

}
