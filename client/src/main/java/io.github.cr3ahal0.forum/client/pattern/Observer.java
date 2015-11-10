package io.github.cr3ahal0.forum.client.pattern;

import io.github.cr3ahal0.forum.client.ihm.Channel;

/**
 * Created by Maxime on 27/09/2015.
 */
public interface Observer {

    public void update(Channel chan, String str);

}
