package papaya.geoecho.Model;

import java.io.Serializable;

/**
 * Created by Dani on 04/03/2018.
 */

public class LoginApp implements Serializable {

    private String user;
    private String pass;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

}
