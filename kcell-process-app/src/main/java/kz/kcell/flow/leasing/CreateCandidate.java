package kz.kcell.flow.leasing;

import kz.kcell.flow.files.Minio;
import lombok.extern.java.Log;
import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.camunda.spin.Spin.JSON;

@Log
@Service("CreateCandidate")
public class CreateCandidate implements JavaDelegate {

    private Minio minioClient;

    @Autowired
    DataSource dataSource;

    @Autowired
    public CreateCandidate(Minio minioClient) {
        this.minioClient = minioClient;
    }

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
            log.info(udbOracleUrl);
            log.info(udbOracleUsername);
            log.info(udbOraclePassword);
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
                    String ncpId = delegateExecution.getVariable("ncpID") != null ? delegateExecution.getVariable("ncpID").toString() : null;
                    Long ncpCreatedId = delegateExecution.getVariable("ncpCreatedId") != null ? (Long) delegateExecution.getVariable("ncpCreatedId") : null;
//                    String region = delegateExecution.getVariable("region").toString(); // not number
                    String longitude = delegateExecution.getVariable("longitude") != null ? delegateExecution.getVariable("longitude").toString() : null;
                    String latitude = delegateExecution.getVariable("latitude") != null ? delegateExecution.getVariable("latitude").toString() : null;
//                    String reason = delegateExecution.getVariable("reason").toString(); // it's string value, not dictionaries id
                    String starter = delegateExecution.getVariable("starter") != null ? delegateExecution.getVariable("starter").toString() : null;
                    String targetCell = delegateExecution.getVariable("createNCPTaskComment") != null ? delegateExecution.getVariable("createNCPTaskComment").toString() : null;
                    String targetCoverage = delegateExecution.getVariable("targetCoverage") != null ? delegateExecution.getVariable("targetCoverage").toString() : null;
                    String regionCode = delegateExecution.getVariable("regionCode") != null ? delegateExecution.getVariable("regionCode").toString() : null;
                    String rbsType = delegateExecution.getVariable("rbsTypeId") != null ? delegateExecution.getVariable("rbsTypeId").toString() : null;
                    Integer rbsTypeInt = Integer.parseInt(rbsType);
                    String createNCPTaskComment = delegateExecution.getVariable("createNCPTaskComment") != null ? delegateExecution.getVariable("createNCPTaskComment").toString() : null;
                    String bandsIdForUDB = delegateExecution.getVariable("bandsIdForUDB") != null ? delegateExecution.getVariable("bandsIdForUDB").toString() : null;
                    String plannedCabinetTypeIdForUDB = delegateExecution.getVariable("plannedCabinetTypeIdForUDB") != null ? delegateExecution.getVariable("plannedCabinetTypeIdForUDB").toString() : null;

                    SpinJsonNode createNewCandidateSiteFilesJSON = JSON(delegateExecution.getVariable("createNewCandidateSiteFiles"));
                    SpinList createNewCandidateSiteFiles = createNewCandidateSiteFilesJSON.elements();

                    Integer reasonInt = null;
                    SpinJsonNode reasonJson = delegateExecution.getVariable("reason") != null ? JSON(delegateExecution.getVariable("reason")) : null;
                    if (reasonJson != null) {
                        try {
                            SpinJsonNode reason = reasonJson.prop("id");
                            String reasonString = reason.value().toString();
                            reasonInt = Integer.parseInt(reasonString);
                        } catch (Exception e) {
                            log.warning(e.getMessage());
                        }
                    }

                    Integer siteTypeInt = null;
                    SpinJsonNode siteTypeJson = delegateExecution.getVariable("siteType") != null ? JSON(delegateExecution.getVariable("siteType")) : null;
                    if (siteTypeJson != null) {
                        try {
                            SpinJsonNode siteType = siteTypeJson.prop("id");
                            String siteTypeString = siteType.value().toString();
                            siteTypeInt = Integer.parseInt(siteTypeString);
                        } catch (Exception e) {
                            log.warning(e.getMessage());
                        }
                    }

                    Integer projectInt = null;
                    SpinJsonNode projectJson = delegateExecution.getVariable("project") != null ? JSON(delegateExecution.getVariable("project")) : null;
                    if (projectJson != null) {
                        try {
                            SpinJsonNode project = projectJson.prop("id");
                            String projectString = project.value().toString();
                            projectInt = Integer.parseInt(projectString);
                        } catch (Exception e) {
                            log.warning(e.getMessage());
                        }
                    }

                    Integer initiatorInt = null;
                    SpinJsonNode initiatorJson = delegateExecution.getVariable("initiator") != null ? JSON(delegateExecution.getVariable("initiator")) : null;
                    if (initiatorJson != null) {
                        try {
                            SpinJsonNode initiator = initiatorJson.prop("id");
                            String initiatorString = initiator.value().toString();
                            initiatorInt = Integer.parseInt(initiatorString);
                        } catch (Exception e) {
                            log.warning(e.getMessage());
                        }
                    }

                    Integer partInt = null;
                    SpinJsonNode partJson = delegateExecution.getVariable("part") != null ? JSON(delegateExecution.getVariable("part")) : null;
                    try {
                        SpinJsonNode part = partJson.prop("id");
                        String partString = part.value().toString();
                        partInt = Integer.parseInt(partString);
                    } catch (Exception e) {
                        log.warning(e.getMessage());
                    }

                    SpinJsonNode powerSource = delegateExecution.hasVariable("powerSource") ? JSON(delegateExecution.getVariable("powerSource")) : null;
                    SpinList ps_lt_ids = powerSource != null && powerSource.hasProp("cableLayingType") && powerSource.prop("cableLayingType") != null ? powerSource.prop("cableLayingType").elements() : null;
                    SpinJsonNode ps_lt_id = ps_lt_ids != null ? (SpinJsonNode) ps_lt_ids.get(0) : null;
//                    String ps_lt_id = powerSource != null && powerSource.hasProp("cableLayingType") && powerSource.prop("cableLayingType") != null ? powerSource.prop("cableLayingType").value().toString() : null;
                    String landlord_cable_length = powerSource != null && powerSource.hasProp("cableLength") && powerSource.prop("cableLength").value() != null ? (powerSource.prop("cableLength").value().toString()) : null;
                    String landlord_monthly_pc = powerSource != null && powerSource.hasProp("agreeToReceiveMonthlyPayment") && powerSource.prop("agreeToReceiveMonthlyPayment").value() != null ? (powerSource.prop("agreeToReceiveMonthlyPayment").value().toString()) : null;
                    String res_4kv = powerSource != null && powerSource.hasProp("closestPublic04") && powerSource.prop("closestPublic04").value() != null ? (powerSource.prop("closestPublic04").value().toString()) : null;
                    String res_10kv = powerSource != null && powerSource.hasProp("closestPublic10") && powerSource.prop("closestPublic10").value() != null ? (powerSource.prop("closestPublic10").value().toString()) : null;

                    SpinJsonNode candidate = delegateExecution.getVariable("candidate") != null ? JSON(delegateExecution.getVariable("candidate")) : null;
                    String cn_longitude = candidate != null && candidate.hasProp("longitude") && candidate.prop("longitude") != null ? (candidate.prop("longitude").value().toString()) : null;
                    String cn_latitude = candidate != null && candidate.hasProp("latitude") && candidate.prop("latitude") != null ? (candidate.prop("latitude").value().toString()) : null;
                    String cn_siteName = candidate != null && candidate.hasProp("siteName") && candidate.prop("siteName") != null ? (candidate.prop("siteName").value().toString()) : null;
                    String cn_comments = candidate != null ? (candidate.hasProp("comments") && candidate.prop("comments") != null ? candidate.prop("comments").value().toString() : null) : null;
                    Number cn_square = candidate != null && candidate.hasProp("square") && candidate.prop("square") != null ? (candidate.prop("square").numberValue()) : null;
                    String cn_rbs_location = candidate != null && candidate.hasProp("rbsLocation") && candidate.prop("rbsLocation") != null && candidate.prop("rbsLocation").hasProp("id") && candidate.prop("rbsLocation").prop("id") != null ? (candidate.prop("rbsLocation").prop("id").value().toString()) : null;
                    String cn_constructionType = candidate != null ? (candidate.hasProp("constructionType") && candidate.prop("constructionType").hasProp("id") ? candidate.prop("constructionType").prop("id").value().toString() : "0") : null;

                    String cn_assigned_user = candidate != null ? (candidate.hasProp("assignedUser") ? candidate.prop("assignedUser").value().toString() : "") : null;

                    Number cn_height_constr = (candidate != null && candidate.hasProp("cn_height_constr") && candidate.prop("cn_height_constr") != null) ? Integer.parseInt(candidate.prop("cn_height_constr").value().toString()) : 0;
                    Number cn_altitude = candidate != null && candidate.hasProp("cn_altitude") && candidate.prop("cn_altitude") != null ? Integer.parseInt(candidate.prop("cn_altitude").value().toString()) : 0;
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX"); //2020-01-02T18:00:00.000Z
                    String cn_date_of_visit = candidate != null && candidate.hasProp("dateOfVisit") && candidate.prop("dateOfVisit") != null ? (candidate.prop("dateOfVisit").value().toString()) : null;
                    Date date_of_visit_date = cn_date_of_visit != null ? formatter.parse(cn_date_of_visit) : null;
                    Calendar date_of_visit = Calendar.getInstance();
                    date_of_visit.setTime(date_of_visit_date);
                    date_of_visit.add(Calendar.HOUR_OF_DAY, 6);


                    //{"antennaType":"ML 0.6","diameter":"0.6","weight":"14","antennaQuantity":1,"frequencyBand":"8GHz","suspensionHeight":12,"azimuth":"123"}
                    SpinJsonNode ne = delegateExecution.getVariable("transmissionAntenna") != null ? JSON(delegateExecution.getVariable("transmissionAntenna")) : null;
                    String ne_azimuth = ne != null ? (ne.hasProp("azimuth") ? ne.prop("azimuth").value().toString() : "0") : null;
                    String ne_diameter = ne != null ? (ne.hasProp("diameter") ? ne.prop("diameter").value().toString().replaceAll("[^(\\d.\\d)]", "") : "0") : null;
                    String ne_frequencyBand = ne != null && ne.hasProp("frequencyBand") ? ne.prop("frequencyBand").value().toString().replaceAll("[^0-9.]", "") : null;
                    Number ne_suspensionHeight = ne != null && ne.hasProp("suspensionHeight") ? (ne.prop("suspensionHeight").numberValue()) : null;
                    String ne_longitude = ne != null && ne.hasProp("address") && ne.prop("address").hasProp("longitude") ? (ne.prop("address").prop("longitude").value().toString()) : cn_longitude != null ? cn_longitude : null;
                    String ne_latitude = ne != null && ne.hasProp("address") && ne.prop("address").hasProp("latitude") ? (ne.prop("address").prop("latitude").value().toString()) : cn_latitude != null ? cn_latitude : null;


//                    farEndInformation

