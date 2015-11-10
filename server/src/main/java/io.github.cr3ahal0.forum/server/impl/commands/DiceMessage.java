package io.github.cr3ahal0.forum.server.impl.commands;

import io.github.cr3ahal0.forum.server.IMessage;
import io.github.cr3ahal0.forum.server.ISujetDiscussion;
import io.github.cr3ahal0.forum.server.impl.Message;

import java.awt.*;
import java.rmi.RemoteException;
import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Created by Maxime on 02/10/2015.
 */
public class DiceMessage extends Message {

    public DiceMessage(String content, String username, ISujetDiscussion chanel) throws RemoteException {
        super(content, username, chanel);
    }

    public DiceMessage(Integer id, String username, String content, Date date, ISujetDiscussion chanel) throws RemoteException {
        super(id, username, content, date, chanel);
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
