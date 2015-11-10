package io.github.cr3ahal0.forum.server;

import java.awt.*;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Maxime on 14/10/2015.
 */
public interface IMessage extends Remote {

    public String getString() throws RemoteException;

    public Color getColor() throws RemoteException;
}
