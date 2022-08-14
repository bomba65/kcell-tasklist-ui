package kz.kcell.flow.fixedInternet;

import lombok.extern.java.Log;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

@Log
@RestController
@RequestMapping("/fixed-internet")
public class FixedInternetController {

    @Autowired
    private TaskService taskService;

    @RequestMapping(value = "/{taskId}/{term}/calculate-kp", method = RequestMethod.GET)
    @ResponseBody
    public void calculateKP(@PathVariable("taskId") String taskId,@PathVariable("term") String term, HttpServletResponse response) throws Exception {
        Map<String, Object> variables = taskService.getVariables(taskId);
        variables.entrySet().forEach(p -> System.out.println(p.getKey() + ":" + p.getValue()));
        JSONArray timePayments = new JSONArray(variables.get("timePayments").toString());
        JSONArray monthPayments = new JSONArray(variables.get("monthPayments").toString());
        String total_nds = variables.get("total_nds").toString();
        String total_amount = variables.get("total_amount").toString();
        String total_amount_month = variables.get("total_amount_month").toString();
        String total_nds_month = variables.get("total_nds_month").toString();
        String lastMileWeeks = variables.get("lastMileWeeks").toString();

        XWPFDocument doc = new XWPFDocument(FixedInternetController.class.getResourceAsStream("/fixedInternet/ggg.docx"));
        XWPFParagraph paragraph = doc.createParagraph();
        XWPFRun title = paragraph.createRun();
        XWPFTable table = doc.createTable();
        table.setWidth("100%");

        title.setText("Единовременные платежи:");

        for (int i=0; i < timePayments.length(); i++) {
            JSONObject item = timePayments.getJSONObject(i);
            if(i == 0){
                XWPFTableRow tableRow = table.getRow(0);
                tableRow.getCell(0).setText("№");
                tableRow.addNewTableCell().setText("Описание");
                tableRow.addNewTableCell().setText("Кол-во");
                tableRow.addNewTableCell().setText("Цена за ед., тенге с НДС");
                tableRow.addNewTableCell().setText("Сумма, в тенге с НДС");
                tableRow.addNewTableCell().setText("В том числе НДС 12%, тенге");
                tableRow.addNewTableCell().setText("Адрес точки подключения");
            }
            XWPFTableRow tableRow = table.createRow();
            tableRow.getCell(0).setText(String.valueOf((i+1)));
            tableRow.getCell(1).setText(item.get("spec_ont").toString());
            tableRow.getCell(2).setText(item.get("qpay").toString());
            tableRow.getCell(3).setText(item.get("price").toString());
            tableRow.getCell(4).setText(item.get("amount").toString());
            tableRow.getCell(5).setText(item.get("nds").toString());
            tableRow.getCell(6).setText(item.get("addres").toString());
        }

        XWPFTableRow tableRow = table.createRow();
        tableRow.getCell(0).setText("");
        tableRow.getCell(1).setText("Итого");
        tableRow.getCell(2).setText("");
        tableRow.getCell(3).setText("");
        tableRow.getCell(4).setText(total_amount);
        tableRow.getCell(5).setText(total_nds);
        tableRow.getCell(6).setText("");

        paragraph = doc.createParagraph();
        XWPFRun title_month = paragraph.createRun();
        title_month.setText("Ежемесячные платежи:");
        XWPFTable table_month = doc.createTable();
        table_month.setWidth("100%");

        for (int i=0; i < monthPayments.length(); i++) {
            JSONObject item = monthPayments.getJSONObject(i);
            if(i == 0){
                XWPFTableRow tableRow_month = table_month.getRow(0);
                tableRow_month.getCell(0).setText("№");
                tableRow_month.addNewTableCell().setText("Описание");
                tableRow_month.addNewTableCell().setText("Кол-во");
                tableRow_month.addNewTableCell().setText("Цена за ед., тенге с НДС");
                tableRow_month.addNewTableCell().setText("Сумма, в тенге с НДС");
                tableRow_month.addNewTableCell().setText("В том числе НДС 12%, тенге");
                tableRow_month.addNewTableCell().setText("Адрес точки подключения");
            }
            XWPFTableRow tableRow_month = table_month.createRow();
            tableRow_month.getCell(0).setText(String.valueOf((i+1)));
            tableRow_month.getCell(1).setText(item.get("spec_month").toString());
            tableRow_month.getCell(2).setText(item.get("qpay_month").toString());
            tableRow_month.getCell(3).setText(item.get("price_month").toString());
            tableRow_month.getCell(4).setText(item.get("amount_month").toString());
            tableRow_month.getCell(5).setText(item.get("nds_month").toString());
            tableRow_month.getCell(6).setText(item.get("addres_month").toString());
        }

        XWPFTableRow tableRow_month = table_month.createRow();
        tableRow_month.getCell(0).setText("");
        tableRow_month.getCell(1).setText("Итого");
        tableRow_month.getCell(2).setText("");
        tableRow_month.getCell(3).setText("");
        tableRow_month.getCell(4).setText(total_amount_month);
        tableRow_month.getCell(5).setText(total_nds_month);
        tableRow_month.getCell(6).setText("");

        paragraph = doc.createParagraph();
        XWPFRun footer = paragraph.createRun();
        footer.setBold(true);
        footer.setText("Срок контракта - " + term + " мес.");

        paragraph = doc.createParagraph();
        footer = paragraph.createRun();
        footer.setBold(true);
        footer.setText("Срок подключения Услуги Клиенту со дня подписания Сторонами Договора - 4 нед.");

        paragraph = doc.createParagraph();
        footer = paragraph.createRun();
        footer.setBold(true);
        footer.setText("Примечание:");

        paragraph = doc.createParagraph();
        footer = paragraph.createRun();
        footer.setText("Тарифы на Услуги определены в национальной валюте Республики Казахстан.");

        paragraph = doc.createParagraph();
        footer = paragraph.createRun();
        footer.setText("В случае прекращения действия Договора Клиент утрачивает права на использование IP-адресов, предоставляемых Оператором.");

        ByteArrayOutputStream downloadFile = new ByteArrayOutputStream();
        doc.write(downloadFile);
        doc.close();
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Calculate_KP.docx\"");
        IOUtils.copy(new ByteArrayInputStream(downloadFile.toByteArray()), response.getOutputStream());
    }
}
