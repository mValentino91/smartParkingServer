package com.parking.persistence.mongo.repositories;

import com.parking.persistence.mongo.documents.Parking;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 *
 * @author mekko
 */
public interface ParkingRepository extends PagingAndSortingRepository<Parking,String>{
    
}

