package io.github.cr3ahal0.forum.client.ihm;

import io.github.cr3ahal0.forum.client.impl.ClientForum;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import static javafx.geometry.HPos.RIGHT;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.scene.control.PasswordField;

import java.util.*;
import java.rmi.RemoteException;

public class Launcher extends Application 
{

    private TextField m_login;
    private PasswordField m_password;
    private TextField m_url;
    private TextField m_port;
    private Stage m_primaryStage;
    private Text m_textError;

	public void start(final Stage primaryStage){

        m_primaryStage = primaryStage;
        primaryStage.setTitle("Launcher");
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text scenetitle = new Text("Welcome");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);

        Label userName = new Label("Login:");
        grid.add(userName, 0, 1);

        m_login = new TextField();
        grid.add(m_login, 1, 1);
        m_login.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                launchForum();
            }
        });         

        Label pw = new Label("Password:");
        grid.add(pw, 0, 2);

        m_password = new PasswordField();
        grid.add(m_password, 1, 2);
        m_password.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                launchForum();
            }
        });

        Label server = new Label("Server:");
        grid.add(server, 0, 3);

        m_url = new TextField();
        m_url.setText("//127.0.0.1");
        grid.add(m_url, 1, 3);
        m_url.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                launchForum();
            }
        });

        Label port = new Label("Port:");
        grid.add(port, 0, 4);

        m_port = new TextField();
        m_port.setText("8090");
        grid.add(m_port, 1, 4);
        m_port.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                launchForum();
            }
        });

        Button btn = new Button("Sign in");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btn);
        grid.add(hbBtn, 1, 5);

        m_textError = new Text();
        grid.add(m_textError, 0, 7);
        grid.setColumnSpan(m_textError, 2);
        grid.setHalignment(m_textError, RIGHT);

        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                launchForum();
            }
        });

        Scene scene = new Scene(grid, 325, 250);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

	}

    public void launchForum(){
        try {
            ClientForum c = new ClientForum(m_primaryStage, m_url.getText(), m_port.getText());

            boolean test = c.tryLogin(m_login.getText(),m_password.getText());
            if(test)
            {
                c.startWindow();
            }
            else{
                m_textError.setFill(Color.FIREBRICK);
                m_textError.setText("Erreur de login/password !");
                m_password.setText("");
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (NullPointerException ex2){
            m_textError.setFill(Color.FIREBRICK);
            m_textError.setText("Erreur : Server not start ...");
            ex2.printStackTrace();
            System.out.println("Erreur : Serveur not start");
        }

    }

}
