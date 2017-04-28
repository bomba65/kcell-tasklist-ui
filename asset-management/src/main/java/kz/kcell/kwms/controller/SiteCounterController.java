package kz.kcell.kwms.controller;

import kz.kcell.kwms.model.SiteCounter;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;

@RestController
@Log
public class SiteCounterController {

    @Autowired
    EntityManager em;

    @GetMapping(path = "/sitecounter/{siteId}", produces = "application/json")
    @Transactional
    public int getNextCounter(@PathVariable("siteId") String siteId) {
        SiteCounter siteCounter = (SiteCounter) em.createNativeQuery(
                "insert into site_counter as d (id,value) values(?1,1) on conflict(id) do update set value = d.value + 1 returning *\n",
                SiteCounter.class)
                .setParameter(1, siteId).getSingleResult();
        return siteCounter.getValue();
    }
}