package io.github.cr3ahal0.forum.server;

import io.github.cr3ahal0.forum.client.IClientForum;

import java.rmi.Remote;
import java.rmi.RemoteException;
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

	public boolean delete(String topic, String owner) throws RemoteException;

    public void addServer(String url) throws RemoteException;

    public void acknowledgeServer(IServeurForum server) throws RemoteException;

    public boolean broadcast(String title, String owner) throws RemoteException;

}
