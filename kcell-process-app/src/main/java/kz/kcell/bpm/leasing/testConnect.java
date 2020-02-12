package kz.kcell.bpm.leasing;

import java.sql.*;
import java.util.Date;

public class testConnect {
    public testConnect() {
    }

    public static void main(String[] args) {
        System.out.println("testConnect!");
        try {
            Class.forName ("oracle.jdbc.OracleDriver");
            Connection udbConnect = DriverManager.getConnection(
                "jdbc:oracle:thin:@//sc2-appcl010406:1521/apexudb", "app_apexudb_camunda", "p28zt#7C");
            try {
                if (udbConnect != null) {
                    udbConnect.setAutoCommit(false);
                    System.out.println("Connected to the database!");

//                    //insert ARTEFACT_TSD_EXT
//                    Long createdArtefactExtTSDId = null;
//                    String artefactExtTSDReturnStatus[] = { "TSDID" };
//                    String insertNewArtefactExtTSD = "INSERT INTO APP_APEXUDB_CAMUNDA.ARTEFACT_TSD_EXT (TSDID, ARTEFACTID, NE_LONGITUDE, NE_LATITUDE, FE_SITENAME, FE_CONSTR_TYPE, FE_ADDRESS, SURVEY_DATE, NE_AZIMUTH, NE_ANTENNADIAMETER, NE_SUSPENSIONHEIGHT, NE_TXRF_FREQUENCY, FE_AZIMUTH, FE_ANTENNADIAMETER, FE_SUSPENSIONHEIGHT, FE_TXRF_FREQUENCY, COMMENTS, INSERT_DATE, INSERT_PERSON) VALUES (ARTEFACT_TSD_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
//                    PreparedStatement newArtefactExtTSDPreparedStatement = udbConnect.prepareStatement(insertNewArtefactExtTSD, artefactExtTSDReturnStatus);
//
//                    int i = 1;
//                    System.out.println("newArtefactExtTSDPreparedStatement.setString");
//                    newArtefactExtTSDPreparedStatement.setLong(i++, 1234); // ARTEFACTID
//                    newArtefactExtTSDPreparedStatement.setString(i++, "N 54"); // NE_LONGITUDE
//                    newArtefactExtTSDPreparedStatement.setString(i++, "E 54"); // NE_LATITUDE
//                    newArtefactExtTSDPreparedStatement.setString(i++, "qweSitte1"); // FE_SITENAME
//                    newArtefactExtTSDPreparedStatement.setLong(i++, Integer.parseInt("1")); // FE_CONSTR_TYPE
//                    newArtefactExtTSDPreparedStatement.setString(i++, "fe_address"); // FE_ADDRESS
//                    newArtefactExtTSDPreparedStatement.setDate(i++,new java.sql.Date(new Date().getTime())); // SURVEY_DATE (fe_survey_date)
//                    newArtefactExtTSDPreparedStatement.setLong(i++, Integer.parseInt("1")); // NE_AZIMUTH
//                    newArtefactExtTSDPreparedStatement.setFloat(i++, Float.parseFloat("1")); // NE_ANTENNADIAMETER
//                    newArtefactExtTSDPreparedStatement.setLong(i++, 1); // NE_SUSPENSIONHEIGHT
//                    newArtefactExtTSDPreparedStatement.setLong(i++, Integer.parseInt("1")); // NE_TXRF_FREQUENCY
//                    newArtefactExtTSDPreparedStatement.setLong(i++, Integer.parseInt("1")); // FE_AZIMUTH
//                    newArtefactExtTSDPreparedStatement.setFloat(i++, Float.parseFloat("1")); // FE_ANTENNADIAMETER
//                    newArtefactExtTSDPreparedStatement.setLong(i++, Integer.parseInt("2")); // FE_SUSPENSIONHEIGHT
//                    newArtefactExtTSDPreparedStatement.setLong(i++, Integer.parseInt("2")); // FE_TXRF_FREQUENCY
//                    newArtefactExtTSDPreparedStatement.setString(i++, "fe_comment"); // COMMENTS
//                    newArtefactExtTSDPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // INSERT_DATE
//                    newArtefactExtTSDPreparedStatement.setString(i++, "starter"); // INSERT_PERSON
//                    System.out.println("newArtefactExtTSDPreparedStatement.executeUpdate()");
//                    newArtefactExtTSDPreparedStatement.executeUpdate();
//                    System.out.println("successfull insert to database!");
//
//                    ResultSet artefactExtTSDGeneratedIdResultSet = newArtefactExtTSDPreparedStatement.getGeneratedKeys();
//                    artefactExtTSDGeneratedIdResultSet.next();
//                    createdArtefactExtTSDId = artefactExtTSDGeneratedIdResultSet.getLong(1);
//                    System.out.println("createdArtefactExtTSDId:");
//                    System.out.println(createdArtefactExtTSDId);

//                    //insert NCP
//                    Long ncpCreatedId = null;
//                    String returnCols[] = { "ARTEFACTID" };
//                    String insertNCP = "INSERT INTO APP_APEXUDB_CAMUNDA.NCP_CREATION ( ARTEFACTID, NCPID, REGION, LONGITUDE, LATITUDE, REASON, PROJECT, CREATOR, DATEOFINSERT, COMMENTS, CABINETID, TARGET_COVERAGE, TYPE, GEN_STATUS, NCP_STATUS, NCP_STATUS_DATE, BAND, INITIATOR, PART) VALUES (NCP_CREATION_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 7, 1, ?, ?, ?, ?)";
//                    PreparedStatement preparedStatement = udbConnect.prepareStatement(insertNCP, returnCols);
//
//                    int i = 1;
//                    System.out.println("preparedStatement.setValues");
//
//                    String ncpId = "32315";
//                    // set values to insert
//                    preparedStatement.setString(i++, ncpId); // NCPID
//                    preparedStatement.setLong(i++, 1); // REGION
//                    preparedStatement.setString(i++, "E 76,890775"); // LONGITUDE ex. E 76,890775
//                    preparedStatement.setString(i++, "N 43,210375"); // LATITUDE
//                    preparedStatement.setLong(i++, 150); // REASON
//                    preparedStatement.setLong(i++, 343); // PROJECT
//                    preparedStatement.setString(i++, "Test Testovich"); // CREATOR 'SERGEI.ZAITSEV'
//                    preparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // DATEOFINSERT
//                    preparedStatement.setString(i++, "TEST TEST TEST TEST"); // COMMENTS
//                    preparedStatement.setLong(i++, 25); // CABINETID
//                    preparedStatement.setString(i++, "г.Алматы, альтернатива (ул. Тажибаева 184 (угол ул. Березовского)) для демонтируемого сайта 01830XTAZHIPIPE по адресу: ул. Тажибаева 155a, pipe.  M"); // TARGET_COVERAGE
//                    preparedStatement.setLong(i++, 6); // TYPE ncp_type (Подставлять ID согласно справочнику Site Type) ex: 6
//                    preparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // NCP_STATUS_DATE
//                    preparedStatement.setString(i++, "1"); // BAND ex:'1'   ncp_band	(Подставлять ID согласно справочнику Bands)
//                    preparedStatement.setLong(i++, 3); // INITIATOR (Подставлять ID согласно справочнику Part)
//                    preparedStatement.setLong(i++, 61); // PART
//
//                    System.out.println("preparedStatement.executeUpdate()");
//                    preparedStatement.executeUpdate();
//                    System.out.println("successfull insert to database!");
////
//                    ResultSet headGeneratedIdResultSet = preparedStatement.getGeneratedKeys();
//                    headGeneratedIdResultSet.next();
//                    ncpCreatedId = headGeneratedIdResultSet.getLong(1);
//                    System.out.println("artefactGeneratedId:");
//                    System.out.println(ncpCreatedId);

//                    //insert new status
//                    Long createdNcpStatusId = null;
//                    String returnStatus[] = { "STATUS_ACTION_ID" };
//                    String insertNewStatus = "insert into NCP_CREATION_STATUS_ACTION values ( NCP_CREATION_STATUS_ACTIO_SEQ.nextval, ?, 2, ?, ?, null)";
//                    PreparedStatement newNcpStatusPreparedStatement = udbConnect.prepareStatement(insertNewStatus, returnStatus);
//
//                    i = 1;
//                    System.out.println("newNcpStatusPreparedStatement.setString");
//                    newNcpStatusPreparedStatement.setString(i++, ncpCreatedId.toString());
//                    newNcpStatusPreparedStatement.setString(i++, "Test Testovich"); // CREATOR 'SERGEI.ZAITSEV'
//                    newNcpStatusPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // DATEOFINSERT
//                    System.out.println("newNcpStatusPreparedStatement.executeUpdate()");
//                    newNcpStatusPreparedStatement.executeUpdate();
//                    System.out.println("successfull insert to database!");
//
//                    ResultSet statusGeneratedIdResultSet = newNcpStatusPreparedStatement.getGeneratedKeys();
//                    statusGeneratedIdResultSet.next();
//                    createdNcpStatusId = statusGeneratedIdResultSet.getLong(1);
//                    System.out.println("createdNcpStatusId:");
//                    System.out.println(createdNcpStatusId);
//
//                    //insert new Candidate (in Artefact table)
//                    Long createdArtefactId = null;
//                    String returnArtefactId[] = { "ARTEFACTID" };
//                    String insertNewArtefact = "insert into ARTEFACT values (ARTEFACT_SEQ.nextval, ?, ?)";
//                    PreparedStatement newArtefactPreparedStatement = udbConnect.prepareStatement(insertNewArtefact, returnArtefactId);
//
//                    i = 1;
//                    System.out.println("newArtefactPreparedStatement.setString");
//                    newArtefactPreparedStatement.setString(i++, "46003ARALSKTV4"); // sitename	cn_sitename
//                    newArtefactPreparedStatement.setLong(i++, Integer.parseInt(ncpId)); //ncp_id
//                    System.out.println("newArtefactPreparedStatement.executeUpdate()");
//                    newArtefactPreparedStatement.executeUpdate();
//                    System.out.println("successfull insert to database!");
//
//                    ResultSet createdArtefactIdResultSet = newArtefactPreparedStatement.getGeneratedKeys();
//                    createdArtefactIdResultSet.next();
//                    createdArtefactId = createdArtefactIdResultSet.getLong(1);
//                    System.out.println("createdArtefactId:");
//                    System.out.println(createdArtefactId);
//
//                    //insert new Candidate (in ARTEFACT_CURRENT_STATE table)
//                    String insertNewArtefactCurrentState = "INSERT INTO APP_APEXUDB_CAMUNDA.ARTEFACT_CURRENT_STATE (ARTEFACTID,\n" +
//                        "                                                        NCPID,\n" +
//                        "                                                        CAND_STATUS,\n" +
//                        "                                                        CAND_STATUS_PERSON,\n" +
//                        "                                                        CAND_STATUS_DATE,\n" +
//                        "                                                        RR_STATUS,\n" +
//                        "                                                        RR_STATUS_PERSON,\n" +
//                        "                                                        RR_STATUS_DATE,\n" +
//                        "                                                        LONGITUDE,\n" +
//                        "                                                        LATITUDE,\n" +
//                        "                                                        RBS_TYPE,\n" +
//                        "                                                        BSC,\n" +
//                        "                                                        BAND,\n" +
//                        "                                                        RBS_LOCATION,\n" +
//                        "                                                        ALTITUDE,\n" +
//                        "                                                        CONSTRUCTION_HEIGHT,\n" +
//                        "                                                        CONSTRUCTION_TYPE,\n" +
//                        "                                                        ADDRESS,\n" +
//                        "                                                        CONTACT_PERSON,\n" +
//                        "                                                        COMMENTS,\n" +
//                        "                                                        INSERT_DATE,\n" +
//                        "                                                        INSERT_PERSON,\n" +
//                        "                                                        GS_STATUS,\n" +
//                        "                                                        PL_COMMENTS) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
//                    PreparedStatement newArtefactCurrentStatePreparedStatement = udbConnect.prepareStatement(insertNewArtefactCurrentState);
//
//                    i = 1;
//                    System.out.println("newArtefactCurrentStatePreparedStatement.setString");
//                    newArtefactCurrentStatePreparedStatement.setLong(i++, createdArtefactId); //ARTEFACTID
//                    newArtefactCurrentStatePreparedStatement.setLong(i++, Integer.parseInt(ncpId)); //ncp_id
//                    newArtefactCurrentStatePreparedStatement.setLong(i++, 1); // CAND_STATUS first insert value mast be = 1
//                    newArtefactCurrentStatePreparedStatement.setString(i++, "test testovich"); // CAND_STATUS_PERSON (current user)
//                    newArtefactCurrentStatePreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // CAND_STATUS_DATE
//                    newArtefactCurrentStatePreparedStatement.setLong(i++, 1); // RR_STATUS first insert value mast be = 1
//                    newArtefactCurrentStatePreparedStatement.setString(i++, "test testovich"); // RR_STATUS_PERSON (current user)
//                    newArtefactCurrentStatePreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // RR_STATUS_DATE
//                    newArtefactCurrentStatePreparedStatement.setString(i++, "E 80 50 42,4"); // LONGITUDE ex: E 80 50 42,4
//                    newArtefactCurrentStatePreparedStatement.setString(i++, "N 48 48 05,6"); // LATITUDE ex: N 48 48 05,6
//                    newArtefactCurrentStatePreparedStatement.setLong(i++, 181); // RBS_TYPE (cn_rbs_type)
//                    newArtefactCurrentStatePreparedStatement.setLong(i++, 342); // BSC (cn_bsc)
//                    newArtefactCurrentStatePreparedStatement.setLong(i++, 1); // BAND (cn_band)
//                    newArtefactCurrentStatePreparedStatement.setLong(i++, 2); // RBS_LOCATION (cn_rbs_location)
//                    newArtefactCurrentStatePreparedStatement.setLong(i++, 689); // ALTITUDE (cn_altitude)
//                    newArtefactCurrentStatePreparedStatement.setLong(i++, 3); // CONSTRUCTION_HEIGHT (cn_height_constr)
//                    newArtefactCurrentStatePreparedStatement.setLong(i++, 10); // CONSTRUCTION_TYPE (cn_construction_type)
//                    newArtefactCurrentStatePreparedStatement.setString(i++, "Восточно-Казахстанская область, Зайсанский район, село Карабулак, водонапорная башня"); // ADDRESS
//                    newArtefactCurrentStatePreparedStatement.setString(i++, "Мария Николаевна Специалист акимата 8 701 479 19 86"); // CONTACT_PERSON
//                    newArtefactCurrentStatePreparedStatement.setString(i++, "test test"); // COMMENTS (cn_contact_information)
//                    newArtefactCurrentStatePreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // INSERT_DATE
//                    newArtefactCurrentStatePreparedStatement.setString(i++, "demo demo"); // INSERT_PERSON
//                    newArtefactCurrentStatePreparedStatement.setLong(i++, 7); // GS_STATUS
//                    newArtefactCurrentStatePreparedStatement.setString(i++, "test"); // PL_COMMENTS (cn_comments)
//                    System.out.println("newArtefactCurrentStatePreparedStatement.executeUpdate()");
//                    newArtefactCurrentStatePreparedStatement.executeUpdate();
//                    System.out.println("successfull insert to database!");

//                    //update artefact
//                    //update Candidate (in Artefact table)
//                    String updateNewArtefact = "update ARTEFACT set sitename = ?, ncpid = ? where ARTEFACTID = ?";
//                    PreparedStatement newArtefactPreparedStatement = udbConnect.prepareStatement(updateNewArtefact);
//
//                    int i = 1;
//                    System.out.println("updateArtefactPreparedStatement.setString");
//                    newArtefactPreparedStatement.setString(i++, null); // sitename	cn_sitename
//                    newArtefactPreparedStatement.setLong(i++, 32313); //ncp_id
//                    newArtefactPreparedStatement.setLong(i++, 13701); //ARTEFACTID
//
//                    System.out.println("updateArtefactPreparedStatement.executeUpdate()");
//                    newArtefactPreparedStatement.executeUpdate();
//                    System.out.println("successfull insert to database!");
//                    //insert ARTEFACT
//                    String insertNCP = "insert into ARTEFACT values (ARTEFACT_SEQ.nextval, ?, ?)";
//                    PreparedStatement preparedStatement = udbConnect.prepareStatement(insertNCP);
//
//                    int i = 1;
//                    System.out.println("preparedStatement.setValues");
//
//                    String ncpId = "32316";
//                    // set values to insert
//                    preparedStatement.setString(i++, "ncpId2"); // NCPID
//                    preparedStatement.setLong(i++, 999998); // REGION
//
//                    System.out.println("preparedStatement.executeUpdate()");
//                    preparedStatement.executeUpdate();
//                    System.out.println("successfull insert to database!");

//                    //UPDATE ARTEFACT
//                    String UPDATENCP = "update ARTEFACT set NCPID = ?, SITENAME = ? where ARTEFACTID = ?";
//                    PreparedStatement updatePreparedStatement = udbConnect.prepareStatement(UPDATENCP);
//
//                    int i = 1;
//                    System.out.println("preparedStatement.setValues");
//                    // set values to insert
//                    updatePreparedStatement.setLong(i++, 29999); // REGION
//                    updatePreparedStatement.setString(i++, "re1named"); // NCPID
//                    updatePreparedStatement.setLong(i++, 70374); // REGION
//
//                    System.out.println("preparedStatement.executeUpdate()");
//                    updatePreparedStatement.executeUpdate();
//                    System.out.println("successfull insert to database!");
//
//                    //asd
//                    String _SET_NCP_STATUS = "2";
//                    String ncpId = "32320";
//                    System.out.println("_SET_NCP_STATUS");
//
//                    System.out.println(_SET_NCP_STATUS);
//                    //UPDATE NCP
                    String UPDATE_TSD_EXT = "UPDATE ARTEFACT_TSD_EXT SET NE_LONGITUDE = ?, NE_LATITUDE = ?, FE_SITENAME = ?, FE_CONSTR_TYPE = ?, FE_ADDRESS = ?, SURVEY_DATE = ?, NE_AZIMUTH = ?, NE_ANTENNADIAMETER = ?, NE_SUSPENSIONHEIGHT = ?, NE_TXRF_FREQUENCY = ?, FE_AZIMUTH = ?, FE_ANTENNADIAMETER = ?, FE_SUSPENSIONHEIGHT = ?, FE_TXRF_FREQUENCY = ?, COMMENTS = ?, UPDATE_DATE = ?, UPDATE_PERSON = ? WHERE TSDID = ?";
                    PreparedStatement updateTSDextPreparedStatement = udbConnect.prepareStatement(UPDATE_TSD_EXT);

                    int i = 1;
                    System.out.println("_SET_NCP_STATUS preparedStatement SQL UPDATE VALUES");
                    // set values to update
                    updateTSDextPreparedStatement.setString(i++, "cn_longitude"); // NE_LONGITUDE
                    updateTSDextPreparedStatement.setString(i++, "cn_latitude"); // NE_LATITUDE
                    updateTSDextPreparedStatement.setString(i++, "fe_sitename"); // FE_SITENAME
                    updateTSDextPreparedStatement.setLong(i++, Integer.parseInt("1")); // FE_CONSTR_TYPE
                    updateTSDextPreparedStatement.setString(i++, "fe_address"); // FE_ADDRESS
                    updateTSDextPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // SURVEY_DATE (fe_survey_date)
                    updateTSDextPreparedStatement.setLong(i++, Integer.parseInt("1")); // NE_AZIMUTH
                    updateTSDextPreparedStatement.setFloat(i++, Float.parseFloat("1")); // NE_ANTENNADIAMETER
                    updateTSDextPreparedStatement.setLong(i++, 2); // NE_SUSPENSIONHEIGHT
                    updateTSDextPreparedStatement.setLong(i++, Integer.parseInt("1")); // NE_TXRF_FREQUENCY
                    updateTSDextPreparedStatement.setLong(i++, Integer.parseInt("2")); // FE_AZIMUTH
                    updateTSDextPreparedStatement.setFloat(i++, Float.parseFloat("2")); // FE_ANTENNADIAMETER
                    updateTSDextPreparedStatement.setLong(i++, Integer.parseInt("2")); // FE_SUSPENSIONHEIGHT
                    updateTSDextPreparedStatement.setLong(i++, Integer.parseInt("1")); // FE_TXRF_FREQUENCY
                    updateTSDextPreparedStatement.setString(i++, "fe_comment"); // COMMENTS
                    updateTSDextPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // INSERT_DATE
                    updateTSDextPreparedStatement.setString(i++, "starter"); // INSERT_PERSON
                    updateTSDextPreparedStatement.setLong(i++, 50786); // TSDID

                    updateTSDextPreparedStatement.executeUpdate();
                    System.out.println("successfull NCP_STATUS updated!");

//                    //UPDATE ARTEFACT
//                    String UpdateArtefactCurrentState = "update ARTEFACT_CURRENT_STATE set ncpid = ?, cand_status_person = null, longitude = null, latitude = null, rbs_type = null, bsc = null,band = null,rbs_location = null, construction_height = null, construction_type = null, address = null, contact_person = null, comments = null, pl_comments = null where ARTEFACTID = ?";
//                    PreparedStatement updateArtefactCurrentStatePreparedStatement = udbConnect.prepareStatement(UpdateArtefactCurrentState);
//
//                    i = 1;
//                    System.out.println("preparedStatement.setValues");
//                    // set values to insert
//                    updateArtefactCurrentStatePreparedStatement.setLong(i++, 39999); // REGION
////                    updatePreparedStatement.setString(i++, "re1named"); // NCPID
//                    updateArtefactCurrentStatePreparedStatement.setLong(i++, 13701); // REGION
//
//                    System.out.println("preparedStatement.executeUpdate()");
//                    updateArtefactCurrentStatePreparedStatement.executeUpdate();
//                    System.out.println("successfull insert to database!");

//                    //UPDATE ARTEFACT
//                    String UPDATENCP = "update ARTEFACT set NCPID = ?, SITENAME = ? where ARTEFACTID = ?";
//                    PreparedStatement updatePreparedStatement = udbConnect.prepareStatement(UPDATENCP);
//
//                    int i = 1;
//                    System.out.println("preparedStatement.setValues");
//                    // set values to insert
//                    updatePreparedStatement.setLong(i++, 49999); // REGION
//                    updatePreparedStatement.setString(i++, "re1named"); // NCPID
//                    updatePreparedStatement.setLong(i++, 70374); // REGION
//
//                    System.out.println("preparedStatement.executeUpdate()");
//                    updatePreparedStatement.executeUpdate();
//                    System.out.println("successfull insert to database!");

                    udbConnect.commit();
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
        } catch (Exception e) {
            System.out.println("testConnect Exception!");
            e.printStackTrace();
        }

    }
}
