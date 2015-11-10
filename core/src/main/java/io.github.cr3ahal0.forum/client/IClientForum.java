package io.github.cr3ahal0.forum.client;

import io.github.cr3ahal0.forum.server.ISujetDiscussion;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * Created by Maxime on 22/09/2015.
 */
public interface IClientForum extends Remote, Serializable {

    public void updateTopics(Map<Integer, ISujetDiscussion> topics) throws RemoteException;

	public void notifyShutdown() throws RemoteException;

}
