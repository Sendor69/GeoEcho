package model.client;

/**
 * Created by Dani on 06/03/2018.
 */

public class RegisterApp extends Packet {
    private String user;
    private String pass;
    private String mail;

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

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }
}