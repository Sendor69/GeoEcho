package model.client;

import java.util.List;

/**
 * Created by Dani on 05/04/2018.
 */

public abstract class ResponseQuery extends Response{

    private List<Message> messageList;

    /**
     * Getter de messageList
     * @return Retorna la llista de missatges
     */
    public List<Message> getMessageList() {
        return messageList;
    }

    /**
     * Setter de messageList
     * @param messageList Llista de missatges
     */
    public void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
    }

}
