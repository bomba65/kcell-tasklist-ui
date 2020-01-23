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

                    //insert NCP
                    Long ncpCreatedId = null;
                    String returnCols[] = { "ARTEFACTID" };
                    String insertNCP = "INSERT INTO APP_APEXUDB_CAMUNDA.NCP_CREATION ( ARTEFACTID, NCPID, REGION, LONGITUDE, LATITUDE, REASON, PROJECT, CREATOR, DATEOFINSERT, COMMENTS, CABINETID, TARGET_COVERAGE, TYPE, GEN_STATUS, NCP_STATUS, NCP_STATUS_DATE, BAND, INITIATOR, PART) VALUES (NCP_CREATION_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 7, 1, ?, ?, ?, ?)";
                    PreparedStatement preparedStatement = udbConnect.prepareStatement(insertNCP, returnCols);

                    int i = 1;
                    System.out.println("preparedStatement.setValues");

                    String ncpId = "99962";
                    // set values to insert
                    preparedStatement.setString(i++, ncpId); // NCPID
                    preparedStatement.setLong(i++, 1); // REGION
                    preparedStatement.setString(i++, "E 76,890775"); // LONGITUDE ex. E 76,890775
                    preparedStatement.setString(i++, "N 43,210375"); // LATITUDE
                    preparedStatement.setLong(i++, 150); // REASON
                    preparedStatement.setLong(i++, 343); // PROJECT
                    preparedStatement.setString(i++, "Test Testovich"); // CREATOR 'SERGEI.ZAITSEV'
                    preparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // DATEOFINSERT
                    preparedStatement.setString(i++, "TEST TEST TEST TEST"); // COMMENTS
                    preparedStatement.setLong(i++, 25); // CABINETID
                    preparedStatement.setString(i++, "г.Алматы, альтернатива (ул. Тажибаева 184 (угол ул. Березовского)) для демонтируемого сайта 01830XTAZHIPIPE по адресу: ул. Тажибаева 155a, pipe.  M"); // TARGET_COVERAGE
                    preparedStatement.setLong(i++, 6); // TYPE ncp_type (Подставлять ID согласно справочнику Site Type) ex: 6
                    preparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // NCP_STATUS_DATE
                    preparedStatement.setString(i++, "1"); // BAND ex:'1'   ncp_band	(Подставлять ID согласно справочнику Bands)
                    preparedStatement.setLong(i++, 3); // INITIATOR (Подставлять ID согласно справочнику Part)
                    preparedStatement.setLong(i++, 61); // PART

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
                    newNcpStatusPreparedStatement.setString(i++, "Test Testovich"); // CREATOR 'SERGEI.ZAITSEV'
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
                    newArtefactPreparedStatement.setString(i++, "46003ARALSKTV4"); // sitename	cn_sitename
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
                    newArtefactCurrentStatePreparedStatement.setString(i++, "test testovich"); // CAND_STATUS_PERSON (current user)
                    newArtefactCurrentStatePreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // CAND_STATUS_DATE
                    newArtefactCurrentStatePreparedStatement.setLong(i++, 1); // RR_STATUS first insert value mast be = 1
                    newArtefactCurrentStatePreparedStatement.setString(i++, "test testovich"); // RR_STATUS_PERSON (current user)
                    newArtefactCurrentStatePreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // RR_STATUS_DATE
                    newArtefactCurrentStatePreparedStatement.setString(i++, "E 80 50 42,4"); // LONGITUDE ex: E 80 50 42,4
                    newArtefactCurrentStatePreparedStatement.setString(i++, "N 48 48 05,6"); // LATITUDE ex: N 48 48 05,6
                    newArtefactCurrentStatePreparedStatement.setLong(i++, 181); // RBS_TYPE (cn_rbs_type)
                    newArtefactCurrentStatePreparedStatement.setLong(i++, 342); // BSC (cn_bsc)
                    newArtefactCurrentStatePreparedStatement.setLong(i++, 1); // BAND (cn_band)
                    newArtefactCurrentStatePreparedStatement.setLong(i++, 2); // RBS_LOCATION (cn_rbs_location)
                    newArtefactCurrentStatePreparedStatement.setLong(i++, 689); // ALTITUDE (cn_altitude)
                    newArtefactCurrentStatePreparedStatement.setLong(i++, 3); // CONSTRUCTION_HEIGHT (cn_height_constr)
                    newArtefactCurrentStatePreparedStatement.setLong(i++, 10); // CONSTRUCTION_TYPE (cn_construction_type)
                    newArtefactCurrentStatePreparedStatement.setString(i++, "Восточно-Казахстанская область, Зайсанский район, село Карабулак, водонапорная башня"); // ADDRESS
                    newArtefactCurrentStatePreparedStatement.setString(i++, "Мария Николаевна Специалист акимата 8 701 479 19 86"); // CONTACT_PERSON
                    newArtefactCurrentStatePreparedStatement.setString(i++, "test test"); // COMMENTS (cn_contact_information)
                    newArtefactCurrentStatePreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // INSERT_DATE
                    newArtefactCurrentStatePreparedStatement.setString(i++, "demo demo"); // INSERT_PERSON
                    newArtefactCurrentStatePreparedStatement.setLong(i++, 7); // GS_STATUS
                    newArtefactCurrentStatePreparedStatement.setString(i++, "test"); // PL_COMMENTS (cn_comments)
                    System.out.println("newArtefactCurrentStatePreparedStatement.executeUpdate()");
                    newArtefactCurrentStatePreparedStatement.executeUpdate();
                    System.out.println("successfull insert to database!");

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
