package com.naltruist.android.naltruist;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by viveknarra on 11/6/16.
 */

public class HelpRoom {

    private String helprequestor;
    private String firstmessage;



    public HelpRoom() {

    }

    public HelpRoom(String helprequestor, String firstmessage) {
        this.helprequestor = helprequestor;
        this.firstmessage=firstmessage;


    }

    public String getHelprequestor() {
        return helprequestor;
    }
    public String getFirstmessage() {
        return firstmessage;
    }

    public void setuid(String helprequestor) {
        this.helprequestor = helprequestor;
    }

    public void setMessage(String message) {
        this.firstmessage = message;
    }

    @Exclude
    public Map<String, Object> roomMap() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("helprequestor", helprequestor);
        result.put("firstmessage", firstmessage);

        return result;
    }

}
