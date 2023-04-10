package cn.edu.sustech.cs209.chatting.client.util;

import cn.edu.sustech.cs209.chatting.client.Main;
import cn.edu.sustech.cs209.chatting.client.view.*;
import java.io.IOException;
import cn.edu.sustech.cs209.chatting.common.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;

public class Sender implements Runnable{
    public static final int port = 25565;
    private String username;
    private String password;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket socket;
    private boolean register = false;
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
            socket = new Socket("localhost",port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            if(register) {
                toRegister();
                register = false;
            }else {
                connect();
            }
            while (socket.isConnected()){
                Message msg = (Message) in.readObject();
                if(msg != null){
                    switch (msg.getType()){
                        case connect:
                            if(msg.getData().equals("true")){
                                Platform.runLater(()->{
                                    Stage now = Main.getPrimaryStage();
                                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../view/main.fxml"));
                                    try {
                                        now.setScene(new Scene(fxmlLoader.load()));
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                    now.setTitle("Chatting Client");
                                    now.show();
                                });
                            } else {
                                Platform.runLater(()->{
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
                            if(msg.getData().equals("same")) {
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("注册失败");
                                    alert.setContentText("该用户名已经被占用，\n请尝试使用其他用户名");
                                    DialogPane dialogPane = alert.getDialogPane();
                                    dialogPane.setHeaderText("");
                                    dialogPane.setGraphic(null);
                                    alert.showAndWait();
                                });
                            } else if(msg.getData().equals("null")){
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("注册失败");
                                    alert.setContentText("请不要使用空用户名或密码");
                                    DialogPane dialogPane = alert.getDialogPane();
                                    dialogPane.setHeaderText("");
                                    dialogPane.setGraphic(null);
                                    alert.showAndWait();
                                });
                            }
                            else {
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("注册成功");
                                    alert.setContentText(String.format("用户名:%s\n密码:%s",username,password));
                                    DialogPane dialogPane = alert.getDialogPane();
                                    dialogPane.setHeaderText("");
                                    dialogPane.setGraphic(null);
                                    alert.showAndWait();
                                });
                            }
                            break;
                        case chat:
                            break;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
    void toRegister() throws IOException {
        Message pw = new Message(0L,username, "Server",password,MessageType.register);
        out.writeObject(pw);
    }
    void connect() throws IOException {
        Message pw = new Message(0L,username, "Server",password,MessageType.connect);
        out.writeObject(pw);
        //out.flush();
    }
}
