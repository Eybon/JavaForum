package io.github.cr3ahal0.forum.client.ihm;

import io.github.cr3ahal0.forum.client.pattern.Observable;
import io.github.cr3ahal0.forum.client.pattern.Observer;
import io.github.cr3ahal0.forum.server.IMessage;

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
import javafx.application.Platform;
import javafx.scene.control.ScrollPane;

import java.awt.*;
//import javafx.scene.paint.Color;
import java.util.*;
import java.rmi.RemoteException;

public class Channel extends Tab implements Observable
{

	private Tab m_tab;
	private String m_name;

	private GridPane m_displayMessage;
	private int m_nbMessage;
	private TextField m_textZone;

	private java.util.List<Observer> listObserver = new ArrayList<Observer>();	

	public Channel(String name){
		super(name);
		m_name = name;
		m_nbMessage = 0;

		createPageOfChannel();
	}


	public void createPageOfChannel(){
		final Channel current = this;

		/* Channel zone */
		m_displayMessage = new GridPane();

		/* Writing zone */
		GridPane panelWriter = new GridPane();
		Pane blankStart = new Pane();
		blankStart.setPrefWidth(50);
		panelWriter.add(blankStart,1,1);

		m_textZone = new TextField();
		m_textZone.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
                if (m_textZone.getText() != "") {
                    notifyObserver(current, m_textZone.getText());
                    m_textZone.setText("");
                }
			}
		});			
		m_textZone.setPrefWidth(500);
		panelWriter.add(m_textZone,2,1);

		Button validate = new Button("Ok");
		validate.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
                if (m_textZone.getText() != "") {
                    notifyObserver(current, m_textZone.getText());
                    m_textZone.setText("");
                }
			}
		});		
		validate.setPrefWidth(50);
		panelWriter.add(validate,3,1);

		panelWriter.setVgap(30);
		panelWriter.setHgap(10);


		/* Main Zone */
		BorderPane globalPanel = new BorderPane();
		ScrollPane scrollPanel = new ScrollPane();
		scrollPanel.setContent(m_displayMessage);
		globalPanel.setCenter(scrollPanel);
		globalPanel.setBottom(panelWriter);	

		this.setContent(globalPanel);
	}

final static int MAX_SIZE_OF_TEXT = 70;
	
    public void addNewMessage(IMessage message)
    {

        try {    	
			final String textToPrint = message.getString();
			//System.out.println("DEBUG :: "+textToPrint);
	        int nbLines = textToPrint.length() / MAX_SIZE_OF_TEXT;

	        int i;
	        for(i=0; i<nbLines; i++)
	        {
	        	addLineForMessage(textToPrint.substring(i * MAX_SIZE_OF_TEXT, i * MAX_SIZE_OF_TEXT + MAX_SIZE_OF_TEXT),message.getColor());
	            //m_displayMessage.add(new Label(textToPrint.substring(i * MAX_SIZE_OF_TEXT, i * MAX_SIZE_OF_TEXT + MAX_SIZE_OF_TEXT)),m_nbMessage,0);
	            //m_nbMessage++;
	        }

	        addLineForMessage(textToPrint.substring(i * MAX_SIZE_OF_TEXT),message.getColor());

	        //m_displayMessage.add(new Label(textToPrint.substring(i * MAX_SIZE_OF_TEXT)),m_nbMessage,0);
	        //m_nbMessage++;

	        //System.out.println("DEBUG :: nb message on display :"+m_nbMessage);
	        //System.out.println("DEBUG :: value of display : "+m_displayMessage.toString());

        } catch (RemoteException e) {
            //do nothing
        } catch (NullPointerException e) {
            System.out.println("Impossible to add new message");
            e.printStackTrace();
        }        
    }

    public void addLineForMessage(final String textForLine,  Color color){

	    Platform.runLater(new Runnable() {
	        @Override
	        public void run() {
	            try{
	            	System.out.println("DEBUG::Add new line");
	            	//Label label = new Label(textForLine);
	            	//label.setTextFill(color);
					m_displayMessage.add(new Label(textForLine),0,m_nbMessage);
					m_nbMessage++;
	            }    
	            catch (Exception e){

	            }  
	        }
	    });
    }

    public String getName(){
    	return m_name;
    }

    /* Methode for Pattern Observer */

    public void update() {
        //this.updateUI();
    }

    //Implémentation du pattern observer
    public void addObserver(Observer obs) {
        this.listObserver.add(obs);
    }

    public void notifyObserver(Channel chan, String str) {
        for(Observer obs : listObserver) {
            obs.update(chan, str);
        }
    }

    public void removeObserver() {
        listObserver = new ArrayList<Observer>();
    }
}