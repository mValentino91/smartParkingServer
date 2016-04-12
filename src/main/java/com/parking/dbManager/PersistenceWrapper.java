package com.parking.dbManager;

import com.parking.csv.CsvCreator;

/**
 *
 * @author Marco
 */
public class PersistenceWrapper {

    private static PersistenceManager pm = null;
    private static CsvCreator csv = null;
    public static int numAgents = 0;

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

    /**
     * @return the pm
     */
    public static CsvCreator getCsvCreator() {
        return csv;
    }
    
     /**
     * @param csv the csv to set
     */
    public static void setCsvCreator(CsvCreator csv) {
        PersistenceWrapper.csv = csv;
    }

}
