package kz.kcell.flow.leasing;

import io.minio.errors.*;
import kz.kcell.flow.files.Minio;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.spin.plugin.variable.SpinValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xmlpull.v1.XmlPullParserException;

import javax.script.*;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Scanner;
import java.util.TimeZone;

@RestController
@RequestMapping("/leasing")
@Log
public class LeasingController {

    private final String template;

    @Autowired
    private IdentityService identityService;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Value("${udb.oracle.url:jdbc:oracle:thin:@//sc2-appcl010406:1521/apexudb}")
    private String udbOracleUrl;

    @Value("${udb.oracle.username:udbrnd}")
    private String udbOracleUsername;

    @Value("${udb.oracle.password:udb}")
    private String udbOraclePassword;

    private Minio minioClient;

    @Autowired
    public LeasingController(ScriptEngineManager manager, Minio minioClient) {
        InputStream fis = LeasingController.class.getResourceAsStream("/leasing/rent_request_source.rtf");
        Scanner s = new Scanner(fis, "UTF-8").useDelimiter("\\A");
        this.template = s.hasNext() ? s.next() : "";

        this.minioClient = minioClient;
    }


    @RequestMapping(value = "/contract/{contactId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getPresignedGetObjectUrl(@PathVariable("contactId") String contactId, HttpServletRequest request) throws InvalidEndpointException, InvalidPortException, InvalidKeyException, InvalidBucketNameException, NoSuchAlgorithmException, InsufficientDataException, NoResponseException, ErrorResponseException, InternalException, InvalidExpiresRangeException, IOException, XmlPullParserException, ClassNotFoundException, SQLException {
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Almaty");
        TimeZone.setDefault(timeZone);
        Class.forName ("oracle.jdbc.OracleDriver");
        Connection udbConnect = DriverManager.getConnection(
            udbOracleUrl,
            udbOracleUsername,
            udbOraclePassword);
        try {
            if (udbConnect != null) {
                udbConnect.setAutoCommit(false);
                int i;

//                String SelectContract = "select c.cid,\n" +
                String SelectContract = "select c.* \n" +
////                    "       cas.action_id,\n" +
////                    "       csr.csrelid,\n" +
////                    "       ca.aaid,\n" +
//                    "       c.RENTSUM,\n" +
//                    "       c.rentsum_vat,\n" +
//                    "       c.contractid,\n" +
//                    "       c.incomingdate,\n" +
//                    "       c.incomingweek,\n" +
//                    "       c.contracttype,\n" +
//                    "       c.powersupply,\n" +
//                    "       c.legaltype,\n" +
//                    "       c.legalname,\n" +
//                    "       c.legaladdress,\n" +
//                    "       c.contactperson,\n" +
//                    "       c.contactphone,\n" +
//                    "       c.access_status,\n" +
//                    "       c.contract_sap_no,\n" +
//                    "       c.vendor_sap_no,\n" +
//                    "       c.contract_executor,\n" +
//                    "       c.vat,\n" +
//                    "       c.needvat,\n" +
//                    "       c.paymentperiod,\n" +
//                    "       c.paymentway,\n" +
//                    "       c.contractstartdate,\n" +
//                    "       c.contractenddate,\n" +
//                    "       c.autoprolongation,\n" +
//                    "       c.username,\n" +
//                    "       c.oblast_villageid,\n" +
//                    "       c.area_act_accept_date,\n" +
//                    "       c.RNN,\n" +
//                    "       c.INN,\n" +
//                    "       c.IBAN,\n" +
////                    "       ca.aa_type,\n" +
////                    "       ca.autoprolongation,\n" +
//                    "       c.BANK_RNN,\n" +
//                    "       c.BANK_ACCOUNT\n" +
                    "       from contracts c\n" +
//                    "  left join contract_aa ca on c.CID = ca.CID\n" +
//                    "  left join CONTRACT_AA_STATUS cas on cas.AAID = ca.AAID and cas.statusid != 40\n" +
                    "  left join CONTRACT_STATUS_REL csr on csr.contractid = c.cid where c.contractid = ?";
                PreparedStatement selectContractPreparedStatement = udbConnect.prepareStatement(SelectContract);

                i = 1;
                log.info("try to get contracts...");
                selectContractPreparedStatement.setString(i++, contactId); // contactId
                ResultSet resultSet = selectContractPreparedStatement.executeQuery();

                JSONArray json = new JSONArray();
                ResultSetMetaData rsmd = resultSet.getMetaData();
                while(resultSet.next()) {
                    int numColumns = rsmd.getColumnCount();
                    JSONObject obj = new JSONObject();
                    for (int j=1; j<=numColumns; j++) {
                        String column_name = rsmd.getColumnName(j);
                        obj.put(column_name, resultSet.getObject(column_name));
                    }
                    json.put(obj);
                }

                udbConnect.commit();
                udbConnect.close();
                return ResponseEntity.ok(json.toString());
            } else {
                udbConnect.close();
                JSONArray json = new JSONArray();
                JSONObject obj = new JSONObject();
                obj.put("result", "not found");
                json.put(obj);
                log.warning("Failed to make connection!");
                return ResponseEntity.ok("Failed to make connection!");
            }
        } catch (Exception e) {
            udbConnect.rollback();
            udbConnect.close();
            log.warning("connection Exception!");
            log.warning(e.getMessage());
            throw e;
        }
    }

