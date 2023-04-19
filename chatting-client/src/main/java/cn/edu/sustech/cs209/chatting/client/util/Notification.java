package cn.edu.sustech.cs209.chatting.client.util;

import cn.edu.sustech.cs209.chatting.common.MessageType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class Notification {
    @FXML
    private AnchorPane rootNode;
    @FXML
    private Label lblTitle, lblMessage;
    @FXML
    private Rectangle rectangleColor;
    @FXML
    private ImageView imageIcon;
    private CustomStage stage;
    private MessageType type;
    private Timeline timeline;
    public Notification(MessageType type) {
        this.type = type;
        initStage();
    }
    private void initStage() {
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../view/notice.fxml"));
            fxmlLoader.setController(this);
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //init stage
        stage = new CustomStage(rootNode, StageStyle.UNDECORATED);
        stage.setScene(new Scene(rootNode));
        stage.setAlwaysOnTop(true);
        stage.setLocation(stage.getBottomRight());

        //set message
        setBasic();
        InitialAnimation();
        stage.show();
        timeline.play();
    }
    public void InitialAnimation(){
        timeline = new Timeline();
        timeline.setCycleCount(1);
        timeline.setAutoReverse(false);
        Duration duration = Duration.seconds(2);
        int frames = 60;
        double interval = duration.toMillis() / frames;
        double opacityDelta = 1.0 / frames;
        List<KeyValue> keyValues = new ArrayList<>();
        for (int i = 0; i < frames; i++) {
            double time = i * interval;
            double opacity = 1.0 - i * opacityDelta;
            keyValues.add(new KeyValue(stage.opacityProperty(), opacity, Interpolator.LINEAR));
            KeyValue frameKeyValue = new KeyValue(stage.opacityProperty(), opacity, Interpolator.LINEAR);
            KeyFrame keyFrame = new KeyFrame(Duration.millis(time), frameKeyValue);
            timeline.getKeyFrames().add(keyFrame);
        }
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(2), event -> {
            stage.close();
        }));
    }
    public void setBasic(){
        Image image = null;
        switch (type){
            case online:
                image = new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream("../image/default.png")));
                rectangleColor.setStyle("-fx-fill: #7cd270");
                lblTitle.setStyle("-fx-text-fill: #57c557");
                break;
            case disconnect:
                image = new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream("../image/disconnect.png")));
                rectangleColor.setStyle("-fx-fill: #ef40be");
                lblTitle.setStyle("-fx-text-fill: #ff6767");
                break;
            case chat:
                image = new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream("../image/chat.png")));
                rectangleColor.setStyle("-fx-fill: #ffc092");
                lblTitle.setStyle("-fx-text-fill: #d07f4a");
                break;
        }
        imageIcon.setImage(image);
    }
    public void setContent(String title, String message){
        lblTitle.setText(title);
        lblMessage.setText(message);
    }
    private static class CustomStage extends Stage {

        private final Location bottomRight;

        public CustomStage(AnchorPane ap, StageStyle style) {
            initStyle(style);

            setSize(ap.getPrefWidth(), ap.getPrefHeight());

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double x = screenBounds.getMinX() + screenBounds.getWidth() - ap.getPrefWidth() - 2;
            double y = screenBounds.getMinY() + screenBounds.getHeight() - ap.getPrefHeight() - 2;

            bottomRight = new Location(x, y);
        }

        public Location getBottomRight() {
            return bottomRight;
        }

        public void setSize(double width, double height) {
            setWidth(width);
            setHeight(height);
        }

        public void setLocation(Location loc) {
            setX(loc.getX());
            setY(loc.getY());
        }

    }
    private static class Location {

        private final double x,y;

        public Location(double xLoc, double yLoc) {
            this.x = xLoc;
            this.y = yLoc;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }
}
