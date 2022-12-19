package kz.kcell.flow.dismantleReplace;

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
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatisticsService {
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
        row.createCell(2).setCellValue("Region head approve");
        row.createCell(4).setCellValue("Modify request");
        row.createCell(6).setCellValue("Central group \"Central Leasing Unit\"");
        row.createCell(8).setCellValue("Central group \"Central Planning Unit\"");
        row.createCell(10).setCellValue("Central group \"Central SAO Unit\"");
        row.createCell(12).setCellValue("Central group \"Central TNU Unit\"");
        row.createCell(14).setCellValue("Central group \"Central SFM Unit\"");
        row.createCell(16).setCellValue("Completion notification");
        row = sheet.createRow(1);
        for (int i = 2; i <= 14; i += 2) {
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

        int[] cellsWithFontB12 = new int[]{0, 1, 2, 4, 6, 8, 10, 12, 14, 16};
        for (int cellNum : cellsWithFontB12) {
            row = sheet.getRow(0);
            Cell cell = row.getCell(cellNum);
            if (cellNum < 2 || cellNum > 14) {
                cell.setCellStyle(headerCellStyle1);
            } else {
                cell.setCellStyle(headerCellStyle2);
            }
            CellUtil.setFont(cell, fontB12);
        }

        sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 16, 16));

        // ------ Data -------------------------------------------------------------------------------------------------
        for (int i = 0; i < statisticsList.size(); i++) {
            Statistics stat = statisticsList.get(i);
            row = sheet.createRow(2 + i);
            row.createCell(0).setCellValue(stat.getJrNumber());
            row.createCell(1).setCellValue(stat.getSiteName());
            row.createCell(2).setCellValue(stat.getRegionHeadApprove().getStartTime());
            row.createCell(3).setCellValue(stat.getRegionHeadApprove().getEndTime());
            row.createCell(4).setCellValue(stat.getModifyRequest().getStartTime());
            row.createCell(5).setCellValue(stat.getModifyRequest().getEndTime());
            row.createCell(6).setCellValue(stat.getCentralLeasingUnit().getStartTime());
            row.createCell(7).setCellValue(stat.getCentralLeasingUnit().getEndTime());
            row.createCell(8).setCellValue(stat.getCentralPlanningUni().getStartTime());
            row.createCell(9).setCellValue(stat.getCentralPlanningUni().getEndTime());
            row.createCell(10).setCellValue(stat.getCentralSaoUnit().getStartTime());
            row.createCell(11).setCellValue(stat.getCentralSaoUnit().getEndTime());
            row.createCell(12).setCellValue(stat.getCentralTnuUnit().getStartTime());
            row.createCell(13).setCellValue(stat.getCentralTnuUnit().getEndTime());
            row.createCell(14).setCellValue(stat.getCentralSfmUnit().getStartTime());
            row.createCell(15).setCellValue(stat.getCentralSfmUnit().getEndTime());
            row.createCell(16).setCellValue(stat.getCompletionNotification());
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
            for (int j = 0; j < 17; j++) {
                CellUtil.setCellStyleProperties(row.getCell(j) != null ? row.getCell(j) : row.createCell(j), properties);
            }
        }

        for (int i = 0; i < 17; i++) {
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

            List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery().processInstanceBusinessKeyIn(req.getBusinessKey()).list();
            statistics.setRegionHeadApprove(statistics.fromTasks("region head approve", tasks));
            statistics.setModifyRequest(statistics.fromTasks("Modify request", tasks));
            statistics.setCentralLeasingUnit(statistics.fromTasks("central group \"Central Leasing Unit\"", tasks));
            statistics.setCentralPlanningUni(statistics.fromTasks("central group \"Central Planning Unit\"", tasks));
            statistics.setCentralSaoUnit(statistics.fromTasks("central group \"Central SAO Unit\"", tasks));
            statistics.setCentralTnuUnit(statistics.fromTasks("central group \"Central Transmission Unit\"", tasks));
            statistics.setCentralSfmUnit(statistics.fromTasks("central group \"Central S&FM Unit\"", tasks));

            statisticsList.add(statistics);
        }
        return statisticsList;
    }

    @Getter
    @Setter
    class Statistics {
        private String jrNumber;
        private String siteName;
        private StatisticsTask regionHeadApprove;
        private StatisticsTask modifyRequest;
        private StatisticsTask centralLeasingUnit;
        private StatisticsTask centralPlanningUni;
        private StatisticsTask centralSaoUnit;
        private StatisticsTask centralTnuUnit;
        private StatisticsTask centralSfmUnit;
        private String completionNotification;

        @Getter
        @Setter
        class StatisticsTask {
            private String startTime;
            private String endTime;
        }

        StatisticsTask fromTasks(String taskName, List<HistoricTaskInstance> tasks) {
            Optional<HistoricTaskInstance> activity = tasks.stream().filter(e -> taskName.equals(e.getName())).findFirst();
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
