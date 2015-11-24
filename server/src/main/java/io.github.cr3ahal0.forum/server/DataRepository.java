package io.github.cr3ahal0.forum.server;

/**
 * Created by Maxime on 22/11/2015.
 */
public interface DataRepository<T, U> {

    public T get(U id);

    public CRUDResult add(T object);

    public CRUDResult remove(T object);

    public boolean has(T object);

}
