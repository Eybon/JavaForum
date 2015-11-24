package io.github.cr3ahal0.forum.server;

import io.github.cr3ahal0.forum.client.IAfficheurClient;
import io.github.cr3ahal0.forum.client.IClientForum;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Created by Maxime on 22/09/2015.
 */
public interface IServeurForum extends Remote {

    public String getUrl() throws RemoteException;

    public String getPort() throws RemoteException;

    public boolean auth(IClientForum token, String username, String password) throws RemoteException;

    public void disconnect(String username) throws RemoteException;

    public Map<String, ISujetDiscussion> list() throws RemoteException;

    public ISujetDiscussion join(String id) throws RemoteException;

    public ICommandFeedback handleCommand(String cmd, String user, ISujetDiscussion topic) throws RemoteException;

    public ServeurResponse add(String title, String owner) throws RemoteException;

    public ServeurResponse addMessage(ISujetDiscussion topic, LocalDateTime date, String content, String author) throws RemoteException;

    public ServeurResponse join(ISujetDiscussion topic, IAfficheurClient client) throws RemoteException;

	public boolean delete(String topic, String owner) throws RemoteException;

    /*
    public boolean broadcastNewTopic(String title, String owner) throws RemoteException;

    public boolean broadcastNewMessage(ISujetDiscussion topic, Date date, String content, String author) throws RemoteException;
    */
}
