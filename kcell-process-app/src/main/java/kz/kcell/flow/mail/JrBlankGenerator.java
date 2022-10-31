package kz.kcell.flow.mail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import kz.kcell.bpm.SetPricesDelegate;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Service
public class JrBlankGenerator {

    private static final Map<String, String> worksTitle = new HashMap<>();

    private static final Map<String, String> contractorsTitle =
        ((Supplier<Map<String, String>>) (() -> {
            Map<String, String> map = new HashMap<>();
            map.put("1", "ТОО Аврора Сервис");
            map.put("2", "ТОО AICOM");
            map.put("3", "ТОО Spectr energy group");
            map.put("4", "TOO Line System Engineering");
            map.put("5", "Kcell_region");
            map.put("6", "Алта Телеком");
            map.put("7", "Логиком");
            map.put("8", "Arlan SI");
            map.put("9", "ТОО Inter Service");
            map.put("10", "Forester-Hes Group");
            map.put("11", "Транстелеком");
            return Collections.unmodifiableMap(map);
        })).get();

    public byte[] generate(DelegateExecution delegateExecution) throws Exception {

        if (worksTitle.size() == 0) {
            String mainContract = (String) delegateExecution.getVariable("mainContract");
            ObjectMapper mapper = new ObjectMapper();
            InputStream fis = SetPricesDelegate.class.getResourceAsStream("/revision/" + ("Roll-outRevision2020".equals(mainContract) ? "newWorkPrice.json" : "workPrice.json"));
            InputStreamReader reader = new InputStreamReader(fis, "utf-8");
            ArrayNode json = (ArrayNode) mapper.readTree(reader);
            for (JsonNode workPrice : json) {
                worksTitle.put(workPrice.get("id").textValue(), workPrice.get("title").textValue());
            }
        }
        String siteAddress = (String) delegateExecution.getVariable("siteAddress");
        String cityName = (String) delegateExecution.getVariable("cityNameShow");
        String siteName = (String) delegateExecution.getVariable("siteName");
        String site_name = (String) delegateExecution.getVariable("site_name");
        String jrNumber = (String) delegateExecution.getVariable("jrNumber");
        String jobDescription = (String) delegateExecution.getVariable("jobDescription");
        String explanation = (String) delegateExecution.getVariable("explanation");
        String contractor = delegateExecution.getVariable("contractor").toString();
        String regionApproval = (String) delegateExecution.getVariable("regionApproval");
        String centralApproval = (String) delegateExecution.getVariable("centralApproval");
        Date requestDate = (Date) delegateExecution.getVariable("requestedDate");
        Date contractorJobAssignedDate = (Date) delegateExecution.getVariable("contractorJobAssignedDate");
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode jobWorks = (ArrayNode) mapper.readTree(delegateExecution.getVariableTyped("jobWorks").getValue().toString());
        JsonNode initiatorFull = mapper.readTree(delegateExecution.getVariableTyped("initiatorFull").getValue().toString());
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();

        XSSFFont arial9 = workbook.createFont();
        arial9.setFontName(HSSFFont.FONT_ARIAL);
        arial9.setFontHeightInPoints((short) 9);

        XSSFFont arialViolet9 = workbook.createFont();
        arialViolet9.setFontName(HSSFFont.FONT_ARIAL);
        arialViolet9.setFontHeightInPoints((short) 9);
        arialViolet9.setColor(HSSFColor.HSSFColorPredefined.VIOLET.getIndex());
        arialViolet9.setBold(true);

        CellStyle autoWrap = workbook.createCellStyle();
        autoWrap.setFont(arial9);
        autoWrap.setWrapText(true);

        XSSFFont boldArial = workbook.createFont();
        boldArial.setFontName(HSSFFont.FONT_ARIAL);
        boldArial.setFontHeightInPoints((short) 9);
        boldArial.setBold(true);

        XSSFFont boldArial11 = workbook.createFont();
        boldArial.setFontName(HSSFFont.FONT_ARIAL);
        boldArial.setFontHeightInPoints((short) 11);
        boldArial.setBold(true);

        CellStyle bold = workbook.createCellStyle();
        bold.setFont(boldArial);

        CellStyle center = workbook.createCellStyle();
        center.setFont(arial9);
        center.setAlignment(HorizontalAlignment.CENTER);

        CellStyle boldCenter = workbook.createCellStyle();
        boldCenter.setFont(boldArial);
        boldCenter.setAlignment(HorizontalAlignment.CENTER);

        CellStyle alignRight = workbook.createCellStyle(); //Create new style
        alignRight.setFont(arialViolet9);
        alignRight.setAlignment(HorizontalAlignment.RIGHT);

        CellStyle orangeBoldCenter = workbook.createCellStyle();
        orangeBoldCenter.setFont(boldArial11);
        orangeBoldCenter.setAlignment(HorizontalAlignment.CENTER);
        orangeBoldCenter.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        orangeBoldCenter.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Row row = sheet.createRow(0);
        row.createCell(0);
        Cell cell = row.createCell(1);
        cell.setCellValue("Технический департамент");
        cell.setCellStyle(center);

        row = sheet.createRow(1);
        cell = row.createCell(1);
        cell.setCellValue("Модернизация/оптимизация сайтов");
        cell.setCellStyle(boldCenter);

        row = sheet.createRow(2);

        cell = row.createCell(1);
        cell.setCellValue("No.:");
        cell.setCellStyle(bold);

        cell = row.createCell(2);
        cell.setCellValue("Rev.No.:");
        cell.setCellStyle(bold);

        cell = row.createCell(3);
        cell.setCellValue("Rev.Date:");
        cell.setCellStyle(bold);

        cell = row.createCell(4);
        cell.setCellValue("Page:");
        cell.setCellStyle(bold);

        row = sheet.createRow(3);
        row.createCell(1).setCellValue(siteName);
        row.createCell(2).setCellValue("");
        row.createCell(3).setCellValue("2");
        row.createCell(4).setCellValue("1 of 1");

        row = sheet.createRow(4);
        row.createCell(1).setCellValue("На исполнение:");
        row.createCell(2).setCellValue("Подтвержден:");
        row.createCell(3).setCellValue("Проверил:");
        row.createCell(4).setCellValue("Подготовил:");

        row = sheet.createRow(5);
        cell = row.createCell(0);
        cell.setCellStyle(autoWrap);
        cell.setCellValue("Для Внутреннего и Внешнего использования");

        row.createCell(1).setCellValue("Подрядчик");
        row.createCell(2).setCellValue("Заместитель ТД");
        row.createCell(3).setCellValue("Глава");
        row.createCell(4).setCellValue("Кокин Б.");

        row = sheet.createRow(6);
        cell = row.createCell(0);
        cell.setCellValue("Заявка на выполнение работ согласно контракта № ____ от ________");
        cell.setCellStyle(orangeBoldCenter);


        row = sheet.createRow(7);
        cell = row.createCell(0);
        cell.setCellValue("Номер заявки :");
        cell.setCellStyle(alignRight);

        row.createCell(1).setCellValue(jrNumber);

        row = sheet.createRow(8);
        cell = row.createCell(0);

        SimpleDateFormat datFormatter = new SimpleDateFormat("yyyy-MM-dd'T'H:m:s.S");
        String compareDateString = "2019-02-05T06:00:00.000";
        Date compareDate = datFormatter.parse(compareDateString);

        if (requestDate.after(compareDate)) {
            cell.setCellValue("Дата заявки :");
            cell.setCellStyle(alignRight);
            row.createCell(1).setCellValue(sdf.format(requestDate));
        } else {
            cell.setCellValue("Дата заявки :");
            cell.setCellStyle(alignRight);
            row.createCell(1).setCellValue(sdf.format(requestDate));
        }


        row = sheet.createRow(9);
        cell = row.createCell(0);
        cell.setCellValue("Город :");
        cell.setCellStyle(alignRight);
        row.createCell(1).setCellValue(cityName);

        row = sheet.createRow(10);
        cell = row.createCell(0);
        cell.setCellValue("Наименование сайта :");
        cell.setCellStyle(alignRight);

        if (site_name != null && !site_name.isEmpty()) {
            row.createCell(1).setCellValue(site_name);
        } else {
            row.createCell(1).setCellValue("Not found");
        }

        row = sheet.createRow(11);
        cell = row.createCell(0);
        cell.setCellValue("Адрес сайта :");
        cell.setCellStyle(alignRight);

        cell = row.createCell(1);
        cell.setCellValue(siteAddress);
        cell.setCellStyle(autoWrap);

        row = sheet.createRow(12);
        row.createCell(0).setCellValue("Заказанные пункты и их количество:" + (jobDescription != null ? jobDescription : ""));

        for (int i = 0; i < jobWorks.size(); i++) {
            row = sheet.createRow(13 + i);
            cell = row.createCell(0);
            String title = (worksTitle.get(jobWorks.get(i).get("sapServiceNumber").textValue()) != null ? worksTitle.get(jobWorks.get(i).get("sapServiceNumber").textValue()) : jobWorks.get(i).get("sapServiceNumber").textValue()) + " - " + jobWorks.get(i).get("quantity").numberValue().intValue();
            if (jobWorks.get(i).get("relatedSites") != null && jobWorks.get(i).get("relatedSites").isArray()) {
                String relatedSites = "";
                for (JsonNode rs : jobWorks.get(i).get("relatedSites")) {
                    if (rs.get("site_name") != null) {
                        relatedSites += (!rs.get("site_name").textValue().isEmpty() ? rs.get("site_name").textValue() : rs.get("siteName").textValue()) + ", ";
                    } else {
                        relatedSites += (rs.get("siteName").textValue()) + ", ";
                    }
                }
                if (relatedSites.length() > 0) {
                    title += ", on sites: " + relatedSites.substring(0, relatedSites.length() - 2);
                }
                if (jobWorks.get(i).has("materialsProvidedBy")) {
                    title += "; Materials from: " + jobWorks.get(i).get("materialsProvidedBy").asText();
                }
            }
            cell.setCellStyle(autoWrap);
            cell.setCellValue(title);

            if (title.length() > 100) {
                row.setHeight((short) (row.getHeight() * (title.length() / 100 + 1)));
            }
        }

        row = sheet.createRow(13 + jobWorks.size());
        row.createCell(0).setCellValue("Описание работ(ы):");

        row = sheet.createRow(14 + jobWorks.size());
        row.setHeight((short) 1200);

        cell = row.createCell(0);
        cell.setCellStyle(autoWrap);
        cell.setCellValue(explanation != null ? explanation : "");


        row = sheet.createRow(16 + jobWorks.size());
        row.createCell(2).setCellValue("Заказана:");

        row = sheet.createRow(17 + jobWorks.size());
        row.createCell(0).setCellValue("Исполнитель:");
        row.createCell(2).setCellValue(initiatorFull.get("firstName").textValue() + " " + initiatorFull.get("lastName").textValue());

        row = sheet.createRow(18 + jobWorks.size());
        row.createCell(0).setCellValue((contractorsTitle.get(contractor) != null ? contractorsTitle.get(contractor) : "Not found"));
        row.createCell(2).setCellValue("             (Должность,Фамилия Имя, подпись)");

        row = sheet.createRow(20 + jobWorks.size());
        row.createCell(2).setCellValue("Подтверждена:");

        row = sheet.createRow(21 + jobWorks.size());
        row.createCell(2).setCellValue(((regionApproval != null && !regionApproval.isEmpty() && !regionApproval.equals("null")) ? regionApproval : "") + ((centralApproval != null && !centralApproval.isEmpty() && !centralApproval.equals("null")) ? (", " + centralApproval) : ""));

        row = sheet.createRow(22 + jobWorks.size());
        row.createCell(2).setCellValue("             (Должность,Фамилия Имя, подпись)");

        row = sheet.createRow(23 + jobWorks.size());
        row.createCell(0).setCellValue("");

        row = sheet.createRow(24 + jobWorks.size());
        row.createCell(0).setCellValue("Получена на исполнение:");

        row = sheet.createRow(25 + jobWorks.size());
        row.createCell(0).setCellValue("");

        row = sheet.createRow(26 + jobWorks.size());
        row.createCell(0).setCellValue("             (Должность,Фамилия Имя, подпись представителя подрядчика)");

        //
        sheet.addMergedRegion(new CellRangeAddress(6, 6, 0, 4));
        sheet.addMergedRegion(new CellRangeAddress(7, 7, 1, 4));
        sheet.addMergedRegion(new CellRangeAddress(8, 8, 1, 4));

        //

        sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 4));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 4));
        sheet.addMergedRegion(new CellRangeAddress(0, 4, 0, 0));
        sheet.addMergedRegion(new CellRangeAddress(9, 9, 1, 4));
        sheet.addMergedRegion(new CellRangeAddress(10, 10, 1, 4));
        sheet.addMergedRegion(new CellRangeAddress(11, 11, 1, 4));
        sheet.addMergedRegion(new CellRangeAddress(14 + jobWorks.size(), 14 + jobWorks.size(), 0, 4));

        for (int i = 0; i < jobWorks.size(); i++) {
            sheet.addMergedRegion(new CellRangeAddress(13 + i, 13 + i, 0, 4));
        }

        sheet.setColumnWidth(0, 4718);
        sheet.setColumnWidth(1, 5586);
        sheet.setColumnWidth(2, 4206);
        sheet.setColumnWidth(3, 2926);
        sheet.setColumnWidth(4, 2670);

        Map<String, Object> fullBorder = new HashMap<>();
        fullBorder.put(CellUtil.BORDER_LEFT, BorderStyle.THIN);
        fullBorder.put(CellUtil.BORDER_RIGHT, BorderStyle.THIN);
        fullBorder.put(CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
        fullBorder.put(CellUtil.BORDER_TOP, BorderStyle.THIN);

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                if (sheet.getRow(i).getCell(j) != null) {
                    CellUtil.setCellStyleProperties(sheet.getRow(i).getCell(j), fullBorder);
                } else {
                    sheet.getRow(i).createCell(j);
                    CellUtil.setCellStyleProperties(sheet.getRow(i).getCell(j), fullBorder);
                }
            }
        }

        cell = sheet.getRow(6).getCell(4);
        if (cell == null) {
            cell = sheet.getRow(6).createCell(4);
        }
        CellUtil.setCellStyleProperty(cell, CellUtil.BORDER_RIGHT, BorderStyle.THIN);


        CellUtil.setCellStyleProperty(sheet.getRow(7).getCell(0), CellUtil.BORDER_TOP, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(7).getCell(1), CellUtil.BORDER_TOP, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(7).createCell(2), CellUtil.BORDER_TOP, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(7).createCell(3), CellUtil.BORDER_TOP, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(7).createCell(4), CellUtil.BORDER_TOP, BorderStyle.THIN);

        CellUtil.setCellStyleProperty(sheet.getRow(7).getCell(0), CellUtil.BORDER_LEFT, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(8).getCell(0), CellUtil.BORDER_LEFT, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(9).getCell(0), CellUtil.BORDER_LEFT, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(10).getCell(0), CellUtil.BORDER_LEFT, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(11).getCell(0), CellUtil.BORDER_LEFT, BorderStyle.THIN);

        CellUtil.setCellStyleProperty(sheet.getRow(11).getCell(0), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(11).getCell(1), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(11).createCell(2), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(11).createCell(3), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(11).createCell(4), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);

        CellUtil.setCellStyleProperty(sheet.getRow(7).getCell(4), CellUtil.BORDER_RIGHT, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(8).createCell(4), CellUtil.BORDER_RIGHT, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(9).createCell(4), CellUtil.BORDER_RIGHT, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(10).createCell(4), CellUtil.BORDER_RIGHT, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(11).getCell(4), CellUtil.BORDER_RIGHT, BorderStyle.THIN);

        cell = sheet.getRow(12).getCell(4);
        if (cell == null) {
            cell = sheet.getRow(12).createCell(4);
        }
        CellUtil.setCellStyleProperty(cell, CellUtil.BORDER_RIGHT, BorderStyle.THIN);

        for (int i = 0; i < jobWorks.size(); i++) {
            if (sheet.getRow(13 + i).getCell(0) != null) {
                CellUtil.setCellStyleProperty(sheet.getRow(13 + i).getCell(0), CellUtil.BORDER_LEFT, BorderStyle.THIN);
            } else {
                CellUtil.setCellStyleProperty(sheet.getRow(13 + i).createCell(0), CellUtil.BORDER_LEFT, BorderStyle.THIN);
            }
            CellUtil.setCellStyleProperty(sheet.getRow(13 + i).createCell(4), CellUtil.BORDER_RIGHT, BorderStyle.THIN);
        }

        CellUtil.setCellStyleProperty(sheet.getRow(13 + jobWorks.size()).getCell(0), CellUtil.BORDER_TOP, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(13 + jobWorks.size()).createCell(1), CellUtil.BORDER_TOP, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(13 + jobWorks.size()).createCell(2), CellUtil.BORDER_TOP, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(13 + jobWorks.size()).createCell(3), CellUtil.BORDER_TOP, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(13 + jobWorks.size()).createCell(4), CellUtil.BORDER_TOP, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(13 + jobWorks.size()).getCell(4), CellUtil.BORDER_RIGHT, BorderStyle.THIN);

        CellUtil.setCellStyleProperty(sheet.getRow(14 + jobWorks.size()).getCell(0), CellUtil.BORDER_TOP, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(14 + jobWorks.size()).createCell(1), CellUtil.BORDER_TOP, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(14 + jobWorks.size()).createCell(2), CellUtil.BORDER_TOP, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(14 + jobWorks.size()).createCell(3), CellUtil.BORDER_TOP, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(14 + jobWorks.size()).createCell(4), CellUtil.BORDER_TOP, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(14 + jobWorks.size()).getCell(4), CellUtil.BORDER_RIGHT, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(14 + jobWorks.size()).getCell(0), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(14 + jobWorks.size()).getCell(1), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(14 + jobWorks.size()).getCell(2), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(14 + jobWorks.size()).getCell(3), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(14 + jobWorks.size()).getCell(4), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);


        CellUtil.setCellStyleProperty((sheet.getRow(15 + jobWorks.size()) == null ? sheet.createRow(15 + jobWorks.size()) : sheet.getRow(15 + jobWorks.size())).createCell(4), CellUtil.BORDER_RIGHT, BorderStyle.THIN);

        CellUtil.setCellStyleProperty(sheet.getRow(16 + jobWorks.size()).createCell(0), CellUtil.BORDER_TOP, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(16 + jobWorks.size()).createCell(1), CellUtil.BORDER_TOP, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(16 + jobWorks.size()).getCell(2), CellUtil.BORDER_TOP, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(16 + jobWorks.size()).getCell(2), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(16 + jobWorks.size()).createCell(3), CellUtil.BORDER_TOP, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(16 + jobWorks.size()).createCell(4), CellUtil.BORDER_TOP, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(16 + jobWorks.size()).getCell(4), CellUtil.BORDER_RIGHT, BorderStyle.THIN);

        CellUtil.setCellStyleProperty(sheet.getRow(17 + jobWorks.size()).getCell(0), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(17 + jobWorks.size()).getCell(2), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(17 + jobWorks.size()).createCell(3), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(17 + jobWorks.size()).createCell(4), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(17 + jobWorks.size()).getCell(4), CellUtil.BORDER_RIGHT, BorderStyle.THIN);