                    SpinJsonNode feJson = delegateExecution.getVariable("farEndInformation") != null ? JSON(delegateExecution.getVariable("farEndInformation")) : null;
                    SpinList farEnds = feJson != null ? feJson.elements() : null;
                    SpinJsonNode fe = farEnds != null && farEnds.size()>0 ? (SpinJsonNode) farEnds.get(0) : null;

                    Number fe_artefact_id = 0;

                    String fe_azimuth = fe != null && fe.hasProp("azimuth") ? (fe.prop("azimuth").value().toString()) : null;
                    String fe_diameter = fe != null && fe.hasProp("diameter") ? (fe.prop("diameter").value().toString().replaceAll("[^(\\d.\\d)]", "")) : null;
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
                    String fe_equipment_type = fe != null && fe.hasProp("equipmentType") && fe.prop("equipmentType").value() != null  ? fe.prop("equipmentType").value().toString() : null;

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

                    String contact_person = renterCompany != null ? ("" + (renterCompany.hasProp("contactName") && !renterCompany.prop("contactName").equals(null) ? renterCompany.prop("contactName").value().toString() : "") +
                        " " + (renterCompany.hasProp("contactPosition") && !renterCompany.prop("contactPosition").equals(null) ? renterCompany.prop("contactPosition").value().toString() : "") +
                        " ( tel:" + (renterCompany.hasProp("telFax") && !renterCompany.prop("telFax").equals(null) ? renterCompany.prop("telFax").value().toString() : "") + ")") : null;

                    //{"cn_addr_oblast":"область Акмолинская","cn_addr_district":"","cn_addr_city":"г. Кокшетау","cn_addr_street":"фывфв","ca_not_full_addres":false,"cn_addr_building":"фывыфв"}
                    SpinJsonNode address = delegateExecution.getVariable("address") != null ? JSON(delegateExecution.getVariable("address")) : null;
                    String cn_address = address != null ? ("" + (address.hasProp("cn_addr_oblast") && address.prop("cn_addr_oblast") != null ? address.prop("cn_addr_oblast").value().toString() : "") +
                        (address.hasProp("cn_addr_district") && address.prop("cn_addr_district") != null ? ", " + address.prop("cn_addr_district").value().toString() : "") +
                        (address.hasProp("cn_addr_city") && address.prop("cn_addr_city") != null ? ", " + address.prop("cn_addr_city").value().toString() : "") +
                        (address.hasProp("cn_addr_street") && address.prop("cn_addr_street") != null ? ", " + address.prop("cn_addr_street").value().toString() : "") +
                        (address.hasProp("cn_addr_building") && address.prop("cn_addr_building") != null ? ", " + address.prop("cn_addr_building").value().toString() : "") +
                        (address.hasProp("cn_addr_cadastral_number") && address.prop("cn_addr_cadastral_number") != null ? ", " + address.prop("cn_addr_cadastral_number").value().toString() : "") +
                        (address.hasProp("cn_addr_note") && address.prop("cn_addr_note") != null ? ", " + address.prop("cn_addr_note").value().toString() : "")) : null;

                    String cn_bsc = candidate != null ? (candidate.hasProp("bsc") ? (candidate.prop("bsc").hasProp("id") ? candidate.prop("bsc").prop("id").value().toString() : null) : null) : null;
                    String trType = candidate != null ? (candidate.hasProp("transmissionType") ? candidate.prop("transmissionType").value().toString() : null) : null;
                    Integer cn_bscInt = cn_bsc != null ? Integer.parseInt(cn_bsc) : null;


//                    //insert NCP
//                    String returnCols[] = {"ARTEFACTID"};
//                    String insertNCP = "INSERT INTO NCP_CREATION ( ARTEFACTID, NCPID, TARGET_CELL, REGION, LONGITUDE, LATITUDE, REASON, PROJECT, CREATOR, DATEOFINSERT, COMMENTS, CABINETID, TARGET_COVERAGE, TYPE, GEN_STATUS, TR_STATUS, NCP_STATUS, NCP_STATUS_DATE, BAND, INITIATOR, PART) VALUES (NCP_CREATION_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, ?, ?, ?, ?, 7, 0, 1, SYSDATE, ?, ?, ?)";
//                    PreparedStatement preparedStatement = udbConnect.prepareStatement(insertNCP, returnCols);
//
//                    i = 1;
//                    log.info("preparedStatement.setValues");
//                    // set values to insert
//                    preparedStatement.setString(i++, ncpId); // NCPID
//                    Integer regionCodeInt = Integer.parseInt(regionCode);
//                    if (targetCell != null) {
//                        preparedStatement.setString(i++, targetCell); // target_cell
//                    } else {
//                        preparedStatement.setNull(i++, Types.VARCHAR);
//                    }
//                    if (regionCodeInt != null) {
//                        preparedStatement.setLong(i++, regionCodeInt); // REGION
//                    } else {
//                        preparedStatement.setNull(i++, Types.BIGINT);
//                    }
//                    preparedStatement.setString(i++, longitude); // LONGITUDE ex. E 76,890775
//                    preparedStatement.setString(i++, latitude); // LATITUDE
//                    if (reasonInt != null) {
//                        preparedStatement.setLong(i++, reasonInt); // REASON
//                    } else {
//                        preparedStatement.setNull(i++, Types.BIGINT);
//
//                    }
//                    if (projectInt != null) {
//                        preparedStatement.setLong(i++, projectInt); // PROJECT
//                    } else {
//                        preparedStatement.setNull(i++, Types.BIGINT);
//                    }
//                    preparedStatement.setString(i++, starter); // CREATOR 'SERGEI.ZAITSEV'
////                    preparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // DATEOFINSERT
//                    preparedStatement.setString(i++, createNCPTaskComment); // COMMENTS
//                    Integer plannedCabinetTypeIdForUDBInt = Integer.parseInt(plannedCabinetTypeIdForUDB);
//                    if (rbsTypeInt != null) {
//                        preparedStatement.setLong(i++, rbsTypeInt); // CABINETID
//                    } else {
//                        preparedStatement.setNull(i++, Types.BIGINT);
//                    }
//                    preparedStatement.setString(i++, targetCoverage); // TARGET_COVERAGE
//                    if (siteTypeInt != null) {
//                        preparedStatement.setLong(i++, siteTypeInt); // TYPE ncp_type (Подставлять ID согласно справочнику Site Type) ex: 6
//                    } else {
//                        preparedStatement.setNull(i++, Types.BIGINT);
//                    }
////                    preparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // NCP_STATUS_DATE
//                    preparedStatement.setString(i++, bandsIdForUDB); // BAND ex:'1'   ncp_band	(Подставлять ID согласно справочнику Bands)
//                    if (initiatorInt != null) {
//                        preparedStatement.setLong(i++, initiatorInt); // INITIATOR
//                    } else {
//                        preparedStatement.setNull(i++, Types.BIGINT);
//                    }
//                    if (partInt != null) {
//                        preparedStatement.setLong(i++, partInt); // PART (Подставлять ID согласно справочнику Part)
//                    } else {
//                        preparedStatement.setNull(i++, Types.BIGINT);
//                    }
//
//                    log.info("preparedStatement.executeUpdate()");
//                    preparedStatement.executeUpdate();
//                    log.info("successfull insert to database!");
////
//                    ResultSet headGeneratedIdResultSet = preparedStatement.getGeneratedKeys();
//                    headGeneratedIdResultSet.next();
//                    Long ncpCreatedId = headGeneratedIdResultSet.getLong(1);
//                    log.info("artefactGeneratedId:");
//                    log.info(ncpCreatedId.toString());
//
//                    //insert new status
//                    Long createdNcpStatusId = null;
//                    String returnStatus[] = {"STATUS_ACTION_ID"};
//                    String insertNewStatus = "insert into NCP_CREATION_STATUS_ACTION values ( NCP_CREATION_STATUS_ACTIO_SEQ.nextval, ?, 2, ?, SYSDATE, null)";
//                    PreparedStatement newNcpStatusPreparedStatement = udbConnect.prepareStatement(insertNewStatus, returnStatus);
//
//                    i = 1;
//                    log.info("newNcpStatusPreparedStatement.setString");
//                    newNcpStatusPreparedStatement.setLong(i++, ncpCreatedId);
//                    newNcpStatusPreparedStatement.setString(i++, starter); // CREATOR 'SERGEI.ZAITSEV'
////                    newNcpStatusPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // DATEOFINSERT
//                    log.info("newNcpStatusPreparedStatement.executeUpdate()");
//                    newNcpStatusPreparedStatement.executeUpdate();
//                    log.info("successfull insert to database!");
//
//                    ResultSet statusGeneratedIdResultSet = newNcpStatusPreparedStatement.getGeneratedKeys();
//                    statusGeneratedIdResultSet.next();
//                    createdNcpStatusId = statusGeneratedIdResultSet.getLong(1);
//                    log.info("createdNcpStatusId:");
//                    log.info(createdNcpStatusId.toString());

                    //insert new Candidate (in Artefact table)
                    Long createdArtefactId = null;
                    String returnArtefactId[] = {"ARTEFACTID"};
                    String insertNewArtefact = "insert into ARTEFACT values (ARTEFACT_SEQ.nextval, ?, ?)";
                    PreparedStatement newArtefactPreparedStatement = udbConnect.prepareStatement(insertNewArtefact, returnArtefactId);

