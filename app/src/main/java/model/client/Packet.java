package model.client;

import java.io.Serializable;

/**
 * Created by Dani on 05/03/2018.
 */

public abstract class Packet implements Serializable {
    private int sessionID;

    public int getSessionID() {
        return sessionID;
    }

    public void setSessionID(int sessionID) {
        this.sessionID = sessionID;
    }
}