package cn.edu.sustech.cs209.chatting.client.view;

import cn.edu.sustech.cs209.chatting.client.Main;
import cn.edu.sustech.cs209.chatting.client.util.Group;
import cn.edu.sustech.cs209.chatting.client.util.Sender;
import cn.edu.sustech.cs209.chatting.client.util.User;
import cn.edu.sustech.cs209.chatting.common.*;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

public class Controller implements Initializable {

    @FXML
    ListView<Message> chatContentList;
    @FXML
    ListView<User> chatList;
    @FXML
    public TextArea inputCon;
    @FXML
    public Label currentOnlineCnt;
    public static User thisuser;
    private Group group;
    private ObservableList<Message> messageList;
    private ObservableList<User> userList;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        messageList = FXCollections.observableArrayList();
        chatContentList.setCellFactory(new MessageCellFactory());
        chatContentList.setItems(messageList);
        userList = FXCollections.observableArrayList();
    }
    public void initUserList(ObservableList<User> userList){
        Long count = 0L;
        for(User i:userList){
            if(group.getGroupMember().contains(i.getUsername())){
                if(i.isOnline()) count++;
                this.userList.add(i);
            }
        }
        currentOnlineCnt.setText(String.format("Online: %d",count));
        chatList.setCellFactory(new UserCellFactory());
        chatList.setItems(this.userList);
    }
    public void addMessage(Message msg){
        group.addMessage(msg);
        messageList.add(msg);
        chatContentList.refresh();
    }
    public void refresh(){
        chatList.setCellFactory(new UserCellFactory());
        chatList.setItems(this.userList);
        Long count = 0L;
        for(User i:userList){
            if(i.isOnline()) count++;
        }
        currentOnlineCnt.setText(String.format("Online: %d",count));
    }
    public void setGroup(Group group) {
        this.group = group;
    }

    /**
     * Sends the message to the <b>currently selected</b> chat.
     * <p>
     * Blank messages are not allowed.
     * After sending the message, you should clear the text input field.
     */
    @FXML
    public void doSendMessage() throws IOException {
        if(inputCon.getText().equals("")) return;
        Message msg = new Message(System.currentTimeMillis(), thisuser.getUsername(), group.getGroupMember(), inputCon.getText(), MessageType.chat);
        System.out.println("当前时间:"+msg.getTimestamp());
        Sender.send(msg);
        inputCon.clear();
    }
    /**
     * You may change the cell factory if you changed the design of {@code Message} model.
     * Hint: you may also define a cell factory for the chats displayed in the left panel, or simply override the toString method.
     */
    private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
        @Override
        public ListCell<Message> call(ListView<Message> param) {
            return new ListCell<Message>() {

                @Override
                public void updateItem(Message msg, boolean empty) {
                    super.updateItem(msg, empty);
                    if (empty || Objects.isNull(msg)) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    HBox wrapper = new HBox();
                    Label nameLabel = new Label(msg.getSentBy());
                    Label msgLabel = new Label(msg.getData());

                    nameLabel.setPrefSize(50, 20);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

                    if (thisuser.getUsername().equals(msg.getSentBy())) {
                        wrapper.setAlignment(Pos.TOP_RIGHT);
                        wrapper.getChildren().addAll(msgLabel, nameLabel);
                        msgLabel.setPadding(new Insets(0, 20, 0, 0));
                    } else {
                        wrapper.setAlignment(Pos.TOP_LEFT);
                        wrapper.getChildren().addAll(nameLabel, msgLabel);
                        msgLabel.setPadding(new Insets(0, 0, 0, 20));
                    }

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }
    private class UserCellFactory implements Callback<ListView<User>, ListCell<User>> {
        @Override
        public ListCell<User> call(ListView<User> param) {
            return new ListCell<User>() {

                @Override
                public void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);
                    if (empty || Objects.isNull(user)) {
                        return;
                    }

                    HBox wrapper = new HBox();
                    Label nameLabel = new Label(user.getUsername());
                    Label infoLabel = new Label("");
                    //picture
                    ImageView pictureView = new ImageView();
                    Image picImage = new Image(
                        Objects.requireNonNull(this.getClass()
                                .getResource(String.format("../image/%s.png", user.getPicture())))
                            .toString(),16,16,true,true);
                    pictureView.setImage(picImage);
                    //status
                    ImageView statusImageView = new ImageView();
                    String status = "offline";
                    if(user.isOnline()) status = "online";
                    Image statusImage = new Image(
                        Objects.requireNonNull(this.getClass()
                                .getResource(String.format("../image/%s.png", status)))
                            .toString(),16,16,true,true);
                    statusImageView.setImage(statusImage);
                    //name
                    nameLabel.setPrefSize(50, 20);
                    nameLabel.setWrapText(true);
//                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

                    if (thisuser.getUsername().equals(user.getUsername())) {
                        wrapper.setAlignment(Pos.TOP_LEFT);
                        infoLabel.setText("Your username:");
                        //infoLabel.setPadding(new Insets(0, 20, 0, 0));
                    } else {
                        wrapper.setAlignment(Pos.TOP_CENTER);
                        infoLabel.setText("Offline");
                        infoLabel.setPadding(new Insets(0, 20, 0, 0));
                        if(user.isOnline()){
                            infoLabel.setText("Online");
                        }
                    }
                    wrapper.getChildren().addAll(pictureView,statusImageView,infoLabel, nameLabel);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }
}
