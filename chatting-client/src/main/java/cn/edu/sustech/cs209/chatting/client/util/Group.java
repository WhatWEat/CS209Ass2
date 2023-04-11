package cn.edu.sustech.cs209.chatting.client.util;

import cn.edu.sustech.cs209.chatting.common.Message;
import java.io.Serializable;
import java.util.ArrayList;

public class Group implements Serializable {
    ArrayList<String> groupMember = new ArrayList<>();
    ArrayList<Message> history = new ArrayList<>();
    public Group(ArrayList<String> groupMember) {
        this.groupMember = groupMember;
    }
    public void addMessage(Message msg){
        if(!msg.getSentBy().equals("Server")) history.add(msg);
    }
    public ArrayList<String> getGroupMember() {
        return groupMember;
    }
    public ArrayList<Message> getHistory() {
        return history;
    }
}
