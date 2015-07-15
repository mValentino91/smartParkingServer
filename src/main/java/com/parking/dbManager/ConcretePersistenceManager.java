/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parking.dbManager;

import com.parking.persistence.mongo.documents.Parking;
import com.parking.persistence.mongo.documents.ParkingManager;
import com.parking.persistence.mongo.repositories.ParkingManagerRepository;
import com.parking.persistence.mongo.repositories.ParkingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoOperations;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.data.mongodb.core.query.Query;

/**
 *
 * @author Marco Valentino
 */
public class ConcretePersistenceManager implements PersistenceManager {

    @Autowired
    ParkingManagerRepository managerRepo; //repository manager parcheggi

    @Autowired
    ParkingRepository parkingRepo; //repository parcheggi

    @Autowired
    private MongoOperations mongo; //mongo operations manager

    @Override
    public Iterable<ParkingManager> getAllParkingManager() {
        return managerRepo.findAll();
    }

    @Override
    public Iterable<Parking> getAllParking() {
        return parkingRepo.findAll();
    }

    @Override
    public Iterable<Parking> getParkingByManager(String parkingManagerId) {
        return mongo.find(new Query(where("parkingManagerId").is(parkingManagerId)), Parking.class);
    }

    @Override
    public GeoResults<Parking> findNearParking(double location[], double radius) {

        Point point = new Point(location[0], location[1]);
        NearQuery query = NearQuery.near(point).maxDistance(new Distance(radius, Metrics.KILOMETERS));

        return mongo.geoNear(query, Parking.class);
    }

    @Override
    public void saveParking(Parking p) {
        parkingRepo.save(p);
    }

    @Override
    public void saveParkingManager(ParkingManager p) {
        managerRepo.save(p);
    }

}
