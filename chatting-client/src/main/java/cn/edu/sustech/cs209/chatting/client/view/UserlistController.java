package cn.edu.sustech.cs209.chatting.client.view;

import cn.edu.sustech.cs209.chatting.client.util.FileOperator;
import cn.edu.sustech.cs209.chatting.client.util.Group;
import cn.edu.sustech.cs209.chatting.client.util.Notification;
import cn.edu.sustech.cs209.chatting.client.util.Sender;
import cn.edu.sustech.cs209.chatting.client.util.User;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
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
  public static User thisuser;
  public static ObservableList<User> userList;
  public static HashMap<ArrayList<String>, Stage> stages = new HashMap<>();
  private final ArrayList<User> nowGroup = new ArrayList<>();
  private final HashMap<ArrayList<String>, Controller> cons = new HashMap<>();


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    chatList.setCellFactory(new UserCellFactory());
    chatList.setItems(userList);
  }

  public void addMessage(Message msg) {
    Controller nowCons = cons.get(msg.getSendTo());
    Stage stage = stages.get(msg.getSendTo());
    if (stage != null) {
      if (!stage.isShowing()) {
        stage.show();
        Platform.runLater(() -> {
          Notification notification = new Notification(MessageType.chat);
          notification.setContent("用户" + msg.getSentBy() + "给你发来了消息", "请注意查收");
        });
      }
    }

    if (nowCons != null) {
      for (User i : userList) {
        if (i.getUsername().equals(msg.getSentBy())) {
          i.setLastMessage(-System.currentTimeMillis());
        }
      }
      if (msg.getSendTo().size() == 2) {
        String name = msg.getSendTo().get(0).equals(thisuser.getUsername()) ?
            msg.getSendTo().get(1) : msg.getSendTo().get(0);
        for (User i : userList) {
          if (i.getUsername().equals(name)) {
            i.setLastMessage(-System.currentTimeMillis());
          }
        }
      }
      nowCons.addMessage(msg);
    }

    userList.sort(User.userComparator);
    ObservableList<User> users = FXCollections.observableArrayList();
    users.addAll(userList);
    chatList.setCellFactory(new UserCellFactory());
    chatList.setItems(users);
  }

  public synchronized void addOnline(User user) {
    ObservableList<User> users = FXCollections.observableArrayList();
    boolean flag = true;
    for (User i : userList) {
      users.add(i);
      if (i.equals(user)) {
        System.out.println("存在" + i.getUsername());
        flag = false;
        i.setOnline(true);
      }
    }
    if (flag) {
      System.out.println("不存在" + user.getUsername());
      users.add(user);
    }
    Platform.runLater(() -> {
      Notification notification = new Notification(MessageType.online);
      notification.setContent("用户" + user.getUsername() + "上线了", "点击用户名即可聊天");
    });
    users.sort(User.userComparator);
    //refresh the userList in every chat window
    for (Controller i : cons.values()) {
      i.refresh();
    }
    chatList.setItems(users);
    chatList.setCellFactory(new UserCellFactory());
    userList.setAll(users);
  }

  public synchronized void disconnect(User user) {
    ObservableList<User> users = FXCollections.observableArrayList();
    for (User i : userList) {
      users.add(i);
      if (i.equals(user)) {
        System.out.println("存在" + i.getUsername() + "下号");
        i.setOnline(false);
      }
    }
    Platform.runLater(() -> {
      Notification notification = new Notification(MessageType.disconnect);
      notification.setContent("用户" + user.getUsername() + "下线了", "");
    });
    //refresh the userList in every chat window
    for (Controller i : cons.values()) {
      i.refresh();
    }
    chatList.setItems(users);
    chatList.setCellFactory(new UserCellFactory());
    userList.setAll(users);
  }

  public void createChat(ArrayList<String> usernames) throws IOException {
    usernames.sort(Comparator.naturalOrder());
    if (cons.containsKey(usernames)) {
      // if the chat window already exists, it will show the window
      System.out.println("该聊天窗口已存在");
      stages.get(usernames).show();
      return;
    }
    // else it will create a new chat window
    FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/main.fxml"));
    Parent root = loader.load();
    Stage stage = new Stage();
    stage.setScene(new Scene(root));
    stage.initStyle(StageStyle.DECORATED);
    stage.setOnCloseRequest(event -> {
      event.consume();
      stage.hide();
    });
//        if(usernames.size()==2){
//            stage.setTitle("正在和"+usernames.get(0)+"聊天");
//        } else {
//            stage.setTitle(String.format("%s,%s,%s等的群组聊天(%d)",usernames.get(0),usernames.get(1),usernames.get(2),
//                usernames.size()));
//        }
    Controller nowCon = loader.getController();
    //read from the file
    Group group = FileOperator.readGroupList(usernames);
    FileOperator.saveGroupList(group);

    Controller.thisuser = thisuser;
    cons.put(group.getGroupMember(), nowCon);

    nowCon.setGroup(group);
    nowCon.initUserList(userList);
    nowCon.initMessageList();

    //nowGroup.forEach(user -> user.getGroups().add(group));
    stages.put(usernames, stage);
    Sender.send(
        new Message(0L, thisuser.getUsername(), usernames, "start", MessageType.createGroup));
    stage.show();
  }

  @FXML
  public void createGroupChat() throws IOException {
    // if it doesn't select any user, return
    if (nowGroup.isEmpty()) {
      Alert alert = new Alert(AlertType.INFORMATION);
      alert.setTitle("提示");
      alert.setHeaderText(null);
      alert.setContentText("请先选择用户");
      alert.showAndWait();
      return;
    }
    //get the arraylist of usernames
    ArrayList<String> usernames = new ArrayList<>();
    nowGroup.forEach(user -> usernames.add(user.getUsername()));
    usernames.add(thisuser.getUsername());
    createChat(usernames);
  }

  @FXML
  public void createPrivateChat(User username) throws IOException {
    ArrayList<String> usernames = new ArrayList<>();
    usernames.add(username.getUsername());
    usernames.add(thisuser.getUsername());
    usernames.sort(Comparator.naturalOrder());
    Group group = FileOperator.readGroupList(usernames);
    if (!group.getHistory().isEmpty() || username.isOnline()) {
      createChat(usernames);
    } else {
      Alert alert = new Alert(AlertType.INFORMATION);
      alert.setTitle("提示");
      alert.setHeaderText(null);
      alert.setContentText("你与该用户不存在聊天记录，且该用户不在线");
      alert.showAndWait();
    }
  }

  public void getOnline(String username) {
      if (userList.isEmpty()) {
          addOnline(thisuser);
      }
    currentUsername.setText(username);
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
            setText(null);
            setGraphic(null);
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
                  .toString(), 16, 16, true, true);
          pictureView.setImage(picImage);
          //status
          ImageView statusImageView = new ImageView();
          String status = "offline";
            if (user.isOnline()) {
                status = "online";
            }
          Image statusImage = new Image(
              Objects.requireNonNull(this.getClass()
                      .getResource(String.format("../image/%s.png", status)))
                  .toString(), 16, 16, true, true);
          statusImageView.setImage(statusImage);
          //name
          nameLabel.setPrefSize(50, 20);
          nameLabel.setWrapText(true);
          //double click
          EventHandler<MouseEvent> doubleClickHandler = event -> {
            if (event.getClickCount() == 2) {
              try {
                createPrivateChat(user);
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          };

          if (thisuser.getUsername().equals(user.getUsername())) {
            wrapper.setAlignment(Pos.TOP_LEFT);
            infoLabel.setText("Your username");
            infoLabel.setPadding(new Insets(0, 10, 0, 0));
          } else {
            wrapper.setAlignment(Pos.TOP_CENTER);
            infoLabel.setText("Offline");
            infoLabel.setPadding(new Insets(0, 10, 0, 0));
            if (user.isOnline()) {
              infoLabel.setText("Online");
              //deal with group choice
              CheckBox toChat = new CheckBox();
              toChat.setSelected(nowGroup.contains(user));
              toChat.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                  nowGroup.add(user);
                  System.out.println(user.getUsername() + "is now selected");
                } else {
                  nowGroup.remove(user);
                  System.out.println(user.getUsername() + "is now deselected");
                }
              });
              wrapper.getChildren().add(toChat);
            }
          }
          //deal with private click
          nameLabel.setOnMouseClicked(doubleClickHandler);
          infoLabel.setOnMouseClicked(doubleClickHandler);
          //add to wrapper
          wrapper.getChildren().addAll(pictureView, statusImageView, infoLabel, nameLabel);
          setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
          setGraphic(wrapper);
        }
      };
    }
  }
}
