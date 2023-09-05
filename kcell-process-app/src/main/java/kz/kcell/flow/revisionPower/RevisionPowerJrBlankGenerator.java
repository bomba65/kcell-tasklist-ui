package kz.kcell.flow.revisionPower;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.kcell.flow.files.Minio;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.plugin.variable.SpinValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

@Service("revisionPowerJrBlankGenerator")
public class RevisionPowerJrBlankGenerator implements JavaDelegate {

    @Autowired
    private Minio minioClient;

    private final String SHEET_NAME = "power-revision-jr-blank";

    private static final Map<String, String> SUBCONTRACTOR_MAP = new HashMap<String,String>() {{
        put("Договор № 758437/2022/1 / CM 99377 от 01.12.2022", "ТОО \"ar.commm\"");
        put("Договор №758437/2022/2 / СM 99412 от 05.12.2022", "ТОО \"ALFA MET SERVICE\"");
        put("Договор №758437/2022/3 / CM 99419 от 05.12.2022", "ТОО \"ALTA Telecom\"");
    }};

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String jrNumber = (String) delegateExecution.getVariable("jrNumber");
        byte[] jrBlank = generateJrBlank(delegateExecution);

        String path = delegateExecution.getProcessInstanceId() + "/" + jrNumber + ".xlsx";
        String fileName = jrNumber + ".xlsx";
        ByteArrayInputStream bis = new ByteArrayInputStream(jrBlank);
        minioClient.saveFile(path, bis, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        bis.close();
        String json = "{\"name\" :\"" + fileName + "\",\"path\" : \"" + path + "\"}";
        delegateExecution.setVariable("jrBlank", SpinValues.jsonValue(json));
    }

