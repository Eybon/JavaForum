package io.github.cr3ahal0.forum.server.impl.broadcast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by Maxime on 19/12/2015.
 */
public class Causality extends HashMap<UUID, Integer> {

    public void increment(UUID guid) {
        Integer value = get(guid);
        if (guid == null) {
            value = 0;
        }
        put(guid, value+1);
    }

    public static void debug(Causality c1, Causality c2) {

        List<Integer> vals = new ArrayList(c1.values());
        String b = "(";
        for (Integer in : vals) {
            b += String.valueOf(in) + " ";
        }
        b += ")";

        System.out.println("Incoming value = " + b);

        vals = new ArrayList(c2.values());
        b = "(";
        for (Integer in : vals) {
            b += String.valueOf(in) + " ";
        }
        b += ")";

        System.out.println("mine = " + b);

    }

}
