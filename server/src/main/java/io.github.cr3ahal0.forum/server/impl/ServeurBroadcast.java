package io.github.cr3ahal0.forum.server.impl;

import io.github.cr3ahal0.forum.server.IServeurForum;
import io.github.cr3ahal0.forum.server.ServeurResponse;

import java.rmi.RemoteException;

/**
 * Created by Maxime on 11/11/2015.
 */
public class ServeurBroadcast implements Runnable {

    IServeurForum remoteServer;

    String title;

    String owner;

    boolean alive = true;

    public ServeurBroadcast(IServeurForum server, String title, String owner) {
        remoteServer = server;
        this.title = title;
        this.owner = owner;
    }

    @Override
    public void run() {
        ServeurResponse end = ServeurResponse.ERROR;
        int attempts = 5;

        while (end.equals(ServeurResponse.ERROR) && attempts > 0) {
            try {

                System.out.println("Attempt to notify server "+ remoteServer.getUrl() +":"+ remoteServer.getPort() +" ...");
                end = remoteServer.add(title, owner);

                if (end.equals(ServeurResponse.TOPIC_UNKNOWN)) {
                    System.out.println("Server " + remoteServer.getUrl() + ":" + remoteServer.getPort() + " has been notified correctly");
                }
                else if (end.equals(ServeurResponse.TOPIC_KNOWN)) {
                    System.out.println("Server "+ remoteServer.getUrl() + ":" + remoteServer.getPort() + " already knew this topic");
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
