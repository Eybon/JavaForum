package io.github.cr3ahal0.forum.server.impl;

import io.github.cr3ahal0.forum.server.*;
import io.github.cr3ahal0.forum.server.exceptions.UnknownContentKindException;
import io.github.cr3ahal0.forum.server.impl.broadcast.BroadcastOperation;
import io.github.cr3ahal0.forum.server.impl.broadcast.HistoryAction;
import io.github.cr3ahal0.forum.server.impl.broadcast.HistoryHandlerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Maxime on 23/11/2015.
 */
public class ServerEndpoint extends UnicastRemoteObject implements BroadcastEndpoint {

    private static final Logger logger = LogManager.getLogger(ServeurForum.class);

    private IServeurForum parent;

    private RepositoryHandler handler;

    private Set<HistoryAction> history = Collections.synchronizedSet(new HashSet<HistoryAction>());

    private Set<BroadcastEndpoint> knownServers = new HashSet<BroadcastEndpoint>();

    protected ServerEndpoint(IServeurForum parent, RepositoryHandler handler) throws RemoteException {
        this.parent = parent;
        this.handler = handler;
    }

    @Override
    public Set<HistoryAction> getHistory() throws RemoteException {
        return history;
    }

    /**
     * Add a server to the list of known servers.
     * @param url the complete url (containing port if required) of the given server
     */
    public void addServer(String url) throws RemoteException {
        try {
            BroadcastEndpoint server = (BroadcastEndpoint) Naming.lookup(url + "/endpoint");
            boolean alreadyknown = knownServers.add(server);
            if (!alreadyknown) {
                System.out.println("server "+ url +" is already known");
                return;
            }

            //Force the remote serveur to add the current one
            server.acknowledgeServer(this);

        } catch (RemoteException | MalformedURLException e) {
            System.out.println("Error while accessing server " + url + " : url may be malformed or the remote server may be unavailable");
        } catch (NotBoundException e) {
            System.out.println("No associate binding for server " + url + "");
        }
    }

    @Override
    public void acknowledgeServer(BroadcastEndpoint server) throws RemoteException {
        knownServers.add(server);
    }

    @Override
    public CRUDResult broadcast(HistoryAction action) throws RemoteException {

        try {
            //Local apply
            logger.info("[" + this.parent.getUrl() + ":" + this.parent.getPort() + "]Local handling of a new history");
            CRUDResult isOk = handleHistory(action);

            if (isOk.equals(CRUDResult.KO)) {
                logger.info("[" + this.parent.getUrl() + ":" + this.parent.getPort() + "] Already known history");
                return isOk;
            }
            else if (isOk.equals(CRUDResult.ERROR)){
                logger.info("[" + this.parent.getUrl() + ":" + this.parent.getPort() + "] An error occured while handling history");
                return isOk;
            }
            else
            {
                history.add(action);
            }

            //broadcast
            logger.info("Broadcasting "+ knownServers.size() +" known servers a new history");
            for (BroadcastEndpoint server : knownServers) {
                BroadcastOperation bcast = new BroadcastOperation(server, action);
                new Thread(bcast).start();
            }

            return isOk;
        } catch (UnknownContentKindException e) {
            logger.error("An unknown kind of content has been broadcasted. Interrupting broadcast now");
        }

        return CRUDResult.ERROR;
    }


    /**
     * Apply the local computation required depending on the type of the history action :
     * <ul>
     *  <li> if a topic, should add the topic (if required) and update clients GUI</li>
     *  <li> if a message, should add the message in its topic and update clients GUI</li>
     * </ul>
     * @param history the event broadcasted by a server
     * @return how does the server reacts to the request
     * @throws RemoteException when unable to connect to a remote Object
     * @throws UnknownContentKindException unknown content to be broadcasted
     */
    @Override
    public CRUDResult handleHistory(HistoryAction history) throws RemoteException, UnknownContentKindException {

        IHistoryHandler handler = HistoryHandlerFactory.get(history.getClassifier());
        CRUDResult result = handler.handle(history, getHandler().get(history.getClassifier()));

        return result;
    }

    @Override
    public RepositoryHandler getHandler() throws RemoteException {
        return this.handler;
    }

    @Override
    public IServeurForum getParent() throws RemoteException {
        return parent;
    }

}
