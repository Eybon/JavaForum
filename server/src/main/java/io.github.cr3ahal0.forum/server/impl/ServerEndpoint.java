package io.github.cr3ahal0.forum.server.impl;

import com.sun.org.apache.xpath.internal.SourceTree;
import io.github.cr3ahal0.forum.server.*;
import io.github.cr3ahal0.forum.server.exceptions.UnknownContentKindException;
import io.github.cr3ahal0.forum.server.impl.broadcast.BroadcastOperation;
import io.github.cr3ahal0.forum.server.impl.broadcast.Causality;
import io.github.cr3ahal0.forum.server.impl.broadcast.HistoryAction;
import io.github.cr3ahal0.forum.server.impl.broadcast.HistoryHandlerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.UuidUtil;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/**
 * Created by Maxime on 23/11/2015.
 */
public class ServerEndpoint extends UnicastRemoteObject implements BroadcastEndpoint {

    private final UUID GUID = UUID.randomUUID();

    private Causality causality;

    private static final Logger logger = LogManager.getLogger(ServeurForum.class);

    private IServeurForum parent;

    private RepositoryHandler handler;

    private Set<HistoryAction> history = Collections.synchronizedSet(new HashSet<HistoryAction>());

    private Set<BroadcastEndpoint> knownServers = new HashSet<BroadcastEndpoint>();

    protected ServerEndpoint(IServeurForum parent, RepositoryHandler handler) throws RemoteException {
        this.parent = parent;
        this.handler = handler;
        this.causality = new Causality();

        //set new causality value
        causality.put(GUID, 0);
    }

    @Override
    public UUID getGUID() throws RemoteException {
        return GUID;
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
            boolean alreadyknown = knownServers.contains(server);
            if (alreadyknown) {
                System.out.println("[add]server "+ url +" is already known");
                return;
            }

            //Force the remote serveur to add the current one
            if (server.acknowledgeServer(this)) {
                knownServers.add(server);
                causality.put(server.getGUID(), server.getHistory().size());
            }
        } catch (RemoteException | MalformedURLException e) {
            System.out.println("Error while accessing server " + url + " : url may be malformed or the remote server may be unavailable");
        } catch (NotBoundException e) {
            System.out.println("No associate binding for server " + url + "");
        }
    }

    @Override
    public boolean acknowledgeServer(BroadcastEndpoint server) throws RemoteException {

        if (knownServers.contains(server)) {
            logger.info("[ack]This remote server is already known");
            return true;
        }

        //Check if the remote server is equally causal to this one
        logger.info("Checking history causality");
        Set<HistoryAction> remoteActions = server.getHistory();

        //In case we need some antienthropy mecanism AND the other server has already begun its journey
        if (remoteActions.size() == 0 || (remoteActions.size() > 0 && doesAcceptPartitionTolerance())) {
            //One-sided checking : local -> remote
            for (HistoryAction action : getHistory()) {
                if (!remoteActions.contains(action)) {
                    try {
                        server.handleHistory(action);
                    } catch (UnknownContentKindException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        else
        {
            logger.info("Unable to acknowledge remote server since local server does not accept partition tolerance");
            return false;
        }

        knownServers.add(server);
        causality.put(server.getGUID(), remoteActions.size());

        return true;
    }

    @Override
    public boolean doesAcceptPartitionTolerance() {
        return false;
    }

    @Override
    public Causality getCausality() throws RemoteException {
        return causality;
    }

    public boolean isLower(HistoryAction action) {
        boolean isLower = true;

        Causality c = action.getCausality();

        Set<UUID> from = c.keySet();

        try {

            if (action.getAuthor().equals(getGUID())) {
                return true;
            }

            for (UUID i : from) {

                //Is this history known?
                if (causality.get(i) == null) {
                    causality.put(i, 0);
                }

                isLower = ((!i.equals(action.getAuthor()) && causality.get(i) >= c.get(i)) ||
                        (i.equals(action.getAuthor()) && (c.get(i) - 1 == causality.get(i))));

                if (!isLower) {
                    return false;
                }
            }
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }

        return isLower;
    }

    @Override
    public CRUDResult broadcast(HistoryAction action) throws RemoteException {

        try {
            //FIRST Check causality
            if (isLower(action)) {
                logger.info("received new history causally consistent");
            }
            else
            {
                logger.info("received a new history which is NOT causally consistent");

                /*
                * Causality.debug(action.getCausality(), causality);
                */
            }

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
            List<Thread> threads = new ArrayList<Thread>();
            for (BroadcastEndpoint server : knownServers) {
                BroadcastOperation bcast = new BroadcastOperation(server, action);
                Thread t = new Thread(bcast);
                t.start();
                threads.add(t);
            }

            for (Thread t : threads) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
