package io.github.cr3ahal0.forum.client.commands;

import io.github.cr3ahal0.forum.server.IMessage;
import io.github.cr3ahal0.forum.server.ISujetDiscussion;

import java.awt.*;
import java.rmi.RemoteException;
import java.sql.Date;
import java.time.LocalDateTime;

/**
 * Created by Maxime on 14/10/2015.
 */
public class ErrorMessage implements IMessage {

    private String content;

    public ErrorMessage(String content) {
        this.content = content;
    }

    @Override
    public String getContent() throws RemoteException {
        return content;
    }

    @Override
    public String getString() throws RemoteException {
        return content;
    }

    @Override
    public Color getColor() throws RemoteException {
        return Color.RED;
    }

    @Override
    public LocalDateTime getDate() throws RemoteException {
        return null;
    }

    @Override
    public String getUsername() throws RemoteException {
        return "SYSTEM";
    }

    @Override
    public String getId() throws RemoteException {
        return null;
    }

    @Override
    public ISujetDiscussion getChanel() throws RemoteException {
        return null;
    }
}