    private byte[] generateJrBlank(DelegateExecution delegateExecution) throws Exception {
        String jrNumber = (String) delegateExecution.getVariable("jrNumber");
        Date jrOrderedDate = (Date) delegateExecution.getVariable("jrOrderedDate");
        String siteName = (String) (delegateExecution.getVariable("Site_Name") != null
            ? delegateExecution.getVariable("Site_Name") : delegateExecution.getVariable("Switch_Name"));
        String siteAddress = (String) delegateExecution.getVariable("address");
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode jobs = (ArrayNode) mapper.readTree(delegateExecution.getVariableTyped("jobs").getValue().toString());
        ObjectNode contract = (ObjectNode) mapper.readTree(delegateExecution.getVariableTyped("contract").getValue().toString());
        String subcontractor = SUBCONTRACTOR_MAP.get(contract.get("name").textValue());
        ObjectNode initiatorFull = (ObjectNode) mapper.readTree(delegateExecution.getVariableTyped("initiatorFull").getValue().toString());
        String initiator = initiatorFull.get("firstName").textValue() + " " + initiatorFull.get("lastName").textValue();
        String explanation = (String) delegateExecution.getVariable("explanation");

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(SHEET_NAME);

        sheet.createRow(1).createCell(3).setCellValue("ICT Department, K'Cell");
        sheet.createRow(2).createCell(3).setCellValue("ACCEPTANCE  FORM  FOR  SITE  REVISION");
        sheet.createRow(3).createCell(3).setCellValue("No.:");
        sheet.getRow(3).createCell(4).setCellValue("Rev.No.:");
        sheet.getRow(3).createCell(5).setCellValue("Rev.Date:");
        sheet.getRow(3).createCell(6).setCellValue("Page:");
        sheet.createRow(4).createCell(6).setCellValue("1 of 1");
        sheet.createRow(5).createCell(3).setCellValue("In Attention of:");
        sheet.getRow(5).createCell(4).setCellValue("Approved By:");
        sheet.getRow(5).createCell(5).setCellValue("Reviewed By:");
        sheet.getRow(5).createCell(6).setCellValue("Prepared By:");
        sheet.createRow(6).createCell(3).setCellValue("Contractors");
        sheet.getRow(6).createCell(4).setCellValue("Department Director");
        sheet.getRow(6).createCell(5).setCellValue("Manager");

        sheet.createRow(7).createCell(1).setCellValue("I.");
        sheet.getRow(7).createCell(2).setCellValue("Unit & Group Name:");
        sheet.createRow(8).createCell(2).setCellValue("Requested By:");
        sheet.getRow(8).createCell(3).setCellValue("АО \"Kcell\"");
        sheet.createRow(9).createCell(2).setCellValue("Job Request Number :");
        sheet.getRow(9).createCell(3).setCellValue(jrNumber);
        sheet.createRow(10).createCell(2).setCellValue("Job Request Date :");
        sheet.getRow(10).createCell(3).setCellValue(DateFormatUtils.format(jrOrderedDate, "dd.MM.yyyy"));
        sheet.createRow(11).createCell(2).setCellValue("Site Name :");
        sheet.getRow(11).createCell(3).setCellValue(siteName);
        sheet.createRow(12).createCell(2).setCellValue("Site Address :");
        sheet.getRow(12).createCell(3).setCellValue(siteAddress);

        sheet.createRow(14).createCell(1).setCellValue("II. Ordered Job Description");
        sheet.createRow(15).createCell(2).setCellValue("Index");
        sheet.getRow(15).createCell(3).setCellValue("Type");
        sheet.getRow(15).createCell(5).setCellValue("Unit");
        sheet.getRow(15).createCell(6).setCellValue("Quantity");

        for (int i = 0; i < jobs.size(); i++) {
            sheet.createRow(16 + i).createCell(2).setCellValue(jobs.get(i).get("num").textValue());
            sheet.getRow(16 + i).createCell(3).setCellValue(jobs.get(i).get("title").textValue());
            sheet.getRow(16 + i).createCell(5).setCellValue(jobs.get(i).get("materialUnit").textValue());
            sheet.getRow(16 + i).createCell(6).setCellValue(jobs.get(i).get("quantity").asText());
        }

        int jobsOffset = jobs.size();

        sheet.createRow(17 + jobsOffset).createCell(1).setCellValue("III. Executed Job Description");
        sheet.createRow(18 + jobsOffset).createCell(1).setCellValue(explanation);

        sheet.createRow(21 + jobsOffset).createCell(1).setCellValue("Photo before Performance");
        sheet.createRow(22 + jobsOffset).createCell(1).setCellValue("Photo after  Performance ");
        sheet.createRow(23 + jobsOffset).createCell(1).setCellValue("Material List");
        sheet.createRow(24 + jobsOffset).createCell(1).setCellValue("Act of Dismantle");
        sheet.createRow(25 + jobsOffset).createCell(1).setCellValue("Act of Broken Unit");
        sheet.createRow(26 + jobsOffset).createCell(1).setCellValue("Tech. conditions with permission");
        sheet.createRow(27 + jobsOffset).createCell(1).setCellValue("Electro Installation switching document");
        sheet.createRow(28 + jobsOffset).createCell(1).setCellValue("document");
        sheet.createRow(29 + jobsOffset).createCell(1).setCellValue("Quality control document");
        sheet.createRow(30 + jobsOffset).createCell(1).setCellValue("High-voltage analysis and measuring document");
        sheet.createRow(31 + jobsOffset).createCell(1).setCellValue("measuring document");
        sheet.createRow(32 + jobsOffset).createCell(1).setCellValue("Page of List");

        sheet.createRow(33 + jobsOffset).createCell(1).setCellValue("V. Job Request Ordered to:");
        sheet.getRow(33 + jobsOffset).createCell(3).setCellValue("VI. Executed by:");
        sheet.createRow(35 + jobsOffset).createCell(1).setCellValue(subcontractor);
        sheet.getRow(35 + jobsOffset).createCell(3).setCellValue("Contractor`s name:");
        sheet.getRow(35 + jobsOffset).createCell(5).setCellValue(subcontractor);
        sheet.createRow(36 + jobsOffset).createCell(3).setCellValue("Date of executed work:");
        sheet.getRow(36 + jobsOffset).createCell(5).setCellValue("");
        sheet.createRow(37 + jobsOffset).createCell(3).setCellValue("Contractor`s signature:");
        sheet.createRow(38 + jobsOffset).createCell(3).setCellValue("Manager (name & signature):");

        sheet.createRow(40 + jobsOffset).createCell(1).setCellValue("VII. Accepted by:");
        sheet.createRow(42 + jobsOffset).createCell(1).setCellValue("Engineer`s name:");
        sheet.getRow(42 + jobsOffset).createCell(3).setCellValue(initiator);
        sheet.createRow(43 + jobsOffset).createCell(1).setCellValue("Unit / group name:");
        sheet.getRow(43 + jobsOffset).createCell(3).setCellValue("NOD-OSP-S");
        sheet.createRow(44 + jobsOffset).createCell(1).setCellValue("Accepted date:");
        sheet.getRow(44 + jobsOffset).createCell(3).setCellValue("");
        sheet.createRow(45 + jobsOffset).createCell(1).setCellValue("Engineer`s signature:");
        sheet.createRow(46 + jobsOffset).createCell(1).setCellValue("Supervisor (name & signature):");

        setStyle(workbook, jobsOffset);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        out.close();
        return out.toByteArray();
    }

