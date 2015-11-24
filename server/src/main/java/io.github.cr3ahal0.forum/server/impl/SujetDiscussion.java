package io.github.cr3ahal0.forum.server.impl;

import com.sun.org.apache.xerces.internal.parsers.CachingParserPool;
import com.sun.org.apache.xpath.internal.SourceTree;
import io.github.cr3ahal0.forum.client.IAfficheurClient;
import io.github.cr3ahal0.forum.server.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Maxime on 22/09/2015.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class SujetDiscussion extends UnicastRemoteObject implements ISujetDiscussion, DataRepository<IMessage, String> {

    private String id;

    private String title;

	private String owner;

    List<IMessage> messages;

    Map<String, IMessage> identifiers;

    private List<IAfficheurClient> abonnes;

    @XmlElement
    public void setId(String id) {
        this.id = id;
    }

    @XmlElement
    public void setTitle(String title) {
        this.title = title;
    }

    @XmlElement
    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getTitle() throws RemoteException {
        return title;
    }

	public String getOwner() throws RemoteException {
		return owner;
	}

	public List<IAfficheurClient> getAfficheurs() throws RemoteException {
		return abonnes;
	}

    public SujetDiscussion() throws RemoteException {
        abonnes = Collections.synchronizedList(new ArrayList<IAfficheurClient>());
        messages = Collections.synchronizedList(new ArrayList<IMessage>());
        identifiers = new ConcurrentHashMap<String, IMessage>();
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
    public ServeurResponse diffuser(LocalDateTime date, String content, String author) throws RemoteException {
        System.out.println("Message diffusé aux " + abonnes.size() + " participants : " + content);

        try {
            Message message = new Message(content, author, this);
            message.setDate(date);

            CRUDResult response = add(message);
            if (!response.equals(CRUDResult.OK)) {
                if (response.equals(CRUDResult.KO)) {
                    return ServeurResponse.MESSAGE_KNOWN;
                }
                return ServeurResponse.ERROR;
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

    @Override
    public IMessage get(String id) {
        return identifiers.get(id);
    }

    @Override
    public CRUDResult add(IMessage message) {

        try {
            if (!has(message)) {
                identifiers.put(message.getId(), message);
                messages.add(message);

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

                return CRUDResult.OK;
            }
            return CRUDResult.KO;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return CRUDResult.ERROR;
    }

    /*
    public ServeurResponse add(IMessage message) {

        try {
            String key = getMessageKey(message);

            IMessage test = getMessage(key);
            if (test != null) {
                return ServeurResponse.MESSAGE_KNOWN;
            }

            System.out.println("Message not known, adding it to the list");

            identifiers.put(key, message);
            messages.add(message);

            return ServeurResponse.MESSAGE_UNKNOWN;
        } catch (RemoteException | NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return ServeurResponse.ERROR;
        }

    }
*/
    @Override
    public CRUDResult remove(IMessage object) {
        //TODO implement message removal
        return CRUDResult.KO;
    }

    @Override
    public boolean has(IMessage object) {
        try {
            return (identifiers.get(object.getId()) != null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }


    public String getMessageKey(String author, LocalDateTime date) throws RemoteException, UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest encryption = null;

        byte[] bytesUserKey = author.getBytes("UTF-8");
        long time = date.getNano();
        String dateS = String.valueOf(time);
        byte[] bytesDateKey = dateS.getBytes("UTF-8");
        byte[] byteKey = (getId() + author + dateS).getBytes("UTF-8");

        encryption = MessageDigest.getInstance("MD5");
        final BigInteger bigint = new BigInteger(1, encryption.digest(byteKey));
        String key = String.format("%032x", bigint);

        return key;
    }

    public IMessage getMessage(String key) throws RemoteException {
        return identifiers.get(key);
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
