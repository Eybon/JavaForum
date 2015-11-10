package io.github.cr3ahal0.forum.server;

import io.github.cr3ahal0.forum.client.IAfficheurClient;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Created by Maxime on 22/09/2015.
 */
public interface ISujetDiscussion extends Remote {

    public void diffuser(String content, String author) throws RemoteException;

    public void diffuser(IMessage m) throws RemoteException;

    public String getTitle() throws RemoteException;

	public String getOwner() throws RemoteException;

    public boolean join(IAfficheurClient client) throws RemoteException;

    public boolean leave(IAfficheurClient client) throws RemoteException;

	public List<IAfficheurClient> getAfficheurs() throws RemoteException;

}
