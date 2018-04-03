package model.client;

/**
 * Created by Dani on 03/04/2018.
 */

import android.graphics.PointF;

import java.util.Date;

/**
 * Classe Message que conté el model del missatge del sistema
 * @author Dani Machado
 */
public class Message extends Packet {

    protected PointF coordinates;
    protected String text;
    protected String photoBase64;
    protected String userSender;
    protected String userReceiver;
    protected Date date;
    protected int life;
    protected boolean msgPublic;
    protected boolean msgVisible;
    protected boolean msgReaded;

    /**
     * Constructor per defecte
     */
    public Message() {
    }

    /**
     * Constructor principal
     * @param coordinates
     * @param text
     * @param photoBase64
     * @param userSender
     * @param userReceiver
     * @param date
     * @param life
     * @param msgPublic
     * @param msgVisible
     * @param msgReaded
     */
    public Message(PointF coordinates, String text, String photoBase64, String userSender, String userReceiver, Date date, int life, boolean msgPublic, boolean msgVisible, boolean msgReaded) {
        this.coordinates = coordinates;
        this.text = text;
        this.photoBase64 = photoBase64;
        this.userSender = userSender;
        this.userReceiver = userReceiver;
        this.date = date;
        this.life = life;
        this.msgPublic = msgPublic;
        this.msgVisible = msgVisible;
        this.msgReaded = msgReaded;
    }

    /**
     * Getter coordinates
     * @return
     */
    public PointF getCoordinates() {
        return coordinates;
    }

    /**
     * Setter coordinates
     * @param coordinates
     */
    public void setCoordinates(PointF coordinates) {
        this.coordinates = coordinates;
    }

    /**
     * Getter text
     * @return
     */
    public String getText() {
        return text;
    }

    /**
     * Setter text
     * @param text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Getter photo
     * @return
     */
    public String getPhotoBase64() {
        return photoBase64;
    }

    /**
     * Setter photo
     * @param photoBase64
     */
    public void setPhotoBase64(String photoBase64) {
        this.photoBase64 = photoBase64;
    }

    /**
     * Getter userSender
     * @return
     */
    public String getUserSender() {
        return userSender;
    }

    /**
     * Setter userSender
     * @param userSender
     */
    public void setUserSender(String userSender) {
        this.userSender = userSender;
    }

    /**
     * Getter userReceiver
     * @return
     */
    public String getUserReceiver() {
        return userReceiver;
    }

    /**
     * Setter userReceiver
     * @param userReceiver
     */
    public void setUserReceiver(String userReceiver) {
        this.userReceiver = userReceiver;
    }

    /**
     * Getter date
     * @return
     */
    public Date getDate() {
        return date;
    }

    /**
     * Setter date
     * @param date
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Getter life
     * @return
     */
    public int getLife() {
        return life;
    }

    /**
     * Setter life
     * @param life
     */
    public void setLife(int life) {
        this.life = life;
    }

    /**
     * Getter msgPublic
     * @return
     */
    public boolean isMsgPublic() {
        return msgPublic;
    }

    /**
     * Setter msgPublic
     * @param msgPublic
     */
    public void setMsgPublic(boolean msgPublic) {
        this.msgPublic = msgPublic;
    }

    /**
     * Getter msgVivible
     * @return
     */
    public boolean isMsgVisible() {
        return msgVisible;
    }

    /**
     * Setter msgVisible
     * @param msgVisible
     */
    public void setMsgVisible(boolean msgVisible) {
        this.msgVisible = msgVisible;
    }

    /**
     * Getter msgReader
     * @return
     */
    public boolean isMsgReaded() {
        return msgReaded;
    }

    /**
     * Setter msgReader
     * @param msgReaded
     */
    public void setMsgReaded(boolean msgReaded) {
        this.msgReaded = msgReaded;
    }

}