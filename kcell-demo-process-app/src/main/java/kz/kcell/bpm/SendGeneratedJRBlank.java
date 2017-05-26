package kz.kcell.bpm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.MultiPartEmail;
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
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.extension.mail.config.MailConfiguration;
import org.camunda.bpm.extension.mail.config.MailConfigurationFactory;

import javax.activation.DataSource;
import javax.mail.internet.InternetAddress;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SendGeneratedJRBlank implements JavaDelegate {

    private final static Logger LOGGER = Logger.getLogger(SendGeneratedJRBlank.class.getName());

    MailConfiguration configuration = MailConfigurationFactory.getConfiguration();
    private static final Map<String, String> worksTitle =
            ((Supplier<Map<String, String>>) (() -> {
                Map<String, String> map = new HashMap();
                map.put("1", "1.BTS Macro/BTS Ult inst(CabOnly) on-air");
                map.put("2", "2.BTS Micro/BTS Midi inst(CabOnly)on-air");
                map.put("3", "3.RBS/BTS Extension inst(cab+batt)on-air");
                map.put("4", "4.RBS/BTS type chge (cab+batt)on-air");
                map.put("5", "5.RBS/BTS add(BBS+cab+ant+feed+pole)oair");
                map.put("6", "6.BTS Macro removal ");
                map.put("7", "7.BTS Micro/BTS Midi removal ");
                map.put("8", "8.Site chge/repl (All equips)");
                map.put("9", "9.Site removal - all equipment including FE deinstallation");
                map.put("10", "10.BTS Swap-Vend (incl pack Kcell req)");
                map.put("11", "11.BattBackSys inst BBS on-air (^2 sets)");
                map.put("12", "12.BattBackSys chg/repl on-air (^2 sets)");
                map.put("13", "13.Battery Backup System Removal");
                map.put("14", "14.BattChg (de&inst)/set (1set=4batt)");
                map.put("15", "15.Batt inst/deinst /set (1set=4batt)");
                map.put("16", "16.Container inst");
                map.put("17", "17.Container chge/repl");
                map.put("18", "18.Container removal");
                map.put("19", "19.Site Grounding System inst");
                map.put("20", "20.Site Grounding System сhange/repl");
                map.put("21", "21.Site Grounding System removal");
                map.put("22", "22.Site Lightning Protection inst");
                map.put("23", "23.Lighting rod chge/ repl");
                map.put("24", "24.Lighting rod remove");
                map.put("25", "25.Air Conditioning inst");
                map.put("26", "26.Air Conditioning chge/repl");
                map.put("27", "27.Air Conditioning removal");
                map.put("28", "28.TMA inst and testing");
                map.put("29", "29.TMA chge/repl");
                map.put("30", "30.TMA remove and package");
                map.put("31", "31.TRU/TRX addition inst and testing");
                map.put("32", "32.TRU/TRX chge/repl");
                map.put("33", "33.TRU/TRX removal.");
                map.put("34", "34.EDGE DTRU/TSGB Upgrade");
                map.put("35", "35.Repeater inst and testing");
                map.put("36", "36.Repeater chge/repl");
                map.put("37", "37.Repeater removal.");
                map.put("38", "38.GSM Antenna inst");
                map.put("39", "39.GSM Ant loc chge/repl (incl poles)");
                map.put("40", "40.GSM Antenna direction chge");
                map.put("41", "41.GSM Antenna tilting");
                map.put("42", "42.GSM Antenna chge (with pole)");
                map.put("43", "43.GSM Antenna height chge (up/down)");
                map.put("44", "44.GSM Antenna removal");
                map.put("45", "45.FeederInst 0-75m(/sect,noFeedBefor)");
                map.put("46", "46.FeederInst 76-175m(/sec,noFeedBefor)");
                map.put("47", "47.FeederInst 176-^m(/sec,noFeedBefor)");
                map.put("48", "48.Feeder chg/rpl 0-75m(/sect,chge feed)");
                map.put("49", "49.Feeder chg/rpl 76-175m(/sec,chge feed");
                map.put("50", "50.Feeder chg/repl 176-^m(/sec,chgeFeed)");
                map.put("51", "51.Feeder removal 0-75m ");
                map.put("52", "52.Feeder removal 76-175m");
                map.put("53", "53.Feeder removal 176-up m");
                map.put("54", "54.Isol&Inst&Impl grdShelRoofPoles 2-6m");
                map.put("55", "55.Isol&Inst&Impl grdShelRoofPoles 7-10m");
                map.put("56", "56.Chge/repl grd/shel/roof poles 2-6m");
                map.put("57", "57.Chge/repl grd/shel/roof poles 7-10m");
                map.put("58", "58.Removal on ground/ on shelter / on roof for all type of Poles from 2m till 6m");
                map.put("59", "59.Removal on ground/ on shelter / on roof for all type of Poles from 7m till 10m");
                map.put("60", "60.2Mb, connection of PCM cbl to DDF");
                map.put("61", "61.chg/rpl 2Mb conn of PCM cbl to DDF");
                map.put("62", "62.HDSLModem/rout ciscoDlink/SIU ins&tst");
                map.put("63", "63.HDSLModem/rout ciscoDlink/SIU chg/rpl");
                map.put("64", "64.HDSL Modem/router cisco, d-link/SIU Removal");
                map.put("65", "65.MW HOP ins&tst in/odoorActAdj03-1,2m");
                map.put("66", "66.MW HOP ins&tst in/odoorActAdj1,6-3,3m");
                map.put("67", "67.MW HOP chg/rpl in/odoorActAdj03-1,2m");
                map.put("68", "68.MW HOP chg/rpl in/odoorActAdj1,6-3,3m");
                map.put("69", "69.MW HOP removal including indoor, outdoor (0,3-1,2m)");
                map.put("70", "70.MW HOP removal including indoor, outdoor (1,6-3,3m)");
                map.put("71", "71.MW Ant inst and testing (0,3-1,2m)");
                map.put("72", "72.MW Ant inst and testing (1,6-3,3m)");
                map.put("73", "73.MW Antenna chg/repl (0,3-1,2m)");
                map.put("74", "74.MW Antenna chg/repl (1,6-3,3m)");
                map.put("75", "75.MW Antenna removal (0,3-1,2m)");
                map.put("76", "76.MW Antenna removal (1,6-3,3m)");
                map.put("77", "77.MW Antenna dir chg incl adj(0,3-1,2m)");
                map.put("78", "78.MW Ant dir chge incl adj(1,6-3,3m)");
                map.put("79", "79.Power Splitter chge");
                map.put("80", "80.Add fastening by metal pipe (/1pipe)");
                map.put("81", "81.LTU/ETU/PFU inst cabl&test (any type)");
                map.put("82", "82.LTU/ETU/PFU chge/repl (any type)");
                map.put("83", "83.LTU/ETU/PFU removal (any type)");
                map.put("84", "84.NPU inst/chge/repl");
                map.put("85", "85.FAU inst/chge/rem");
                map.put("86", "86.UpgrChngConf MWequip(capProtSoftMod)");
                map.put("87", "87.TraffNode 2p/6p/FIU19 inst and tst");
                map.put("88", "88.TraffNode 20p/Metro HUB inst and tst");
                map.put("89", "89.TraffNod FIU19MetroHUB chgRpl(atypes");
                map.put("90", "90.Traffic Node all type/MetroHUB/FIU19 removal");
                map.put("91", "91.MMU/SMU inst and testing");
                map.put("92", "92.MMU/SMU chge/repl");
                map.put("93", "93.MMU/SMU removal");
                map.put("94", "94.RAU inst+ power splitter (any type)");
                map.put("95", "95.RAU chge (any type)");
                map.put("96", "96.RAU removal with power splitter (any type)");
                map.put("97", "97.AMM 2U/4U/14U inst (incl grounding)");
                map.put("98", "98.AMM 2U/4U/14U chge/repl (incl grnd)");
                map.put("99", "99.AMM 2U/4U/14U removal (including grounding)");
                map.put("100", "100.Transmiss rack 19\" inst (incl grnd)");
                map.put("101", "101.Transm rack 19\" che/rpl (incl grnd)");
                map.put("102", "102.Transmission rack 19\" removal (including grounding)");
                map.put("103", "103.MW Feeder/cbl inst 0-75m");
                map.put("104", "104.MW Feeder/cbl inst 76-175m");
                map.put("105", "105.MW Feeder/cbl inst 176- up m");
                map.put("106", "106.MW Feeder/cbl chge/repl 0-75m");
                map.put("107", "107.MW Feeder/cbl chge/repl 76-175m");
                map.put("108", "108.MW Feeder/cbl chge/repl 176- up m");
                map.put("109", "109.MW Feeder/cable removal 0-75m ");
                map.put("110", "110.MW Feeder/cable removal 75-175m");
                map.put("111", "111.MW Feeder/cable removal 176-up m");
                map.put("112", "112.Add fast+instPipesMLfeeder cbl(*len)");
                map.put("113", "113.PCM/UTP/KVSM/opt p.crd inst 0-75m");
                map.put("114", "114.PCM/UTP/KVSM/opt p.crd inst 76-175m");
                map.put("115", "115.PCM/UTP/KVSM/opt p.crd inst 176-^m");
                map.put("116", "116.PCM/UTP/KVSM/opt p.crd chg/rpl 0-75m");
                map.put("117", "117.PCM/UTP/KVSM/optP.crdChg/rpl76-175m");
                map.put("118", "118.PCM/UTP/KVSM/opt p.crd chg/rpl 176-m");
                map.put("119", "119.PCM/UTP/KVSM/optical patchcord Cable removal 0-75m");
                map.put("120", "120.PCM/UTP/KVSM/optical patchcord Cable removal 76-175m");
                map.put("121", "121.PCM/UTP/KVSM/optical patchcord Cable removal 176-up m");
                map.put("122", "122.DDF/plinth inst+ cabiling (for pair)");
                map.put("123", "123.DDF/plinth chge/repl (for pair)");
                map.put("124", "124.DDF/plinth removal (for pair) ");
                map.put("125", "125.Ind cbl trac way i/c/r/rm 0-50m");
                map.put("126", "126.Ind cbl trac way i/c/r/rm 51-100m");
                map.put("127", "127.Ind cbl trac way i/c/r/rm 101-^m");
                map.put("128", "128.Odoor cbl trac way i/c/r/rm 0-50m");
                map.put("129", "129.Odoor cbl trac way i/c/r/rm 101-^m");
                map.put("130", "130.Odoor cbl trac way i/c/r/rm 51-100 m");
                map.put("131", "131.Odoor TR rack inst");
                map.put("132", "132.Odoor TR rack chge/repl");
                map.put("133", "133.Outdoor TR rack removal");
                map.put("134", "134.DC/DC convert inst, cabling connect");
                map.put("135", "135.DC/DC converter chge/repl");
                map.put("136", "136.DC/DC converter removal");
                map.put("137", "137.DDU inst/chge/rem");
                map.put("138", "138.PwrCblIns(4x10/,4x16)/m(exclMat)");
                map.put("139", "139.PwrCbl chg/rpl (4x10, 4x16),/m");
                map.put("140", "140.Power Cable Removal (4x10mm2, 4x16mm2), per m ");
                map.put("141", "141.PwrCbl inst (4x25mm2),/m");
                map.put("142", "142.PwrCbl chge/repl (4x25mm2),/m");
                map.put("143", "143.Power cable removal (4x25mm2), per m");
                map.put("144", "144.Actura/Rectifier inst (any type)");
                map.put("145", "145.Actura/Rectifier chge (any type)");
                map.put("146", "146.Actura/Rectifier Removal (any type)");
                map.put("147", "147.Power counter inst");
                map.put("148", "148.Power counter chge/repl");
                map.put("149", "149.Power counter Removal");
                map.put("150", "150.Circuit breaker inst per breaker");
                map.put("151", "151.Circuit break chge/ repl per breaker");
                map.put("152", "152.Cross Connection Checking ");
                map.put("153", "153.Lock installation");
                map.put("154", "154.Lock change/replacement");
                map.put("155", "155.Mobile BTS inst (TR,Power,ON-AIR)");
                map.put("156", "156.Inst&On-air Colloc BTS on site");
                map.put("157", "157.Inst surv/build site (incl a.design)");
                map.put("158", "158.Impl/indoor ant (1-20 pcs per site)");
                map.put("159", "159.Impl/indoor ant (21-50 pcs per site)");
                map.put("160", "160.Impl/indoor antenna (51-^ per site)");
                return Collections.unmodifiableMap(map);
            })).get();

    protected void sendMail(DelegateExecution delegateExecution, String assignee, String recipient) {
        try {
            String siteName = (String) delegateExecution.getVariable("siteName");
            String jrNumber = (String) delegateExecution.getVariable("jrNumber");
            String jobDescription = (String) delegateExecution.getVariable("jobDescription");
            String explanation = (String) delegateExecution.getVariable("explanation");
            ObjectMapper mapper = new ObjectMapper();
            ArrayNode jobWorks = (ArrayNode) mapper.readTree(delegateExecution.getVariableTyped("jobWorks").getValue().toString());
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
            cell.setCellValue("Request Date :");
            cell.setCellStyle(alignRight);

            row.createCell(2).setCellValue("");

            row = sheet.createRow(12);
            cell = row.createCell(0);
            cell.setCellValue("City Name :");
            cell.setCellStyle(alignRight);
            row.createCell(2).setCellValue("");

            row = sheet.createRow(13);
            cell = row.createCell(0);
            cell.setCellValue("Site Name :");
            cell.setCellStyle(alignRight);

            row.createCell(2).setCellValue("");

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
                cell.setCellStyle(autoWrap);
                cell.setCellValue((worksTitle.get(jobWorks.get(i).get("sapServiceNumber").textValue()) != null ? worksTitle.get(jobWorks.get(i).get("sapServiceNumber").textValue()) : jobWorks.get(i).get("sapServiceNumber").textValue()) + " - " + jobWorks.get(i).get("quantity").numberValue().intValue());
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
            row.createCell(2).setCellValue("");

            row = sheet.createRow(21 + jobWorks.size());
            row.createCell(2).setCellValue("             (position, name & signature)");

            row = sheet.createRow(22 + jobWorks.size());
            row.createCell(2).setCellValue("Tel:");

            row = sheet.createRow(23 + jobWorks.size());
            row.createCell(2).setCellValue("Approved by:");

            row = sheet.createRow(24 + jobWorks.size());
            row.createCell(2).setCellValue("");

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
                System.out.println((15 + i) + ". " + sheet.getRow(15 + i).getCell(0).getStringCellValue());
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

            DataSource source = new ByteArrayDataSource(is, "application/pdf");

            MultiPartEmail email = new MultiPartEmail();
            email.setCharset("utf-8");
            email.setHostName(configuration.getProperties().getProperty("mail.smtp.host", "mail"));
            email.setSmtpPort(Integer.valueOf(configuration.getProperties().getProperty("mail.smtp.port", "1025")));

            email.setFrom(configuration.getSender());
            email.setSubject("JR " + jrNumber + " Blank");
            email.setMsg("Your JR Approved. JR Blank attached\n" +
                    "\n" +
                    "\n" +
                    "Пройдя по следующей ссылке на страницу в HUB.Kcell.kz, вы можете оставить в поле комментариев свои замечания и/или пожелания относительно функционала и интерфейса системы: https://hub.kcell.kz/x/kYNoAg");
            email.addTo(recipient);
            email.setBcc(Arrays.asList(InternetAddress.parse("Askar.Slambekov@kcell.kz, Yernaz.Kalingarayev@kcell.kz")));
            email.attach(source, "jr-blank.xlsx", "Job Request blank");

            email.send();
            LOGGER.info("Task Assignment Email successfully sent to user '" + assignee + "' with address '" + recipient + "'.");

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.log(Level.WARNING, "Could not send email to assignee", e);
        }
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String recipient = (String) delegateExecution.getVariable("starter");

        if (recipient == null) {
            LOGGER.warning("Recipient is null for activity instance " + delegateExecution.getActivityInstanceId() + ", aborting mail notification");
            return;
        }

        IdentityService identityService = Context.getProcessEngineConfiguration().getIdentityService();
        User user = identityService.createUserQuery().userId(recipient).singleResult();

        if (user != null) {

            // Get Email Address from User Profile
            String recipientEmail = user.getEmail();

            if (recipient != null && !recipient.isEmpty()) {

                sendMail(delegateExecution, recipient, recipientEmail);

            } else {
                LOGGER.warning("Not sending email to user " + recipient + "', user has no email address.");
            }

        } else {
            LOGGER.warning("Not sending email to user " + recipient + "', user is not enrolled with identity service.");
        }
    }
}
