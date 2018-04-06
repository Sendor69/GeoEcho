package model.client;

/**
 * Created by Dani on 07/03/2018.
 */

public abstract class Login extends Packet{

    private String user;
    private String pass;

    /**
     * Getter del nom d'usuari
     * @return Retorna el nom d'usuari
     */
    public String getUser() {
        return user;
    }

    /**
     * Setter del nom d'usuari
     * @param user Nom de l'usuari
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Getter del password
     * @return Retorn el password
     */
    public String getPass() {
        return pass;
    }

    /**
     * Setter del password
     * @param pass Password
     */
    public void setPass(String pass) {
        this.pass = pass;
    }

}
