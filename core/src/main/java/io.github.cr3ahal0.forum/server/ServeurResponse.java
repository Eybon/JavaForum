package io.github.cr3ahal0.forum.server;

/**
 * Created by Maxime on 12/11/2015.
 */
public enum ServeurResponse implements java.io.Serializable {
    TOPIC_KNOWN,
    TOPIC_UNKNOWN,
    MESSAGE_KNOWN,
    MESSAGE_UNKNOWN,
    ERROR,
    OK
}
