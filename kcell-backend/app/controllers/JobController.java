package controllers;

import dtos.core.JobDto;
import models.core.Job;
import models.core.Reason;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class JobController extends Controller {

    @Inject
    JPAApi jpaApi;

    @Transactional
    public Result jobsByReason(Long reasonId) {
        Reason reason = jpaApi.em().find(Reason.class, reasonId);
        if (reason != null) {
            List<Job> jobs = jpaApi.em().createQuery("select j from Job j where j.reason = :reason").setParameter("reason", reason).getResultList();
            List<JobDto> jobDtos = jobs.stream().map(JobDto::new).collect(Collectors.toList());
            return ok(Json.toJson(jobDtos));
        } else {
            return notFound();
        }
    }
}
