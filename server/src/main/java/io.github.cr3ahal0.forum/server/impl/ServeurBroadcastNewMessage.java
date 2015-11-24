package io.github.cr3ahal0.forum.server.impl;

import io.github.cr3ahal0.forum.server.IServeurForum;
import io.github.cr3ahal0.forum.server.ISujetDiscussion;
import io.github.cr3ahal0.forum.server.ServeurResponse;
import org.codehaus.jackson.map.deser.std.DateDeserializer;

import java.rmi.RemoteException;
import java.sql.Date;
import java.time.LocalDateTime;

/**
 * Created by Maxime on 11/11/2015.
 */
public class ServeurBroadcastNewMessage implements Runnable {

    IServeurForum remoteServer;

    ISujetDiscussion topic;

    LocalDateTime date;

    String content;

    String author;

    boolean alive = true;

    public ServeurBroadcastNewMessage(IServeurForum server, ISujetDiscussion topic, LocalDateTime date, String content, String author) {
        remoteServer = server;
        this.topic = topic;
        this.date = date;
        this.content = content;
        this.author = author;
    }

    @Override
    public void run() {
        ServeurResponse end = ServeurResponse.ERROR;
        int attempts = 5;

        while (end.equals(ServeurResponse.ERROR) && attempts > 0) {
            try {

                System.out.println("Attempt to notify server "+ remoteServer.getUrl() +":"+ remoteServer.getPort() +" ...");
                //end = remoteServer.addMessage(topic, date, content, author);

                if (end.equals(ServeurResponse.MESSAGE_UNKNOWN)) {
                    System.out.println("Server " + remoteServer.getUrl() + ":" + remoteServer.getPort() + " has been notified correctly");
                }
                else if (end.equals(ServeurResponse.MESSAGE_KNOWN)) {
                    System.out.println("Server "+ remoteServer.getUrl() + ":" + remoteServer.getPort() + " already knew this message");
                }
                else
                {
                    attempts--;
                    System.out.println("Server seems to be unavailable. " + attempts + " attempts left");
                    Thread.sleep(2000);
                }

            } catch (RemoteException e) {
                attempts--;
                if (attempts > 0) {
                    System.out.println("Server seems to be unavailable. " + attempts + " attempts left");
                }
                else
                {
                    alive = false;
                    System.out.println("Server may be dead...");
                    return;
                }
            } catch (InterruptedException e1) {
                System.out.println("Interrupted thread");
            }
        }
    }

    public boolean isAlive() {
        return alive;
    }

}
