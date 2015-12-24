package io.github.cr3ahal0.forum.server;

/**
 * Created by Maxime on 22/11/2015.
 */
public interface DataRepository<T, U> {

    /**
     * Return the object T corresponding to the given U identifier
     * @param id an Identifier
     * @return the object T corresponding to the given U identifier
     */
    public T get(U id);

    /**
     *  Try to add an object to the repository
     * @param object the object we want to add
     * @return whether the object has been added or not
     */
    public CRUDResult add(T object);

    /**
     * Try to remove the given object from the repository
     * @param object the object to remove
     * @return whether the object has been deleted or not
     */
    public CRUDResult remove(T object);

    /**
     * Return if the current repository contains the given object
     * @param object the object we're looking for
     * @return if the current repository contains the given object
     */
    public boolean has(T object);

}
