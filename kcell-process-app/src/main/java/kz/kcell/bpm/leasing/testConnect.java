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
                    System.out.println("IW: UDB CHECK Connected to the database!");


//                    Long createdArtefactId = delegateExecution.hasVariable("createdArtefactId") ? (Long) delegateExecution.getVariable("createdArtefactId") : 0;
//                    String UDBcheckCronValue = delegateExecution.hasVariable("UDBcheckCronValue") ? delegateExecution.getVariable("UDBcheckCronValue").toString() : "0 58 10 * * ?";

//                    System.out.println("createdArtefactId: " + createdArtefactId.toString());

//                    String selectArtefactCurrentState = "select ARTEFACTID, NCPID, CAND_STATUS_DATE, ONAIR_DATE, G_ONAIR_DATE, INST_STATUS, INST_STATUS_DATE, POWER_STATUS from ARTEFACT_CURRENT_STATE where ARTEFACTID = 70547";
//
//                    PreparedStatement selectArtefactCurrentStatePS = udbConnect.prepareStatement(selectArtefactCurrentState);
//
//                    int i=1;
////                    selectArtefactCurrentStatePS.setLong(i++, createdArtefactId.longValue());
//
////                    System.out.println("UDBcheckCronValue");
////                    System.out.println(UDBcheckCronValue);
////                    System.out.println("selectArtefactCurrentState preparedStatement SQL UPDATE VALUES");
//                    ResultSet resultSet = selectArtefactCurrentStatePS.executeQuery();
//                    JSONArray json = new JSONArray();
//                    ResultSetMetaData rsmd = resultSet.getMetaData();
//                    while(resultSet.next()) {
//                        System.out.println(rsmd.getColumnCount());
//                        int numColumns = rsmd.getColumnCount();
//                        JSONObject obj = new JSONObject();
//                        for (int j=1; j<=numColumns; j++) {
//                            String column_name = rsmd.getColumnName(j);
//                            obj.put(column_name, resultSet.getObject(column_name));
//                        }
//                        json.put(obj);
//                    }
//                    System.out.println("done selectArtefactCurrentStatePS");
//                    String dataFromUDB = "noData";
//                    System.out.println("selectArtefactCurrentStatePS raws: " + json.length());
//                    if (json.length() > 0) {
//                        JSONObject firstJson = json.getJSONObject(0);
//                        System.out.println(firstJson);
//                        System.out.println("done ARTEFACTID");
//                        System.out.println(firstJson.getInt("ARTEFACTID"));
//                        System.out.println("done INST_STATUS");
//                        System.out.println(firstJson.has("INST_STATUS") ?  firstJson.getInt("INST_STATUS") : "");
//                        System.out.println("done POWER_STATUS");
//                        System.out.println(firstJson.has("POWER_STATUS") ?  firstJson.getInt("POWER_STATUS") : "");
//                        System.out.println("done ONAIR_DATE");
//                        System.out.println(firstJson.has("ONAIR_DATE") ? firstJson.getString("ONAIR_DATE") : "");
//                        System.out.println("done CAND_STATUS_DATE");
//                        System.out.println(firstJson.has("CAND_STATUS_DATE") ? firstJson.getString("CAND_STATUS_DATE") : "");
//                        System.out.println("done G_ONAIR_DATE");
//                        System.out.println(firstJson.has("G_ONAIR_DATE") ? firstJson.getString("G_ONAIR_DATE") : "");
//
//                        if((firstJson.has("ONAIR_DATE") && !firstJson.getString("ONAIR_DATE").equals(null)) || (firstJson.has("G_ONAIR_DATE") && !firstJson.getString("G_ONAIR_DATE").equals(null))) {
//                            System.out.println("ONAIR_DATE or G_ONAIR_DATE is not null");
//                            dataFromUDB = "withFinishDate";
////                            delegateExecution.setVariable("setInstStatusFromUDB", "FinishDate");
////                            delegateExecution.setVariable("finishDateFromUDB", firstJson.has("ONAIR_DATE") ? firstJson.getString("ONAIR_DATE") : firstJson.has("G_ONAIR_DATE") ? firstJson.getString("G_ONAIR_DATE") : "");
//                        }
//
//                        if(firstJson.has("INST_STATUS") && dataFromUDB.equals("noData")) {
//                            int uis = firstJson.getInt("INST_STATUS");
//                            if (uis == 8 || uis == 15 || uis == 7 || uis == 4) {
//                                dataFromUDB = "justSetInstStatus";
////                                if (uis == 8) {
////                                    delegateExecution.setVariable("setInstStatusFromUDB", "Installation Problem");
////                                } else if (uis == 15) {
////                                    delegateExecution.setVariable("setInstStatusFromUDB", "Installation Finish");
////                                } else if (uis == 7) {
////                                    delegateExecution.setVariable("setInstStatusFromUDB", "Installation in Progress");
////                                } else if (uis == 4) {
////                                    delegateExecution.setVariable("setInstStatusFromUDB", "SSID in Progress");
////                                }
//                                System.out.println("justSetInstStatus");
////                                delegateExecution.setVariable("instStatusFromUDB", uis);
//                            } else if (uis == 16) {
//                                dataFromUDB = "finishWithSetInstStatus";
//                                System.out.println("finishWithSetInstStatus 16");
////                                delegateExecution.setVariable("setInstStatusFromUDB", "Transmission problem");
////                                delegateExecution.setVariable("instStatusFromUDB", uis);
//                            } else if (uis == 13) {
//                                dataFromUDB = "finishWithSetInstStatus";
//                                System.out.println("finishWithSetInstStatus 13");
////                                delegateExecution.setVariable("setInstStatusFromUDB", "Leasing problem");
////                                delegateExecution.setVariable("instStatusFromUDB", uis);
//                            }
//                            System.out.println("INST_STATUS is not null");
//                        }
//
//                        if(firstJson.has("POWER_STATUS") && firstJson.getInt("POWER_STATUS") == 3 && dataFromUDB.equals("noData")) {
//                            dataFromUDB = "withPowerProblem";
////                            delegateExecution.setVariable("powerStatusFromUDB", firstJson.getInt("POWER_STATUS"));
////                            delegateExecution.setVariable("setInstStatusFromUDB", "Power problem");
//                            System.out.println("POWER_STATUS = 3 (withPowerProblem)");
//                        }
//                        if (dataFromUDB.equals("noData")) {
//                            System.out.println("no changed data");
//                            dataFromUDB = "noData";
//                        }
//                    }
//                    String selectArtefactCurrentState = "select ARTEFACTID, NCPID, CAND_STATUS_DATE, ONAIR_DATE, G_ONAIR_DATE, INST_STATUS, INST_STATUS_DATE, POWER_STATUS from ARTEFACT_CURRENT_STATE where ARTEFACTID = 70547";
//
//                    PreparedStatement selectArtefactCurrentStatePS = udbConnect.prepareStatement(selectArtefactCurrentState);
//
//                    int i=1;
////                    selectArtefactCurrentStatePS.setLong(i++, createdArtefactId.longValue());
//
////                    System.out.println("UDBcheckCronValue");
////                    System.out.println(UDBcheckCronValue);
////                    System.out.println("selectArtefactCurrentState preparedStatement SQL UPDATE VALUES");
//                    ResultSet resultSet = selectArtefactCurrentStatePS.executeQuery();
//                    JSONArray json = new JSONArray();
//                    ResultSetMetaData rsmd = resultSet.getMetaData();
//                    while(resultSet.next()) {
//                        System.out.println(rsmd.getColumnCount());
//                        int numColumns = rsmd.getColumnCount();
//                        JSONObject obj = new JSONObject();
//                        for (int j=1; j<=numColumns; j++) {
//                            String column_name = rsmd.getColumnName(j);
//                            obj.put(column_name, resultSet.getObject(column_name));
//                        }
//                        json.put(obj);
//                    }
//                    System.out.println("done selectArtefactCurrentStatePS");
//                    String dataFromUDB = "noData";
//                    System.out.println("selectArtefactCurrentStatePS raws: " + json.length());
//                    if (json.length() > 0) {
//                        JSONObject firstJson = json.getJSONObject(0);
//                        System.out.println(firstJson);
//                        System.out.println("done ARTEFACTID");
//                        System.out.println(firstJson.getInt("ARTEFACTID"));
//                        System.out.println("done INST_STATUS");
//                        System.out.println(firstJson.has("INST_STATUS") ?  firstJson.getInt("INST_STATUS") : "");
//                        System.out.println("done POWER_STATUS");
//                        System.out.println(firstJson.has("POWER_STATUS") ?  firstJson.getInt("POWER_STATUS") : "");
//                        System.out.println("done ONAIR_DATE");
//                        System.out.println(firstJson.has("ONAIR_DATE") ? firstJson.getString("ONAIR_DATE") : "");
//                        System.out.println("done CAND_STATUS_DATE");
//                        System.out.println(firstJson.has("CAND_STATUS_DATE") ? firstJson.getString("CAND_STATUS_DATE") : "");
//                        System.out.println("done G_ONAIR_DATE");
//                        System.out.println(firstJson.has("G_ONAIR_DATE") ? firstJson.getString("G_ONAIR_DATE") : "");
//
//                        if((firstJson.has("ONAIR_DATE") && !firstJson.getString("ONAIR_DATE").equals(null)) || (firstJson.has("G_ONAIR_DATE") && !firstJson.getString("G_ONAIR_DATE").equals(null))) {
//                            System.out.println("ONAIR_DATE or G_ONAIR_DATE is not null");
//                            dataFromUDB = "withFinishDate";
////                            delegateExecution.setVariable("setInstStatusFromUDB", "FinishDate");
////                            delegateExecution.setVariable("finishDateFromUDB", firstJson.has("ONAIR_DATE") ? firstJson.getString("ONAIR_DATE") : firstJson.has("G_ONAIR_DATE") ? firstJson.getString("G_ONAIR_DATE") : "");
//                        }
//
//                        if(firstJson.has("INST_STATUS") && dataFromUDB.equals("noData")) {
//                            int uis = firstJson.getInt("INST_STATUS");
//                            if (uis == 8 || uis == 15 || uis == 7 || uis == 4) {
//                                dataFromUDB = "justSetInstStatus";
////                                if (uis == 8) {
////                                    delegateExecution.setVariable("setInstStatusFromUDB", "Installation Problem");
////                                } else if (uis == 15) {
////                                    delegateExecution.setVariable("setInstStatusFromUDB", "Installation Finish");
////                                } else if (uis == 7) {
////                                    delegateExecution.setVariable("setInstStatusFromUDB", "Installation in Progress");
////                                } else if (uis == 4) {
////                                    delegateExecution.setVariable("setInstStatusFromUDB", "SSID in Progress");
////                                }
//                                System.out.println("justSetInstStatus");
////                                delegateExecution.setVariable("instStatusFromUDB", uis);
//                            } else if (uis == 16) {
//                                dataFromUDB = "finishWithSetInstStatus";
//                                System.out.println("finishWithSetInstStatus 16");
////                                delegateExecution.setVariable("setInstStatusFromUDB", "Transmission problem");
////                                delegateExecution.setVariable("instStatusFromUDB", uis);
//                            } else if (uis == 13) {
//                                dataFromUDB = "finishWithSetInstStatus";
//                                System.out.println("finishWithSetInstStatus 13");
////                                delegateExecution.setVariable("setInstStatusFromUDB", "Leasing problem");
////                                delegateExecution.setVariable("instStatusFromUDB", uis);
//                            }
//                            System.out.println("INST_STATUS is not null");
//                        }
//
//                        if(firstJson.has("POWER_STATUS") && firstJson.getInt("POWER_STATUS") == 3 && dataFromUDB.equals("noData")) {
//                            dataFromUDB = "withPowerProblem";
////                            delegateExecution.setVariable("powerStatusFromUDB", firstJson.getInt("POWER_STATUS"));
////                            delegateExecution.setVariable("setInstStatusFromUDB", "Power problem");
//                            System.out.println("POWER_STATUS = 3 (withPowerProblem)");
//                        }
//                        if (dataFromUDB.equals("noData")) {
//                            System.out.println("no changed data");
//                            dataFromUDB = "noData";
//                        }
//                    }
//                    delegateExecution.setVariable("dataFromUDB", dataFromUDB);
//                    if (UDBcheckCronValue.equals("0 58 10 * * ?")) {
////                        delegateExecution.setVariable("UDBcheckCronValue", "0 58 10 * * ?");
//                    } else {
////                        delegateExecution.setVariable("UDBcheckCronValue", "0 10 11 * * ?");
//                    }

                    Number fe_artefact_id = 0;
                    String SelectArtefactBySite = "select * from ARTEFACT where SITENAME = ?";
                    PreparedStatement selectArtefactBySitePreparedStatement = udbConnect.prepareStatement(SelectArtefactBySite);
                    int i = 1;
                    System.out.println("get contracts...");
                    selectArtefactBySitePreparedStatement.setString(i++, "46009ZHALAGASH"); // sitename
                    ResultSet resultSet = selectArtefactBySitePreparedStatement.executeQuery();
//                    System.out.println(resultSet.wasNull());

                    if (resultSet.next() == false) {
                        System.out.println("not Found");
                    } else {
                        fe_artefact_id = resultSet.getInt("ARTEFACTID");
                    }
                    System.out.println("resultSet:");
                    System.out.println(fe_artefact_id.longValue());

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
