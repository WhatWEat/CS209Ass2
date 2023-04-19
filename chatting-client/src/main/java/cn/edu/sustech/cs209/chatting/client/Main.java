package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.client.util.Sender;
import cn.edu.sustech.cs209.chatting.client.util.User;
import cn.edu.sustech.cs209.chatting.client.view.UserlistController;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    private static Stage stage;
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        Main.stage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("view/login.fxml"));
        stage.setScene(new Scene(fxmlLoader.load()));
        stage.setTitle("Chat!");
        stage.setOnCloseRequest(e->{
            try {
                saveUserList();
                Sender.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        stage.show();
    }
    public static void readUserList(){
        try {
            File file = new File(".\\user\\"+
                UserlistController.thisuser.getUsername()+"\\"+UserlistController.thisuser.getUsername());
            if(!file.getParentFile().getParentFile().exists()) file.getParentFile().getParentFile().mkdirs();
            if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            ArrayList<User> userList = (ArrayList<User>) objectIn.readObject();
            UserlistController.userList = FXCollections.observableArrayList(userList);
            userList.forEach(i->i.setOnline(false));
            objectIn.close();
            fileIn.close();
        } catch (IOException e) {
            // 如果文件不存在，则创建一个新的 ArrayList<User> 对象
            UserlistController.userList = FXCollections.observableArrayList();
            saveUserList();
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found: " + e.getMessage());
        }
    }
    public static void saveUserList(){
        try{
            //读取目录
            File file = new File(".\\user\\"+
                UserlistController.thisuser.getUsername()+"\\"+UserlistController.thisuser.getUsername());
            if(!file.getParentFile().getParentFile().exists()) file.getParentFile().getParentFile().mkdirs();
            if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
            //从file读取流
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(fileOutputStream);
            ArrayList<User> userList = new ArrayList<>(UserlistController.userList);
            out.writeObject(userList);
            out.close();
            fileOutputStream.close();
            System.out.println("用户列表保存成功");
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    public static Stage getPrimaryStage() {
        return stage;
    }
    public static void setStage(Stage stage){
        Main.stage = stage;
    }
}
