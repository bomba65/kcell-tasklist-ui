package kz.kcell.flow.leasing;

import kz.kcell.flow.files.Minio;
import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.postgresql.util.PGobject;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.awt.print.Pageable;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

import static org.camunda.spin.Spin.JSON;

@Log
@Service("CreateNCP")
public class CreateNCP implements JavaDelegate {

    private Minio minioClient;

    @Autowired
    DataSource dataSource;

    @Autowired
    public CreateNCP(Minio minioClient) {
        this.minioClient = minioClient;
    }

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
            Class.forName("oracle.jdbc.OracleDriver");
            log.info(udbOracleUrl);
            log.info(udbOracleUsername);
            log.info(udbOraclePassword);
            Connection udbConnect = DriverManager.getConnection(
                udbOracleUrl,
                udbOracleUsername,
                udbOraclePassword);
//            Connection udbConnect = DriverManager.getConnection("jdbc:oracle:thin:@//sc2-appcl010406:1521/apexudb", "app_apexudb_camunda", "p28zt#7C");
            try {
                if (udbConnect != null) {
                    udbConnect.setAutoCommit(false);
                    log.info("Connected to the database!");

                    // proc vars
                    String ncpId = delegateExecution.getVariable("ncpID") != null ? delegateExecution.getVariable("ncpID").toString() : null;
//                    String region = delegateExecution.getVariable("region").toString(); // not number
                    String longitude = delegateExecution.getVariable("longitude") != null ? delegateExecution.getVariable("longitude").toString() : null;
                    String latitude = delegateExecution.getVariable("latitude") != null ? delegateExecution.getVariable("latitude").toString() : null;
//                    String reason = delegateExecution.getVariable("reason").toString(); // it's string value, not dictionaries id
                    String starter = delegateExecution.getVariable("starter") != null ? delegateExecution.getVariable("starter").toString() : null;
                    String targetCell = delegateExecution.getVariable("createNCPTaskComment") != null ? delegateExecution.getVariable("createNCPTaskComment").toString() : null;
                    String targetCoverage = delegateExecution.getVariable("targetCoverage") != null ? delegateExecution.getVariable("targetCoverage").toString() : null;
                    String regionCode = delegateExecution.getVariable("regionCode") != null ? delegateExecution.getVariable("regionCode").toString() : null;
                    String rbsType = delegateExecution.getVariable("rbsTypeId") != null ? delegateExecution.getVariable("rbsTypeId").toString() : null;
                    Integer rbsTypeInt = Integer.parseInt(rbsType);
                    String createNCPTaskComment = delegateExecution.getVariable("createNCPTaskComment") != null ? delegateExecution.getVariable("createNCPTaskComment").toString() : null;
                    String bandsIdForUDB = delegateExecution.getVariable("bandsIdForUDB") != null ? delegateExecution.getVariable("bandsIdForUDB").toString() : null;
                    String plannedCabinetTypeIdForUDB = delegateExecution.getVariable("plannedCabinetTypeIdForUDB") != null ? delegateExecution.getVariable("plannedCabinetTypeIdForUDB").toString() : null;

                    Integer reasonInt = null;
                    SpinJsonNode reasonJson = delegateExecution.getVariable("reason") != null ? JSON(delegateExecution.getVariable("reason")) : null;
                    if (reasonJson != null) {
                        try {
                            SpinJsonNode reason = reasonJson.prop("id");
                            String reasonString = reason.value().toString();
                            reasonInt = Integer.parseInt(reasonString);
                        } catch (Exception e) {
                            log.warning(e.getMessage());
                        }
                    }

                    Integer siteTypeInt = null;
                    SpinJsonNode siteTypeJson = delegateExecution.getVariable("siteType") != null ? JSON(delegateExecution.getVariable("siteType")) : null;
                    if (siteTypeJson != null) {
                        try {
                            SpinJsonNode siteType = siteTypeJson.prop("id");
                            String siteTypeString = siteType.value().toString();
                            siteTypeInt = Integer.parseInt(siteTypeString);
                        } catch (Exception e) {
                            log.warning(e.getMessage());
                        }
                    }

                    Integer projectInt = null;
                    SpinJsonNode projectJson = delegateExecution.getVariable("project") != null ? JSON(delegateExecution.getVariable("project")) : null;
                    if (projectJson != null) {
                        try {
                            SpinJsonNode project = projectJson.prop("id");
                            String projectString = project.value().toString();
                            projectInt = Integer.parseInt(projectString);
                        } catch (Exception e) {
                            log.warning(e.getMessage());
                        }
                    }

                    Integer initiatorInt = null;
                    SpinJsonNode initiatorJson = delegateExecution.getVariable("initiator") != null ? JSON(delegateExecution.getVariable("initiator")) : null;
                    if (initiatorJson != null) {
                        try {
                            SpinJsonNode initiator = initiatorJson.prop("id");
                            String initiatorString = initiator.value().toString();
                            initiatorInt = Integer.parseInt(initiatorString);
                        } catch (Exception e) {
                            log.warning(e.getMessage());
                        }
                    }

                    Integer partInt = null;
                    SpinJsonNode partJson = delegateExecution.getVariable("part") != null ? JSON(delegateExecution.getVariable("part")) : null;
                    try {
                        SpinJsonNode part = partJson.prop("id");
                        String partString = part.value().toString();
                        partInt = Integer.parseInt(partString);
                    } catch (Exception e) {
                        log.warning(e.getMessage());
                    }

                    //insert NCP
                    String returnCols[] = {"ARTEFACTID"};
                    String insertNCP = "INSERT INTO NCP_CREATION ( ARTEFACTID, NCPID, TARGET_CELL, REGION, LONGITUDE, LATITUDE, REASON, PROJECT, CREATOR, DATEOFINSERT, COMMENTS, CABINETID, TARGET_COVERAGE, TYPE, GEN_STATUS, TR_STATUS, NCP_STATUS, NCP_STATUS_DATE, BAND, INITIATOR, PART, SOURCE) VALUES (NCP_CREATION_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, ?, ?, ?, ?, 7, 0, 2, SYSDATE, ?, ?, ?, ?)";
                    PreparedStatement preparedStatement = udbConnect.prepareStatement(insertNCP, returnCols);

                    int i = 1;
                    log.info("preparedStatement.setValues");
                    // set values to insert
                    preparedStatement.setString(i++, ncpId); // NCPID
                    Integer regionCodeInt = Integer.parseInt(regionCode);
                    if (targetCell != null) {
                        preparedStatement.setString(i++, targetCell); // target_cell
                    } else {
                        preparedStatement.setNull(i++, Types.VARCHAR);
                    }
                    if (regionCodeInt != null) {
                        preparedStatement.setLong(i++, regionCodeInt); // REGION
                    } else {
                        preparedStatement.setNull(i++, Types.BIGINT);
                    }
                    preparedStatement.setString(i++, "E " + longitude.replace(".", ",")); // LONGITUDE ex. E 76,890775
                    preparedStatement.setString(i++, "N " + latitude.replace(".", ",")); // LATITUDE
                    if (reasonInt != null) {
                        preparedStatement.setLong(i++, reasonInt); // REASON
                    } else {
                        preparedStatement.setNull(i++, Types.BIGINT);

                    }
                    if (projectInt != null) {
                        preparedStatement.setLong(i++, projectInt); // PROJECT
                    } else {
                        preparedStatement.setNull(i++, Types.BIGINT);
                    }
                    preparedStatement.setString(i++, starter); // CREATOR 'SERGEI.ZAITSEV'
//                    preparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // DATEOFINSERT
                    preparedStatement.setString(i++, createNCPTaskComment); // COMMENTS
//                    Integer plannedCabinetTypeIdForUDBInt = Integer.parseInt(plannedCabinetTypeIdForUDB);
                    if (rbsTypeInt != null) {
                        preparedStatement.setLong(i++, rbsTypeInt); // CABINETID
                    } else {
                        preparedStatement.setNull(i++, Types.BIGINT);
                    }
                    preparedStatement.setString(i++, targetCoverage); // TARGET_COVERAGE
                    if (siteTypeInt != null) {
                        preparedStatement.setLong(i++, siteTypeInt); // TYPE ncp_type (Подставлять ID согласно справочнику Site Type) ex: 6
                    } else {
                        preparedStatement.setNull(i++, Types.BIGINT);
                    }
//                    preparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // NCP_STATUS_DATE
                    preparedStatement.setString(i++, bandsIdForUDB); // BAND ex:'1'   ncp_band	(Подставлять ID согласно справочнику Bands)
                    if (initiatorInt != null) {
                        preparedStatement.setLong(i++, initiatorInt); // INITIATOR
                    } else {
                        preparedStatement.setNull(i++, Types.BIGINT);
                    }
                    if (partInt != null) {
                        preparedStatement.setLong(i++, partInt); // PART (Подставлять ID согласно справочнику Part)
                    } else {
                        preparedStatement.setNull(i++, Types.BIGINT);
                    }
                    preparedStatement.setString(i++, "KWMS");

                    log.info("preparedStatement.executeUpdate()");
                    preparedStatement.executeUpdate();
                    log.info("successfull insert to database!");
//
                    ResultSet headGeneratedIdResultSet = preparedStatement.getGeneratedKeys();
                    headGeneratedIdResultSet.next();
                    Long ncpCreatedId = headGeneratedIdResultSet.getLong(1);
                    log.info("artefactGeneratedId:");
                    log.info(ncpCreatedId.toString());

                    //insert new status
                    Long createdNcpStatusId = null;
                    String returnStatus[] = {"STATUS_ACTION_ID"};
                    String insertNewStatus = "insert into NCP_CREATION_STATUS_ACTION values ( NCP_CREATION_STATUS_ACTIO_SEQ.nextval, ?, 2, ?, SYSDATE, ?)";
                    PreparedStatement newNcpStatusPreparedStatement = udbConnect.prepareStatement(insertNewStatus, returnStatus);

                    i = 1;
                    log.info("newNcpStatusPreparedStatement.setString");
                    newNcpStatusPreparedStatement.setLong(i++, ncpCreatedId);
                    newNcpStatusPreparedStatement.setString(i++, starter); // CREATOR 'SERGEI.ZAITSEV'
//                    newNcpStatusPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // DATEOFINSERT
                    newNcpStatusPreparedStatement.setString(i++, createNCPTaskComment); // COMMENTS
                    log.info("newNcpStatusPreparedStatement.executeUpdate()");
                    newNcpStatusPreparedStatement.executeUpdate();
                    log.info("successfull insert to database!");

                    ResultSet statusGeneratedIdResultSet = newNcpStatusPreparedStatement.getGeneratedKeys();
                    statusGeneratedIdResultSet.next();
                    createdNcpStatusId = statusGeneratedIdResultSet.getLong(1);
                    log.info("createdNcpStatusId:");
                    log.info(createdNcpStatusId.toString());

                    udbConnect.commit();
                    delegateExecution.setVariable("ncpCreatedId", ncpCreatedId);
                    delegateExecution.setVariable("createdNcpStatusId", createdNcpStatusId);
                    delegateExecution.setVariable("ncpInsertDate", new Date());
                    udbConnect.close();
                    log.warning("udbConnection closed!");
                } else {
                    udbConnect.close();
                    log.warning("Failed to make connection!");
                }
            } catch (Exception e) {
                udbConnect.rollback();
                udbConnect.close();
                log.warning("connection Exception!");
                log.warning(e.getMessage());
                throw e;
            }
        } catch (SQLException e) {
            log.warning("testConnect SQLException!");
            log.warning(e.toString());
            log.warning("SQL State: %s\n%s");
            log.warning(e.getSQLState());
            log.warning(e.getMessage());
            throw e;
        } catch (Exception e) {
            log.info("testConnect Exception!");
            e.printStackTrace();
            throw e;
        }
    }
}