//        CellUtil.setCellStyleProperty(sheet.getRow(19 + jobWorks.size()).createCell(2), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);

        CellUtil.setCellStyleProperty(sheet.getRow(20 + jobWorks.size()).getCell(2), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(20 + jobWorks.size()).getCell(2), CellUtil.BORDER_TOP, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(20 + jobWorks.size()).createCell(3), CellUtil.BORDER_TOP, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(20 + jobWorks.size()).createCell(4), CellUtil.BORDER_TOP, BorderStyle.THIN);
//
        CellUtil.setCellStyleProperty(sheet.getRow(22 + jobWorks.size()).getCell(2), CellUtil.BORDER_TOP, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(22 + jobWorks.size()).createCell(3), CellUtil.BORDER_TOP, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(22 + jobWorks.size()).createCell(4), CellUtil.BORDER_TOP, BorderStyle.THIN);

        CellUtil.setCellStyleProperty(sheet.getRow(23 + jobWorks.size()).getCell(0), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);

        CellUtil.setCellStyleProperty(sheet.getRow(24 + jobWorks.size()).getCell(0), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);

//        CellUtil.setCellStyleProperty(sheet.getRow(21 + jobWorks.size()).getCell(2), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
//        CellUtil.setCellStyleProperty(sheet.getRow(21 + jobWorks.size()).createCell(3), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
//        CellUtil.setCellStyleProperty(sheet.getRow(21 + jobWorks.size()).getCell(4), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);


