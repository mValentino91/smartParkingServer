/*
 * Copyright (C) 2015 Marco
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
package com.parking.controller;

import com.google.gson.Gson;
import com.parking.dbManager.PersistenceManager;
import com.parking.persistence.mongo.documents.Parking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Marco Valentino
 */
@Controller
@Scope("request")
public class MapController {

    @Autowired
    private PersistenceManager persistence;

    @RequestMapping("/Map")
    public ModelAndView getAllPoi( /*@RequestParam(value = "lat") String lat,
             @RequestParam(value = "lon") String lon,
             @RequestParam(value = "lat1") String lat1,
             @RequestParam(value = "lon1") String lon1*/) {
        //Creo la view che sarà mostrata all'utente
        
        ModelAndView model = new ModelAndView("mapParcheggi");
        Iterable<Parking> poiList = persistence.getAllParking();
        //aggiungo la lista al model
        model.addObject("poiList", poiList);

        return model;
    }
}
