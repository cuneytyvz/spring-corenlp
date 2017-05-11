package com.gsu.semantic.controller;

import com.gsu.semantic.model.Place;
import com.gsu.semantic.model.Property;
import com.gsu.semantic.repository.HistoricalIstanbulDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;

/**
 * Created by cnytync on 21/12/14.
 */
@Controller("HistoricalIstanbulApi")
@RequestMapping(value = "/historicalIstanbul/api")
public class HistoricalIstanbulApi {

    @Autowired
    private HistoricalIstanbulDao historicalIstanbulDao;

    @RequestMapping(value = "/savePlace", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object savePlace(@RequestBody Place place) throws Exception {
        Place pl = historicalIstanbulDao.findPlaceByName(place.getName());
        if (pl != null) {
            return null;
        }

        Long id = historicalIstanbulDao.savePlace(place);

        for (Property property : place.getProperties()) {
            property.setPlaceId(id);

            historicalIstanbulDao.saveProperty(property);
        }

        return id;
    }

    @RequestMapping(value = "/getAllPlaces", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Object getAllPlaces() throws Exception {
        Collection<Place> places = historicalIstanbulDao.findAllPlaces();

        return places;
    }
}
