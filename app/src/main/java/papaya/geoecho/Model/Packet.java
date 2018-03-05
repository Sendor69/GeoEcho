package papaya.geoecho.Model;

import java.io.Serializable;

/**
 * Created by Dani on 05/03/2018.
 */

public abstract class Packet implements Serializable {
    private String sessionID;

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }
}