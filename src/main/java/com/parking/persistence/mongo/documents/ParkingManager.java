package com.parking.persistence.mongo.documents;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 * @author Marco
 */

@Document(collection = "parkingManager")
public class ParkingManager {
    
    
    private String name;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
}
