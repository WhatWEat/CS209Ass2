package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.client.util.FileOperator;
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
                if(UserlistController.thisuser != null) FileOperator.saveUserList();
                Sender.close();
            } catch (IOException ex) {
                //throw new RuntimeException(ex);
            }
        });
        stage.show();
    }

    public static Stage getPrimaryStage() {
        return stage;
    }
    public static void setStage(Stage stage){
        Main.stage = stage;
    }
}
