package cn.edu.sustech.cs209.chatting.server.util;

import cn.edu.sustech.cs209.chatting.client.util.User;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class UserServer implements Runnable {

  private final Socket s;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private String username;
  static private HashMap<String, String> userList = new HashMap<>();
  /*use to save the user and password*/
  static private HashMap<String, ObjectOutputStream> outList = new HashMap<>();

  /*save the username corresponding socket*/
  public UserServer(Socket s, HashMap<String, String> userList) {
    this.s = s;
    if (UserServer.userList.isEmpty()) {
      UserServer.userList = userList;
    }
  }

  @Override
  public void run() {
    try {
      in = new ObjectInputStream(s.getInputStream());
      out = new ObjectOutputStream(s.getOutputStream());
      Message msg = (Message) in.readObject();
      checkPassword(msg);
      while (s.isConnected() && !s.isClosed()) {
        msg = (Message) in.readObject();
        if (msg != null) {
          switch (msg.getType()) {
            case chat:
            case file:
            case createGroup:
              sendGroup(msg);
              break;
            case disconnect:
              close();
              break;
          }
        }
      }
    } catch (IOException | ClassNotFoundException e) {
      System.out.println("用户强制下线");
      throw new RuntimeException(e);
    } finally {
      try {
        close();
      } catch (IOException e) {
        System.out.println("该socket已经关闭");
//                throw new RuntimeException(e);
      }
    }
  }

  synchronized void checkPassword(Message msg) throws IOException {
    String user = msg.getSentBy();
    String password = msg.getData();
    switch (msg.getType()) {
      case connect:
        if (outList.containsKey(user)) {
          send(user, "same", MessageType.connect);
          System.out.println("same user");
        } else if (userList.containsKey(user) && userList.get(user).equals(password)) {
          send(user, "true", MessageType.connect);
          username = msg.getSentBy();
          outList.put(username, out);
          ArrayList<String> userList = new ArrayList<>(outList.keySet());
          sendALL(new Message(0L, "Server", userList, user, MessageType.online));
          sendALL(new Message(0L, "Server", "ALL", String.valueOf(outList.size()),
              MessageType.disconnect));
          System.out.println("right answer 在线人数" + outList.size() + " 上号:" + user);
        } else {
          send(user, "false", MessageType.connect);
          System.out.println("Wrong answer");
        }
        break;
      case register:
        if (userList.containsKey(user)) {
          send(user, "same", MessageType.register);
        } else if (password.equals("")) {
          send(user, "null", MessageType.register);
        } else {
          userList.put(user, password);
          send(user, "success", MessageType.register);
          System.out.println("Register");
          savePassword(userList);
        }
        break;
    }
  }

  static void savePassword(HashMap<String, String> userList) {
    try (FileOutputStream fileOut = new FileOutputStream("pw.map");
        ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
      out.writeObject(userList);
      System.out.println("HashMap saved to " + "pw.map");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  void send(String username, String message, MessageType type) throws IOException {
    Message back = new Message(0L, "Server", username, message, type);
    out.writeObject(back);
    out.flush();
  }

  void sendALL(Message msg) {
    if (msg.getType() == MessageType.online) {
      for (String s1 : outList.keySet()) {
        msg.getSendTo().add(s1);
      }
      System.out.println(msg.getSentBy().toString() + "的上号信息");
    }
    System.out.println("给全体发送了信号");
    outList.values().forEach(value -> {
      try {
        value.writeObject(msg);
        value.flush();
      } catch (IOException e) {
        System.err.println(value + "该用户已经下号了");
        //throw new RuntimeException(e);
      }
    });
  }

  void sendGroup(Message msg) {
    ArrayList<String> sendTo = msg.getSendTo();
    for (String i : sendTo) {
      try {
        switch (msg.getType()) {
          case file:
            System.out.println(msg.getSentBy() + "发送了文件" + msg.getData().substring(0, 10));
            break;
          default:
            System.out.println(msg.getSentBy() + "发送了群组消息" + msg.getData());
            break;
        }
        ObjectOutputStream out = outList.get(i);
        if (out != null) {
          out.writeObject(msg);
        } else {
          outList.remove(i);
        }
      } catch (IOException e) {
        System.out.println(i + "该用户已经下号了");
        throw new RuntimeException(e);
      }
    }
  }

  void close() throws IOException {
    outList.remove(username);
    System.out.println("发送下号信息");
    sendALL(new Message(0L, "Server", username, String.valueOf(outList.size()),
        MessageType.disconnect));
      if (in != null) {
          in.close();
      }
      if (out != null) {
          out.close();
      }
      if (s != null || !s.isClosed()) {
          s.close();
      }
  }
}
