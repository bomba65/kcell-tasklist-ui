package kz.kcell.flow.leasing;

import kz.kcell.flow.files.Minio;
import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
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

@Service("SaveRsdTsdVsdFiles")
public class SaveRsdTsdVsdFiles implements JavaDelegate {

    private Minio minioClient;

    @Autowired
    public SaveRsdTsdVsdFiles(Minio minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        System.out.println("try to connect....");
        try {
            TimeZone timeZone = TimeZone.getTimeZone("Asia/Almaty");
            TimeZone.setDefault(timeZone);
            Class.forName ("oracle.jdbc.OracleDriver");
            Connection udbConnect = DriverManager.getConnection(
                "jdbc:oracle:thin:@//sc2-appcl010406:1521/apexudb", "udbrnd", "udb");
            try {
                if (udbConnect != null) {
                    udbConnect.setAutoCommit(false);
                    System.out.println("Connected to the database!");
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
                    System.out.println("preparedStatement.setValues");
                    // set values to insert
                    updateTSDextPreparedStatement.setLong(i++, createdArtefactId); // ARTEFACTID
                    updateTSDextPreparedStatement.setLong(i++, createdArtefactRSDId); // RSDID
                    updateTSDextPreparedStatement.setString(i++, tsdFileName); // FILENAME
                    updateTSDextPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // LASTUPDATE
                    updateTSDextPreparedStatement.setBinaryStream(i++, tsdIs);

                    System.out.println("preparedStatement.executeUpdate()");
                    updateTSDextPreparedStatement.executeUpdate();
                    System.out.println("successfull insert to database!");

                    i = 1;
                    updateVSDextPreparedStatement.setLong(i++, createdArtefactId); // ARTEFACTID
                    updateVSDextPreparedStatement.setLong(i++, createdArtefactRSDId); // RSDID
                    updateVSDextPreparedStatement.setString(i++, vsdFileName); // FILENAME
                    updateVSDextPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // LASTUPDATE
                    updateVSDextPreparedStatement.setBinaryStream(i++, vsdIs);

                    System.out.println("preparedStatement.executeUpdate()");
                    updateVSDextPreparedStatement.executeUpdate();
                    System.out.println("successfull insert to database!");

                    i = 1;
                    updateRSDextPreparedStatement.setLong(i++, createdArtefactId); // ARTEFACTID
                    updateRSDextPreparedStatement.setLong(i++, createdArtefactRSDId); // RSDID
                    updateRSDextPreparedStatement.setString(i++, rsdFileName); // FILENAME
                    updateRSDextPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // LASTUPDATE
                    updateRSDextPreparedStatement.setBinaryStream(i++, rsdIs);

                    System.out.println("preparedStatement.executeUpdate()");
                    updateRSDextPreparedStatement.executeUpdate();
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
