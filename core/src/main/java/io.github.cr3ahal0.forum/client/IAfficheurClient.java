package io.github.cr3ahal0.forum.client;

import io.github.cr3ahal0.forum.server.IMessage;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Maxime on 22/09/2015.
 */
public interface IAfficheurClient extends Remote {

    public void afficher (IMessage message) throws RemoteException;

    public void clear () throws RemoteException;

}
