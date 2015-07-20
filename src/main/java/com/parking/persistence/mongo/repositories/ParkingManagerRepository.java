package com.parking.persistence.mongo.repositories;

import com.parking.persistence.mongo.documents.ParkingManager;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 *
 * @author Marco
 */
public interface ParkingManagerRepository extends PagingAndSortingRepository<ParkingManager,String>{
    
}
