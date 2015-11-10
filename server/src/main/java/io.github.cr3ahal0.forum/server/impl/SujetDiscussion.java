package io.github.cr3ahal0.forum.server.impl;

import io.github.cr3ahal0.forum.client.IAfficheurClient;
import io.github.cr3ahal0.forum.server.IMessage;
import io.github.cr3ahal0.forum.server.ISujetDiscussion;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Maxime on 22/09/2015.
 */
public class SujetDiscussion extends UnicastRemoteObject implements ISujetDiscussion {

    private String title;

	private String owner;

    private List<IAfficheurClient> abonnes;

    public String getTitle() throws RemoteException {
        return title;
    }

	public String getOwner() throws RemoteException {
		return owner;
	}

	public List<IAfficheurClient> getAfficheurs() throws RemoteException {
		return abonnes;
	}

	public SujetDiscussion(String title) throws RemoteException {
		this (title, null);
	}

    public SujetDiscussion(String title, String owner) throws RemoteException {
        this.title = title;
		this.owner = owner;
        abonnes = new ArrayList<IAfficheurClient>();
    }

    @Override
    public void diffuser(String content, String author) throws RemoteException {
        System.out.println("Message diffusé aux " + abonnes.size() + " participants : " + content);

        try {
            Message message = new Message(content, author, this);
            Calendar cal = Calendar.getInstance();
            message.setDate(new Date(cal.getTime().getTime()));

            for (IAfficheurClient abonne : abonnes) {
                try {
                    abonne.afficher(message);
                } catch (RemoteException e) {
                    System.out.println("Unable to deliver message to one client");
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    System.out.println("Unable to deliver message to one client caused by NullPointerException");
                    e.printStackTrace();
                }
            }

            System.out.println("DEBUG :: Message deliver to all clients");

        } catch (RemoteException e) {
            System.out.println("Unable to build message");
            e.printStackTrace();
        }
    }

    public void diffuser(IMessage m) throws RemoteException {
        System.out.println("Message diffusé aux " + abonnes.size() + " participants : " + m.getString());


        for (IAfficheurClient abonne : abonnes) {
            try {
                abonne.afficher(m);
            } catch (RemoteException e) {
                System.out.println("Unable to deliver message to one client");
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean join(IAfficheurClient client) throws RemoteException {
	    if (!abonnes.contains(client)) {
	        System.out.println("Someone has joined the chanel " + title);
	        abonnes.add(client);
	        return true;
	    }
	    return false;
    }

    @Override
    public boolean leave(IAfficheurClient client) throws RemoteException {
		System.out.println("Try to disconnect client");
        if (abonnes.contains(client)) {
            System.out.println("deconnexion");
            abonnes.remove(client);
            return true;
        }
        return false;
    }

}
