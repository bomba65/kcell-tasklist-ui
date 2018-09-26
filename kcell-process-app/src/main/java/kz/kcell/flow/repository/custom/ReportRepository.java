package kz.kcell.flow.repository.custom;

import kz.kcell.flow.repository.dto.FinancialReportDto;
import kz.kcell.flow.repository.dto.ReportDto;
import kz.kcell.flow.repository.dto.ExtendedReportByJobsDto;
import kz.kcell.flow.repository.dto.TechnicalReportByJobsDto;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

public class ReportRepository {

    @PersistenceContext
    EntityManager em;

    public List<ReportDto> report(String query){
        return em.createNativeQuery(query).getResultList();
    }

    public List<TechnicalReportByJobsDto> technicalReportByJobs(String query){
        return em.createNativeQuery(query).getResultList();
    }

    public List<FinancialReportDto> financialReport(String query){
        return em.createNativeQuery(query).getResultList();
    }

    public List<ExtendedReportByJobsDto> extendedReportByJobs(String query){
        return em.createNativeQuery(query).getResultList();
    }
}
