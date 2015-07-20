package com.parking.dbManager;

/**
 *
 * @author Marco
 */
public class PersistenceWrapper {
    
    private static PersistenceManager pm = null;

    /**
     * @return the pm
     */
    public static PersistenceManager get() {
        return pm;
    }

    /**
     * @param pm the pm to set
     */
    public static void set(PersistenceManager pm) {
        PersistenceWrapper.pm = pm;
    }
    
    
    
}