    private void setStyle(XSSFWorkbook workbook, int jobsOffset) {
        setColumnWidth(workbook);
        setThinBorders(workbook, jobsOffset);
        setThickBorders(workbook, jobsOffset);
        setFont(workbook, jobsOffset);
        mergeFields(workbook, jobsOffset);
        wrapText(workbook, jobsOffset);
        alignText(workbook, jobsOffset);
    }

    private void setColumnWidth(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.getSheet(SHEET_NAME);
        sheet.setColumnWidth(2, 25*256);
        sheet.setColumnWidth(3, 25*256);
        sheet.setColumnWidth(4, 25*256);
        sheet.setColumnWidth(5, 25*256);
        sheet.setColumnWidth(6, 25*256);
    }

    private void setFont(XSSFWorkbook workbook, int jobsOffset) {
        XSSFSheet sheet = workbook.getSheet(SHEET_NAME);

        XSSFFont arialB10 = workbook.createFont();
        arialB10.setFontName(HSSFFont.FONT_ARIAL);
        arialB10.setFontHeightInPoints((short) 10);
        arialB10.setBold(true);

        XSSFFont arialBI9 = workbook.createFont();
        arialBI9.setFontName(HSSFFont.FONT_ARIAL);
        arialBI9.setFontHeightInPoints((short) 9);
        arialBI9.setItalic(true);
        arialBI9.setBold(true);

        XSSFFont arialRed10 = workbook.createFont();
        arialRed10.setFontName(HSSFFont.FONT_ARIAL);
        arialRed10.setFontHeightInPoints((short) 10);
        arialRed10.setColor(Font.COLOR_RED);

        CellUtil.setFont(sheet.getRow(2).getCell(3), arialB10);
        CellUtil.setFont(sheet.getRow(3).getCell(3), arialB10);
        CellUtil.setFont(sheet.getRow(3).getCell(4), arialB10);
        CellUtil.setFont(sheet.getRow(3).getCell(5), arialB10);
        CellUtil.setFont(sheet.getRow(3).getCell(6), arialB10);
        CellUtil.setFont(sheet.getRow(5).getCell(3), arialB10);
        CellUtil.setFont(sheet.getRow(5).getCell(4), arialB10);
        CellUtil.setFont(sheet.getRow(5).getCell(5), arialB10);
        CellUtil.setFont(sheet.getRow(5).getCell(6), arialB10);

        CellUtil.setFont(sheet.getRow(7).getCell(1), arialBI9);
        CellUtil.setFont(sheet.getRow(7).getCell(2), arialB10);
        CellUtil.setFont(sheet.getRow(8).getCell(2), arialB10);
        CellUtil.setFont(sheet.getRow(9).getCell(2), arialB10);
        CellUtil.setFont(sheet.getRow(10).getCell(2), arialB10);
        CellUtil.setFont(sheet.getRow(11).getCell(2), arialB10);
        CellUtil.setFont(sheet.getRow(12).getCell(2), arialB10);

        CellUtil.setFont(sheet.getRow(14).getCell(1), arialBI9);
        CellUtil.setFont(sheet.getRow(15).getCell(2), arialB10);
        CellUtil.setFont(sheet.getRow(15).getCell(3), arialB10);
        CellUtil.setFont(sheet.getRow(15).getCell(5), arialB10);
        CellUtil.setFont(sheet.getRow(15).getCell(6), arialB10);
        for (int i = 0; i < jobsOffset; i++) {
            CellUtil.setFont(sheet.getRow(16 + i).getCell(2), arialRed10);
            CellUtil.setFont(sheet.getRow(16 + i).getCell(3), arialRed10);
            CellUtil.setFont(sheet.getRow(16 + i).getCell(5), arialRed10);
            CellUtil.setFont(sheet.getRow(16 + i).getCell(6), arialRed10);
        }

        CellUtil.setFont(sheet.getRow(17 + jobsOffset).getCell(1), arialBI9);

        CellUtil.setFont(sheet.getRow(21 + jobsOffset).getCell(1), arialB10);
        CellUtil.setFont(sheet.getRow(22 + jobsOffset).getCell(1), arialB10);
        CellUtil.setFont(sheet.getRow(23 + jobsOffset).getCell(1), arialB10);
        CellUtil.setFont(sheet.getRow(24 + jobsOffset).getCell(1), arialB10);
        CellUtil.setFont(sheet.getRow(25 + jobsOffset).getCell(1), arialB10);
        CellUtil.setFont(sheet.getRow(26 + jobsOffset).getCell(1), arialB10);
        CellUtil.setFont(sheet.getRow(27 + jobsOffset).getCell(1), arialB10);
        CellUtil.setFont(sheet.getRow(28 + jobsOffset).getCell(1), arialB10);
        CellUtil.setFont(sheet.getRow(29 + jobsOffset).getCell(1), arialB10);
        CellUtil.setFont(sheet.getRow(30 + jobsOffset).getCell(1), arialB10);
        CellUtil.setFont(sheet.getRow(31 + jobsOffset).getCell(1), arialB10);
        CellUtil.setFont(sheet.getRow(32 + jobsOffset).getCell(1), arialB10);


        CellUtil.setFont(sheet.getRow(33 + jobsOffset).getCell(1), arialBI9);
        CellUtil.setFont(sheet.getRow(33 + jobsOffset).getCell(3), arialBI9);
        CellUtil.setFont(sheet.getRow(35 + jobsOffset).getCell(3), arialB10);
        CellUtil.setFont(sheet.getRow(36 + jobsOffset).getCell(3), arialB10);
        CellUtil.setFont(sheet.getRow(37 + jobsOffset).getCell(3), arialB10);
        CellUtil.setFont(sheet.getRow(38 + jobsOffset).getCell(3), arialB10);

        CellUtil.setFont(sheet.getRow(40 + jobsOffset).getCell(1), arialBI9);
        CellUtil.setFont(sheet.getRow(42 + jobsOffset).getCell(1), arialB10);
        CellUtil.setFont(sheet.getRow(43 + jobsOffset).getCell(1), arialB10);
        CellUtil.setFont(sheet.getRow(43 + jobsOffset).getCell(3), arialB10);
        CellUtil.setFont(sheet.getRow(44 + jobsOffset).getCell(1), arialB10);
        CellUtil.setFont(sheet.getRow(45 + jobsOffset).getCell(1), arialB10);
        CellUtil.setFont(sheet.getRow(46 + jobsOffset).getCell(1), arialB10);

    }

