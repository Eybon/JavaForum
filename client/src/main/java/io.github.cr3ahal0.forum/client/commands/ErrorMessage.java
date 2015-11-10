package io.github.cr3ahal0.forum.client.commands;

import io.github.cr3ahal0.forum.server.IMessage;

import java.awt.*;
import java.rmi.RemoteException;

/**
 * Created by Maxime on 14/10/2015.
 */
public class ErrorMessage implements IMessage {

    private String content;

    public ErrorMessage(String content) {
        this.content = content;
    }

    @Override
    public String getString() throws RemoteException {
        return content;
    }

    @Override
    public Color getColor() throws RemoteException {
        return Color.RED;
    }
}
