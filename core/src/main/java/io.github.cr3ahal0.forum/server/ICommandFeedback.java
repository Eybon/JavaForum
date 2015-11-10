package io.github.cr3ahal0.forum.server;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Maxime on 02/10/2015.
 */
public interface ICommandFeedback extends Remote {

    public void setValid(boolean valid) throws RemoteException;

    public boolean isValid() throws RemoteException;

    public void setMessage(String msg) throws RemoteException;

    public String getMessage() throws RemoteException;

    public boolean isDisplayRequired() throws RemoteException;

    public void setDisplayRequired(boolean required) throws RemoteException;

}
