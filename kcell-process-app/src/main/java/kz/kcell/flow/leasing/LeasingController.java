package kz.kcell.flow.leasing;

import kz.kcell.flow.files.Minio;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.script.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

@RestController
@RequestMapping("/leasing")
@Log
public class LeasingController {

    private final String template;

    @Autowired
    private IdentityService identityService;

    @Autowired
    RuntimeService runtimeService;

    private Minio minioClient;

    @Autowired
    public LeasingController(ScriptEngineManager manager, Minio minioClient) throws ScriptException {
        InputStream fis = LeasingController.class.getResourceAsStream("/template/leasing/rent_request_source.rtf");
        Scanner s = new Scanner(fis, "UTF-8").useDelimiter("\\A");
        this.template = s.hasNext() ? s.next() : "";

        this.minioClient = minioClient;
    }


    @RequestMapping(value = "/rrfile/create", method = RequestMethod.POST, produces={"plain/text"}, consumes="application/json")
    @ResponseBody
    public ResponseEntity<String> FreephoneClientCreateUpdate(@RequestBody RRFileData rrFileData) throws Exception {

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }

        String path = rrFileData.processInstanceId + "/" + rrFileData.taskId + "/rrFile.rtf";

        try {
            String content = template;

            content = content.replaceAll("\\$NCP_FORM.NCP_ID\\$", rrFileData.ncpID != null?rrFileData.ncpID:"")
                .replaceAll("\\$CANDIDATE_FORM.SITENAME\\$", rrFileData.siteName != null?rrFileData.siteName:"")
                .replaceAll("\\$INFILL_DATE\\$", "")      // ????????
                .replaceAll("\\$USERNAME\\$", "")         // ????????
                .replaceAll("\\$NCP_FORM_REASON\\$", "")  // ????????
                .replaceAll("\\$NCP_FORM.PROJECT\\$", "") // ????????
                .replaceAll("\\$CANDIDATE_FORM.DATE OF VISIT\\$", rrFileData.dateOfVisit != null?rrFileData.dateOfVisit:"")
                .replaceAll("\\$CANDIDATE_FORM.ADDRESS\\$", rrFileData.cellAntennaAddress != null?rrFileData.cellAntennaAddress:"")
                .replaceAll("\\$NE_CONTACT_NAME\\$", rrFileData.contactName != null?rrFileData.contactName:"")
                .replaceAll("\\$NE_CONTACT_POSITION\\$", rrFileData.contactPosition != null?rrFileData.contactPosition:"")
                .replaceAll("\\$NE_CONTACT_INFO\\$", rrFileData.contactInfo != null?rrFileData.contactInfo:"")
                .replaceAll("\\$LATITUDE\\$", rrFileData.latitude != null?rrFileData.latitude:"")
                .replaceAll("\\$LONGITUDE\\$", rrFileData.longitude != null?rrFileData.longitude:"")
                .replaceAll("\\$CNSTRTYPENAME\\$", rrFileData.constructionType != null?rrFileData.constructionType:"")
                .replaceAll("\\$SQUARE\\$", rrFileData.square != null?rrFileData.square:"")
                .replaceAll("\\$TOWERTYPENAME\\$", "")   // ????????
                .replaceAll("\\$RBSTYPE\\$", rrFileData.rbsType != null?rrFileData.rbsType:"")
                .replaceAll("\\$BAND\\$", rrFileData.selectedBands != null?rrFileData.selectedBands:"")
                .replaceAll("\\$RBS_LOCATION\\$", rrFileData.rbsLocation != null?rrFileData.rbsLocation:"")
                .replaceAll("\\$ANTENNA\\$", rrFileData.selectedCellAntennas != null?rrFileData.selectedCellAntennas:"")
                .replaceAll("\\$ANTENNA_QNT\\$", rrFileData.cellAntennaQuantity != null?rrFileData.cellAntennaQuantity:"")
                .replaceAll("\\$DIMENSIONS\\$", rrFileData.cellAntennaDimensions != null?rrFileData.cellAntennaDimensions:"")
                .replaceAll("\\$WEIGHT\\$", rrFileData.cellAntennaWeight != null?rrFileData.cellAntennaWeight:"")
                .replaceAll("\\$SUSPENSION\\$", rrFileData.cellAntennaSuspensionHeight != null?rrFileData.cellAntennaSuspensionHeight:"")
                .replaceAll("\\$AZIMUTH\\$", rrFileData.cellAntennaAzimuth != null?rrFileData.cellAntennaAzimuth:"")
                .replaceAll("\\$NE_ANTENNA\\$", rrFileData.transmissionAntennaAntennaType != null?rrFileData.transmissionAntennaAntennaType:"")
                .replaceAll("\\$NE_ANTENNA_QNT\\$", rrFileData.transmissionAntennaAntennaQuantity != null?rrFileData.transmissionAntennaAntennaQuantity:"")
                .replaceAll("\\$NE_FREQUENCY_BAND\\$", rrFileData.transmissionAntennaAntennaFrequencyBand != null?rrFileData.transmissionAntennaAntennaFrequencyBand:"")
                .replaceAll("\\$NE_DIAMETER\\$", rrFileData.transmissionAntennaAntennaDiameter != null?rrFileData.transmissionAntennaAntennaDiameter:"")
                .replaceAll("\\$NE_WEIGHT\\$", rrFileData.transmissionAntennaAntennaWeight != null?rrFileData.transmissionAntennaAntennaWeight:"")
                .replaceAll("\\$NE_SUSPENSION\\$", rrFileData.transmissionAntennaAntennaSuspensionHeight != null?rrFileData.transmissionAntennaAntennaSuspensionHeight:"")
                .replaceAll("\\$NE_AZIMUTH\\$", rrFileData.transmissionAntennaAntennaAzimuth != null?rrFileData.transmissionAntennaAntennaAzimuth:"")
                .replaceAll("\\$COMMENTS\\$", rrFileData.newCandidateComments != null?rrFileData.newCandidateComments:"")
                .replaceAll("\\$NE_LEGAL_NAME\\$", rrFileData.rcLegalName != null?rrFileData.rcLegalName:"")
                .replaceAll("\\$NE_LEGAL_ADDRESS\\$", rrFileData.rcLegalAddress != null?rrFileData.rcLegalAddress:"")
                .replaceAll("\\$NE_TEL\\$", rrFileData.rcTelFax != null?rrFileData.rcTelFax:"")
                .replaceAll("\\$NE_LEADER_NAME\\$", rrFileData.rcFirstLeaderName != null?rrFileData.rcFirstLeaderName:"")
                .replaceAll("\\$NE_LEADER_POSITION\\$", rrFileData.rcFirstLeaderPos != null?rrFileData.rcFirstLeaderPos:"")
                .replaceAll("\\$NE_EMAIL\\$", rrFileData.rcEmail != null?rrFileData.rcEmail:"")
                .replaceAll("\\$LANDLORD\\$", rrFileData.landlord != null?rrFileData.landlord:"")
                .replaceAll("\\$CABLE_LENGTH\\$", rrFileData.cableLength != null?rrFileData.cableLength:"")
                .replaceAll("\\$LAYING_TYPE\\$", rrFileData.cableLayingType != null?rrFileData.cableLayingType:"")
                .replaceAll("\\$MONTHLY_PC\\$", rrFileData.monthlyPC != null?rrFileData.monthlyPC:"")
                .replaceAll("\\$RES4\\$", rrFileData.res4 != null?rrFileData.res4:"")
                .replaceAll("\\$RES10\\$", rrFileData.res10 != null?rrFileData.res10:"")
                .replaceAll("\\$FE_SURVEY_DATE\\$", "") // ????????
                .replaceAll("\\$VISIT_DATE\\$", "") // ????????
                .replaceAll("\\$FE_SITENAME\\$", rrFileData.farEndSitename != null?rrFileData.farEndSitename:"")
                .replaceAll("\\$FE_ADDRESS\\$", rrFileData.farEndAdress != null?rrFileData.farEndAdress:"")
                .replaceAll("\\$FE_CONTACT\\$", rrFileData.farEndContact != null?rrFileData.farEndContact:"")
                .replaceAll("\\$FE_SQUARE\\$", rrFileData.farEndSquare != null?rrFileData.farEndSquare:"")
                .replaceAll("\\$FE_EQUIPMENT\\$", rrFileData.farEndEquipmentType != null?rrFileData.farEndEquipmentType:"")
                .replaceAll("\\$FE_ANTENNA_QNT\\$", rrFileData.farEndAntennasQuantity != null?rrFileData.farEndAntennasQuantity:"")
                .replaceAll("\\$FE_ANTENNA_DMTR\\$", rrFileData.farEndAntennaDiameter != null?rrFileData.farEndAntennaDiameter:"")
                .replaceAll("\\$FE_TX_FREQ\\$", rrFileData.farEndTXFrequency != null?rrFileData.farEndTXFrequency:"")
                .replaceAll("\\$FE_RX_FREQ\\$", rrFileData.farEndRXFrequency != null?rrFileData.farEndRXFrequency:"")
                .replaceAll("\\$FE_ANTENNA_WEIGHT\\$", rrFileData.farEndWeight != null?rrFileData.farEndWeight:"")
                .replaceAll("\\$FE_SUSPENSION\\$", rrFileData.farEndSuspensionHeight != null?rrFileData.farEndSuspensionHeight:"")
                .replaceAll("\\$FE_AZIMUTH\\$", rrFileData.farEndAzimuth != null?rrFileData.farEndAzimuth:"")
                .replaceAll("\\$FE_CONTSR_TYPE\\$", rrFileData.farEndConstructionType != null?rrFileData.farEndConstructionType:"")
                .replaceAll("\\$FE_COMMENTS\\$", rrFileData.farEndComments != null?rrFileData.farEndComments:"")
                .replaceAll("\\$FE_LEGAL_NAME\\$", rrFileData.farEndRcLegalName != null?rrFileData.farEndRcLegalName:"")
                .replaceAll("\\$FE_LEGAL_ADDRESS\\$", rrFileData.farEndRcLegalAddress != null?rrFileData.farEndRcLegalAddress:"")
                .replaceAll("\\$FE_TEL\\$", rrFileData.farEndRcTelFax != null?rrFileData.farEndRcTelFax:"")
                .replaceAll("\\$FE_LEADER_NAME\\$", rrFileData.farEndRcFirstLeaderName != null?rrFileData.farEndRcFirstLeaderName:"")
                .replaceAll("\\$FE_LEADER_POSITION\\$", rrFileData.farEndRcFirstLeaderPosition != null?rrFileData.farEndRcFirstLeaderPosition:"")
                .replaceAll("\\$FE_EMAIL\\$", rrFileData.farEndRcEmail != null?rrFileData.farEndRcEmail:"")
                .replaceAll("\\$FE_CONTACT_NAME\\$", rrFileData.farEndRcContactName != null?rrFileData.farEndRcContactName:"")
                .replaceAll("\\$FE_CONTACT_POSITION\\$", rrFileData.farEndRcContactPosition != null?rrFileData.farEndRcContactPosition:"")
                .replaceAll("\\$FE_CONTACT_INFO\\$", rrFileData.farEndRcContactInformation != null?rrFileData.farEndRcContactInformation:"")
                .replaceAll("\\$FE_RESULTS\\$", rrFileData.farEndResultsOfVisit != null?rrFileData.farEndResultsOfVisit:"");

            InputStream is = new ByteArrayInputStream(content.getBytes("UTF-8"));

            minioClient.saveFile(path, is, "text/rtf");

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage());
        }

        return ResponseEntity.ok(path);
    }
}
