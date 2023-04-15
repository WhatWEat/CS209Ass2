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
                        case online:
                            Platform.runLater(() -> {
                                for (String i : msg.getSendTo()) {
                                    if (i.equals("ALL") || i.equals(username)) {
                                    } else {
                                        System.out.println("添加" + i);
                                        con.addOnline(new User(i));
                                    }
                                }
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
                        case chat:

                            break;
                        case connect:
                            String data = msg.getData();
                            if (data.equals("true")) {
                                Platform.runLater(() -> {
                                    Stage now = Main.getPrimaryStage();
                                    FXMLLoader fxmlLoader = new FXMLLoader(
                                        getClass().getResource("../view/userlist.fxml"));
                                    try {
                                        now.setScene(new Scene(fxmlLoader.load()));
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                    con = fxmlLoader.getController();
                                    now.show();
                                    con.getOnline(username);
                                });
                            } else if (data.equals("same")) {
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("登录失败");
                                    alert.setContentText("该用户已经在线，\n请确认后再试");
                                    DialogPane dialogPane = alert.getDialogPane();
                                    dialogPane.setHeaderText("");
                                    dialogPane.setGraphic(null);
                                    alert.showAndWait();
                                });
                            } else {
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("登录失败");
                                    alert.setContentText("错误的用户名或密码，\n请确认后再试");
                                    DialogPane dialogPane = alert.getDialogPane();
                                    dialogPane.setHeaderText("");
                                    dialogPane.setGraphic(null);
                                    alert.showAndWait();
                                });
                            }
                            break;
                        case register:
                            if (msg.getData().equals("same")) {
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("注册失败");
                                    alert.setContentText(
                                        "该用户名已经被占用，\n请尝试使用其他用户名");
                                    DialogPane dialogPane = alert.getDialogPane();
                                    dialogPane.setHeaderText("");
                                    dialogPane.setGraphic(null);
                                    alert.showAndWait();
                                });
                            } else if (msg.getData().equals("null")) {
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("注册失败");
                                    alert.setContentText("请不要使用空用户名或密码");
                                    DialogPane dialogPane = alert.getDialogPane();
                                    dialogPane.setHeaderText("");
                                    dialogPane.setGraphic(null);
                                    alert.showAndWait();
                                });
                            } else {
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("注册成功");
                                    alert.setContentText(
                                        String.format("用户名:%s\n密码:%s", username, password));
                                    DialogPane dialogPane = alert.getDialogPane();
                                    dialogPane.setHeaderText("");
                                    dialogPane.setGraphic(null);
                                    alert.showAndWait();
                                });
                            }
                            break;
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("Socket已经关闭");
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    static void send(Message msg) throws IOException {
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
        System.out.println("close the client");
    }
}
