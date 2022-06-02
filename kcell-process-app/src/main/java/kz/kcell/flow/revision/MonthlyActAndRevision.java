package kz.kcell.flow.revision;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.*;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/monthlyactandrevision")
public class MonthlyActAndRevision {

    @RequestMapping(value = "/generateFin", method = RequestMethod.POST, consumes = {"application/json"})
    @ResponseBody
    public ResponseEntity<byte[]> generateFin(@RequestBody String jsonBody) throws Exception {
        return generateDocxFile(jsonBody, true);
    }

    @RequestMapping(value = "/generateNotFin", method = RequestMethod.POST, consumes = {"application/json"})
    @ResponseBody
    public ResponseEntity<byte[]> generateNotFin(@RequestBody String jsonBody) throws Exception {
        return generateDocxFile(jsonBody, false);
    }

    private final static Map<String, String> monthsTitleRu;
    static
    {
        monthsTitleRu = new HashMap<String, String>();
        monthsTitleRu.put("January", "Январь");
        monthsTitleRu.put("February", "Февраль");
        monthsTitleRu.put("March", "Март");
        monthsTitleRu.put("April", "Апрель");
        monthsTitleRu.put("May", "Май");
        monthsTitleRu.put("June", "Июнь");
        monthsTitleRu.put("July", "Июль");
        monthsTitleRu.put("August", "Август");
        monthsTitleRu.put("September", "Сентябрь");
        monthsTitleRu.put("October", "Октябрь");
        monthsTitleRu.put("November", "Ноябрь");
        monthsTitleRu.put("December", "Декабрь");
    }

    private final static Map<String, String> regionsMap;
    static
    {
        regionsMap = new HashMap<String, String>();
        regionsMap.put("alm", "Almaty");
        regionsMap.put("astana", "Astana");
        regionsMap.put("nc", "N&C");
        regionsMap.put("east", "East");
        regionsMap.put("south", "South");
        regionsMap.put("west", "West");
    }

    private final static Map<String, String> mapSubcontractorsToNum;
    static
    {
        mapSubcontractorsToNum = new HashMap<String, String>();
        mapSubcontractorsToNum.put("ALTA", "6");
        mapSubcontractorsToNum.put("ARLAN", "8");
        mapSubcontractorsToNum.put("LOGYCOM", "7");
        mapSubcontractorsToNum.put("LINE", "4");
        mapSubcontractorsToNum.put("Inter", "9");
    }

    private final static Map<String, String> mapSubcontractorsContract;
    static
    {
        mapSubcontractorsContract = new HashMap<String, String>();
        mapSubcontractorsContract.put("ALTA", "№87649 от 01.10.2020 г");
        mapSubcontractorsContract.put("ARLAN", "№ 88323 от 27.11.2020 г");
        mapSubcontractorsContract.put("LOGYCOM", "№ 88327 от 20.11.2020 г");
        mapSubcontractorsContract.put("LINE", "№87073 от 19.08.2020 г");
    }

    private final static Map<String, String> contractorsTitle;
    static
    {
        contractorsTitle = new HashMap<String, String>();
        contractorsTitle.put("-1", "Subcontractor");
        contractorsTitle.put("1", "ТОО Аврора Сервис");
        contractorsTitle.put("2", "ТОО AICOM");
        contractorsTitle.put("3", "ТОО Spectr energy group");
        contractorsTitle.put("4", "ТОО «Line System Engineering»");
        contractorsTitle.put("5", "Kcell_region");
        contractorsTitle.put("6", "ТОО «ALTA Telecom (АЛТА Телеком)»\n");
        contractorsTitle.put("7", "АО «Логиком»");
        contractorsTitle.put("8", "ТОО «ARLAN SI»");
        contractorsTitle.put("9", "ТОО «Inter Service»");
    }

    private final static Map<String, String> reasonsTitle;
    static
    {
        reasonsTitle = new HashMap<String, String>();
        reasonsTitle.put("1", "Optimization works");
        reasonsTitle.put("2", "Transmission works");
        reasonsTitle.put("3", "Infrastructure works");
        reasonsTitle.put("4", "Operation works");
        reasonsTitle.put("5", "Roll-out works");
    }

