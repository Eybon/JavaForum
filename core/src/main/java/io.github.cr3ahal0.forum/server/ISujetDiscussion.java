package io.github.cr3ahal0.forum.server;

import io.github.cr3ahal0.forum.client.IAfficheurClient;

import java.io.UnsupportedEncodingException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by Maxime on 22/09/2015.
 */
public interface ISujetDiscussion extends Remote {

    public ServeurResponse diffuser(LocalDateTime date, String content, String author) throws RemoteException;

    public void diffuser(IMessage m) throws RemoteException;

    public String getId() throws RemoteException;

    public String getTitle() throws RemoteException;

	public String getOwner() throws RemoteException;

    public boolean join(IAfficheurClient client) throws RemoteException;

    public boolean leave(IAfficheurClient client) throws RemoteException;

	public List<IAfficheurClient> getAfficheurs() throws RemoteException;

    public IMessage getMessage(String key) throws RemoteException;

    public String getMessageKey(String author, LocalDateTime date) throws RemoteException, UnsupportedEncodingException, NoSuchAlgorithmException;

}
