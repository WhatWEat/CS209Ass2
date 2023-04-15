package cn.edu.sustech.cs209.chatting.client.view;

import cn.edu.sustech.cs209.chatting.client.util.Group;
import cn.edu.sustech.cs209.chatting.client.util.User;
import cn.edu.sustech.cs209.chatting.common.Message;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

public class UserlistController implements Initializable {

    @FXML
    public Label currentOnlineCnt;
    @FXML
    private Label currentUsername;
    @FXML
    private ListView<User> chatList;
    public User thisuser;
    public static ObservableList<User> userList;
    private ArrayList<User> nowGroup = new ArrayList<>();
    private ArrayList<Group> groups = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userList = FXCollections.observableArrayList();
        chatList.setItems(userList);
        chatList.setCellFactory(new UserCellFactory());
    }
    //@FXML
    public void createPrivateChat() {
        userList.add(thisuser);
        AtomicReference<String> user = new AtomicReference<>();

        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();

        // FIXME: get the user list from server, the current user's name should be filtered out
        userSel.getItems().addAll("Item 1", "Item 2", "Item 3");

        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            user.set(userSel.getSelectionModel().getSelectedItem());
            stage.close();
        });

        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(userSel, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();

        // TODO: if the current user already chatted with the selected user, just open the chat with that user
        // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name
    }
    public void getOnline(String username){
        thisuser = new User(username);
        addOnline(thisuser);
        currentUsername.setText(username);
    }
    public synchronized void addOnline(User user){
        ObservableList<User> users = FXCollections.observableArrayList();
        boolean flag = true;
        for(User i:userList){
            users.add(i);
            if(i.equals(user)){
                System.out.println("存在"+i.getUsername());
                flag = false;
                i.setOnline(true);
            }
        }
        if(flag) {
            System.out.println("不存在"+user.getUsername());
            users.add(user);
        }
        chatList.setItems(users);
        chatList.setCellFactory(new UserCellFactory());
        userList.setAll(users);
    }
    public void disconnect(User user){
        ObservableList<User> users = FXCollections.observableArrayList();
        for(User i:userList){
            users.add(i);
            if(i.equals(user)){
                System.out.println("存在"+i.getUsername()+"下号");
                i.setOnline(false);
            }
        }
        chatList.setItems(users);
        chatList.setCellFactory(new UserCellFactory());
        userList.setAll(users);
    }
    /**
     * A new dialog should contain a multi-select list, showing all user's name.
     * You can select several users that will be joined in the group chat, including yourself.
     * <p>
     * The naming rule for group chats is similar to WeChat:
     * If there are > 3 users: display the first three usernames, sorted in lexicographic order, then use ellipsis with the number of users, for example:
     * UserA, UserB, UserC... (10)
     * If there are <= 3 users: do not display the ellipsis, for example:
     * UserA, UserB (2)
     */
    @FXML
    public void createGroupChat() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/main.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.initStyle(StageStyle.DECORATED);
        //create a new group instance
        ArrayList<String> usernames = new ArrayList<>();
        nowGroup.forEach(user -> usernames.add(user.getUsername()));
        usernames.sort(Comparator.naturalOrder());
        Group group = new Group(usernames,loader.getController());
        Controller.thisuser = thisuser;
        nowGroup.forEach(user -> user.getGroups().add(group));
        stage.show();
    }

    /*
     * Sends the message to the <b>currently selected</b> chat.
     * <p>
     * Blank messages are not allowed.
     * After sending the message, you should clear the text input field.
     */
    /*
     * You may change the cell factory if you changed the design of {@code Message} model.
     * Hint: you may also define a cell factory for the chats displayed in the left panel, or simply override the toString method.
     */
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
                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");
                    //double click
                    EventHandler<MouseEvent> doubleClickHandler = event -> {
                        if (event.getClickCount() == 2) {
                            Alert alert = new Alert(AlertType.INFORMATION);
                            alert.setTitle("Double Click");
                            alert.setHeaderText(null);
                            alert.setContentText("You double-clicked the label!");
                            alert.showAndWait();
                        }
                    };

                    if (thisuser.getUsername().equals(user.getUsername())) {
                        wrapper.setAlignment(Pos.TOP_LEFT);
                        infoLabel.setText("Your username");
                        infoLabel.setPadding(new Insets(0, 20, 0, 0));
                    } else {
                        wrapper.setAlignment(Pos.TOP_CENTER);
                        infoLabel.setText("Offline");
                        infoLabel.setPadding(new Insets(0, 30, 0, 0));
                        if(user.isOnline()){
                            infoLabel.setText("Online");
                            //deal with group choice
                            CheckBox toChat = new CheckBox();
                            toChat.setSelected(nowGroup.contains(user));
                            toChat.selectedProperty().addListener((observable, oldValue, newValue) -> {
                                if (newValue) {
                                    nowGroup.add(user);
                                    System.out.println(user.getUsername()+"is now selected");
                                } else {
                                    nowGroup.remove(user);
                                    System.out.println(user.getUsername()+"is now deselected");
                                }
                            });
                            wrapper.getChildren().add(toChat);
                            //deal with private click
                            nameLabel.setOnMouseClicked(doubleClickHandler);
                            infoLabel.setOnMouseClicked(doubleClickHandler);
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