    private void setThinBorders(XSSFWorkbook workbook, int jobsOffset) {
        XSSFSheet sheet = workbook.getSheet(SHEET_NAME);
        Map<String, Object> properties = new HashMap<>();
        properties.put(CellUtil.BORDER_LEFT, BorderStyle.THIN);
        properties.put(CellUtil.BORDER_BOTTOM, BorderStyle.THIN);

        for (int i = 1; i < 7; i++) {
            for (int j = 3; j < 7; j++) {
                Cell cell = sheet.getRow(i).getCell(j) != null ? sheet.getRow(i).getCell(j) : sheet.getRow(i).createCell(j);
                CellUtil.setCellStyleProperties(cell, properties);
            }
        }
    }

    private void alignText(XSSFWorkbook workbook, int jobsOffset) {
        XSSFSheet sheet = workbook.getSheet(SHEET_NAME);
        Map<String, Object> properties = new HashMap<>();
        properties.put(CellUtil.ALIGNMENT, HorizontalAlignment.CENTER);

        CellUtil.setCellStyleProperties(sheet.getRow(1).getCell(3), properties);
        CellUtil.setCellStyleProperties(sheet.getRow(2).getCell(3), properties);
        CellUtil.setCellStyleProperties(sheet.getRow(33 + jobsOffset).getCell(3), properties);
        CellUtil.setCellStyleProperties(sheet.getRow(35 + jobsOffset).getCell(3), properties);
        CellUtil.setCellStyleProperties(sheet.getRow(36 + jobsOffset).getCell(3), properties);
        CellUtil.setCellStyleProperties(sheet.getRow(37 + jobsOffset).getCell(3), properties);
        CellUtil.setCellStyleProperties(sheet.getRow(38 + jobsOffset).getCell(3), properties);

        properties.replace(CellUtil.ALIGNMENT, HorizontalAlignment.RIGHT);
        CellUtil.setCellStyleProperties(sheet.getRow(7).getCell(2), properties);
        CellUtil.setCellStyleProperties(sheet.getRow(8).getCell(2), properties);
        CellUtil.setCellStyleProperties(sheet.getRow(9).getCell(2), properties);
        CellUtil.setCellStyleProperties(sheet.getRow(10).getCell(2), properties);
        CellUtil.setCellStyleProperties(sheet.getRow(11).getCell(2), properties);
        CellUtil.setCellStyleProperties(sheet.getRow(12).getCell(2), properties);

        properties.remove(CellUtil.ALIGNMENT);
        properties.put(CellUtil.VERTICAL_ALIGNMENT, VerticalAlignment.TOP);
        CellUtil.setCellStyleProperties(sheet.getRow(7).getCell(2), properties);
        CellUtil.setCellStyleProperties(sheet.getRow(8).getCell(2), properties);
        CellUtil.setCellStyleProperties(sheet.getRow(9).getCell(2), properties);
        CellUtil.setCellStyleProperties(sheet.getRow(10).getCell(2), properties);
        CellUtil.setCellStyleProperties(sheet.getRow(11).getCell(2), properties);
        CellUtil.setCellStyleProperties(sheet.getRow(12).getCell(2), properties);
        CellUtil.setCellStyleProperties(sheet.getRow(18 + jobsOffset).getCell(1), properties);
    }
    private void wrapText(XSSFWorkbook workbook, int jobsOffset) {
        XSSFSheet sheet = workbook.getSheet(SHEET_NAME);
        Map<String, Object> properties = new HashMap<>();
        properties.put(CellUtil.WRAP_TEXT, true);


        CellUtil.setCellStyleProperties(sheet.getRow(9).getCell(3), properties);
        CellUtil.setCellStyleProperties(sheet.getRow(10).getCell(3), properties);
        CellUtil.setCellStyleProperties(sheet.getRow(11).getCell(3), properties);
        CellUtil.setCellStyleProperties(sheet.getRow(12).getCell(3), properties);

        for (int i = 0; i < jobsOffset; i++) {
            CellUtil.setCellStyleProperties(sheet.getRow(16 + i).getCell(3), properties);
        }

        sheet.getRow(26 + jobsOffset).setHeightInPoints(2 * sheet.getDefaultRowHeightInPoints());
        sheet.getRow(27 + jobsOffset).setHeightInPoints(2 * sheet.getDefaultRowHeightInPoints());
        sheet.getRow(30 + jobsOffset).setHeightInPoints(2 * sheet.getDefaultRowHeightInPoints());
        CellUtil.setCellStyleProperties(sheet.getRow(26 + jobsOffset).getCell(1), properties);
        CellUtil.setCellStyleProperties(sheet.getRow(27 + jobsOffset).getCell(1), properties);
        CellUtil.setCellStyleProperties(sheet.getRow(30 + jobsOffset).getCell(1), properties);
        CellUtil.setCellStyleProperties(sheet.getRow(35 + jobsOffset).getCell(1), properties);
        CellUtil.setCellStyleProperties(sheet.getRow(35 + jobsOffset).getCell(5), properties);
        CellUtil.setCellStyleProperties(sheet.getRow(42 + jobsOffset).getCell(3), properties);
        CellUtil.setCellStyleProperties(sheet.getRow(44 + jobsOffset).getCell(3), properties);
    }

