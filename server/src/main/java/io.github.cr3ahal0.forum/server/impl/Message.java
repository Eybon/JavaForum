package io.github.cr3ahal0.forum.server.impl;

import io.github.cr3ahal0.forum.LocalDateTimeAdapter;
import io.github.cr3ahal0.forum.server.IMessage;
import io.github.cr3ahal0.forum.server.ISujetDiscussion;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import java.awt.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Date;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by Maxime on 24/09/2015.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class Message extends UnicastRemoteObject implements IMessage, Comparable<Message>, Serializable{

    private String id;

    protected String username;

    protected String content;

    protected LocalDateTime date;

    private SujetDiscussion chanel;

    public Message() throws RemoteException {

    }

    public Message (String content, String username, SujetDiscussion chanel) throws RemoteException {
        this.username = username;
        this.content = content;
        this.chanel = chanel;
    }

    public Message (String id, String username, String content, LocalDateTime date, SujetDiscussion chanel) throws RemoteException {
        this.id = id;
        this.username = username;
        this.content = content;
        this.date = date;
        this.chanel = chanel;
    }

    public String getId() throws RemoteException {
        return id;
    }


    @XmlElement
    public void setId(String id) throws RemoteException {
        this.id = id;
    }

    public String getUsername() throws RemoteException {
        return username;
    }

    @XmlElement
    public void setUsername(String username) throws RemoteException {
        this.username = username;
    }

    public String getContent() throws RemoteException {
        return content;
    }

    @XmlElement
    public void setContent(String content) throws RemoteException {
        this.content = content;
    }

    public LocalDateTime getDate() throws RemoteException {
        return date;
    }

    @XmlJavaTypeAdapter(type=LocalDateTime.class, value=LocalDateTimeAdapter.class)
    @XmlElement
    public void setDate(LocalDateTime date) throws RemoteException {
        this.date = date;
    }

    public SujetDiscussion getChanel() throws RemoteException {
        return chanel;
    }

    @XmlElement
    public void setChanel(SujetDiscussion chanel) throws RemoteException {
        this.chanel = chanel;
    }

    @Override
    public int compareTo(Message o) {
        try {
            if (o.getDate().isAfter(date)) {
                return -1;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public String getString() throws RemoteException {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            String date = getDate().format(formatter);
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
