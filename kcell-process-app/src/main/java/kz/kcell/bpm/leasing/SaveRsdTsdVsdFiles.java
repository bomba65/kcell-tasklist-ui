package kz.kcell.bpm.leasing;

import kz.kcell.flow.files.Minio;
import lombok.extern.java.Log;
import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static java.sql.DriverManager.println;
import static org.camunda.spin.Spin.JSON;

@Log
@Service("SaveRsdTsdVsdFiles")
public class SaveRsdTsdVsdFiles implements JavaDelegate {

    private Minio minioClient;

    @Autowired
    public SaveRsdTsdVsdFiles(Minio minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        log.info("try to connect....");
        try {
            TimeZone timeZone = TimeZone.getTimeZone("Asia/Almaty");
            TimeZone.setDefault(timeZone);
            Class.forName ("oracle.jdbc.OracleDriver");
            Connection udbConnect = DriverManager.getConnection(
                "jdbc:oracle:thin:@//sc2-appcl010406:1521/apexudb", "app_apexudb_camunda", "p28zt#7C");
            try {
                if (udbConnect != null) {
                    udbConnect.setAutoCommit(false);
                    log.info("Connected to the database!");

                    // proc vars
                    Long createdArtefactId = (Long) delegateExecution.getVariable("createdArtefactId");
                    Long createdArtefactRSDId = (Long) delegateExecution.getVariable("createdArtefactRSDId");
                    String starter = delegateExecution.getVariable("starter").toString();

//                    SpinJsonNode tsdJson = JSON(delegateExecution.getVariable("uploadTSDfileFiles"));
                    SpinJsonNode tsdJson = JSON(delegateExecution.getVariable("createNewCandidateSiteFiles"));
                    SpinList tsdFiles = tsdJson.elements();
                    SpinJsonNode tsd = (SpinJsonNode) tsdFiles.get(0);
                    String fileName = tsd.prop("name").stringValue();
                    String filePath = tsd.prop("path").stringValue();
                    log.info("file....");
                    log.info(fileName);
                    log.info(filePath);
                    log.info("------");

                    InputStream inputStream = minioClient.getObject(filePath);
                    byte[] bytes = IOUtils.toByteArray(inputStream);

                    ByteArrayInputStream is = new ByteArrayInputStream(bytes);

                    //UPDATE ARTEFACT
                    String UPDATE_TSD_EXT = "INSERT INTO ARTEFACT_VSD(VSDID, ARTEFACTID, RSDID, FILENAME, LASTUPDATE, VSD_FILE) VALUE (ARTEFACT_VSD_SEQ.nextval, ?, ?, ?, ?, ?)";
                    PreparedStatement updateTSDextPreparedStatement = udbConnect.prepareStatement(UPDATE_TSD_EXT);

                    int i = 1;
                    log.info("preparedStatement.setValues");
                    // set values to insert
                    updateTSDextPreparedStatement.setLong(i++, createdArtefactId); // ARTEFACTID
                    updateTSDextPreparedStatement.setLong(i++, createdArtefactRSDId); // RSDID
                    updateTSDextPreparedStatement.setString(i++, fileName); // FILENAME
                    updateTSDextPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // LASTUPDATE
                    updateTSDextPreparedStatement.setBinaryStream(i++, is);

                    log.info("preparedStatement.executeUpdate()");
                    updateTSDextPreparedStatement.executeUpdate();
                    log.info("successfull insert to database!");

                    udbConnect.commit();
                    udbConnect.close();
                    log.info("udbConnection closed!");
                } else {
                    udbConnect.close();
                    log.warning("Failed to make connection!");
                }
            } catch (Exception e) {
                udbConnect.rollback();
                udbConnect.close();
                log.warning("connection Exception!");
                log.warning(e.toString());
                throw e;
            }
        } catch (SQLException e) {
            log.warning("testConnect SQLException!");
            log.warning(e.toString());
            log.warning("SQL State: %s\n%s");
            log.warning(e.getSQLState());
            log.warning(e.getMessage());
        } catch (Exception e) {
            log.warning("testConnect Exception!");
            e.printStackTrace();
        }

    }
}
