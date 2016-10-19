/*
 * Copyright (C) 2016 Marco
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.parking.csv;

import com.opencsv.*;
import com.parking.dbManager.PersistenceWrapper;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.aspectj.weaver.patterns.PerSingleton;

/**
 *
 * @author Marco
 */
public class CsvCreator {

    private String path;
    private CSVWriter writer;
    private ArrayList<String> list = new ArrayList<String>();

    public CsvCreator(String path, String name) {
        this.path = path;
        char separator = ';';
        try {
            writer = new CSVWriter(new FileWriter(path + name), separator);
            String[] header = {"ID User","Soglia","Utilità User","Zona Parcheggio","ID Parking","Utilità Parking","Capienza Parcheggio","Posti Occupati","Nome Parcheggio","Round"};
            writer.writeNext(header);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeTest(String test) {

        //Create carattere di tabulazione "," pippo va a capo!
        //String[] record = "first#second#third\npippo"
        list.add(test);
        System.out.println("\nWrite: "+PersistenceWrapper.numAgents+"\n");
        
    }

    public void close() {
        System.out.println("\nClose: "+PersistenceWrapper.numAgents+"\n");
        try {
            for (String string : list) {
                String[] record = string.split("#");
                writer.writeNext(record);
            }      
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
