package kz.kcell.flow.leasing;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.postgresql.util.PGobject;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.awt.print.Pageable;
import java.sql.*;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

import static org.camunda.spin.Spin.JSON;

@Log
@Service("CreateNCP")
public class CreateNCP implements JavaDelegate {

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
            log.info(udbOracleUrl);
            log.info(udbOracleUsername);
            log.info(udbOraclePassword);
            Connection udbConnect = DriverManager.getConnection(
                udbOracleUrl,
                udbOracleUsername,
                udbOraclePassword);
            try {
                if (udbConnect != null) {
                    udbConnect.setAutoCommit(false);
                    log.info("Connected to the database!");

                    //insert NCP
                    String returnCols[] = {"ARTEFACTID"};
                    String insertNCP = "INSERT INTO NCP_CREATION ( ARTEFACTID, NCPID, TARGET_CELL, REGION, LONGITUDE, LATITUDE, REASON, PROJECT, CREATOR, DATEOFINSERT, COMMENTS, CABINETID, TARGET_COVERAGE, TYPE, GEN_STATUS, NCP_STATUS, NCP_STATUS_DATE, BAND, INITIATOR, PART) VALUES (NCP_CREATION_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 7, 1, ?, ?, ?, ?)";
                    PreparedStatement preparedStatement = udbConnect.prepareStatement(insertNCP, returnCols);

                    Integer i = 1;
                    log.info("preparedStatement.setValues");

                    // proc vars
                    Integer cn_rbs_location = 2;
                    String ncpId = delegateExecution.getVariable("ncpID") != null ? delegateExecution.getVariable("ncpID").toString() : null;
//                    String region = delegateExecution.getVariable("region").toString(); // not number
                    String longitude = delegateExecution.getVariable("longitude") != null ? delegateExecution.getVariable("longitude").toString() : null;
                    String latitude = delegateExecution.getVariable("latitude") != null ? delegateExecution.getVariable("latitude").toString() : null;
//                    String reason = delegateExecution.getVariable("reason").toString(); // it's string value, not dictionaries id
                    String starter = delegateExecution.getVariable("starter") != null ? delegateExecution.getVariable("starter").toString() : null;
                    String targetCell = delegateExecution.getVariable("createNCPTaskComment") != null ? delegateExecution.getVariable("createNCPTaskComment").toString() : null;
                    String targetCoverage = delegateExecution.getVariable("targetCoverage") != null ? delegateExecution.getVariable("targetCoverage").toString() : null;
                    String regionCode = delegateExecution.getVariable("regionCode") != null ? delegateExecution.getVariable("regionCode").toString() : null;
                    String rbsType = delegateExecution.getVariable("rbsType") != null ? delegateExecution.getVariable("rbsType").toString() : null;
                    String createNCPTaskComment = delegateExecution.getVariable("createNCPTaskComment") != null ? delegateExecution.getVariable("createNCPTaskComment").toString() : null;
                    String bandsIdForUDB = delegateExecution.getVariable("bandsIdForUDB") != null ? delegateExecution.getVariable("bandsIdForUDB").toString() : null;
                    String plannedCabinetTypeIdForUDB = delegateExecution.getVariable("plannedCabinetTypeIdForUDB") != null ? delegateExecution.getVariable("plannedCabinetTypeIdForUDB").toString() : null;

                    Integer reasonInt = null;
                    SpinJsonNode reasonJson = delegateExecution.getVariable("reason") != null ? JSON(delegateExecution.getVariable("reason")) : null;
                    if (reasonJson != null) {
                        try {
                            SpinJsonNode reason = reasonJson.prop("id");
                            String reasonString = reason.stringValue();
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
                            String siteTypeString = siteType.stringValue();
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
                            String projectString = project.stringValue();
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
                            String initiatorString = initiator.stringValue();
                            initiatorInt = Integer.parseInt(initiatorString);
                        } catch (Exception e) {
                            log.warning(e.getMessage());
                        }
                    }

                    Integer partInt = null;
                    SpinJsonNode partJson = delegateExecution.getVariable("part") != null ? JSON(delegateExecution.getVariable("part")) : null;
                    try {
                        SpinJsonNode part = partJson.prop("id");
                        String partString = part.stringValue();
                        partInt = Integer.parseInt(partString);
                    } catch (Exception e) {
                        log.warning(e.getMessage());
                    }

                    SpinJsonNode candidate = delegateExecution.getVariable("candidate") != null ? JSON(delegateExecution.getVariable("candidate")) : null;
                    String cn_longitude = candidate != null && candidate.hasProp("longitude") && candidate.prop("longitude") != null ? (candidate.prop("longitude").stringValue()) : null;
                    String cn_latitude = candidate != null && candidate.hasProp("latitude") && candidate.prop("latitude") != null ? (candidate.prop("latitude").stringValue()) : null;
                    String cn_siteName = candidate != null && candidate.hasProp("siteName") && candidate.prop("siteName") != null ? (candidate.prop("siteName").stringValue()) : null;
                    String cn_comments = candidate != null ? (candidate.hasProp("comments") && candidate.prop("comments") != null ? candidate.prop("comments").stringValue() : null) : null;
                    Number cn_square = candidate != null && candidate.hasProp("square") && candidate.prop("square") != null ? (candidate.prop("square").numberValue()) : null;
                    String cn_constructionType = candidate != null ? (candidate.hasProp("constructionType") && candidate.prop("constructionType").hasProp("id") ? candidate.prop("constructionType").prop("id").stringValue() : "0") : null;

                    Number cn_height_constr = candidate.hasProp("cn_height_constr") ? Integer.parseInt(candidate.prop("cn_height_constr").value().toString()) : 0;
                    Number cn_altitude = candidate.hasProp("cn_altitude") ? Integer.parseInt(candidate.prop("cn_altitude").value().toString()) : 0;
                    log.info("cn_altitude:" + cn_altitude);
                    log.info("cn_height_constr:" + cn_height_constr);
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-dd-MM"); //2020-01-02T18:00:00.000Z
                    String cn_date_of_visit = candidate != null && candidate.hasProp("dateOfVisit") && candidate.prop("dateOfVisit") != null ? (candidate.prop("dateOfVisit").stringValue().substring(0, 9)) : null;
                    Date date_of_visit = cn_date_of_visit != null ? formatter.parse(cn_date_of_visit) : null;


                    //{"antennaType":"ML 0.6","diameter":"0.6","weight":"14","antennaQuantity":1,"frequencyBand":"8GHz","suspensionHeight":12,"azimuth":"123"}
                    SpinJsonNode ne = delegateExecution.getVariable("transmissionAntenna") != null ? JSON(delegateExecution.getVariable("transmissionAntenna")) : null;
                    String ne_azimuth = ne != null ? (ne.hasProp("azimuth") ? ne.prop("azimuth").stringValue() : "0") : null;
                    String ne_diameter = ne != null ? (ne.hasProp("diameter") ? ne.prop("diameter").stringValue() : "0") : null;
                    String ne_frequencyBand = ne != null && ne.hasProp("frequencyBand") ? ne.prop("frequencyBand").stringValue().replaceAll("[^0-9.]", "") : null;
                    Number ne_suspensionHeight = ne != null && ne.hasProp("suspensionHeight") ? (ne.prop("suspensionHeight").numberValue()) : null;

//                    farEndInformation

                    SpinJsonNode feJson = delegateExecution.getVariable("farEndInformation") != null ? JSON(delegateExecution.getVariable("farEndInformation")) : null;
                    SpinList farEnds = feJson != null ? feJson.elements() : null;
                    SpinJsonNode fe = farEnds != null ? (SpinJsonNode) farEnds.get(0) : null;
                    String fe_azimuth = fe != null && fe.hasProp("azimuth") ? (fe.prop("azimuth").stringValue()) : null;
                    String fe_diameter = fe != null && fe.hasProp("diameter") ? (fe.prop("diameter").stringValue()) : null;
                    String fe_frequencyBand = fe != null && fe.hasProp("frequencyBand") ? fe.prop("frequencyBand").stringValue().replaceAll("[^0-9.]", "") : null;
                    String fe_suspensionHeight = fe != null && fe.hasProp("suspensionHeight") && fe.prop("suspensionHeight") != null ? fe.prop("suspensionHeight").stringValue() : null;
                    String fe_constructionType = fe != null && fe.hasProp("constructionType") && fe.prop("constructionType") != null ? (fe.prop("constructionType").hasProp("id") ? fe.prop("constructionType").prop("id").stringValue() : null) : null;
                    String fe_sitename = fe != null && fe.hasProp("farEndName") && fe.prop("farEndName") != null ? fe.prop("farEndName").stringValue() : null;
                    String fe_comment = fe != null && fe.hasProp("comments") && fe.prop("comments") != null ? fe.prop("comments").stringValue() : null;
                    String fe_survey_date = fe != null && fe.hasProp("surveyDate") && fe.prop("surveyDate") != null ? (fe.prop("surveyDate").stringValue().substring(0, 9)) : null;
                    Date fe_formated_survey_date = fe_survey_date != null ? formatter.parse(fe_survey_date) : null;

                    String fe_address = fe != null ? ("" +
                        (fe.hasProp("address") && fe.prop("address").hasProp("cn_addr_oblast") ? fe.prop("address").prop("cn_addr_oblast").stringValue() : "") +
                        (fe.hasProp("address") && fe.prop("address").hasProp("cn_addr_district") && fe.prop("address").prop("cn_addr_district") != null ? ", " + fe.prop("address").prop("cn_addr_district").stringValue() : "") +
                        (fe.hasProp("address") && fe.prop("address").hasProp("cn_addr_city") && fe.prop("address").prop("cn_addr_city") != null ? ", " + fe.prop("address").prop("cn_addr_city").stringValue() : "") +
                        (fe.hasProp("address") && fe.prop("address").hasProp("cn_addr_street") && fe.prop("address").prop("cn_addr_street") != null ? ", " + fe.prop("address").prop("cn_addr_street").stringValue() : "") +
                        (fe.hasProp("address") && fe.prop("address").hasProp("cn_addr_building") && fe.prop("address").prop("cn_addr_building") != null ? ", " + fe.prop("address").prop("cn_addr_building").stringValue() : "") +
                        (fe.hasProp("address") && fe.prop("address").hasProp("cn_addr_cadastral_number") && fe.prop("address").prop("cn_addr_cadastral_number") != null ? ", " + fe.prop("address").prop("cn_addr_cadastral_number").stringValue() : "") +
                        (fe.hasProp("address") && fe.prop("address").hasProp("cn_addr_note") && fe.prop("address").prop("cn_addr_note") != null ? ", " + fe.prop("address").prop("cn_addr_note").stringValue() : "")) : null;

                    SpinJsonNode renterCompany = delegateExecution.getVariable("renterCompany") != null ? JSON(delegateExecution.getVariable("renterCompany")) : null;
                    String contact_person = renterCompany != null ? ("" + (renterCompany.hasProp("contactName") && !renterCompany.prop("contactName").equals(null) ? renterCompany.prop("contactName").stringValue() : "") +
                        " " + (renterCompany.hasProp("contactPosition") && !renterCompany.prop("contactPosition").equals(null) ? renterCompany.prop("contactPosition").stringValue() : "") +
                        " ( tel:" + (renterCompany.hasProp("telFax") && !renterCompany.prop("telFax").equals(null) ? renterCompany.prop("telFax").stringValue() : "") + ")") : null;

                    //{"cn_addr_oblast":"область Акмолинская","cn_addr_district":"","cn_addr_city":"г. Кокшетау","cn_addr_street":"фывфв","ca_not_full_addres":false,"cn_addr_building":"фывыфв"}
                    SpinJsonNode address = delegateExecution.getVariable("address") != null ? JSON(delegateExecution.getVariable("address")) : null;
                    String cn_address = address != null ? ("" + (address.hasProp("cn_addr_oblast") && address.prop("cn_addr_oblast") != null ? address.prop("cn_addr_oblast").stringValue() : "") +
                        (address.hasProp("cn_addr_district") && address.prop("cn_addr_district") != null ? ", " + address.prop("cn_addr_district").stringValue() : "") +
                        (address.hasProp("cn_addr_city") && address.prop("cn_addr_city") != null ? ", " + address.prop("cn_addr_city").stringValue() : "") +
                        (address.hasProp("cn_addr_street") && address.prop("cn_addr_street") != null ? ", " + address.prop("cn_addr_street").stringValue() : "") +
                        (address.hasProp("cn_addr_building") && address.prop("cn_addr_building") != null ? ", " + address.prop("cn_addr_building").stringValue() : "") +
                        (address.hasProp("cn_addr_cadastral_number") && address.prop("cn_addr_cadastral_number") != null ? ", " + address.prop("cn_addr_cadastral_number").stringValue() : "") +
                        (address.hasProp("cn_addr_note") && address.prop("cn_addr_note") != null ? ", " + address.prop("cn_addr_note").stringValue() : "")) : null;

                    String cn_bsc = candidate != null ? (candidate.hasProp("bsc") ? (candidate.prop("bsc").hasProp("id") ? candidate.prop("bsc").prop("id").stringValue() : null) : null) : null;
                    Integer cn_bscInt = cn_bsc != null ? Integer.parseInt(cn_bsc) : null;

                    // set values to insert
                    preparedStatement.setString(i++, ncpId); // NCPID
                    Integer regionCodeInt = Integer.parseInt(regionCode);
                    if (targetCell != null) {
                        preparedStatement.setString(i++, targetCell); // target_cell
                    } else {
                        preparedStatement.setNull(i++, Types.VARCHAR);
                    }
                    if (regionCodeInt != null) {
                        preparedStatement.setLong(i++, regionCodeInt); // REGION
                    } else {
                        preparedStatement.setNull(i++, Types.BIGINT);
                    }
                    preparedStatement.setString(i++, longitude); // LONGITUDE ex. E 76,890775
                    preparedStatement.setString(i++, latitude); // LATITUDE
                    if (reasonInt != null) {
                        preparedStatement.setLong(i++, reasonInt); // REASON
                    } else {
                        preparedStatement.setNull(i++, Types.BIGINT);

                    }
                    if (projectInt != null) {
                        preparedStatement.setLong(i++, projectInt); // PROJECT
                    } else {
                        preparedStatement.setNull(i++, Types.BIGINT);
                    }
                    preparedStatement.setString(i++, starter); // CREATOR 'SERGEI.ZAITSEV'
                    preparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // DATEOFINSERT
                    preparedStatement.setString(i++, createNCPTaskComment); // COMMENTS
                    Integer plannedCabinetTypeIdForUDBInt = Integer.parseInt(plannedCabinetTypeIdForUDB);
                    if (plannedCabinetTypeIdForUDBInt != null) {
                        preparedStatement.setLong(i++, plannedCabinetTypeIdForUDBInt); // CABINETID
                    } else {
                        preparedStatement.setNull(i++, Types.BIGINT);
                    }
                    preparedStatement.setString(i++, targetCoverage); // TARGET_COVERAGE
                    if (siteTypeInt != null) {
                        preparedStatement.setLong(i++, siteTypeInt); // TYPE ncp_type (Подставлять ID согласно справочнику Site Type) ex: 6
                    } else {
                        preparedStatement.setNull(i++, Types.BIGINT);
                    }
                    preparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // NCP_STATUS_DATE
                    preparedStatement.setString(i++, bandsIdForUDB); // BAND ex:'1'   ncp_band	(Подставлять ID согласно справочнику Bands)
                    if (initiatorInt != null) {
                        preparedStatement.setLong(i++, initiatorInt); // INITIATOR
                    } else {
                        preparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (partInt != null) {
                        preparedStatement.setLong(i++, partInt); // PART (Подставлять ID согласно справочнику Part)
                    } else {
                        preparedStatement.setNull(i++, Types.BIGINT);
                    }

                    log.info("preparedStatement.executeUpdate()");
                    preparedStatement.executeUpdate();
                    log.info("successfull insert to database!");
//
                    ResultSet headGeneratedIdResultSet = preparedStatement.getGeneratedKeys();
                    headGeneratedIdResultSet.next();
                    Long ncpCreatedId = headGeneratedIdResultSet.getLong(1);
                    log.info("artefactGeneratedId:");
                    log.info(ncpCreatedId.toString());

                    //insert new status
                    Long createdNcpStatusId = null;
                    String returnStatus[] = {"STATUS_ACTION_ID"};
                    String insertNewStatus = "insert into NCP_CREATION_STATUS_ACTION values ( NCP_CREATION_STATUS_ACTIO_SEQ.nextval, ?, 2, ?, ?, null)";
                    PreparedStatement newNcpStatusPreparedStatement = udbConnect.prepareStatement(insertNewStatus, returnStatus);

                    i = 1;
                    log.info("newNcpStatusPreparedStatement.setString");
                    newNcpStatusPreparedStatement.setLong(i++, ncpCreatedId);
                    newNcpStatusPreparedStatement.setString(i++, starter); // CREATOR 'SERGEI.ZAITSEV'
                    newNcpStatusPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // DATEOFINSERT
                    log.info("newNcpStatusPreparedStatement.executeUpdate()");
                    newNcpStatusPreparedStatement.executeUpdate();
                    log.info("successfull insert to database!");

                    ResultSet statusGeneratedIdResultSet = newNcpStatusPreparedStatement.getGeneratedKeys();
                    statusGeneratedIdResultSet.next();
                    createdNcpStatusId = statusGeneratedIdResultSet.getLong(1);
                    log.info("createdNcpStatusId:");
                    log.info(createdNcpStatusId.toString());

                    //insert new Candidate (in Artefact table)
                    Long createdArtefactId = null;
                    String returnArtefactId[] = {"ARTEFACTID"};
                    String insertNewArtefact = "insert into ARTEFACT values (ARTEFACT_SEQ.nextval, ?, ?)";
                    PreparedStatement newArtefactPreparedStatement = udbConnect.prepareStatement(insertNewArtefact, returnArtefactId);

                    i = 1;
                    log.info("newArtefactPreparedStatement.setString");
                    newArtefactPreparedStatement.setString(i++, cn_siteName); // sitename	cn_sitename
                    Integer ncpIdInt = Integer.parseInt(ncpId);
                    if (ncpIdInt != null) {
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

                    //insert new Candidate (in ARTEFACT_CURRENT_STATE table)
                    String insertNewArtefactCurrentState = "INSERT INTO ARTEFACT_CURRENT_STATE (ARTEFACTID,\n" +
                        "                                                        NCPID,\n" +
                        "                                                        RSD_EXIST,\n" +
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
                        "                                                        PL_COMMENTS) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
                    newArtefactCurrentStatePreparedStatement.setLong(i++, 0); // RSD_EXIST first insert value mast be = 1
                    newArtefactCurrentStatePreparedStatement.setLong(i++, 1); // CAND_STATUS first insert value mast be = 1

                    newArtefactCurrentStatePreparedStatement.setString(i++, starter); // CAND_STATUS_PERSON (current user) // current or start ?????? (who complete create candidate task?)
                    newArtefactCurrentStatePreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // CAND_STATUS_DATE
                    newArtefactCurrentStatePreparedStatement.setLong(i++, 1); // RR_STATUS first insert value mast be = 1
                    newArtefactCurrentStatePreparedStatement.setString(i++, starter); // RR_STATUS_PERSON (current user)
                    newArtefactCurrentStatePreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // RR_STATUS_DATE
                    newArtefactCurrentStatePreparedStatement.setString(i++, cn_longitude); // LONGITUDE ex: E 80 50 42,4
                    newArtefactCurrentStatePreparedStatement.setString(i++, cn_latitude); // LATITUDE ex: N 48 48 05,6
                    Integer rbsTypeInt = Integer.parseInt(rbsType);
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
                        newArtefactCurrentStatePreparedStatement.setLong(i++, cn_rbs_location); // RBS_LOCATION (cn_rbs_location) //
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
                    Integer cn_constructionTypeInt = Integer.parseInt(cn_constructionType);
                    if (cn_constructionTypeInt != null) {
                        newArtefactCurrentStatePreparedStatement.setLong(i++, cn_constructionTypeInt); // CONSTRUCTION_TYPE (cn_construction_type)
                    } else {
                        newArtefactCurrentStatePreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    newArtefactCurrentStatePreparedStatement.setString(i++, cn_address); // ADDRESS "Восточно-Казахстанская область, Зайсанский район, село Карабулак, водонапорная башня"
                    newArtefactCurrentStatePreparedStatement.setString(i++, contact_person); // CONTACT_PERSON "Мария Николаевна Специалист акимата 8 701 479 19 86"
                    newArtefactCurrentStatePreparedStatement.setString(i++, renterCompany != null ? ((renterCompany.hasProp("contactInfo") && !renterCompany.prop("contactInfo").equals(null) ? renterCompany.prop("contactInfo").stringValue() : "")) : null); // COMMENTS (cn_contact_information)
                    newArtefactCurrentStatePreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // INSERT_DATE
                    newArtefactCurrentStatePreparedStatement.setString(i++, starter); // INSERT_PERSON
                    newArtefactCurrentStatePreparedStatement.setLong(i++, 7); // GS_STATUS
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
                    String insertNewArtefactRSD = "INSERT INTO ARTEFACT_RSD (RSDID, ARTEFACTID, BSCID, CNSTRTYPEID, HEIGHT, DATEOFINSERT, DATEOFVISIT, CONTACTPERSON, COMMENTS, STATE, RBSID, SITE_TYPE) VALUES ( ARTEFACT_RSD_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
                    if (cn_constructionTypeInt != null) {
                        newArtefactRSDPreparedStatement.setLong(i++, cn_constructionTypeInt); //CNSTRTYPEID
                    } else {
                        newArtefactRSDPreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (cn_height_constr != null) {
                        newArtefactRSDPreparedStatement.setLong(i++, cn_height_constr.longValue()); // CONSTRUCTION_HEIGHT
                    } else {
                        newArtefactRSDPreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    newArtefactRSDPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // INSERT_DATE
                    newArtefactRSDPreparedStatement.setDate(i++, date_of_visit != null ? new java.sql.Date(date_of_visit.getTime()) : null); // DATE OF VISIT (cn_date_visit)
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
                    String insertNewArtefactRR = "INSERT INTO ARTEFACT_RR (RR_ID, ARTEFACTID, DATEOFVISIT, ADDRESS, LATITUDE, LONGITUDE, CONSTR_TYPE, SQUARE, RBS_TYPE, BAND, RBS_LOCATION, COMMENTS, DATEOFCREATION, CREATOR) VALUES (ARTEFACT_RR_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement newArtefactRRPreparedStatement = udbConnect.prepareStatement(insertNewArtefactRR, rrReturnStatus);

                    i = 1;
                    log.info("newArtefactRRPreparedStatement.setString");
                    if (createdArtefactId != null) {
                        newArtefactRRPreparedStatement.setLong(i++, createdArtefactId); //ARTEFACTID
                    } else {
                        newArtefactRRPreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    newArtefactRRPreparedStatement.setDate(i++, date_of_visit != null ? new java.sql.Date(date_of_visit.getTime()) : null); // DATEOFVISIT
                    newArtefactRRPreparedStatement.setString(i++, cn_address); //ADDRESS
                    newArtefactRRPreparedStatement.setString(i++, latitude); //LATITUDE
                    newArtefactRRPreparedStatement.setString(i++, longitude); //LONGITUDE
                    if (cn_constructionTypeInt != null) {
                        newArtefactRRPreparedStatement.setLong(i++, cn_constructionTypeInt); //CONSTR_TYPE
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
                        newArtefactRRPreparedStatement.setLong(i++, cn_rbs_location); //RBS_LOCATION
                    } else {
                        newArtefactRRPreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    newArtefactRRPreparedStatement.setString(i++, cn_comments); //COMMENTS
                    newArtefactRRPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // DATEOFCREATION
                    newArtefactRRPreparedStatement.setString(i++, cn_comments); //CREATOR
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
                    String insertNewCandApproval = "insert into CANDAPPROVAL(ID, ARTEFACTID, RSDID) values (CANDAPPROVAL_SEQ.nextval, ?, ?)";
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
                    log.info("newCandApprovalPreparedStatement.executeUpdate()");
                    newCandApprovalPreparedStatement.executeUpdate();
                    log.info("successfull insert to database!");

                    ResultSet candApprovalGeneratedIdResultSet = newCandApprovalPreparedStatement.getGeneratedKeys();
                    candApprovalGeneratedIdResultSet.next();
                    createdCandApprovalId = candApprovalGeneratedIdResultSet.getLong(1);
                    log.info("createdCandApprovalId:");
                    log.info(createdCandApprovalId.toString());

                    //insert ARTEFACT_RR_STATUS
                    Long createdArtefactRRStatusId = null;
                    String artefactRRStatusReturnStatus[] = {"ID"};
                    String insertNewArtefactRRStatus = "insert into ARTEFACT_RR_STATUS(ID, ARTEFACTID, RR_ID, DATEOFPERFORM) values (ARTEFACT_RR_STATUS_SEQ.nextval, ?, ?, ?)";
                    PreparedStatement newArtefactRRStatusPreparedStatement = udbConnect.prepareStatement(insertNewArtefactRRStatus, artefactRRStatusReturnStatus);

                    i = 1;
                    log.info("newArtefactRRStatusPreparedStatement.setString");
                    if (createdArtefactId != null) {
                        newArtefactRRStatusPreparedStatement.setLong(i++, createdArtefactId);
                    } else {
                        newArtefactRRStatusPreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (createdArtefactRSDId != null) {
                        newArtefactRRStatusPreparedStatement.setLong(i++, createdArtefactRSDId);
                    } else {
                        newArtefactRRStatusPreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    newArtefactRRStatusPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime()));
                    log.info("newArtefactRRStatusPreparedStatement.executeUpdate()");
                    newArtefactRRStatusPreparedStatement.executeUpdate();
                    log.info("successfull insert to database!");

                    ResultSet artefactRRStatusGeneratedIdResultSet = newArtefactRRStatusPreparedStatement.getGeneratedKeys();
                    artefactRRStatusGeneratedIdResultSet.next();
                    createdArtefactRRStatusId = artefactRRStatusGeneratedIdResultSet.getLong(1);
                    log.info("createdArtefactRRStatusId:");
                    log.info(createdArtefactRRStatusId.toString());

                    udbConnect.commit();
                    //insert ARTEFACT_TSD_EXT
                    Long createdArtefactExtTSDId = null;
                    String artefactExtTSDReturnStatus[] = {"TSDID"};
                    StringBuilder insertNewArtefactExtTSDbuilder = new StringBuilder("INSERT INTO ARTEFACT_TSD_EXT (TSDID, ARTEFACTID, INSERT_DATE, INSERT_PERSON");
                    StringBuilder insertNewArtefactExtTSDbuilderValues = new StringBuilder(") VALUES (ARTEFACT_TSD_SEQ.nextval, ?, ?, ?");
                    if (cn_longitude != null) {
                        insertNewArtefactExtTSDbuilder.append(", NE_LONGITUDE");
                        insertNewArtefactExtTSDbuilderValues.append(", ?");
                    }
                    if (cn_latitude != null) {
                        insertNewArtefactExtTSDbuilder.append(", NE_LATITUDE");
                        insertNewArtefactExtTSDbuilderValues.append(", ?");
                    }
                    if (fe_sitename != null) {
                        insertNewArtefactExtTSDbuilder.append(", FE_SITENAME");
                        insertNewArtefactExtTSDbuilderValues.append(", ?");
                    }
                    if (fe_constructionType != null) {
                        insertNewArtefactExtTSDbuilder.append(", FE_CONSTR_TYPE");
                        insertNewArtefactExtTSDbuilderValues.append(", ?");
                    }
                    if (fe_address != null) {
                        insertNewArtefactExtTSDbuilder.append(", FE_ADDRESS");
                        insertNewArtefactExtTSDbuilderValues.append(", ?");
                    }
                    if (fe_formated_survey_date != null) {
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
                    if (fe_azimuth != null) {
                        insertNewArtefactExtTSDbuilder.append(", FE_AZIMUTH");
                        insertNewArtefactExtTSDbuilderValues.append(", ?");
                    }
                    if (fe_diameter != null) {
                        insertNewArtefactExtTSDbuilder.append(", FE_ANTENNADIAMETER");
                        insertNewArtefactExtTSDbuilderValues.append(", ?");
                    }
                    if (fe_suspensionHeight != null) {
                        insertNewArtefactExtTSDbuilder.append(", FE_SUSPENSIONHEIGHT");
                        insertNewArtefactExtTSDbuilderValues.append(", ?");
                    }
                    if (fe_frequencyBand != null) {
                        insertNewArtefactExtTSDbuilder.append(", FE_TXRF_FREQUENCY");
                        insertNewArtefactExtTSDbuilderValues.append(", ?");
                    }
                    if (fe_comment != null) {
                        insertNewArtefactExtTSDbuilder.append(", COMMENTS");
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
                    newArtefactExtTSDPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // INSERT_DATE
                    newArtefactExtTSDPreparedStatement.setString(i++, starter); // INSERT_PERSON

                    if (cn_longitude != null) {
                        newArtefactExtTSDPreparedStatement.setString(i++, cn_longitude); // NE_LONGITUDE
                    }
                    if (cn_latitude != null) {
                        newArtefactExtTSDPreparedStatement.setString(i++, cn_latitude); // NE_LATITUDE
                    }
                    if (fe_sitename != null) {
                        newArtefactExtTSDPreparedStatement.setString(i++, fe_sitename); // FE_SITENAME
                    }
                    if (fe_constructionType != null) {
                        Integer fe_constructionTypeInt = Integer.parseInt(fe_constructionType);
                        if (fe_constructionTypeInt != null) {
                            newArtefactExtTSDPreparedStatement.setLong(i++, fe_constructionTypeInt); // FE_CONSTR_TYPE
                        } else {
                            newArtefactExtTSDPreparedStatement.setNull(i++, Types.BIGINT);
                        }
                    }
                    if (fe_address != null) {
                        newArtefactExtTSDPreparedStatement.setString(i++, fe_address); // FE_ADDRESS
                    }
                    if (fe_formated_survey_date != null) {
                        newArtefactExtTSDPreparedStatement.setDate(i++, new java.sql.Date(fe_formated_survey_date.getTime())); // SURVEY_DATE (fe_survey_date)
                    }
                    if (ne_azimuth != null) {
                        Integer ne_azimuthInt = Integer.parseInt(ne_azimuth);
                        if (ne_azimuthInt != null) {
                            newArtefactExtTSDPreparedStatement.setLong(i++, ne_azimuthInt); // NE_AZIMUTH
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
                    if (fe_azimuth != null) {
                        Integer fe_azimuthInt = Integer.parseInt(fe_azimuth);
                        if (fe_azimuthInt != null) {
                            newArtefactExtTSDPreparedStatement.setLong(i++, fe_azimuthInt); // FE_AZIMUTH
                        } else {
                            newArtefactExtTSDPreparedStatement.setNull(i++, Types.BIGINT);
                        }
                    }
                    if (fe_diameter != null) {
                        newArtefactExtTSDPreparedStatement.setFloat(i++, Float.parseFloat(fe_diameter)); // FE_ANTENNADIAMETER
                    }
                    if (fe_suspensionHeight != null) {
                        Integer fe_suspensionHeightInt = Integer.parseInt(fe_suspensionHeight);
                        if (fe_suspensionHeightInt != null) {
                            newArtefactExtTSDPreparedStatement.setLong(i++, fe_suspensionHeightInt); // FE_SUSPENSIONHEIGHT
                        } else {
                            newArtefactExtTSDPreparedStatement.setNull(i++, Types.BIGINT);
                        }
                    }
                    if (fe_frequencyBand != null) {
                        Integer fe_frequencyBandInt = Integer.parseInt(fe_frequencyBand);
                        if (fe_frequencyBandInt != null) {
                            newArtefactExtTSDPreparedStatement.setLong(i++, fe_frequencyBandInt); // FE_TXRF_FREQUENCY
                        } else {
                            newArtefactExtTSDPreparedStatement.setNull(i++, Types.BIGINT);
                        }
                    }
                    if (fe_comment != null) {
                        newArtefactExtTSDPreparedStatement.setString(i++, fe_comment); // COMMENTS
                    }
                    log.info("newArtefactExtTSDPreparedStatement.executeUpdate()");
                    newArtefactExtTSDPreparedStatement.executeUpdate();
                    log.info("successfull insert to database!");

                    ResultSet artefactExtTSDGeneratedIdResultSet = newArtefactExtTSDPreparedStatement.getGeneratedKeys();
                    artefactExtTSDGeneratedIdResultSet.next();
                    createdArtefactExtTSDId = artefactExtTSDGeneratedIdResultSet.getLong(1);
                    log.info("createdArtefactExtTSDId:");
                    log.info(createdArtefactExtTSDId.toString());


                    udbConnect.commit();
                    delegateExecution.setVariable("ncpCreatedId", ncpCreatedId);
                    delegateExecution.setVariable("createdNcpStatusId", createdNcpStatusId);
                    delegateExecution.setVariable("createdArtefactId", createdArtefactId);
                    delegateExecution.setVariable("createdArtefactRSDId", createdArtefactRSDId);
                    delegateExecution.setVariable("createdArtefactRRId", createdArtefactRRId);
//                    delegateExecution.setVariable("createdArtefactVSDId", createdArtefactVsdId);
                    delegateExecution.setVariable("createdCandApprovalId", createdCandApprovalId);
                    delegateExecution.setVariable("createdArtefactRRStatusId", createdArtefactRRStatusId);
                    delegateExecution.setVariable("createdArtefactExtTSDId", createdArtefactExtTSDId);
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
