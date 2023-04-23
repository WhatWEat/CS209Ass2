package cn.edu.sustech.cs209.chatting.client.util;

import cn.edu.sustech.cs209.chatting.client.view.UserlistController;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class User implements Serializable {

  String picture = "default";
  String username;
  long lastMessage = 0L;
  boolean online = false;
  public static Comparator<User> userComparator = (e1, e2) -> {
      if (e1.equals(UserlistController.thisuser)) {
          return -1;
      } else if (e2.equals(UserlistController.thisuser)) {
          return 1;
      } else {
          return Long.compare(e1.getLastMessage(), e2.getLastMessage());
      }
  };

  public User(String username) {
    this.username = username;
    this.online = true;
  }

  public String getUsername() {
    return username;
  }

  public String getPicture() {
    return picture;
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

  public void setLastMessage(long lastMessage) {
    this.lastMessage = lastMessage;
  }

  public long getLastMessage() {
    return lastMessage;
  }

  @Override
  public int hashCode() {
    return Objects.hash(username);
  }

}
