package io.github.cr3ahal0.forum.server;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.awt.*;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.Date;
import java.time.LocalDateTime;

/**
 * Created by Maxime on 14/10/2015.
 */
public interface IMessage extends Remote {

    public String getContent() throws RemoteException;

    public String getString() throws RemoteException;

    public Color getColor() throws RemoteException;

    public LocalDateTime getDate() throws RemoteException;

    public String getUsername() throws RemoteException;

    public String getId() throws RemoteException;

    public ISujetDiscussion getChanel() throws RemoteException;

}
