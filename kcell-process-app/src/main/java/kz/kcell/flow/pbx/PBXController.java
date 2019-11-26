package kz.kcell.flow.pbx;

import lombok.extern.java.Log;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log
@RestController
@RequestMapping("/pbx")
public class PBXController {

    @Autowired
    private TaskService taskService;

    @RequestMapping(value = "/{taskId}/generate-technical-condition", method = RequestMethod.GET)
    @ResponseBody
    public void technicalCondition(@PathVariable("taskId") String taskId, HttpServletResponse response) throws Exception {

        Map<String, Object> variables = taskService.getVariables(taskId);

        XWPFDocument doc = new XWPFDocument(PBXController.class.getResourceAsStream("/PBX/technical_conditions_template.docx"));

        JSONObject customerInformation = new JSONObject(variables.get("customerInformation").toString());
        JSONObject technicalSpecifications = new JSONObject(variables.get("technicalSpecifications").toString());
        JSONObject sipProtocol = new JSONObject(variables.get("sipProtocol").toString());

        replaceText(doc, "legalName", customerInformation.getString("legalName"));
        replaceText(doc, "cityCompany", customerInformation.getString("companyRegistrationCity"));

        if (sipProtocol.getString("authorizationType").startsWith("SIP-авторизация")) {
            replaceText(doc, "connectionLevel", "Внутрисетевой, SIP Proxy");
            replaceText(doc, "sipLevel1", "SIP Proxy");
        } else if (sipProtocol.getString("authorizationType").startsWith("SIP-транк")) {
            replaceText(doc, "connectionLevel", "Внутрисетевой, SBC");
            replaceText(doc, "sipLevel1", "SBC");
        } else {
            replaceText(doc, "sipLevel1", "");
        }

        String numbers = technicalSpecifications.getString("pbxNumbers");
        numbers = numbers.replaceAll("\n", ",").replaceAll("\r", ",").replaceAll(",,", ",");
        String[] split = numbers.split(",");
        String pbxNumbers[] = new String[split.length];
        for (int i = 0; i < split.length; i++) {
            pbxNumbers[i] = "+" + split[i].charAt(0) + "-" + split[i].charAt(1) + split[i].charAt(2) + split[i].charAt(3) + "-" + split[i].charAt(4) + split[i].charAt(5) + split[i].charAt(6) + "-" + split[i].charAt(7) + split[i].charAt(8) + "-" + split[i].charAt(9) + split[i].charAt(10);
        }
        replaceText(doc, "virtualNumbers", Arrays.asList(pbxNumbers).stream().collect(Collectors.joining(", ")));
        if (technicalSpecifications.getInt("virtualNumbersCount") == 1) {
            replaceText(doc, "pbxQuantity", technicalSpecifications.getString("virtualNumbersCount") + " номер");
        } else if (technicalSpecifications.getInt("virtualNumbersCount") <= 4) {
            replaceText(doc, "pbxQuantity", technicalSpecifications.getString("virtualNumbersCount") + " номер");
        } else {
            replaceText(doc, "pbxQuantity", technicalSpecifications.getString("virtualNumbersCount") + " номеров");
        }
        replaceText(doc, "connectedTypeEquipment", technicalSpecifications.getString("pbxType"));
        replaceText(doc, "ipAddress", Arrays.asList((sipProtocol.getString("ipVoiceTraffic") + ", " + sipProtocol.getString("ipSignaling")).replaceAll(" ", "").split(",")).stream().collect(Collectors.toSet()).stream().collect(Collectors.joining(", ")));
        replaceText(doc, "codec", sipProtocol.getString("preferredCoding"));
        replaceText(doc, "udpPort", sipProtocol.getString("signalingPort"));
        replaceText(doc, "rtpPort", sipProtocol.getString("voiceTrafficPortStart") + " - " + sipProtocol.getString("voiceTrafficPortEnd"));
        replaceText(doc, "sessionNumber", sipProtocol.getString("sessionCount"));
        replaceText(doc, "internationalCall", technicalSpecifications.getString("intenationalCallAccess").equals("Yes") ? "открыт" : "закрыт");

        BufferedImage image = ImageIO.read(PBXController.class.getResourceAsStream("/PBX/tcSchema.png"));

        Graphics g = image.getGraphics();
        g.setFont(g.getFont().deriveFont(22f));
        Rectangle bounds = new Rectangle(460, 0, 320, 230);
        TextRenderer.drawString(g,
            technicalSpecifications.getString("connectionPoint") + "\n" +
                "Kcell\n" +
                "г. Алматы",
            g.getFont(), Color.BLACK, bounds, TextAlignment.MIDDLE, TextFormat.FIRST_LINE_VISIBLE
        );
        bounds = new Rectangle(950, 0, 320, 230);
        TextRenderer.drawString(g,
            "PBX\n" +
                technicalSpecifications.getString("pbxType") + "\n" +
                customerInformation.getString("legalName") + "\n" +
                technicalSpecifications.getString("pbxCity"),
            g.getFont(), Color.BLACK, bounds, TextAlignment.MIDDLE, TextFormat.FIRST_LINE_VISIBLE
        );

        g.dispose();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        replaceImage(doc, "image", new ByteArrayInputStream(output.toByteArray()));

        output.close();
        ByteArrayOutputStream downloadFile = new ByteArrayOutputStream();
        doc.write(downloadFile);
        doc.close();
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + variables.get("numberRequest").toString() + ".docx\"");
        IOUtils.copy(new ByteArrayInputStream(downloadFile.toByteArray()), response.getOutputStream());
    }

    public static void replaceText(XWPFDocument doc, String findText, String replaceText) {
        for (XWPFParagraph p : doc.getParagraphs()) {
            List<XWPFRun> runs = p.getRuns();
            if (runs != null) {
                for (XWPFRun r : runs) {
                    String text = r.getText(0);
                    if (text != null && text.contains(findText)) {
                        text = text.replace(findText, replaceText);
                        r.setText(text, 0);
                    }
                }
            }
        }
    }

    public static void replaceImage(XWPFDocument doc, String findText, InputStream inputStream) {
        for (XWPFParagraph p : doc.getParagraphs()) {
            List<XWPFRun> runs = p.getRuns();
            if (runs != null) {
                for (XWPFRun r : runs) {
                    String text = r.getText(0);
                    if (text != null && text.contains(findText)) {
                        text = text.replace(findText, "");
                        r.setText(text, 0);
                        try {
                            r.addPicture(inputStream, Document.PICTURE_TYPE_PNG, null, Units.toEMU(500), Units.toEMU(95));
                        } catch (InvalidFormatException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
