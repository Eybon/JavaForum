package io.github.cr3ahal0.forum.server.impl.handlers;

import io.github.cr3ahal0.forum.server.*;
import io.github.cr3ahal0.forum.server.exceptions.UnknownContentKindException;
import io.github.cr3ahal0.forum.server.impl.SujetDiscussion;
import io.github.cr3ahal0.forum.server.impl.broadcast.ActionKind;
import io.github.cr3ahal0.forum.server.impl.broadcast.HistoryAction;
import io.github.cr3ahal0.forum.server.impl.broadcast.HistoryHandlerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;

/**
 * Created by Maxime on 22/11/2015.
 */
public class SujetDiscussionHandler implements IHistoryHandler {

    @Override
    public CRUDResult handle(HistoryAction action, DataRepository repository) {

        try {

            JAXBContext jaxbContext = null;
            jaxbContext = JAXBContext.newInstance(action.getClassifier());
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            StringReader reader = new StringReader(action.getData());
            SujetDiscussion topic = (SujetDiscussion) jaxbUnmarshaller.unmarshal(reader);

            if (action.getAction().equals(ActionKind.CREATE)) {
                return ((DataRepository<ISujetDiscussion,String>)repository).add(topic);
            }
            else if (action.getAction().equals(ActionKind.DELETE)){
                return ((DataRepository<ISujetDiscussion,String>)repository).remove(topic);
            }

        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return CRUDResult.ERROR;

    }
}
