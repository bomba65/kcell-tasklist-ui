package kz.kcell.flow.repository;

import kz.kcell.flow.repository.custom.ReportRepository;
import kz.kcell.flow.repository.dto.FinancialReportDto;
import kz.kcell.flow.repository.dto.ExtendedReportByJobsDto;
import kz.kcell.flow.repository.dto.ReportDto;
import kz.kcell.flow.repository.dto.TechnicalReportByJobsDto;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.IdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Log
@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private IdentityService identityService;

    @Autowired
    ReportRepository reportRepository;

    @RequestMapping(value = "/report", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<ReportDto>> getReport(){

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.ok(new ArrayList<ReportDto>());
        }

        InputStream fis = ReportController.class.getResourceAsStream("/reports/semyonov-report.sql");
        Scanner s = new Scanner(fis).useDelimiter("\\A");
        String query = s.hasNext() ? s.next() : "";

        List<ReportDto> reportDtos = reportRepository.report(query);

        return ResponseEntity.ok(reportDtos);
    }


    @RequestMapping(value = "/technical-report-by-jobs", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<TechnicalReportByJobsDto>> getTechnicalReportByJobs(){

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.ok(new ArrayList<TechnicalReportByJobsDto>());
        }

        InputStream fis = ReportController.class.getResourceAsStream("/reports/semyonov-report-by-job.sql");
        Scanner s = new Scanner(fis).useDelimiter("\\A");
        String query = s.hasNext() ? s.next() : "";

        List<TechnicalReportByJobsDto> reportDtos = reportRepository.technicalReportByJobs(query);

        return ResponseEntity.ok(reportDtos);
    }
    @RequestMapping(value = "/financialreport2022", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<FinancialReportDto>> getFinancialReport2022(){
        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.ok(new ArrayList<FinancialReportDto>());
        }
        InputStream fis = ReportController.class.getResourceAsStream("/reports/financial-report_contract2022.sql");
        Scanner s = new Scanner(fis).useDelimiter("\\A");
        String query = s.hasNext() ? s.next() : "";
        List<FinancialReportDto> reportDtos = reportRepository.financialReport(query);
        return ResponseEntity.ok(reportDtos);
    }

    @RequestMapping(value = "/financialreportsao2023", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<FinancialReportDto>> getFinancialReportSao2023(){
        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.ok(new ArrayList<FinancialReportDto>());
        }
        InputStream fis = ReportController.class.getResourceAsStream("/reports/financial-report_contract-sao-2023.sql");
        Scanner s = new Scanner(fis).useDelimiter("\\A");
        String query = s.hasNext() ? s.next() : "";
        List<FinancialReportDto> reportDtos = reportRepository.financialReport(query);
        return ResponseEntity.ok(reportDtos);
    }
    @RequestMapping(value = "/financialreportps2023", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<FinancialReportDto>> getFinancialReportPS2023(){
        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.ok(new ArrayList<FinancialReportDto>());
        }
        InputStream fis = ReportController.class.getResourceAsStream("/reports/financial-report_contract-ps-2023.sql");
        Scanner s = new Scanner(fis).useDelimiter("\\A");
        String query = s.hasNext() ? s.next() : "";
        List<FinancialReportDto> reportDtos = reportRepository.financialReport(query);
        return ResponseEntity.ok(reportDtos);
    }

    @RequestMapping(value = "/power-extended-financial-report-by-works", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<FinancialReportDto>> getPowerExtendedReportByWorks(){
        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.ok(new ArrayList<FinancialReportDto>());
        }
        InputStream fis = ReportController.class.getResourceAsStream("/reports/power-extended-financial-report-by-works.sql");
        Scanner s = new Scanner(fis).useDelimiter("\\A");
        String query = s.hasNext() ? s.next() : "";
        List<FinancialReportDto> reportDtos = reportRepository.financialReport(query);
        return ResponseEntity.ok(reportDtos);
    }
    @RequestMapping(value = "/power-extended-financial-report-by-jobs", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<FinancialReportDto>> getPowerExtendedReportByJobs(){
        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.ok(new ArrayList<FinancialReportDto>());
        }
        InputStream fis = ReportController.class.getResourceAsStream("/reports/power-extended-financial-report-by-jobs.sql");
        Scanner s = new Scanner(fis).useDelimiter("\\A");
        String query = s.hasNext() ? s.next() : "";
        List<FinancialReportDto> reportDtos = reportRepository.financialReport(query);
        return ResponseEntity.ok(reportDtos);
    }

    @RequestMapping(value = "/financialreport", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<FinancialReportDto>> getFinancialReport(){

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.ok(new ArrayList<FinancialReportDto>());
        }

        InputStream fis = ReportController.class.getResourceAsStream("/reports/financial-report.sql");
        Scanner s = new Scanner(fis).useDelimiter("\\A");
        String query = s.hasNext() ? s.next() : "";

        List<FinancialReportDto> reportDtos = reportRepository.financialReport(query);

        return ResponseEntity.ok(reportDtos);
    }

    @RequestMapping(value = "/extended-report-by-jobs2022", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<ExtendedReportByJobsDto>> getExtendedReportByJobs2022(){
        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.ok(new ArrayList<ExtendedReportByJobsDto>());
        }
        InputStream fis = ReportController.class.getResourceAsStream("/reports/extended-financial-report-by-job_contract2022.sql");
        Scanner s = new Scanner(fis).useDelimiter("\\A");
        String query = s.hasNext() ? s.next() : "";
        List<ExtendedReportByJobsDto> reportDtos = reportRepository.extendedReportByJobs(query);
        return ResponseEntity.ok(reportDtos);
    }

    @RequestMapping(value = "/extended-report-by-jobs-sao-2023", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<ExtendedReportByJobsDto>> getExtendedReportByJobsSao2023(){
        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.ok(new ArrayList<ExtendedReportByJobsDto>());
        }
        InputStream fis = ReportController.class.getResourceAsStream("/reports/extended-financial-report-by-job_contract-sao-2023.sql");
        Scanner s = new Scanner(fis).useDelimiter("\\A");
        String query = s.hasNext() ? s.next() : "";
        List<ExtendedReportByJobsDto> reportDtos = reportRepository.extendedReportByJobs(query);
        return ResponseEntity.ok(reportDtos);
    }

    @RequestMapping(value = "/extended-report-by-jobs-ps-2023", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<ExtendedReportByJobsDto>> getExtendedReportByJobsPS2023(){
        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.ok(new ArrayList<ExtendedReportByJobsDto>());
        }
        InputStream fis = ReportController.class.getResourceAsStream("/reports/extended-financial-report-by-job_contract-ps-2023.sql");
        Scanner s = new Scanner(fis).useDelimiter("\\A");
        String query = s.hasNext() ? s.next() : "";
        List<ExtendedReportByJobsDto> reportDtos = reportRepository.extendedReportByJobs(query);
        return ResponseEntity.ok(reportDtos);
    }

    @RequestMapping(value = "/extended-report-by-jobs", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<ExtendedReportByJobsDto>> getExtendedReportByJobs(){

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.ok(new ArrayList<ExtendedReportByJobsDto>());
        }

        InputStream fis = ReportController.class.getResourceAsStream("/reports/extended-financial-report-by-job.sql");
        Scanner s = new Scanner(fis).useDelimiter("\\A");
        String query = s.hasNext() ? s.next() : "";

        List<ExtendedReportByJobsDto> reportDtos = reportRepository.extendedReportByJobs(query);

        return ResponseEntity.ok(reportDtos);
    }
}
