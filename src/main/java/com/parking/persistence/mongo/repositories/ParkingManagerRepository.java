/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.parking.persistence.mongo.repositories;

import com.parking.persistence.mongo.documents.ParkingManager;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 *
 * @author Marco
 */
public interface ParkingManagerRepository extends PagingAndSortingRepository<ParkingManager,String>{
    
}
