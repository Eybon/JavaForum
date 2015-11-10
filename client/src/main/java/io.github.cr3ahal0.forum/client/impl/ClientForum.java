package io.github.cr3ahal0.forum.client.impl;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import io.github.cr3ahal0.forum.client.IAfficheurClient;
import io.github.cr3ahal0.forum.client.IClientForum;
import io.github.cr3ahal0.forum.client.commands.ErrorMessage;
import io.github.cr3ahal0.forum.client.ihm.Channel;
import io.github.cr3ahal0.forum.client.ihm.Window;
import io.github.cr3ahal0.forum.client.ihm.Launcher;
import io.github.cr3ahal0.forum.client.pattern.Observer;
import io.github.cr3ahal0.forum.server.ICommandFeedback;
import io.github.cr3ahal0.forum.server.IServeurForum;
import io.github.cr3ahal0.forum.server.ISujetDiscussion;

import java.rmi.server.UnicastRemoteObject;
import javax.swing.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.*;
import java.util.List;

import javafx.stage.Stage;
import javafx.application.Platform;

public class ClientForum
		extends UnicastRemoteObject
        implements IClientForum, Observer
{

    private HashMap<Channel, ISujetDiscussion> topics;

    private Map<ISujetDiscussion, IAfficheurClient> displays;

    private Window gui;

    private Thread wt;

    private String login;

    IServeurForum server;

    IServeurForum auth;

    SessionToken token;

    Stage primaryStage;

    private Launcher launcher;


    public ClientForum(Stage primaryStage) throws RemoteException {

        try {

            this.primaryStage = primaryStage;

            auth = (IServeurForum) Naming.lookup("//127.0.0.1:8090/auth");

            server = (IServeurForum) Naming.lookup("//127.0.0.1:8090/list");

            topics = new HashMap<Channel, ISujetDiscussion>();
            displays = new HashMap<ISujetDiscussion, IAfficheurClient>();

        }
        catch(RemoteException e) {
            System.out.println(e.getMessage());
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }


	public boolean tryLogin(String login, String password) throws RemoteException{
        this.login = login;
        return auth.auth(this, login, password);
	}

    ClientForum current;
	public void startWindow()
	{     
        current = this;

        try{
            gui = new Window(current);
            gui.start(primaryStage);
            gui.updateListOfChannel(server.list());
        }    
        catch (Exception e){

        }
	}

    //operation à 1 pour ajouter et à 0 pour supprimer
    public void operationOnChannelWithName(String nameOfChannel, boolean operation){
        Map<Integer, ISujetDiscussion>  top = null;
        try{
            top = server.list();
        }catch(Exception e){ }
        int nb_topic = top.size();

        for(int i = 1; i <= nb_topic; i++)
        {  
            try
            {
                //System.out.println("DEBUG :: topic num"+i+"-"+top.get(i).getTitle()+"-");
                if(nameOfChannel.equals( top.get(i).getTitle()) ){
                    if(operation){
                        joinChannel("/join "+i);
                    }
                    else{
                        exitChannel("/exit "+i);
                    }
                }
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void joinChannel(String command)
    {
        try {        
            Integer id = Integer.valueOf(command.replace("/join ", ""));
            ISujetDiscussion destination = server.join(id);
            if (destination == null) {
                System.out.println("Error : this chanel does not exist !");
            } else {
                boolean opened = false;
                Collection<ISujetDiscussion> rows = topics.values();
                if (rows.contains(destination)) {
                    //This topic is already opened
                }
                else {

                    IAfficheurClient window = new AfficheurClient();

                    boolean test = destination.join(window);

                    if (!test) {
                        System.out.println("Error : failed to join " + destination.getTitle());
                    } else {
                        Channel panel = gui.addNewChannel(destination.getTitle());
                        ((AfficheurClient) window).setChannel(panel);

                        topics.put(panel, destination);
						displays.put(destination, window);

                        System.out.println("Welcome to chanel " + destination.getTitle() + " !");
                    }

                }
            }
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }            
    }


    //Attention rajouter test sur l'existence du chanel !!! 
    public void addChannel(String command){
        try{
            String id = command.replace("/add ", "");
            //ISujetDiscussion destination = server.join(id);
            ISujetDiscussion destination = null;
            if(destination != null){
                System.out.println("Error : this chanel exist !");
            } else {

                boolean test = server.add(id,login);

                if (!test) {
                    System.out.println("Error : failed to add ");
                } else {
                    System.out.println("New Sujet Add");
                }                
            }
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }         
    }

    //Attention rajouter test sur l'existence du chanel !!!   
    public void deleteChannel(String command){
        try{
            int id = Integer.valueOf(command.replace("/delete ", ""));
            //ISujetDiscussion destination = server.join(id);
            ISujetDiscussion destination = null;
            if(destination != null){
                System.out.println("Error : this chanel exist !");
            } else {

                boolean test = server.delete(id,login);

                if (!test) {
                    System.out.println("Error : failed to delete ");
                } else {
                    System.out.println("Sujet Delete");
                }                
            }
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }       
    }

	private void exitChannel(String command)
	{
        try {    
	 		if (command.startsWith("/exit")) 
			{
				
				String strArgs = command.replaceAll("[^0-9]+"," ");
				List<String> list = Arrays.asList(strArgs.trim().split(" "));

				if (list.size() > 0) {
					ISujetDiscussion destination = server.join(Integer.valueOf(list.get(0)));
					if (destination == null) {
						return;
					}
		
					System.out.println("topic found : "+ destination.getTitle());

					Set<Channel> chans = topics.keySet();
					for (Channel c : chans) {
						if (topics.get(c).equals(destination)) {
							System.out.println("GUI chanel found for this topic");

							//we found the chanel
							IAfficheurClient iac = displays.get(destination);
							if (iac == null) {
								System.out.println("No IAfficheurClient found for this topic");
								return;
							}

							boolean test = destination.leave(iac);
							if (test) {
								gui.removeChannel(c);

								topics.remove(c);

								System.out.println("Topic left");
								break;
							}
							else
							{
								System.out.println("Error while leaving !");
							}
						}
                        else{
                            System.out.println("Error : need to follow first");
                        }
					}
				}
			}
		}
        catch (RemoteException e) {
            e.printStackTrace();
        } 

	}


    private void handleCommand(String command, Channel chan) {
        try {
            if (command.startsWith("/join ")) {
                Integer id = Integer.valueOf(command.replace("/join ", ""));
                ISujetDiscussion destination = server.join(id);
                if (destination == null) {
                    System.out.println("Error : this chanel does not exist !");
                } else {
                    boolean opened = false;
                    Collection<ISujetDiscussion> rows = topics.values();
                    if (rows.contains(destination)) {
                        //This topic is already opened
                        //chan.grabFocus();
                    }
                    else {

                        IAfficheurClient window = new AfficheurClient();

                        boolean test = destination.join(window);

                        if (!test) {
                            System.out.println("Error : failed to join " + destination.getTitle());
                        } else {
                            Channel panel = gui.addNewChannel(destination.getTitle());
                            ((AfficheurClient) window).setChannel(panel);

                            topics.put(panel, destination);

                            System.out.println("Welcome to chanel " + destination.getTitle() + " !");
                        }
                    }
                }
            }
            else if(command.startsWith("/add ")){
                addChannel(command);
            }
            else if(command.startsWith("/delete ")){
                deleteChannel(command);
            }
            else if (command.startsWith("/exit")) {
                System.out.println("exit ?");
                if (chan != null) {

                    gui.removeChannel(chan);

                    topics.remove(chan);
                    /*gui.invalidate();
                    gui.validate();
                    gui.repaint();*/

                    System.out.println("pan removed");
                }
                else
                {
                    System.out.println("chan null");
                }
			}
            else
            {
                ISujetDiscussion topic = topics.get(chan);
                ICommandFeedback feedback = server.handleCommand(command, login, topic);

                if (feedback == null || !feedback.isValid()) {
                    if (feedback != null && feedback.isDisplayRequired()) {
                        chan.addNewMessage(new ErrorMessage(feedback.getMessage()));
                    }
                }
                else
                {
                    System.out.println(feedback.getMessage());
                }
            }
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void update(Channel chan, String str) {
        System.out.println("Received command : " + str + " from");
        if (str.startsWith("/")) {
            handleCommand(str, chan);
        }
        else
        {
            if (chan != null) {
                ISujetDiscussion sujet = topics.get(chan);
                try {
                    System.out.println("Value of Message : "+str+" for user : "+login);
                    sujet.diffuser(str, login);
                    System.out.println("Message send to server!!");
                } catch (RemoteException e) {
                    System.out.println("Unable to send the message.");
                } catch (NullPointerException e) {
                    System.out.println("This topic does not exist.");
                }
            }
        }
    }

    public void newTopic() {
            String text = "My topic";
            String input = JOptionPane.showInputDialog("What is the title of your topic ?", text);
            if (input != null && input.length() > 0) {
				try {
                	server.add(input, this.login);
				} catch (RemoteException e) {
            		System.out.println("Unable to fetch RMI server");
        		}
            }
    }

    public void askForChange() {
        try {
			System.out.println("Force Update");
            this.gui.updateListOfChannel(server.list());
        } catch (RemoteException e) {
            System.out.println("Unable to force update");
        }
    }

    public void updateTopics(Map<Integer, ISujetDiscussion> topics) throws RemoteException {
        System.out.println("Server has notified some changes from topics ! Updating...");
        this.gui.updateListOfChannel(topics);
    }

	public void notifyShutdown() throws RemoteException {
		//JOptionPane.showMessageDialog(this.gui, "Server has been shutdown", "Error", JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}


}