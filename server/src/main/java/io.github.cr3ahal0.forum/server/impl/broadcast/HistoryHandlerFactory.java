package io.github.cr3ahal0.forum.server.impl.broadcast;

import io.github.cr3ahal0.forum.server.IHistoryHandler;
import io.github.cr3ahal0.forum.server.exceptions.UnknownContentKindException;
import io.github.cr3ahal0.forum.server.impl.Message;
import io.github.cr3ahal0.forum.server.impl.SujetDiscussion;
import io.github.cr3ahal0.forum.server.impl.handlers.MessageHandler;
import io.github.cr3ahal0.forum.server.impl.handlers.SujetDiscussionHandler;

import java.util.HashMap;

/**
 * Created by Maxime on 22/11/2015.
 */
public class HistoryHandlerFactory {

    private static HashMap<Class, IHistoryHandler> _handlers;

    static {
        _handlers = new HashMap<Class, IHistoryHandler>();
        _handlers.put(Message.class, new MessageHandler());
        _handlers.put(SujetDiscussion.class, new SujetDiscussionHandler());
    }

    public static IHistoryHandler get(Class handler) throws UnknownContentKindException {
        IHistoryHandler hand = _handlers.get(handler);
        if (hand == null) {
            throw new UnknownContentKindException();
        }
        return hand;
    }

    private HistoryHandlerFactory() {

    }

}
