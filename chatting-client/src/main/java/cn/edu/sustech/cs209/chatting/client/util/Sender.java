package cn.edu.sustech.cs209.chatting.client.util;

import cn.edu.sustech.cs209.chatting.client.Main;
import cn.edu.sustech.cs209.chatting.client.view.*;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/* TODO Exception
    server crash: in login stage, in userlist stage
    client down: other client can receive the message
    History:
* */
public class Sender implements Runnable {

  public static final int port = 25565;
  private final String username;
  private final String password;
  private static ObjectInputStream in;
  private static ObjectOutputStream out;
  private static Socket socket;
  private boolean register = false;
  private static UserlistController con;

  public Sender(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public Sender(String username, String password, boolean register) {
    this.username = username;
    this.password = password;
    this.register = register;
  }

  @Override
  public void run() {
    try {
      socket = new Socket("localhost", port);
      out = new ObjectOutputStream(socket.getOutputStream());
      in = new ObjectInputStream(socket.getInputStream());

      if (register) {
        toRegister();
        register = false;
      } else {
        connect();
      }
      while (socket.isConnected()) {
        Message msg = (Message) in.readObject();
        if (msg != null) {
          switch (msg.getType()) {
            case file:
            case chat:
              Platform.runLater(() -> {
                con.addMessage(msg);
              });
              break;
            case createGroup:
              Platform.runLater(() -> {
                try {
                  con.createChat(msg.getSendTo());
                } catch (IOException e) {
                  System.out.println("创建群聊失败");
                }
              });
              break;
            case online:
              Platform.runLater(() -> {
                String online = msg.getSentBy();
                ArrayList<String> sendTo = msg.getSendTo();
                for (String i : sendTo) {
                  System.out.println("添加上号:" + i);
                  con.addOnline(new User(i));
                }
                Notification notification = new Notification(MessageType.online);
                notification.setContent("用户" + online + "上线了", "点击用户名即可聊天");
                System.out.println("online");
              });
              break;
            case disconnect:
              String num = msg.getData();
              Platform.runLater(() -> {
                if (con != null) {
                  con.currentOnlineCnt.setText("Online: " + num);
                  String toDis = msg.getSendTo().get(0);
                  if (!toDis.equals(username) && !toDis.equals("ALL")) {
                    System.out.println("当前离线" + username + toDis);
                    con.disconnect(new User(toDis));
                  }

                }
              });
              break;
            case connect:
              String data = msg.getData();
              if (data.equals("true")) {
                Platform.runLater(() -> {
                  Stage now = Main.getPrimaryStage();
                  UserlistController.thisuser = new User(username);
                  FileOperator.readUserList();
                  FXMLLoader fxmlLoader = new FXMLLoader(
                      getClass().getResource("../view/userlist.fxml"));
                  try {
                    now.setScene(new Scene(fxmlLoader.load()));
                  } catch (IOException e) {
                    System.out.println("无法加载userlist.fxml");
                  }
                  con = fxmlLoader.getController();
                  now.show();
                  con.getOnline(username);
                  System.out.println("connection");
                });
              } else if (data.equals("same")) {
                Platform.runLater(() -> {
                  createAlert("登录失败", "该用户已经在线，\n请确认后再试");
                });
              } else {
                Platform.runLater(() -> {
                  createAlert("登录失败", "错误的用户名或密码，\n请确认后再试");
                });
              }
              break;
            case register:
              if (msg.getData().equals("same")) {
                Platform.runLater(() -> {
                  createAlert("注册失败", "该用户名已经被占用，\n请尝试使用其他用户名");
                });
              } else if (msg.getData().equals("null")) {
                Platform.runLater(() -> {
                  createAlert("注册失败", "请不要使用空用户名或密码");
                });
              } else {
                Platform.runLater(() -> {
                  createAlert("注册成功", String.format("用户名:%s\n密码:%s", username, password));
                });
              }
              break;
          }
        }
      }
    } catch (SocketException e) {
      Platform.runLater(() -> {
          if (con == null) {
              createAlert("连接失败", "服务器未启动关闭");
          }
      });
      System.out.println("Socket已经关闭");
    } catch (IOException | ClassNotFoundException e) {
      System.out.println("Sender异常,没有对应类型的消息");
      //throw new RuntimeException(e);
    } finally {
      try {
        close();
      } catch (IOException e) {
        Platform.runLater(() -> {
          FileOperator.saveUserList();
          UserlistController.stages.values().forEach(Stage::close);
          Notification notification = new Notification(MessageType.close);
          Main.getPrimaryStage().close();
        });
        //throw new RuntimeException(e);
      }
    }
  }

  public static void createAlert(String title, String content) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setContentText(content);
    DialogPane dialogPane = alert.getDialogPane();
    dialogPane.setHeaderText("");
    dialogPane.setGraphic(null);
    alert.showAndWait();
  }

  public static void send(Message msg) throws IOException {
    out.writeObject(msg);
    out.flush();
  }

  void toRegister() throws IOException {
    Message pw = new Message(0L, username, "Server", password, MessageType.register);
    out.writeObject(pw);
  }

  void connect() throws IOException {
    Message pw = new Message(0L, username, "Server", password, MessageType.connect);
    out.writeObject(pw);
    out.flush();
  }

  public static void close() throws IOException {
    if (con != null && !socket.isClosed()) {
      System.out.println("给server发下号信息");
      send(
          new Message(0L, con.thisuser.getUsername(), "Server", "c", MessageType.disconnect));
    }
    if (in != null) {
      in.close();
    }
    if (out != null) {
      out.close();
    }
    if (socket != null) {
      socket.close();
    }
    for (Stage i : UserlistController.stages.values()) {
      Platform.runLater(i::close);
    }
    System.out.println("close the client");
  }
}
