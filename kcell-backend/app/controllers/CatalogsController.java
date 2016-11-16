package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dtos.core.ContractDto;
import dtos.core.ContractorDto;
import dtos.core.ServiceDto;
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

    @Transactional
    public Result all() {
        List<Unit> units = jpaApi.em().createQuery("select u from Unit u order by u.id").getResultList();
        List<Reason> reasons = jpaApi.em().createQuery("select r from Reason r order by r.id").getResultList();
        List<Contract> contracts = jpaApi.em().createQuery("select c from Contract c order by c.id").getResultList();
        List<ContractDto> contractDtos = contracts.stream().map(ContractDto::new).collect(Collectors.toList());
        List<Contractor> contractors = jpaApi.em().createQuery("select c from Contractor c order by c.id").getResultList();
        List<ContractorDto> contractorsDtos = contractors.stream().map(ContractorDto::new).collect(Collectors.toList());
        List<Service> services = jpaApi.em().createQuery("select s from Service s order by s.id").getResultList();
        List<ServiceDto> serviceDtos = services.stream().map(ServiceDto::new).collect(Collectors.toList());

        ObjectNode node = Json.newObject();
        node.put("units", Json.toJson(reasons));
        node.put("reasons", Json.toJson(reasons));
        node.put("contracts", Json.toJson(contractDtos));
        node.put("contractors", Json.toJson(contractorsDtos));
        node.put("services", Json.toJson(serviceDtos));
        ArrayNode unitsTitle = Json.newArray();
        for (Unit unit : units) {
            ObjectNode obj = Json.newObject();
            obj.put(unit.getValue(), unit.getDescription());
            unitsTitle.add(obj);
        }
        ArrayNode reasonsTitle = Json.newArray();
        for (Reason reason : reasons) {
            ObjectNode obj = Json.newObject();
            obj.put(reason.getId().toString(), reason.getName());
            reasonsTitle.add(obj);
        }
        ArrayNode servicesTitle = Json.newArray();
        for (Service service : services) {
            ObjectNode obj = Json.newObject();
            obj.put(service.getId().toString(), service.getName());
            servicesTitle.add(obj);
        }
        ArrayNode contractorsTitle = Json.newArray();
        for (Contractor contractor : contractors) {
            ObjectNode obj = Json.newObject();
            obj.put(contractor.getId().toString(), contractor.getName());
            contractorsTitle.add(obj);
        }
        node.put("unitsTitle", unitsTitle);
        node.put("reasonsTitle", reasonsTitle);
        node.put("servicesTitle", servicesTitle);
        node.put("contractorsTitle", contractorsTitle);
        return ok(Json.toJson(node));
    }
}
