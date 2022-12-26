package kz.kcell.flow.leasing;

import kz.kcell.flow.dismantleReplace.dto.StatisticsRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LeasingStatisticsService {
    private final HistoryService historyService;

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    public byte[] generateStatistics(List<StatisticsRequest> requests) throws Exception {
        List<Statistics> statisticsList = collectStatistics(requests);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();

        XSSFFont fontB12 = workbook.createFont();
        fontB12.setFontHeightInPoints((short) 12);
        fontB12.setBold(true);

        XSSFFont font12 = workbook.createFont();
        font12.setFontName(XSSFFont.DEFAULT_FONT_NAME);
        font12.setFontHeightInPoints((short) 12);

        CellStyle headerCellStyle1 = sheet.getWorkbook().createCellStyle();
        headerCellStyle1.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        headerCellStyle1.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle headerCellStyle2 = sheet.getWorkbook().createCellStyle();
        headerCellStyle2.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.index);
        headerCellStyle2.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // ------ Header ----------------------------------------------------------------------------------------------
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("Jr Number");
        row.createCell(1).setCellValue("Sitename");
        row.createCell(2).setCellValue("Approved NCP");
        row.createCell(4).setCellValue("Create new candidate");
        row.createCell(6).setCellValue("Regional Power approval");
        row.createCell(8).setCellValue("Regional TNU approval");
        row.createCell(10).setCellValue("Central TNU approval");
        row.createCell(12).setCellValue("Leasing approval");
        row.createCell(14).setCellValue("Candidate Leasing");
        row.createCell(16).setCellValue("Far End Leasing");
        row.createCell(18).setCellValue("Candidate Contract");
        row.createCell(20).setCellValue("Far End Contract");
        row.createCell(22).setCellValue("Prepare Power");
        row.createCell(24).setCellValue("Prepare VSD");
        row.createCell(26).setCellValue("Prepare TSD");
        row.createCell(28).setCellValue("Installation process");
        row.createCell(30).setCellValue("Contract Signing (Candidate)");
        row.createCell(32).setCellValue("Contract Signing (Far End)");
        row.createCell(34).setCellValue("Complete notification");
        row = sheet.createRow(1);
        for (int i = 2; i <= 32; i += 2) {
            Cell cell = row.createCell(i);
            cell.setCellValue("Assigned date");
            cell.setCellStyle(headerCellStyle1);
            CellUtil.setFont(cell, font12);

            cell = row.createCell(i + 1);
            cell.setCellValue("Complete date");
            cell.setCellStyle(headerCellStyle1);
            CellUtil.setFont(cell, font12);

            sheet.addMergedRegion(new CellRangeAddress(0, 0, i, i + 1));
        }

        int[] cellsWithFontB12 = new int[]{0, 1, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34};
        for (int cellNum : cellsWithFontB12) {
            row = sheet.getRow(0);
            Cell cell = row.getCell(cellNum);
            if (cellNum < 2 || cellNum > 32) {
                cell.setCellStyle(headerCellStyle1);
            } else {
                cell.setCellStyle(headerCellStyle2);
            }
            CellUtil.setFont(cell, fontB12);
        }

        sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 34, 34));

        // ------ Data -------------------------------------------------------------------------------------------------
        for (int i = 0; i < statisticsList.size(); i++) {
            Statistics stat = statisticsList.get(i);
            row = sheet.createRow(2 + i);
            row.createCell(0).setCellValue(stat.getJrNumber());
            row.createCell(1).setCellValue(stat.getSiteName());
            row.createCell(2).setCellValue(stat.getAddOutNCP().getStartTime());
            row.createCell(3).setCellValue(stat.getAddOutNCP().getEndTime());
            row.createCell(4).setCellValue(stat.getCreateNewCandidate().getStartTime());
            row.createCell(5).setCellValue(stat.getCreateNewCandidate().getEndTime());
            row.createCell(6).setCellValue(stat.getPreliminaryEstimate().getStartTime());
            row.createCell(7).setCellValue(stat.getPreliminaryEstimate().getEndTime());
            row.createCell(8).setCellValue(stat.getCheckAndApproveFE().getStartTime());
            row.createCell(9).setCellValue(stat.getCheckAndApproveFE().getEndTime());
            row.createCell(10).setCellValue(stat.getConfirmPossibilityTransmissionChannel().getStartTime());
            row.createCell(11).setCellValue(stat.getConfirmPossibilityTransmissionChannel().getEndTime());
            row.createCell(12).setCellValue(stat.getConfirmPermissionLeasingProcess().getStartTime());
            row.createCell(13).setCellValue(stat.getConfirmPermissionLeasingProcess().getEndTime());
            row.createCell(14).setCellValue(stat.getConfirmPermissionBaseStation().getStartTime());
            row.createCell(15).setCellValue(stat.getConfirmPermissionBaseStation().getEndTime());
            row.createCell(16).setCellValue(stat.getConfirmPermissionInstallFE().getStartTime());
            row.createCell(17).setCellValue(stat.getConfirmPermissionInstallFE().getEndTime());
            row.createCell(18).setCellValue(stat.getUploadLeaseContract().getStartTime());
            row.createCell(19).setCellValue(stat.getUploadLeaseContract().getEndTime());
            row.createCell(20).setCellValue(stat.getUploadLeaseContractFE().getStartTime());
            row.createCell(21).setCellValue(stat.getUploadLeaseContractFE().getEndTime());
            //row.createCell(22).setCellValue(stat.get.getAssignedDate());
            //row.createCell(23).setCellValue(stat.get.getCompleteDate());
            row.createCell(24).setCellValue(stat.getUploadVSD().getStartTime());
            row.createCell(25).setCellValue(stat.getUploadVSD().getEndTime());
            row.createCell(26).setCellValue(stat.getUploadTSD().getStartTime());
            row.createCell(27).setCellValue(stat.getUploadTSD().getEndTime());
            //row.createCell(28).setCellValue(stat.get.getAssignedDate());
            //row.createCell(29).setCellValue(stat.get.getCompleteDate());
            row.createCell(30).setCellValue(stat.getFillDetailsCandidateContract().getStartTime());
            row.createCell(31).setCellValue(stat.getFillDetailsCandidateContract().getEndTime());
            row.createCell(32).setCellValue(stat.getFillDetailsFarendContract().getStartTime());
            row.createCell(33).setCellValue(stat.getFillDetailsFarendContract().getEndTime());
            row.createCell(34).setCellValue(stat.getCompletionNotification());
        }

        // ------ Borders ---------------------------------------------------------------------------------------------
        Map<String, Object> properties = new HashMap<>();
        properties.put(CellUtil.WRAP_TEXT, true);
        properties.put(CellUtil.BORDER_TOP, BorderStyle.THIN);
        properties.put(CellUtil.BORDER_LEFT, BorderStyle.THIN);
        properties.put(CellUtil.BORDER_RIGHT, BorderStyle.THIN);
        properties.put(CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
        properties.put(CellUtil.VERTICAL_ALIGNMENT, VerticalAlignment.TOP);

        for (int i = 0; i < 2 + statisticsList.size(); i++) {
            row = sheet.getRow(i);
            for (int j = 0; j < 35; j++) {
                CellUtil.setCellStyleProperties(row.getCell(j) != null ? row.getCell(j) : row.createCell(j), properties);
            }
        }

        for (int i = 0; i < 35; i++) {
            sheet.autoSizeColumn(i);
        }
        sheet.getRow(0).setHeight((short) 800);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        out.close();
        return out.toByteArray();
    }

    private List<Statistics> collectStatistics(List<StatisticsRequest> requests) {
        List<Statistics> statisticsList = new ArrayList<>();
        for (StatisticsRequest req : requests) {
            Statistics statistics = new Statistics();
            statistics.setJrNumber(req.getBusinessKey());
            statistics.setSiteName(req.getSiteName());
            statistics.setCompletionNotification(req.getEndTime() != null ? DATE_FORMAT.format(req.getEndTime()) : null);

            List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery().processInstanceId(req.getProcessInstanceId()).list();
            statistics.setAddOutNCP(statistics.fromTasks("Add/Out NCP to Roll Out", activities));
            statistics.setCreateNewCandidate(statistics.fromTasks("Create new candidate site", activities));
            statistics.setPreliminaryEstimate(statistics.fromTasks("Preliminary estimate of Technical Capability from RES", activities));
            statistics.setCheckAndApproveFE(statistics.fromTasks("Check and approve FE", activities));
            statistics.setConfirmPossibilityTransmissionChannel(statistics.fromTasks("Confirm the possibility of organizing a transmission channel", activities));
            statistics.setConfirmPermissionLeasingProcess(statistics.fromTasks("Confirm permission to start the Leasing process", activities));
            statistics.setConfirmPermissionBaseStation(statistics.fromTasks("Confirm permission to install Base Station", activities));
            statistics.setConfirmPermissionInstallFE(statistics.fromTasks("Confirm permission to install FE", activities));
            statistics.setUploadLeaseContract(statistics.fromTasks("Upload the Lease Contract signed by the owner. Fill the Contract details", activities));
            statistics.setUploadLeaseContractFE(statistics.fromTasks("Upload the Lease Contract for FE signed by the owner. Fill the Contract details", activities));
            statistics.setContractApprovalFE(statistics.fromTasks("CONTRACT APPROVAL FE", activities));
            statistics.setUploadVSD(statistics.fromTasks("Upload VSD file & fill Cell Antenna Information form", activities));
            statistics.setUploadTSD(statistics.fromTasks("Upload TSD file", activities));
            statistics.setInstallationWorks(statistics.fromTasks("INSTALLATION WORKS", activities));
            statistics.setFillDetailsCandidateContract(statistics.fromTasks("Fill in the details of the Candidate Contract", activities));
            statistics.setFillDetailsFarendContract(statistics.fromTasks("Fill in the details of the Farend Contract", activities));

            statisticsList.add(statistics);
        }
        return statisticsList;
    }

    @Getter
    @Setter
    class Statistics {
        private String jrNumber;
        private String siteName;
        private StatisticsTask addOutNCP;
        private StatisticsTask createNewCandidate;
        private StatisticsTask preliminaryEstimate;
        private StatisticsTask checkAndApproveFE;
        private StatisticsTask confirmPossibilityTransmissionChannel;
        private StatisticsTask confirmPermissionLeasingProcess;
        private StatisticsTask confirmPermissionBaseStation;
        private StatisticsTask confirmPermissionInstallFE;
        private StatisticsTask uploadLeaseContract;
        private StatisticsTask uploadLeaseContractFE;
        private StatisticsTask contractApprovalFE;
        private StatisticsTask uploadVSD;
        private StatisticsTask uploadTSD;
        private StatisticsTask installationWorks;
        private StatisticsTask fillDetailsCandidateContract;
        private StatisticsTask fillDetailsFarendContract;
        private String completionNotification;

        @Getter
        @Setter
        class StatisticsTask {
            private String startTime;
            private String endTime;
        }

        StatisticsTask fromTasks(String activityName, List<HistoricActivityInstance> activities) {
            Comparator<HistoricActivityInstance> comparator = Comparator.comparing(HistoricActivityInstance::getStartTime);
            Optional<HistoricActivityInstance> activity = activities.stream()
                .filter(e -> activityName.equals(e.getActivityName()) && e.getStartTime() != null)
                .max(comparator);
            StatisticsTask statisticsTask = new StatisticsTask();
            if (activity.isPresent()) {
                String startTime = activity.get().getStartTime() != null ? DATE_FORMAT.format(activity.get().getStartTime()) : null;
                String endTime = activity.get().getEndTime() != null ? DATE_FORMAT.format(activity.get().getEndTime()) : null;
                statisticsTask.setStartTime(startTime);
                statisticsTask.setEndTime(endTime);
            }
            return statisticsTask;
        }
    }
}
