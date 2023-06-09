package cn.edu.sustech.cs209.chatting.client.view;

import cn.edu.sustech.cs209.chatting.client.util.FileOperator;
import cn.edu.sustech.cs209.chatting.client.util.Group;
import cn.edu.sustech.cs209.chatting.client.util.Sender;
import cn.edu.sustech.cs209.chatting.client.util.User;
import cn.edu.sustech.cs209.chatting.common.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
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

  public void initUserList(ObservableList<User> userList) {
    Long count = 0L;
    for (User i : userList) {
      if (group.getGroupMember().contains(i.getUsername())) {
          if (i.isOnline()) {
              count++;
          }
        this.userList.add(i);
      }
    }
    currentOnlineCnt.setText(String.format("Online: %d", count));
    chatList.setCellFactory(new UserCellFactory());
    chatList.setItems(this.userList);
  }

  public void initMessageList() {
    ArrayList<Message> historyList = group.getHistory();
    messageList.addAll(historyList);
    //chatContentList.refresh();
  }

  public void addMessage(Message msg) {
    group.addMessage(msg);
    messageList.add(msg);
    chatContentList.refresh();
    FileOperator.saveGroupList(group);
  }

  public String fileToBase64(String filePath) {
    //convert file to base64
    try {
      byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
      return Base64.getEncoder().encodeToString(fileContent);
    } catch (IOException e) {
      System.out.println("File convert wrong in encoder");
      e.printStackTrace();
      return null;
    }
  }

  public static void base64StringToFile(String base64String, String outputFile) {
    try {
      byte[] decodedBytes = Base64.getDecoder().decode(base64String);
      Files.write(Paths.get(outputFile), decodedBytes);
    } catch (IOException e) {
      System.out.println("File convert wrong in decoder");
      e.printStackTrace();
    }

  }

  public void refresh() {
    chatList.setCellFactory(new UserCellFactory());
    chatList.setItems(this.userList);
    Long count = 0L;
    for (User i : userList) {
        if (i.isOnline()) {
            count++;
        }
    }
    currentOnlineCnt.setText(String.format("Online: %d", count));
  }

  public void setGroup(Group group) {
    this.group = group;
  }

  @FXML
  public void sendFile() {
    try {
      FileChooser fileChooser = new FileChooser();
      File selectedFile = fileChooser.showOpenDialog(null);
      if (selectedFile != null) {
        // 打印所选文件的路径
        String filePath = selectedFile.getAbsolutePath();
        System.out.println("Selected file: " + filePath);
        String data = String.format("%s@@%s", selectedFile.getName(), fileToBase64(filePath));
        Sender.send(
            new Message(System.currentTimeMillis(), thisuser.getUsername(), group.getGroupMember(),
                data, MessageType.file));
      } else {
        System.out.println("File selection cancelled.");
      }
    } catch (IOException e) {
      System.out.println("File sending is wrong");
    }
  }

  public void saveChatInfo() {
    System.out.println();
  }

  @FXML
  public void doSendMessage() throws IOException {
      if (inputCon.getText().equals("")) {
          return;
      }
    Message msg = new Message(System.currentTimeMillis(), thisuser.getUsername(),
        group.getGroupMember(), inputCon.getText(), MessageType.chat);
    System.out.println("当前时间:" + msg.getTimestamp());
    Sender.send(msg);
    inputCon.clear();
  }

  /**
   * You may change the cell factory if you changed the design of {@code Message} model. Hint: you
   * may also define a cell factory for the chats displayed in the left panel, or simply override
   * the toString method.
   */
  private static class MessageCellFactory implements
      Callback<ListView<Message>, ListCell<Message>> {

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
          Label msgLabel = new Label();

          nameLabel.setPrefSize(50, 20);
          nameLabel.setWrapText(true);
          nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");
          //file or text
          switch (msg.getType()) {
            case chat:
              msgLabel.setText(msg.getData());
              break;
            case file:
              Image image = new Image(
                  Objects.requireNonNull(getClass().getResource("../image/download.png"))
                      .toString());
              msgLabel.setGraphic(new ImageView(image));
              msgLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                  //get file name and data
                  int index = msg.getData().indexOf("@@");
                  String fileName = msg.getData().substring(0, index);
                  String data = msg.getData().substring(index + 2);
                  // set file chooser
                  FileChooser fileChooser = new FileChooser();
                  fileChooser.setTitle("选择保存位置");
                  fileChooser.setInitialFileName(fileName);
                  File selectedFile = fileChooser.showSaveDialog(null);
                  if (selectedFile != null) {
                    base64StringToFile(data, selectedFile.getAbsolutePath());
                    new Alert(AlertType.INFORMATION, "保存成功!").showAndWait();
                  }
                }
              });
              break;
          }
          // other or me
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

  private static class UserCellFactory implements Callback<ListView<User>, ListCell<User>> {

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
//                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

          if (thisuser.getUsername().equals(user.getUsername())) {
            wrapper.setAlignment(Pos.TOP_LEFT);
            infoLabel.setText("Your username:");
            //infoLabel.setPadding(new Insets(0, 20, 0, 0));
          } else {
            wrapper.setAlignment(Pos.TOP_CENTER);
            infoLabel.setText("Offline");
            infoLabel.setPadding(new Insets(0, 20, 0, 0));
            if (user.isOnline()) {
              infoLabel.setText("Online");
            }
          }
          wrapper.getChildren().addAll(pictureView, statusImageView, infoLabel, nameLabel);
          setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
          setGraphic(wrapper);
        }
      };
    }
  }
}
