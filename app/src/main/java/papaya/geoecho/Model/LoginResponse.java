package papaya.geoecho.Model;

import java.io.Serializable;

/**
 * Created by Dani on 04/03/2018.
 */

public class LoginResponse implements Serializable {

    private boolean login;
    private String ID;

    public boolean isLogin() {
        return login;
    }

    public void setLogin(boolean login) {
        this.login = login;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

}