package io.github.cr3ahal0.forum.server.impl;

import io.github.cr3ahal0.forum.server.ICommandFeedback;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by Maxime on 02/10/2015.
 */
public class CommandFeedback extends UnicastRemoteObject implements ICommandFeedback, Serializable {

    private boolean isValid;

    private String message;

    private boolean isDisplayRequired;

    public CommandFeedback() throws RemoteException {
        this("", true, false);
    }

    public CommandFeedback(String message) throws RemoteException {
        this(message, true);
    }

    public CommandFeedback(String message, boolean isValid) throws RemoteException {
        this(message, isValid, false);
    }

    public CommandFeedback(String message, boolean isValid, boolean isDisplayRequired) throws RemoteException {
        this.isValid = isValid;
        this.message = message;
        this.isDisplayRequired = isDisplayRequired;
    }

    @Override
    public void setValid(boolean valid) throws RemoteException {
        isValid = valid;
    }

    @Override
    public boolean isValid() throws RemoteException {
        return isValid;
    }

    @Override
    public void setMessage(String msg) throws RemoteException {
        this.message = msg;
    }

    @Override
    public String getMessage() throws RemoteException  {
        return message;
    }

    @Override
    public boolean isDisplayRequired() throws RemoteException {
        return isDisplayRequired;
    }

    @Override
    public void setDisplayRequired(boolean isDisplayRequired) throws RemoteException {
        this.isDisplayRequired = isDisplayRequired;
    }
}
