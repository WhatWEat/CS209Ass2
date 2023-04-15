package cn.edu.sustech.cs209.chatting.client.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class User implements Serializable {
    String picture = "default";
    String username;

    boolean online = false;
    ArrayList<Group> groups = new ArrayList<>();

    public User(String username) {
        this.username = username;
        this.online = true;
    }

    public User(String username, ArrayList<Group> groups) {
        this.username = username;
        this.groups = groups;
    }

    public String getUsername() {
        return username;
    }

    public String getPicture() {
        return picture;
    }

    public ArrayList<Group> getGroups() {
        return groups;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
