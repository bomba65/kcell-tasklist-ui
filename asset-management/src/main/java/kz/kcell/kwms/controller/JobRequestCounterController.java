package kz.kcell.kwms.controller;

import kz.kcell.kwms.model.JobRequestCounter;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;

@RestController
@Log
public class JobRequestCounterController {

    @Autowired
    EntityManager em;

    @PostMapping(path = "/jobrequestcounter/{counterId}", produces = "application/json")
    @Transactional
    @ResponseBody
    public String getNextCounter(@PathVariable("counterId") String counterId) {
        JobRequestCounter jobRequestCounter = (JobRequestCounter) em.createNativeQuery(
                "insert into jobrequest_counter as d (id,value) values(?1,7000) on conflict(id) do update set value = d.value + 1 returning *\n",
                JobRequestCounter.class)
                .setParameter(1, counterId).getSingleResult();

        String counter = counterId.endsWith("-Ro") ? counterId.replace("-Ro", "") : counterId;

        return "\"" + counter + "-" + String.format("%04d", jobRequestCounter.getValue()) + (counterId.endsWith("-Ro") ? "-Ro" : "") + "\"";
    }

    @PostMapping(path = "/sdrrequestcounter/{counterId}", produces = "application/json")
    @Transactional
    @ResponseBody
    public String getNextSDRCounter(@PathVariable("counterId") String counterId) {
        JobRequestCounter jobRequestCounter = (JobRequestCounter) em.createNativeQuery(
                "insert into jobrequest_counter as d (id,value) values(?1,0001) on conflict(id) do update set value = d.value + 1 returning *\n",
                JobRequestCounter.class)
                .setParameter(1, counterId).getSingleResult();

        return "\"" + counterId + "-" + String.format("%04d", jobRequestCounter.getValue()) + "\"";
    }

    @PostMapping(path = "/rolloutcounter/{counterId}", produces = "application/json")
    @Transactional
    @ResponseBody
    public String getNextRollourCounter(@PathVariable("counterId") String counterId) {
        JobRequestCounter jobRequestCounter = (JobRequestCounter) em.createNativeQuery(
                "insert into jobrequest_counter as d (id,value) values(?1,5000) on conflict(id) do update set value = d.value + 1 returning *\n",
                JobRequestCounter.class)
                .setParameter(1, counterId).getSingleResult();

        return "\"" + String.format("%04d", jobRequestCounter.getValue()) + "\"";
    }
}