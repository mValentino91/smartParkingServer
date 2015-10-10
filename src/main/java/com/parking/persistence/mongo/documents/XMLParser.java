/*
 * Copyright (C) 2015 Luca CPZ
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
package com.parking.persistence.mongo.documents;

import com.parking.negotiation.ConcreteInputCalculator;
import com.parking.negotiation.InputCalculator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Luca CPZ
 */
public class XMLParser {

    private final String filePath;
    private final String tagName;
    private final String collection;
    private final InputCalculator calculator = new ConcreteInputCalculator();

    private ArrayList<? extends Collection> list;

    /**
     * Parses a given XML into a list of Collections, which can be directly used
     * or stored through ConcretePersistenceManager.
     *
     * @param filePath - path to XML file
     * @param tagName - tag name of elements (i.e. "place" for
     * nominatim.openstreetmap)
     * @param collection - "parking" or "parkingManager"
     */
    public XMLParser(String filePath, String tagName, String collection) {

        this.filePath = filePath;
        this.tagName = tagName;
        this.collection = collection;

        Document dom = getXMLFile();

        if (dom != null) {
            list = parseDocument(dom);
        }

    }

    public ArrayList getList() {
        return list;
    }

    private Document getXMLFile() {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.parse(filePath);

        } catch (ParserConfigurationException pce) {
            System.out.println("XMLParser: error parsing " + filePath);
        } catch (SAXException se) {
            System.out.println("XMLParser: SAXException");
        } catch (IOException ioe) {
            System.out.println("XMLParser: IOException");
        }

        return null;

    }

    private ArrayList parseDocument(Document dom) {

        // get the root element
        Element docEle = dom.getDocumentElement();

        // get a nodelist of elements
        NodeList nl = docEle.getElementsByTagName(tagName);

        if (nl == null || nl.getLength() <= 0) {
            return null;
        }

        // define either a parking or parkingManager list
        if (collection.equals("parking")) {
            return parkingList(nl);
        } else {
            return parkingManagerList(nl);
        }

    }

    private ArrayList parkingList(NodeList nl) {

        ArrayList<Parking> list = new ArrayList<Parking>(nl.getLength());

        for (int i = 0; i < nl.getLength(); i++) {

            int zone = 1 + (int)(Math.random() * ((4 - 1) + 1));
            int capacity = 10 + (int)(Math.random() * ((50 - 10) + 1));
            int indexmanager = (int)(Math.random() * ((2) + 1));
            double price = calculator.getStaticPrice(zone,capacity);
            String[] pm = {"NapoliPark","ParkingPrisca","ParcheggiCampania"};

            // get the element
            Element el = (Element) nl.item(i);

            // create parking object
            Parking p = new Parking();

            double[] location = new double[]{
                Double.parseDouble(el.getAttribute("lat")),
                Double.parseDouble(el.getAttribute("lon"))
            };

            p.setLocation(location);
            p.setAddress(el.getAttribute("display_name"));

            String[] str = el.getAttribute("display_name").split("-");
            if (str.length > 0) {
                p.setName(str[0]);
            }

            p.setZone(zone);
            p.setCapacity(capacity);
            p.setOccupied(0);
            p.setIsFull(false);
            p.setUtility(0);
            p.setPrice(price);
            p.setParkingManagerId(pm[indexmanager]);

            // add to list
            list.add(i, p);
        }

        return list;
    }

    private ArrayList parkingManagerList(NodeList nl) {
        return null; // STUB
    }

}
