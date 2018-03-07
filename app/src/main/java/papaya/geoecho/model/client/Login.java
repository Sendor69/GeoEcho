package papaya.geoecho.model.client;

/**
 * Created by Dani on 07/03/2018.
 */

public abstract class Login extends Packet{

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
