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
            try {
                if (udbConnect != null) {
                    udbConnect.setAutoCommit(false);
                    log.info("Connected to the database!");

                    // proc vars
                    Integer cn_rbs_location = 2;

                    //generated vars
                    Long createdArtefactId = delegateExecution.getVariable("createdArtefactId") != null ? (Long) delegateExecution.getVariable("createdArtefactId") : null;
                    Long createdArtefactRSDId = delegateExecution.getVariable("createdArtefactRSDId") != null ? (Long) delegateExecution.getVariable("createdArtefactRSDId") : null;
                    Long createdArtefactRRId = delegateExecution.getVariable("createdArtefactRRId") != null ? (Long) delegateExecution.getVariable("createdArtefactRRId") : null;
                    Long createdArtefactExtTSDId = delegateExecution.getVariable("createdArtefactExtTSDId") != null ? (Long) delegateExecution.getVariable("createdArtefactExtTSDId") : null;
                    Long createdArtefactRRStatusId = (Long) delegateExecution.getVariable("createdArtefactRRStatusId");

                    String ncpId = delegateExecution.getVariable("ncpID") != null ? delegateExecution.getVariable("ncpID").toString() : null;
                    String starter = delegateExecution.getVariable("starter") != null ? delegateExecution.getVariable("starter").toString() : null;
                    String rbsType = delegateExecution.getVariable("rbsType") != null ? delegateExecution.getVariable("rbsType").toString() : null;
                    String bandsIdForUDB = delegateExecution.getVariable("bandsIdForUDB") != null ? delegateExecution.getVariable("bandsIdForUDB").toString() : null;

                    SpinJsonNode siteTypeJson = delegateExecution.getVariable("siteType") != null ? JSON(delegateExecution.getVariable("siteType")) : null;
                    SpinJsonNode siteType = siteTypeJson != null && siteTypeJson.hasProp("id") ? siteTypeJson.prop("id") : null;
                    String siteTypeString = siteType != null ? siteType.stringValue() : null;
                    Integer siteTypeInt = siteTypeString != null ? Integer.parseInt(siteTypeString) : null;

                    SpinJsonNode candidate = delegateExecution.getVariable("candidate") != null ? JSON(delegateExecution.getVariable("candidate")) : null;
                    String cn_longitude = candidate != null && candidate.hasProp("longitude") && candidate.prop("longitude") != null ? candidate.prop("longitude").stringValue() : null;
                    String cn_latitude = candidate != null && candidate.hasProp("latitude") && candidate.prop("latitude") != null ? candidate.prop("latitude").stringValue() : null;
                    String cn_siteName = candidate != null && candidate.hasProp("siteName") && candidate.prop("siteName") != null ? candidate.prop("siteName").stringValue() : null;
                    String cn_comments = candidate != null && candidate.hasProp("comments") && candidate.prop("comments") != null ? candidate.prop("comments").stringValue() : null;
                    Number cn_square = candidate != null && candidate.hasProp("square") && candidate.prop("square") != null ? candidate.prop("square").numberValue() : null;
                    String cn_constructionType = candidate != null && candidate.hasProp("constructionType") && candidate.prop("constructionType").hasProp("id") && candidate.prop("constructionType").prop("id") != null ? candidate.prop("constructionType").prop("id").stringValue() : null;

                    Number cn_height_constr = candidate.hasProp("cn_height_constr") ? Integer.parseInt(candidate.prop("cn_height_constr").value().toString()) : 0;
                    Number cn_altitude = candidate.hasProp("cn_altitude") ? Integer.parseInt(candidate.prop("cn_altitude").value().toString()) : 0;

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-dd-MM"); //2020-01-02T18:00:00.000Z
                    String cn_date_of_visit = candidate.prop("dateOfVisit").stringValue().substring(0, 9);
                    Date date_of_visit = formatter.parse(cn_date_of_visit);

                    SpinJsonNode ne = delegateExecution.getVariable("transmissionAntenna") != null ? JSON(delegateExecution.getVariable("transmissionAntenna")) : null;
                    String ne_azimuth = ne != null ? (ne.hasProp("azimuth") ? ne.prop("azimuth").stringValue() : "0") : null;
                    String ne_diameter = ne != null ? (ne.hasProp("diameter") ? ne.prop("diameter").stringValue() : "0") : null;
                    String ne_frequencyBand = ne != null && ne.hasProp("frequencyBand") ? ne.prop("frequencyBand").stringValue().replaceAll("[^0-9.]", "") : null;
                    Number ne_suspensionHeight = ne != null && ne.hasProp("suspensionHeight") ? (ne.prop("suspensionHeight").numberValue()) : null;

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

                    String fe_equipment_type_string = fe != null && fe.hasProp("equipmentType") ? (fe.prop("equipmentType").stringValue()) : null;
//                    TN2P		2
//                    TN20P		6
//                    TN6P		4
                    Number fe_equipment_type = null;
                    if (fe_equipment_type_string.equals("TN2P")) {
                        fe_equipment_type = 2;
                    } else if (fe_equipment_type_string.equals("TN20P")) {
                        fe_equipment_type = 6;
                    } else if (fe_equipment_type_string.equals("TN6P")) {
                        fe_equipment_type = 4;
                    }

                    String fe_address = fe != null ? ("" +
                        (fe.hasProp("address") && fe.prop("address").hasProp("cn_addr_oblast") ? fe.prop("address").prop("cn_addr_oblast").stringValue() : "") +
                        (fe.hasProp("address") && fe.prop("address").hasProp("cn_addr_district") && fe.prop("address").prop("cn_addr_district") != null ? ", " + fe.prop("address").prop("cn_addr_district").stringValue() : "") +
                        (fe.hasProp("address") && fe.prop("address").hasProp("cn_addr_city") && fe.prop("address").prop("cn_addr_city") != null ? ", " + fe.prop("address").prop("cn_addr_city").stringValue() : "") +
                        (fe.hasProp("address") && fe.prop("address").hasProp("cn_addr_street") && fe.prop("address").prop("cn_addr_street") != null ? ", " + fe.prop("address").prop("cn_addr_street").stringValue() : "") +
                        (fe.hasProp("address") && fe.prop("address").hasProp("cn_addr_building") && fe.prop("address").prop("cn_addr_building") != null ? ", " + fe.prop("address").prop("cn_addr_building").stringValue() : "") +
                        (fe.hasProp("address") && fe.prop("address").hasProp("cn_addr_cadastral_number") && fe.prop("address").prop("cn_addr_cadastral_number") != null ? ", " + fe.prop("address").prop("cn_addr_cadastral_number").stringValue() : "") +
                        (fe.hasProp("address") && fe.prop("address").hasProp("cn_addr_note") && fe.prop("address").prop("cn_addr_note") != null ? ", " + fe.prop("address").prop("cn_addr_note").stringValue() : "")) : null;


                    SpinJsonNode renterCompany = delegateExecution.getVariable("renterCompany") != null ? JSON(delegateExecution.getVariable("renterCompany")) : null;
                    String contact_person = "" +
                        (renterCompany != null && renterCompany.hasProp("contactName") && !renterCompany.prop("contactName").equals(null) ? renterCompany.prop("contactName").stringValue() : "") +
                        " " + (renterCompany != null && renterCompany.hasProp("contactPosition") && !renterCompany.prop("contactPosition").equals(null) ? renterCompany.prop("contactPosition").stringValue() : "") +
                        " ( tel:" + (renterCompany != null && renterCompany.hasProp("telFax") && !renterCompany.prop("telFax").equals(null) ? renterCompany.prop("telFax").stringValue() : "") + ")";

                    //{"cn_addr_oblast":"область Акмолинская","cn_addr_district":"","cn_addr_city":"г. Кокшетау","cn_addr_street":"фывфв","ca_not_full_addres":false,"cn_addr_building":"фывыфв"}
                    SpinJsonNode address = delegateExecution.getVariable("address") != null ? JSON(delegateExecution.getVariable("address")) : null;
                    String cn_address = "" + (address != null && address.hasProp("cn_addr_oblast") && address.prop("cn_addr_oblast") != null ? address.prop("cn_addr_oblast").stringValue() : "") +
                        (address != null && address.hasProp("cn_addr_district") && address.prop("cn_addr_district") != null ? ", " + address.prop("cn_addr_district").stringValue() : "") +
                        (address != null && address.hasProp("cn_addr_city") && address.prop("cn_addr_city") != null ? ", " + address.prop("cn_addr_city").stringValue() : "") +
                        (address != null && address.hasProp("cn_addr_street") && address.prop("cn_addr_street") != null ? ", " + address.prop("cn_addr_street").stringValue() : "") +
                        (address != null && address.hasProp("cn_addr_building") && address.prop("cn_addr_building") != null ? ", " + address.prop("cn_addr_building").stringValue() : "") +
                        (address != null && address.hasProp("cn_addr_cadastral_number") && address.prop("cn_addr_cadastral_number") != null ? ", " + address.prop("cn_addr_cadastral_number").stringValue() : "") +
                        (address != null && address.hasProp("cn_addr_note") && address.prop("cn_addr_note") != null ? ", " + address.prop("cn_addr_note").stringValue() : "");

                    String cn_bsc = candidate != null && candidate.hasProp("bsc") && candidate.prop("bsc") != null && candidate.prop("bsc").hasProp("id") && candidate.prop("bsc").prop("id") != null ? candidate.prop("bsc").prop("id").stringValue() : null;
                    Integer cn_bscInt = cn_bsc != null ? Integer.parseInt(cn_bsc) : null;

                    //UPDATE ARTEFACT ARTEFACT_CURRENT_STATE
                    log.info("UPDATE ARTEFACT_CURRENT_STATE");
                    String UpdateArtefactCurrentState = "UPDATE ARTEFACT_CURRENT_STATE SET RR_STATUS = ?, RR_STATUS_DATE = ?, LONGITUDE = ?, LATITUDE = ?, RBS_TYPE = ?, BSC = ?, BAND = ?, RBS_LOCATION = ?, ALTITUDE = ?, CONSTRUCTION_HEIGHT = ?, CONSTRUCTION_TYPE = ?, ADDRESS = ?, CONTACT_PERSON = ?, COMMENTS = ?, EQUIPMENT_TYPE = ?, GS_STATUS = ?, PL_COMMENTS = ? WHERE ARTEFACTID = ?";
                    PreparedStatement updateArtefactCurrentStatePreparedStatement = udbConnect.prepareStatement(UpdateArtefactCurrentState);
                    Integer rbsTypeInt = rbsType != null ? Integer.parseInt(rbsType) : null;
                    Integer cnConstructionTypeInt = cn_constructionType != null ? Integer.parseInt(cn_constructionType) : null;

                    Integer bandsIdForUDBInt = bandsIdForUDB != null ? Integer.parseInt(bandsIdForUDB) : null;
                    String contractInfoString = renterCompany != null && renterCompany.hasProp("contactInfo") && !renterCompany.prop("contactInfo").equals(null) ? renterCompany.prop("contactInfo").stringValue() : "";
                    int i = 1;
                    log.info("UPDATE ARTEFACT ARTEFACT_CURRENT_STATE");
                    // set values to insert
                    updateArtefactCurrentStatePreparedStatement.setLong(i++, 2); // RR_STATUS
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
                        updateArtefactCurrentStatePreparedStatement.setLong(i++, cn_rbs_location); // RBS_LOCATION (cn_rbs_location) //
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
                    if (cnConstructionTypeInt != null) {
                        updateArtefactCurrentStatePreparedStatement.setLong(i++, fe_equipment_type.longValue()); //equipment_type
                    } else {
                        updateArtefactCurrentStatePreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    updateArtefactCurrentStatePreparedStatement.setLong(i++, 3); //gs_status
                    updateArtefactCurrentStatePreparedStatement.setString(i++, cn_comments); //pl_comments
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
                    java.sql.Date dateOfVisitDate = date_of_visit != null ? new java.sql.Date(date_of_visit.getTime()) : null;
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
                        updateArtefactRRPreparedStatement.setLong(i++, cn_rbs_location); //RBS_LOCATION
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
                    String UpdateArtefactTSD = "update ARTEFACT_TSD_EXT set equipment_id = ?, ne_longitude = ?, ne_latitude = ?, fe_sitename = ?, fe_constr_type = ?, fe_address = ?, survey_date = ?, ne_azimuth = ?, ne_antennadiameter = ?, ne_suspensionheight = ?, ne_txrf_frequency = ?, fe_azimuth = ?, fe_antennadiameter = ?, fe_suspensionheight = ?, fe_txrf_frequency = ?, update_date = ? where tsdid = ?";
                    PreparedStatement updateArtefactTSDPreparedStatement = udbConnect.prepareStatement(UpdateArtefactTSD);

                    i = 1;
                    if (fe_equipment_type != null) {
                        updateArtefactTSDPreparedStatement.setLong(i++, fe_equipment_type.longValue()); // equipment_id
                    } else {
                        updateArtefactTSDPreparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (cn_longitude != null) {
                        updateArtefactTSDPreparedStatement.setString(i++, cn_longitude); // NE_LONGITUDE
                    } else {
                        updateArtefactTSDPreparedStatement.setNull(i++, Types.VARCHAR);
                    }
                    if (cn_latitude != null) {
                        updateArtefactTSDPreparedStatement.setString(i++, cn_latitude); // NE_LATITUDE
                    } else {
                        updateArtefactTSDPreparedStatement.setNull(i++, Types.VARCHAR);
                    }
                    if (fe_sitename != null) {
                        updateArtefactTSDPreparedStatement.setString(i++, fe_sitename); // FE_SITENAME
                    }
                    if (fe_constructionType != null) {
                        Integer fe_constructionTypeInt = Integer.parseInt(fe_constructionType);
                        if (fe_constructionTypeInt != null) {
                            updateArtefactTSDPreparedStatement.setLong(i++, fe_constructionTypeInt); // FE_CONSTR_TYPE
                        } else {
                            updateArtefactTSDPreparedStatement.setNull(i++, Types.BIGINT);
                        }
                    }
                    if (fe_address != null) {
                        updateArtefactTSDPreparedStatement.setString(i++, fe_address); // FE_ADDRESS
                    }
                    if (fe_formated_survey_date != null) {
                        updateArtefactTSDPreparedStatement.setDate(i++, new java.sql.Date(fe_formated_survey_date.getTime())); // SURVEY_DATE (fe_survey_date)
                    }
                    if (ne_azimuth != null) {
                        Integer ne_azimuthInt = Integer.parseInt(ne_azimuth);
                        if (ne_azimuthInt != null) {
                            updateArtefactTSDPreparedStatement.setLong(i++, ne_azimuthInt); // NE_AZIMUTH
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
                    if (fe_azimuth != null) {
                        Integer fe_azimuthInt = Integer.parseInt(fe_azimuth);
                        if (fe_azimuthInt != null) {
                            updateArtefactTSDPreparedStatement.setLong(i++, fe_azimuthInt); // FE_AZIMUTH
                        } else {
                            updateArtefactTSDPreparedStatement.setNull(i++, Types.BIGINT);
                        }
                    }
                    if (fe_diameter != null) {
                        updateArtefactTSDPreparedStatement.setFloat(i++, Float.parseFloat(fe_diameter)); // FE_ANTENNADIAMETER
                    }
                    if (fe_suspensionHeight != null) {
                        Integer fe_suspensionHeightInt = Integer.parseInt(fe_suspensionHeight);
                        if (fe_suspensionHeightInt != null) {
                            updateArtefactTSDPreparedStatement.setLong(i++, fe_suspensionHeightInt); // FE_SUSPENSIONHEIGHT
                        } else {
                            updateArtefactTSDPreparedStatement.setNull(i++, Types.BIGINT);
                        }
                    }
                    if (fe_frequencyBand != null) {
                        Integer fe_frequencyBandInt = Integer.parseInt(fe_frequencyBand);
                        if (fe_frequencyBandInt != null) {
                            updateArtefactTSDPreparedStatement.setLong(i++, fe_frequencyBandInt); // FE_TXRF_FREQUENCY
                        } else {
                            updateArtefactTSDPreparedStatement.setNull(i++, Types.BIGINT);
                        }
                    }
                    updateArtefactTSDPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // update_date
                    updateArtefactTSDPreparedStatement.setLong(i++, createdArtefactExtTSDId); //tsdid

                    log.info("updateArtefactTSDPreparedStatement.executeUpdate()");
                    updateArtefactTSDPreparedStatement.executeUpdate();
                    log.info("successfull insert to database!");

                    //UPDATE NCP
                    String UPDATE_ArtRRst_CREATION_RR_STATUS = "update ARTEFACT_RR_STATUS set RR_STATUS_ID = ?, DATEOFPERFORM = ? where ID = ?";
                    PreparedStatement updateRRstatusInArtRRstPreparedStatement = udbConnect.prepareStatement(UPDATE_ArtRRst_CREATION_RR_STATUS);

                    log.info("RR_STATUS preparedStatement SQL UPDATE VALUES");
                    // set values to update
                    i = 1;
                    updateRRstatusInArtRRstPreparedStatement.setLong(i++, 2); // RR_STATUS
                    updateRRstatusInArtRRstPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // DATEOFPERFORM
                    updateRRstatusInArtRRstPreparedStatement.setLong(i++, createdArtefactRRStatusId); // Artefact RRStatus ID
                    updateRRstatusInArtRRstPreparedStatement.executeUpdate();

                    log.info("ArtefactID: " + createdArtefactId + " successfully RR_STATUS updated to 2");

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
