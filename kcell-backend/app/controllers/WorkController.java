package controllers;

import dtos.core.WorkDto;
import models.core.Work;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class WorkController extends Controller {

    @Inject
    JPAApi jpaApi;

    @Transactional
    public Result list() {
        List<Work> works = jpaApi.em().createQuery("select w from Work w order by w.sapPOServiceName").getResultList();
        List<WorkDto> workDtos = works.stream().map(WorkDto::new).collect(Collectors.toList());
        return ok(Json.toJson(workDtos));
    }
}
