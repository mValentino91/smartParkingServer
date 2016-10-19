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
import java.awt.Point;
import java.awt.geom.Point2D;
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
    private float lat;
    private float lon;
    private final InputCalculator calculator = new ConcreteInputCalculator();
    private int indexZone1 = 0;
    private int indexZone2 = 0;
    private int indexZone3 = 0;
    private int indexZone4 = 0;
    private int numParkingManagers = 5;
    private int capacity = 20;

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
    public XMLParser(String filePath, String tagName, String collection, float lat, float lon) {

        this.filePath = filePath;
        this.tagName = tagName;
        this.collection = collection;
        this.lat = lat;
        this.lon = lon;

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

            //int zone = 1 + (int) (Math.random() * ((4 - 1) + 1));
            //int capacity = 10 + (int) (Math.random() * ((50 - 10) + 1));
            //int indexmanager = (int) (Math.random() * ((4) + 1));
            //int indexmanager = 0;
            //double price = calculator.getStaticPrice(zone, capacity);
            //String[] pm = {"NapoliPark", "ParkingPrisca", "ParcheggiCampania"};

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

            Point.Double center = new Point2D.Double(lat, lon);
            double distance = center.distance(p.getLocation()[0], p.getLocation()[1]);

            String[] pm = {"parking1", "parking2", "parking3", "parking4", "parking5"}; //Single Parking Manager
            //distribuzione uniforme dei parkeggi ai parking managers
            if (distance < 0.005) {
                p.setZone(1);
                p.setParkingManagerId(pm[indexZone1 % numParkingManagers]);
                System.out.println(indexZone1);
                indexZone1++;
            } else if (distance < 0.01) {
                p.setZone(2);
                p.setParkingManagerId(pm[indexZone2 % numParkingManagers]);
                indexZone2++;
                System.out.println(indexZone2);
            } else if (distance < 0.018) {
                p.setZone(3);
                p.setParkingManagerId(pm[indexZone3 % numParkingManagers]);
                indexZone3++;
                System.out.println(indexZone3);
            } else {
                p.setZone(4);
                p.setParkingManagerId(pm[indexZone4 % numParkingManagers]);
                indexZone4++;
                System.out.println(indexZone4);
            }

            p.setCapacity(capacity);
            p.setOccupied(0);
            p.setIsFull(false);
            p.setUtility(0);
            p.setPrice(calculator.getStaticPrice(p.getZone(), capacity));
            //p.setParkingManagerId(pm[indexmanager]);

            // add to list
            list.add(i, p);
        }

        return list;
    }

    private ArrayList parkingManagerList(NodeList nl) {
        return null; // STUB
    }

}