    @RequestMapping(value = "/rrfile/create", method = RequestMethod.POST, produces={"plain/text"}, consumes="application/json")
    @ResponseBody
    public ResponseEntity<String> FreephoneClientCreateUpdate(@RequestBody RRFileData rrFileData) throws Exception {

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }

        if (rrFileData.processInstanceId == null || rrFileData.taskId == null) {
            log.warning("No Process Instance or Task specified");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No Process Instance or Task specified");
        }

        boolean taskBelongsToProcessInstance = taskService.createTaskQuery()
            .taskId(rrFileData.taskId)
            .processInstanceId(rrFileData.processInstanceId)
            .count() > 0;

        if (!taskBelongsToProcessInstance) {
            log.warning("The Task not found or does not belong to the Process Instance");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The Task not found or does not belong to the Process Instance");
        }

        String path = rrFileData.processInstanceId + "/" + rrFileData.taskId + "/rrFile.rtf";

        try {
            String content = template;

            content = replaceAll(content,"\\$NCP_FORM.NCP_ID\\$", rrFileData.ncpID);
            content = replaceAll(content,"\\$CANDIDATE_FORM.SITENAME\\$", rrFileData.siteName);
            content = replaceAll(content,"\\$INFILL_DATE\\$", ""); // ????????
            content = replaceAll(content,"\\$USERNAME\\$", "");    // ????????
            content = replaceAll(content,"\\$NCP_FORM_REASON\\$", ""); // ????????
            content = replaceAll(content,"\\$NCP_FORM.PROJECT\\$", ""); // ????????
            content = replaceAll(content, "\\$CANDIDATE_FORM.DATE OF VISIT\\$", rrFileData.dateOfVisit);
            content = replaceAll(content, "\\$CANDIDATE_FORM.ADDRESS\\$", rrFileData.cellAntennaAddress);
            content = replaceAll(content, "\\$NE_CONTACT_NAME\\$", rrFileData.contactName);
            content = replaceAll(content, "\\$NE_CONTACT_POSITION\\$", rrFileData.contactPosition);
            content = replaceAll(content, "\\$NE_CONTACT_INFO\\$", rrFileData.contactInfo);
            content = replaceAll(content, "\\$LATITUDE\\$", rrFileData.latitude);
            content = replaceAll(content, "\\$LONGITUDE\\$", rrFileData.longitude);
            content = replaceAll(content, "\\$CNSTRTYPENAME\\$", rrFileData.constructionType);
            content = replaceAll(content, "\\$SQUARE\\$", rrFileData.square);
            content = replaceAll(content, "\\$TOWERTYPENAME\\$", ""); // ????????
            content = replaceAll(content, "\\$RBSTYPE\\$", rrFileData.rbsType);
            content = replaceAll(content, "\\$BAND\\$", rrFileData.selectedBands);
            content = replaceAll(content, "\\$RBS_LOCATION\\$", rrFileData.rbsLocation);
            content = replaceAll(content, "\\$ANTENNA\\$", rrFileData.selectedCellAntennas);
            content = replaceAll(content, "\\$ANTENNA_QNT\\$", rrFileData.cellAntennaQuantity);
            content = replaceAll(content, "\\$DIMENSIONS\\$", rrFileData.cellAntennaDimensions);
            content = replaceAll(content, "\\$WEIGHT\\$", rrFileData.cellAntennaWeight);
            content = replaceAll(content, "\\$SUSPENSION\\$", rrFileData.cellAntennaSuspensionHeight);
            content = replaceAll(content, "\\$AZIMUTH\\$", rrFileData.cellAntennaAzimuth);
            content = replaceAll(content, "\\$NE_ANTENNA\\$", rrFileData.transmissionAntennaAntennaType);
            content = replaceAll(content, "\\$NE_ANTENNA_QNT\\$", rrFileData.transmissionAntennaAntennaQuantity);
            content = replaceAll(content, "\\$NE_FREQUENCY_BAND\\$", rrFileData.transmissionAntennaAntennaFrequencyBand);
            content = replaceAll(content, "\\$NE_DIAMETER\\$", rrFileData.transmissionAntennaAntennaDiameter);
            content = replaceAll(content, "\\$NE_WEIGHT\\$", rrFileData.transmissionAntennaAntennaWeight);
            content = replaceAll(content, "\\$NE_SUSPENSION\\$", rrFileData.transmissionAntennaAntennaSuspensionHeight);
            content = replaceAll(content, "\\$NE_AZIMUTH\\$", rrFileData.transmissionAntennaAntennaAzimuth);
            content = replaceAll(content, "\\$COMMENTS\\$", rrFileData.newCandidateComments);
            content = replaceAll(content, "\\$NE_LEGAL_NAME\\$", rrFileData.rcLegalName);
            content = replaceAll(content, "\\$NE_LEGAL_ADDRESS\\$", rrFileData.rcLegalAddress);
            content = replaceAll(content, "\\$NE_TEL\\$", rrFileData.rcTelFax);
            content = replaceAll(content, "\\$NE_LEADER_NAME\\$", rrFileData.rcFirstLeaderName);
            content = replaceAll(content, "\\$NE_LEADER_POSITION\\$", rrFileData.rcFirstLeaderPos);
            content = replaceAll(content, "\\$NE_EMAIL\\$", rrFileData.rcEmail);
            content = replaceAll(content, "\\$LANDLORD\\$", rrFileData.landlord);
            content = replaceAll(content, "\\$CABLE_LENGTH\\$", rrFileData.cableLength);
            content = replaceAll(content, "\\$LAYING_TYPE\\$", rrFileData.cableLayingType);
            content = replaceAll(content, "\\$MONTHLY_PC\\$", rrFileData.monthlyPC);
            content = replaceAll(content, "\\$RES4\\$", rrFileData.res4);
            content = replaceAll(content, "\\$RES10\\$", rrFileData.res10);
            content = replaceAll(content, "\\$FE_SURVEY_DATE\\$", ""); // ????????
            content = replaceAll(content,"\\$VISIT_DATE\\$", "");
            content = replaceAll(content, "\\$FE_SITENAME\\$", rrFileData.farEndSitename);
            content = replaceAll(content, "\\$FE_ADDRESS\\$", rrFileData.farEndAdress);
            content = replaceAll(content, "\\$FE_CONTACT\\$", rrFileData.farEndContact);
            content = replaceAll(content, "\\$FE_SQUARE\\$", rrFileData.farEndSquare);
            content = replaceAll(content, "\\$FE_EQUIPMENT\\$", rrFileData.farEndEquipmentType);
            content = replaceAll(content, "\\$FE_ANTENNA_QNT\\$", rrFileData.farEndAntennasQuantity);
            content = replaceAll(content, "\\$FE_ANTENNA_DMTR\\$", rrFileData.farEndAntennaDiameter);
            content = replaceAll(content, "\\$FE_TX_FREQ\\$", rrFileData.farEndTXFrequency);
            content = replaceAll(content, "\\$FE_RX_FREQ\\$", rrFileData.farEndRXFrequency);
            content = replaceAll(content, "\\$FE_ANTENNA_WEIGHT\\$", rrFileData.farEndWeight);
            content = replaceAll(content, "\\$FE_SUSPENSION\\$", rrFileData.farEndSuspensionHeight);
            content = replaceAll(content, "\\$FE_AZIMUTH\\$", rrFileData.farEndAzimuth);
            content = replaceAll(content, "\\$FE_CONTSR_TYPE\\$", rrFileData.farEndConstructionType);
            content = replaceAll(content, "\\$FE_COMMENTS\\$", rrFileData.farEndComments);
            content = replaceAll(content, "\\$FE_LEGAL_NAME\\$", rrFileData.farEndRcLegalName);
            content = replaceAll(content, "\\$FE_LEGAL_ADDRESS\\$", rrFileData.farEndRcLegalAddress);
            content = replaceAll(content, "\\$FE_TEL\\$", rrFileData.farEndRcTelFax);
            content = replaceAll(content, "\\$FE_LEADER_NAME\\$", rrFileData.farEndRcFirstLeaderName);
            content = replaceAll(content, "\\$FE_LEADER_POSITION\\$", rrFileData.farEndRcFirstLeaderPosition);
            content = replaceAll(content, "\\$FE_EMAIL\\$", rrFileData.farEndRcEmail);
            content = replaceAll(content, "\\$FE_CONTACT_NAME\\$", rrFileData.farEndRcContactName);
            content = replaceAll(content, "\\$FE_CONTACT_POSITION\\$", rrFileData.farEndRcContactPosition);
            content = replaceAll(content, "\\$FE_CONTACT_INFO\\$", rrFileData.farEndRcContactInformation);
            content = replaceAll(content, "\\$FE_RESULTS\\$", rrFileData.farEndResultsOfVisit);

            ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes("UTF-8"));
            minioClient.saveFile(path, is, "text/rtf;charset=UTF-8");

            String name = "rrFile.rtf";
            String json = "{\"name\" : \"" + name + "\",\"path\" : \"" + path + "\"}";

            runtimeService.setVariable(rrFileData.processInstanceId, "rrFile", SpinValues.jsonValue(json));
            is.close();

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage());
        }

        return ResponseEntity.ok(path);
    }

    private String replaceAll(String content, String from, String to){
        return content.replaceAll(from, to != null?convertToAnsiCode(to):"");
    }

    private String convertToAnsiCode(String input){

        StringBuilder str = new StringBuilder("\\\\uc0");

        for (char i:input.toCharArray()){
            str.append("\\\\u"+ (int)i);
        }

        return str.toString();
    }
}
