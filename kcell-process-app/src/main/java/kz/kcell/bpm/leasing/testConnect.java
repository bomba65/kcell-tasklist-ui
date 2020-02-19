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

                    //vars

                    

//                    //UPDATE NCP
                    String INSERT_CONTRACTS = "INSERT INTO APP_APEXUDB_CAMUNDA.CONTRACTS (CID, RENTSUM, RENTSUM_VAT, CONTRACTID, INCOMINGDATE, INCOMINGWEEK, CONTRACTTYPE, POWERSUPPLY, LEGALTYPE, LEGALNAME, LEGALADDRESS, CONTACTPERSON, CONTACTPHONE, ACCESS_STATUS, CONTRACT_SAP_NO, VENDOR_SAP_NO, CONTRACT_EXECUTOR, VAT, NEEDVAT, PAYMENTPERIOD, PAYMENTWAY, CONTRACTSTARTDATE, CONTRACTENDDATE, AUTOPROLONGATION, USERNAME, OBLAST_VILLAGEID, AREA_ACT_ACCEPT_DATE, RNN, INN, IBAN) VALUES (CONTRACTS_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement INSERT_CONTRACTSPreparedStatement = udbConnect.prepareStatement(INSERT_CONTRACTS);

                    int i = 1;
                    System.out.println("_SET_NCP_STATUS preparedStatement SQL UPDATE VALUES");
                    // set values to update
                    INSERT_CONTRACTSPreparedStatement.setLong(i++, 8);  // RENTSUM
                    INSERT_CONTRACTSPreparedStatement.setLong(i++, 8);  // RENTSUM_VAT
                    INSERT_CONTRACTSPreparedStatement.setString(i++, "TESTADNKA111");  // CONTRACTID
                    INSERT_CONTRACTSPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // INCOMINGDATE
                    INSERT_CONTRACTSPreparedStatement.setString(i++, "12");  // INCOMINGWEEK
                    INSERT_CONTRACTSPreparedStatement.setLong(i++, 8);  // CONTRACTTYPE
                    INSERT_CONTRACTSPreparedStatement.setLong(i++, 8);  // POWERSUPPLY
                    INSERT_CONTRACTSPreparedStatement.setLong(i++, 8);  // LEGALTYPE
                    INSERT_CONTRACTSPreparedStatement.setString(i++, "cn_longitude");  // LEGALNAME
                    INSERT_CONTRACTSPreparedStatement.setString(i++, "cn_longitude");  // LEGALADDRESS
                    INSERT_CONTRACTSPreparedStatement.setString(i++, "cn_longitude");  // CONTACTPERSON
                    INSERT_CONTRACTSPreparedStatement.setString(i++, "cn_longitude");  // CONTACTPHONE
                    INSERT_CONTRACTSPreparedStatement.setLong(i++, 8);  // ACCESS_STATUS
                    INSERT_CONTRACTSPreparedStatement.setLong(i++, 8);  // CONTRACT_SAP_NO
                    INSERT_CONTRACTSPreparedStatement.setLong(i++, 8);  // VENDOR_SAP_NO
                    INSERT_CONTRACTSPreparedStatement.setLong(i++, 8);  // CONTRACT_EXECUTOR
                    INSERT_CONTRACTSPreparedStatement.setString(i++, "cn_longitude");  // VAT
                    INSERT_CONTRACTSPreparedStatement.setString(i++, "cn_longitude");  // NEEDVAT
                    INSERT_CONTRACTSPreparedStatement.setLong(i++, 8);  // PAYMENTPERIOD
                    INSERT_CONTRACTSPreparedStatement.setLong(i++, 8);  // PAYMENTWAY ???
                    INSERT_CONTRACTSPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // CONTRACTSTARTDATE
                    INSERT_CONTRACTSPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // CONTRACTENDDATE
                    INSERT_CONTRACTSPreparedStatement.setString(i++, "cn_longitude");  // AUTOPROLONGATION
                    INSERT_CONTRACTSPreparedStatement.setString(i++, "cn_longitude");  // USERNAME
                    INSERT_CONTRACTSPreparedStatement.setLong(i++, 8);  // OBLAST_VILLAGEID
                    INSERT_CONTRACTSPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime()));  // AREA_ACT_ACCEPT_DATE
                    INSERT_CONTRACTSPreparedStatement.setString(i++, "cn_longitude");  // RNN
                    INSERT_CONTRACTSPreparedStatement.setString(i++, "cn_longitude");  // INN
                    INSERT_CONTRACTSPreparedStatement.setString(i++, "cn_longitude");  // IBAN

                    INSERT_CONTRACTSPreparedStatement.executeUpdate();
                    System.out.println("successfull NCP_STATUS updated!");


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