    public ResponseEntity<byte[]> generateDocxFile(String jsonBody, Boolean isFin) throws Exception{

        JSONArray regularWorks = new JSONArray();
        JSONArray emergencyWorks = new JSONArray();
        JSONObject rootObject = new JSONObject(jsonBody);
        JSONObject mainObject = rootObject.getJSONObject("selectedRevisions");
        String subcontractor = rootObject.getString("subcontractor");
        String businessKey = rootObject.getString("businessKey");
        String monthOfFormalPeriod = rootObject.getString("monthOfFormalPeriod");
        String yearOfFormalPeriod = rootObject.getString("yearOfFormalPeriod");
        Iterator keys = mainObject.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            JSONObject object = mainObject.getJSONObject(key);
            if (object.getString("priority").equals("emergency")) {
                emergencyWorks.put(object);
            } else {
                regularWorks.put(object);
            }
        }
        SimpleDateFormat regularfd = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat emergencyfd = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX");

        computeInformation(regularWorks, regularfd, format);
        computeInformation(emergencyWorks, emergencyfd, format);

        XWPFDocument document = new XWPFDocument();

        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();

        CTSectPr ctSectPr = document.getDocument().getBody().addNewSectPr();
        XWPFHeaderFooterPolicy headerFooterPolicy = document.getHeaderFooterPolicy();
        if (headerFooterPolicy == null) headerFooterPolicy = document.createHeaderFooterPolicy();

        XWPFHeader header = headerFooterPolicy.createHeader(XWPFHeaderFooterPolicy.DEFAULT);

