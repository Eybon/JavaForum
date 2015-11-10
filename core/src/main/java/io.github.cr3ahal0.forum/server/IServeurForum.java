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

    public boolean auth(IClientForum token, String username, String password) throws RemoteException;

    public void disconnect(String username) throws RemoteException;

    public Map<Integer, ISujetDiscussion> list() throws RemoteException;

    public ISujetDiscussion join(Integer id) throws RemoteException;

    public ICommandFeedback handleCommand(String cmd, String user, ISujetDiscussion topic) throws RemoteException;

    public boolean add(String title, String owner) throws RemoteException;

	public boolean delete(Integer topic, String owner) throws RemoteException;

}
