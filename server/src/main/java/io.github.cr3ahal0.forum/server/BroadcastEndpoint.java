package io.github.cr3ahal0.forum.server;

import io.github.cr3ahal0.forum.server.exceptions.UnknownContentKindException;
import io.github.cr3ahal0.forum.server.impl.broadcast.Causality;
import io.github.cr3ahal0.forum.server.impl.broadcast.HistoryAction;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Maxime on 21/11/2015.
 */
public interface BroadcastEndpoint extends Remote, Serializable {

    /**
     * Ask the current EndPoint to review its buffer and apply possible changes
     * @throws UnknownContentKindException An unknown content is tried to be handled
     * @throws RemoteException RMI exception
     */
    public void reviewBuffer() throws UnknownContentKindException, RemoteException;

    /**
     * Return if the local states vector is lower than the given one
     * @param action the information to handle
     * @return if the local states vector is lower than the given one
     * @throws RemoteException RMI exception
     */
    public boolean isLower(HistoryAction action) throws RemoteException;

    /**
     * Return the Global Unique Identifier of the current Endpoint
     * @return the Global Unique Identifier of the current Endpoint
     * @throws RemoteException RMI exception
     */
    public UUID getGUID() throws RemoteException;

    /**
     * Return the current local states vector
     * @return the current local states vector
     * @throws RemoteException RMI exception
     */
    public Causality getCausality() throws RemoteException;

    /**
     * Return the complete set of HistoryAction
     * @return the complete set of HistoryAction
     * @throws RemoteException RMI exception
     */
    public Set<HistoryAction> getHistory() throws RemoteException;

    /**
     * Try to add an endpoint to the list of neighbours
     * @param url the url pointing to a remote endpoint
     * @throws RemoteException RMI exception
     */
    public void addServer(String url) throws RemoteException;

    /**
     * Check if a remote server can be added as a neighbour
     * @param server the endpoint to add
     * @return if the remote server can be added
     * @throws RemoteException RMI exception
     */
    public boolean acknowledgeServer(BroadcastEndpoint server) throws RemoteException;

    /**
     * Apply and broadcast a given information to all neighbours
     * @param history the information to broadcast
     * @return a CRUDResult depending on the handling of the information
     * @throws RemoteException RMI exception
     */
    public CRUDResult broadcast(HistoryAction history) throws RemoteException;

    /**
     * Ask the endpoint to take immediately into account the information
     * @param history the information to handle
     * @return a CRUDResult depending on the handling of the information
     * @throws RemoteException RMI exception
     * @throws UnknownContentKindException An unknown content has been provided
     */
    public CRUDResult handleHistory(HistoryAction history) throws RemoteException, UnknownContentKindException;

    /**
     * Return the entity handling the repositories of the application
     * @return the entity handling the repositories of the application
     * @throws RemoteException RMI exception
     */
    public RepositoryHandler getHandler() throws RemoteException;

    /**
     * Return the container of the current endpoint
     * @return the container of the current endpoint
     * @throws RemoteException RMI exception
     */
    public IServeurForum getParent() throws RemoteException;

    /**
     * Return if the current endpoint does accept new endpoints with already existing non-null history
     * @return if the current endpoint does accept new endpoints with already existing non-null history
     * @throws RemoteException RMI exception
     */
    public boolean doesAcceptPartitionTolerance() throws RemoteException;
}
