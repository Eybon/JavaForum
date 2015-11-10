package io.github.cr3ahal0.forum.client.impl;


import io.github.cr3ahal0.forum.client.IAfficheurClient;
import io.github.cr3ahal0.forum.client.ihm.Channel;
import io.github.cr3ahal0.forum.server.IMessage;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by Maxime on 22/09/2015.
 */
public class AfficheurClient extends UnicastRemoteObject implements IAfficheurClient {

    private Channel channel;

    public AfficheurClient() throws RemoteException {

    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void afficher(IMessage message) throws RemoteException {
        //System.out.println("[" + message.getChanel().getTitle() + "] " + message.getUsername() + " : " + message.getContent());
        this.channel.addNewMessage(message);
    }

    @Override
    public void clear() throws RemoteException {

    }
}