//        CellUtil.setCellStyleProperty(sheet.getRow(26 + jobWorks.size()).getCell(0), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
//        CellUtil.setCellStyleProperty(sheet.getRow(26 + jobWorks.size()).createCell(1), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
//        CellUtil.setCellStyleProperty(sheet.getRow(26 + jobWorks.size()).createCell(2), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
//        CellUtil.setCellStyleProperty(sheet.getRow(26 + jobWorks.size()).createCell(3), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
//        CellUtil.setCellStyleProperty(sheet.getRow(26 + jobWorks.size()).createCell(4), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
//
//        CellUtil.setCellStyleProperty(sheet.getRow(16 + jobWorks.size()).getCell(2), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
//

//
//
//        CellUtil.setCellStyleProperty(sheet.getRow(23 + jobWorks.size()).createCell(0), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
//        CellUtil.setCellStyleProperty(sheet.getRow(24 + jobWorks.size()).getCell(0), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
//

        CellUtil.setCellStyleProperty(sheet.getRow(26 + jobWorks.size()).getCell(0), CellUtil.BORDER_TOP, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(26 + jobWorks.size()).createCell(1), CellUtil.BORDER_TOP, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(26 + jobWorks.size()).createCell(2), CellUtil.BORDER_TOP, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(26 + jobWorks.size()).getCell(0), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(26 + jobWorks.size()).getCell(1), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(26 + jobWorks.size()).getCell(2), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(26 + jobWorks.size()).createCell(3), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
        CellUtil.setCellStyleProperty(sheet.getRow(26 + jobWorks.size()).createCell(4), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);


        int[] rows = new int[] {18,19, 20, 21, 22, 23, 24, 25,26};

        for (int indx : rows) {
            row = sheet.getRow(indx + jobWorks.size());
            if (row == null) {
                row = sheet.createRow(indx + jobWorks.size());
            }
            cell = row.getCell(4);

            if (cell == null) {
                cell = sheet.getRow(indx + jobWorks.size()).createCell(4);
            }
            CellUtil.setCellStyleProperty(cell, CellUtil.BORDER_RIGHT, BorderStyle.THIN);
        }


        //IMAGE BLOCK
        InputStream inputStream = this.getClass().getResourceAsStream("/images/kcell_logo_90.png");
        byte[] bytes = IOUtils.toByteArray(inputStream);
        int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
        inputStream.close();

        CreationHelper helper = workbook.getCreationHelper();
        Drawing drawing = sheet.createDrawingPatriarch();

        //Create an anchor that is attached to the worksheet
        ClientAnchor anchor = helper.createClientAnchor();

        anchor.setCol1(0);
        anchor.setRow1(1);
        anchor.setCol2(1);
        anchor.setRow2(3);

        //Creates a picture
        Picture pict = drawing.createPicture(anchor, pictureIdx);
        //Reset the image to the original size
        pict.resize();
        pict.resize(0.5);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        out.close();
        return out.toByteArray();
    }
}
