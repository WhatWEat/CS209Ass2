package cn.edu.sustech.cs209.chatting.client.util;

import cn.edu.sustech.cs209.chatting.client.view.Controller;
import cn.edu.sustech.cs209.chatting.common.Message;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class Group implements Serializable {
    ArrayList<String> groupMember = new ArrayList<>();
    ArrayList<Message> history = new ArrayList<>();
//    Controller con = null;
    public Group(ArrayList<String> groupMember) {
        groupMember.sort(Comparator.naturalOrder());
        this.groupMember = groupMember;
//        this.con = con;
    }
    public void addMessage(Message msg){
        if(!msg.getSentBy().equals("Server")) history.add(msg);
    }
    public boolean isEqual(ArrayList<String> memberList) {
        //judge if the group is the same as the memberList
        memberList.sort(Comparator.naturalOrder());
        return Objects.equals(groupMember, memberList);
    }
    public ArrayList<String> getGroupMember() {
        return groupMember;
    }
    public ArrayList<Message> getHistory() {
        return history;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return Objects.equals(groupMember, group.groupMember);
    }
    @Override
    public int hashCode() {
        return Objects.hash(groupMember);
    }

    @Override
    public String toString() {
        return "Group{" + groupMember +
            '}';
    }
}
