package io.github.cr3ahal0.forum.server.impl.commands;

import io.github.cr3ahal0.forum.server.IMessage;
import io.github.cr3ahal0.forum.server.ISujetDiscussion;
import io.github.cr3ahal0.forum.server.impl.Message;
import io.github.cr3ahal0.forum.server.impl.SujetDiscussion;

import java.awt.*;
import java.rmi.RemoteException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

/**
 * Created by Maxime on 02/10/2015.
 */
public class DiceMessage extends Message {

    public DiceMessage(String content, String username, SujetDiscussion chanel) throws RemoteException {
        super(content, username, chanel);
    }

    public DiceMessage(String id, String username, String content, LocalDateTime date, SujetDiscussion chanel) throws RemoteException {
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
