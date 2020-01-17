package kz.kcell.bpm.leasing;

import java.sql.*;

public class testConnect {
    public static void main(String[] args) {
        System.out.println("testConnect!");
        try {
            Class.forName ("oracle.jdbc.OracleDriver");
            Connection udbConnect = DriverManager.getConnection(
                "jdbc:oracle:thin:@//sc2-appcl010406:1521/apexudb", "app_apexudb_camunda", "p28zt#7C");
            udbConnect.close();
            udbConnect = DriverManager.getConnection(
                "jdbc:oracle:thin:@//sc2-appcl010406:1521/apexudb", "app_apexudb_camunda", "p28zt#7C");
            if (udbConnect != null) {
                udbConnect.setAutoCommit(false);
                System.out.println("Connected to the database!");

                //insert NCP
                Long ncpCreatedId = null;
                String returnCols[] = { "ARTEFACTID" };
//                String insertNCP = "INSERT INTO APP_APEXUDB_CAMUNDA.NCP_CREATION ( ARTEFACTID, NCPID, REGION, LONGITUDE, LATITUDE, REASON, PROJECT, PLANNEDBY, BAND_OLD, CREATOR, DATEOFINSERT, OBLAST_VILLAGE_ID, LASTEDITOR,  COMMENTS, OBLAST_OBJECT_ID, CABINETID, TARGET_CELL, TARGET_COVERAGE, TYPE, GEN_STATUS, NCP_STATUS, NCP_STATUS_DATE, CANDIDATE_ID, BAND, INITIATOR, VIP_INITIATOR, PART, CBR_ID, TR_STATUS) VALUES ( NCP_CREATION_SEQ.nextval, ?, 1, 'E 76,890775', 'N 43,210375', 150, 343, 3, null, 'SERGEI.ZAITSEV', TO_DATE('2013-08-09 09:25:55', 'YYYY-MM-DD HH24:MI:SS'), 750000000, 'VLADIMIR.GRACHYOV', 'TEST TEST TEST TEST', null, 25, null, 'г.Алматы, альтернатива (ул. Тажибаева 184 (угол ул. Березовского)) для демонтируемого сайта 01830XTAZHIPIPE по адресу: ул. Тажибаева 155a, pipe.  M', 6, 3, 2, TO_DATE('2013-08-09 09:25:55', 'YYYY-MM-DD HH24:MI:SS'), 60623, '1', 3, null, 61, null, null)";
                String insertNCP = "begin\n" +
                    "INSERT INTO APP_APEXUDB_CAMUNDA.NCP_CREATION ( ARTEFACTID, NCPID, REGION, LONGITUDE, LATITUDE, REASON, PROJECT, CREATOR, DATEOFINSERT, COMMENTS, CABINETID, TARGET_COVERAGE, TYPE, GEN_STATUS, NCP_STATUS, NCP_STATUS_DATE, BAND, INITIATOR, PART) VALUES (NCP_CREATION_SEQ.nextval, ?, 1, 'E 76,890775', 'N 43,210375', 150, 343, 'SERGEI.ZAITSEV', TO_DATE('2013-08-09 09:25:55', 'YYYY-MM-DD HH24:MI:SS'), 'TEST TEST TEST TEST', 25, 'г.Алматы, альтернатива (ул. Тажибаева 184 (угол ул. Березовского)) для демонтируемого сайта 01830XTAZHIPIPE по адресу: ул. Тажибаева 155a, pipe.  M', 6, 3, 2, TO_DATE('2013-08-09 09:25:55', 'YYYY-MM-DD HH24:MI:SS'), '1', 3, 61);\n" +
                    "end;";
                PreparedStatement preparedStatement = udbConnect.prepareStatement(insertNCP, returnCols);

                int i = 1;
                System.out.println("preparedStatement.setValues");
//
//                // set values to insert
                preparedStatement.setString(i, "99998"); // NCPID
//                preparedStatement.setLong(i++, 1); // REGION
//                preparedStatement.setString(i++, "E 76,890775"); // LONGITUDE ex. E 76,890775
//                preparedStatement.setString(i++, "N 43,210375"); // LATITUDE
//                preparedStatement.setLong(i++, 150); // REASON
//                preparedStatement.setLong(i++, 343); // PROJECT
//                preparedStatement.setString(i++, "Test Testovich"); // CREATOR 'SERGEI.ZAITSEV'
//                preparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // DATEOFINSERT
//                preparedStatement.setString(i++, "TEST TEST TEST TEST"); // COMMENTS
//                preparedStatement.setLong(i++, 25); // CABINETID
//                preparedStatement.setString(i++, "г.Алматы, альтернатива (ул. Тажибаева 184 (угол ул. Березовского)) для демонтируемого сайта 01830XTAZHIPIPE по адресу: ул. Тажибаева 155a, pipe.  M"); // TARGET_COVERAGE
//                preparedStatement.setLong(i++, 6); // TYPE ncp_type (Подставлять ID согласно справочнику Site Type) ex: 6
//                preparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // NCP_STATUS_DATE
//                preparedStatement.setString(i++, "1"); // BAND ex:'1'   ncp_band	(Подставлять ID согласно справочнику Bands)
//                preparedStatement.setString(i++, "Test Testovich"); // INITIATOR (Подставлять ID согласно справочнику Part)
//                preparedStatement.setLong(i++, 61); // PART

                System.out.println("preparedStatement.executeUpdate()");
                int status = preparedStatement.executeUpdate();
                System.out.println("insert status:");
                System.out.println(status); //1
                System.out.println("successfull insert to database!");

                ResultSet headGeneratedIdResultSet = preparedStatement.getGeneratedKeys();
                headGeneratedIdResultSet.next();
                ncpCreatedId = headGeneratedIdResultSet.getLong(1);
                System.out.println("artefactGeneratedId:");
                System.out.println(ncpCreatedId);

                //insert new status
                Long createdNcpStatusId = null;
                String returnStatus[] = { "STATUS_ACTION_ID" };
                String insertNewStatus = "insert into NCP_CREATION_STATUS_ACTION values ( NCP_CREATION_STATUS_ACTIO_SEQ.nextval, ?, 2, 'demo', TO_DATE('2013-08-09 09:25:55', 'YYYY-MM-DD HH24:MI:SS'), null)";
                preparedStatement = udbConnect.prepareStatement(insertNewStatus, returnStatus);

                i = 1;
                System.out.println("preparedStatement.setString");
                preparedStatement.setString(i, ncpCreatedId.toString());
                System.out.println("companiesPreparedStatement.executeUpdate()");
                status = preparedStatement.executeUpdate();
                System.out.println("insert status:");
                System.out.println(status); //1
                System.out.println("successfull insert to database!");

                ResultSet statusGeneratedIdResultSet = preparedStatement.getGeneratedKeys();
                statusGeneratedIdResultSet.next();
                createdNcpStatusId = statusGeneratedIdResultSet.getLong(1);
                System.out.println("createdNcpStatusId:");
                System.out.println(createdNcpStatusId);

                udbConnect.commit();

//                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
//                    if (generatedKeys.next()) {
//                        System.out.println(generatedKeys.getLong(1));
//                        System.out.println(generatedKeys.getLong(2));
//                    }
//                    else {
//                        throw new SQLException("Creating user failed, no ID obtained.");
//                    }
//                }

                ResultSet rs = preparedStatement.getGeneratedKeys();

                if (rs.next()) {
                    System.out.println("rs.next()");
                    long id = rs.getLong(1);
                    System.out.println("Inserted ID -" + id); // display inserted record
                }
                udbConnect.close();
                System.out.println("udbConnection closed!");
            } else {
                udbConnect.close();
                System.out.println("Failed to make connection!");
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
