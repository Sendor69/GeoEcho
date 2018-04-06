package model.client;

import java.io.Serializable;

/**
 * Created by Dani on 05/03/2018.
 */

public abstract class Packet implements Serializable{

    private int sessionID;

    /**
     * Getter del id de sessió
     * @return Retorna el id de la sessió
     */
    public int getSessionID() {
        return sessionID;
    }

    /**
     * Setter del id de sessió
     * @param sessionID Id de la sessió
     */
    public void setSessionID(int sessionID) {
        this.sessionID = sessionID;
    }

}