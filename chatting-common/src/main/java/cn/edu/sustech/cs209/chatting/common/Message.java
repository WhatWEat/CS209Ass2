package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable {

    private Long timestamp;

    private String sentBy;

    private ArrayList<String> sendTo = new ArrayList<>();

    private String data;
    private MessageType type;

    public Message(Long timestamp, String sentBy, String sendTo, String data,MessageType type) {
        this.timestamp = timestamp;
        this.sentBy = sentBy;
        this.sendTo.add(sendTo);
        this.data = data;
        this.type = type;
    }

    public Message(Long timestamp, String sentBy, ArrayList<String> sendTo, String data,
        MessageType type) {
        this.timestamp = timestamp;
        this.sentBy = sentBy;
        this.sendTo = sendTo;
        this.data = data;
        this.type = type;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getSentBy() {
        return sentBy;
    }

    public ArrayList<String> getSendTo() {
        return sendTo;
    }

    public String getData() {
        return data;
    }
    public MessageType getType() {
        return type;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }


    public void setData(String data) {
        this.data = data;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
}