        paragraph = header.getParagraphArray(0);
        if (paragraph == null) paragraph = header.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.LEFT);

        run = paragraph.createRun();
        run.setText("Номер акта:");
        run.setFontFamily("Calibri");
        run.setFontSize(12);
        run = paragraph.createRun();
        run.setText(businessKey +"\n");
        run.setBold(true);
        run.setFontFamily("Calibri");
        run.setFontSize(12);
        run = paragraph.createRun();
        run.addBreak();
        run.setText("Отчетный период:");
        run.setFontFamily("Calibri");
        run.setFontSize(12);
        run = paragraph.createRun();
        run.setText(monthsTitleRu.get(monthOfFormalPeriod) + " " + yearOfFormalPeriod);
        run.setFontFamily("Calibri");
        run.setFontSize(12);
        run.setBold(true);

        XWPFFooter footer = headerFooterPolicy.createFooter(XWPFHeaderFooterPolicy.DEFAULT);

        paragraph = footer.getParagraphArray(0);
        if (paragraph == null) paragraph = footer.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);

        run = paragraph.createRun();
        run.setText("Page ");
        paragraph.getCTP().addNewFldSimple().setInstr("PAGE \\* MERGEFORMAT");
        run = paragraph.createRun();
        run.setText(" of ");
        paragraph.getCTP().addNewFldSimple().setInstr("NUMPAGES \\* MERGEFORMAT");

        CTDocument1 doc = document.getDocument();
        CTBody body = doc.getBody();

        if (!body.isSetSectPr()) {
            body.addNewSectPr();
        }
        CTSectPr section = body.getSectPr();

        if (!section.isSetPgSz()) {
            section.addNewPgSz();
        }
        CTPageSz pageSize = section.getPgSz();

        pageSize.setOrient(STPageOrientation.LANDSCAPE);
        pageSize.setW(BigInteger.valueOf(15840));
        pageSize.setH(BigInteger.valueOf(12240));


        XWPFParagraph title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun titleRun = title.createRun();
        titleRun.setText("Месячный технический акт выполненных работ " + contractorsTitle.get(mapSubcontractorsToNum.get(subcontractor)) +" согласно");
        titleRun.setBold(true);
        titleRun.setFontFamily("Calibri");
        titleRun.setFontSize(12);

        title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        titleRun = title.createRun();
        titleRun.setText("Договора Подряда на Выполнение Работ по Внедрению и Модификации " + mapSubcontractorsContract.get(subcontractor));
        titleRun.setBold(true);
        titleRun.setFontFamily("Calibri");
        titleRun.setFontSize(12);

        title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        titleRun = title.createRun();
        titleRun.setText("Плановые работы");
        titleRun.setBold(true);
        titleRun.setFontFamily("Calibri");
        titleRun.setFontSize(12);


        Double totalRegular = 0.0;
        Double totalEmergency = 0.0;
        XWPFTable table = document.createTable();
        table.setWidth("100%");
        XWPFTableRow row;
        generateHeader(table, paragraph, run, isFin);

        int k = 1;
        JSONArray arr = new JSONArray();

        for (int i = 0; i < regularWorks.length(); i++) {
            JSONObject obj = new JSONObject();
            JSONObject localeObject = regularWorks.getJSONObject(i);
            JSONArray works = localeObject.getJSONArray(isFin ? "workPrices" : "works");
            int firstRow = 0;
            int lastRow = 0;
            Double localeTotalFin = 0.0;
            for (int j = 0; j < works.length(); j++) {
                JSONObject work = works.getJSONObject(j);
                table.createRow();
                row = table.getRow(k);
                if (j == 0) {
                    firstRow = k;
                }

                if (j == works.length() - 1) {
                    lastRow = k;
                }
                k = fillTableData(row, k, localeObject, j, work, paragraph, run, isFin);
                if (isFin) {
                    totalRegular += Double.parseDouble(work.has("total") ? work.getString("total") : "0.0");
                    localeTotalFin += Double.parseDouble(work.has("total") ? work.getString("total") : "0.0");
                }
            }

            if (firstRow != lastRow) {
                obj.put("first", firstRow);
                obj.put("last", lastRow);
                if (isFin) {
                    table.getRow(firstRow).getCell(8);
                    paragraph = table.getRow(firstRow).getCell(8).getParagraphArray(0);
                    List<XWPFRun> runs = paragraph.getRuns();
                    for(int ii = runs.size() - 1; ii > 0; ii--) {
                        paragraph.removeRun(ii);
                    }
                    run = runs.get(0);
                    run.setText(localeTotalFin.toString(), 0);

                }
                arr.put(obj);
            }
        }

        if(isFin) {
            table.createRow();
            row = table.getRow(k);
            XWPFTableCell cell = row.getCell(0);
            paragraph = cell.getParagraphArray(0);
            paragraph.setAlignment(ParagraphAlignment.CENTER);
            run = paragraph.createRun();
            run.setText("Итого");
            run.setFontSize(8);

            mergeCellsHorizontal(table, k, 0, 7);

            cell = row.getCell(8);
            paragraph = cell.getParagraphArray(0);
            run = paragraph.createRun();
            run.setText(totalRegular.toString());
            run.setFontSize(8);
        }

        title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        titleRun = title.createRun();
        titleRun.setText("Внеплановые работы");
        titleRun.setBold(true);
        titleRun.setFontFamily("Calibri");
        titleRun.setFontSize(12);


        XWPFTable table2 = document.createTable();
        table2.setWidth("100%");
        generateHeader(table2, paragraph, run, isFin);

        int l = 1;
        JSONArray arr2 = new JSONArray();
        for (int i = 0; i < emergencyWorks.length(); i++) {
            JSONObject obj2 = new JSONObject();
            JSONObject localeObject = emergencyWorks.getJSONObject(i);
            JSONArray works = localeObject.getJSONArray(isFin ? "workPrices" : "works");
            int firstRow = 0;
            int lastRow = 0;
            Double localeTotal = 0.0;
            for (int j = 0; j < works.length(); j++) {
                if (j == 0) {
                    firstRow = l;
                }

                if (j == works.length() - 1) {
                    lastRow = l;
                }
                JSONObject work = works.getJSONObject(j);
                table2.createRow();
                row = table2.getRow(l);
                l = fillTableData(row, l, localeObject, j, work, paragraph, run, isFin);
                if (isFin) {
                    totalEmergency += Double.parseDouble(work.has("total") ? work.getString("total") : "0.0");
                    localeTotal += Double.parseDouble(work.has("total") ? work.getString("total") : "0.0");
                }
            }
            if (firstRow != lastRow) {
                obj2.put("first", firstRow);
                obj2.put("last", lastRow);
                if (isFin) {
                    table2.getRow(firstRow).getCell(8);
                    paragraph = table2.getRow(firstRow).getCell(8).getParagraphArray(0);
                    List<XWPFRun> runs = paragraph.getRuns();
                    for(int ii = runs.size() - 1; ii > 0; ii--) {
                        paragraph.removeRun(ii);
                    }
                    run = runs.get(0);
                    run.setText(localeTotal.toString(), 0);

                }

                arr2.put(obj2);
            }
        }
        if (isFin) {
            table2.createRow();
            row = table2.getRow(l);
            XWPFTableCell cell2 = row.getCell(0);
            paragraph = cell2.getParagraphArray(0);
            paragraph.setAlignment(ParagraphAlignment.CENTER);
            run = paragraph.createRun();
            run.setText("Итого");
            run.setFontSize(8);

            mergeCellsHorizontal(table2, l, 0, 7);

            cell2 = row.getCell(8);
            paragraph = cell2.getParagraphArray(0);
            run = paragraph.createRun();
            run.setText(totalEmergency.toString());
            run.setFontSize(8);
        }

        MergeRows(table, arr, isFin);
        MergeRows(table2, arr2, isFin);

        title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.LEFT);
        titleRun = title.createRun();
        titleRun.setText("Количество заявок в акте:" + (emergencyWorks.length() + regularWorks.length()));
        titleRun.setItalic(true);
        titleRun.setFontFamily("Calibri");
        titleRun.setFontSize(12);

        title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.LEFT);
        titleRun = title.createRun();
        titleRun.setText("Все копии подписанных заявок должны быть приложены с печатью");
        titleRun.setItalic(true);
        titleRun.setFontFamily("Calibri");
        titleRun.setFontSize(12);

        XWPFTable table3 = document.createTable(16,2);
        int width = table3.getWidth();
        table3.setWidth("100%");
        table3.removeBorders();
        row = table3.getRow(0);
        XWPFTableCell cell3 = row.getCell(0);
        XWPFParagraph para = cell3.getParagraphArray(0);
        XWPFRun run3;
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.SINGLE);

        cell3 = row.getCell(1);
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.SINGLE);

        row = table3.getRow(1);
        cell3 = row.getCell(0);
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.NONE);
        run3 = para.createRun();
        run3.setFontFamily("Calibri");
        run3.setFontSize(12);
        run3.setText("Представитель " + contractorsTitle.get(mapSubcontractorsToNum.get(subcontractor)));

        cell3 = row.getCell(1);
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.NONE);
        run3 = para.createRun();
        run3.setFontFamily("Calibri");
        run3.setFontSize(12);
        run3.setText("Подпись");

        row = table3.getRow(2);
        cell3 = row.getCell(0);
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.NONE);
        cell3 = row.getCell(1);
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.NONE);


        row = table3.getRow(3);
        cell3 = row.getCell(0);
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.NONE);
        para.setAlignment(ParagraphAlignment.CENTER);
        run3 = para.createRun();
        run3.setText("АО «Кселл» ");
        setFont(run3, true);
        cell3 = row.getCell(1);
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.NONE);


        row = table3.getRow(4);
        cell3 = row.getCell(0);
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.SINGLE);
        run3 = para.createRun();
        setFont(run3, false);
        run3.setText("Ким Александр");
        cell3 = row.getCell(1);
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.SINGLE);
        run3 = para.createRun();
        run3.setFontFamily("Calibri");
        run3.setText("");
        run3.setFontSize(12);

        row = table3.getRow(5);
        cell3 = row.getCell(0);
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.NONE);
        run3 = para.createRun();
        run3.setText("Начальник Сектора Строительства радио и транспортной сети");
        setFont(run3, false);

        cell3 = row.getCell(1);
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.NONE);
        run3 = para.createRun();
        run3.setText("Подпись");
        setFont(run3, false);

        row = table3.getRow(6);
        cell3 = row.getCell(0);
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.SINGLE);
        run3 = para.createRun();
        setFont(run3, false);
        run3.setText("Парамонова Марина");
        cell3 = row.getCell(1);
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.SINGLE);
        run3 = para.createRun();
        run3.setFontFamily("Calibri");
        run3.setText("");
        run3.setFontSize(12);

        row = table3.getRow(7);
        cell3 = row.getCell(0);
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.NONE);
        run3 = para.createRun();
        run3.setText("Начальник сектора по планированию и оптимизации радиосети");
        setFont(run3, false);

        cell3 = row.getCell(1);
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.NONE);
        run3 = para.createRun();
        run3.setText("Подпись");
        setFont(run3, false);

        row = table3.getRow(8);
        cell3 = row.getCell(0);
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.SINGLE);
        run3 = para.createRun();
        setFont(run3, false);
        run3.setText("Александр Галат");
        cell3 = row.getCell(1);
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.SINGLE);
        run3 = para.createRun();
        run3.setFontFamily("Calibri");
        run3.setText("");
        run3.setFontSize(12);

        row = table3.getRow(9);
        cell3 = row.getCell(0);
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.NONE);
        run3 = para.createRun();
        run3.setText("Начальник сектора планирования и оптимизации транспортной сети");
        setFont(run3, false);

        cell3 = row.getCell(1);
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.NONE);
        run3 = para.createRun();
        run3.setText("Подпись");
        setFont(run3, false);

        row = table3.getRow(10);
        cell3 = row.getCell(0);
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.SINGLE);
        run3 = para.createRun();
        setFont(run3, false);
        run3.setText("Азамат Галиулла");
        cell3 = row.getCell(1);
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.SINGLE);
        run3 = para.createRun();
        run3.setFontFamily("Calibri");
        run3.setText("");
        run3.setFontSize(12);

        row = table3.getRow(11);
        cell3 = row.getCell(0);
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.NONE);
        run3 = para.createRun();
        run3.setText("Начальник сектора эксплуатации сети_Центр");
        setFont(run3, false);

        cell3 = row.getCell(1);
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.NONE);
        run3 = para.createRun();
        run3.setText("Подпись");
        setFont(run3, false);

        row = table3.getRow(12);
        cell3 = row.getCell(0);
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.SINGLE);
        run3 = para.createRun();
        run3.setText("Страшенко Кирилл");
        setFont(run3, false);
        cell3 = row.getCell(1);
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.SINGLE);

        row = table3.getRow(13);
        cell3 = row.getCell(0);
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.NONE);
        run3 = para.createRun();
        run3.setText("Менеджер отдела развития сети");
        setFont(run3, false);

        cell3 = row.getCell(1);
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.NONE);
        run3 = para.createRun();
        run3.setText("Подпись");
        setFont(run3, false);

        //

        row = table3.getRow(14);
        cell3 = row.getCell(0);
        cell3.setWidth(String.valueOf(width / 2));
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.SINGLE);
        run3 = para.createRun();
        run3.setText("Есеркегенов Аскар");
        setFont(run3, false);
        cell3 = row.getCell(1);
        cell3.setWidth(String.valueOf(width / 2));
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.SINGLE);
        run3 = para.createRun();
        setFont(run3, false);

        row = table3.getRow(15);
        cell3 = row.getCell(0);
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.NONE);
        run3 = para.createRun();
        run3.setText("Главный Технический Директор, Член Правления АО \"Кселл\"");
        setFont(run3, false);

        cell3 = row.getCell(1);
        para = cell3.getParagraphArray(0);
        para.setBorderBetween(Borders.NONE);
        para.setBorderLeft(Borders.NONE);
        para.setBorderRight(Borders.NONE);
        para.setBorderTop(Borders.NONE);
        para.setBorderBottom(Borders.NONE);
        run3 = para.createRun();
        run3.setText("Подпись");
        setFont(run3, false);

        //
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.write(out);
        out.close();
        document.close();

        byte[] xwpfDocumentBytes = out.toByteArray();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment;filename=hero.docx");
        headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        return ResponseEntity.ok()
            .headers(headers)
