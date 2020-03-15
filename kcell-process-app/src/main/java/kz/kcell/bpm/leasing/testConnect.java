package kz.kcell.bpm.leasing;

import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

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

                    String selectArtefactCurrentState = "select ARTEFACTID, NCPID, ONAIR_DATE, G_ONAIR_DATE, INST_STATUS, INST_STATUS_DATE, POWER_STATUS from ARTEFACT_CURRENT_STATE where NCPID = 29999";
                    PreparedStatement selectArtefactCurrentStatePS = udbConnect.prepareStatement(selectArtefactCurrentState);

                    System.out.println("selectArtefactCurrentState preparedStatement SQL UPDATE VALUES");
                    ResultSet resultSet = selectArtefactCurrentStatePS.executeQuery();
                    JSONArray json = new JSONArray();
                    ResultSetMetaData rsmd = resultSet.getMetaData();
                    System.out.println(rsmd.getColumnCount());
                    while(resultSet.next()) {
                        System.out.println(rsmd.getColumnCount());
                        int numColumns = rsmd.getColumnCount();
                        JSONObject obj = new JSONObject();
                        for (int j=1; j<=numColumns; j++) {
                            String column_name = rsmd.getColumnName(j);
                            obj.put(column_name, resultSet.getObject(column_name));
                        }
                        json.put(obj);
                    }
                    System.out.println("done selectArtefactCurrentStatePS");
                    JSONObject firstJson = json.getJSONObject(0);
                    System.out.println(firstJson);
                    System.out.println("done ARTEFACTID");
                    System.out.println(firstJson.getInt("ARTEFACTID"));
                    System.out.println("done INST_STATUS");
                    System.out.println(firstJson.getInt("INST_STATUS"));
                    System.out.println("done POWER_STATUS");
                    System.out.println(firstJson.has("POWER_STATUS") ?  firstJson.getInt("POWER_STATUS") : "");
                    System.out.println("done ONAIR_DATE");
                    System.out.println(firstJson.has("ONAIR_DATE") ? firstJson.getString("ONAIR_DATE") : "");
                    System.out.println("done G_ONAIR_DATE");
                    System.out.println(firstJson.has("G_ONAIR_DATE") ? firstJson.getString("G_ONAIR_DATE") : "");

                    if((firstJson.has("ONAIR_DATE") && !firstJson.getString("ONAIR_DATE").equals(null)) || (firstJson.has("G_ONAIR_DATE") && !firstJson.getString("G_ONAIR_DATE").equals(null))) {
                        System.out.println("ONAIR_DATE or G_ONAIR_DATE is not null");
                    } else if(firstJson.has("INST_STATUS") && !firstJson.getString("INST_STATUS").equals(null)) {
                        System.out.println("INST_STATUS is not null");
                    } else if(firstJson.has("POWER_STATUS") && !firstJson.getString("POWER_STATUS").equals(null)) {
                        System.out.println("G_ONAIR_DATE is not null");
                    } else {
                        System.out.println("no changed data");
                    }

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
