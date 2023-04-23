package cn.edu.sustech.cs209.chatting.client.view;

import cn.edu.sustech.cs209.chatting.client.Main;
import cn.edu.sustech.cs209.chatting.client.util.Sender;
import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javax.swing.text.html.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;

public class LoginController implements Initializable {

  @FXML
  public TextField passwordTextfield;

  @FXML
  private TextField usernameTextfield;

  @FXML
  private Label selectedPicture;
  public static Controller con;
  @FXML
  private BorderPane borderPane;
  private double xOffset;
  private double yOffset;
  private Scene scene;

  private static LoginController instance;

  public LoginController() {
    instance = this;
  }

  public static LoginController getInstance() {
    return instance;
  }

  public void loginButtonAction() throws IOException {
    String password = passwordTextfield.getText();
    String username = usernameTextfield.getText();

    Sender sender = new Sender(username, password);
    new Thread(sender).start();
        /*TODO:
           check the password
        * */
  }

  public void regButtonAction() throws IOException {
    String password = passwordTextfield.getText();
    String username = usernameTextfield.getText();

    Sender sender = new Sender(username, password, true);

    new Thread(sender).start();
    //showAlert("same");

  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    for (int i = 1; i < 30; i++) {
      generateAnimation();
    }
  }


  /* This method is used to generate the animation on the login window, It will generate random ints to determine
   * the size, speed, starting points and direction of each square.
   */
  public void generateAnimation() {
    Random rand = new Random();
    int sizeOfSqaure = rand.nextInt(50) + 1;
    int speedOfSqaure = rand.nextInt(10) + 5;
    int startXPoint = rand.nextInt(420);
    int startYPoint = rand.nextInt(350);
    int direction = rand.nextInt(5) + 1;

    KeyValue moveXAxis = null;
    KeyValue moveYAxis = null;

    Rectangle r1 = null;
    Circle r2 = null;
    switch (direction) {
      case 1:
        // MOVE LEFT TO RIGHT
        r1 = new Rectangle(0, startYPoint, sizeOfSqaure, sizeOfSqaure);
        moveXAxis = new KeyValue(r1.xProperty(), 350 - sizeOfSqaure);
        break;
      case 2:
        // MOVE TOP TO BOTTOM
        r2 = new Circle(startXPoint, 0, sizeOfSqaure);
        moveYAxis = new KeyValue(r2.centerYProperty(), 420 - sizeOfSqaure);
        break;
      case 3:
        //MOVE RIGHT TO LEFT, BOTTOM TO TOP
        // MOVE LEFT TO RIGHT, TOP TO BOTTOM
        r1 = new Rectangle(startXPoint, 0, sizeOfSqaure, sizeOfSqaure);
        moveXAxis = new KeyValue(r1.xProperty(), 350 - sizeOfSqaure);
        moveYAxis = new KeyValue(r1.yProperty(), 420 - sizeOfSqaure);
        break;
      case 4:
        // MOVE BOTTOM TO TOP
        r1 = new Rectangle(startXPoint, 420 - sizeOfSqaure, sizeOfSqaure, sizeOfSqaure);
        moveYAxis = new KeyValue(r1.xProperty(), 0);
        break;
      case 5:
        // MOVE RIGHT TO LEFT
        r2 = new Circle(420 - sizeOfSqaure, startYPoint, sizeOfSqaure);
        moveXAxis = new KeyValue(r2.centerXProperty(), 0);
        break;
      default:
        System.out.println("default");
    }
    KeyFrame keyFrame = new KeyFrame(Duration.millis(speedOfSqaure * 1000), moveXAxis,
        moveYAxis);
    Timeline timeline = new Timeline();
    timeline.setCycleCount(Timeline.INDEFINITE);
    timeline.setAutoReverse(true);
    timeline.getKeyFrames().add(keyFrame);
    timeline.play();
    if (direction == 2 || direction == 5) {
      r2.setFill(Color.web("#98DFD6"));
      r2.setOpacity(0.3);
      borderPane.getChildren().add(borderPane.getChildren().size() - 1, r2);
    } else {
      r1.setFill(Color.web("#B3E5BE"));
      r1.setOpacity(0.2);
      borderPane.getChildren().add(borderPane.getChildren().size() - 1, r1);
    }

  }

  /* This displays an alert message to the user */
  public void showErrorDialog(String message) {
    Platform.runLater(() -> {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setTitle("Warning!");
      alert.setHeaderText(message);
      alert.setContentText(
          "Please check for firewall issues and check if the server is running.");
      alert.showAndWait();
    });

  }
}
