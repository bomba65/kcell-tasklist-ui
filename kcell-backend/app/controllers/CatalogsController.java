package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dtos.core.ContractDto;
import dtos.core.ContractorDto;
import dtos.core.ServiceDto;
import dtos.core.WorkDto;
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

        List<Work> works = jpaApi.em().createQuery("select w from Work w order by w.sapPOServiceName").getResultList();
        List<WorkDto> workDtos = works.stream().map(WorkDto::new).collect(Collectors.toList());

        ArrayNode equipments = Json.newArray();
        ObjectNode equipmentsTitle = Json.newObject();
        for (int a = (int) 'A'; a <= (int) 'Z'; a++) {
            ObjectNode objectNode = Json.newObject();
            objectNode.put("id",((char) a) + "");
            objectNode.put("name", "Equipment " + ((char) a));
            equipments.add(objectNode);
            equipmentsTitle.put(((char) a) + "", "Equipment " + ((char) a));
        }
        ArrayNode connectionWorks = Json.newArray();
        ObjectNode connectionWorksTitle = Json.newObject();
        for (int a = (int) 'A'; a <= (int) 'Z'; a++) {
            ObjectNode objectNode = Json.newObject();
            objectNode.put("id", ((char) a) + "");
            objectNode.put("name", "Work " + ((char) a));
            connectionWorks.add(objectNode);
            connectionWorksTitle.put(((char) a) + "", "Work " + ((char) a));
        }
        ArrayNode connections = Json.newArray();
        ObjectNode connectionsTitle = Json.newObject();
        connectionsTitle.put("1", "E2E...");
        connectionsTitle.put("2", "Power line...");
        ObjectNode a = Json.newObject();
        a.put("id", "1");
        a.put("name", "E2E...");
        ObjectNode b = Json.newObject();
        b.put("id", "2");
        b.put("name", "Power line...");
        connections.add(a);
        connections.add(b);


        ObjectNode node = Json.newObject();
        node.put("units", Json.toJson(units));
        node.put("reasons", Json.toJson(reasons));
        node.put("contracts", Json.toJson(contractDtos));
        node.put("contractors", Json.toJson(contractorsDtos));
        node.put("services", Json.toJson(serviceDtos));
        node.put("works", Json.toJson(workDtos));
        node.put("connections", Json.toJson(connections));
        node.put("equipments", Json.toJson(equipments));
        node.put("connectionWorks", Json.toJson(connectionWorks));
        ObjectNode unitsTitle = Json.newObject();
        for (Unit unit : units) {
            unitsTitle.put(unit.getValue(), unit.getDescription());
        }
        ObjectNode reasonsTitle = Json.newObject();
        for (Reason reason : reasons) {
            reasonsTitle.put(reason.getId().toString(), reason.getName());
        }
        ObjectNode servicesTitle = Json.newObject();
        for (Service service : services) {
            servicesTitle.put(service.getId().toString(), service.getName());
        }
        ObjectNode contractorsTitle = Json.newObject();
        for (Contractor contractor : contractors) {
            contractorsTitle.put(contractor.getId().toString(), contractor.getName());
        }
        ObjectNode worksTitle = Json.newObject();
        for (Work work : works) {
            worksTitle.put(work.getSapServiceNumber().toString(), work.getSapPOServiceName());
        }
        node.put("unitsTitle", unitsTitle);
        node.put("reasonsTitle", reasonsTitle);
        node.put("servicesTitle", servicesTitle);
        node.put("contractorsTitle", contractorsTitle);
        node.put("worksTitle", worksTitle);
        node.put("equipmentsTitle", equipmentsTitle);
        node.put("connectionsTitle", connectionsTitle);
        node.put("connectionWorksTitle", connectionWorksTitle);
        return ok(Json.toJson(node));
    }
}
