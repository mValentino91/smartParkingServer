package com.parking.negotiation;

import com.parking.persistence.mongo.documents.Parking;
import java.util.ArrayList;

public class QuickSortParking {
    /**
     * This method sort the input ArrayList using quick sort algorithm.
     *
     * @param input the ArrayList of integers.
     * @return sorted ArrayList of integers.
     */
    public ArrayList<Parking> quicksort(ArrayList<Parking> input) {

        if (input.size() <= 1) {
            return input;
        }

        int middle = (int) Math.ceil((double) input.size() / 2);
        Parking pivot = input.get(middle);

        ArrayList<Parking> less = new ArrayList<Parking>();
        ArrayList<Parking> greater = new ArrayList<Parking>();

        for (int i = 0; i < input.size(); i++) {
            if (input.get(i).getUtility() >= pivot.getUtility()) {
                if (i == middle) {
                    continue;
                }
                less.add(input.get(i));
            } else {
                greater.add(input.get(i));
            }
        }

        return concatenate(quicksort(less), pivot, quicksort(greater));
    }

    /**
     * Join the less array, pivot integer, and greater array to single array.
     *
     * @param less integer ArrayList with values less than pivot.
     * @param pivot the pivot integer.
     * @param greater integer ArrayList with values greater than pivot.
     * @return the integer ArrayList after join.
     */
    private ArrayList<Parking> concatenate(ArrayList<Parking> less, Parking pivot, ArrayList<Parking> greater) {

        ArrayList<Parking> list = new ArrayList<Parking>();

        for (Parking les : less) {
            list.add(les);
        }

        list.add(pivot);

        for (Parking greater1 : greater) {
            list.add(greater1);
        }

        return list;
    }

}
