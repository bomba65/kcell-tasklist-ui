package controllers;

import dtos.core.ContractDto;
import models.core.*;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class CatalogsController extends Controller {

    @Inject
    JPAApi jpaApi;

    @Transactional
    public Result getUnits() {
        List<Unit> units = jpaApi.em().createQuery("select u from Unit u order by u.id").getResultList();
        return ok(Json.toJson(units));
    }

    @Transactional
    public Result getReasons() {
        List<Reason> reasons = jpaApi.em().createQuery("select r from Reason r order by r.id").getResultList();
        return ok(Json.toJson(reasons));
    }

    @Transactional
    public Result getContracts() {
        List<Contract> contracts = jpaApi.em().createQuery("select c from Contract c order by c.id").getResultList();
        List<ContractDto> contractDtos = contracts.stream().map(ContractDto::new).collect(Collectors.toList());
        return ok(Json.toJson(contractDtos));
    }

    @Transactional
    public Result getContractors() {
        List<Contractor> contractors = jpaApi.em().createQuery("select c from Contractor c order by c.id").getResultList();
        return ok(Json.toJson(contractors));
    }

    @Transactional
    public Result getServices() {
        List<Service> services = jpaApi.em().createQuery("select s from Service s order by s.id").getResultList();
        return ok(Json.toJson(services));
    }
}
