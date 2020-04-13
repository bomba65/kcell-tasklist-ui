package kz.kcell.bpm.leasing;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

import static org.camunda.spin.Spin.JSON;

@Service("CreateNCP")
public class CreateNCP implements JavaDelegate {

    @Autowired
    DataSource dataSource;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        try {
            TimeZone timeZone = TimeZone.getTimeZone("Asia/Almaty");
            TimeZone.setDefault(timeZone);
            Class.forName ("oracle.jdbc.OracleDriver");
            Connection udbConnect = DriverManager.getConnection(
                "jdbc:oracle:thin:@//sc2-appcl010406:1521/apexudb", "app_apexudb_camunda", "p28zt#7C");
            try {
                if (udbConnect != null) {
                    udbConnect.setAutoCommit(false);
                    System.out.println("Connected to the database!");

                    //insert NCP
                    Long ncpCreatedId = null;
                    String returnCols[] = { "ARTEFACTID" };
                    String insertNCP = "INSERT INTO APP_APEXUDB_CAMUNDA.NCP_CREATION ( ARTEFACTID, NCPID, REGION, LONGITUDE, LATITUDE, REASON, PROJECT, CREATOR, DATEOFINSERT, COMMENTS, CABINETID, TARGET_COVERAGE, TYPE, GEN_STATUS, NCP_STATUS, NCP_STATUS_DATE, BAND, INITIATOR, PART) VALUES (NCP_CREATION_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 7, 1, ?, ?, ?, ?)";
                    PreparedStatement preparedStatement = udbConnect.prepareStatement(insertNCP, returnCols);

                    int i = 1;
                    System.out.println("preparedStatement.setValues");

                    // proc vars
                    int cn_rbs_location = 2;
                    String ncpId = delegateExecution.getVariable("ncpID").toString();
//                    String region = delegateExecution.getVariable("region").toString(); // not number
                    String longitude = delegateExecution.getVariable("longitude").toString();
                    String latitude = delegateExecution.getVariable("latitude").toString();
//                    String reason = delegateExecution.getVariable("reason").toString(); // it's string value, not dictionaries id
                    String starter = delegateExecution.getVariable("starter").toString();
                    String targetCoverage = delegateExecution.getVariable("targetCoverage").toString();
                    String regionCode = delegateExecution.getVariable("regionCode").toString();
                    String rbsType = delegateExecution.getVariable("rbsType").toString();
                    String createNCPTaskComment = delegateExecution.getVariable("createNCPTaskComment").toString();
                    String bandsIdForUDB = delegateExecution.getVariable("bandsIdForUDB").toString();
                    String plannedCabinetTypeIdForUDB = delegateExecution.getVariable("plannedCabinetTypeIdForUDB").toString();

                    SpinJsonNode reasonJson = JSON(delegateExecution.getVariable("reason"));
                    SpinJsonNode reason = reasonJson.prop("id");
                    String reasonString = reason.stringValue();
                    int reasonInt = Integer.parseInt(reasonString);

                    SpinJsonNode siteTypeJson = JSON(delegateExecution.getVariable("siteType"));
                    SpinJsonNode siteType = siteTypeJson.prop("id");
                    String siteTypeString = siteType.stringValue();
                    int siteTypeInt = Integer.parseInt(siteTypeString);

                    SpinJsonNode projectJson = JSON(delegateExecution.getVariable("project"));
                    SpinJsonNode project = projectJson.prop("id");
                    String projectString = project.stringValue();
                    int projectInt = Integer.parseInt(projectString);

                    SpinJsonNode initiatorJson = JSON(delegateExecution.getVariable("initiator"));
                    SpinJsonNode initiator = initiatorJson.prop("id");
                    String initiatorString = initiator.stringValue();
                    int initiatorInt = Integer.parseInt(initiatorString);

                    SpinJsonNode partJson = JSON(delegateExecution.getVariable("part"));
                    SpinJsonNode part = partJson.prop("id");
                    String partString = part.stringValue();
                    int partInt = Integer.parseInt(partString);

                    SpinJsonNode candidate = JSON(delegateExecution.getVariable("candidate"));
                    String cn_longitude = candidate.prop("longitude").stringValue();
                    String cn_latitude = candidate.prop("latitude").stringValue();
                    String cn_siteName = candidate.prop("siteName").stringValue();
                    String cn_comments = candidate.hasProp("comments")?candidate.prop("comments").stringValue():null;
                    Number cn_square = candidate.prop("square").numberValue();
                    String cn_constructionType = candidate.hasProp("constructionType") ? candidate.prop("constructionType").prop("id").stringValue() : "0";

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-dd-MM"); //2020-01-02T18:00:00.000Z
                    String cn_date_of_visit = candidate.prop("dateOfVisit").stringValue().substring(0,9);
                    Date date_of_visit = formatter.parse(cn_date_of_visit);


                    //{"antennaType":"ML 0.6","diameter":"0.6","weight":"14","antennaQuantity":1,"frequencyBand":"8GHz","suspensionHeight":12,"azimuth":"123"}
                    SpinJsonNode ne = JSON(delegateExecution.getVariable("transmissionAntenna"));
                    String ne_azimuth = ne.hasProp("azimuth") ? ne.prop("azimuth").stringValue() : "0";
                    String ne_diameter = ne.hasProp("diameter") ? ne.prop("diameter").stringValue() : "0";
                    String ne_frequencyBand = "8";//ne.prop("frequencyBand").numberValue();
                    Number ne_suspensionHeight = ne.prop("suspensionHeight").numberValue();

//                    farEndInformation

                    SpinJsonNode feJson = JSON(delegateExecution.getVariable("farEndInformation"));
                    SpinList farEnds = feJson.elements();
                    SpinJsonNode fe = (SpinJsonNode) farEnds.get(0);
                    String fe_azimuth = fe.prop("azimuth").stringValue();
                    String fe_diameter = fe.prop("diameter").stringValue();
                    String fe_frequencyBand = "8"; //fe.prop("frequencyBand").numberValue();
                    String fe_suspensionHeight = fe.prop("suspensionHeight").stringValue();
                    String fe_constructionType = fe.prop("constructionType").prop("id").stringValue();
                    String fe_sitename = fe.hasProp("farEndName") ? fe.prop("farEndName").stringValue() : "";
                    String fe_comment = fe.hasProp("comments") ? fe.prop("comments").stringValue() : "";
                    String fe_survey_date = fe.prop("surveyDate").stringValue().substring(0,9);
                    Date fe_formated_survey_date = formatter.parse(fe_survey_date);

                    String fe_address = "" + (fe.prop("address").hasProp("cn_addr_oblast") ? fe.prop("address").prop("cn_addr_oblast").stringValue() : "") +
                        (fe.prop("address").hasProp("cn_addr_district") ? ", " + fe.prop("address").prop("cn_addr_district").stringValue() : "") +
                        (fe.prop("address").hasProp("cn_addr_city") ? ", " + fe.prop("address").prop("cn_addr_city").stringValue() : "") +
                        (fe.prop("address").hasProp("cn_addr_street") ? ", " + fe.prop("address").prop("cn_addr_street").stringValue() : "") +
                        (fe.prop("address").hasProp("cn_addr_building") ? ", " + fe.prop("address").prop("cn_addr_building").stringValue() : "") +
                        (fe.prop("address").hasProp("cn_addr_cadastral_number") ? ", " + fe.prop("address").prop("cn_addr_cadastral_number").stringValue() : "") +
                        (fe.prop("address").hasProp("cn_addr_note") ? ", " + fe.prop("address").prop("cn_addr_note").stringValue() : "");

                    SpinJsonNode renterCompany = JSON(delegateExecution.getVariable("renterCompany"));
                    String contact_person = "" + (!renterCompany.prop("contactName").equals(null) ? renterCompany.prop("contactName").stringValue() : "") +
                        " " + (!renterCompany.prop("contactPosition").equals(null) ? renterCompany.prop("contactPosition").stringValue() : "") +
                        " ( tel:" + (!renterCompany.prop("telFax").equals(null) ? renterCompany.prop("telFax").stringValue() : "") + ")";

                    //{"cn_addr_oblast":"область Акмолинская","cn_addr_district":"","cn_addr_city":"г. Кокшетау","cn_addr_street":"фывфв","ca_not_full_addres":false,"cn_addr_building":"фывыфв"}
                    SpinJsonNode address = JSON(delegateExecution.getVariable("address"));
                    String cn_address = "" + (address.hasProp("cn_addr_oblast") ? address.prop("cn_addr_oblast").stringValue() : "") +
                        (address.hasProp("cn_addr_district") ? ", " + address.prop("cn_addr_district").stringValue() : "") +
                        (address.hasProp("cn_addr_city") ? ", " + address.prop("cn_addr_city").stringValue() : "") +
                        (address.hasProp("cn_addr_street") ? ", " + address.prop("cn_addr_street").stringValue() : "") +
                        (address.hasProp("cn_addr_building") ? ", " + address.prop("cn_addr_building").stringValue() : "") +
                        (address.hasProp("cn_addr_cadastral_number") ? ", " + address.prop("cn_addr_cadastral_number").stringValue() : "") +
                        (address.hasProp("cn_addr_note") ? ", " + address.prop("cn_addr_note").stringValue() : "");

                    String cn_bsc = candidate.prop("bsc").prop("id").stringValue();
                    int cn_bscInt = Integer.parseInt(cn_bsc);

                    // set values to insert
                    preparedStatement.setString(i++, ncpId); // NCPID
                    preparedStatement.setLong(i++, Integer.parseInt(regionCode)); // REGION
                    preparedStatement.setString(i++, longitude); // LONGITUDE ex. E 76,890775
                    preparedStatement.setString(i++, latitude); // LATITUDE
                    preparedStatement.setLong(i++, reasonInt); // REASON
                    preparedStatement.setLong(i++, projectInt); // PROJECT
                    preparedStatement.setString(i++, starter); // CREATOR 'SERGEI.ZAITSEV'
                    preparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // DATEOFINSERT
                    preparedStatement.setString(i++, createNCPTaskComment); // COMMENTS
                    preparedStatement.setLong(i++, Integer.parseInt(plannedCabinetTypeIdForUDB)); // CABINETID
                    preparedStatement.setString(i++, targetCoverage); // TARGET_COVERAGE
                    preparedStatement.setLong(i++, siteTypeInt); // TYPE ncp_type (Подставлять ID согласно справочнику Site Type) ex: 6
                    preparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // NCP_STATUS_DATE
                    preparedStatement.setString(i++, bandsIdForUDB); // BAND ex:'1'   ncp_band	(Подставлять ID согласно справочнику Bands)
                    preparedStatement.setLong(i++, initiatorInt); // INITIATOR
                    preparedStatement.setLong(i++, partInt); // PART (Подставлять ID согласно справочнику Part)

                    System.out.println("preparedStatement.executeUpdate()");
                    preparedStatement.executeUpdate();
                    System.out.println("successfull insert to database!");
//
                    ResultSet headGeneratedIdResultSet = preparedStatement.getGeneratedKeys();
                    headGeneratedIdResultSet.next();
                    ncpCreatedId = headGeneratedIdResultSet.getLong(1);
                    System.out.println("artefactGeneratedId:");
                    System.out.println(ncpCreatedId);

                    //insert new status
                    Long createdNcpStatusId = null;
                    String returnStatus[] = { "STATUS_ACTION_ID" };
                    String insertNewStatus = "insert into NCP_CREATION_STATUS_ACTION values ( NCP_CREATION_STATUS_ACTIO_SEQ.nextval, ?, 2, ?, ?, null)";
                    PreparedStatement newNcpStatusPreparedStatement = udbConnect.prepareStatement(insertNewStatus, returnStatus);

                    i = 1;
                    System.out.println("newNcpStatusPreparedStatement.setString");
                    newNcpStatusPreparedStatement.setString(i++, ncpCreatedId.toString());
                    newNcpStatusPreparedStatement.setString(i++, starter); // CREATOR 'SERGEI.ZAITSEV'
                    newNcpStatusPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // DATEOFINSERT
                    System.out.println("newNcpStatusPreparedStatement.executeUpdate()");
                    newNcpStatusPreparedStatement.executeUpdate();
                    System.out.println("successfull insert to database!");

                    ResultSet statusGeneratedIdResultSet = newNcpStatusPreparedStatement.getGeneratedKeys();
                    statusGeneratedIdResultSet.next();
                    createdNcpStatusId = statusGeneratedIdResultSet.getLong(1);
                    System.out.println("createdNcpStatusId:");
                    System.out.println(createdNcpStatusId);

                    //insert new Candidate (in Artefact table)
                    Long createdArtefactId = null;
                    String returnArtefactId[] = { "ARTEFACTID" };
                    String insertNewArtefact = "insert into ARTEFACT values (ARTEFACT_SEQ.nextval, ?, ?)";
                    PreparedStatement newArtefactPreparedStatement = udbConnect.prepareStatement(insertNewArtefact, returnArtefactId);

                    i = 1;
                    System.out.println("newArtefactPreparedStatement.setString");
                    newArtefactPreparedStatement.setString(i++, cn_siteName); // sitename	cn_sitename
                    newArtefactPreparedStatement.setLong(i++, Integer.parseInt(ncpId)); //ncp_id
                    System.out.println("newArtefactPreparedStatement.executeUpdate()");
                    newArtefactPreparedStatement.executeUpdate();
                    System.out.println("successfull insert to database!");

                    ResultSet createdArtefactIdResultSet = newArtefactPreparedStatement.getGeneratedKeys();
                    createdArtefactIdResultSet.next();
                    createdArtefactId = createdArtefactIdResultSet.getLong(1);
                    System.out.println("createdArtefactId:");
                    System.out.println(createdArtefactId);

                    //insert new Candidate (in ARTEFACT_CURRENT_STATE table)
                    String insertNewArtefactCurrentState = "INSERT INTO APP_APEXUDB_CAMUNDA.ARTEFACT_CURRENT_STATE (ARTEFACTID,\n" +
                        "                                                        NCPID,\n" +
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
                        "                                                        PL_COMMENTS) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement newArtefactCurrentStatePreparedStatement = udbConnect.prepareStatement(insertNewArtefactCurrentState);

                    i = 1;
                    System.out.println("newArtefactCurrentStatePreparedStatement.setString");
                    newArtefactCurrentStatePreparedStatement.setLong(i++, createdArtefactId); //ARTEFACTID
                    newArtefactCurrentStatePreparedStatement.setLong(i++, Integer.parseInt(ncpId)); //ncp_id
                    newArtefactCurrentStatePreparedStatement.setLong(i++, 1); // CAND_STATUS first insert value mast be = 1
                    newArtefactCurrentStatePreparedStatement.setString(i++, starter); // CAND_STATUS_PERSON (current user) // current or start ?????? (who complete create candidate task?)
                    newArtefactCurrentStatePreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // CAND_STATUS_DATE
                    newArtefactCurrentStatePreparedStatement.setLong(i++, 1); // RR_STATUS first insert value mast be = 1
                    newArtefactCurrentStatePreparedStatement.setString(i++, starter); // RR_STATUS_PERSON (current user)
                    newArtefactCurrentStatePreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // RR_STATUS_DATE
                    newArtefactCurrentStatePreparedStatement.setString(i++, cn_longitude); // LONGITUDE ex: E 80 50 42,4
                    newArtefactCurrentStatePreparedStatement.setString(i++, cn_latitude); // LATITUDE ex: N 48 48 05,6
                    newArtefactCurrentStatePreparedStatement.setLong(i++, Integer.parseInt(rbsType)); // RBS_TYPE (cn_rbs_type)
                    newArtefactCurrentStatePreparedStatement.setLong(i++, cn_bscInt); // BSC (cn_bsc)
                    newArtefactCurrentStatePreparedStatement.setLong(i++, Integer.parseInt(bandsIdForUDB)); // BAND (cn_band)
                    newArtefactCurrentStatePreparedStatement.setLong(i++, cn_rbs_location); // RBS_LOCATION (cn_rbs_location) //
                    newArtefactCurrentStatePreparedStatement.setLong(i++, 689); // ALTITUDE (cn_altitude) еще не реализовано в данной версии
                    newArtefactCurrentStatePreparedStatement.setLong(i++, 3); // CONSTRUCTION_HEIGHT (cn_height_constr) еще не реализовано в данной версии
                    newArtefactCurrentStatePreparedStatement.setLong(i++, Integer.parseInt(cn_constructionType)); // CONSTRUCTION_TYPE (cn_construction_type) еще не реализовано в данной версии
                    newArtefactCurrentStatePreparedStatement.setString(i++, cn_address); // ADDRESS "Восточно-Казахстанская область, Зайсанский район, село Карабулак, водонапорная башня"
                    newArtefactCurrentStatePreparedStatement.setString(i++, contact_person); // CONTACT_PERSON "Мария Николаевна Специалист акимата 8 701 479 19 86"
                    newArtefactCurrentStatePreparedStatement.setString(i++, (!renterCompany.prop("contactInfo").equals(null) ? renterCompany.prop("contactInfo").stringValue() : "")); // COMMENTS (cn_contact_information)
                    newArtefactCurrentStatePreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // INSERT_DATE
                    newArtefactCurrentStatePreparedStatement.setString(i++, starter); // INSERT_PERSON
                    newArtefactCurrentStatePreparedStatement.setLong(i++, 7); // GS_STATUS
                    newArtefactCurrentStatePreparedStatement.setString(i++, cn_comments); // PL_COMMENTS (cn_comments)
                    System.out.println("newArtefactCurrentStatePreparedStatement.executeUpdate()");
                    newArtefactCurrentStatePreparedStatement.executeUpdate();
                    System.out.println("successfull insert to database!");

                    //insert new installation

                    String insertNewInstallation = "insert into INSTALLATION_CURRENT_STATE(ARTEFACTID) values (?)";
                    PreparedStatement NewInstallationPreparedStatement = udbConnect.prepareStatement(insertNewInstallation);

                    i = 1;
                    System.out.println("NewInstallationPreparedStatement.setString");
                    NewInstallationPreparedStatement.setLong(i++, createdArtefactId);
                    System.out.println("NewInstallationPreparedStatement.executeUpdate()");
                    NewInstallationPreparedStatement.executeUpdate();
                    System.out.println("successfull insert to database!");

                    //insert new Candidate (in ARTEFACT_RSD table)
                    Long createdArtefactRSDId = null;
                    String rsdReturnStatus[] = { "RSDID" };
                    String insertNewArtefactRSD = "INSERT INTO APP_APEXUDB_CAMUNDA.ARTEFACT_RSD (RSDID, ARTEFACTID, BSCID, CNSTRTYPEID, HEIGHT, DATEOFINSERT, DATEOFVISIT, CONTACTPERSON, COMMENTS, STATE, RBSID, SITE_TYPE) VALUES ( ARTEFACT_RSD_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement newArtefactRSDPreparedStatement = udbConnect.prepareStatement(insertNewArtefactRSD, rsdReturnStatus);

                    i = 1;
                    System.out.println("newArtefactRSDPreparedStatement.setString");
                    newArtefactRSDPreparedStatement.setLong(i++, createdArtefactId); //ARTEFACTID
                    newArtefactRSDPreparedStatement.setLong(i++, cn_bscInt); //BSCID
                    newArtefactRSDPreparedStatement.setLong(i++, Integer.parseInt(cn_constructionType)); //CNSTRTYPEID
                    newArtefactRSDPreparedStatement.setLong(i++, 3); // CONSTRUCTION_HEIGHT (cn_height_constr) еще не реализовано в данной версии
                    newArtefactRSDPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // INSERT_DATE
                    newArtefactRSDPreparedStatement.setDate(i++, new java.sql.Date(date_of_visit.getTime())); // DATE OF VISIT (cn_date_visit)
                    newArtefactRSDPreparedStatement.setString(i++, contact_person); //CONTACTPERSON
                    newArtefactRSDPreparedStatement.setString(i++, cn_comments); //COMMENTS
                    newArtefactRSDPreparedStatement.setLong(i++, 2); //STATE
                    newArtefactRSDPreparedStatement.setLong(i++, Integer.parseInt(rbsType)); //RBSID
                    newArtefactRSDPreparedStatement.setLong(i++, siteTypeInt); //SITE_TYPE
                    System.out.println("newArtefactRSDPreparedStatement.executeUpdate()");
                    newArtefactRSDPreparedStatement.executeUpdate();
                    System.out.println("successfull insert to database!");

                    ResultSet artefactRSDGeneratedIdResultSet = newArtefactRSDPreparedStatement.getGeneratedKeys();
                    artefactRSDGeneratedIdResultSet.next();
                    createdArtefactRSDId = artefactRSDGeneratedIdResultSet.getLong(1);
                    System.out.println("createdArtefactRSDId:");
                    System.out.println(createdArtefactRSDId);

                    //insert new Candidate (in ARTEFACT_RR table)
                    Long createdArtefactRRId = null;
                    String rrReturnStatus[] = { "RR_ID" };
                    String insertNewArtefactRR = "INSERT INTO APP_APEXUDB_CAMUNDA.ARTEFACT_RR (RR_ID, ARTEFACTID, DATEOFVISIT, ADDRESS, LATITUDE, LONGITUDE, CONSTR_TYPE, SQUARE, RBS_TYPE, BAND, RBS_LOCATION, COMMENTS, DATEOFCREATION, CREATOR) VALUES (ARTEFACT_RR_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement newArtefactRRPreparedStatement = udbConnect.prepareStatement(insertNewArtefactRR, rrReturnStatus);

                    i = 1;
                    System.out.println("newArtefactRRPreparedStatement.setString");
                    newArtefactRRPreparedStatement.setLong(i++, createdArtefactId); //ARTEFACTID
                    newArtefactRRPreparedStatement.setDate(i++, new java.sql.Date(date_of_visit.getTime())); // DATEOFVISIT
                    newArtefactRRPreparedStatement.setString(i++, cn_address); //ADDRESS
                    newArtefactRRPreparedStatement.setString(i++, latitude); //LATITUDE
                    newArtefactRRPreparedStatement.setString(i++, longitude); //LONGITUDE
                    newArtefactRRPreparedStatement.setLong(i++, Integer.parseInt(cn_constructionType)); //CONSTR_TYPE
                    newArtefactRRPreparedStatement.setLong(i++, cn_square.longValue()); //SQUARE
                    newArtefactRRPreparedStatement.setLong(i++, Integer.parseInt(rbsType)); //RBS_TYPE
                    newArtefactRRPreparedStatement.setString(i++, bandsIdForUDB); //BAND
                    newArtefactRRPreparedStatement.setLong(i++, cn_rbs_location); //RBS_LOCATION
                    newArtefactRRPreparedStatement.setString(i++, cn_comments); //COMMENTS
                    newArtefactRRPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // DATEOFCREATION
                    newArtefactRRPreparedStatement.setString(i++, cn_comments); //CREATOR
                    System.out.println("newArtefactRRPreparedStatement.executeUpdate()");
                    newArtefactRRPreparedStatement.executeUpdate();
                    System.out.println("successfull insert to database!");

                    ResultSet artefactRRGeneratedIdResultSet = newArtefactRRPreparedStatement.getGeneratedKeys();
                    artefactRRGeneratedIdResultSet.next();
                    createdArtefactRRId = artefactRRGeneratedIdResultSet.getLong(1);
                    System.out.println("createdArtefactRRId:");
                    System.out.println(createdArtefactRRId);

                    //insert ARTEFACT_VSD
                    Long createdArtefactVsdId = null;
                    String vsdReturnStatus[] = { "VSDID" };
                    String insertNewVsd = "INSERT INTO APP_APEXUDB_CAMUNDA.ARTEFACT_VSD (VSDID, ARTEFACTID, RSDID) VALUES (ARTEFACT_VSD_SEQ.nextval, ?, ?)";
                    PreparedStatement newArtefactVsdPreparedStatement = udbConnect.prepareStatement(insertNewVsd, vsdReturnStatus);

                    i = 1;
                    System.out.println("newArtefactVsdPreparedStatement.setString");
                    newArtefactVsdPreparedStatement.setLong(i++, createdArtefactId);
                    newArtefactVsdPreparedStatement.setLong(i++, createdArtefactRSDId);
                    System.out.println("newArtefactVsdPreparedStatement.executeUpdate()");
                    newArtefactVsdPreparedStatement.executeUpdate();
                    System.out.println("successfull insert to database!");

                    ResultSet vsdGeneratedIdResultSet = newArtefactVsdPreparedStatement.getGeneratedKeys();
                    vsdGeneratedIdResultSet.next();
                    createdArtefactVsdId = vsdGeneratedIdResultSet.getLong(1);
                    System.out.println("createdArtefactVsdId:");
                    System.out.println(createdArtefactVsdId);

                    //insert CANDAPPROVAL
                    Long createdCandApprovalId = null;
                    String candApprovalReturnStatus[] = { "ID" };
                    String insertNewCandApproval = "insert into CANDAPPROVAL(ID, ARTEFACTID, RSDID) values (CANDAPPROVAL_SEQ.nextval, ?, ?)";
                    PreparedStatement newCandApprovalPreparedStatement = udbConnect.prepareStatement(insertNewCandApproval, candApprovalReturnStatus);

                    i = 1;
                    System.out.println("newCandApprovalPreparedStatement.setString");
                    newCandApprovalPreparedStatement.setLong(i++, createdArtefactId);
                    newCandApprovalPreparedStatement.setLong(i++, createdArtefactRSDId);
                    System.out.println("newCandApprovalPreparedStatement.executeUpdate()");
                    newCandApprovalPreparedStatement.executeUpdate();
                    System.out.println("successfull insert to database!");

                    ResultSet candApprovalGeneratedIdResultSet = newCandApprovalPreparedStatement.getGeneratedKeys();
                    candApprovalGeneratedIdResultSet.next();
                    createdCandApprovalId = candApprovalGeneratedIdResultSet.getLong(1);
                    System.out.println("createdCandApprovalId:");
                    System.out.println(createdCandApprovalId);

                    //insert ARTEFACT_RR_STATUS
                    Long createdArtefactRRStatusId = null;
                    String artefactRRStatusReturnStatus[] = { "ID" };
                    String insertNewArtefactRRStatus = "insert into ARTEFACT_RR_STATUS(ID, ARTEFACTID, RR_ID, DATEOFPERFORM) values (ARTEFACT_RR_STATUS_SEQ.nextval, ?, ?, ?)";
                    PreparedStatement newArtefactRRStatusPreparedStatement = udbConnect.prepareStatement(insertNewArtefactRRStatus, artefactRRStatusReturnStatus);

                    i = 1;
                    System.out.println("newArtefactRRStatusPreparedStatement.setString");
                    newArtefactRRStatusPreparedStatement.setLong(i++, createdArtefactId);
                    newArtefactRRStatusPreparedStatement.setLong(i++, createdArtefactRSDId);
                    newArtefactRRStatusPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime()));
                    System.out.println("newArtefactRRStatusPreparedStatement.executeUpdate()");
                    newArtefactRRStatusPreparedStatement.executeUpdate();
                    System.out.println("successfull insert to database!");

                    ResultSet artefactRRStatusGeneratedIdResultSet = newArtefactRRStatusPreparedStatement.getGeneratedKeys();
                    artefactRRStatusGeneratedIdResultSet.next();
                    createdArtefactRRStatusId = artefactRRStatusGeneratedIdResultSet.getLong(1);
                    System.out.println("createdArtefactRRStatusId:");
                    System.out.println(createdArtefactRRStatusId);

                    udbConnect.commit();
                    //insert ARTEFACT_TSD_EXT
                    Long createdArtefactExtTSDId = null;
                    String artefactExtTSDReturnStatus[] = { "TSDID" };
                    String insertNewArtefactExtTSD = "INSERT INTO APP_APEXUDB_CAMUNDA.ARTEFACT_TSD_EXT (TSDID, ARTEFACTID, NE_LONGITUDE, NE_LATITUDE, FE_SITENAME, FE_CONSTR_TYPE, FE_ADDRESS, SURVEY_DATE, NE_AZIMUTH, NE_ANTENNADIAMETER, NE_SUSPENSIONHEIGHT, NE_TXRF_FREQUENCY, FE_AZIMUTH, FE_ANTENNADIAMETER, FE_SUSPENSIONHEIGHT, FE_TXRF_FREQUENCY, COMMENTS, INSERT_DATE, INSERT_PERSON) VALUES (ARTEFACT_TSD_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement newArtefactExtTSDPreparedStatement = udbConnect.prepareStatement(insertNewArtefactExtTSD, artefactExtTSDReturnStatus);

                    i = 1;
                    System.out.println("newArtefactExtTSDPreparedStatement.setString");
                    newArtefactExtTSDPreparedStatement.setLong(i++, createdArtefactId); // ARTEFACTID
                    newArtefactExtTSDPreparedStatement.setString(i++, cn_longitude); // NE_LONGITUDE
                    newArtefactExtTSDPreparedStatement.setString(i++, cn_latitude); // NE_LATITUDE
                    newArtefactExtTSDPreparedStatement.setString(i++, fe_sitename); // FE_SITENAME
                    newArtefactExtTSDPreparedStatement.setLong(i++, Integer.parseInt(fe_constructionType)); // FE_CONSTR_TYPE
                    newArtefactExtTSDPreparedStatement.setString(i++, fe_address); // FE_ADDRESS
                    newArtefactExtTSDPreparedStatement.setDate(i++, new java.sql.Date(fe_formated_survey_date.getTime())); // SURVEY_DATE (fe_survey_date)
                    newArtefactExtTSDPreparedStatement.setLong(i++, Integer.parseInt(ne_azimuth)); // NE_AZIMUTH
                    newArtefactExtTSDPreparedStatement.setFloat(i++, Float.parseFloat(ne_diameter)); // NE_ANTENNADIAMETER
                    newArtefactExtTSDPreparedStatement.setLong(i++, ne_suspensionHeight.longValue()); // NE_SUSPENSIONHEIGHT
                    newArtefactExtTSDPreparedStatement.setLong(i++, Integer.parseInt(ne_frequencyBand)); // NE_TXRF_FREQUENCY
                    newArtefactExtTSDPreparedStatement.setLong(i++, Integer.parseInt(fe_azimuth)); // FE_AZIMUTH
                    newArtefactExtTSDPreparedStatement.setFloat(i++, Float.parseFloat(fe_diameter)); // FE_ANTENNADIAMETER
                    newArtefactExtTSDPreparedStatement.setLong(i++, Integer.parseInt(fe_suspensionHeight)); // FE_SUSPENSIONHEIGHT
                    newArtefactExtTSDPreparedStatement.setLong(i++, Integer.parseInt(fe_frequencyBand)); // FE_TXRF_FREQUENCY
                    newArtefactExtTSDPreparedStatement.setString(i++, fe_comment); // COMMENTS
                    newArtefactExtTSDPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // INSERT_DATE
                    newArtefactExtTSDPreparedStatement.setString(i++, starter); // INSERT_PERSON
                    System.out.println("newArtefactExtTSDPreparedStatement.executeUpdate()");
                    newArtefactExtTSDPreparedStatement.executeUpdate();
                    System.out.println("successfull insert to database!");

                    ResultSet artefactExtTSDGeneratedIdResultSet = newArtefactExtTSDPreparedStatement.getGeneratedKeys();
                    artefactExtTSDGeneratedIdResultSet.next();
                    createdArtefactExtTSDId = artefactExtTSDGeneratedIdResultSet.getLong(1);
                    System.out.println("createdArtefactExtTSDId:");
                    System.out.println(createdArtefactExtTSDId);


                    udbConnect.commit();
                    delegateExecution.setVariable("ncpCreatedId", ncpCreatedId);
                    delegateExecution.setVariable("createdNcpStatusId", createdNcpStatusId);
                    delegateExecution.setVariable("createdArtefactId", createdArtefactId);
                    delegateExecution.setVariable("createdArtefactRSDId", createdArtefactRSDId);
                    delegateExecution.setVariable("createdArtefactRRId", createdArtefactRRId);
                    delegateExecution.setVariable("createdArtefactVSDId", createdArtefactVsdId);
                    delegateExecution.setVariable("createdCandApprovalId", createdCandApprovalId);
                    delegateExecution.setVariable("createdArtefactRRStatusId", createdArtefactRRStatusId);
                    delegateExecution.setVariable("createdArtefactExtTSDId", createdArtefactExtTSDId);
                    udbConnect.close();
                    System.out.println("udbConnection closed!");
                } else {
                    udbConnect.close();
                    System.out.println("Failed to make connection!");
                }
            } catch (Exception e) {
                udbConnect.rollback();
                udbConnect.close();
                System.out.println("connection Exception!");
                System.out.println(e);
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("testConnect SQLException!");
            System.out.println(e.toString());
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("testConnect Exception!");
            e.printStackTrace();
            throw e;
        }
    }
}
