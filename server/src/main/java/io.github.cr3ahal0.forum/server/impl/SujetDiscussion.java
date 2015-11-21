package io.github.cr3ahal0.forum.server.impl;

import com.sun.org.apache.xerces.internal.parsers.CachingParserPool;
import com.sun.org.apache.xpath.internal.SourceTree;
import io.github.cr3ahal0.forum.client.IAfficheurClient;
import io.github.cr3ahal0.forum.server.IMessage;
import io.github.cr3ahal0.forum.server.ISujetDiscussion;
import io.github.cr3ahal0.forum.server.ServeurResponse;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Maxime on 22/09/2015.
 */
public class SujetDiscussion extends UnicastRemoteObject implements ISujetDiscussion {

    private String id;

    private String title;

	private String owner;

    List<IMessage> messages;

    Map<String, IMessage> identifiers;

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
		this (title, null, title);
	}

    public SujetDiscussion(String title, String owner, String id) throws RemoteException {
        this.id = id;
        this.title = title;
		this.owner = owner;
        abonnes = Collections.synchronizedList(new ArrayList<IAfficheurClient>());
        messages = Collections.synchronizedList(new ArrayList<IMessage>());
        identifiers = new ConcurrentHashMap<String, IMessage>();
    }

    @Override
    public ServeurResponse diffuser(Date date, String content, String author) throws RemoteException {
        System.out.println("Message diffusé aux " + abonnes.size() + " participants : " + content);

        try {
            Message message = new Message(content, author, this);
            message.setDate(date);

            ServeurResponse response = add(message);
            if (!response.equals(ServeurResponse.MESSAGE_UNKNOWN)) {
                return response;
            }

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

            return ServeurResponse.MESSAGE_UNKNOWN;

        } catch (RemoteException e) {
            System.out.println("Unable to build message");
            e.printStackTrace();
        }

        return ServeurResponse.ERROR;
    }

    public ServeurResponse add(IMessage message) {
        MessageDigest encryption = null;
        try {
            byte[] bytesTopicsKey = getId().getBytes("UTF-8");
            byte[] bytesUserKey = message.getUsername().getBytes("UTF-8");
            Date date = message.getDate();
            long time = date.getTime();
            String dateS = String.valueOf(time);
            byte[] bytesDateKey = dateS.getBytes("UTF-8");

            encryption = MessageDigest.getInstance("MD5");
            String topicKey =  new String(encryption.digest(bytesTopicsKey),  StandardCharsets.UTF_8);
            String userKey = new String(encryption.digest(bytesUserKey),  StandardCharsets.UTF_8);
            String dateKey = new String(encryption.digest(bytesDateKey),  StandardCharsets.UTF_8);
            String key = topicKey + userKey + dateKey;

            System.out.println(topicKey +" "+ userKey + " " + dateKey);

            System.out.println("Calculated key for message : "+ key);

            if (identifiers.get(key) != null) {
                return ServeurResponse.MESSAGE_KNOWN;
            }

            System.out.println("Message not known, adding it to the list");

            identifiers.put(key, message);
            messages.add(message);

            return ServeurResponse.MESSAGE_UNKNOWN;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return ServeurResponse.ERROR;
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
    public String getId() throws RemoteException {
        return id;
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
