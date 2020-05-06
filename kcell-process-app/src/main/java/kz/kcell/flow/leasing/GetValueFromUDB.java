package kz.kcell.flow.leasing;

import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.TimeZone;

@Log
@Service("GetValueFromUDB")
public class GetValueFromUDB implements JavaDelegate {

    @Value("${udb.oracle.url:jdbc:oracle:thin:@//sc2-appcl010406:1521/apexudb}")
    private String udbOracleUrl;

    @Value("${udb.oracle.username:udbrnd}")
    private String udbOracleUsername;

    @Value("${udb.oracle.password:udb}")
    private String udbOraclePassword;

    @Override
    public void execute(DelegateExecution delegateExecution) {
        log.info("try to connect....");
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
                    log.info("IW: UDB CHECK Connected to the database!");


                    Long createdArtefactId = delegateExecution.hasVariable("createdArtefactId") ? (Long) delegateExecution.getVariable("createdArtefactId") : 0;
                    String UDBcheckCronValue = delegateExecution.hasVariable("UDBcheckCronValue") ? delegateExecution.getVariable("UDBcheckCronValue").toString() : "0 58 10 * * ?";

                    log.info("createdArtefactId: " + createdArtefactId.toString());

                    String selectArtefactCurrentState = "select ARTEFACTID, NCPID, ONAIR_DATE, G_ONAIR_DATE, INST_STATUS, INST_STATUS_DATE, POWER_STATUS from ARTEFACT_CURRENT_STATE where ARTEFACTID = ?";

                    PreparedStatement selectArtefactCurrentStatePS = udbConnect.prepareStatement(selectArtefactCurrentState);

                    int i = 1;
                    selectArtefactCurrentStatePS.setLong(i++, createdArtefactId.longValue());

                    log.info("UDBcheckCronValue");
                    log.info(UDBcheckCronValue);
                    log.info("selectArtefactCurrentState preparedStatement SQL UPDATE VALUES");
                    ResultSet resultSet = selectArtefactCurrentStatePS.executeQuery();
                    JSONArray json = new JSONArray();
                    ResultSetMetaData rsmd = resultSet.getMetaData();
//                    log.info(rsmd.getColumnCount());
                    while (resultSet.next()) {
//                        log.info(rsmd.getColumnCount());
                        int numColumns = rsmd.getColumnCount();
                        JSONObject obj = new JSONObject();
                        for (int j = 1; j <= numColumns; j++) {
                            String column_name = rsmd.getColumnName(j);
                            obj.put(column_name, resultSet.getObject(column_name));
                        }
                        json.put(obj);
                    }
                    log.info("done selectArtefactCurrentStatePS");
                    String dataFromUDB = "noData";
                    log.info("selectArtefactCurrentStatePS raws: " + json.length());
                    if (json.length() > 0) {
                        JSONObject firstJson = json.getJSONObject(0);
                        log.info(firstJson.toString());
                        log.info("done ARTEFACTID");
                        log.info(firstJson.getString("ARTEFACTID"));
                        log.info("done INST_STATUS");
                        log.info(firstJson.has("INST_STATUS") ? firstJson.getString("INST_STATUS") : "");
                        log.info("done POWER_STATUS");
                        log.info(firstJson.has("POWER_STATUS") ? firstJson.getString("POWER_STATUS") : "");
                        log.info("done ONAIR_DATE");
                        log.info(firstJson.has("ONAIR_DATE") ? firstJson.getString("ONAIR_DATE") : "");
                        log.info("done G_ONAIR_DATE");
                        log.info(firstJson.has("G_ONAIR_DATE") ? firstJson.getString("G_ONAIR_DATE") : "");

                        if ((firstJson.has("ONAIR_DATE") && !firstJson.getString("ONAIR_DATE").equals(null)) || (firstJson.has("G_ONAIR_DATE") && !firstJson.getString("G_ONAIR_DATE").equals(null))) {
                            log.info("ONAIR_DATE or G_ONAIR_DATE is not null");
                            dataFromUDB = "withFinishDate";
                            delegateExecution.setVariable("setInstStatusFromUDB", "FinishDate");
                            delegateExecution.setVariable("finishDateFromUDB", firstJson.has("ONAIR_DATE") ? firstJson.getString("ONAIR_DATE") : firstJson.has("G_ONAIR_DATE") ? firstJson.getString("G_ONAIR_DATE") : "");
                        }

                        if (firstJson.has("INST_STATUS") && dataFromUDB.equals("noData")) {
                            int uis = firstJson.getInt("INST_STATUS");
                            if (uis == 8 || uis == 15 || uis == 7 || uis == 4) {
                                dataFromUDB = "justSetInstStatus";
                                if (uis == 8) {
                                    delegateExecution.setVariable("setInstStatusFromUDB", "Instalation problem");
                                } else if (uis == 15) {
                                    delegateExecution.setVariable("setInstStatusFromUDB", "Installation finish");
                                } else if (uis == 7) {
                                    delegateExecution.setVariable("setInstStatusFromUDB", "Installation in progress");
                                } else if (uis == 4) {
                                    delegateExecution.setVariable("setInstStatusFromUDB", "SSID in progress");
                                }
                                log.info("justSetInstStatus");
                                delegateExecution.setVariable("instStatusFromUDB", uis);
                            } else if (uis == 16) {
                                dataFromUDB = "finishWithSetInstStatus";
                                log.info("finishWithSetInstStatus 16");
                                delegateExecution.setVariable("setInstStatusFromUDB", "Transmission problem");
                                delegateExecution.setVariable("instStatusFromUDB", uis);
                            } else if (uis == 13) {
                                dataFromUDB = "finishWithSetInstStatus";
                                log.info("finishWithSetInstStatus 13");
                                delegateExecution.setVariable("setInstStatusFromUDB", "Leasing problem");
                                delegateExecution.setVariable("instStatusFromUDB", uis);
                            }
                            log.info("INST_STATUS is not null");
                        }

                        if (firstJson.has("POWER_STATUS") && firstJson.getInt("POWER_STATUS") == 3 && dataFromUDB.equals("noData")) {
                            dataFromUDB = "withPowerStatus";
                            delegateExecution.setVariable("powerStatusFromUDB", firstJson.getInt("POWER_STATUS"));
                            delegateExecution.setVariable("setInstStatusFromUDB", "Power problem");
                            log.info("POWER_STATUS = 3 (withPowerProblem)");
                        }
                        if (dataFromUDB.equals("noData")) {
                            log.info("no changed data");
                            dataFromUDB = "noData";
                        }
                    }

                    delegateExecution.setVariable("dataFromUDB", dataFromUDB);
                    if (UDBcheckCronValue.equals("0 58 10 * * ?")) {
                        delegateExecution.setVariable("UDBcheckCronValue", "0 58 10 * * ?");
                    } else {
                        delegateExecution.setVariable("UDBcheckCronValue", "0 10 11 * * ?");
                    }

                    udbConnect.commit();
                    udbConnect.close();
                    log.info("udbConnection closed!");
                } else {
                    delegateExecution.setVariable("dataFromUDB", "noData");
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
