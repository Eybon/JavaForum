package io.github.cr3ahal0.forum.server;

import io.github.cr3ahal0.forum.server.exceptions.UnknownContentKindException;
import io.github.cr3ahal0.forum.server.impl.broadcast.HistoryAction;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

/**
 * Created by Maxime on 21/11/2015.
 */
public interface BroadcastEndpoint extends Remote, Serializable {

    public Set<HistoryAction> getHistory() throws RemoteException;

    public void addServer(String url) throws RemoteException;

    public void acknowledgeServer(BroadcastEndpoint server) throws RemoteException;

    public CRUDResult broadcast(HistoryAction history) throws RemoteException;

    public CRUDResult handleHistory(HistoryAction history) throws RemoteException, UnknownContentKindException;

    public RepositoryHandler getHandler() throws RemoteException;

    public IServeurForum getParent() throws RemoteException;

}
