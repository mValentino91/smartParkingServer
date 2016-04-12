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

import static java.lang.System.*;
import java.io.File;
import com.opencsv.*;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Marco
 */
public class CsvCreator {

    private String path;
    private CSVWriter writer;

    public CsvCreator(String path, String name) {
        this.path = path;
        try {
            writer = new CSVWriter(new FileWriter(path + name));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeTest(String test) {

        //Create carattere di tabulazione "," pippo va a capo!
        //String[] record = "first#second#third\npippo"
        String[] record = test.split("#");
        //Write the record to file
        writer.writeNext(record);
    }

    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
