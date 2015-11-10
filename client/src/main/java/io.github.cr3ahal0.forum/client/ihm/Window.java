package io.github.cr3ahal0.forum.client.ihm;

import io.github.cr3ahal0.forum.client.impl.ClientForum;
import io.github.cr3ahal0.forum.server.ISujetDiscussion;

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
import javafx.scene.control.ListView;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

import java.util.*;
import java.rmi.RemoteException;

public class Window extends Application {

	final static String NAME_OF_GLOBAL_CHAT = "GlobalChat";

	private Stage m_primaryStage;
	private ClientForum m_clientForumParent;

	/* Zone Channel */
	private TabPane m_tabPane;
	private ArrayList<Channel> m_listTab;
	private int m_nbChannel;

	/* Zone Tools */
	private GridPane m_toolZone;
	private ListView<String> m_listOfChannel;
	private Button addBtn;
	
	public Window(ClientForum parent){
		m_clientForumParent = parent;
	}

	public void createForum(){
		m_tabPane = new TabPane();
		m_listTab = new ArrayList<Channel>();
		m_nbChannel = 0;

		/* Configuration of TabPane */
		m_tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
		m_tabPane.setMinWidth(700);
		m_tabPane.setMaxWidth(700);
		m_tabPane.setMinHeight(600);
		m_tabPane.setMaxHeight(600);

		createListOfChannel();
		createPanelTools();

		m_clientForumParent.operationOnChannelWithName(NAME_OF_GLOBAL_CHAT,true);

	}

	public boolean subscribe(String nameOfChannel){
		for(int i=0;i<m_nbChannel;i++){
			if(nameOfChannel.equals(m_listTab.get(i).getName()) ){
				return true;
			}
		}
		return false;
	}


	public void createPanelTools(){
		/* List of Channel Zone */
		m_listOfChannel = new ListView<String>();
	    m_listOfChannel.setOnMouseClicked(new EventHandler<MouseEvent>() {
	        @Override
	        public void handle(MouseEvent event) {
	        	String nameOfChannel = m_listOfChannel.getSelectionModel().getSelectedItem();
	            //System.out.println("clicked on " + nameOfChannel);
	        	if(nameOfChannel.equals(NAME_OF_GLOBAL_CHAT)){
	        		addBtn.setVisible(false);
	        	}
	        	else{
	        		addBtn.setVisible(true);
		            if(subscribe(nameOfChannel)){
		            	addBtn.setText("Se désabonner");
		            }
		            else{
		            	addBtn.setText("S'abonner");
		            }
	        	}

	        }
	    });

		m_listOfChannel.setMinWidth(200);
		m_listOfChannel.setMaxWidth(200);
		m_listOfChannel.setMinHeight(500);
		m_listOfChannel.setMaxHeight(500);

		/* Button */
		addBtn = new Button("S'abonner");
		addBtn.setPrefWidth(200);
		addBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				//m_clientForumParent.joinChannel("/join 2");
				String nameOfChannel = m_listOfChannel.getSelectionModel().getSelectedItem();
				//System.out.println("DEBUG :: Choix du channel : -"+value+"-");
	            if(!subscribe(nameOfChannel)){
	            	m_clientForumParent.operationOnChannelWithName(nameOfChannel,true);
	            	addBtn.setText("Se désabonner");
	            }
	            else{
	            	m_clientForumParent.operationOnChannelWithName(nameOfChannel,false);
	            	addBtn.setText("S'abonner");
	            }				
			}
		});	
		//Hide button on start
		addBtn.setVisible(false);	

		/* Main Panel */
		m_toolZone = new GridPane();
		Pane blank1 = new Pane();
		m_toolZone.add(blank1,1,0);
		Pane blank2 = new Pane();
		m_toolZone.add(blank2,1,1);		
		m_toolZone.add(m_listOfChannel,1,2);
		m_toolZone.add(addBtn,1,3);

		m_toolZone.setHgap(50);
		m_toolZone.setVgap(20);

	}

	public void createListOfChannel(){		
		for(int i = 0;i<m_listTab.size();i++){
			m_tabPane.getTabs().add(m_listTab.get(i));
		}
	}

	public void updateListOfChannel(Map<Integer, ISujetDiscussion> topics)
	{
		int nb_topic = topics.size();
		String[] list = new String[nb_topic];

        if(nb_topic > 0)
        {
			System.out.println("nombre de section : "+nb_topic);
            for(int i = 1; i <= nb_topic; i++)
            {  
            	try{
            		list[i-1] = topics.get(i).getTitle();
				}catch (RemoteException e) {
				    e.printStackTrace();
				}            	
            }
        }

        m_listOfChannel.setItems(FXCollections.observableArrayList(list));

	}

	public Channel addNewChannel(String name){

        Channel chan = new Channel(name);
        chan.addObserver(m_clientForumParent);

        m_listTab.add(chan);
        m_tabPane.getTabs().add(chan);

        m_nbChannel++;

        return chan;
	}

    public void removeChannel(Channel chan) {

    	m_listTab.remove(chan);
    	m_tabPane.getTabs().removeAll(chan);
    	m_nbChannel--;

    }

	public void initializeForTest(){
		m_listTab.add(new Channel("tab 1"));
		m_listTab.add(new Channel("tab 2"));
		m_listTab.add(new Channel("tab 3"));
		m_listTab.add(new Channel("tab 4"));
	}


final static String WINDOW_NAME = "Forum#Chat#IRC#Yolo";
final static int WINDOW_HEIGHT = 650;
final static int WINDOW_WIDTH = 1000;


    @Override public void start(Stage primaryStage) throws Exception {

    	createForum();

		/** Pane principal**/
		BorderPane root = new BorderPane();
		root.setRight(m_tabPane);
		root.setLeft(m_toolZone);

		/** Fenetre **/
		final Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
		//scene.getStylesheets().add(MainWindow.class.getResource("MainWindow.css").toExternalForm());

		Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
		primaryStage.setX((screenBounds.getWidth() - WINDOW_WIDTH) / 2);
		primaryStage.setY((screenBounds.getHeight() - WINDOW_HEIGHT) / 2);

		m_primaryStage = primaryStage;
		primaryStage.setTitle(WINDOW_NAME);
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();
    }

    public void setObserver(ClientForum parent){
    	m_clientForumParent = parent;
    }

    /**
     * Java main for when running without JavaFX launcher
     */
    /*public static void main(String[] args) {
        launch(args);
    }*/

}