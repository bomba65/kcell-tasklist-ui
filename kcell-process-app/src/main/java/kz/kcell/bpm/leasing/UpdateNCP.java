package kz.kcell.bpm.leasing;

import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.camunda.spin.Spin.JSON;

@Log
@Service("UpdateNCP")
public class UpdateNCP implements JavaDelegate {

    @Autowired
    DataSource dataSource;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        try {
            TimeZone timeZone = TimeZone.getTimeZone("Asia/Almaty");
            TimeZone.setDefault(timeZone);
            Class.forName("oracle.jdbc.OracleDriver");
            Connection udbConnect = DriverManager.getConnection(
                "jdbc:oracle:thin:@//sc2-appcl010406:1521/apexudb", "udbrnd", "udb");
            try {
                if (udbConnect != null) {
                    udbConnect.setAutoCommit(false);
                    log.info("Connected to the database!");

                    // proc vars
                    int cn_rbs_location = 2;

                    //generated vars
//                    Long ncpCreatedId = delegateExecution.getVariable("ncpCreatedId") != null ? (Long) delegateExecution.getVariable("ncpCreatedId") : null;
//                    Long createdNcpStatusId = delegateExecution.getVariable("createdNcpStatusId") != null ? (Long) delegateExecution.getVariable("createdNcpStatusId") : null;
                    Long createdArtefactId = delegateExecution.getVariable("createdArtefactId") != null ? (Long) delegateExecution.getVariable("createdArtefactId") : null;
                    Long createdArtefactRSDId = delegateExecution.getVariable("createdArtefactRSDId") != null ? (Long) delegateExecution.getVariable("createdArtefactRSDId") : null;
                    Long createdArtefactRRId = delegateExecution.getVariable("createdArtefactRRId") != null ? (Long) delegateExecution.getVariable("createdArtefactRRId") : null;
//                    Long createdArtefactVSDId = delegateExecution.getVariable("createdArtefactVSDId") != null ? (Long) delegateExecution.getVariable("createdArtefactVSDId") : null;
//                    Long createdCandApprovalId = delegateExecution.getVariable("createdCandApprovalId") != null ? (Long) delegateExecution.getVariable("createdCandApprovalId") : null;
//                    Long createdArtefactRRStatusId = delegateExecution.getVariable("createdArtefactRRStatusId") != null ? (Long) delegateExecution.getVariable("createdArtefactRRStatusId") : null;

                    String ncpId = delegateExecution.getVariable("ncpID") != null ? delegateExecution.getVariable("ncpID").toString() : null;
//                    String region = delegateExecution.getVariable("region").toString(); // not number
                    //String longitude = delegateExecution.getVariable("longitude").toString();
                    //String latitude = delegateExecution.getVariable("latitude").toString();
//                    String reason = delegateExecution.getVariable("reason").toString(); // it's string value, not dictionaries id
                    String starter = delegateExecution.getVariable("starter") != null ? delegateExecution.getVariable("starter").toString() : null;
//                    String targetCoverage = delegateExecution.getVariable("targetCoverage").toString();
//                    String regionCode = delegateExecution.getVariable("regionCode").toString();
                    String rbsType = delegateExecution.getVariable("rbsType") != null ? delegateExecution.getVariable("rbsType").toString() : null;
//                    String createNCPTaskComment = delegateExecution.getVariable("createNCPTaskComment").toString();
                    String bandsIdForUDB = delegateExecution.getVariable("bandsIdForUDB") != null ? delegateExecution.getVariable("bandsIdForUDB").toString() : null;
//                    String plannedCabinetTypeIdForUDB = delegateExecution.getVariable("plannedCabinetTypeIdForUDB").toString();

//                    SpinJsonNode reasonJson = JSON(delegateExecution.getVariable("reason"));
//                    SpinJsonNode reason = reasonJson.prop("id");
//                    String reasonString = reason.stringValue();
//                    int reasonInt = Integer.parseInt(reasonString);

                    SpinJsonNode siteTypeJson = delegateExecution.getVariable("siteType") != null ? JSON(delegateExecution.getVariable("siteType")) : null;
                    SpinJsonNode siteType = siteTypeJson != null && siteTypeJson.hasProp("id") ? siteTypeJson.prop("id") : null;
                    String siteTypeString = siteType != null ? siteType.stringValue() : null;
                    Integer siteTypeInt = siteTypeString != null ? Integer.parseInt(siteTypeString) : null;

//                    SpinJsonNode projectJson = JSON(delegateExecution.getVariable("project"));
//                    SpinJsonNode project = projectJson.prop("id");
//                    String projectString = project.stringValue();
//                    int projectInt = Integer.parseInt(projectString);

//                    SpinJsonNode initiatorJson = JSON(delegateExecution.getVariable("initiator"));
//                    SpinJsonNode initiator = initiatorJson.prop("id");
//                    String initiatorString = initiator.stringValue();
//                    int initiatorInt = Integer.parseInt(initiatorString);

//                    SpinJsonNode partJson = JSON(delegateExecution.getVariable("part"));
//                    SpinJsonNode part = partJson.prop("id");
//                    String partString = part.stringValue();
//                    int partInt = Integer.parseInt(partString);

                    SpinJsonNode candidate = delegateExecution.getVariable("candidate") != null ? JSON(delegateExecution.getVariable("candidate")) : null;
                    String cn_longitude = candidate != null && candidate.hasProp("longitude") && candidate.prop("longitude") != null ? candidate.prop("longitude").stringValue() : null;
                    String cn_latitude = candidate != null && candidate.hasProp("latitude") && candidate.prop("latitude") != null ? candidate.prop("latitude").stringValue() : null;
                    String cn_siteName = candidate != null && candidate.hasProp("siteName") && candidate.prop("siteName") != null ? candidate.prop("siteName").stringValue() : null;
                    String cn_comments = candidate != null && candidate.hasProp("comments") && candidate.prop("comments") != null ? candidate.prop("comments").stringValue() : null;
                    Number cn_square = candidate != null && candidate.hasProp("square") && candidate.prop("square") != null ? candidate.prop("square").numberValue() : null;
                    String cn_constructionType = candidate != null && candidate.hasProp("constructionType") && candidate.prop("constructionType").hasProp("id") && candidate.prop("constructionType").prop("id") != null ? candidate.prop("constructionType").prop("id").stringValue() : null;

                    Integer cn_altitude = candidate != null && candidate.hasProp("cn_altitude") ? Integer.parseInt(candidate.prop("cn_altitude") != null ? candidate.prop("cn_altitude").value().toString() : "0") : 0;

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-dd-MM"); //2020-01-02T18:00:00.000Z
                    String cn_date_of_visit = candidate.prop("dateOfVisit").stringValue().substring(0, 9);
                    Date date_of_visit = formatter.parse(cn_date_of_visit);

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

                    //UPDATE ARTEFACT
                    String UPDATENCP = "update ARTEFACT set NCPID = ?, SITENAME = ? where ARTEFACTID = ?";
                    PreparedStatement updatePreparedStatement = udbConnect.prepareStatement(UPDATENCP);

                    int i = 1;
                    log.info("SET preparedStatement SQL UPDATE VALUES");
                    // set values to update
                    Integer ncpIdInt = ncpId != null ? Integer.parseInt(ncpId) : null;
                    updatePreparedStatement.setLong(i++, ncpIdInt); // NCPID
                    updatePreparedStatement.setString(i++, cn_siteName); // SITENAME
                    updatePreparedStatement.setLong(i++, createdArtefactId); // ARTEFACTID

                    updatePreparedStatement.executeUpdate();
                    log.info("successfull insert to database!");

                    //UPDATE ARTEFACT
                    String UpdateArtefactCurrentState = "update ARTEFACT_CURRENT_STATE set ncpid = ?, cand_status_person = ?, longitude = ?, latitude = ?, rbs_type = ?, bsc = ?, band = ?, rbs_location = ?, construction_height = ?, construction_type = ?, address = ?, contact_person = ?, comments = ?, pl_comments = ? where ARTEFACTID = ?";
                    PreparedStatement updateArtefactCurrentStatePreparedStatement = udbConnect.prepareStatement(UpdateArtefactCurrentState);
                    Integer rbsTypeInt = rbsType != null ? Integer.parseInt(rbsType) : null;
                    Integer cnConstructionTypeInt = cn_constructionType != null ? Integer.parseInt(cn_constructionType) : null;
                    Integer bandsIdForUDBInt = bandsIdForUDB != null ? Integer.parseInt(bandsIdForUDB) : null;
                    String contractInfoString = renterCompany != null && renterCompany.hasProp("contactInfo") && !renterCompany.prop("contactInfo").equals(null) ? renterCompany.prop("contactInfo").stringValue() : "";
                    i = 1;
                    log.info("preparedStatement.setValues");
                    // set values to insert
                    updateArtefactCurrentStatePreparedStatement.setLong(i++, ncpIdInt); //ncpid
                    updateArtefactCurrentStatePreparedStatement.setString(i++, starter); //cand_status_person (пока стартер)
                    updateArtefactCurrentStatePreparedStatement.setString(i++, cn_longitude); //longitude
                    updateArtefactCurrentStatePreparedStatement.setString(i++, cn_latitude); //latitude
                    updateArtefactCurrentStatePreparedStatement.setLong(i++, rbsTypeInt); // RBS_TYPE (cn_rbs_type)
                    updateArtefactCurrentStatePreparedStatement.setLong(i++, cn_bscInt); // BSC (cn_bsc)
                    updateArtefactCurrentStatePreparedStatement.setLong(i++, bandsIdForUDBInt); // BAND (cn_band)
                    updateArtefactCurrentStatePreparedStatement.setLong(i++, cn_rbs_location); // RBS_LOCATION (cn_rbs_location) //
                    updateArtefactCurrentStatePreparedStatement.setLong(i++, 3); //construction_height // (cn_height_constr) еще не реализовано в данной версии
                    updateArtefactCurrentStatePreparedStatement.setLong(i++, cnConstructionTypeInt); //construction_type
                    updateArtefactCurrentStatePreparedStatement.setString(i++, cn_address); //address
                    updateArtefactCurrentStatePreparedStatement.setString(i++, contact_person); //contact_person
                    updateArtefactCurrentStatePreparedStatement.setString(i++, contractInfoString);
                    updateArtefactCurrentStatePreparedStatement.setString(i++, cn_comments); //pl_comments
                    updateArtefactCurrentStatePreparedStatement.setLong(i++, createdArtefactId); //ARTEFACTID

                    log.info("preparedStatement.executeUpdate()");
                    updateArtefactCurrentStatePreparedStatement.executeUpdate();
                    log.info("successfull insert to database!");

                    //UPDATE ARTEFACT_RSD
                    String UpdateArtefactRsd = "UPDATE APP_APEXUDB_CAMUNDA.ARTEFACT_RSD SET ARTEFACTID = ?, BSCID = ?, ALTITUDE = ?, CNSTRTYPEID = ?, HEIGHT = ?, DATEOFINSERT = ?, DATEOFVISIT = ?, CONTACTPERSON = ?, COMMENTS = ?, RBSID = ?, SITE_TYPE = ? WHERE RSDID = ?";
                    PreparedStatement updateArtefactRsdPreparedStatement = udbConnect.prepareStatement(UpdateArtefactRsd);

                    i = 1;
                    log.info("preparedStatement.setValues");
                    java.sql.Date dateOfVisitDate = date_of_visit != null ? new java.sql.Date(date_of_visit.getTime()) : null;
                    // set values to update
                    updateArtefactRsdPreparedStatement.setLong(i++, createdArtefactId); //ARTEFACTID
                    updateArtefactRsdPreparedStatement.setLong(i++, cn_bscInt); //BSCID
                    updateArtefactRsdPreparedStatement.setLong(i++, cn_altitude); //ALTITUDE
                    updateArtefactRsdPreparedStatement.setLong(i++, cnConstructionTypeInt); //CNSTRTYPEID
                    updateArtefactRsdPreparedStatement.setLong(i++, 3); // CONSTRUCTION_HEIGHT (cn_height_constr) еще не реализовано в данной версии
                    updateArtefactRsdPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // INSERT_DATE
                    updateArtefactRsdPreparedStatement.setDate(i++, dateOfVisitDate); // DATE OF VISIT (cn_date_visit)
                    updateArtefactRsdPreparedStatement.setString(i++, contact_person); //CONTACTPERSON
                    updateArtefactRsdPreparedStatement.setString(i++, cn_comments); //COMMENTS
                    updateArtefactRsdPreparedStatement.setLong(i++, rbsTypeInt); //RBSID
                    updateArtefactRsdPreparedStatement.setLong(i++, siteTypeInt); //SITE_TYPE
                    updateArtefactRsdPreparedStatement.setLong(i++, createdArtefactRSDId); //RSDID

                    log.info("preparedStatement.executeUpdate()");
                    updateArtefactRsdPreparedStatement.executeUpdate();
                    log.info("successfull insert to database!");

                    //UPDATE ARTEFACT_RR
                    String UpdateArtefactRR = "UPDATE APP_APEXUDB_CAMUNDA.ARTEFACT_RR SET ARTEFACTID = ?, DATEOFVISIT = ?, ADDRESS = ?, LATITUDE = ?, LONGITUDE = ?, CONSTR_TYPE = ?, SQUARE = ?, RBS_TYPE = ?, BAND = ?, RBS_LOCATION = ?, COMMENTS = ? WHERE RR_ID = ?";
                    PreparedStatement updateArtefactRRPreparedStatement = udbConnect.prepareStatement(UpdateArtefactRR);

                    i = 1;
                    log.info("preparedStatement.setValues");
                    // set values to update
                    updateArtefactRRPreparedStatement.setLong(i++, createdArtefactId); //ARTEFACTID
                    updateArtefactRRPreparedStatement.setDate(i++, dateOfVisitDate); // DATEOFVISIT
                    updateArtefactRRPreparedStatement.setString(i++, cn_address); //ADDRESS
                    updateArtefactRRPreparedStatement.setString(i++, cn_latitude); //LATITUDE
                    updateArtefactRRPreparedStatement.setString(i++, cn_longitude); //LONGITUDE
                    updateArtefactRRPreparedStatement.setLong(i++, cnConstructionTypeInt); //CONSTR_TYPE
                    updateArtefactRRPreparedStatement.setLong(i++, cn_square.longValue()); //SQUARE
                    updateArtefactRRPreparedStatement.setLong(i++, rbsTypeInt); //RBS_TYPE
                    updateArtefactRRPreparedStatement.setString(i++, bandsIdForUDB); //BAND
                    updateArtefactRRPreparedStatement.setLong(i++, cn_rbs_location); //RBS_LOCATION
                    updateArtefactRRPreparedStatement.setString(i++, cn_comments); //COMMENTS
                    updateArtefactRRPreparedStatement.setLong(i++, createdArtefactRRId); //RR_ID

                    log.info("preparedStatement.executeUpdate()");
                    updateArtefactRRPreparedStatement.executeUpdate();
                    log.info("successfull insert to database!");


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
        } catch (Exception e) {
            log.info("testConnect Exception!");
            e.printStackTrace();
        }

    }
}
