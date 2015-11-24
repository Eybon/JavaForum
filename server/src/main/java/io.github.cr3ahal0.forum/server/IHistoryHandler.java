package io.github.cr3ahal0.forum.server;

import io.github.cr3ahal0.forum.server.impl.broadcast.HistoryAction;

/**
 * Created by Maxime on 22/11/2015.
 */
public interface IHistoryHandler {

    public CRUDResult handle(HistoryAction action, DataRepository repository);

}
