package kz.kcell.flow.repository;

import kz.kcell.flow.repository.custom.ReportRepository;
import kz.kcell.flow.repository.dto.FinancialReportDto;
import kz.kcell.flow.repository.dto.ReportDto;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.IdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.io.InputStreamReader;
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

}
