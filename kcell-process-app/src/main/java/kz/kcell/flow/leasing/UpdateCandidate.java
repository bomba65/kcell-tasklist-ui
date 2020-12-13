package kz.kcell.flow.leasing;

import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.camunda.spin.Spin.JSON;

@Log
@Service("UpdateCandidate")
public class UpdateCandidate implements JavaDelegate {

    @Autowired
    DataSource dataSource;

    @Value("${udb.oracle.url:jdbc:oracle:thin:@//sc2-appcl010406:1521/apexudb}")
    private String udbOracleUrl;

    @Value("${udb.oracle.username:udbrnd}")
    private String udbOracleUsername;

    @Value("${udb.oracle.password:udb}")
    private String udbOraclePassword;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        try {
            TimeZone timeZone = TimeZone.getTimeZone("Asia/Almaty");
            TimeZone.setDefault(timeZone);
            Class.forName("oracle.jdbc.OracleDriver");
            Connection udbConnect = DriverManager.getConnection(
                udbOracleUrl,
                udbOracleUsername,
                udbOraclePassword);
//            Connection udbConnect = DriverManager.getConnection("jdbc:oracle:thin:@//sc2-appcl010406:1521/apexudb", "app_apexudb_camunda", "p28zt#7C");
            try {
                if (udbConnect != null) {
                    udbConnect.setAutoCommit(false);
                    log.info("Connected to the database!");

                    // proc vars
//                    Integer cn_rbs_location = 2;

                    //generated vars
                    Long createdArtefactId = delegateExecution.getVariable("createdArtefactId") != null ? (Long) delegateExecution.getVariable("createdArtefactId") : null;
                    Long createdArtefactRSDId = delegateExecution.getVariable("createdArtefactRSDId") != null ? (Long) delegateExecution.getVariable("createdArtefactRSDId") : null;
                    Long createdArtefactRRId = delegateExecution.getVariable("createdArtefactRRId") != null ? (Long) delegateExecution.getVariable("createdArtefactRRId") : null;
                    Long createdArtefactExtTSDId = delegateExecution.getVariable("createdArtefactExtTSDId") != null ? (Long) delegateExecution.getVariable("createdArtefactExtTSDId") : null;
                    Long createdArtefactRRStatusId = (Long) delegateExecution.getVariable("createdArtefactRRStatusId");

                    Long createdArtefactRrPowerId = delegateExecution.getVariable("createdArtefactRrPowerId") != null ? (Long) delegateExecution.getVariable("createdArtefactRrPowerId") : null;
                    Long createdArtefactRrRenterCnId = delegateExecution.getVariable("createdArtefactRrRenterCnId") != null ? (Long) delegateExecution.getVariable("createdArtefactRrRenterCnId") : null;
                    Long createdArtefactRrRenterFeId = delegateExecution.getVariable("createdArtefactRrRenterFeId") != null ? (Long) delegateExecution.getVariable("createdArtefactRrRenterFeId") : null;
                    Long createdArtefactRrTrId = delegateExecution.getVariable("createdArtefactRrTrId") != null ? (Long) delegateExecution.getVariable("createdArtefactRrTrId") : null;
                    Long createdTrAntennaId = delegateExecution.getVariable("createdTrAntennaId") != null ? (Long) delegateExecution.getVariable("createdTrAntennaId") : null;
//                    Long createdRsdHistoryId = delegateExecution.getVariable("createdRsdHistoryId") != null ? (Long) delegateExecution.getVariable("createdRsdHistoryId") : null;

                    String ncpId = delegateExecution.getVariable("ncpID") != null ? delegateExecution.getVariable("ncpID").toString() : null;
                    String starter = delegateExecution.getVariable("starter") != null ? delegateExecution.getVariable("starter").toString() : null;
                    String rbsType = delegateExecution.getVariable("rbsTypeId") != null ? delegateExecution.getVariable("rbsTypeId").toString() : null;
                    String bandsIdForUDB = delegateExecution.getVariable("bandsIdForUDB") != null ? delegateExecution.getVariable("bandsIdForUDB").toString() : null;
                    String targetCoverage = delegateExecution.getVariable("targetCoverage") != null ? delegateExecution.getVariable("targetCoverage").toString() : null;
                    String connectionPossibleFromRES = delegateExecution.getVariable("connectionPossibleFromRES") != null ? delegateExecution.getVariable("connectionPossibleFromRES").toString() : null;

                    SpinJsonNode siteTypeJson = delegateExecution.getVariable("siteType") != null ? JSON(delegateExecution.getVariable("siteType")) : null;
                    SpinJsonNode siteType = siteTypeJson != null && siteTypeJson.hasProp("id") ? siteTypeJson.prop("id") : null;
                    String siteTypeString = siteType != null ? siteType.value().toString() : null;
                    Integer siteTypeInt = siteTypeString != null ? Integer.parseInt(siteTypeString) : null;

                    SpinJsonNode powerSource = delegateExecution.hasVariable("powerSource") ? JSON(delegateExecution.getVariable("powerSource")) : null;
//                    String ps_lt_id = powerSource != null && powerSource.hasProp("cableLayingType") && powerSource.prop("cableLayingType") != null ? (powerSource.prop("cableLayingType").value().toString()) : null;
                    String landlord_cable_length = powerSource != null && powerSource.hasProp("cableLength") && powerSource.prop("cableLength").value() != null ? (powerSource.prop("cableLength").value().toString()) : null;
                    String landlord_monthly_pc = powerSource != null && powerSource.hasProp("agreeToReceiveMonthlyPayment") && powerSource.prop("agreeToReceiveMonthlyPayment").value() != null ? (powerSource.prop("agreeToReceiveMonthlyPayment").value().toString()) : null;
                    String res_4kv = powerSource != null && powerSource.hasProp("closestPublic04") && powerSource.prop("closestPublic04").value() != null ? (powerSource.prop("closestPublic04").value().toString()) : null;
                    String res_10kv = powerSource != null && powerSource.hasProp("closestPublic10") && powerSource.prop("closestPublic10").value() != null ? (powerSource.prop("closestPublic10").value().toString()) : null;

                    SpinJsonNode candidate = delegateExecution.getVariable("candidate") != null ? JSON(delegateExecution.getVariable("candidate")) : null;
                    String cn_longitude = candidate != null && candidate.hasProp("longitude") && candidate.prop("longitude") != null ? "E " + candidate.prop("longitude").value().toString().replace(".", ",") : null;
                    String cn_latitude = candidate != null && candidate.hasProp("latitude") && candidate.prop("latitude") != null ? "N " + candidate.prop("latitude").value().toString().replace(".", ",") : null;
                    String cn_siteName = candidate != null && candidate.hasProp("siteName") && candidate.prop("siteName") != null ? candidate.prop("siteName").value().toString() : null;
                    String cn_comments = candidate != null && candidate.hasProp("comments") && candidate.prop("comments") != null ? candidate.prop("comments").value().toString() : null;
                    Number cn_square = candidate != null && candidate.hasProp("square") && candidate.prop("square") != null ? candidate.prop("square").numberValue() : null;
                    String cn_constructionType = candidate != null && candidate.hasProp("constructionType") && candidate.prop("constructionType").hasProp("id") && candidate.prop("constructionType").prop("id") != null ? candidate.prop("constructionType").prop("id").value().toString() : null;
                    String trType = candidate != null ? (candidate.hasProp("transmissionType") ? candidate.prop("transmissionType").value().toString() : null) : null;

                    Number cn_height_constr = candidate.hasProp("cn_height_constr") ? Integer.parseInt(candidate.prop("cn_height_constr").value().toString()) : 0;
                    Number cn_altitude = candidate.hasProp("cn_altitude") ? Integer.parseInt(candidate.prop("cn_altitude").value().toString()) : 0;

                    String cn_rbs_location = candidate != null && candidate.hasProp("rbsLocation") && candidate.prop("rbsLocation") != null && candidate.prop("rbsLocation").hasProp("id") && candidate.prop("rbsLocation").prop("id") != null ? (candidate.prop("rbsLocation").prop("id").value().toString()) : null;

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX"); //2020-01-02T18:00:00.000Z
                    String cn_date_of_visit = candidate != null && candidate.hasProp("dateOfVisit") && candidate.prop("dateOfVisit") != null ? (candidate.prop("dateOfVisit").value().toString()) : null;
                    Date date_of_visit_date = cn_date_of_visit != null ? formatter.parse(cn_date_of_visit) : null;
                    Calendar date_of_visit = Calendar.getInstance();
                    date_of_visit.setTime(date_of_visit_date);
                    date_of_visit.add(Calendar.HOUR_OF_DAY, 6);

                    SpinJsonNode ne = delegateExecution.getVariable("transmissionAntenna") != null ? JSON(delegateExecution.getVariable("transmissionAntenna")) : null;
                    String ne_azimuth = ne != null ? (ne.hasProp("azimuth") ? ne.prop("azimuth").value().toString() : "0") : null;
                    String ne_diameter = ne != null ? (ne.hasProp("diameter") ? ne.prop("diameter").value().toString() : "0") : null;
                    String ne_frequencyBand = ne != null && ne.hasProp("frequencyBand") ? ne.prop("frequencyBand").value().toString().replaceAll("[^0-9.]", "") : null;
                    Number ne_suspensionHeight = ne != null && ne.hasProp("suspensionHeight") ? (ne.prop("suspensionHeight").numberValue()) : null;
                    String ne_longitude = ne != null && ne.hasProp("address") && ne.prop("address").hasProp("longitude") ? (ne.prop("address").prop("longitude").value().toString()) : null;
                    String ne_latitude = ne != null && ne.hasProp("address") && ne.prop("address").hasProp("latitude") ? (ne.prop("address").prop("latitude").value().toString()) : null;

                    SpinJsonNode feJson = delegateExecution.getVariable("farEndInformation") != null ? JSON(delegateExecution.getVariable("farEndInformation")) : null;
                    SpinList farEnds = feJson != null ? feJson.elements() : null;
                    SpinJsonNode fe = (farEnds != null && farEnds.size() > 0) ? (SpinJsonNode) farEnds.get(0) : null;

                    String fe_azimuth = fe != null && fe.hasProp("azimuth") ? (fe.prop("azimuth").value().toString()) : null;
                    String fe_diameter = fe != null && fe.hasProp("diameter") ? (fe.prop("diameter").value().toString()) : null;
                    String fe_frequencyBand = fe != null && fe.hasProp("frequencyBand") ? fe.prop("frequencyBand").value().toString().replaceAll("[^0-9.]", "") : null;
                    String fe_suspensionHeight = fe != null && fe.hasProp("suspensionHeight") && fe.prop("suspensionHeight") != null ? fe.prop("suspensionHeight").value().toString() : null;
                    String fe_constructionType = fe != null && fe.hasProp("constructionType") && fe.prop("constructionType") != null ? (fe.prop("constructionType").hasProp("id") ? fe.prop("constructionType").prop("id").value().toString() : null) : null;
                    String fe_sitename = fe != null && fe.hasProp("farEndName") && fe.prop("farEndName") != null ? fe.prop("farEndName").value().toString() : null;
                    String fe_comment = fe != null && fe.hasProp("comments") && fe.prop("comments") != null ? fe.prop("comments").value().toString() : null;
                    String fe_survey_date = fe != null && fe.hasProp("surveyDate") && fe.prop("surveyDate") != null ? (fe.prop("surveyDate").value().toString()) : null;
                    Date fe_formated_survey_date = fe_survey_date != null ? formatter.parse(fe_survey_date) : null;
                    Calendar fe_cal_survey_date = Calendar.getInstance();
                    if (fe_formated_survey_date != null){
                        fe_cal_survey_date.setTime(fe_formated_survey_date);
                        fe_cal_survey_date.add(Calendar.HOUR_OF_DAY, 6);
                    }

                    String fe_legal_name = fe != null && fe.hasProp("renterCompany") && fe.prop("renterCompany").hasProp("legalName") ? fe.prop("renterCompany").prop("legalName").value().toString() : null;
                    String fe_legal_address = fe != null && fe.hasProp("renterCompany") && fe.prop("renterCompany").hasProp("fe_legal_address") ? fe.prop("renterCompany").prop("fe_legal_address").value().toString() : null;
                    String fe_phone_fax = fe != null && fe.hasProp("renterCompany") && fe.prop("renterCompany").hasProp("telFax") ? fe.prop("renterCompany").prop("telFax").value().toString() : null;
                    String fe_leader_name = fe != null && fe.hasProp("renterCompany") && fe.prop("renterCompany").hasProp("firstLeaderName") ? fe.prop("renterCompany").prop("firstLeaderName").value().toString() : null;
                    String fe_leader_position = fe != null && fe.hasProp("renterCompany") && fe.prop("renterCompany").hasProp("firstLeaderPosition") ? fe.prop("renterCompany").prop("firstLeaderPosition").value().toString() : null;
                    String fe_email = fe != null && fe.hasProp("renterCompany") && fe.prop("renterCompany").hasProp("email") ? fe.prop("renterCompany").prop("email").value().toString() : null;
                    String fe_contact_name = fe != null && fe.hasProp("renterCompany") && fe.prop("renterCompany").hasProp("name") ? fe.prop("renterCompany").prop("name").value().toString() : null;
                    String fe_contact_position = fe != null && fe.hasProp("renterCompany") && fe.prop("renterCompany").hasProp("position") ? fe.prop("renterCompany").prop("position").value().toString() : null;
                    String fe_contact_information = fe != null && fe.hasProp("renterCompany") && fe.prop("renterCompany").hasProp("contactInformation") ? fe.prop("renterCompany").prop("contactInformation").value().toString() : null;
                    String fe_name = fe != null && fe.hasProp("farEndName") ? fe.prop("farEndName").value().toString() : null;
                    String fe_square = fe != null && fe.hasProp("square") ? fe.prop("square").value().toString() : null;
                    String fe_antennas_quantity = fe != null && fe.hasProp("antennasQuantity") ? fe.prop("antennasQuantity").value().toString() : null;
                    String fe_weight = fe != null && fe.hasProp("weight") ? fe.prop("weight").value().toString() : null;
                    String fe_suspension_height = fe != null && fe.hasProp("suspensionHeight") && fe.prop("suspensionHeight") != null ? fe.prop("suspensionHeight").value().toString() : null;
                    String fe_comments = fe != null && fe.hasProp("comments") && fe.prop("comments") != null ? fe.prop("comments").value().toString() : null;
                    String fe_results_visit_objects = fe != null && fe.hasProp("resultsOfVisit") && fe.prop("resultsOfVisit") != null ? fe.prop("resultsOfVisit").value().toString() : null;
//                    String fe_equipment_type = fe != null && fe.hasProp("equipmentType") && fe.prop("equipmentType").value() != null  ? fe.prop("equipmentType").value().toString() : null;

                    String fe_equipment_type_string = fe != null && fe.hasProp("equipmentType") ? (fe.prop("equipmentType").value().toString()) : null;
//                    TN2P		2
//                    TN20P		6
//                    TN6P		4
                    Number fe_equipment_type = null;
                    if (fe_equipment_type_string.toUpperCase().equals("TN2P")) {
                        fe_equipment_type = 2;
                    } else if (fe_equipment_type_string.toUpperCase().equals("TN20P")) {
                        fe_equipment_type = 6;
                    } else if (fe_equipment_type_string.toUpperCase().equals("TN6P")) {
                        fe_equipment_type = 4;
                    }

                    Number fe_artefact_id = 0;
                    String SelectArtefactBySite = "select * from ARTEFACT where SITENAME = ?";
                    PreparedStatement selectArtefactBySitePreparedStatement = udbConnect.prepareStatement(SelectArtefactBySite);
                    int i = 1;
                    log.info("get artefact_id by fe_sitename...");
                    log.info(fe_sitename);
                    selectArtefactBySitePreparedStatement.setString(i++, fe_sitename); // sitename
                    ResultSet resultSet = selectArtefactBySitePreparedStatement.executeQuery();

                    if (resultSet.next() == false ) {
                        log.info("not Found");
                    } else {
                        fe_artefact_id = resultSet.getInt("ARTEFACTID");
                        log.info("fe_artefact_id: " + fe_artefact_id);
                    }

                    String fe_address = fe != null ? ("" +
                        (fe.hasProp("address") && fe.prop("address").hasProp("cn_addr_oblast") ? fe.prop("address").prop("cn_addr_oblast").value().toString() : "") +
                        (fe.hasProp("address") && fe.prop("address").hasProp("cn_addr_district") && fe.prop("address").prop("cn_addr_district") != null ? ", " + fe.prop("address").prop("cn_addr_district").value().toString() : "") +
                        (fe.hasProp("address") && fe.prop("address").hasProp("cn_addr_city") && fe.prop("address").prop("cn_addr_city") != null ? ", " + fe.prop("address").prop("cn_addr_city").value().toString() : "") +
                        (fe.hasProp("address") && fe.prop("address").hasProp("cn_addr_street") && fe.prop("address").prop("cn_addr_street") != null ? ", " + fe.prop("address").prop("cn_addr_street").value().toString() : "") +
                        (fe.hasProp("address") && fe.prop("address").hasProp("cn_addr_building") && fe.prop("address").prop("cn_addr_building") != null ? ", " + fe.prop("address").prop("cn_addr_building").value().toString() : "") +
                        (fe.hasProp("address") && fe.prop("address").hasProp("cn_addr_cadastral_number") && fe.prop("address").prop("cn_addr_cadastral_number") != null ? ", " + fe.prop("address").prop("cn_addr_cadastral_number").value().toString() : "") +
                        (fe.hasProp("address") && fe.prop("address").hasProp("cn_addr_note") && fe.prop("address").prop("cn_addr_note") != null ? ", " + fe.prop("address").prop("cn_addr_note").value().toString() : "")) : null;


                    SpinJsonNode renterCompany = delegateExecution.getVariable("renterCompany") != null ? JSON(delegateExecution.getVariable("renterCompany")) : null;
                    String cn_legal_name = renterCompany != null && renterCompany.hasProp("legalName") && renterCompany.prop("legalName") != null ? (renterCompany.prop("legalName").value().toString()) : null;
                    String cn_phone_fax = renterCompany != null && renterCompany.hasProp("telFax") && renterCompany.prop("telFax") != null ? (renterCompany.prop("telFax").value().toString()) : null;
                    String cn_legal_address = renterCompany != null && renterCompany.hasProp("legalAddress") && renterCompany.prop("legalAddress") != null ? (renterCompany.prop("legalAddress").value().toString()) : null;
                    String cn_leader_name = renterCompany != null && renterCompany.hasProp("firstLeaderName") && renterCompany.prop("firstLeaderName") != null ? (renterCompany.prop("firstLeaderName").value().toString()) : null;
                    String cn_leader_position = renterCompany != null && renterCompany.hasProp("firstLeaderPos") && renterCompany.prop("firstLeaderPos") != null ? (renterCompany.prop("firstLeaderPos").value().toString()) : null;
                    String cn_email = renterCompany != null && renterCompany.hasProp("email") && renterCompany.prop("email") != null ? (renterCompany.prop("email").value().toString()) : null;
                    String cn_contact_name = renterCompany != null && renterCompany.hasProp("contactName") && renterCompany.prop("contactName") != null ? (renterCompany.prop("contactName").value().toString()) : "" +
                        " " + renterCompany != null && renterCompany.hasProp("contactLastName") && renterCompany.prop("contactLastName") != null ? (renterCompany.prop("contactLastName").value().toString()) : ""
                        ;
                    String cn_contact_position = renterCompany != null && renterCompany.hasProp("contactPosition") && renterCompany.prop("contactPosition") != null ? (renterCompany.prop("contactPosition").value().toString()) : null;
                    String cn_contact_information = renterCompany != null && renterCompany.hasProp("contactInfo") && renterCompany.prop("contactInfo") != null ? (renterCompany.prop("contactInfo").value().toString()) : null;

                    String contact_person = "" +
                        (renterCompany != null && renterCompany.hasProp("contactName") && !renterCompany.prop("contactName").equals(null) ? renterCompany.prop("contactName").value().toString() : "") +
                        " " + (renterCompany != null && renterCompany.hasProp("contactPosition") && !renterCompany.prop("contactPosition").equals(null) ? renterCompany.prop("contactPosition").value().toString() : "") +
                        " ( tel:" + (renterCompany != null && renterCompany.hasProp("telFax") && !renterCompany.prop("telFax").equals(null) ? renterCompany.prop("telFax").value().toString() : "") + ")";

                    //{"cn_addr_oblast":"область Акмолинская","cn_addr_district":"","cn_addr_city":"г. Кокшетау","cn_addr_street":"фывфв","ca_not_full_addres":false,"cn_addr_building":"фывыфв"}
                    SpinJsonNode address = delegateExecution.getVariable("address") != null ? JSON(delegateExecution.getVariable("address")) : null;
                    String cn_address = "" + (address != null && address.hasProp("cn_addr_oblast") && address.prop("cn_addr_oblast") != null ? address.prop("cn_addr_oblast").value().toString() : "") +
                        (address != null && address.hasProp("cn_addr_district") && address.prop("cn_addr_district") != null ? ", " + address.prop("cn_addr_district").value().toString() : "") +
                        (address != null && address.hasProp("cn_addr_city") && address.prop("cn_addr_city") != null ? ", " + address.prop("cn_addr_city").value().toString() : "") +
                        (address != null && address.hasProp("cn_addr_street") && address.prop("cn_addr_street") != null ? ", " + address.prop("cn_addr_street").value().toString() : "") +
                        (address != null && address.hasProp("cn_addr_building") && address.prop("cn_addr_building") != null ? ", " + address.prop("cn_addr_building").value().toString() : "") +
                        (address != null && address.hasProp("cn_addr_cadastral_number") && address.prop("cn_addr_cadastral_number") != null ? ", " + address.prop("cn_addr_cadastral_number").value().toString() : "") +
                        (address != null && address.hasProp("cn_addr_note") && address.prop("cn_addr_note") != null ? ", " + address.prop("cn_addr_note").value().toString() : "");

                    String cn_bsc = candidate != null && candidate.hasProp("bsc") && candidate.prop("bsc") != null && candidate.prop("bsc").hasProp("id") && candidate.prop("bsc").prop("id") != null ? candidate.prop("bsc").prop("id").value().toString() : null;
                    Integer cn_bscInt = cn_bsc != null ? Integer.parseInt(cn_bsc) : null;

                    //UPDATE ARTEFACT ARTEFACT_CURRENT_STATE
                    boolean trTypeSatelliteOrProvider = trType != null && (trType.equals("Provider") || trType.equals("Satellite"));
                    log.info("UPDATE ARTEFACT_CURRENT_STATE");
                    StringBuilder updateArtefactCurrentStateFirstPart = new StringBuilder("UPDATE ARTEFACT_CURRENT_STATE SET ");
                    StringBuilder updateArtefactCurrentStateSecondPart = new StringBuilder("RR_STATUS = ?, RR_STATUS_DATE = ?, LONGITUDE = ?, LATITUDE = ?, RBS_TYPE = ?, BSC = ?, BAND = ?, RBS_LOCATION = ?, ALTITUDE = ?, CONSTRUCTION_HEIGHT = ?, CONSTRUCTION_TYPE = ?, ADDRESS = ?, CONTACT_PERSON = ?, COMMENTS = ?, EQUIPMENT_TYPE = ?, GS_STATUS = ?, PL_COMMENTS = ?, INST_COMMENTS = ? WHERE ARTEFACTID = ?");

                    if(trTypeSatelliteOrProvider) {
                        updateArtefactCurrentStateFirstPart.append("FE_STATUS = ?, ");
                    }
//                    String UpdateArtefactCurrentState = "UPDATE ARTEFACT_CURRENT_STATE SET FE_STATUS = ?,RR_STATUS = ?, RR_STATUS_DATE = ?, LONGITUDE = ?, LATITUDE = ?, RBS_TYPE = ?, BSC = ?, BAND = ?, RBS_LOCATION = ?, ALTITUDE = ?, CONSTRUCTION_HEIGHT = ?, CONSTRUCTION_TYPE = ?, ADDRESS = ?, CONTACT_PERSON = ?, COMMENTS = ?, EQUIPMENT_TYPE = ?, GS_STATUS = ?, PL_COMMENTS = ?, INST_COMMENTS = ? WHERE ARTEFACTID = ?";
                    updateArtefactCurrentStateFirstPart.append(updateArtefactCurrentStateSecondPart.toString());
                    String UpdateArtefactCurrentState = updateArtefactCurrentStateFirstPart.toString();

                    PreparedStatement updateArtefactCurrentStatePreparedStatement = udbConnect.prepareStatement(UpdateArtefactCurrentState);
                    Integer rbsTypeInt = rbsType != null ? Integer.parseInt(rbsType) : null;
                    Integer cnConstructionTypeInt = cn_constructionType != null ? Integer.parseInt(cn_constructionType) : null;

                    Integer bandsIdForUDBInt = bandsIdForUDB != null ? Integer.parseInt(bandsIdForUDB) : null;
                    String contractInfoString = renterCompany != null && renterCompany.hasProp("contactInfo") && !renterCompany.prop("contactInfo").equals(null) ? renterCompany.prop("contactInfo").value().toString() : "";

                    String inst_comments = null;
                    if(delegateExecution.hasVariable("ne_tr_parameters")){
                        inst_comments = (String) delegateExecution.getVariable("ne_tr_parameters");
                    }

                    i = 1;
                    log.info("UPDATE ARTEFACT ARTEFACT_CURRENT_STATE");
                    // set values to insert

                    if (trTypeSatelliteOrProvider) {
                        updateArtefactCurrentStatePreparedStatement.setLong(i++, 3); //FE_STATUS
                    }
                    updateArtefactCurrentStatePreparedStatement.setLong(i++, 3); // RR_STATUS
                    updateArtefactCurrentStatePreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // RR_STATUS_DATE
                    updateArtefactCurrentStatePreparedStatement.setString(i++, cn_longitude); //longitude
                    updateArtefactCurrentStatePreparedStatement.setString(i++, cn_latitude); //latitude
                    if (rbsTypeInt != null) {
                        updateArtefactCurrentStatePreparedStatement.setLong(i++, rbsTypeInt); // RBS_TYPE (cn_rbs_type)
                    } else {
                        updateArtefactCurrentStatePreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (cn_bscInt != null) {
                        updateArtefactCurrentStatePreparedStatement.setLong(i++, cn_bscInt); // BSC (cn_bsc)
                    } else {
                        updateArtefactCurrentStatePreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (bandsIdForUDBInt != null) {
                        updateArtefactCurrentStatePreparedStatement.setLong(i++, bandsIdForUDBInt); // BAND (cn_band)
                    } else {
                        updateArtefactCurrentStatePreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (cn_rbs_location != null) {
                        Integer cn_rbs_location_int = Integer.parseInt(cn_rbs_location);
                        updateArtefactCurrentStatePreparedStatement.setLong(i++, cn_rbs_location_int); // RBS_LOCATION (cn_rbs_location) //
                    } else {
                        updateArtefactCurrentStatePreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (cn_altitude != null) {
                        updateArtefactCurrentStatePreparedStatement.setLong(i++, cn_altitude.longValue()); // ALTITUDE (cn_altitude)
                    } else {
                        updateArtefactCurrentStatePreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (cn_height_constr != null) {
                        updateArtefactCurrentStatePreparedStatement.setLong(i++, cn_height_constr.longValue()); // CONSTRUCTION_HEIGHT (cn_height_constr)
                    } else {
                        updateArtefactCurrentStatePreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (cnConstructionTypeInt != null) {
                        updateArtefactCurrentStatePreparedStatement.setLong(i++, cnConstructionTypeInt); //construction_type
                    } else {
                        updateArtefactCurrentStatePreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    updateArtefactCurrentStatePreparedStatement.setString(i++, cn_address); //address
                    updateArtefactCurrentStatePreparedStatement.setString(i++, contact_person); //contact_person
                    updateArtefactCurrentStatePreparedStatement.setString(i++, contractInfoString); //comments
                    if (trTypeSatelliteOrProvider) {
                        updateArtefactCurrentStatePreparedStatement.setLong(i++, trType.equals("Provider") ? 22 : 23); //EQUIPMENT_TYPE
                    } else if (fe_equipment_type != null ) {
                        updateArtefactCurrentStatePreparedStatement.setLong(i++, fe_equipment_type.longValue()); //equipment_type
                    } else {
                        updateArtefactCurrentStatePreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    updateArtefactCurrentStatePreparedStatement.setLong(i++, 3); //gs_status
                    updateArtefactCurrentStatePreparedStatement.setString(i++, cn_comments); //pl_comments
                    if(inst_comments!=null){
                        updateArtefactCurrentStatePreparedStatement.setString(i++, inst_comments); //inst_comments
                    } else {
                        updateArtefactCurrentStatePreparedStatement.setNull(i++, Types.VARCHAR);
                    }
                    if (createdArtefactId != null) {
                        updateArtefactCurrentStatePreparedStatement.setLong(i++, createdArtefactId); //ARTEFACTID
                    } else {
                        updateArtefactCurrentStatePreparedStatement.setNull(i++, Types.BIGINT);
                    }

                    log.info("preparedStatement.executeUpdate()");
                    updateArtefactCurrentStatePreparedStatement.executeUpdate();
                    log.info("successfull insert to database!");

                    //UPDATE ARTEFACT_RSD
                    log.info("UPDATE ARTEFACT_RSD");
                    String UpdateArtefactRsd = "update ARTEFACT_RSD SET BSCID = ?, ALTITUDE = ?, CNSTRTYPEID = ?, HEIGHT = ?, DATEOFINSERT = ?, DATEOFVISIT = ?, CONTACTPERSON = ?, COMMENTS = ?, DATEOFUPDATE = ?, RBSID = ?, SITE_TYPE = ? where RSDID = ?";
                    PreparedStatement updateArtefactRsdPreparedStatement = udbConnect.prepareStatement(UpdateArtefactRsd);

                    i = 1;
//
                    java.sql.Date dateOfVisitDate = date_of_visit != null ? new java.sql.Date(date_of_visit.getTimeInMillis()) : null;
                    // set values to update
                    if (cn_bscInt != null) {
                        updateArtefactRsdPreparedStatement.setLong(i++, cn_bscInt); //BSCID
                    } else {
                        updateArtefactRsdPreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (cn_altitude != null) {
                        updateArtefactRsdPreparedStatement.setLong(i++, cn_altitude.longValue()); //ALTITUDE
                    } else {
                        updateArtefactRsdPreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (cnConstructionTypeInt != null) {
                        updateArtefactRsdPreparedStatement.setLong(i++, cnConstructionTypeInt); //CNSTRTYPEID
                    } else {
                        updateArtefactRsdPreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (cn_height_constr != null) {
                        updateArtefactRsdPreparedStatement.setLong(i++, cn_height_constr.longValue()); // CONSTRUCTION_HEIGHT (cn_height_constr)
                    } else {
                        updateArtefactRsdPreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    updateArtefactRsdPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // INSERT_DATE
                    updateArtefactRsdPreparedStatement.setDate(i++, dateOfVisitDate); // DATE OF VISIT (cn_date_visit)
                    updateArtefactRsdPreparedStatement.setString(i++, contact_person); //CONTACTPERSON
                    updateArtefactRsdPreparedStatement.setString(i++, cn_comments); //COMMENTS
                    updateArtefactRsdPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // INSERT_DATE
                    if (rbsTypeInt != null) {
                        updateArtefactRsdPreparedStatement.setLong(i++, rbsTypeInt); //RBSID
                    } else {
                        updateArtefactRsdPreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (siteTypeInt != null) {
                        updateArtefactRsdPreparedStatement.setLong(i++, siteTypeInt); //SITE_TYPE
                    } else {
                        updateArtefactRsdPreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (createdArtefactRSDId != null) {
                        updateArtefactRsdPreparedStatement.setLong(i++, createdArtefactRSDId); //RSDID
                    } else {
                        updateArtefactRsdPreparedStatement.setNull(i++, Types.BIGINT);
                    }

                    log.info("preparedStatement.executeUpdate()");
                    updateArtefactRsdPreparedStatement.executeUpdate();
                    log.info("successfull insert to database!");

                    //UPDATE ARTEFACT_RR
                    log.info("UPDATE ARTEFACT_RR");
                    String UpdateArtefactRR = "UPDATE ARTEFACT_RR SET DATEOFVISIT = ?, ADDRESS = ?, LATITUDE = ?, LONGITUDE = ?, CONSTR_TYPE = ?, SQUARE = ?, RBS_TYPE = ?, BAND = ?, RBS_LOCATION = ?, COMMENTS = ? where RR_ID = ?";
                    PreparedStatement updateArtefactRRPreparedStatement = udbConnect.prepareStatement(UpdateArtefactRR);

                    i = 1;
                    log.info("preparedStatement.setValues");
                    // set values to update
                    updateArtefactRRPreparedStatement.setDate(i++, dateOfVisitDate); // DATEOFVISIT
                    updateArtefactRRPreparedStatement.setString(i++, cn_address); //ADDRESS
                    updateArtefactRRPreparedStatement.setString(i++, cn_latitude); //LATITUDE
                    updateArtefactRRPreparedStatement.setString(i++, cn_longitude); //LONGITUDE
                    if (cnConstructionTypeInt != null) {
                        updateArtefactRRPreparedStatement.setLong(i++, cnConstructionTypeInt); //CONSTR_TYPE
                    } else {
                        updateArtefactRRPreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (cn_square != null) {
                        updateArtefactRRPreparedStatement.setLong(i++, cn_square.longValue()); //SQUARE
                    } else {
                        updateArtefactRRPreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (rbsTypeInt != null) {
                        updateArtefactRRPreparedStatement.setLong(i++, rbsTypeInt); //RBS_TYPE
                    } else {
                        updateArtefactRRPreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    updateArtefactRRPreparedStatement.setString(i++, bandsIdForUDB); //BAND
                    if (cn_rbs_location != null) {
                        Integer cn_rbs_location_int = Integer.parseInt(cn_rbs_location);
                        updateArtefactRRPreparedStatement.setLong(i++, cn_rbs_location_int); //RBS_LOCATION
                    } else {
                        updateArtefactRRPreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    updateArtefactRRPreparedStatement.setString(i++, cn_comments); //COMMENTS
                    if (createdArtefactRRId != null) {
                        updateArtefactRRPreparedStatement.setLong(i++, createdArtefactRRId); //RR_ID
                    } else {
                        updateArtefactRRPreparedStatement.setNull(i++, Types.BIGINT);
                    }

                    log.info("preparedStatement.executeUpdate()");
                    updateArtefactRRPreparedStatement.executeUpdate();
                    log.info("successfull insert to database!");

                    //UPDATE ARTEFACT_TSD
                    log.info("UPDATE ARTEFACT_TSD");
                    String UpdateArtefactTSD = "update ARTEFACT_TSD_EXT set equipment_id = ?, ne_longitude = ?, ne_latitude = ?, fe_sitename = ?, fe_constr_type = ?, fe_address = ?, survey_date = ?, ne_azimuth = ?, ne_antennadiameter = ?, ne_suspensionheight = ?, ne_txrf_frequency = ?, fe_azimuth = ?, fe_antennadiameter = ?, fe_suspensionheight = ?, fe_txrf_frequency = ?, update_date = ?, fe_artefactid = ? where tsdid = ?";
                    PreparedStatement updateArtefactTSDPreparedStatement = udbConnect.prepareStatement(UpdateArtefactTSD);

                    i = 1;
                    if (trTypeSatelliteOrProvider) {
                        if(trType.equals("Provider")){
                            updateArtefactTSDPreparedStatement.setLong(i++, 22L); // equipment_id
                        } else if(trType.equals("Satellite")){
                            updateArtefactTSDPreparedStatement.setLong(i++, 23L); // equipment_id
                        } else {
                            updateArtefactTSDPreparedStatement.setNull(i++, Types.BIGINT);
                        }
                    } else if (fe_equipment_type != null) {
                        updateArtefactTSDPreparedStatement.setLong(i++, fe_equipment_type.longValue()); // equipment_id
                    } else {
                        updateArtefactTSDPreparedStatement.setNull(i++, Types.BIGINT);
                    }

                    if (ne_longitude != null) {
                        updateArtefactTSDPreparedStatement.setString(i++, "E " + ne_longitude.replace(".", ",")); // NE_LONGITUDE
                    } else {
                        updateArtefactTSDPreparedStatement.setNull(i++, Types.VARCHAR);
                    }

                    if (ne_latitude != null) {
                        updateArtefactTSDPreparedStatement.setString(i++, "N " + ne_latitude.replace(".", ",")); // NE_LATITUDE
                    } else {
                        updateArtefactTSDPreparedStatement.setNull(i++, Types.VARCHAR);
                    }

                    if (trTypeSatelliteOrProvider) {
                        updateArtefactTSDPreparedStatement.setString(i++, trType.equals("Provider") ? "Rented channel" : "Satellite"); // FE_SITENAME
                    } else if (fe_sitename != null) {
                        updateArtefactTSDPreparedStatement.setString(i++, fe_sitename); // FE_SITENAME
                    } else {
                        updateArtefactTSDPreparedStatement.setNull(i++, Types.VARCHAR);
                    }

                    if (trTypeSatelliteOrProvider) {
                        updateArtefactTSDPreparedStatement.setNull(i++, Types.BIGINT);
                    } else if (fe_constructionType != null) {
                        Integer fe_constructionTypeInt = Integer.parseInt(fe_constructionType);
                        if (fe_constructionTypeInt != null) {
                            updateArtefactTSDPreparedStatement.setLong(i++, fe_constructionTypeInt); // FE_CONSTR_TYPE
                        } else {
                            updateArtefactTSDPreparedStatement.setNull(i++, Types.BIGINT);
                        }
                    }
                    if (trTypeSatelliteOrProvider) {
                        updateArtefactTSDPreparedStatement.setNull(i++, Types.VARCHAR);
                    } else if (fe_address != null) {
                        updateArtefactTSDPreparedStatement.setString(i++, fe_address); // FE_ADDRESS
                    }
                    if (trTypeSatelliteOrProvider) {
                        updateArtefactTSDPreparedStatement.setNull(i++, Types.DATE);
                    } else if (fe_formated_survey_date != null) {
                        updateArtefactTSDPreparedStatement.setDate(i++, new java.sql.Date(fe_cal_survey_date.getTimeInMillis())); // SURVEY_DATE (fe_survey_date)
                    }
                    if (ne_azimuth != null) {
                        Float ne_azimuthFloat = Float.parseFloat(ne_azimuth);
                        if (ne_azimuthFloat != null) {
                            updateArtefactTSDPreparedStatement.setFloat(i++, ne_azimuthFloat); // NE_AZIMUTH
                        } else {
                            updateArtefactTSDPreparedStatement.setNull(i++, Types.BIGINT);
                        }
                    }
                    if (ne_diameter != null) {
                        updateArtefactTSDPreparedStatement.setFloat(i++, Float.parseFloat(ne_diameter)); // NE_ANTENNADIAMETER
                    }
                    if (ne_suspensionHeight != null) {
                        if (ne_suspensionHeight != null) {
                            updateArtefactTSDPreparedStatement.setLong(i++, ne_suspensionHeight.longValue()); // NE_SUSPENSIONHEIGHT
                        } else {
                            updateArtefactTSDPreparedStatement.setNull(i++, Types.BIGINT);
                        }
                    }
                    if (ne_frequencyBand != null) {
                        Integer ne_frequencyBandInt = Integer.parseInt(ne_frequencyBand);
                        if (ne_frequencyBandInt != null) {
                            updateArtefactTSDPreparedStatement.setLong(i++, ne_frequencyBandInt); // NE_TXRF_FREQUENCY
                        } else {
                            updateArtefactTSDPreparedStatement.setNull(i++, Types.BIGINT);
                        }
                    }
                    if (trTypeSatelliteOrProvider) {
                        updateArtefactTSDPreparedStatement.setNull(i++, Types.BIGINT);
                    } else if (fe_azimuth != null) {
                        Float fe_azimuthFloat = Float.parseFloat(fe_azimuth);
                        if (fe_azimuthFloat != null) {
                            updateArtefactTSDPreparedStatement.setFloat(i++, fe_azimuthFloat); // FE_AZIMUTH
                        } else {
                            updateArtefactTSDPreparedStatement.setNull(i++, Types.BIGINT);
                        }
                    }
                    if (trTypeSatelliteOrProvider) {
                        updateArtefactTSDPreparedStatement.setNull(i++, Types.FLOAT);
                    } else if (fe_diameter != null) {
                        updateArtefactTSDPreparedStatement.setFloat(i++, Float.parseFloat(fe_diameter)); // FE_ANTENNADIAMETER
                    } else {
                        updateArtefactTSDPreparedStatement.setNull(i++, Types.FLOAT);
                    }
                    if (trTypeSatelliteOrProvider) {
                        updateArtefactTSDPreparedStatement.setNull(i++, Types.BIGINT);
                    } else if (fe_suspensionHeight != null) {
                        Integer fe_suspensionHeightInt = Integer.parseInt(fe_suspensionHeight);
                        if (fe_suspensionHeightInt != null) {
                            updateArtefactTSDPreparedStatement.setLong(i++, fe_suspensionHeightInt); // FE_SUSPENSIONHEIGHT
                        } else {
                            updateArtefactTSDPreparedStatement.setNull(i++, Types.BIGINT);
                        }
                    }
                    if (trTypeSatelliteOrProvider) {
                        updateArtefactTSDPreparedStatement.setNull(i++, Types.BIGINT);
                    } else if (fe_frequencyBand != null) {
                        Integer fe_frequencyBandInt = Integer.parseInt(fe_frequencyBand);
                        if (fe_frequencyBandInt != null) {
                            updateArtefactTSDPreparedStatement.setLong(i++, fe_frequencyBandInt); // FE_TXRF_FREQUENCY
                        } else {
                            updateArtefactTSDPreparedStatement.setNull(i++, Types.BIGINT);
                        }
                    }
                    updateArtefactTSDPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // update_date

                    if (trTypeSatelliteOrProvider) {
                        updateArtefactTSDPreparedStatement.setNull(i++, Types.BIGINT);
                    } else if (fe_artefact_id != null && fe_artefact_id.longValue() > 0) {
                        updateArtefactTSDPreparedStatement.setLong(i++, fe_artefact_id.longValue()); // FE_ARTEFACTID
                    } else {
                        updateArtefactTSDPreparedStatement.setNull(i++, Types.BIGINT);
                    }

                    updateArtefactTSDPreparedStatement.setLong(i++, createdArtefactExtTSDId); //tsdid

                    log.info("updateArtefactTSDPreparedStatement.executeUpdate()");
                    updateArtefactTSDPreparedStatement.executeUpdate();
                    log.info("successfull insert to database!");

//                    //UPDATE NCP
//                    String UPDATE_ArtRRst_CREATION_RR_STATUS = "update ARTEFACT_RR_STATUS set RR_STATUS_ID = ?, DATEOFPERFORM = ? where ID = ?";
//                    PreparedStatement updateRRstatusInArtRRstPreparedStatement = udbConnect.prepareStatement(UPDATE_ArtRRst_CREATION_RR_STATUS);
//
//                    log.info("RR_STATUS preparedStatement SQL UPDATE VALUES");
//                    // set values to update
//                    i = 1;
//                    updateRRstatusInArtRRstPreparedStatement.setLong(i++, 2); // RR_STATUS
//                    updateRRstatusInArtRRstPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // DATEOFPERFORM
//                    updateRRstatusInArtRRstPreparedStatement.setLong(i++, createdArtefactRRStatusId); // Artefact RRStatus ID
//                    updateRRstatusInArtRRstPreparedStatement.executeUpdate();
//
//                    log.info("ArtefactID: " + createdArtefactId + " successfully RR_STATUS updated to 2");


                    //KWMS-940


                    // INSERT_ARTEFACT_RR_POWER
                    String INSERT_ARTEFACT_RR_POWER = "UPDATE ARTEFACT_RR_POWER SET LT_ID = ?, LANDLORD = ?, LANDLORD_CABLE_LENGTH = ?, LANDLORD_MONTHLY_PC = ?, RES_4KV = ?, RES_10KV = ? WHERE POWER_ID = ?";
                    PreparedStatement ARTEFACT_RR_POWER_PreparedStatement = udbConnect.prepareStatement(INSERT_ARTEFACT_RR_POWER);
                    log.info("INSERT INTO ARTEFACT_RR_POWER preparedStatement SQL UPDATE VALUES");

                    i = 1;
                    ARTEFACT_RR_POWER_PreparedStatement.setNull(i++, Types.BIGINT); //LT_ID
                    if (connectionPossibleFromRES != null) {
                        if (connectionPossibleFromRES.equals("yes")) {
                            ARTEFACT_RR_POWER_PreparedStatement.setLong(i++, 1); //LANDLORD
                        } else if (connectionPossibleFromRES.equals("no")) {
                            ARTEFACT_RR_POWER_PreparedStatement.setLong(i++, 0); //LANDLORD
                        } else {
                            ARTEFACT_RR_POWER_PreparedStatement.setNull(i++, Types.BIGINT);
                        }
                    } else {
                        ARTEFACT_RR_POWER_PreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (landlord_cable_length != null) {
                        Integer landlord_cable_lengthInt = Integer.parseInt(landlord_cable_length);
                        if (landlord_cable_lengthInt != null) {
                            ARTEFACT_RR_POWER_PreparedStatement.setLong(i++, landlord_cable_lengthInt); //LANDLORD_CABLE_LENGTH
                        } else {
                            ARTEFACT_RR_POWER_PreparedStatement.setNull(i++, Types.BIGINT);
                        }
                    } else {
                        ARTEFACT_RR_POWER_PreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (landlord_monthly_pc != null) {
                        if (landlord_monthly_pc.equals("Yes")){
                            ARTEFACT_RR_POWER_PreparedStatement.setLong(i++, 1); //LANDLORD_MONTHLY_PC
                        } else {
                            ARTEFACT_RR_POWER_PreparedStatement.setLong(i++, 0); //LANDLORD_MONTHLY_PC
                        }
                    } else {
                        ARTEFACT_RR_POWER_PreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (res_4kv != null) {
                        ARTEFACT_RR_POWER_PreparedStatement.setLong(i++, Integer.parseInt(res_4kv)); //RES_4KV
                    } else {
                        ARTEFACT_RR_POWER_PreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (res_10kv != null) {
                        ARTEFACT_RR_POWER_PreparedStatement.setLong(i++, Integer.parseInt(res_10kv)); //RES_10KV
                    } else {
                        ARTEFACT_RR_POWER_PreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    ARTEFACT_RR_POWER_PreparedStatement.setLong(i++, createdArtefactRrPowerId); //RR Power Id


                    ARTEFACT_RR_POWER_PreparedStatement.executeUpdate();
                    log.info("ARTEFACT_RR_POWER Successfully updated");

                    // INSERT INTO ARTEFACT_RR_RENTER CN
                    String UPDATE_ARTEFACT_RR_RENTER = "UPDATE ARTEFACT_RR_RENTER SET LEGAL_NAME = ?, LEGAL_ADDRESS = ?, PHONE_FAX = ?, LEADER_NAME = ?, LEADER_POSITION = ?, EMAIL = ?, CONTACT_NAME = ?, CONTACT_POSITION = ?, CONTACT_INFORMATION = ? WHERE RENTER_ID = ?";
                    PreparedStatement ARTEFACT_RR_RENTER_PreparedStatement = udbConnect.prepareStatement(UPDATE_ARTEFACT_RR_RENTER);
                    log.info("INSERT INTO ARTEFACT_RR_RENTER preparedStatement SQL UPDATE VALUES");

                    i = 1;
                    if (cn_legal_name != null) {
                        ARTEFACT_RR_RENTER_PreparedStatement.setString(i++, cn_legal_name); // LEGAL_NAME
                    } else {
                        ARTEFACT_RR_RENTER_PreparedStatement.setNull(i++, Types.VARCHAR);
                    }
                    if (cn_phone_fax != null) {
                        ARTEFACT_RR_RENTER_PreparedStatement.setString(i++, cn_phone_fax); // LEGAL_ADDRESS
                    } else {
                        ARTEFACT_RR_RENTER_PreparedStatement.setNull(i++, Types.VARCHAR);
                    }
                    if (cn_legal_address != null) {
                        ARTEFACT_RR_RENTER_PreparedStatement.setString(i++, cn_legal_address); // PHONE_FAX
                    } else {
                        ARTEFACT_RR_RENTER_PreparedStatement.setNull(i++, Types.VARCHAR);
                    }
                    if (cn_leader_name != null) {
                        ARTEFACT_RR_RENTER_PreparedStatement.setString(i++, cn_leader_name); // LEADER_NAME
                    } else {
                        ARTEFACT_RR_RENTER_PreparedStatement.setNull(i++, Types.VARCHAR);
                    }
                    if (cn_leader_position != null) {
                        ARTEFACT_RR_RENTER_PreparedStatement.setString(i++, cn_leader_position); // LEADER_POSITION
                    } else {
                        ARTEFACT_RR_RENTER_PreparedStatement.setNull(i++, Types.VARCHAR);
                    }
                    if (cn_email != null) {
                        ARTEFACT_RR_RENTER_PreparedStatement.setString(i++, cn_email); // EMAIL
                    } else {
                        ARTEFACT_RR_RENTER_PreparedStatement.setNull(i++, Types.VARCHAR);
                    }
                    if (cn_contact_name != null) {
                        ARTEFACT_RR_RENTER_PreparedStatement.setString(i++, cn_contact_name); // CONTACT_NAME
                    } else {
                        ARTEFACT_RR_RENTER_PreparedStatement.setNull(i++, Types.VARCHAR);
                    }
                    if (cn_contact_position != null) {
                        ARTEFACT_RR_RENTER_PreparedStatement.setString(i++, cn_contact_position); // CONTACT_POSITION
                    } else {
                        ARTEFACT_RR_RENTER_PreparedStatement.setNull(i++, Types.VARCHAR);
                    }
                    if (cn_contact_information != null) {
                        ARTEFACT_RR_RENTER_PreparedStatement.setString(i++, cn_contact_information); // CONTACT_INFORMATION
                    } else {
                        ARTEFACT_RR_RENTER_PreparedStatement.setNull(i++, Types.VARCHAR);
                    }
                    ARTEFACT_RR_RENTER_PreparedStatement.setLong(i++, createdArtefactRrRenterCnId); //RenterCnId

                    ARTEFACT_RR_RENTER_PreparedStatement.executeUpdate();
                    log.info("Successfully inserted");

                    // INSERT INTO ARTEFACT_RR_RENTER FE
                    String INSERT_ARTEFACT_RR_RENTER_FE = "UPDATE ARTEFACT_RR_RENTER SET LEGAL_NAME = ?, LEGAL_ADDRESS = ?, PHONE_FAX = ?, LEADER_NAME = ?, LEADER_POSITION = ?, EMAIL = ?, CONTACT_NAME = ?, CONTACT_POSITION = ?, CONTACT_INFORMATION = ? WHERE RENTER_ID = ?";
                    PreparedStatement ARTEFACT_RR_RENTER_FE_PreparedStatement = udbConnect.prepareStatement(INSERT_ARTEFACT_RR_RENTER_FE);
                    log.info("INSERT INTO ARTEFACT_RR_RENTER preparedStatement SQL UPDATE VALUES");

                    i = 1;
                    if (fe_legal_name != null) {
                        ARTEFACT_RR_RENTER_FE_PreparedStatement.setString(i++, fe_legal_name); // LEGAL_NAME
                    } else {
                        ARTEFACT_RR_RENTER_FE_PreparedStatement.setNull(i++, Types.VARCHAR);
                    }
                    if (fe_phone_fax != null) {
                        ARTEFACT_RR_RENTER_FE_PreparedStatement.setString(i++, fe_legal_address ); // LEGAL_ADDRESS
                    } else {
                        ARTEFACT_RR_RENTER_FE_PreparedStatement.setNull(i++, Types.VARCHAR);
                    }
                    if (fe_address != null) {
                        ARTEFACT_RR_RENTER_FE_PreparedStatement.setString(i++, fe_phone_fax); // PHONE_FAX
                    } else {
                        ARTEFACT_RR_RENTER_FE_PreparedStatement.setNull(i++, Types.VARCHAR);
                    }
                    if (fe_leader_name != null) {
                        ARTEFACT_RR_RENTER_FE_PreparedStatement.setString(i++, fe_leader_name); // LEADER_NAME
                    } else {
                        ARTEFACT_RR_RENTER_FE_PreparedStatement.setNull(i++, Types.VARCHAR);
                    }
                    if (fe_leader_position != null) {
                        ARTEFACT_RR_RENTER_FE_PreparedStatement.setString(i++, fe_leader_position); // LEADER_POSITION
                    } else {
                        ARTEFACT_RR_RENTER_FE_PreparedStatement.setNull(i++, Types.VARCHAR);
                    }
                    if (fe_email != null) {
                        ARTEFACT_RR_RENTER_FE_PreparedStatement.setString(i++, fe_email); // EMAIL
                    } else {
                        ARTEFACT_RR_RENTER_FE_PreparedStatement.setNull(i++, Types.VARCHAR);
                    }
                    if (fe_contact_name != null) {
                        ARTEFACT_RR_RENTER_FE_PreparedStatement.setString(i++, fe_contact_name); // CONTACT_NAME
                    } else {
                        ARTEFACT_RR_RENTER_FE_PreparedStatement.setNull(i++, Types.VARCHAR);
                    }
                    if (fe_contact_position != null) {
                        ARTEFACT_RR_RENTER_FE_PreparedStatement.setString(i++, fe_contact_position); // CONTACT_POSITION
                    } else {
                        ARTEFACT_RR_RENTER_FE_PreparedStatement.setNull(i++, Types.VARCHAR);
                    }
                    if (fe_contact_information != null) {
                        ARTEFACT_RR_RENTER_FE_PreparedStatement.setString(i++, fe_contact_information); // CONTACT_INFORMATION
                    } else {
                        ARTEFACT_RR_RENTER_FE_PreparedStatement.setNull(i++, Types.VARCHAR);
                    }
                    ARTEFACT_RR_RENTER_FE_PreparedStatement.setLong(i++, createdArtefactRrRenterFeId); //RenterFeId

                    ARTEFACT_RR_RENTER_FE_PreparedStatement.executeUpdate();
                    log.info("Successfully inserted");

                    // --ARTEFACT_RR_TR
                    String UPDATE_ARTEFACT_RR_TR = "UPDATE ARTEFACT_RR_TR SET FE_NAME = ?, SURVEY_DATE = ?, FE_ADDRESS = ?, CONTACT_INFO = ?, SQUARE = ?, EQUIPMENT_TYPE = ?, DMTR = ?, ANTENNA_QUANTITY = ?, WEIGHT = ?, SUSPENSION_HEIGHT = ?, AZIMUTH = ?, CONSTR_TYPE = ?, COMMENTS = ?, RESULTS = ?, FE_ARTEFACTID = ? WHERE RR_TR_ID = ?";
                    PreparedStatement ARTEFACT_RR_TR_PreparedStatement = udbConnect.prepareStatement(UPDATE_ARTEFACT_RR_TR);
                    log.info("INSERT INTO ARTEFACT_RR_TR preparedStatement SQL UPDATE VALUES");

                    i = 1;
                    if (fe_name != null) {
                        ARTEFACT_RR_TR_PreparedStatement.setString(i++, fe_name ); // FE_NAME
                    } else {
                        ARTEFACT_RR_TR_PreparedStatement.setNull(i++, Types.VARCHAR);
                    }

                    if (fe_formated_survey_date != null) {
                        ARTEFACT_RR_TR_PreparedStatement.setDate(i++, new java.sql.Date(fe_cal_survey_date.getTimeInMillis())); // SURVEY_DATE (fe_survey_date)
                    } else {
                        ARTEFACT_RR_TR_PreparedStatement.setNull(i++, Types.DATE);
                    }

                    if (fe_address != null) {
                        ARTEFACT_RR_TR_PreparedStatement.setString(i++, fe_address); // FE_ADDRESS
                    } else {
                        ARTEFACT_RR_TR_PreparedStatement.setNull(i++, Types.VARCHAR);
                    }

                    if (fe_contact_information != null) {
                        ARTEFACT_RR_TR_PreparedStatement.setString(i++, fe_contact_information); // CONTACT_INFO
                    } else {
                        ARTEFACT_RR_TR_PreparedStatement.setNull(i++, Types.VARCHAR);
                    }

                    if (fe_square != null) {
                        ARTEFACT_RR_TR_PreparedStatement.setLong(i++, Integer.parseInt(fe_square)); // SQUARE
                    } else {
                        ARTEFACT_RR_TR_PreparedStatement.setNull(i++, Types.INTEGER);
                    }

//                    ARTEFACT_RR_TR_PreparedStatement.setLong(i++, fe_equipment_type.longValue()); // equipment_id
                    if (fe_equipment_type != null) {
                        ARTEFACT_RR_TR_PreparedStatement.setLong(i++, fe_equipment_type.longValue()); // equipment_id
                    } else {
                        ARTEFACT_RR_TR_PreparedStatement.setNull(i++, Types.INTEGER);
                    }

                    if (fe_diameter != null) {
                        ARTEFACT_RR_TR_PreparedStatement.setFloat(i++, Float.parseFloat(fe_diameter)); // DMTR fe.diameter
                    } else {
                        ARTEFACT_RR_TR_PreparedStatement.setNull(i++, Types.FLOAT);
                    }

                    if (fe_antennas_quantity != null) {
                        ARTEFACT_RR_TR_PreparedStatement.setLong(i++, Integer.parseInt(fe_antennas_quantity)); // ANTENNA_QUANTITY
                    } else {
                        ARTEFACT_RR_TR_PreparedStatement.setNull(i++, Types.INTEGER);
                    }

                    if (fe_weight != null) {
                        ARTEFACT_RR_TR_PreparedStatement.setLong(i++, Integer.parseInt(fe_weight)); // WEIGHT
                    } else {
                        ARTEFACT_RR_TR_PreparedStatement.setNull(i++, Types.INTEGER);
                    }

                    if (fe_suspension_height != null) {
                        ARTEFACT_RR_TR_PreparedStatement.setLong(i++, Integer.parseInt(fe_suspension_height)); // SUSPENSION_HEIGHT
                    } else {
                        ARTEFACT_RR_TR_PreparedStatement.setNull(i++, Types.INTEGER);
                    }

                    if (fe_azimuth != null) {
                        ARTEFACT_RR_TR_PreparedStatement.setFloat(i++, Float.parseFloat(fe_azimuth)); // AZIMUTH
                    } else {
                        ARTEFACT_RR_TR_PreparedStatement.setNull(i++, Types.INTEGER);
                    }

                    if (fe_constructionType != null) {
                        ARTEFACT_RR_TR_PreparedStatement.setLong(i++, Integer.parseInt(fe_constructionType)); // CONSTR_TYPE
                    } else {
                        ARTEFACT_RR_TR_PreparedStatement.setNull(i++, Types.INTEGER);
                    }

                    if (fe_comments != null) {
                        ARTEFACT_RR_TR_PreparedStatement.setString(i++, fe_comments); // COMMENTS
                    } else {
                        ARTEFACT_RR_TR_PreparedStatement.setNull(i++, Types.VARCHAR);
                    }

                    if (fe_results_visit_objects != null) {
                        ARTEFACT_RR_TR_PreparedStatement.setString(i++, fe_results_visit_objects); // RESULTS
                    } else {
                        ARTEFACT_RR_TR_PreparedStatement.setNull(i++, Types.VARCHAR);
                    }

                    if (fe_artefact_id != null && fe_artefact_id.longValue() > 0) {
                        ARTEFACT_RR_TR_PreparedStatement.setLong(i++, fe_artefact_id.longValue()); // FE_ARTEFACTID
                    } else {
                        ARTEFACT_RR_TR_PreparedStatement.setNull(i++, Types.BIGINT);
                    }
//                    ARTEFACT_RR_TR_PreparedStatement.setNull(i++, Types.INTEGER); //FE_ARTEFACTID
                    ARTEFACT_RR_TR_PreparedStatement.setLong(i++, createdArtefactRrTrId); //createdArtefactRrTrId


                    ARTEFACT_RR_TR_PreparedStatement.executeUpdate();
                    log.info("Successfully inserted");

                    // --ARTEFACT_RR_TR_ANTENNA
                    String UPDATE_ARTEFACT_RR_TR_ANTENNA = "UPDATE ARTEFACT_RR_TR_ANTENNA SET RR_ID = ?, EQUIP_ID = ?, ANTENNA_QUANTITY = ?, FREQ_BAND = ?, DMTR = ?, WEIGHT = ?, SUSPENSION_HEIGHT = ?, AZIMUTH = ? WHERE TR_ANTENNA_ID = ?";
                    PreparedStatement ARTEFACT_RR_TR_ANTENNA_PreparedStatement = udbConnect.prepareStatement(UPDATE_ARTEFACT_RR_TR_ANTENNA);
                    log.info("INSERT INTO ARTEFACT_RR_TR_ANTENNA preparedStatement SQL UPDATE VALUES");

                    i = 1;
                    ARTEFACT_RR_TR_ANTENNA_PreparedStatement.setLong(i++, createdArtefactRRId);// RR_ID
//                    if (fe_equipment_type != null) {
//                        if (fe_equipment_type.equals("TN2P")){
//                            ARTEFACT_RR_TR_ANTENNA_PreparedStatement.setLong(i++, 2); // EQUIP_ID fe.equipmentType
//                        } else if (fe_equipment_type.equals("TN20P")){
//                            ARTEFACT_RR_TR_ANTENNA_PreparedStatement.setLong(i++, 6); // EQUIP_ID fe.equipmentType
//                        } else if (fe_equipment_type.equals("TN6P")){
//                            ARTEFACT_RR_TR_ANTENNA_PreparedStatement.setLong(i++, 4); // EQUIP_ID fe.equipmentType
//                        } else {
//                            ARTEFACT_RR_TR_ANTENNA_PreparedStatement.setNull(i++, Types.INTEGER); // EQUIP_ID
//                        }
//                    } else {
//                        ARTEFACT_RR_TR_ANTENNA_PreparedStatement.setNull(i++, Types.INTEGER); // EQUIP_ID
//                    }
                    if (fe_equipment_type != null) {
                        ARTEFACT_RR_TR_ANTENNA_PreparedStatement.setLong(i++, fe_equipment_type.longValue()); // equipment_id
                    } else {
                        ARTEFACT_RR_TR_ANTENNA_PreparedStatement.setNull(i++, Types.INTEGER);
                    }
                    if (fe_antennas_quantity != null) {
                        ARTEFACT_RR_TR_ANTENNA_PreparedStatement.setLong(i++, Integer.parseInt(fe_antennas_quantity)); // ANTENNA_QUANTITY
                    } else {
                        ARTEFACT_RR_TR_ANTENNA_PreparedStatement.setNull(i++, Types.INTEGER);
                    }

                    if (fe_frequencyBand != null) {
                        Integer fe_frequencyBandInt = Integer.parseInt(fe_frequencyBand);
                        if (fe_frequencyBandInt != null) {
                            ARTEFACT_RR_TR_ANTENNA_PreparedStatement.setLong(i++, fe_frequencyBandInt); // FE_TXRF_FREQUENCY
                        } else {
                            ARTEFACT_RR_TR_ANTENNA_PreparedStatement.setNull(i++, Types.BIGINT);
                        }
                    } else {
                        ARTEFACT_RR_TR_ANTENNA_PreparedStatement.setNull(i++, Types.BIGINT);
                    }

                    if (fe_diameter != null) {
                        ARTEFACT_RR_TR_ANTENNA_PreparedStatement.setFloat(i++, Float.parseFloat(fe_diameter)); // DMTR fe.diameter
                    } else {
                        ARTEFACT_RR_TR_ANTENNA_PreparedStatement.setNull(i++, Types.FLOAT);
                    }

                    if (fe_weight != null) {
                        ARTEFACT_RR_TR_ANTENNA_PreparedStatement.setLong(i++, Integer.parseInt(fe_weight)); // WEIGHT
                    } else {
                        ARTEFACT_RR_TR_ANTENNA_PreparedStatement.setNull(i++, Types.INTEGER);
                    }

                    if (fe_suspension_height != null) {
                        ARTEFACT_RR_TR_ANTENNA_PreparedStatement.setLong(i++, Integer.parseInt(fe_suspension_height)); // SUSPENSION_HEIGHT
                    } else {
                        ARTEFACT_RR_TR_ANTENNA_PreparedStatement.setNull(i++, Types.INTEGER);
                    }

                    if (fe_azimuth != null) {
                        ARTEFACT_RR_TR_ANTENNA_PreparedStatement.setFloat(i++, Float.parseFloat(fe_azimuth)); // AZIMUTH
                    } else {
                        ARTEFACT_RR_TR_ANTENNA_PreparedStatement.setNull(i++, Types.INTEGER);
                    }
                    ARTEFACT_RR_TR_ANTENNA_PreparedStatement.setLong(i++, createdTrAntennaId); // createdTrAntennaId
                    //

                    ARTEFACT_RR_TR_ANTENNA_PreparedStatement.executeUpdate();
                    log.info("Successfully inserted");


                    // --ARTEFACT_RSD_HISTORY
                    String UPDATE_ARTEFACT_RSD_HISTORY = "UPDATE ARTEFACT_RSD_HISTORY SET RSDID = ?, ARTEFACTID = ?, BSCID = ?, ALTITUDE = ?, CNSTRTYPEID = ?, HEIGHT = ?, DATEOFINSERT = ?, DATEOFVISIT = ?, PLANNER = ?, CONTACTPERSON = ?, COMMENTS = ?, LASTEDITOR = ?, RBSID = ?, SITE_TYPE = ?, PLANNING_TARGET = ? WHERE ID = ?";
                    PreparedStatement ARTEFACT_RSD_HISTORY_PreparedStatement = udbConnect.prepareStatement(UPDATE_ARTEFACT_RSD_HISTORY);
                    log.info("INSERT INTO ARTEFACT_RSD_HISTORY preparedStatement SQL UPDATE VALUES");

                    i = 1;
                    ARTEFACT_RSD_HISTORY_PreparedStatement.setLong(i++, createdArtefactRSDId);// RSDID
                    ARTEFACT_RSD_HISTORY_PreparedStatement.setLong(i++, createdArtefactId);// ARTEFACTID
                    if (cn_bscInt != null) {
                        ARTEFACT_RSD_HISTORY_PreparedStatement.setLong(i++, cn_bscInt); //BSCID
                    } else {
                        ARTEFACT_RSD_HISTORY_PreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (cn_altitude != null) {
                        ARTEFACT_RSD_HISTORY_PreparedStatement.setLong(i++, cn_altitude.longValue()); // ALTITUDE (cn_altitude)
                    } else {
                        ARTEFACT_RSD_HISTORY_PreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (cn_constructionType != null) {
                        ARTEFACT_RSD_HISTORY_PreparedStatement.setLong(i++, Integer.parseInt(cn_constructionType)); //CNSTRTYPEID
                    } else {
                        ARTEFACT_RSD_HISTORY_PreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (cn_height_constr != null) {
                        ARTEFACT_RSD_HISTORY_PreparedStatement.setLong(i++, cn_height_constr.longValue()); // CONSTRUCTION_HEIGHT (cn_height_constr)
                    } else {
                        ARTEFACT_RSD_HISTORY_PreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    ARTEFACT_RSD_HISTORY_PreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // INSERT_DATE
                    if (date_of_visit != null) {
                        ARTEFACT_RSD_HISTORY_PreparedStatement.setDate(i++, new java.sql.Date(date_of_visit.getTimeInMillis())); //DATE OF VISIT (cn_date_visit)
                    } else {
                        ARTEFACT_RSD_HISTORY_PreparedStatement.setNull(i++, Types.DATE);
                    }
                    ARTEFACT_RSD_HISTORY_PreparedStatement.setString(i++, starter); // starter need change to current user // PLANNER
                    ARTEFACT_RSD_HISTORY_PreparedStatement.setString(i++, contact_person); //CONTACTPERSON
                    ARTEFACT_RSD_HISTORY_PreparedStatement.setString(i++, cn_comments); // PL_COMMENTS (cn_comments)
                    ARTEFACT_RSD_HISTORY_PreparedStatement.setString(i++, starter); // starter need change to current user // LASTEDITOR
                    if (rbsTypeInt != null) {
                        ARTEFACT_RSD_HISTORY_PreparedStatement.setLong(i++, rbsTypeInt); //RBSID
                    } else {
                        ARTEFACT_RSD_HISTORY_PreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (siteTypeInt != null) {
                        ARTEFACT_RSD_HISTORY_PreparedStatement.setLong(i++, siteTypeInt); //SITE_TYPE
                    } else {
                        ARTEFACT_RSD_HISTORY_PreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    ARTEFACT_RSD_HISTORY_PreparedStatement.setString(i++, targetCoverage); // PLANNING_TARGET

//                    ARTEFACT_RSD_HISTORY_PreparedStatement.setLong(i++, createdRsdHistoryId); //createdRsdHistoryId
                    ARTEFACT_RSD_HISTORY_PreparedStatement.setNull(i++, Types.BIGINT); //createdRsdHistoryId

                    ARTEFACT_RSD_HISTORY_PreparedStatement.executeUpdate();
                    log.info("Successfully inserted");

                    ////

                    udbConnect.commit();
                    udbConnect.close();
                    log.info("udbConnection closed!");
                } else {
                    udbConnect.close();
                    log.info("Failed to make connection!");
                }
            } catch (Exception e) {
                udbConnect.rollback();
                udbConnect.close();
                log.info("connection Exception!");
                log.info(e.toString());
                throw e;
            }
        } catch (SQLException e) {
            log.info("testConnect SQLException!");
            log.info(e.toString());
            log.warning("SQL State: %s\n%s");
            log.warning(e.getSQLState());
            log.warning(e.getMessage());
            throw e;
        } catch (Exception e) {
            log.info("testConnect Exception!");
            e.printStackTrace();
            throw e;
        }

    }
}
