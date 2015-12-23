package io.github.cr3ahal0.forum.server.impl.broadcast;

import io.github.cr3ahal0.forum.server.BroadcastEndpoint;
import io.github.cr3ahal0.forum.server.CRUDResult;
import io.github.cr3ahal0.forum.server.IServeurForum;
import io.github.cr3ahal0.forum.server.ServeurResponse;

import java.rmi.RemoteException;

/**
 * Created by Maxime on 22/11/2015.
 */
public class BroadcastOperation implements Runnable {

    BroadcastEndpoint remoteServer;

    HistoryAction action;

    boolean alive = true;

    public BroadcastOperation(BroadcastEndpoint server, HistoryAction action) {
        remoteServer = server;
        this.action = action;
    }

    @Override
    public void run() {
        CRUDResult end = CRUDResult.ERROR;
        int attempts = 5;

        while (end.equals(CRUDResult.ERROR) && attempts > 0) {
            try {

                System.out.println("Attempt to notify server " + remoteServer.getParent().getUrl() + ":" + remoteServer.getParent().getPort() +" ...");
                end = remoteServer.broadcast(action);

                if (end.equals(CRUDResult.OK) || end.equals(CRUDResult.UNKNOWN)) {
                    System.out.println("Server " + remoteServer.getParent().getUrl() + ":" + remoteServer.getParent().getPort() + " has been notified correctly");
                }
                else if (end.equals(CRUDResult.KO)) {
                    System.out.println("Server "+ remoteServer.getParent().getUrl() + ":" + remoteServer.getParent().getPort() + " already knew this history");
                }
                else
                {
                    attempts--;
                    System.out.println("Server seems to be unavailable or there must be errors occuring. " + attempts + " attempts left");
                    Thread.sleep(2000);
                }

            } catch (RemoteException e) {
                attempts--;
                if (attempts > 0) {
                    System.out.println("Server seems to be unavailable. " + attempts + " attempts left");
                    e.printStackTrace();
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
