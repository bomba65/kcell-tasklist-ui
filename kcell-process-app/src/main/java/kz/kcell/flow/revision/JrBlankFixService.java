package kz.kcell.flow.revision;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import kz.kcell.flow.files.Minio;
import lombok.extern.java.Log;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.spin.plugin.variable.SpinValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.activation.DataSource;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import javax.script.ScriptEngineManager;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Service
@Log
public class JrBlankFixService {

    @Autowired
    JavaMailSender mailSender;

    @Autowired
    private Minio minioClient;

    @Value("${mail.sender:flow@kcell.kz}")
    private String sender;

    @Autowired
    ScriptEngineManager manager;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    IdentityService identityService;

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
            map.put("9", "TOO Inter Service");
            map.put("10", "Forester-Hes Group");
            map.put("11", "Транстелеком");
            return Collections.unmodifiableMap(map);
        })).get();

    private static final Map<String, String> contractorsCode =
        ((Supplier<Map<String, String>>) (() -> {
            Map<String, String> map = new HashMap<>();
            map.put("1", "avrora");
            map.put("2", "aicom");
            map.put("3", "spectr");
            map.put("4", "lse");
            map.put("5", "kcell");
            map.put("6", "alta");
            map.put("7", "logycom");
            map.put("8", "arlan");
            map.put("9", "inter");
            return Collections.unmodifiableMap(map);
        })).get();

    protected void sendMail(String processId, String assignee, String recipient) {
        try {
            if (worksTitle.size() == 0) {
                String mainContract = (String) runtimeService.getVariable(processId, "mainContract");
                ObjectMapper mapper = new ObjectMapper();
                InputStream fis = JrBlankFixService.class.getResourceAsStream("/revision/" + ("Roll-outRevision2020".equals(mainContract) ? "newWorkPrice.json" : "workPrice.json"));
                InputStreamReader reader = new InputStreamReader(fis, "utf-8");
                ArrayNode json = (ArrayNode) mapper.readTree(reader);
                for (JsonNode workPrice : json) {
                    worksTitle.put(workPrice.get("id").textValue(), workPrice.get("title").textValue());
                }
            }
            String siteName = (String) runtimeService.getVariable(processId, "siteName");
            String site_name = (String) runtimeService.getVariable(processId, "site_name");
            String jrNumber = (String) runtimeService.getVariable(processId, "jrNumber");
            String jobDescription = (String) runtimeService.getVariable(processId, "jobDescription");
            String explanation = (String) runtimeService.getVariable(processId, "explanation");
            String contractor = runtimeService.getVariable(processId, "contractor").toString();
            String regionApproval = (String) runtimeService.getVariable(processId, "regionApproval");
            String centralApproval = (String) runtimeService.getVariable(processId, "centralApproval");
            String siteRegion = runtimeService.getVariable(processId, "siteRegion").toString();
            String reason = runtimeService.getVariable(processId, "reason").toString();
            Date requestDate = (Date) runtimeService.getVariable(processId, "requestedDate");
            Date contractorJobAssignedDate = (Date) runtimeService.getVariable(processId, "contractorJobAssignedDate");
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            ObjectMapper mapper = new ObjectMapper();
            ArrayNode jobWorks = (ArrayNode) mapper.readTree(runtimeService.getVariableTyped(processId, "jobWorks").getValue().toString());
            JsonNode initiatorFull = mapper.readTree(runtimeService.getVariableTyped(processId, "initiatorFull").getValue().toString());
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

            Row row = sheet.createRow(0);
            row.createCell(0);
            Cell cell = row.createCell(1);
            cell.setCellValue("ICT Department, K’Cell");
            cell.setCellStyle(center);

            row = sheet.createRow(1);
            cell = row.createCell(1);
            cell.setCellValue("SITE REVISION");
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
            row.createCell(1).setCellValue("In Attention of:");
            row.createCell(2).setCellValue("Approved By:");
            row.createCell(3).setCellValue("Reviewed By:");
            row.createCell(4).setCellValue("Prepared By:");

            row = sheet.createRow(5);
            row.createCell(0).setCellValue("External & Internal Use");
            row.createCell(1).setCellValue("Contractors");
            row.createCell(2).setCellValue("Department Director");
            row.createCell(3).setCellValue("Manager");
            row.createCell(4).setCellValue("");

            row = sheet.createRow(10);
            cell = row.createCell(0);
            cell.setCellValue("Job Request Number:");
            cell.setCellStyle(alignRight);

            row.createCell(2).setCellValue(jrNumber);

            row = sheet.createRow(11);
            cell = row.createCell(0);

            SimpleDateFormat datFormatter = new SimpleDateFormat("yyyy-MM-dd'T'H:m:s.S");
            String compareDateString = "2019-02-05T06:00:00.000";
            Date compareDate = datFormatter.parse(compareDateString);

            if (requestDate.after(compareDate)) {
                cell.setCellValue("Job Request date :");
                cell.setCellStyle(alignRight);
                row.createCell(2).setCellValue(contractorJobAssignedDate != null ? sdf.format(contractorJobAssignedDate) : sdf.format(requestDate));
            } else {
                cell.setCellValue("Request Date :");
                cell.setCellStyle(alignRight);
                row.createCell(2).setCellValue(sdf.format(requestDate));
            }


            row = sheet.createRow(12);
            cell = row.createCell(0);
            cell.setCellValue("City Name :");
            cell.setCellStyle(alignRight);
            row.createCell(2).setCellValue("");

            row = sheet.createRow(13);
            cell = row.createCell(0);
            cell.setCellValue("Site Name :");
            cell.setCellStyle(alignRight);

            if (site_name != null && !site_name.isEmpty()) {
                row.createCell(2).setCellValue(site_name);
            } else {
                row.createCell(2).setCellValue("Not found");
            }

            row = sheet.createRow(14);
            cell = row.createCell(0);
            cell.setCellValue("Site Address :");
            cell.setCellStyle(alignRight);
            row.createCell(2).setCellValue("");

            row = sheet.createRow(15);
            row.createCell(0).setCellValue("Job Description: " + (jobDescription != null ? jobDescription : ""));

            for (int i = 0; i < jobWorks.size(); i++) {
                row = sheet.createRow(16 + i);
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
                }
                cell.setCellStyle(autoWrap);
                cell.setCellValue(title);
                if (title.length() > 100) {
                    row.setHeight((short) (row.getHeight() * (title.length() / 100 + 1)));
                }
            }

            row = sheet.createRow(16 + jobWorks.size());
            row.createCell(0).setCellValue("Explanation of works:");

            row = sheet.createRow(17 + jobWorks.size());
            row.setHeight((short) 1200);

            cell = row.createCell(0);
            cell.setCellStyle(autoWrap);
            cell.setCellValue(explanation != null ? explanation : "");

            row = sheet.createRow(18 + jobWorks.size());
            row.createCell(0).setCellValue("Vailidity:");

            row = sheet.createRow(19 + jobWorks.size());
            row.createCell(2).setCellValue("Requested by:");

            row = sheet.createRow(20 + jobWorks.size());
            row.createCell(0).setCellValue("Job Request to:");
            row.createCell(2).setCellValue(initiatorFull.get("firstName").textValue() + " " + initiatorFull.get("lastName").textValue());

            row = sheet.createRow(21 + jobWorks.size());
            row.createCell(0).setCellValue((contractorsTitle.get(contractor) != null ? contractorsTitle.get(contractor) : "Not found"));
            row.createCell(2).setCellValue("             (position, name & signature)");

            row = sheet.createRow(22 + jobWorks.size());
            row.createCell(2).setCellValue("Tel:");

            row = sheet.createRow(23 + jobWorks.size());
            row.createCell(2).setCellValue("Approved by:");

            row = sheet.createRow(24 + jobWorks.size());
            row.createCell(2).setCellValue(((regionApproval != null && !regionApproval.isEmpty() && !regionApproval.equals("null")) ? regionApproval : "") + ((centralApproval != null && !centralApproval.isEmpty() && !centralApproval.equals("null")) ? (", " + centralApproval) : ""));

            row = sheet.createRow(25 + jobWorks.size());
            row.createCell(2).setCellValue("             (position, name & signature)");

            row = sheet.createRow(26 + jobWorks.size());
            row.createCell(0).setCellValue("Vendor:");

            row = sheet.createRow(27 + jobWorks.size());
            row.createCell(0).setCellValue("Received by:");

            row = sheet.createRow(28 + jobWorks.size());
            row.createCell(0).setCellValue("");

            row = sheet.createRow(29 + jobWorks.size());
            row.createCell(0).setCellValue("             (contractor name, position, signature &  date)");

            sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 4));
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 4));
            sheet.addMergedRegion(new CellRangeAddress(0, 4, 0, 0));
            sheet.addMergedRegion(new CellRangeAddress(10, 10, 0, 1));
            sheet.addMergedRegion(new CellRangeAddress(10, 10, 2, 4));
            sheet.addMergedRegion(new CellRangeAddress(11, 11, 0, 1));
            sheet.addMergedRegion(new CellRangeAddress(11, 11, 2, 4));
            sheet.addMergedRegion(new CellRangeAddress(12, 12, 0, 1));
            sheet.addMergedRegion(new CellRangeAddress(12, 12, 2, 4));
            sheet.addMergedRegion(new CellRangeAddress(13, 13, 0, 1));
            sheet.addMergedRegion(new CellRangeAddress(13, 13, 2, 4));
            sheet.addMergedRegion(new CellRangeAddress(14, 14, 0, 1));
            sheet.addMergedRegion(new CellRangeAddress(14, 14, 2, 4));
            sheet.addMergedRegion(new CellRangeAddress(17 + jobWorks.size(), 17 + jobWorks.size(), 0, 4));

            for (int i = 0; i < jobWorks.size(); i++) {
                sheet.addMergedRegion(new CellRangeAddress(16 + i, 16 + i, 0, 4));
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

            CellUtil.setCellStyleProperty(sheet.getRow(10).getCell(0), CellUtil.BORDER_TOP, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(10).createCell(1), CellUtil.BORDER_TOP, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(10).getCell(2), CellUtil.BORDER_TOP, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(10).createCell(3), CellUtil.BORDER_TOP, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(10).createCell(4), CellUtil.BORDER_TOP, BorderStyle.THIN);

            CellUtil.setCellStyleProperty(sheet.getRow(10).getCell(0), CellUtil.BORDER_LEFT, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(11).getCell(0), CellUtil.BORDER_LEFT, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(12).getCell(0), CellUtil.BORDER_LEFT, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(13).getCell(0), CellUtil.BORDER_LEFT, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(14).getCell(0), CellUtil.BORDER_LEFT, BorderStyle.THIN);

            CellUtil.setCellStyleProperty(sheet.getRow(14).getCell(0), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(14).createCell(1), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(14).getCell(2), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(14).createCell(3), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(14).createCell(4), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);

            CellUtil.setCellStyleProperty(sheet.getRow(10).getCell(4), CellUtil.BORDER_RIGHT, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(11).createCell(4), CellUtil.BORDER_RIGHT, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(12).createCell(4), CellUtil.BORDER_RIGHT, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(13).createCell(4), CellUtil.BORDER_RIGHT, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(14).getCell(4), CellUtil.BORDER_RIGHT, BorderStyle.THIN);

            for (int i = 0; i < jobWorks.size() + 15; i++) {
                if (sheet.getRow(15 + i).getCell(0) != null) {
                    CellUtil.setCellStyleProperty(sheet.getRow(15 + i).getCell(0), CellUtil.BORDER_LEFT, BorderStyle.THIN);
                } else {
                    CellUtil.setCellStyleProperty(sheet.getRow(15 + i).createCell(0), CellUtil.BORDER_LEFT, BorderStyle.THIN);
                }
                CellUtil.setCellStyleProperty(sheet.getRow(15 + i).createCell(4), CellUtil.BORDER_RIGHT, BorderStyle.THIN);
            }

            CellUtil.setCellStyleProperty(sheet.getRow(16 + jobWorks.size()).getCell(0), CellUtil.BORDER_TOP, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(16 + jobWorks.size()).createCell(1), CellUtil.BORDER_TOP, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(16 + jobWorks.size()).createCell(2), CellUtil.BORDER_TOP, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(16 + jobWorks.size()).createCell(3), CellUtil.BORDER_TOP, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(16 + jobWorks.size()).getCell(4), CellUtil.BORDER_TOP, BorderStyle.THIN);

            CellUtil.setCellStyleProperty(sheet.getRow(17 + jobWorks.size()).getCell(0), CellUtil.BORDER_TOP, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(17 + jobWorks.size()).createCell(1), CellUtil.BORDER_TOP, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(17 + jobWorks.size()).createCell(2), CellUtil.BORDER_TOP, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(17 + jobWorks.size()).createCell(3), CellUtil.BORDER_TOP, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(17 + jobWorks.size()).getCell(4), CellUtil.BORDER_TOP, BorderStyle.THIN);

            CellUtil.setCellStyleProperty(sheet.getRow(18 + jobWorks.size()).getCell(0), CellUtil.BORDER_TOP, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(18 + jobWorks.size()).createCell(1), CellUtil.BORDER_TOP, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(18 + jobWorks.size()).createCell(2), CellUtil.BORDER_TOP, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(18 + jobWorks.size()).createCell(3), CellUtil.BORDER_TOP, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(18 + jobWorks.size()).getCell(4), CellUtil.BORDER_TOP, BorderStyle.THIN);

            CellUtil.setCellStyleProperty(sheet.getRow(19 + jobWorks.size()).getCell(0), CellUtil.BORDER_TOP, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(19 + jobWorks.size()).createCell(1), CellUtil.BORDER_TOP, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(19 + jobWorks.size()).getCell(2), CellUtil.BORDER_TOP, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(19 + jobWorks.size()).createCell(3), CellUtil.BORDER_TOP, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(19 + jobWorks.size()).getCell(4), CellUtil.BORDER_TOP, BorderStyle.THIN);

            CellUtil.setCellStyleProperty(sheet.getRow(29 + jobWorks.size()).getCell(0), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(29 + jobWorks.size()).createCell(1), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(29 + jobWorks.size()).createCell(2), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(29 + jobWorks.size()).createCell(3), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(29 + jobWorks.size()).getCell(4), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);

            CellUtil.setCellStyleProperty(sheet.getRow(19 + jobWorks.size()).getCell(2), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);

            CellUtil.setCellStyleProperty(sheet.getRow(20 + jobWorks.size()).getCell(0), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(20 + jobWorks.size()).getCell(2), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(20 + jobWorks.size()).createCell(3), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(20 + jobWorks.size()).getCell(4), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);

            CellUtil.setCellStyleProperty(sheet.getRow(22 + jobWorks.size()).getCell(2), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(22 + jobWorks.size()).createCell(3), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(22 + jobWorks.size()).getCell(4), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);

            CellUtil.setCellStyleProperty(sheet.getRow(23 + jobWorks.size()).getCell(2), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);

            CellUtil.setCellStyleProperty(sheet.getRow(24 + jobWorks.size()).getCell(2), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(24 + jobWorks.size()).createCell(3), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(24 + jobWorks.size()).getCell(4), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);

            CellUtil.setCellStyleProperty(sheet.getRow(26 + jobWorks.size()).getCell(0), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(27 + jobWorks.size()).getCell(0), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);

            CellUtil.setCellStyleProperty(sheet.getRow(28 + jobWorks.size()).getCell(0), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(28 + jobWorks.size()).createCell(1), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);
            CellUtil.setCellStyleProperty(sheet.getRow(28 + jobWorks.size()).createCell(2), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);

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
            ByteArrayInputStream is = new ByteArrayInputStream(out.toByteArray());

            DataSource source = new ByteArrayDataSource(is, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);

            messageHelper.setFrom(sender);
            messageHelper.setSubject("JR " + jrNumber + " Blank");


            messageHelper.setText("Your JR Approved. JR Blank attached\n" +
                "\n" +
                "\n" +
                "Пройдя по следующей ссылке на страницу в HUB.Kcell.kz, вы можете оставить в поле комментариев свои замечания и/или пожелания относительно функционала и интерфейса системы: https://hub.kcell.kz/x/kYNoAg");


            Set<String> ccList = identityService.createUserQuery().userId(runtimeService.getVariable(processId, "starter").toString()).list().stream().map(user -> user.getId()).collect(Collectors.toSet());
            if (reason != null && reason.equals("3")) {
                ccList.add("Tatyana.Solovyova@kcell.kz");
            }

            messageHelper.setTo(InternetAddress.parse(ccList.stream().collect(Collectors.joining(","))));


            String path = processId + "/" + jrNumber.replace("-####", "").replace("-##", "") + ".xlsx";
            String fileName = jrNumber.replace("-####", "").replace("-##", "") + ".xlsx";

            messageHelper.addAttachment(fileName, source);

            mailSender.send(message);

            log.info("Task Assignment Email successfully sent to user '" + assignee + "' with address '" + recipient + "'.");

            ByteArrayInputStream bis = new ByteArrayInputStream(out.toByteArray());
            minioClient.saveFile(path, bis, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            bis.close();

            String json = "{\"name\" :\"" + fileName + "\",\"path\" : \"" + path + "\"}";
            runtimeService.setVariable(processId, "jrBlank", SpinValues.jsonValue(json));

            runtimeService.setVariable(processId, "isNewProcessCreated", "false");

        } catch (Exception e) {
            e.printStackTrace();
            log.log(Level.WARNING, "Could not send email to assignee", e);
        }
    }

    public void execute(String processId) throws Exception {
        String recipient = (String) runtimeService.getVariable(processId, "starter");

        if (recipient == null) {
            return;
        }

        User user = identityService.createUserQuery().userId(recipient).singleResult();

        if (user != null) {

            // Get Email Address from User Profile
            String recipientEmail = user.getEmail();

            if (recipient != null && !recipient.isEmpty()) {

                sendMail(processId, recipient, recipientEmail);

            } else {
                log.warning("Not sending email to user " + recipient + "', user has no email address.");
            }

        } else {
            log.warning("Not sending email to user " + recipient + "', user is not enrolled with identity service.");
        }
    }
}
