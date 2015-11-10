package io.github.cr3ahal0.forum.server.impl;

import io.github.cr3ahal0.forum.server.IMessage;
import io.github.cr3ahal0.forum.server.ISujetDiscussion;

import java.awt.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Date;
import java.io.Serializable;
import java.text.SimpleDateFormat;

/**
 * Created by Maxime on 24/09/2015.
 */
public class Message extends UnicastRemoteObject implements IMessage, Comparable<Message>, Serializable{

    private Integer id;

    protected String username;

    protected String content;

    protected Date date;

    private ISujetDiscussion chanel;

    public Message (String content, String username, ISujetDiscussion chanel) throws RemoteException {
        this.username = username;
        this.content = content;
        this.chanel = chanel;
    }

    public Message (Integer id, String username, String content, Date date, ISujetDiscussion chanel) throws RemoteException {
        this.id = id;
        this.username = username;
        this.content = content;
        this.date = date;
        this.chanel = chanel;
    }

    public Integer getId() throws RemoteException {
        return id;
    }

    public void setId(Integer id) throws RemoteException {
        this.id = id;
    }

    public String getUsername() throws RemoteException {
        return username;
    }

    public void setUsername(String username) throws RemoteException {

    }

    public String getContent() throws RemoteException {
        return content;
    }

    public void setContent(String content) throws RemoteException {
        this.content = content;
    }

    public Date getDate() throws RemoteException {
        return date;
    }

    public void setDate(Date date) throws RemoteException {
        this.date = date;
    }

    public ISujetDiscussion getChanel() throws RemoteException {
        return chanel;
    }

    public void setChanel(ISujetDiscussion chanel) throws RemoteException {
        this.chanel = chanel;
    }

    @Override
    public int compareTo(Message o) {
        try {
            if (o.getDate().after(date)) {
                return -1;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public String getString() throws RemoteException {
        try {
            String date = new SimpleDateFormat("HH:mm").format(getDate());
            return "[" + date + "]" + " " + "<" + getUsername() + "> " + getContent();
        } catch (RemoteException e) {
            System.out.println("Unable to print message string");
            return "";
        }
    }

    @Override
    public Color getColor() throws RemoteException {
        return Color.BLACK;
    }
}