//            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(xwpfDocumentBytes);

    }

    private void setFont(XWPFRun run, Boolean bold) {
        run.setFontSize(12);
        run.setFontFamily("Calibri");
        run.setText("");
        run.setBold(bold);
    }
    private void computeInformation(JSONArray emergencyWorks, SimpleDateFormat emergencyfd, SimpleDateFormat format) throws ParseException {
        for (int i = 0; i < emergencyWorks.length(); i++) {
            emergencyWorks.getJSONObject(i).put("contractorJobAssignedDateString", emergencyfd.format(format.parse(emergencyWorks.getJSONObject(i).getString("contractorJobAssignedDate"))));
            emergencyWorks.getJSONObject(i).put("performDateString", emergencyfd.format(format.parse(emergencyWorks.getJSONObject(i).getString("performDate"))));
            emergencyWorks.getJSONObject(i).put("dueDateString", emergencyfd.format(format.parse(emergencyWorks.getJSONObject(i).getString("dueDate"))));
        }
    }

    private int fillTableData(XWPFTableRow row, int l, JSONObject localeObject, int j, JSONObject work, XWPFParagraph paragraph, XWPFRun run, Boolean isFin) {
        if (j == 0) {
            paragraph = row.getCell(0).getParagraphArray(0);
            run = paragraph.createRun();
            run.setText(localeObject.getString("businessKey"));
            run.setFontSize(8);
            paragraph = row.getCell(1).getParagraphArray(0);
            run = paragraph.createRun();
            run.setText(localeObject.getString("site_name"));
            run.setFontSize(8);
            paragraph = row.getCell(2).getParagraphArray(0);
            run = paragraph.createRun();
            run.setText(regionsMap.get(localeObject.getString("siteRegion")));
            run.setFontSize(8);
            paragraph = row.getCell(3).getParagraphArray(0);
            run = paragraph.createRun();
            run.setText(reasonsTitle.get(localeObject.getString("reason")));
            run.setFontSize(8);
            paragraph = row.getCell(4).getParagraphArray(0);
            run = paragraph.createRun();
            run.setText(work.getString("displayServiceName"));
            run.setFontSize(8);
            paragraph = row.getCell(5).getParagraphArray(0);
            run = paragraph.createRun();
            run.setText(work.getString("quantity"));
            run.setFontSize(8);
            paragraph = row.getCell(6).getParagraphArray(0);
            run = paragraph.createRun();
            run.setText(isFin ? work.getString("unitWorkPrice") : localeObject.getString("contractorJobAssignedDateString"));
            run.setFontSize(8);
            paragraph = row.getCell(7).getParagraphArray(0);
            run = paragraph.createRun();
            run.setText(isFin ? work.getString("basePriceByQuantity") : localeObject.getString("performDateString"));
            run.setFontSize(8);
            paragraph = row.getCell(8).getParagraphArray(0);
            run = paragraph.createRun();
            run.setText(isFin ? work.getString("total") : localeObject.getString("dueDateString"));
            run.setFontSize(8);
            if (!isFin) {
                paragraph = row.getCell(9).getParagraphArray(0);
                run = paragraph.createRun();
                run.setText(localeObject.getString("delay"));
                run.setFontSize(8);
            }
        } else {
            if (isFin) {
                paragraph = row.getCell(6).getParagraphArray(0);
                run = paragraph.createRun();
                run.setText(isFin ? work.getString("unitWorkPrice") : localeObject.getString("contractorJobAssignedDateString"));
                run.setFontSize(8);
                paragraph = row.getCell(7).getParagraphArray(0);
                run = paragraph.createRun();
                run.setText(isFin ? work.getString("basePriceByQuantity") : localeObject.getString("performDateString"));
                run.setFontSize(8);
            }
            paragraph = row.getCell(4).getParagraphArray(0);
            run = paragraph.createRun();
            run.setText(work.getString("displayServiceName"));
            run.setFontSize(8);
            paragraph = row.getCell(5).getParagraphArray(0);
            run = paragraph.createRun();
            run.setText(work.getString("quantity"));
            run.setFontSize(8);
        }

        l++;
        return l;
    }

    private void MergeRows(XWPFTable table, JSONArray arr, Boolean isFin) {
        for (int i = 0; i < arr.length(); i++) {
            JSONObject jsonObject = arr.getJSONObject(i);
            mergeCellsVertically(table, 0, jsonObject.getInt("first"), jsonObject.getInt("last"));
            mergeCellsVertically(table, 1, jsonObject.getInt("first"), jsonObject.getInt("last"));
            mergeCellsVertically(table, 2, jsonObject.getInt("first"), jsonObject.getInt("last"));
            mergeCellsVertically(table, 3, jsonObject.getInt("first"), jsonObject.getInt("last"));
            mergeCellsVertically(table,  8, jsonObject.getInt("first"), jsonObject.getInt("last"));
            if (!isFin) {
                mergeCellsVertically(table, 6, jsonObject.getInt("first"), jsonObject.getInt("last"));
                mergeCellsVertically(table, 7, jsonObject.getInt("first"), jsonObject.getInt("last"));
                mergeCellsVertically(table, 9, jsonObject.getInt("first"), jsonObject.getInt("last"));
            }

        }
    }

    private static void mergeCellsVertically(XWPFTable table, int col, int fromRow, int toRow) {

        for (int rowIndex = fromRow; rowIndex <= toRow; rowIndex++) {

            XWPFTableCell cell = table.getRow(rowIndex).getCell(col);
            cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);

            if (rowIndex == fromRow) {
                // The first merged cell is set with RESTART merge value
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.RESTART);
            } else {
                // Cells which join (merge) the first one, are set with CONTINUE
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.CONTINUE);
            }
        }
    }

    private static void generateHeader(XWPFTable table, XWPFParagraph paragraph, XWPFRun run, Boolean isFin) {
        XWPFTableRow row = table.getRow(0);
        row.getCell(0).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(6000));
        paragraph = row.getCell(0).getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        run = paragraph.createRun();
        run.setText("Заявка на выполнение работ №");
        run.setFontFamily("Calibri");
        run.setFontSize(8);
        run.setBold(true);
        row.createCell();
        row.getCell(1).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(5000));
        paragraph = row.getCell(1).getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        run = paragraph.createRun();
        run.setText("Сайт");
        run.setFontFamily("Calibri");
        run.setFontSize(8);
        run.setBold(true);
        row.createCell();
        row.getCell(2).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(1000));
        paragraph = row.getCell(2).getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        run = paragraph.createRun();
        run.setText("Регион");
        run.setFontFamily("Calibri");
        run.setFontSize(8);
        run.setBold(true);
        row.createCell();
        row.getCell(3).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(1000));
        paragraph = row.getCell(3).getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        run = paragraph.createRun();
        run.setText("Тип работы");
        run.setFontFamily("Calibri");
        run.setFontSize(8);
        run.setBold(true);
        row.createCell();
        row.getCell(4).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(7000));
        paragraph = row.getCell(4).getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        run = paragraph.createRun();
        run.setText("Наименование Работ");
        run.setFontFamily("Calibri");
        run.setFontSize(8);
        run.setBold(true);
        row.createCell();
        row.getCell(5).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(500));
        paragraph = row.getCell(5).getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        run = paragraph.createRun();
        run.setText("Кол-во работ");
        run.setFontFamily("Calibri");
        run.setFontSize(8);
        run.setBold(true);
        row.createCell();
        row.getCell(6).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(1000));
        paragraph = row.getCell(6).getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        run = paragraph.createRun();
        run.setText(isFin ? "Цена" : "Дата заказа");
        run.setFontFamily("Calibri");
        run.setFontSize(8);
        run.setBold(true);
        row.createCell();
        row.getCell(7).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(1000));
        paragraph = row.getCell(7).getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        run = paragraph.createRun();
        run.setText(isFin ? "Стоимость": "Дата выполнения");
        run.setFontFamily("Calibri");
        run.setFontSize(8);
        run.setBold(true);
        row.createCell();
        row.getCell(8).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(2000));
        paragraph = row.getCell(8).getParagraphArray(0);
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        run = paragraph.createRun();
        run.setText(isFin ? "Стоимость заявки" : "Согласованный срок выполнения работ (Дата)");
        run.setFontFamily("Calibri");
        run.setFontSize(8);
        run.setBold(true);
        if (!isFin) {
            row.createCell();
            row.getCell(9).getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(3000));
            paragraph = row.getCell(9).getParagraphArray(0);
            paragraph.setAlignment(ParagraphAlignment.CENTER);
            run = paragraph.createRun();
            run.setText("Задержка, дней");
            run.setFontFamily("Calibri");
            run.setFontSize(8);
            run.setBold(true);
        }
    }

    public static void mergeCellsHorizontal(XWPFTable table, int row, int fromCell, int toCell) {
        for (int cellIndex = fromCell; cellIndex <= toCell; cellIndex++) {
            XWPFTableCell cell = table.getRow(row).getCell(cellIndex);
            if ( cellIndex == fromCell ) {
                // The first merged cell is set with RESTART merge value
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
            } else {
                // Cells which join (merge) the first one, are set with CONTINUE
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
            }
        }
    }
}
