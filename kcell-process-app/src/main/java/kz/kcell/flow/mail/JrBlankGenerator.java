package kz.kcell.flow.mail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import kz.kcell.bpm.SetPricesDelegate;
import kz.kcell.bpm.revision.SetWorkVariables;
import lombok.SneakyThrows;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JrBlankGenerator {

    private static final Map<String, String> worksTitle = new HashMap<>();
    private static final Map<String, JsonNode> worksPrice = new HashMap<>();

    private static final Map<String, String> contractorsTitle =
        ((Supplier<Map<String, String>>) (() -> {
            Map<String, String> map = new HashMap<>();
            map.put("1", "ТОО \"Аврора Сервис\"");
            map.put("2", "ТОО \"AICOM\"");
            map.put("3", "ТОО \"Spectr energy group\"");
            map.put("4", "TOO \"Line System Engineering\"");
            map.put("5", "Kcell_region");
            map.put("6", "TOO \"Алта Телеком\"");
            map.put("7", "АО \"Логиком\"");
            map.put("8", "TOO \"Arlan SI\"");
            map.put("9", "ТОО \"Inter Service\"");
            map.put("10", "TOO \"Forester-Hes Group\"");
            map.put("11", "TOO \"Транстелеком\"");
            map.put("12", "TOO \"Востоктелеком\"");
            map.put("13", "TOO \"Antares engineering\"");
            map.put("14", "TOO \"NURTRANSENERGOTELECOM\"");
            return Collections.unmodifiableMap(map);
        })).get();

    private static final Map<String, Double> DISCOUNT_MAP = new HashMap<String,Double>() {{
        put("Акмолинская область", 0.20);
        put("Алматинская область", 0.20);
        put("Восточно-Казахстанская область", 0.20);
        put("Жамбылская область", 0.20);
        put("Атырауская область", 0.17);
        put("Мангыстауская область", 0.20);
        put("Западно-Казахстанская область", 0.17);
        put("Туркестанская область", 0.17);
        put("Кызылординская область", 0.04);
        put("Актюбинская область", 0.03);
        put("Карагандинская область", 0.03);
        put("Костанайская область", 0.03);
        put("Северо-Казахстанская область", 0.03);
        put("Павлодарская область", 0.03);
    }};

    private final static Map<String, String> contractNumber;
    static
    {
        contractNumber = new HashMap<String, String>();
        contractNumber.put("6", "734974/2022/1 (CM 98137)");
        contractNumber.put("7", "734974/2022/2-1 (CM 98110)");
        contractNumber.put("8", "734974/2022/4 (CM 98132)");
        contractNumber.put("10", "734974/2022/3 (CM 98136)");
        contractNumber.put("11", "734974/2022/5 (CM 98235)");
    };

    private final static Map<String, String> contractNumberSAO2023;
    static
    {
        contractNumberSAO2023 = new HashMap<String, String>();
        contractNumberSAO2023.put("7", "808229/2023/2");
        contractNumberSAO2023.put("8", "777717/2023/1");
        contractNumberSAO2023.put("9", "808229/2023/1");
        contractNumberSAO2023.put("10", "777717/2023/2");
    };

    private final static Map<String, String> contractDateSAO2023;
    static
    {
        contractDateSAO2023 = new HashMap<String, String>();
        contractDateSAO2023.put("7", "14.02.2023 г.");
        contractDateSAO2023.put("8", "17.01.2023 г.");
        contractDateSAO2023.put("9", "10.02.2023 г.");
        contractDateSAO2023.put("10", "18.01.2023 г.");
    };

    private final static Map<String, String> reasonsTitle;
    static
    {
        reasonsTitle = new HashMap<String, String>();
        reasonsTitle.put("1", "Оптимизация и планирование");
        reasonsTitle.put("2", "Трансмиссия");
        reasonsTitle.put("3", "Инфраструктура");
        reasonsTitle.put("4", "Эксплуатация");
        reasonsTitle.put("5", "Строительно-монтажные работы");
        reasonsTitle.put("6", "Подготовка проекта");
    };

    public byte[] generate(DelegateExecution delegateExecution) throws Exception {

        String mainContract = (String) delegateExecution.getVariable("mainContract");
        String reason = reasonsTitle.get(delegateExecution.getVariable("reason"));
        String project = (String) delegateExecution.getVariable("project");
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
        Date workStartDate = (Date) delegateExecution.getVariable("workStartDate");
        Date integrationRunDate = (Date) delegateExecution.getVariable("integrationRunDate");
        Date workCompletionDate = (Date) delegateExecution.getVariable("workCompletionDate");
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        String priority = delegateExecution.getVariable("priority").toString();
        BigDecimal jobWorksTotal = BigDecimal.ZERO;
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode jobWorks = (ArrayNode) mapper.readTree(delegateExecution.getVariableTyped("jobWorks").getValue().toString());
        JsonNode initiatorFull = mapper.readTree(delegateExecution.getVariableTyped("initiatorFull").getValue().toString());
        Object unitWorkPrice_jr_test= delegateExecution.getVariable("unitWorkPrice_jr");
        ArrayNode unitWorkPrice_jr=null;
        if(!(unitWorkPrice_jr_test == null)&& !Objects.equals(unitWorkPrice_jr_test.toString(), "null")){
            unitWorkPrice_jr = (ArrayNode) mapper.readTree(delegateExecution.getVariableTyped("unitWorkPrice_jr").getValue().toString());
       }
        String siteRegion = (String) delegateExecution.getVariable("siteRegion");
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();

        if (worksTitle.size() == 0) {
            if ("nc".equals(siteRegion) || "east".equals(siteRegion)) {
                siteRegion = "astana";
            }
            InputStream fis = SetWorkVariables.class.getResourceAsStream("/revision/newWorkPrice.json");
            InputStreamReader reader = new InputStreamReader(fis, "utf-8");
            ArrayNode json = (ArrayNode) mapper.readTree(reader);

            for (JsonNode workPrice : json) {
                worksPrice.put(workPrice.get("id").textValue(), workPrice.get("price"));
                worksTitle.put(workPrice.get("id").textValue(), workPrice.get("title").textValue());
            }
        }


        if (mainContract.equalsIgnoreCase("2023primary_source") || mainContract.equalsIgnoreCase("Vostoktelecom")||mainContract.equalsIgnoreCase("open-tender-2023")) {
            String subcontractor = (String) delegateExecution.getVariable("subcontractorName");
            String oblastName = (String) delegateExecution.getVariable("oblastName");
            return generateFor2023PrimarySource(
                reason,
                jrNumber,
                explanation,
                subcontractor,
                requestDate,
                workStartDate,
                integrationRunDate,
                workCompletionDate,
                sdf,
                jobWorks,
                initiatorFull,
                oblastName
            );
        } else if (mainContract.equalsIgnoreCase("2022Work-agreement")||mainContract.equals("technical_maintenance_services")){
            if ("nc".equals(siteRegion) || "east".equals(siteRegion)) {
                siteRegion = "astana";
            }
            String oblastName = (String) delegateExecution.getVariable("oblastName");
            if(oblastName.contains("Шымкент (г.а.)")) oblastName="Туркестанская область";

            CellRangeAddress cellRangeAddress;
            Map<String, Object> properties = new HashMap<>();

            XSSFFont timesNewRoman11 = workbook.createFont();
            timesNewRoman11.setFontName(HSSFFont.FONT_ARIAL);
            timesNewRoman11.setFontHeightInPoints((short)11);
            timesNewRoman11.setBold(true);
            timesNewRoman11.setItalic(true);

            XSSFFont arialBI9 = workbook.createFont();
            arialBI9.setFontName(HSSFFont.FONT_ARIAL);
            arialBI9.setFontHeightInPoints((short) 9);
            arialBI9.setItalic(true);
            arialBI9.setBold(true);

            XSSFFont arialB10 = workbook.createFont();
            arialB10.setFontName(HSSFFont.FONT_ARIAL);
            arialB10.setFontHeightInPoints((short) 10);
            arialB10.setBold(true);

            XSSFFont arialB8 = workbook.createFont();
            arialB8.setFontName(HSSFFont.FONT_ARIAL);
            arialB8.setFontHeightInPoints((short) 8);
            arialB8.setBold(true);

            XSSFFont arialI6 = workbook.createFont();
            arialI6.setFontName(HSSFFont.FONT_ARIAL);
            arialI6.setFontHeightInPoints((short) 6);
            arialI6.setItalic(true);

            XSSFFont arial12 = workbook.createFont();
            arial12.setFontName(HSSFFont.FONT_ARIAL);
            arial12.setFontHeightInPoints((short) 12);

            //common properties
            properties.put(CellUtil.ALIGNMENT, HorizontalAlignment.RIGHT);
            properties.put(CellUtil.VERTICAL_ALIGNMENT, VerticalAlignment.CENTER);
            properties.put(CellUtil.WRAP_TEXT, true);

            Row row = sheet.createRow(0);
            row.createCell(0);

            Cell cell = row.createCell(6);
            cell.setCellValue("ПРИЛОЖЕНИЕ № 2");
            CellUtil.setFont(cell, timesNewRoman11);
            CellUtil.setCellStyleProperties(cell, properties);

            cell = row.createCell(11);
            cell.setCellValue("Техническое задание к ");
            CellUtil.setFont(cell, arialB10);

            cell = row.createCell(13);
            cell.setCellValue("Заказ  №"+ jrNumber);

            row = sheet.createRow(1);
            row.setHeight((short)500);
            cell = row.createCell(6);
            StringBuilder stringBuilder = new StringBuilder();
            if (mainContract.equals("technical_maintenance_services")) {
                stringBuilder.append("К Договору подряда №")
                    .append(contractNumberSAO2023.get(contractor) != null ? contractNumberSAO2023.get(contractor) : "________")
                    .append(System.lineSeparator())
                    .append(contractDateSAO2023.get(contractor));
            } else {
                stringBuilder.append("К Договору подряда №")
                    .append(contractNumber.get(contractor) != null ? contractNumber.get(contractor) : "________")
                    .append(System.lineSeparator())
                    .append("от 16.09.2022 г");
            }
            cell.setCellValue(stringBuilder.toString());
            CellUtil.setFont(cell, arialBI9);
            CellUtil.setCellStyleProperties(cell, properties);


            properties.replace(CellUtil.ALIGNMENT, HorizontalAlignment.CENTER);

            row = sheet.createRow(5);
            row.createCell(3).setCellValue("Заказ  №");
            row.createCell(4).setCellValue(jrNumber);
            row.createCell(6).setCellValue("от");
            row.createCell(7).setCellValue(DateFormatUtils.format(requestDate, "dd.MM.yyyy"));

            CellUtil.setFont(row.getCell(3), arial12);
            CellUtil.setFont(row.getCell(4), arial12);
            CellUtil.setFont(row.getCell(6), arial12);
            CellUtil.setFont(row.getCell(7), arial12);

            row = sheet.createRow(7);
            row.createCell(1).setCellValue("Адрес:");
            row.createCell(4).setCellValue(siteAddress);
            CellUtil.setCellStyleProperty(row.getCell(4), CellUtil.WRAP_TEXT, true);

            row = sheet.createRow(8);
            row.createCell(1).setCellValue("Заказчик:");
            row.createCell(4).setCellValue("АО \"Кселл\"");

            row = sheet.createRow(9);
            row.createCell(1).setCellValue("Подрядчик:");
            row.createCell(4).setCellValue(contractorsTitle.get(contractor) != null ? contractorsTitle.get(contractor) : "");

            row = sheet.createRow(10);
            row.createCell(1).setCellValue("Тип Работ:");
            row.createCell(4).setCellValue(reason);
            row.createCell(6).setCellValue("Проект:");
            row.createCell(7).setCellValue(project);

            CellUtil.setFont(row.getCell(7), arialB10);

            row = sheet.createRow(11);
            row.createCell(1).setCellValue("Дата начала выполнения работ:");
            row.createCell(4).setCellValue(workStartDate != null ? sdf.format(workStartDate) : "");

            if (!mainContract.equals("technical_maintenance_services")) {
                row = sheet.createRow(12);
                row.createCell(1).setCellValue("Дата выполнения Интеграции");
                row.createCell(4).setCellValue(integrationRunDate != null ? sdf.format(integrationRunDate) : "");
            }

            row = sheet.createRow(13);
            row.createCell(1).setCellValue("Дата окончания работ:");
            row.createCell(4).setCellValue(workCompletionDate != null ? sdf.format(workCompletionDate) : "");

            row = sheet.createRow(14);
            row.createCell(1).setCellValue("Критическая просрочка(дней):");

            for(int i = 7; i < 15; i++){
                if(i==12)continue;
                CellUtil.setCellStyleProperties(sheet.getRow(i).getCell(1), properties);
                CellUtil.setFont(sheet.getRow(i).getCell(1), arialB10);
            }

            row = sheet.createRow(16);
            row.createCell(0).setCellValue("№");
            row.createCell(1).setCellValue("№ статьи по ТЦП");
            row.createCell(2).setCellValue("Имя сайта");
            row.createCell(3).setCellValue("Наименование");
            row.createCell(4).setCellValue("Ед.изм.");
            row.createCell(5).setCellValue("Кол-во");
            row.createCell(6).setCellValue("Стоимость работ.За ед. Тенге, без НДС.");
            row.createCell(7).setCellValue("Сумма работ включая материалы, без НДС");
            row.createCell(8).setCellValue("Сумма работ включая материалы, вкл. НДС 12%");

            for(int i = 0; i < 9; i++){
                CellUtil.setCellStyleProperties(row.getCell(i), properties);
                CellUtil.setFont(row.getCell(i), arialB8);
            }

            row.createCell(11).setCellValue("Необходимый материал");
            CellUtil.setFont(row.getCell(11), arialB10);

            properties.put(CellUtil.WRAP_TEXT, true);
            properties.put(CellUtil.BORDER_TOP, BorderStyle.THIN);
            properties.put(CellUtil.BORDER_LEFT, BorderStyle.THIN);
            properties.put(CellUtil.BORDER_RIGHT, BorderStyle.THIN);
            properties.put(CellUtil.BORDER_BOTTOM, BorderStyle.THIN);

            CellUtil.setCellStyleProperties(row.getCell(0), properties);
            CellUtil.setCellStyleProperties(row.getCell(1), properties);
            CellUtil.setCellStyleProperties(row.getCell(2), properties);
            CellUtil.setCellStyleProperties(row.getCell(3), properties);
            CellUtil.setCellStyleProperties(row.getCell(4), properties);
            CellUtil.setCellStyleProperties(row.getCell(5), properties);
            CellUtil.setCellStyleProperties(row.getCell(6), properties);
            CellUtil.setCellStyleProperties(row.getCell(7), properties);
            CellUtil.setCellStyleProperties(row.getCell(8), properties);




            for(int i = 17; i < 35; i++){
                row = sheet.createRow(i);
                for(int j = 11; j < 17; j++){
                    if (i == 17) {
                        switch (j) {
                            case 11:
                                row.createCell(j).setCellValue("№");
                                break;
                            case 12:
                                row.createCell(j).setCellValue("Номер SAP");
                                break;
                            case 13:
                                row.createCell(j).setCellValue("Наименование");
                                break;
                            case 14:
                                row.createCell(j).setCellValue("Код продукта");
                                break;
                            case 15:
                                row.createCell(j).setCellValue("Ед.изм.");
                                break;
                            case 16:
                                row.createCell(j).setCellValue("Кол-во");
                                break;
                        }
                    }else {
                        row.createCell(j);
                    }
                    if(j == 11)
                        CellUtil.setCellStyleProperty(sheet.getRow(i).getCell(j), CellUtil.BORDER_LEFT, BorderStyle.THIN);
                    if(i == 34)
                        CellUtil.setCellStyleProperty(sheet.getRow(i).getCell(j), CellUtil.BORDER_BOTTOM, BorderStyle.THIN);

                    CellUtil.setCellStyleProperty(sheet.getRow(i).getCell(j), CellUtil.BORDER_TOP, BorderStyle.THIN);
                    CellUtil.setCellStyleProperty(sheet.getRow(i).getCell(j), CellUtil.BORDER_RIGHT, BorderStyle.THIN);
                    CellUtil.setFont(sheet.getRow(i).getCell(j), arialB8);
                }

            }

            row = sheet.createRow(36);
            row.createCell(11).setCellValue("Контактное лицо: "+ initiatorFull.get("firstName").textValue() + " " + initiatorFull.get("lastName").textValue());
            CellUtil.setFont(row.getCell(11), arialB10);

            for (int i = 0; i < jobWorks.size(); i++) {
                JsonNode priceJson = worksPrice.get(jobWorks.get(i).get("sapServiceNumber").textValue());
                BigDecimal jobPrice=null;
                if ("technical_maintenance_services".equals(mainContract)) {
                    if (jobWorks.get(i).get("materials").asBoolean()) {
                        jobPrice = new BigDecimal(priceJson.get(oblastName).get(jobWorks.get(i).get("isMaterialsActive").textValue()).textValue()).setScale(2, RoundingMode.DOWN);
                    } else if (Arrays.asList("-","").contains(priceJson.get(oblastName).get("active").textValue())) {
                        jobPrice = new BigDecimal(priceJson.get(oblastName).get("inactive").textValue()).setScale(2, RoundingMode.DOWN);
                    } else {
                        jobPrice = new BigDecimal(priceJson.get(oblastName).get("active").textValue()).setScale(2, RoundingMode.DOWN);
                    }
                }else {
                    jobPrice = new BigDecimal(priceJson.get(oblastName).textValue()).setScale(2, RoundingMode.DOWN);
                }
               if (unitWorkPrice_jr!=null) {
                   if (unitWorkPrice_jr.get(i) != null) {
                       String s = String.valueOf(unitWorkPrice_jr.get(i));
                       jobPrice = new BigDecimal(s).setScale(2, RoundingMode.DOWN);
                   }
               }
                if (priority.equals("emergency")) {
                    jobPrice = jobPrice.multiply(new BigDecimal("1.2"));
                }

                BigDecimal priceWithMaterial = jobPrice.multiply(new BigDecimal(jobWorks.get(i).get("quantity").asText())).setScale(2, RoundingMode.DOWN);
                BigDecimal jobPriceWithVAT = priceWithMaterial.add(priceWithMaterial.multiply(new BigDecimal("0.12"))).setScale(2, RoundingMode.DOWN);

                jobWorksTotal = jobWorksTotal.add(jobPriceWithVAT).setScale(2, RoundingMode.DOWN);

                row = sheet.getRow(17+i) != null ? sheet.getRow(17+i) :sheet.createRow(17 + i);
                row.createCell(0).setCellValue(i+1);

                row.createCell(1).setCellValue(jobWorks.get(i).get("displayServiceName").textValue().split(" ")[0]);
                cell = row.createCell(2);
                StringBuilder siteNames = new StringBuilder();
                ArrayNode sites = (ArrayNode) jobWorks.get(i).get("relatedSites");
                for (int j = 0; j < sites.size(); j++) {
                    siteNames.append(sites.get(j).get("site_name").textValue());
                    if (j < sites.size()-1)
                        siteNames.append(',');
                }
                cell.setCellValue(siteNames.toString());

                row.createCell(3).setCellValue(jobWorks.get(i).get("displayServiceName").textValue());

                row.createCell(4).setCellValue(jobWorks.get(i).get("materialUnit").textValue());

                row.createCell(5).setCellValue(jobWorks.get(i).get("quantity").asText());

                row.createCell(6).setCellValue(jobPrice.toString());

                row.createCell(7).setCellValue(priceWithMaterial.toString());

                row.createCell(8).setCellValue(jobPriceWithVAT.toString());

                CellUtil.setCellStyleProperties(row.getCell(0), properties);
                CellUtil.setCellStyleProperties(row.getCell(1), properties);
                CellUtil.setCellStyleProperties(row.getCell(2), properties);
                CellUtil.setCellStyleProperties(row.getCell(3), properties);
                CellUtil.setCellStyleProperties(row.getCell(4), properties);
                CellUtil.setCellStyleProperties(row.getCell(5), properties);
                CellUtil.setCellStyleProperties(row.getCell(6), properties);
                CellUtil.setCellStyleProperties(row.getCell(7), properties);
                CellUtil.setCellStyleProperties(row.getCell(8), properties);

            }

            if (!mainContract.equals("technical_maintenance_services")) {
                row = sheet.getRow(18 + jobWorks.size()) != null ? sheet.getRow(18 + jobWorks.size()) : sheet.createRow(18 + jobWorks.size());
                row.createCell(7).setCellValue("Размер скидки, %:");
                row.createCell(8).setCellValue(Math.round(DISCOUNT_MAP.get(oblastName) * 100));
                CellUtil.setFont(row.getCell(7), arialB10);
            }

            BigDecimal jobWorksTotalDiscounted = null;
            if (!mainContract.equals("technical_maintenance_services")) {
                jobWorksTotalDiscounted = jobWorksTotal.multiply(BigDecimal.valueOf(1 - DISCOUNT_MAP.get(oblastName))).setScale(2, RoundingMode.HALF_UP);
            }
            row = sheet.getRow(19+jobWorks.size())!=null?sheet.getRow(19+jobWorks.size()):sheet.createRow(19 + jobWorks.size());
            row.createCell(7).setCellValue("Всего работ на сумму:");
            if (mainContract.equals("technical_maintenance_services")) {
                row.createCell(8).setCellValue(jobWorksTotal.doubleValue());
            } else {
                row.createCell(8).setCellValue(jobWorksTotalDiscounted.doubleValue());
            }
            CellUtil.setFont(row.getCell(7), arialB10);

            row = sheet.getRow(20+jobWorks.size())!=null?sheet.getRow(20+jobWorks.size()):sheet.createRow(20 + jobWorks.size());
            row.createCell(7).setCellValue("в т.ч. НДС:");
            if (mainContract.equals("technical_maintenance_services")) {
                row.createCell(8).setCellValue(jobWorksTotal.subtract(jobWorksTotal.divide(new BigDecimal("1.12"), 2, RoundingMode.HALF_UP)).toString());
            } else {
                row.createCell(8).setCellValue(jobWorksTotalDiscounted.subtract(jobWorksTotalDiscounted.divide(new BigDecimal("1.12"), 2, RoundingMode.HALF_UP)).toString());
            }
            CellUtil.setFont(row.getCell(7), arialB10);

            row = sheet.getRow(22+jobWorks.size())!=null?sheet.getRow(22+jobWorks.size()):sheet.createRow(22 + jobWorks.size());
            row.createCell(3).setCellValue("Итого сумма включая НДС, 12%:");
            if (mainContract.equals("technical_maintenance_services")) {
                row.createCell(5).setCellValue(jobWorksTotal.doubleValue());
            } else {
                row.createCell(5).setCellValue(jobWorksTotalDiscounted.doubleValue());
            }
            CellUtil.setFont(row.getCell(3), arialB10);

            properties.remove(CellUtil.BORDER_TOP);
            properties.remove(CellUtil.BORDER_BOTTOM);
            properties.remove(CellUtil.BORDER_LEFT);
            properties.remove(CellUtil.BORDER_RIGHT);
            CellUtil.setCellStyleProperties(row.getCell(3), properties);

            row = sheet.getRow(25+jobWorks.size())!=null?sheet.getRow(25+jobWorks.size()):sheet.createRow(25 + jobWorks.size());
            row.setHeight((short)500);
            row.createCell(2).setCellValue("Подрядчик: _________");
            row.createCell(6).setCellValue("Заказчик: АО \"Кселл\"");
            CellUtil.setFont(row.getCell(2), arialB10);
            CellUtil.setFont(row.getCell(6), arialB10);

            properties.replace(CellUtil.VERTICAL_ALIGNMENT, VerticalAlignment.TOP);
            row = sheet.getRow(29+jobWorks.size())!=null?sheet.getRow(29+jobWorks.size()):sheet.createRow(29 + jobWorks.size());
            row.createCell(2).setCellValue("(должность)");
            row.createCell(6).setCellValue("(должность)");
            CellUtil.setCellStyleProperties(row.getCell(2), properties);
            CellUtil.setCellStyleProperties(row.getCell(6), properties);
            CellUtil.setFont(row.getCell(2), arialI6);
            CellUtil.setFont(row.getCell(6), arialI6);

            row = sheet.getRow(32+jobWorks.size())!=null?sheet.getRow(32+jobWorks.size()):sheet.createRow(32 + jobWorks.size());
            row.createCell(2).setCellValue("(Ф.И.О., подпись)");
            row.createCell(6).setCellValue("(Ф.И.О., подпись)");
            CellUtil.setCellStyleProperties(sheet.getRow(row.getRowNum()).getCell(2), properties);
            CellUtil.setCellStyleProperties(sheet.getRow(row.getRowNum()).getCell(6), properties);
            CellUtil.setFont(row.getCell(2), arialI6);
            CellUtil.setFont(row.getCell(6), arialI6);

            sheet.addMergedRegion(new CellRangeAddress(0, 0, 6, 9));
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 6, 9));

            sheet.addMergedRegion(new CellRangeAddress(7, 7, 1, 3));
            sheet.addMergedRegion(new CellRangeAddress(8, 8, 1, 3));
            sheet.addMergedRegion(new CellRangeAddress(9, 9, 1, 3));
            sheet.addMergedRegion(new CellRangeAddress(10, 10, 1, 3));
            sheet.addMergedRegion(new CellRangeAddress(11, 11, 1, 3));
            sheet.addMergedRegion(new CellRangeAddress(12, 12, 1, 3));
            sheet.addMergedRegion(new CellRangeAddress(13, 13, 1, 3));
            sheet.addMergedRegion(new CellRangeAddress(14, 14, 1, 3));

            sheet.addMergedRegion(new CellRangeAddress(5, 5, 4, 5));
            sheet.addMergedRegion(new CellRangeAddress(7, 7, 4, 8));

            sheet.addMergedRegion(new CellRangeAddress(jobWorks.size() + 25, jobWorks.size() + 25, 2, 3));
            sheet.addMergedRegion(new CellRangeAddress(jobWorks.size() + 25, jobWorks.size() + 25, 6, 7));

            cellRangeAddress = new CellRangeAddress(11, 11, 4, 4);
            RegionUtil.setBorderBottom(BorderStyle.THIN, cellRangeAddress, sheet);
            cellRangeAddress = new CellRangeAddress(12, 12, 4, 4);
            RegionUtil.setBorderBottom(BorderStyle.THIN, cellRangeAddress, sheet);
            cellRangeAddress = new CellRangeAddress(13, 13, 4, 4);
            RegionUtil.setBorderBottom(BorderStyle.THIN, cellRangeAddress, sheet);
            cellRangeAddress = new CellRangeAddress(14, 14, 4, 4);
            RegionUtil.setBorderBottom(BorderStyle.THIN, cellRangeAddress, sheet);

            cellRangeAddress = new CellRangeAddress(jobWorks.size() + 28, jobWorks.size() + 28, 2, 3);
            sheet.addMergedRegion(cellRangeAddress);
            RegionUtil.setBorderBottom(BorderStyle.THIN, cellRangeAddress, sheet);
            cellRangeAddress = new CellRangeAddress(jobWorks.size() + 28, jobWorks.size() + 28, 6, 7);
            sheet.addMergedRegion(cellRangeAddress);
            RegionUtil.setBorderBottom(BorderStyle.THIN, cellRangeAddress, sheet);

            cellRangeAddress = new CellRangeAddress(jobWorks.size() + 29, jobWorks.size() + 29, 2, 3);
            sheet.addMergedRegion(cellRangeAddress);
            cellRangeAddress = new CellRangeAddress(jobWorks.size() + 29, jobWorks.size() + 29, 6, 7);
            sheet.addMergedRegion(cellRangeAddress);

            cellRangeAddress = new CellRangeAddress(jobWorks.size() + 30, jobWorks.size() + 30, 2, 3);
            sheet.addMergedRegion(cellRangeAddress);
            cellRangeAddress = new CellRangeAddress(jobWorks.size() + 30, jobWorks.size() + 30, 6, 7);
            sheet.addMergedRegion(cellRangeAddress);

            cellRangeAddress = new CellRangeAddress(jobWorks.size() + 31, jobWorks.size() + 31, 2, 3);
            sheet.addMergedRegion(cellRangeAddress);
            RegionUtil.setBorderTop(BorderStyle.THIN, cellRangeAddress, sheet);
            RegionUtil.setBorderBottom(BorderStyle.THIN, cellRangeAddress, sheet);
            cellRangeAddress = new CellRangeAddress(jobWorks.size() + 31, jobWorks.size() + 31, 6, 7);
            sheet.addMergedRegion(cellRangeAddress);
            RegionUtil.setBorderTop(BorderStyle.THIN, cellRangeAddress, sheet);
            RegionUtil.setBorderBottom(BorderStyle.THIN, cellRangeAddress, sheet);

            cellRangeAddress = new CellRangeAddress(jobWorks.size() + 32, jobWorks.size() + 32, 2, 3);
            sheet.addMergedRegion(cellRangeAddress);
            cellRangeAddress = new CellRangeAddress(jobWorks.size() + 32, jobWorks.size() + 32, 6, 7);
            sheet.addMergedRegion(cellRangeAddress);

            cellRangeAddress = new CellRangeAddress(3, 15, 11, 16);
            sheet.addMergedRegion(cellRangeAddress);

            RegionUtil.setBorderTop(BorderStyle.THIN, cellRangeAddress, sheet);
            RegionUtil.setBorderBottom(BorderStyle.THIN, cellRangeAddress, sheet);
            RegionUtil.setBorderLeft(BorderStyle.THIN, cellRangeAddress, sheet);
            RegionUtil.setBorderRight(BorderStyle.THIN, cellRangeAddress, sheet);


            sheet.setColumnWidth(0, 900);
            sheet.setColumnWidth(1,3000);
            sheet.setColumnWidth(2, 7000);
            sheet.setColumnWidth(3, 11000);
            sheet.setColumnWidth(4, 5000);
            sheet.setColumnWidth(5, 2000);
            sheet.setColumnWidth(6, 4000);
            sheet.setColumnWidth(7, 8000);
            sheet.setColumnWidth(8, 3000);
            sheet.setColumnWidth(11, 8000);
            sheet.setColumnWidth(12, 3000);
            sheet.setColumnWidth(13, 12000);
            sheet.setColumnWidth(14, 5000);
            sheet.setColumnWidth(15, 4000);
            sheet.setColumnWidth(16, 4000);

            // explanation of works
            row = sheet.getRow(3) != null ? sheet.getRow(3) : sheet.createRow(3);
            cell = row.getCell(11) != null ? row.getCell(11) : row.createCell(11);
            cell.setCellValue(explanation != null ? explanation : "");
            CellUtil.setAlignment(cell, HorizontalAlignment.LEFT);
            CellUtil.setVerticalAlignment(cell, VerticalAlignment.CENTER);

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setWrapText(true);
            cell.setCellStyle(cellStyle);
        } else{
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
            row.setHeight((short) 2400);

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


            int[] rows = new int[]{18, 19, 20, 21, 22, 23, 24, 25, 26};

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
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        out.close();
        return out.toByteArray();
    }

    @SneakyThrows
    private byte[] generateFor2023PrimarySource(String reason,
                                                String jrNumber,
                                                String explanation,
                                                String subcontractor,
                                                Date requestDate,
                                                Date workStartDate,
                                                Date integrationRunDate,
                                                Date workCompletionDate,
                                                SimpleDateFormat sdf,
                                                ArrayNode jobWorks,
                                                JsonNode initiatorFull,
                                                String oblastName) {

        List<Map<String, String>> subcontractorList = new ArrayList<>();

        Map<String, String> contract1 = new HashMap<>();
        contract1.put("Contractor number", "103120");
        contract1.put("Contract date", "17.07.2023");
        contract1.put("Contractor name", "Arlan Si");
        subcontractorList.add(contract1);

        Map<String, String> contract2 = new HashMap<>();
        contract2.put("Contractor number", "103124");
        contract2.put("Contract date", "17.07.2023");
        contract2.put("Contractor name", "TOO Inter Service");
        subcontractorList.add(contract2);

        Map<String, String> contract3 = new HashMap<>();
        contract3.put("Contractor number", "103291");
        contract3.put("Contract date", "17.07.2023");
        contract3.put("Contractor name", "Логиком");
        contract3.put("Oblast", "Атырауская область");
        subcontractorList.add(contract3);

        Map<String, String> contract4 = new HashMap<>();
        contract4.put("Contractor number", "103122");
        contract4.put("Contract date", "17.07.2023");
        contract4.put("Contractor name", "Логиком");
        contract4.put("Oblast", "Астана (г.а.)");
        subcontractorList.add(contract4);

        Map<String, String> contract5 = new HashMap<>();
        contract5.put("Contractor number", "103122");
        contract5.put("Contract date", "17.07.2023");
        contract5.put("Contractor name", "Логиком");
        contract5.put("Oblast", "Акмолинская область");
        subcontractorList.add(contract5);

        Map<String, String> contract6 = new HashMap<>();
        contract6.put("Contractor number", "103295");
        contract6.put("Contract date", "17.07.2023");
        contract6.put("Contractor name", "Forester-Hes Group");
        subcontractorList.add(contract6);

        Map<String,String> subcontractorInfo = subcontractorList.stream()
            .filter(map -> map.get("Contractor name").equals(subcontractor))
            .filter(map -> map.get("Oblast") == null || map.get("Oblast").equals(oblastName))
            .findFirst()
            .orElse(null);


        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();

        CellRangeAddress cellRangeAddress;
        Map<String, Object> properties = new HashMap<>();

        XSSFFont timesNewRoman11 = workbook.createFont();
        timesNewRoman11.setFontName(HSSFFont.FONT_ARIAL);
        timesNewRoman11.setFontHeightInPoints((short)11);
        timesNewRoman11.setBold(true);
        timesNewRoman11.setItalic(true);

        XSSFFont arialBI9 = workbook.createFont();
        arialBI9.setFontName(HSSFFont.FONT_ARIAL);
        arialBI9.setFontHeightInPoints((short) 9);
        arialBI9.setItalic(true);
        arialBI9.setBold(true);

        XSSFFont arialB10 = workbook.createFont();
        arialB10.setFontName(HSSFFont.FONT_ARIAL);
        arialB10.setFontHeightInPoints((short) 10);
        arialB10.setBold(true);

        XSSFFont arialB8 = workbook.createFont();
        arialB8.setFontName(HSSFFont.FONT_ARIAL);
        arialB8.setFontHeightInPoints((short) 8);
        arialB8.setBold(true);

        XSSFFont arialI6 = workbook.createFont();
        arialI6.setFontName(HSSFFont.FONT_ARIAL);
        arialI6.setFontHeightInPoints((short) 6);
        arialI6.setItalic(true);

        XSSFFont arial12 = workbook.createFont();
        arial12.setFontName(HSSFFont.FONT_ARIAL);
        arial12.setFontHeightInPoints((short) 12);

        //common properties
        properties.put(CellUtil.ALIGNMENT, HorizontalAlignment.RIGHT);
        properties.put(CellUtil.VERTICAL_ALIGNMENT, VerticalAlignment.CENTER);
        properties.put(CellUtil.WRAP_TEXT, true);

        Row row = sheet.createRow(0);
        row.createCell(0);

        Cell cell = row.createCell(6);
        CellUtil.setFont(cell, timesNewRoman11);
        CellUtil.setCellStyleProperties(cell, properties);

        row = sheet.createRow(1);
        row.setHeight((short)500);
        cell = row.createCell(2);

        String contractorNumber = subcontractorInfo != null ? subcontractorInfo.get("Contractor number") : "";
        String contractDate = subcontractorInfo != null ? subcontractorInfo.get("Contract date") : "";
        String stringBuilder = "Договор подряда №  " + contractorNumber +
            "\n" +
            "от " + contractDate;

        cell.setCellValue(stringBuilder);
        CellUtil.setFont(cell, arialBI9);
        CellUtil.setCellStyleProperties(cell, properties);


        properties.replace(CellUtil.ALIGNMENT, HorizontalAlignment.CENTER);

        row = sheet.createRow(5);
        row.createCell(1).setCellValue("Заказ  №");
        row.createCell(2).setCellValue(jrNumber);
        row.createCell(4).setCellValue("от");
        row.createCell(5).setCellValue(DateFormatUtils.format(requestDate, "dd.MM.yyyy"));

        CellUtil.setFont(row.getCell(1), arial12);
        CellUtil.setFont(row.getCell(2), arial12);
        CellUtil.setFont(row.getCell(4), arial12);
        CellUtil.setFont(row.getCell(5), arial12);

        sheet.createRow(7);

        row = sheet.createRow(8);
        row.createCell(1).setCellValue("Заказчик:");
        row.createCell(4).setCellValue("АО \"Кселл\"");

        row = sheet.createRow(9);
        row.createCell(1).setCellValue("Подрядчик:");
        String subcontractorFullName;
        if (subcontractor.equals("ТОО Аврора Сервис")) {
            subcontractorFullName = "ТОО \"Аврора Сервис\"";
        } else if (subcontractor.equals("Логиком")) {
            subcontractorFullName = "АО \"Логиком\"";
        } else if (subcontractor.equals("Arlan Si")) {
            subcontractorFullName = "АО \"ARLAN SI\"";
        } else if (subcontractor.equals("Forester-Hes Group")) {
            subcontractorFullName = "ТОО \"HES Group\"";
        } else if (subcontractor.equals("Востоктелеком")) {
            subcontractorFullName = "ТОО \"Востоктелеком\"";
        } else if (subcontractor.equals("Antares Engineering")) {
            subcontractorFullName = "ТОО \"Antares Engineering\"";
        } else if (subcontractor.equals("NURTRANSENERGOTELECOM")) {
            subcontractorFullName = "ТОО \"NURTRANSENERGOTELECOM\"";
        } else {
            subcontractorFullName = subcontractor;
        }
        row.createCell(4).setCellValue(subcontractorFullName);

        row = sheet.createRow(10);
        row.createCell(1).setCellValue("Тип Работ:");
        row.createCell(4).setCellValue(reason);
        row.createCell(6);
        row.createCell(7);

        CellUtil.setFont(row.getCell(7), arialB10);

        row = sheet.createRow(11);
        row.createCell(1).setCellValue("Дата начала выполнения работ:");
        row.createCell(4).setCellValue(workStartDate != null ? sdf.format(workStartDate) : "");

        row = sheet.createRow(12);
        row.createCell(1).setCellValue("Дата выполнения Интеграции");
        row.createCell(4).setCellValue(integrationRunDate != null ? sdf.format(integrationRunDate) : "");

        row = sheet.createRow(13);
        row.createCell(1).setCellValue("Дата окончания работ:");
        row.createCell(4).setCellValue(workCompletionDate != null ? sdf.format(workCompletionDate) : "");

        row = sheet.createRow(14);
        row.createCell(1).setCellValue("Критическая просрочка(дней):");

        for(int i = 7; i < 15; i++){
            if (sheet.getRow(i) != null && sheet.getRow(i).getCell(1) != null) {
                CellUtil.setCellStyleProperties(sheet.getRow(i).getCell(1), properties);
                CellUtil.setFont(sheet.getRow(i).getCell(1), arialB10);
            } else {
                row = sheet.createRow(i);
                row.createCell(1);
            }
        }

        properties.put(CellUtil.WRAP_TEXT, true);
        properties.put(CellUtil.BORDER_TOP, BorderStyle.THIN);
        properties.put(CellUtil.BORDER_LEFT, BorderStyle.THIN);
        properties.put(CellUtil.BORDER_RIGHT, BorderStyle.THIN);
        properties.put(CellUtil.BORDER_BOTTOM, BorderStyle.THIN);

        row = sheet.createRow(16);
        row.createCell(0).setCellValue("№");
        row.createCell(1).setCellValue("Имя сайта");
        row.createCell(2).setCellValue("№ статьи по ТЦП");
        row.createCell(3).setCellValue("Наименование");
        row.createCell(4).setCellValue("Ед.изм.");
        row.createCell(5).setCellValue("Кол-во");
        cellRangeAddress = new CellRangeAddress(16, 16, 5, 6);
        sheet.addMergedRegion(cellRangeAddress);
        setRegionBorders(cellRangeAddress, sheet);

        for(int i = 0; i < 6; i++){
            CellUtil.setCellStyleProperties(row.getCell(i), properties);
            CellUtil.setFont(row.getCell(i), arialB8);
        }

        for (int i = 0; i < jobWorks.size(); i++) {
            row = sheet.getRow(17+i) != null ? sheet.getRow(17+i) :sheet.createRow(17 + i);
            row.createCell(0).setCellValue(i+1);

            cell = row.createCell(1);
            StringBuilder siteNames = new StringBuilder();
            ArrayNode sites = (ArrayNode) jobWorks.get(i).get("relatedSites");
            for (int j = 0; j < sites.size(); j++) {
                siteNames.append(sites.get(j).get("site_name").textValue());
                if (j < sites.size()-1)
                    siteNames.append(',');
            }
            cell.setCellValue(siteNames.toString());

            row.createCell(2).setCellValue(jobWorks.get(i).get("displayServiceName").textValue().split(" ")[0]);

            row.createCell(3).setCellValue(jobWorks.get(i).get("displayServiceName").textValue());

            row.createCell(4).setCellValue(jobWorks.get(i).get("materialUnit").textValue());

            row.createCell(5).setCellValue(jobWorks.get(i).get("quantity").asText());
            cellRangeAddress = new CellRangeAddress(17+i, 17+i, 5, 6);
            sheet.addMergedRegion(cellRangeAddress);
            setRegionBorders(cellRangeAddress, sheet);

            CellUtil.setCellStyleProperties(row.getCell(0), properties);
            CellUtil.setCellStyleProperties(row.getCell(1), properties);
            CellUtil.setCellStyleProperties(row.getCell(2), properties);
            CellUtil.setCellStyleProperties(row.getCell(3), properties);
            CellUtil.setCellStyleProperties(row.getCell(4), properties);
            CellUtil.setCellStyleProperties(row.getCell(5), properties);
        }

        row = sheet.getRow(18+jobWorks.size())!=null?sheet.getRow(18+jobWorks.size()):sheet.createRow(18 + jobWorks.size());
        cell = row.createCell(1);
        cell.setCellValue("Описание работ:");
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cell.setCellStyle(cellStyle);

        CellUtil.setFont(cell, arialB10);
        row = sheet.getRow(19+jobWorks.size())!=null?sheet.getRow(19+jobWorks.size()):sheet.createRow(19 + jobWorks.size());
        // explanation of works
        cell = row.getCell(1) != null ? row.getCell(1) : row.createCell(1);
        cell.setCellValue(explanation != null ? explanation : "");
        CellUtil.setCellStyleProperties(cell, properties);
        CellUtil.setAlignment(cell, HorizontalAlignment.LEFT);
        CellUtil.setVerticalAlignment(cell, VerticalAlignment.CENTER);
        cellStyle = workbook.createCellStyle();
        cellStyle.setBorderRight(BorderStyle.THIN);
        row.createCell(0).setCellStyle(cellStyle);


        cellRangeAddress = new CellRangeAddress(19+jobWorks.size(), 27 + jobWorks.size(), 1, 6);
        sheet.addMergedRegion(cellRangeAddress);

        setRegionBorders(cellRangeAddress, sheet);


        cellStyle = workbook.createCellStyle();
        cellStyle.setWrapText(true);
        cell.setCellStyle(cellStyle);
        row.createCell(7);
        row.createCell(8);


        // remove borders from properties
        properties.remove(CellUtil.WRAP_TEXT);
        properties.remove(CellUtil.BORDER_TOP);
        properties.remove(CellUtil.BORDER_LEFT);
        properties.remove(CellUtil.BORDER_RIGHT);
        properties.remove(CellUtil.BORDER_BOTTOM);


        row = sheet.getRow(29+jobWorks.size())!=null?sheet.getRow(29+jobWorks.size()):sheet.createRow(29 + jobWorks.size());
        row.setHeight((short)500);
        row.createCell(1).setCellValue("Инициатор:");
        row.createCell(2).setCellValue(initiatorFull.get("firstName").textValue() + " " + initiatorFull.get("lastName").textValue());
        CellUtil.setFont(row.getCell(1), arialB10);
        CellUtil.setFont(row.getCell(2), arialB10);

        row = sheet.getRow(30+jobWorks.size())!=null?sheet.getRow(30+jobWorks.size()):sheet.createRow(30 + jobWorks.size());
        row.setHeight((short)500);
        row.createCell(1).setCellValue("Подрядчик: _________");
        row.createCell(4).setCellValue("Заказчик: _________");
        CellUtil.setFont(row.getCell(1), arialB10);
        CellUtil.setFont(row.getCell(4), arialB10);

        properties.replace(CellUtil.VERTICAL_ALIGNMENT, VerticalAlignment.TOP);
        row = sheet.getRow(34+jobWorks.size())!=null?sheet.getRow(34+jobWorks.size()):sheet.createRow(34 + jobWorks.size());
        row.createCell(1).setCellValue("(должность)");
        row.createCell(4).setCellValue("(должность)");
        CellUtil.setCellStyleProperties(row.getCell(1), properties);
        CellUtil.setCellStyleProperties(row.getCell(4), properties);
        CellUtil.setFont(row.getCell(1), arialI6);
        CellUtil.setFont(row.getCell(4), arialI6);

        row = sheet.getRow(37+jobWorks.size())!=null?sheet.getRow(37+jobWorks.size()):sheet.createRow(37 + jobWorks.size());
        row.createCell(1).setCellValue("(Ф.И.О., подпись)");
        row.createCell(4).setCellValue("(Ф.И.О., подпись)");
        CellUtil.setCellStyleProperties(sheet.getRow(row.getRowNum()).getCell(1), properties);
        CellUtil.setCellStyleProperties(sheet.getRow(row.getRowNum()).getCell(4), properties);
        CellUtil.setFont(row.getCell(1), arialI6);
        CellUtil.setFont(row.getCell(4), arialI6);

        sheet.addMergedRegion(new CellRangeAddress(1, 1, 2, 5));

        sheet.addMergedRegion(new CellRangeAddress(8, 8, 1, 3));
        sheet.addMergedRegion(new CellRangeAddress(9, 9, 1, 3));
        sheet.addMergedRegion(new CellRangeAddress(10, 10, 1, 3));
        sheet.addMergedRegion(new CellRangeAddress(11, 11, 1, 3));
        sheet.addMergedRegion(new CellRangeAddress(12, 12, 1, 3));
        sheet.addMergedRegion(new CellRangeAddress(13, 13, 1, 3));
        sheet.addMergedRegion(new CellRangeAddress(14, 14, 1, 3));

        sheet.addMergedRegion(new CellRangeAddress(5, 5, 2, 3));
        sheet.addMergedRegion(new CellRangeAddress(5, 5, 5, 6));

        sheet.addMergedRegion(new CellRangeAddress(jobWorks.size() + 30, jobWorks.size() + 30, 1, 2));
        sheet.addMergedRegion(new CellRangeAddress(jobWorks.size() + 30, jobWorks.size() + 30, 4, 5));

        cellRangeAddress = new CellRangeAddress(11, 11, 4, 4);
        RegionUtil.setBorderBottom(BorderStyle.THIN, cellRangeAddress, sheet);
        cellRangeAddress = new CellRangeAddress(12, 12, 4, 4);
        RegionUtil.setBorderBottom(BorderStyle.THIN, cellRangeAddress, sheet);
        cellRangeAddress = new CellRangeAddress(13, 13, 4, 4);
        RegionUtil.setBorderBottom(BorderStyle.THIN, cellRangeAddress, sheet);
        cellRangeAddress = new CellRangeAddress(14, 14, 4, 4);
        RegionUtil.setBorderBottom(BorderStyle.THIN, cellRangeAddress, sheet);

        cellRangeAddress = new CellRangeAddress(jobWorks.size() + 33, jobWorks.size() + 33, 1, 2);
        sheet.addMergedRegion(cellRangeAddress);
        RegionUtil.setBorderBottom(BorderStyle.THIN, cellRangeAddress, sheet);
        cellRangeAddress = new CellRangeAddress(jobWorks.size() + 33, jobWorks.size() + 33, 4, 5);
        sheet.addMergedRegion(cellRangeAddress);
        RegionUtil.setBorderBottom(BorderStyle.THIN, cellRangeAddress, sheet);

        cellRangeAddress = new CellRangeAddress(jobWorks.size() + 34, jobWorks.size() + 34, 1, 2);
        sheet.addMergedRegion(cellRangeAddress);
        cellRangeAddress = new CellRangeAddress(jobWorks.size() + 34, jobWorks.size() + 34, 4, 5);
        sheet.addMergedRegion(cellRangeAddress);

        cellRangeAddress = new CellRangeAddress(jobWorks.size() + 35, jobWorks.size() + 35, 1, 2);
        sheet.addMergedRegion(cellRangeAddress);
        cellRangeAddress = new CellRangeAddress(jobWorks.size() + 35, jobWorks.size() + 35, 4, 5);
        sheet.addMergedRegion(cellRangeAddress);

        cellRangeAddress = new CellRangeAddress(jobWorks.size() + 36, jobWorks.size() + 36, 1, 2);
        sheet.addMergedRegion(cellRangeAddress);
        RegionUtil.setBorderTop(BorderStyle.THIN, cellRangeAddress, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN, cellRangeAddress, sheet);
        cellRangeAddress = new CellRangeAddress(jobWorks.size() + 36, jobWorks.size() + 36, 4, 5);
        sheet.addMergedRegion(cellRangeAddress);
        RegionUtil.setBorderTop(BorderStyle.THIN, cellRangeAddress, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN, cellRangeAddress, sheet);

        cellRangeAddress = new CellRangeAddress(jobWorks.size() + 37, jobWorks.size() + 37, 1, 2);
        sheet.addMergedRegion(cellRangeAddress);
        cellRangeAddress = new CellRangeAddress(jobWorks.size() + 37, jobWorks.size() + 37, 4, 5);
        sheet.addMergedRegion(cellRangeAddress);


        sheet.setColumnWidth(0, 900);
        sheet.setColumnWidth(1,7000);
        sheet.setColumnWidth(2, 3000);
        sheet.setColumnWidth(3, 11000);
        sheet.setColumnWidth(4, 5000);
        sheet.setColumnWidth(5, 2000);
        sheet.setColumnWidth(6, 4000);
        sheet.setColumnWidth(7, 8000);
        sheet.setColumnWidth(8, 3000);
        sheet.setColumnWidth(11, 8000);
        sheet.setColumnWidth(12, 3000);
        sheet.setColumnWidth(13, 12000);
        sheet.setColumnWidth(14, 5000);
        sheet.setColumnWidth(15, 4000);
        sheet.setColumnWidth(16, 4000);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        out.close();
        return out.toByteArray();
    }

    private void setRegionBorders(CellRangeAddress cellRangeAddress, Sheet sheet) {
        RegionUtil.setBorderTop(BorderStyle.THIN, cellRangeAddress, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN, cellRangeAddress, sheet);
        RegionUtil.setBorderLeft(BorderStyle.THIN, cellRangeAddress, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN, cellRangeAddress, sheet);
    }
}