                    i = 1;
                    log.info("newArtefactPreparedStatement.setString");
                    newArtefactPreparedStatement.setString(i++, cn_siteName); // sitename	cn_sitename
                    Integer ncpIdInt = Integer.parseInt(ncpId);
                    if (ncpCreatedId != null) {
                        newArtefactPreparedStatement.setLong(i++, ncpCreatedId); //ncp_id (old value: ncpIdInt)
                    } else {
                        newArtefactPreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    log.info("newArtefactPreparedStatement.executeUpdate()");
                    newArtefactPreparedStatement.executeUpdate();
                    log.info("successfull insert to database!");

                    ResultSet createdArtefactIdResultSet = newArtefactPreparedStatement.getGeneratedKeys();
                    createdArtefactIdResultSet.next();
                    createdArtefactId = createdArtefactIdResultSet.getLong(1);
                    log.info("createdArtefactId:");
                    log.info(createdArtefactId.toString());

                    //update ncp_creation (set candidate_id)

                    //UPDATE ARTEFACT
                    log.info("update NCP_CREATION set CANDIDATE_ID...");
                    String UPDATENCP = "update NCP_CREATION set CANDIDATE_ID = ? where ARTEFACTID = ?";
                    PreparedStatement updatePreparedStatement = udbConnect.prepareStatement(UPDATENCP);

                    i = 1;
                    // set values to update
                    updatePreparedStatement.setLong(i++, createdArtefactId); // NCPID
                    updatePreparedStatement.setLong(i++, ncpCreatedId); // NCPID

                    updatePreparedStatement.executeUpdate();
                    log.info("successfull updated database!");

                    //insert new Candidate (in ARTEFACT_CURRENT_STATE table)
                    String insertNewArtefactCurrentState = "INSERT INTO ARTEFACT_CURRENT_STATE (ARTEFACTID,\n" +
                        "                                                        NCPID,\n" +
                        "                                                        EQUIPMENT_TYPE,\n" +
                        "                                                        RSD_EXIST,\n" +
                        "                                                        TSD_EXIST,\n" +
                        "                                                        CAND_STATUS,\n" +
                        "                                                        CAND_STATUS_PERSON,\n" +
                        "                                                        CAND_STATUS_DATE,\n" +
                        "                                                        RR_STATUS,\n" +
                        "                                                        RR_STATUS_PERSON,\n" +
                        "                                                        RR_STATUS_DATE,\n" +
                        "                                                        LONGITUDE,\n" +
                        "                                                        LATITUDE,\n" +
                        "                                                        RBS_TYPE,\n" +
                        "                                                        BSC,\n" +
                        "                                                        BAND,\n" +
                        "                                                        RBS_LOCATION,\n" +
                        "                                                        ALTITUDE,\n" +
                        "                                                        CONSTRUCTION_HEIGHT,\n" +
                        "                                                        CONSTRUCTION_TYPE,\n" +
                        "                                                        ADDRESS,\n" +
                        "                                                        CONTACT_PERSON,\n" +
                        "                                                        COMMENTS,\n" +
                        "                                                        INSERT_DATE,\n" +
                        "                                                        INSERT_PERSON,\n" +
                        "                                                        GS_STATUS,\n" +
                        "                                                        POWER_STATUS,\n" +
                        "                                                        FE_STATUS,\n" +
                        "                                                        PL_COMMENTS) VALUES (?, ?, ?, ?, ?, ?, ?, SYSDATE, ?, ?, SYSDATE, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, ?, ?, 0, ?, ?)";
                    PreparedStatement newArtefactCurrentStatePreparedStatement = udbConnect.prepareStatement(insertNewArtefactCurrentState);

                    i = 1;
                    log.info("newArtefactCurrentStatePreparedStatement.setString");
                    if (createdArtefactId != null) {
                        newArtefactCurrentStatePreparedStatement.setLong(i++, createdArtefactId); //ARTEFACTID
                    } else {
                        newArtefactCurrentStatePreparedStatement.setNull(i++, Types.BIGINT);

                    }
                    if (ncpIdInt != null) {
                        newArtefactCurrentStatePreparedStatement.setLong(i++, ncpCreatedId); //ncp_id (old value: ncpIdInt)
                    } else {
                        newArtefactCurrentStatePreparedStatement.setNull(i++, Types.BIGINT);

                    }
                    if (trType != null && ( trType.equals("Provider") || trType.equals("Satellite"))) {
                        newArtefactCurrentStatePreparedStatement.setLong(i++, trType.equals("Provider") ? 22 : 23); //EQUIPMENT_TYPE
                    } else {
                        newArtefactCurrentStatePreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    newArtefactCurrentStatePreparedStatement.setLong(i++, 0); // RSD_EXIST first insert value mast be = 1
                    newArtefactCurrentStatePreparedStatement.setLong(i++, 0); // TSD_EXIST first insert value mast be = 1
                    newArtefactCurrentStatePreparedStatement.setLong(i++, 1); // CAND_STATUS first insert value mast be = 1

                    if (cn_assigned_user != null) {
                        newArtefactCurrentStatePreparedStatement.setString(i++, cn_assigned_user);
                    } else {
                        newArtefactCurrentStatePreparedStatement.setNull(i++, Types.VARCHAR);
                    }
                     // CAND_STATUS_PERSON (current user) // current or start ?????? (who complete create candidate task?)
//                    newArtefactCurrentStatePreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // CAND_STATUS_DATE
                    newArtefactCurrentStatePreparedStatement.setLong(i++, 1); // RR_STATUS first insert value mast be = 1
                    if (cn_assigned_user != null) { // RR_STATUS_PERSON (current user)
                        newArtefactCurrentStatePreparedStatement.setString(i++, cn_assigned_user);
                    } else {
                        newArtefactCurrentStatePreparedStatement.setNull(i++, Types.VARCHAR);
                    }
//                    newArtefactCurrentStatePreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // RR_STATUS_DATE
                    newArtefactCurrentStatePreparedStatement.setString(i++, "E " + cn_longitude.replace(".", ",")); // LONGITUDE ex: E 80 50 42,4
                    newArtefactCurrentStatePreparedStatement.setString(i++, "N " + cn_latitude.replace(".", ",")); // LATITUDE ex: N 48 48 05,6
                    if (rbsTypeInt != null) {
                        newArtefactCurrentStatePreparedStatement.setLong(i++, rbsTypeInt); // RBS_TYPE (cn_rbs_type)
                    } else {
                        newArtefactCurrentStatePreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (cn_bscInt != null) {
                        newArtefactCurrentStatePreparedStatement.setLong(i++, cn_bscInt); // BSC (cn_bsc)
                    } else {
                        newArtefactCurrentStatePreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    Integer bandsIdForUDBInt = Integer.parseInt(bandsIdForUDB);
                    if (bandsIdForUDBInt != null) {
                        newArtefactCurrentStatePreparedStatement.setLong(i++, bandsIdForUDBInt); // BAND (cn_band)
                    } else {
                        newArtefactCurrentStatePreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (cn_rbs_location != null) {
                        Integer cn_rbs_location_int = Integer.parseInt(cn_rbs_location);
                        newArtefactCurrentStatePreparedStatement.setLong(i++, cn_rbs_location_int); // RBS_LOCATION (cn_rbs_location) //
                    } else {
                        newArtefactCurrentStatePreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (cn_altitude != null) {
                        newArtefactCurrentStatePreparedStatement.setLong(i++, cn_altitude.longValue()); // ALTITUDE (cn_altitude)
                    } else {
                        newArtefactCurrentStatePreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (cn_height_constr != null) {
                        newArtefactCurrentStatePreparedStatement.setLong(i++, cn_height_constr.longValue()); // CONSTRUCTION_HEIGHT (cn_height_constr)
                    } else {
                        newArtefactCurrentStatePreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (cn_constructionType != null) {
                        newArtefactCurrentStatePreparedStatement.setLong(i++, Integer.parseInt(cn_constructionType)); // CONSTRUCTION_TYPE (cn_construction_type)
                    } else {
                        newArtefactCurrentStatePreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    newArtefactCurrentStatePreparedStatement.setString(i++, cn_address); // ADDRESS "Восточно-Казахстанская область, Зайсанский район, село Карабулак, водонапорная башня"
                    newArtefactCurrentStatePreparedStatement.setString(i++, contact_person); // CONTACT_PERSON "Мария Николаевна Специалист акимата 8 701 479 19 86"
                    newArtefactCurrentStatePreparedStatement.setString(i++, renterCompany != null ? ((renterCompany.hasProp("contactInfo") && !renterCompany.prop("contactInfo").equals(null) ? renterCompany.prop("contactInfo").value().toString() : "")) : null); // COMMENTS (cn_contact_information)
//                    newArtefactCurrentStatePreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // INSERT_DATE
                    if (cn_assigned_user != null) { // INSERT_PERSON (current user)
                        newArtefactCurrentStatePreparedStatement.setString(i++, cn_assigned_user);
                    } else {
                        newArtefactCurrentStatePreparedStatement.setNull(i++, Types.VARCHAR);
                    }
//                    newArtefactCurrentStatePreparedStatement.setLong(i++, 7); // GS_STATUS
                    newArtefactCurrentStatePreparedStatement.setNull(i++, Types.BIGINT); //GS_STATUS
                    if (trType != null && ( trType.equals("Provider") || trType.equals("Satellite"))) {
                        newArtefactCurrentStatePreparedStatement.setLong(i++, 3); //FE_STATUS
                    } else {
                        newArtefactCurrentStatePreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    newArtefactCurrentStatePreparedStatement.setString(i++, cn_comments); // PL_COMMENTS (cn_comments)
                    log.info("newArtefactCurrentStatePreparedStatement.executeUpdate()");
                    newArtefactCurrentStatePreparedStatement.executeUpdate();
                    log.info("successfull insert to database!");

                    //insert new installation

                    String insertNewInstallation = "insert into INSTALLATION_CURRENT_STATE(ARTEFACTID, CONTRACTOR_STATUS) values (?, ?)";
                    PreparedStatement NewInstallationPreparedStatement = udbConnect.prepareStatement(insertNewInstallation);

                    i = 1;
                    log.info("NewInstallationPreparedStatement.setString");
                    if (createdArtefactId != null) {
                        NewInstallationPreparedStatement.setLong(i++, createdArtefactId);
                    } else {
                        NewInstallationPreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    NewInstallationPreparedStatement.setLong(i++, 0);
                    log.info("NewInstallationPreparedStatement.executeUpdate()");
                    NewInstallationPreparedStatement.executeUpdate();
                    log.info("successfull insert to database!");

                    //insert new Candidate (in ARTEFACT_RSD table)
                    Long createdArtefactRSDId = null;
                    String rsdReturnStatus[] = {"RSDID"};
                    String insertNewArtefactRSD = "INSERT INTO ARTEFACT_RSD (RSDID, ARTEFACTID, BSCID, CNSTRTYPEID, HEIGHT, DATEOFINSERT, DATEOFVISIT, CONTACTPERSON, COMMENTS, STATE, RBSID, SITE_TYPE) VALUES ( ARTEFACT_RSD_SEQ.nextval, ?, ?, ?, ?, SYSDATE, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement newArtefactRSDPreparedStatement = udbConnect.prepareStatement(insertNewArtefactRSD, rsdReturnStatus);

                    i = 1;
                    log.info("newArtefactRSDPreparedStatement.setString");
                    if (createdArtefactId != null) {
                        newArtefactRSDPreparedStatement.setLong(i++, createdArtefactId); //ARTEFACTID
                    } else {
                        newArtefactRSDPreparedStatement.setNull(i++, Types.BIGINT);

                    }
                    if (cn_bscInt != null) {
                        newArtefactRSDPreparedStatement.setLong(i++, cn_bscInt); //BSCID
                    } else {
                        newArtefactRSDPreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (cn_constructionType != null) {
                        newArtefactRSDPreparedStatement.setLong(i++, Integer.parseInt(cn_constructionType)); //CNSTRTYPEID
                    } else {
                        newArtefactRSDPreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (cn_height_constr != null) {
                        newArtefactRSDPreparedStatement.setLong(i++, cn_height_constr.longValue()); // CONSTRUCTION_HEIGHT
                    } else {
                        newArtefactRSDPreparedStatement.setNull(i++, Types.BIGINT);
                    }
//                    newArtefactRSDPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // INSERT_DATE
                    if (date_of_visit != null) {
                        newArtefactRSDPreparedStatement.setDate(i++, new java.sql.Date(date_of_visit.getTimeInMillis())); //DATE OF VISIT (cn_date_visit)
                    } else {
                        newArtefactRSDPreparedStatement.setNull(i++, Types.DATE);
                    }
                    newArtefactRSDPreparedStatement.setString(i++, contact_person); //CONTACTPERSON
                    newArtefactRSDPreparedStatement.setString(i++, cn_comments); //COMMENTS
                    newArtefactRSDPreparedStatement.setLong(i++, 2); //STATE
                    if (rbsTypeInt != null) {
                        newArtefactRSDPreparedStatement.setLong(i++, rbsTypeInt); //RBSID
                    } else {
                        newArtefactRSDPreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (siteTypeInt != null) {
                        newArtefactRSDPreparedStatement.setLong(i++, siteTypeInt); //SITE_TYPE
                    } else {
                        newArtefactRSDPreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    log.info("newArtefactRSDPreparedStatement.executeUpdate()");
                    newArtefactRSDPreparedStatement.executeUpdate();
                    log.info("successfull insert to database!");

                    ResultSet artefactRSDGeneratedIdResultSet = newArtefactRSDPreparedStatement.getGeneratedKeys();
                    artefactRSDGeneratedIdResultSet.next();
                    createdArtefactRSDId = artefactRSDGeneratedIdResultSet.getLong(1);
                    log.info("createdArtefactRSDId:");
                    log.info(createdArtefactRSDId.toString());

                    //insert new Candidate (in ARTEFACT_RR table)
                    Long createdArtefactRRId = null;
                    String rrReturnStatus[] = {"RR_ID"};
                    String insertNewArtefactRR = "INSERT INTO ARTEFACT_RR (RR_ID, ARTEFACTID, DATEOFVISIT, ADDRESS, LATITUDE, LONGITUDE, CONSTR_TYPE, SQUARE, RBS_TYPE, BAND, RBS_LOCATION, COMMENTS, DATEOFCREATION, CREATOR) VALUES (ARTEFACT_RR_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, ?)";
                    PreparedStatement newArtefactRRPreparedStatement = udbConnect.prepareStatement(insertNewArtefactRR, rrReturnStatus);

                    i = 1;
                    log.info("newArtefactRRPreparedStatement.setString");
                    if (createdArtefactId != null) {
                        newArtefactRRPreparedStatement.setLong(i++, createdArtefactId); //ARTEFACTID
                    } else {
                        newArtefactRRPreparedStatement.setNull(i++, Types.BIGINT);
                    }
//                    newArtefactRRPreparedStatement.setDate(i++, date_of_visit != null ? new java.sql.Date(date_of_visit.getTimeInMillis()) : null); // DATEOFVISIT
                    if (date_of_visit != null) {
                        newArtefactRRPreparedStatement.setDate(i++, new java.sql.Date(date_of_visit.getTimeInMillis())); //DATE OF VISIT (cn_date_visit)
                    } else {
                        newArtefactRRPreparedStatement.setNull(i++, Types.DATE);
                    }
                    newArtefactRRPreparedStatement.setString(i++, cn_address); //ADDRESS


                    if (cn_latitude != null) {
                        newArtefactRRPreparedStatement.setString(i++, "N " + cn_latitude.replace(".", ",")); //LATITUDE
                    } else {
                        newArtefactRRPreparedStatement.setNull(i++, Types.VARCHAR);
                    }
                    if (cn_longitude != null) {
                        newArtefactRRPreparedStatement.setString(i++, "E " + cn_longitude.replace(".", ",")); //LONGITUDE
                    } else {
                        newArtefactRRPreparedStatement.setNull(i++, Types.VARCHAR);

                    }
                    if (cn_constructionType != null) {
                        newArtefactRRPreparedStatement.setLong(i++, Integer.parseInt(cn_constructionType)); //CONSTR_TYPE
                    } else {
                        newArtefactRRPreparedStatement.setNull(i++, Types.BIGINT);

                    }
                    if (cn_square != null) {
                        newArtefactRRPreparedStatement.setLong(i++, cn_square.longValue()); //SQUARE
                    } else {
                        newArtefactRRPreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (rbsTypeInt != null) {
                        newArtefactRRPreparedStatement.setLong(i++, rbsTypeInt); //RBS_TYPE
                    } else {
                        newArtefactRRPreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    newArtefactRRPreparedStatement.setString(i++, bandsIdForUDB); //BAND
                    if (cn_rbs_location != null) {
                        Integer cn_rbs_location_int = Integer.parseInt(cn_rbs_location);
                        newArtefactRRPreparedStatement.setLong(i++, cn_rbs_location_int); //RBS_LOCATION
                    } else {
                        newArtefactRRPreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    newArtefactRRPreparedStatement.setString(i++, cn_comments); //COMMENTS
//                    newArtefactRRPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // DATEOFCREATION
                    if (cn_assigned_user != null) { // CREATOR
                        newArtefactRRPreparedStatement.setString(i++, cn_assigned_user);
                    } else {
                        newArtefactRRPreparedStatement.setNull(i++, Types.VARCHAR);
                    }
                    log.info("newArtefactRRPreparedStatement.executeUpdate()");
                    newArtefactRRPreparedStatement.executeUpdate();
                    log.info("successfull insert to database!");

                    ResultSet artefactRRGeneratedIdResultSet = newArtefactRRPreparedStatement.getGeneratedKeys();
                    artefactRRGeneratedIdResultSet.next();
                    createdArtefactRRId = artefactRRGeneratedIdResultSet.getLong(1);
                    log.info("createdArtefactRRId:");
                    log.info(createdArtefactRRId.toString());

                    //insert ARTEFACT_VSD
//                    Long createdArtefactVsdId = null;
//                    String vsdReturnStatus[] = {"VSDID"};
//                    String insertNewVsd = "INSERT INTO ARTEFACT_VSD (VSDID, ARTEFACTID, RSDID) VALUES (ARTEFACT_VSD_SEQ.nextval, ?, ?)";
//                    PreparedStatement newArtefactVsdPreparedStatement = udbConnect.prepareStatement(insertNewVsd, vsdReturnStatus);
//
//                    i = 1;
//                    log.info("newArtefactVsdPreparedStatement.setString");
//                    if (createdArtefactId != null) {
//                        newArtefactVsdPreparedStatement.setLong(i++, createdArtefactId);
//                    } else {
//                        newArtefactVsdPreparedStatement.setNull(i++, Types.BIGINT);
//
//                    }
//                    if (createdArtefactRSDId != null) {
//                        newArtefactVsdPreparedStatement.setLong(i++, createdArtefactRSDId);
//                    } else {
//                        newArtefactVsdPreparedStatement.setNull(i++, Types.BIGINT);
//                    }
//                    log.info("newArtefactVsdPreparedStatement.executeUpdate()");
//                    newArtefactVsdPreparedStatement.executeUpdate();
//                    log.info("successfull insert to database!");
//
//                    ResultSet vsdGeneratedIdResultSet = newArtefactVsdPreparedStatement.getGeneratedKeys();
//                    vsdGeneratedIdResultSet.next();
//                    createdArtefactVsdId = vsdGeneratedIdResultSet.getLong(1);
//                    log.info("createdArtefactVsdId:");
//                    log.info(createdArtefactVsdId.toString());

                    //insert CANDAPPROVAL
                    Long createdCandApprovalId = null;
                    String candApprovalReturnStatus[] = {"ID"};
                    String insertNewCandApproval = "insert into CANDAPPROVAL(ID, ARTEFACTID, RSDID, STATUSID, DESDATE, APPROVER) values (CANDAPPROVAL_SEQ.nextval, ?, ?, ?, SYSDATE, ?)";
                    PreparedStatement newCandApprovalPreparedStatement = udbConnect.prepareStatement(insertNewCandApproval, candApprovalReturnStatus);

                    i = 1;
                    log.info("newCandApprovalPreparedStatement.setString");
                    if (createdArtefactId != null) {
                        newCandApprovalPreparedStatement.setLong(i++, createdArtefactId);
                    } else {
                        newCandApprovalPreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (createdArtefactRSDId != null) {
                        newCandApprovalPreparedStatement.setLong(i++, createdArtefactRSDId);
                    } else {
                        newCandApprovalPreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    newCandApprovalPreparedStatement.setLong(i++, 1);
//                    newCandApprovalPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime()));
                    newCandApprovalPreparedStatement.setString(i++, starter); //approver
                    log.info("newCandApprovalPreparedStatement.executeUpdate()");
                    newCandApprovalPreparedStatement.executeUpdate();
                    log.info("successfull insert to database!");

                    ResultSet candApprovalGeneratedIdResultSet = newCandApprovalPreparedStatement.getGeneratedKeys();
                    candApprovalGeneratedIdResultSet.next();
                    createdCandApprovalId = candApprovalGeneratedIdResultSet.getLong(1);
                    log.info("createdCandApprovalId:");
                    log.info(createdCandApprovalId.toString());

//                    //insert ARTEFACT_RR_STATUS
//                    Long createdArtefactRRStatusId = null;
//                    String artefactRRStatusReturnStatus[] = {"ID"};
//                    String insertNewArtefactRRStatus = "insert into ARTEFACT_RR_STATUS(ID, ARTEFACTID, RR_ID, DATEOFPERFORM) values (ARTEFACT_RR_STATUS_SEQ.nextval, ?, ?, SYSDATE)";
//                    PreparedStatement newArtefactRRStatusPreparedStatement = udbConnect.prepareStatement(insertNewArtefactRRStatus, artefactRRStatusReturnStatus);
//
//                    i = 1;
//                    log.info("newArtefactRRStatusPreparedStatement.setString");
//                    if (createdArtefactId != null) {
//                        newArtefactRRStatusPreparedStatement.setLong(i++, createdArtefactId);
//                    } else {
//                        newArtefactRRStatusPreparedStatement.setNull(i++, Types.BIGINT);
//                    }
//                    if (createdArtefactRRId != null) {
//                        newArtefactRRStatusPreparedStatement.setLong(i++, createdArtefactRRId);
//                    } else {
//                        newArtefactRRStatusPreparedStatement.setNull(i++, Types.BIGINT);
//                    }
////                    newArtefactRRStatusPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime()));
//                    log.info("newArtefactRRStatusPreparedStatement.executeUpdate()");
//                    newArtefactRRStatusPreparedStatement.executeUpdate();
//                    log.info("successfull insert to database!");
//
//                    ResultSet artefactRRStatusGeneratedIdResultSet = newArtefactRRStatusPreparedStatement.getGeneratedKeys();
//                    artefactRRStatusGeneratedIdResultSet.next();
//                    createdArtefactRRStatusId = artefactRRStatusGeneratedIdResultSet.getLong(1);
//                    log.info("createdArtefactRRStatusId:");
//                    log.info(createdArtefactRRStatusId.toString());

//                    udbConnect.commit();
                    //insert ARTEFACT_TSD_EXT
                    Long createdArtefactExtTSDId = null;
                    String artefactExtTSDReturnStatus[] = {"TSDID"};
                    StringBuilder insertNewArtefactExtTSDbuilder = new StringBuilder("INSERT INTO ARTEFACT_TSD_EXT (TSDID, ARTEFACTID, INSERT_DATE, INSERT_PERSON");
                    StringBuilder insertNewArtefactExtTSDbuilderValues = new StringBuilder(") VALUES (ARTEFACT_TSD_SEQ.nextval, ?, SYSDATE, ?");
                    if (trType != null && ( trType.equals("Provider") || trType.equals("Satellite"))) {
                        insertNewArtefactExtTSDbuilder.append(", EQUIPMENT_ID"); // EQUIPMENT_ID
                        insertNewArtefactExtTSDbuilderValues.append(", ?");
                    }
                    if (ne_longitude != null) {
                        insertNewArtefactExtTSDbuilder.append(", NE_LONGITUDE");
                        insertNewArtefactExtTSDbuilderValues.append(", ?");
                    }
                    if (ne_latitude != null) {
                        insertNewArtefactExtTSDbuilder.append(", NE_LATITUDE");
                        insertNewArtefactExtTSDbuilderValues.append(", ?");
                    }

                    if (trType != null && ( trType.equals("Provider") || trType.equals("Satellite"))){
                        insertNewArtefactExtTSDbuilder.append(", FE_SITENAME");
                        insertNewArtefactExtTSDbuilderValues.append(", ?");
                    } else if (fe_sitename != null) {
                        insertNewArtefactExtTSDbuilder.append(", FE_SITENAME");
                        insertNewArtefactExtTSDbuilderValues.append(", ?");
                    }
                    if (fe_constructionType != null && trType != "Provider" && trType != "Satellite") {
                        insertNewArtefactExtTSDbuilder.append(", FE_CONSTR_TYPE");
                        insertNewArtefactExtTSDbuilderValues.append(", ?");
                    }
                    if (fe_address != null && trType != "Provider" && trType != "Satellite") {
                        insertNewArtefactExtTSDbuilder.append(", FE_ADDRESS");
                        insertNewArtefactExtTSDbuilderValues.append(", ?");
                    }
                    if (fe_formated_survey_date != null && trType != "Provider" && trType != "Satellite") {
                        insertNewArtefactExtTSDbuilder.append(", SURVEY_DATE");
                        insertNewArtefactExtTSDbuilderValues.append(", ?");
                    }
                    if (ne_azimuth != null) {
                        insertNewArtefactExtTSDbuilder.append(", NE_AZIMUTH");
                        insertNewArtefactExtTSDbuilderValues.append(", ?");
                    }
                    if (ne_diameter != null) {
                        insertNewArtefactExtTSDbuilder.append(", NE_ANTENNADIAMETER");
                        insertNewArtefactExtTSDbuilderValues.append(", ?");
                    }
                    if (ne_suspensionHeight != null) {
                        insertNewArtefactExtTSDbuilder.append(", NE_SUSPENSIONHEIGHT");
                        insertNewArtefactExtTSDbuilderValues.append(", ?");
                    }
                    if (ne_frequencyBand != null) {
                        insertNewArtefactExtTSDbuilder.append(", NE_TXRF_FREQUENCY");
                        insertNewArtefactExtTSDbuilderValues.append(", ?");
                    }
                    if (fe_azimuth != null && trType != "Provider" && trType != "Satellite") {
                        insertNewArtefactExtTSDbuilder.append(", FE_AZIMUTH");
                        insertNewArtefactExtTSDbuilderValues.append(", ?");
                    }
                    if (fe_diameter != null && trType != "Provider" && trType != "Satellite") {
                        insertNewArtefactExtTSDbuilder.append(", FE_ANTENNADIAMETER");
                        insertNewArtefactExtTSDbuilderValues.append(", ?");
                    }
                    if (fe_suspensionHeight != null && trType != "Provider" && trType != "Satellite") {
                        insertNewArtefactExtTSDbuilder.append(", FE_SUSPENSIONHEIGHT");
                        insertNewArtefactExtTSDbuilderValues.append(", ?");
                    }
                    if (fe_frequencyBand != null && trType != "Provider" && trType != "Satellite") {
                        insertNewArtefactExtTSDbuilder.append(", FE_TXRF_FREQUENCY");
                        insertNewArtefactExtTSDbuilderValues.append(", ?");
                    }
                    if (fe_comment != null  && trType != "Provider" && trType != "Satellite") {
                        insertNewArtefactExtTSDbuilder.append(", COMMENTS");
                        insertNewArtefactExtTSDbuilderValues.append(", ?");
                    }
                    if (fe_artefact_id != null && fe_artefact_id.longValue() > 0 && trType != "Provider" && trType != "Satellite") {
                        insertNewArtefactExtTSDbuilder.append(", FE_ARTEFACTID");
                        insertNewArtefactExtTSDbuilderValues.append(", ?");
                    }
                    insertNewArtefactExtTSDbuilder.append(insertNewArtefactExtTSDbuilderValues.toString());
                    String insertNewArtefactExtTSD = insertNewArtefactExtTSDbuilder.toString() + ")";
                    PreparedStatement newArtefactExtTSDPreparedStatement = udbConnect.prepareStatement(insertNewArtefactExtTSD, artefactExtTSDReturnStatus);

                    i = 1;
                    log.info("newArtefactExtTSDPreparedStatement.setString");
                    if (createdArtefactId != null) {
                        newArtefactExtTSDPreparedStatement.setLong(i++, createdArtefactId); // ARTEFACTID
                    } else {
                        newArtefactExtTSDPreparedStatement.setNull(i++, Types.BIGINT);
                    }
//                    newArtefactExtTSDPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // INSERT_DATE
                    newArtefactExtTSDPreparedStatement.setString(i++, starter); // INSERT_PERSON

                    if (trType != null && ( trType.equals("Provider") || trType.equals("Satellite"))) {
                        newArtefactExtTSDPreparedStatement.setLong(i++, trType.equals("Provider") ? 22 : 23); //EQUIPMENT_ID
                    }
                    if (cn_longitude != null) {
                        newArtefactExtTSDPreparedStatement.setString(i++, "E " + ne_longitude.replace(".", ",")); // NE_LONGITUDE
                    }
                    if (cn_latitude != null) {
                        newArtefactExtTSDPreparedStatement.setString(i++, "N " + ne_latitude.replace(".", ",")); // NE_LATITUDE
                    }
                    if (trType != null && ( trType.equals("Provider") || trType.equals("Satellite"))){
                        newArtefactExtTSDPreparedStatement.setString(i++, trType.equals("Provider") ? "Rented channel" : "Satellite"); // FE_SITENAME
                    } else if (fe_sitename != null) {
                        newArtefactExtTSDPreparedStatement.setString(i++, fe_sitename); // FE_SITENAME
                    }
                    if (fe_constructionType != null && trType != "Provider" && trType != "Satellite") {
                        Integer fe_constructionTypeInt = Integer.parseInt(fe_constructionType);
                        if (fe_constructionTypeInt != null && trType != "Provider" && trType != "Satellite") {
                            newArtefactExtTSDPreparedStatement.setLong(i++, fe_constructionTypeInt); // FE_CONSTR_TYPE
                        } else {
                            newArtefactExtTSDPreparedStatement.setNull(i++, Types.BIGINT);
                        }
                    }
                    if (fe_address != null  && trType != "Provider" && trType != "Satellite") {
                        newArtefactExtTSDPreparedStatement.setString(i++, fe_address); // FE_ADDRESS
                    }
                    if (fe_formated_survey_date != null  && trType != "Provider" && trType != "Satellite") {
                        newArtefactExtTSDPreparedStatement.setDate(i++, new java.sql.Date(fe_cal_survey_date.getTimeInMillis())); // SURVEY_DATE (fe_survey_date)
                    }
                    if (ne_azimuth != null) {
                        Float ne_azimuthFloat = Float.parseFloat(ne_azimuth);
                        if (ne_azimuthFloat != null) {
                            newArtefactExtTSDPreparedStatement.setFloat(i++, ne_azimuthFloat); // NE_AZIMUTH
                        } else {
                            newArtefactExtTSDPreparedStatement.setNull(i++, Types.BIGINT);
                        }
                    }
                    if (ne_diameter != null) {
                        newArtefactExtTSDPreparedStatement.setFloat(i++, Float.parseFloat(ne_diameter)); // NE_ANTENNADIAMETER
                    }
                    if (ne_suspensionHeight != null) {
                        if (ne_suspensionHeight != null) {
                            newArtefactExtTSDPreparedStatement.setLong(i++, ne_suspensionHeight.longValue()); // NE_SUSPENSIONHEIGHT
                        } else {
                            newArtefactExtTSDPreparedStatement.setNull(i++, Types.BIGINT);
                        }
                    }
                    if (ne_frequencyBand != null) {
                        Integer ne_frequencyBandInt = Integer.parseInt(ne_frequencyBand);
                        if (ne_frequencyBandInt != null) {
                            newArtefactExtTSDPreparedStatement.setLong(i++, ne_frequencyBandInt); // NE_TXRF_FREQUENCY
                        } else {
                            newArtefactExtTSDPreparedStatement.setNull(i++, Types.BIGINT);
                        }
                    }
                    if (fe_azimuth != null && trType != "Provider" && trType != "Satellite") {
                        Float fe_azimuthFloat = Float.parseFloat(fe_azimuth);
                        if (fe_azimuthFloat != null) {
                            newArtefactExtTSDPreparedStatement.setFloat(i++, fe_azimuthFloat); // FE_AZIMUTH
                        } else {
                            newArtefactExtTSDPreparedStatement.setNull(i++, Types.BIGINT);
                        }
                    }
                    if (fe_diameter != null && trType != "Provider" && trType != "Satellite") {
                        newArtefactExtTSDPreparedStatement.setFloat(i++, Float.parseFloat(fe_diameter)); // FE_ANTENNADIAMETER
                    }
                    if (fe_suspensionHeight != null) {
                        Integer fe_suspensionHeightInt = Integer.parseInt(fe_suspensionHeight);
                        if (fe_suspensionHeightInt != null  && trType != "Provider" && trType != "Satellite") {
                            newArtefactExtTSDPreparedStatement.setLong(i++, fe_suspensionHeightInt); // FE_SUSPENSIONHEIGHT
                        } else {
                            newArtefactExtTSDPreparedStatement.setNull(i++, Types.BIGINT);
                        }
                    }
                    if (fe_frequencyBand != null) {
                        Integer fe_frequencyBandInt = Integer.parseInt(fe_frequencyBand);
                        if (fe_frequencyBandInt != null  && trType != "Provider" && trType != "Satellite") {
                            newArtefactExtTSDPreparedStatement.setLong(i++, fe_frequencyBandInt); // FE_TXRF_FREQUENCY
                        } else {
                            newArtefactExtTSDPreparedStatement.setNull(i++, Types.BIGINT);
                        }
                    }
                    if (fe_comment != null  && trType != "Provider" && trType != "Satellite") {
                        newArtefactExtTSDPreparedStatement.setString(i++, fe_comment); // COMMENTS
                    }
                    if (fe_artefact_id != null && fe_artefact_id.longValue() > 0  && trType != "Provider" && trType != "Satellite") {
                        newArtefactExtTSDPreparedStatement.setLong(i++, fe_artefact_id.longValue()); // fe_artefact_id
                    }
                    log.info("newArtefactExtTSDPreparedStatement.executeUpdate()");
                    newArtefactExtTSDPreparedStatement.executeUpdate();
                    log.info("successfull insert to database!");

                    ResultSet artefactExtTSDGeneratedIdResultSet = newArtefactExtTSDPreparedStatement.getGeneratedKeys();
                    artefactExtTSDGeneratedIdResultSet.next();
                    createdArtefactExtTSDId = artefactExtTSDGeneratedIdResultSet.getLong(1);
                    log.info("createdArtefactExtTSDId:");
                    log.info(createdArtefactExtTSDId.toString());

                    // KWMS-940 insert into new 7 tables:

                    Long createdArtefactRrPowerId = null;
                    String artefactRrPowerId[] = {"POWER_ID"};
                    String INSERT_ARTEFACT_RR_POWER = "INSERT INTO ARTEFACT_RR_POWER (POWER_ID, RR_ID, LT_ID, LANDLORD, LANDLORD_CABLE_LENGTH, LANDLORD_MONTHLY_PC, RES_4KV, RES_10KV) VALUES  (ARTEFACT_RR_POWER_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?)"; //(18144, 18744, 1, 1, 30, 1, null, null);
                    PreparedStatement ARTEFACT_RR_POWER_PreparedStatement = udbConnect.prepareStatement(INSERT_ARTEFACT_RR_POWER, artefactRrPowerId);
                    log.info("INSERT INTO ARTEFACT_RR_POWER preparedStatement SQL UPDATE VALUES");

                    i = 1;
                    ARTEFACT_RR_POWER_PreparedStatement.setLong(i++, createdArtefactRRId); //RR_ID
//                    if (fe_suspensionHeight != null) {
//                        Integer fe_suspensionHeightInt = Integer.parseInt(fe_suspensionHeight);
//                        if (fe_suspensionHeightInt != null) {
//                            newArtefactExtTSDPreparedStatement.setLong(i++, fe_suspensionHeightInt); //LT_ID
//                        } else {
//                            newArtefactExtTSDPreparedStatement.setNull(i++, Types.BIGINT);
//                        }
//                    }
//                    ARTEFACT_RR_POWER_PreparedStatement.setNull(i++, Types.BIGINT); //LT_ID
                    if (ps_lt_id != null) {
                        ARTEFACT_RR_POWER_PreparedStatement.setLong(i++, Integer.parseInt(ps_lt_id.prop("id").value().toString())); //LT_ID
                    } else {
                        ARTEFACT_RR_POWER_PreparedStatement.setNull(i++, Types.BIGINT); //LT_ID
                    }
                    ARTEFACT_RR_POWER_PreparedStatement.setNull(i++, Types.BIGINT); //LANDLORD
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


                    ARTEFACT_RR_POWER_PreparedStatement.executeUpdate();
                    log.info("Successfully inserted");

                    ResultSet createdArtefactRrPowerIdResultSet = ARTEFACT_RR_POWER_PreparedStatement.getGeneratedKeys();
                    createdArtefactRrPowerIdResultSet.next();
                    createdArtefactRrPowerId = createdArtefactRrPowerIdResultSet.getLong(1);
                    log.info("createdArtefactRrPowerId:");
                    log.info(createdArtefactRrPowerId.toString());


                // INSERT INTO ARTEFACT_RR_RENTER CN
                    Long createdArtefactRrRenterCnId = null;
                    String artefactRrRenterCnId[] = {"RENTER_ID"};
                    String INSERT_ARTEFACT_RR_RENTER = "INSERT INTO ARTEFACT_RR_RENTER (RENTER_ID, RR_ID, TYPE, LEGAL_NAME, LEGAL_ADDRESS, PHONE_FAX, LEADER_NAME, LEADER_POSITION, EMAIL, CONTACT_NAME, CONTACT_POSITION, CONTACT_INFORMATION) VALUES (ARTEFACT_RR_RENTER_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; //(13029, 10365, 1, 'ОРТПЦ (КазТелеРадио)', 'Almaty, st, Zheltoksana 38,                                                                             Uralsk city, st. Zheleznodorozhnaya 1', '71-75-73, 71-75-78, 71-75-72 (Almaty)', 'Efrimenko Viktor Pavlovich', 'Glavny injener', null, 'Sharafutdinov Serik                  ', null, '50-62-77, 50-35-90                     87112 51-39-32');
                    PreparedStatement ARTEFACT_RR_RENTER_PreparedStatement = udbConnect.prepareStatement(INSERT_ARTEFACT_RR_RENTER, artefactRrRenterCnId);
                    log.info("INSERT INTO ARTEFACT_RR_RENTER preparedStatement SQL UPDATE VALUES");

                    i = 1;
                    ARTEFACT_RR_RENTER_PreparedStatement.setLong(i++, createdArtefactRRId);// RR_ID
                    ARTEFACT_RR_RENTER_PreparedStatement.setLong(i++, 1);// TYPE
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

                    ARTEFACT_RR_RENTER_PreparedStatement.executeUpdate();
                    log.info("Successfully inserted");

                    ResultSet createdArtefactRrRenterCnIdResultSet = ARTEFACT_RR_RENTER_PreparedStatement.getGeneratedKeys();
                    createdArtefactRrRenterCnIdResultSet.next();
                    createdArtefactRrRenterCnId = createdArtefactRrRenterCnIdResultSet.getLong(1);
                    log.info("createdArtefactRrRenterCnId:");
                    log.info(createdArtefactRrRenterCnId.toString());


                // INSERT INTO ARTEFACT_RR_RENTER FE
                    Long createdArtefactRrRenterFeId = null;
                    String artefactRrRenterFeId[] = {"RENTER_ID"};
                    String INSERT_ARTEFACT_RR_RENTER_FE = "INSERT INTO ARTEFACT_RR_RENTER (RENTER_ID, RR_ID, TYPE, LEGAL_NAME, LEGAL_ADDRESS, PHONE_FAX, LEADER_NAME, LEADER_POSITION, EMAIL, CONTACT_NAME, CONTACT_POSITION, CONTACT_INFORMATION) VALUES (ARTEFACT_RR_RENTER_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; //(13029, 10365, 1, 'ОРТПЦ (КазТелеРадио)', 'Almaty, st, Zheltoksana 38,                                                                             Uralsk city, st. Zheleznodorozhnaya 1', '71-75-73, 71-75-78, 71-75-72 (Almaty)', 'Efrimenko Viktor Pavlovich', 'Glavny injener', null, 'Sharafutdinov Serik                  ', null, '50-62-77, 50-35-90                     87112 51-39-32');
                    PreparedStatement ARTEFACT_RR_RENTER_FE_PreparedStatement = udbConnect.prepareStatement(INSERT_ARTEFACT_RR_RENTER_FE, artefactRrRenterFeId);
                    log.info("INSERT INTO ARTEFACT_RR_RENTER preparedStatement SQL UPDATE VALUES");

                    i = 1;
                    ARTEFACT_RR_RENTER_FE_PreparedStatement.setLong(i++, createdArtefactRRId);// RR_ID
                    ARTEFACT_RR_RENTER_FE_PreparedStatement.setLong(i++, 2);// TYPE
                    if (fe_legal_name != null) {
                        ARTEFACT_RR_RENTER_FE_PreparedStatement.setString(i++, fe_legal_name); // LEGAL_NAME
                    } else {
                        ARTEFACT_RR_RENTER_FE_PreparedStatement.setNull(i++, Types.VARCHAR);
                    }
                    if (fe_address != null) {
                        ARTEFACT_RR_RENTER_FE_PreparedStatement.setString(i++, fe_address ); // LEGAL_ADDRESS
                    } else {
                        ARTEFACT_RR_RENTER_FE_PreparedStatement.setNull(i++, Types.VARCHAR);
                    }
                    if (fe_phone_fax != null) {
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

                    ARTEFACT_RR_RENTER_FE_PreparedStatement.executeUpdate();
                    log.info("Successfully inserted");


                    ResultSet createdArtefactRrRenterFeIdResultSet = ARTEFACT_RR_RENTER_FE_PreparedStatement.getGeneratedKeys();
                    createdArtefactRrRenterFeIdResultSet.next();
                    createdArtefactRrRenterFeId = createdArtefactRrRenterFeIdResultSet.getLong(1);
                    log.info("createdArtefactRrRenterFeId:");
                    log.info(createdArtefactRrRenterFeId.toString());


// --ARTEFACT_RR_TR
                    Long createdArtefactRrTrId = null;
                    String artefactRrTrId[] = {"RR_TR_ID"};
                    String INSERT_ARTEFACT_RR_TR = "INSERT INTO ARTEFACT_RR_TR (RR_TR_ID, RR_ID, FE_NAME, SURVEY_DATE, FE_ADDRESS, CONTACT_INFO, SQUARE, EQUIPMENT_TYPE, DMTR, ANTENNA_QUANTITY, WEIGHT, SUSPENSION_HEIGHT, AZIMUTH, CONSTR_TYPE, COMMENTS, RESULTS, FE_ARTEFACTID) VALUES (ARTEFACT_RR_TR_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; //(17902, 18222, '03029SHUHOSPIT', TO_DATE('2010-02-16 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), '(SHHOS) Жамбылская обл., г. Шу, ул. Сатпаева 151, "Региональная поликлиника"', 'Иманалиев Б. 8(72643)23272', null, 4, 0.3, 1, null, null, 13, 13, 201, 26, null, null, 15605);
                    PreparedStatement ARTEFACT_RR_TR_PreparedStatement = udbConnect.prepareStatement(INSERT_ARTEFACT_RR_TR, artefactRrTrId);
                    log.info("INSERT INTO ARTEFACT_RR_TR preparedStatement SQL UPDATE VALUES");

                    i = 1;
                    ARTEFACT_RR_TR_PreparedStatement.setLong(i++, createdArtefactRRId); // RR_ID
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

                    if (fe_equipment_type != null) {
                        if (fe_equipment_type.toUpperCase().equals("TN2P")){
                            ARTEFACT_RR_TR_PreparedStatement.setLong(i++, 2); // EQUIPMENT_TYPE fe.equipmentType
                        } else if (fe_equipment_type.toUpperCase().equals("TN20P")){
                            ARTEFACT_RR_TR_PreparedStatement.setLong(i++, 6); // EQUIPMENT_TYPE fe.equipmentType
                        } else if (fe_equipment_type.toUpperCase().equals("TN6P")){
                            ARTEFACT_RR_TR_PreparedStatement.setLong(i++, 4); // EQUIPMENT_TYPE fe.equipmentType
                        } else {
                            ARTEFACT_RR_TR_PreparedStatement.setNull(i++, Types.INTEGER);
                        }
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
//                        ARTEFACT_RR_TR_PreparedStatement.setLong(i++, Integer.parseInt(fe_constructionType)); // CONSTR_TYPE
                        ARTEFACT_RR_TR_PreparedStatement.setLong(i++, 141); // CONSTR_TYPE
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

                    if (fe_artefact_id != null && fe_artefact_id.longValue() > 0 ) {
                        ARTEFACT_RR_TR_PreparedStatement.setLong(i++, fe_artefact_id.longValue()); // FE_ARTEFACTID
                    } else {
                        ARTEFACT_RR_TR_PreparedStatement.setNull(i++, Types.INTEGER);
                    }
//                    ARTEFACT_RR_TR_PreparedStatement.setNull(i++, Types.INTEGER); //FE_ARTEFACTID


                    ARTEFACT_RR_TR_PreparedStatement.executeUpdate();
                    log.info("Successfully inserted");


                    ResultSet createdArtefactRrTrIdResultSet = ARTEFACT_RR_TR_PreparedStatement.getGeneratedKeys();
                    createdArtefactRrTrIdResultSet.next();
                    createdArtefactRrTrId = createdArtefactRrTrIdResultSet.getLong(1);
                    log.info("createdArtefactRrTrId:");
                    log.info(createdArtefactRrTrId.toString());


// --ARTEFACT_RR_TR_ANTENNA
                    Long createdTrAntennaId = null;
                    String trAntennaId[] = {"TR_ANTENNA_ID"};
                    String INSERT_ARTEFACT_RR_TR_ANTENNA = "INSERT INTO ARTEFACT_RR_TR_ANTENNA (TR_ANTENNA_ID, RR_ID, EQUIP_ID, ANTENNA_QUANTITY, FREQ_BAND, DMTR, WEIGHT, SUSPENSION_HEIGHT, AZIMUTH) VALUES (ARTEFACT_RR_TR_ANTENNA_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?)"; //(23968, 24828, 2, 1, '7', 0.6, 15, 14, 182);
                    PreparedStatement ARTEFACT_RR_TR_ANTENNA_PreparedStatement = udbConnect.prepareStatement(INSERT_ARTEFACT_RR_TR_ANTENNA, trAntennaId);
                    log.info("INSERT INTO ARTEFACT_RR_TR_ANTENNA preparedStatement SQL UPDATE VALUES");

                    i = 1;
                    ARTEFACT_RR_TR_ANTENNA_PreparedStatement.setLong(i++, createdArtefactRRId);// RR_ID
                    if (fe_equipment_type != null) {
                        if (fe_equipment_type.toUpperCase().equals("TN2P")){
                            ARTEFACT_RR_TR_ANTENNA_PreparedStatement.setLong(i++, 2); // EQUIP_ID fe.equipmentType
                        } else if (fe_equipment_type.toUpperCase().equals("TN20P")){
                            ARTEFACT_RR_TR_ANTENNA_PreparedStatement.setLong(i++, 6); // EQUIP_ID fe.equipmentType
                        } else if (fe_equipment_type.toUpperCase().equals("TN6P")){
                            ARTEFACT_RR_TR_ANTENNA_PreparedStatement.setLong(i++, 4); // EQUIP_ID fe.equipmentType
                        } else {
                            ARTEFACT_RR_TR_ANTENNA_PreparedStatement.setNull(i++, Types.INTEGER); // EQUIP_ID
                        }
                    } else {
                        ARTEFACT_RR_TR_ANTENNA_PreparedStatement.setNull(i++, Types.INTEGER); // EQUIP_ID
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

                    ARTEFACT_RR_TR_ANTENNA_PreparedStatement.executeUpdate();
                    log.info("Successfully inserted");

                    ResultSet createdTrAntennaIdResultSet = ARTEFACT_RR_TR_ANTENNA_PreparedStatement.getGeneratedKeys();
                    createdTrAntennaIdResultSet.next();
                    createdTrAntennaId = createdTrAntennaIdResultSet.getLong(1);
                    log.info("createdTrAntennaId:");
                    log.info(createdTrAntennaId.toString());


// --ARTEFACT_RSD_EXIST
                    String INSERT_ARTEFACT_RSD_EXIST = "INSERT INTO ARTEFACT_RSD_EXIST (RSDID, RSD_EXIST, ARTEFACTID) VALUES (?, ?, ?)"; //(38554, 0, 37614);
                    PreparedStatement ARTEFACT_RSD_EXIST_PreparedStatement = udbConnect.prepareStatement(INSERT_ARTEFACT_RSD_EXIST);
                    log.info("INSERT INTO ARTEFACT_RSD_EXIST preparedStatement SQL UPDATE VALUES");

                    i = 1;
                    ARTEFACT_RSD_EXIST_PreparedStatement.setLong(i++, createdArtefactRSDId);// RSDID
                    ARTEFACT_RSD_EXIST_PreparedStatement.setLong(i++, 0);// RSD_EXIST
                    ARTEFACT_RSD_EXIST_PreparedStatement.setLong(i++, createdArtefactId);// ARTEFACTID

                    ARTEFACT_RSD_EXIST_PreparedStatement.executeUpdate();
                    log.info("Successfully inserted");

// --ARTEFACT_RSD_HISTORY
//                    Long createdRsdHistoryId = null;
//                    String rsdHistoryId[] = {"ID"};
//                    String INSERT_ARTEFACT_RSD_HISTORY = "INSERT INTO ARTEFACT_RSD_HISTORY (ID, RSDID, ARTEFACTID, BSCID, ALTITUDE, CNSTRTYPEID, TOWERTYPEID, HEIGHT, DATEOFINSERT, DATEOFVISIT, PLANNER, CONTACTPERSON, COMMENTS, LASTEDITOR, RBSID, SITE_TYPE, PLANNING_TARGET) VALUES (SEQ_ARTEFACT_RSD_HISTORY.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";//(5361, 4701, 167, 1131, 10, null, 6, TO_DATE('2009-05-18 15:53:24', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2009-05-15 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'VITALIY.CHERNIKOV', null, 'Contaiter, poles (3m) on container, all antennas must be installed on poles. ', null, null, TO_DATE('2009-08-07 12:03:07', 'YYYY-MM-DD HH24:MI:SS'), 'VIKTOR.MAXIMENKO', ' ', 565, null, null, null, null, null, null);
//                    PreparedStatement ARTEFACT_RSD_HISTORY_PreparedStatement = udbConnect.prepareStatement(INSERT_ARTEFACT_RSD_HISTORY, rsdHistoryId);
//                    log.info("INSERT INTO ARTEFACT_RSD_HISTORY preparedStatement SQL UPDATE VALUES");
//
//                    i = 1;
//                    ARTEFACT_RSD_HISTORY_PreparedStatement.setLong(i++, createdArtefactRSDId);// RSDID
//                    ARTEFACT_RSD_HISTORY_PreparedStatement.setLong(i++, createdArtefactId);// ARTEFACTID
//                    if (cn_bscInt != null) {
//                        ARTEFACT_RSD_HISTORY_PreparedStatement.setLong(i++, cn_bscInt); //BSCID
//                    } else {
//                        ARTEFACT_RSD_HISTORY_PreparedStatement.setNull(i++, Types.BIGINT);
//                    }
//                    if (cn_altitude != null) {
//                        ARTEFACT_RSD_HISTORY_PreparedStatement.setLong(i++, cn_altitude.longValue()); // ALTITUDE (cn_altitude)
//                    } else {
//                        ARTEFACT_RSD_HISTORY_PreparedStatement.setNull(i++, Types.BIGINT);
//                    }
//                    if (cn_constructionType != null) {
//                        ARTEFACT_RSD_HISTORY_PreparedStatement.setLong(i++, Integer.parseInt(cn_constructionType)); //CNSTRTYPEID
//                    } else {
//                        ARTEFACT_RSD_HISTORY_PreparedStatement.setNull(i++, Types.BIGINT);
//                    }
//                    ARTEFACT_RSD_HISTORY_PreparedStatement.setNull(i++, Types.BIGINT); // TOWERTYPEID
//                    if (cn_height_constr != null) {
//                        ARTEFACT_RSD_HISTORY_PreparedStatement.setLong(i++, cn_height_constr.longValue()); // CONSTRUCTION_HEIGHT (cn_height_constr)
//                    } else {
//                        ARTEFACT_RSD_HISTORY_PreparedStatement.setNull(i++, Types.BIGINT);
//                    }
//                    ARTEFACT_RSD_HISTORY_PreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // INSERT_DATE
//                    if (date_of_visit != null) {
//                        ARTEFACT_RSD_HISTORY_PreparedStatement.setDate(i++, new java.sql.Date(date_of_visit.getTimeInMillis())); //DATE OF VISIT (cn_date_visit)
//                    } else {
//                        ARTEFACT_RSD_HISTORY_PreparedStatement.setNull(i++, Types.DATE);
//                    }
//                    ARTEFACT_RSD_HISTORY_PreparedStatement.setString(i++, starter); // starter need change to current user // PLANNER
//                    ARTEFACT_RSD_HISTORY_PreparedStatement.setString(i++, contact_person); //CONTACTPERSON
//                    ARTEFACT_RSD_HISTORY_PreparedStatement.setString(i++, cn_comments); // PL_COMMENTS (cn_comments)
//                    ARTEFACT_RSD_HISTORY_PreparedStatement.setString(i++, starter); // starter need change to current user // LASTEDITOR
//                    if (rbsTypeInt != null) {
//                        ARTEFACT_RSD_HISTORY_PreparedStatement.setLong(i++, rbsTypeInt); //RBSID
//                    } else {
//                        ARTEFACT_RSD_HISTORY_PreparedStatement.setNull(i++, Types.BIGINT);
//                    }
//                    if (siteTypeInt != null) {
//                        ARTEFACT_RSD_HISTORY_PreparedStatement.setLong(i++, siteTypeInt); //SITE_TYPE
//                    } else {
//                        ARTEFACT_RSD_HISTORY_PreparedStatement.setNull(i++, Types.BIGINT);
//                    }
//                    ARTEFACT_RSD_HISTORY_PreparedStatement.setString(i++, targetCoverage); // PLANNING_TARGET
//
//                    ARTEFACT_RSD_HISTORY_PreparedStatement.executeUpdate();
//                    log.info("Successfully inserted");
//
//                    ResultSet createdRsdHistoryIdResultSet = ARTEFACT_RSD_HISTORY_PreparedStatement.getGeneratedKeys();
//                    createdRsdHistoryIdResultSet.next();
//                    createdRsdHistoryId = createdRsdHistoryIdResultSet.getLong(1);
//                    log.info("createdRsdHistoryId:");
//                    log.info(createdRsdHistoryId.toString());

                    // --ARTEFACT_TSD_EXIST
                    String INSERT_ARTEFACT_TSD_EXIST = "INSERT INTO ARTEFACT_TSD_EXIST (TSDID, TSD_EXIST, ARTEFACTID) VALUES (?, ?, ?)";  //ARTEFACT_TSD_EXIST_, 1, 10373);"
                    PreparedStatement ARTEFACT_TSD_EXIST_PreparedStatement = udbConnect.prepareStatement(INSERT_ARTEFACT_TSD_EXIST);
                    log.info("INSERT INTO ARTEFACT_TSD_EXIST preparedStatement SQL UPDATE VALUES");

                    i = 1;
                    ARTEFACT_TSD_EXIST_PreparedStatement.setLong(i++, createdArtefactExtTSDId);// TSDID
                    ARTEFACT_TSD_EXIST_PreparedStatement.setLong(i++, 1);
                    ARTEFACT_TSD_EXIST_PreparedStatement.setLong(i++, createdArtefactId);// ARTEFACTID

                    ARTEFACT_TSD_EXIST_PreparedStatement.executeUpdate();
                    log.info("Successfully inserted");

                    // end KWMS-940

                    // --Create new candidate form files:

//                    for (int j=0; j<createNewCandidateSiteFiles.size(); j++) {
                    if (createNewCandidateSiteFiles != null && createNewCandidateSiteFiles.size()>0){
                        for (int j=0; j<1; j++) {
                            SpinJsonNode file = (SpinJsonNode) createNewCandidateSiteFiles.get(j);
                            if (file != null) {
                                String INSERT_ARTEFACT_RR_FILE = "INSERT INTO ARTEFACT_RR_FILE (FILE_ID, RR_ID, FILENAME, LASTUPDATED, RR_FILE) VALUES (ARTEFACT_RR_FILE_SEQ.nextval, ?, ?, SYSDATE, ?)";
                                PreparedStatement ARTEFACT_RR_FILE_PreparedStatement = udbConnect.prepareStatement(INSERT_ARTEFACT_RR_FILE);
                                log.info("INSERT INTO ARTEFACT_RR_FILE: #" + j + "/" + createNewCandidateSiteFiles.size() + " " + file.prop("name").value().toString());

                                String rrFilePath = file.prop("path").value().toString();
                                InputStream rrFileInputStream = minioClient.getObject(rrFilePath);
                                byte[] rrFileBytes = IOUtils.toByteArray(rrFileInputStream);
                                ByteArrayInputStream rrFileIs = new ByteArrayInputStream(rrFileBytes);

                                i = 1;
                                ARTEFACT_RR_FILE_PreparedStatement.setLong(i++, createdArtefactRRId);// RR_ID
                                ARTEFACT_RR_FILE_PreparedStatement.setString(i++, file.hasProp("name") ? file.prop("name").value().toString() : "");
    //                            ARTEFACT_RR_FILE_PreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // INSERT_DATE
                                ARTEFACT_RR_FILE_PreparedStatement.setBinaryStream(i++, rrFileIs);
                                ARTEFACT_RR_FILE_PreparedStatement.executeUpdate();
                                log.info("Successfully inserted");
                            }
                        }
                    }

                    // --ARTEFACT_TSD_LEASING_ACTION
                    log.info("INSERT INTO ARTEFACT_TSD_LEASING_ACTION preparedStatement SQL UPDATE VALUES");log.info("INSERT INTO ARTEFACT_TSD_LEASING_ACTION preparedStatement SQL UPDATE VALUES");
                    Long tsdLeasingActionId = null;
                    String tsdLeasingAction[] = {"LEASING_STATUS_ACTION_ID"};
                    String INSERT_ARTEFACT_TSD_LEASING_ACTION = "INSERT INTO ARTEFACT_TSD_LEASING_ACTION (LEASING_STATUS_ACTION_ID, ARTEFACTID, LEASING_STATUS_ID, INSERTDATE) VALUES (ARTEFACT_TSD_LEASING_ACTI_SEQ.nextval, ?, ?, SYSDATE)";
                    PreparedStatement ARTEFACT_TSD_LEASING_ACTION_PreparedStatement = udbConnect.prepareStatement(INSERT_ARTEFACT_TSD_LEASING_ACTION, tsdLeasingAction);


                    i = 1;
                    ARTEFACT_TSD_LEASING_ACTION_PreparedStatement.setLong(i++, createdArtefactId);// RR_ID
                    ARTEFACT_TSD_LEASING_ACTION_PreparedStatement.setLong(i++, 1);// status
//                    ARTEFACT_TSD_LEASING_ACTION_PreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // INSERT_DATE

                    ARTEFACT_TSD_LEASING_ACTION_PreparedStatement.executeUpdate();
                    log.info("Successfully inserted");

                    ResultSet tsdLeasingActionIdResultSet = ARTEFACT_TSD_LEASING_ACTION_PreparedStatement.getGeneratedKeys();
                    tsdLeasingActionIdResultSet.next();
                    tsdLeasingActionId = tsdLeasingActionIdResultSet.getLong(1);
                    log.info("tsdLeasingActionId:");
                    log.info(tsdLeasingActionId.toString());

                    udbConnect.commit();
                    delegateExecution.setVariable("createdArtefactId", createdArtefactId);
                    delegateExecution.setVariable("createdArtefactRSDId", createdArtefactRSDId);
                    delegateExecution.setVariable("createdArtefactRRId", createdArtefactRRId);
//                    delegateExecution.setVariable("createdArtefactVSDId", createdArtefactVsdId);
                    delegateExecution.setVariable("createdCandApprovalId", createdCandApprovalId);
//                    delegateExecution.setVariable("createdArtefactRRStatusId", createdArtefactRRStatusId);
                    delegateExecution.setVariable("createdArtefactExtTSDId", createdArtefactExtTSDId);
                    delegateExecution.setVariable("createdArtefactRrPowerId", createdArtefactRrPowerId);
                    delegateExecution.setVariable("createdArtefactRrRenterCnId", createdArtefactRrRenterCnId);
                    delegateExecution.setVariable("createdArtefactRrRenterFeId", createdArtefactRrRenterFeId);
                    delegateExecution.setVariable("createdArtefactRrTrId", createdArtefactRrTrId);
                    delegateExecution.setVariable("createdTrAntennaId", createdTrAntennaId);
//                    delegateExecution.setVariable("createdRsdHistoryId", createdRsdHistoryId);
                    delegateExecution.setVariable("tsdLeasingAction", tsdLeasingAction);
                    udbConnect.close();
                    log.warning("udbConnection closed!");
                } else {
                    udbConnect.close();
                    log.warning("Failed to make connection!");
                }
            } catch (Exception e) {
                udbConnect.rollback();
                udbConnect.close();
                log.warning("connection Exception!");
                log.warning(e.getMessage());
                throw e;
            }
        } catch (SQLException e) {
            log.warning("testConnect SQLException!");
            log.warning(e.toString());
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
