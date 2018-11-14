package kz.kcell.kwms.controller;

import kz.kcell.kwms.model.Plan;
import kz.kcell.kwms.repository.CurrencyRateRepository;
import kz.kcell.kwms.repository.PlanRepository;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Log
@org.springframework.web.bind.annotation.RestController
@RequestMapping("/networkinfrastructure")
public class NetworkInfrastructureController {

    @Autowired
    PlanRepository planRepository;

    @Autowired
    CurrencyRateRepository currencyRateRepository;

    @RequestMapping(value = "/plan/findBySite/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<Plan>> getNewRequests(@PathVariable("id") Integer id){

        List<Plan> plans = planRepository.findBySite(id);

        return ResponseEntity.ok(plans);
    }

    @RequestMapping(value = "/plan/findCurrentPlanSites", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<Plan>> findCurrentPlanSites(){

        List<Plan> plans = planRepository.findCurrentPlanSites();

        return ResponseEntity.ok(plans);
    }

    @RequestMapping(value = "/plan/changePrevCurrentStatus/{position_number}", method = RequestMethod.GET)
    @ResponseBody
    public void changePrevCurrentStatus(@PathVariable("position_number") Integer position_number){
        planRepository.changePrevCurrentStatus(position_number);
        log.info("GET REQUEST /plan/changePrevCurrentStatus/" + position_number);
    }

    @RequestMapping(value = "/plan/createNewPlan", method = RequestMethod.POST)
    @ResponseBody
//    public void set(@RequestBody String status, @RequestBody Integer site_id, @RequestBody String params,@RequestBody Boolean is_current ){
    public void set(@RequestBody Plan plan ){

//        Plan plan = new Plan();
//        plan.setIs_current(is_current);
//        plan.setParams(params);
//        plan.setSite_id(site_id);
//        plan.setStatus(status);
        log.info("POST REQUEST /plan/createNewPlan");
        planRepository.save(plan);

    }

    @RequestMapping(value = "/plan/changePlanStatus/{status}/{position_number}", method = RequestMethod.GET)
    @ResponseBody
    public void changePlanStatus(@PathVariable("position_number") Integer position_number, @PathVariable("status") String status){
        planRepository.changePlanStatus(position_number, status);
    }

    @RequestMapping(value = "/plan/findCurrentToStartPlanSites", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<Plan>> findCurrentToStartPlanSites(){

        List<Plan> plans = planRepository.findCurrentToStartPlanSites();

        return ResponseEntity.ok(plans);
    }

    @RequestMapping(value = "/plan/findCurrentToStartAndFinishPlanSites", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<Plan>> findCurrentToStartAndFinishPlanSites(){

        List<Plan> plans = planRepository.findCurrentToStartAndFinishPlanSites();

        return ResponseEntity.ok(plans);
    }

    @RequestMapping(value = "/plan/setStartedAndFinished/{position_number}", method = RequestMethod.GET)
    @ResponseBody
    public void setStartedAndFinished(@PathVariable("position_number") Integer position_number){
        planRepository.setStartedAndFinished(position_number);
        log.info("GET REQUEST /plan/setStartedAndFinished/" + position_number);
    }

    @RequestMapping(value = "/currency/rate", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Double> getCurrentExchangeRate(){

        List<Double> rate = currencyRateRepository.getCurrentExchangeRate();

        return ResponseEntity.ok(rate.get(0));
    }
}