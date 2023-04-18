package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.client.util.Sender;
import java.util.ArrayList;
import javafx.application.Application;
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
                Sender.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
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
