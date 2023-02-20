package kz.kcell.kwms.controller;

import kz.kcell.kwms.model.VpnPortCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import java.time.Year;

@RestController
public class VpnPortCounterController {

    @Autowired
    EntityManager em;

    @PostMapping(path = "/vpn_port_counter/{channel}", produces = "application/json")
    @Transactional
    @ResponseBody
    public String getNextCounter(@PathVariable String channel) {
        int year = Year.now().getValue();
        VpnPortCounter vpnPortCounter = (VpnPortCounter) em.createNativeQuery(
                "insert into vpn_port_counter as d (value, year, channel) values(0001,?1,?2) on conflict(year,channel) do update set value = d.value + 1 returning *\n",
                VpnPortCounter.class)
                .setParameter(1, year)
                .setParameter(2, channel)
                .getSingleResult();

        return "\"" + year % 100 + "-" + String.format("%04d", vpnPortCounter.getValue()) + "\"";
    }
}