    private void mergeFields(XSSFWorkbook workbook, int jobsOffset) {
        XSSFSheet sheet = workbook.getSheet(SHEET_NAME);
        sheet.addMergedRegion(new CellRangeAddress(1, 6, 1, 2));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 3, 6));
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 3, 6));
        sheet.addMergedRegion(new CellRangeAddress(14, 14, 1, 3));
        for (int i = 0; i < jobsOffset; i++) {
            sheet.addMergedRegion(new CellRangeAddress(16 + i, 16 + i, 3, 4));
        }
        sheet.addMergedRegion(new CellRangeAddress(17 + jobsOffset, 17 + jobsOffset, 1, 3));
        sheet.addMergedRegion(new CellRangeAddress(18 + jobsOffset, 20 + jobsOffset, 1, 6));
        for (int i = 21 + jobsOffset; i < 33 + jobsOffset; i++) {
            sheet.addMergedRegion(new CellRangeAddress(i, i, 1, 2));
        }
        sheet.addMergedRegion(new CellRangeAddress(33 + jobsOffset, 33 + jobsOffset, 1, 2));
        sheet.addMergedRegion(new CellRangeAddress(33 + jobsOffset, 33 + jobsOffset, 3, 4));
        sheet.addMergedRegion(new CellRangeAddress(35 + jobsOffset, 35 + jobsOffset, 1, 2));
        sheet.addMergedRegion(new CellRangeAddress(35 + jobsOffset, 35 + jobsOffset, 3, 4));
        sheet.addMergedRegion(new CellRangeAddress(35 + jobsOffset, 35 + jobsOffset, 5, 6));
        sheet.addMergedRegion(new CellRangeAddress(36 + jobsOffset, 36 + jobsOffset, 3, 4));
        sheet.addMergedRegion(new CellRangeAddress(36 + jobsOffset, 36 + jobsOffset, 5, 6));
        sheet.addMergedRegion(new CellRangeAddress(37 + jobsOffset, 37 + jobsOffset, 3, 4));
        sheet.addMergedRegion(new CellRangeAddress(38 + jobsOffset, 38 + jobsOffset, 3, 4));
        sheet.addMergedRegion(new CellRangeAddress(40 + jobsOffset, 40 + jobsOffset, 1, 2));
        for (int i = 42 + jobsOffset; i < 47 + jobsOffset; i++) {
            sheet.addMergedRegion(new CellRangeAddress(i, i, 1, 2));
        }
        sheet.addMergedRegion(new CellRangeAddress(42 + jobsOffset, 42 + jobsOffset, 3, 6));
        sheet.addMergedRegion(new CellRangeAddress(44 + jobsOffset, 44 + jobsOffset, 3, 6));
    }
    private void setThickBorders(XSSFWorkbook workbook, int jobsOffset) {
        XSSFSheet sheet = workbook.getSheet(SHEET_NAME);

        Map<String, Object> properties = new HashMap<>();
        properties.put(CellUtil.BORDER_TOP, BorderStyle.MEDIUM);
        List<Integer> listOfIndexes = Arrays.asList(1, 7, 14, 15, 17 + jobsOffset, 21 + jobsOffset, 33 + jobsOffset, 49 + jobsOffset);
        for (int i : listOfIndexes) {
            for (int j = 1; j < 7; j++) {
                if (sheet.getRow(i) == null) {
                    sheet.createRow(i);
                }
                Cell cell = sheet.getRow(i).getCell(j) != null ? sheet.getRow(i).getCell(j) : sheet.getRow(i).createCell(j);
                CellUtil.setCellStyleProperties(cell, properties);
            }
        }
        properties.remove(CellUtil.BORDER_TOP);
        properties.put(CellUtil.BORDER_LEFT, BorderStyle.MEDIUM);
        for (int i = 1; i < 49 + jobsOffset; i++) {
            if (sheet.getRow(i) == null) {
                sheet.createRow(i);
            }
            Cell cell = sheet.getRow(i).getCell(1) != null ? sheet.getRow(i).getCell(1) : sheet.getRow(i).createCell(1);
            CellUtil.setCellStyleProperties(cell, properties);
        }
        properties.remove(CellUtil.BORDER_LEFT);
        properties.put(CellUtil.BORDER_RIGHT, BorderStyle.MEDIUM);
        for (int i = 1; i < 49 + jobsOffset; i++) {
            if (sheet.getRow(i) == null) {
                sheet.createRow(i);
            }
            Cell cell = sheet.getRow(i).getCell(6) != null ? sheet.getRow(i).getCell(6) : sheet.getRow(i).createCell(6);
            CellUtil.setCellStyleProperties(cell, properties);
        }
    }
}
