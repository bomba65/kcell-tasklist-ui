package kz.kcell.flow.leasing;

import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.camunda.spin.Spin.JSON;

@Log
@Service("UpdateTSD")
public class UpdateTSD implements JavaDelegate {

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
            Class.forName ("oracle.jdbc.OracleDriver");
            Connection udbConnect = DriverManager.getConnection(
                udbOracleUrl,
                udbOracleUsername,
                udbOraclePassword);
            try {
                if (udbConnect != null) {
                    udbConnect.setAutoCommit(false);
                    log.info("Connected to the database!");

                    // proc vars
                    Long createdArtefactExtTSDId = (Long) delegateExecution.getVariable("createdArtefactExtTSDId");
                    String starter = delegateExecution.getVariable("starter").toString();

                    SpinJsonNode candidate = JSON(delegateExecution.getVariable("candidate"));
                    String cn_longitude = candidate.prop("longitude").stringValue();
                    String cn_latitude = candidate.prop("latitude").stringValue();

                    SpinJsonNode feJson = JSON(delegateExecution.getVariable("farEndInformation"));
                    SpinList farEnds = feJson.elements();
                    SpinJsonNode fe = (SpinJsonNode) farEnds.get(0);
                    String fe_azimuth = fe.prop("azimuth").stringValue();
                    String fe_diameter = fe.prop("diameter").stringValue();
                    String fe_frequencyBand = "8"; //fe.prop("frequencyBand").numberValue();
                    String fe_suspensionHeight = fe.prop("suspensionHeight").stringValue();
                    String fe_constructionType = fe.prop("constructionType").prop("id").stringValue();
                    String fe_sitename = fe.prop("farEndName").stringValue();
                    String fe_comment = fe.prop("comments").stringValue();

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); //2020-01-02T18:00:00.000Z
                    String fe_survey_date = fe.prop("surveyDate").stringValue().substring(0,10);
                    Date fe_formated_survey_date = formatter.parse(fe_survey_date);

                    String fe_address = "" + (fe.prop("address").hasProp("cn_addr_oblast") ? fe.prop("address").prop("cn_addr_oblast").stringValue() : "") +
                        (fe.prop("address").hasProp("cn_addr_district") ? ", " + fe.prop("address").prop("cn_addr_district").stringValue() : "") +
                        (fe.prop("address").hasProp("cn_addr_city") ? ", " + fe.prop("address").prop("cn_addr_city").stringValue() : "") +
                        (fe.prop("address").hasProp("cn_addr_street") ? ", " + fe.prop("address").prop("cn_addr_street").stringValue() : "") +
                        (fe.prop("address").hasProp("cn_addr_building") ? ", " + fe.prop("address").prop("cn_addr_building").stringValue() : "") +
                        (fe.prop("address").hasProp("cn_addr_cadastral_number") ? ", " + fe.prop("address").prop("cn_addr_cadastral_number").stringValue() : "") +
                        (fe.prop("address").hasProp("cn_addr_note") ? ", " + fe.prop("address").prop("cn_addr_note").stringValue() : "");

                    SpinJsonNode ne = JSON(delegateExecution.getVariable("transmissionAntenna"));
                    String ne_azimuth = ne.prop("azimuth").stringValue();
                    String ne_diameter = ne.prop("diameter").stringValue();
                    String ne_frequencyBand = "8";//ne.prop("frequencyBand").numberValue();
                    Number ne_suspensionHeight = ne.prop("suspensionHeight").numberValue();

                    //UPDATE ARTEFACT
                    String UPDATE_TSD_EXT = "UPDATE ARTEFACT_TSD_EXT SET NE_LONGITUDE = ?, NE_LATITUDE = ?, FE_SITENAME = ?, FE_CONSTR_TYPE = ?, FE_ADDRESS = ?, SURVEY_DATE = ?, NE_AZIMUTH = ?, NE_ANTENNADIAMETER = ?, NE_SUSPENSIONHEIGHT = ?, NE_TXRF_FREQUENCY = ?, FE_AZIMUTH = ?, FE_ANTENNADIAMETER = ?, FE_SUSPENSIONHEIGHT = ?, FE_TXRF_FREQUENCY = ?, COMMENTS = ?, UPDATE_DATE = ?, UPDATE_PERSON = ? WHERE TSDID = ?";
                    PreparedStatement updateTSDextPreparedStatement = udbConnect.prepareStatement(UPDATE_TSD_EXT);

                    int i = 1;
                    log.info("preparedStatement.setValues");
                    // set values to insert
                    updateTSDextPreparedStatement.setString(i++, cn_longitude); // NE_LONGITUDE
                    updateTSDextPreparedStatement.setString(i++, cn_latitude); // NE_LATITUDE
                    updateTSDextPreparedStatement.setString(i++, fe_sitename); // FE_SITENAME
                    updateTSDextPreparedStatement.setLong(i++, Integer.parseInt(fe_constructionType)); // FE_CONSTR_TYPE
                    updateTSDextPreparedStatement.setString(i++, fe_address); // FE_ADDRESS
                    updateTSDextPreparedStatement.setDate(i++, new java.sql.Date(fe_formated_survey_date.getTime())); // SURVEY_DATE (fe_survey_date)
                    updateTSDextPreparedStatement.setLong(i++, Integer.parseInt(ne_azimuth)); // NE_AZIMUTH
                    updateTSDextPreparedStatement.setFloat(i++, Float.parseFloat(ne_diameter)); // NE_ANTENNADIAMETER
                    updateTSDextPreparedStatement.setLong(i++, ne_suspensionHeight.longValue()); // NE_SUSPENSIONHEIGHT
                    updateTSDextPreparedStatement.setLong(i++, Integer.parseInt(ne_frequencyBand)); // NE_TXRF_FREQUENCY
                    updateTSDextPreparedStatement.setLong(i++, Integer.parseInt(fe_azimuth)); // FE_AZIMUTH
                    updateTSDextPreparedStatement.setFloat(i++, Float.parseFloat(fe_diameter)); // FE_ANTENNADIAMETER
                    updateTSDextPreparedStatement.setLong(i++, Integer.parseInt(fe_suspensionHeight)); // FE_SUSPENSIONHEIGHT
                    updateTSDextPreparedStatement.setLong(i++, Integer.parseInt(fe_frequencyBand)); // FE_TXRF_FREQUENCY
                    updateTSDextPreparedStatement.setString(i++, fe_comment); // COMMENTS
                    updateTSDextPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // INSERT_DATE
                    updateTSDextPreparedStatement.setString(i++, starter); // INSERT_PERSON
                    updateTSDextPreparedStatement.setLong(i++, createdArtefactExtTSDId); // TSDID

                    log.info("preparedStatement.executeUpdate()");
                    updateTSDextPreparedStatement.executeUpdate();
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
