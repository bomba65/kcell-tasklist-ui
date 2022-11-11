package kz.kcell.flow.leasing;

import kz.kcell.flow.files.Minio;
import lombok.extern.java.Log;
import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.camunda.spin.Spin.JSON;

@Log
@Service("SaveRsdTsdVsdFiles")
public class SaveRsdTsdVsdFiles implements JavaDelegate {

    private Minio minioClient;

    @Value("${udb.oracle.url:jdbc:oracle:thin:@//apexudb-pmy:1521/apexudb}")
    private String udbOracleUrl;

    @Value("${udb.oracle.username:udbrnd}")
    private String udbOracleUsername;

    @Value("${udb.oracle.password:udb}")
    private String udbOraclePassword;

    @Value("${udb.oracle.enabled}")
    private Boolean udbOracleEnabled;

    @Autowired
    public SaveRsdTsdVsdFiles(Minio minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        if (!udbOracleEnabled) return;
        log.info("try to connect....");
        try {
            TimeZone timeZone = TimeZone.getTimeZone("Asia/Almaty");
            TimeZone.setDefault(timeZone);
            Class.forName ("oracle.jdbc.OracleDriver");
            Connection udbConnect = DriverManager.getConnection(
                udbOracleUrl,
                udbOracleUsername,
                udbOraclePassword);
//                Connection udbConnect = DriverManager.getConnection("jdbc:oracle:thin:@//sc2-appcl010406:1521/apexudb", "app_apexudb_camunda", "p28zt#7C");
            try {
                if (udbConnect != null) {
                    udbConnect.setAutoCommit(false);
                    log.info("Connected to the database!");
                    List<String> fileList = Stream.of("uploadTSDfileFiles",
                        "uploadRSDandVSDfilesFiles",
                        "createdRSDFile").collect(Collectors.toList());

                    Long createdArtefactId = (Long) delegateExecution.getVariable("createdArtefactId");
                    Long createdArtefactRSDId = (Long) delegateExecution.getVariable("createdArtefactRSDId");
                    SpinJsonNode vsdJson = JSON(delegateExecution.getVariable("uploadRSDandVSDfilesFiles"));
                    SpinJsonNode tsdJson = JSON(delegateExecution.getVariable("uploadTSDfileFiles"));
                    SpinJsonNode rsdJson = JSON(delegateExecution.getVariable("createdRSDFile"));

                    SpinList tsdFiles = tsdJson.elements();
                    SpinList vsdFiles = vsdJson.elements();
                    SpinList rsdFiles = rsdJson.elements();

                    SpinJsonNode tsd = (SpinJsonNode) tsdFiles.get(0);
                    SpinJsonNode vsd = (SpinJsonNode) vsdFiles.get(0);
                    SpinJsonNode rsd = (SpinJsonNode) rsdFiles.get(0);
                    String tsdFileName = tsd.prop("name").stringValue();
                    String tsdFilePath = tsd.prop("path").stringValue();
                    String vsdFileName = vsd.prop("name").stringValue();
                    String vsdFilePath = vsd.prop("path").stringValue();
                    String rsdFileName = rsd.prop("name").stringValue();
                    String rsdFilePath = rsd.prop("path").stringValue();

                    InputStream tsdInputStream = minioClient.getObject(tsdFilePath);
                    byte[] tsdBytes = IOUtils.toByteArray(tsdInputStream);
                    ByteArrayInputStream tsdIs = new ByteArrayInputStream(tsdBytes);

                    InputStream rsdInputStream = minioClient.getObject(rsdFilePath);
                    byte[] rsdBytes = IOUtils.toByteArray(rsdInputStream);
                    ByteArrayInputStream rsdIs = new ByteArrayInputStream(rsdBytes);

                    InputStream vsdInputStream = minioClient.getObject(vsdFilePath);
                    byte[] vsdBytes = IOUtils.toByteArray(vsdInputStream);
                    ByteArrayInputStream vsdIs = new ByteArrayInputStream(vsdBytes);


                    //UPDATE ARTEFACT
                    String UPDATE_TSD_EXT = "INSERT INTO ARTEFACT_VSD(VSDID, ARTEFACTID, RSDID, FILENAME, LASTUPDATE, VSD_FILE) VALUES (ARTEFACT_VSD_SEQ.nextval, ?, ?, ?, ?, ?)";
                    String UPDATE_VSD_EXT = "INSERT INTO ARTEFACT_VSD(VSDID, ARTEFACTID, RSDID, FILENAME, LASTUPDATE, VSD_FILE) VALUES (ARTEFACT_VSD_SEQ.nextval, ?, ?, ?, ?, ?)";
                    String UPDATE_RSD_EXT = "INSERT INTO ARTEFACT_VSD(VSDID, ARTEFACTID, RSDID, FILENAME, LASTUPDATE, VSD_FILE) VALUES (ARTEFACT_VSD_SEQ.nextval, ?, ?, ?, ?, ?)";
                    PreparedStatement updateTSDextPreparedStatement = udbConnect.prepareStatement(UPDATE_TSD_EXT);
                    PreparedStatement updateVSDextPreparedStatement = udbConnect.prepareStatement(UPDATE_VSD_EXT);
                    PreparedStatement updateRSDextPreparedStatement = udbConnect.prepareStatement(UPDATE_RSD_EXT);

                    int i = 1;
                    log.info("Save Files (RSD, TSD, VSD) to ArtefactId: " + createdArtefactId);
                    // set values to insert
                    updateTSDextPreparedStatement.setLong(i++, createdArtefactId); // ARTEFACTID
                    updateTSDextPreparedStatement.setLong(i++, createdArtefactRSDId); // RSDID
                    updateTSDextPreparedStatement.setString(i++, tsdFileName); // FILENAME
                    updateTSDextPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // LASTUPDATE
                    updateTSDextPreparedStatement.setBinaryStream(i++, tsdIs);

                    log.info("saving (updateTSDextPreparedStatement.executeUpdate)");
                    updateTSDextPreparedStatement.executeUpdate();
                    log.info("successfull insert tsdFile to database!");

                    i = 1;
                    updateVSDextPreparedStatement.setLong(i++, createdArtefactId); // ARTEFACTID
                    updateVSDextPreparedStatement.setLong(i++, createdArtefactRSDId); // RSDID
                    updateVSDextPreparedStatement.setString(i++, vsdFileName); // FILENAME
                    updateVSDextPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // LASTUPDATE
                    updateVSDextPreparedStatement.setBinaryStream(i++, vsdIs);

                    log.info("saving (updateVSDextPreparedStatement.executeUpdate)");
                    updateVSDextPreparedStatement.executeUpdate();
                    log.info("successfull insert vsdFile to database!");

                    i = 1;
                    updateRSDextPreparedStatement.setLong(i++, createdArtefactId); // ARTEFACTID
                    updateRSDextPreparedStatement.setLong(i++, createdArtefactRSDId); // RSDID
                    updateRSDextPreparedStatement.setString(i++, rsdFileName); // FILENAME
                    updateRSDextPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // LASTUPDATE
                    updateRSDextPreparedStatement.setBinaryStream(i++, rsdIs);

                    log.info("saving (updateRSDextPreparedStatement.executeUpdate)");
                    updateRSDextPreparedStatement.executeUpdate();
                    log.info("successfull insert rsdFile to database!");

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
