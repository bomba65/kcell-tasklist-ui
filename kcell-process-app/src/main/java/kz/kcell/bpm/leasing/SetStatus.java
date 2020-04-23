package kz.kcell.bpm.leasing;

import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
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
@Service("SetStatus")
public class SetStatus implements JavaDelegate {

    @Autowired
    DataSource dataSource;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        try {
            TimeZone timeZone = TimeZone.getTimeZone("Asia/Almaty");
            TimeZone.setDefault(timeZone);
            Class.forName ("oracle.jdbc.OracleDriver");
            Connection udbConnect = DriverManager.getConnection(
                "jdbc:oracle:thin:@//sc2-appcl010406:1521/apexudb", "app_apexudb_camunda", "p28zt#7C");
            try {
                if (udbConnect != null) {
                    udbConnect.setAutoCommit(false);
                    log.info("SET STATUS Connected to the database!");

                    // proc vars
                    int cn_rbs_location = 2;
                    int cn_altitude = 689;
                    //generated vars
                    Long ncpCreatedId = (Long) delegateExecution.getVariable("ncpCreatedId");
//                    Long createdNcpStatusId = (Long) delegateExecution.getVariable("createdNcpStatusId");
                    Long createdArtefactId = (Long) delegateExecution.getVariable("createdArtefactId");
//                    Long createdArtefactRSDId = (Long) delegateExecution.getVariable("createdArtefactRSDId");
//                    Long createdArtefactRRId = (Long) delegateExecution.getVariable("createdArtefactRRId");
//                    Long createdArtefactVSDId = (Long) delegateExecution.getVariable("createdArtefactVSDId");
                    Long createdCandApprovalId = (Long) delegateExecution.getVariable("createdCandApprovalId");
                    Long createdArtefactRRStatusId = (Long) delegateExecution.getVariable("createdArtefactRRStatusId");

                    String ncpId = delegateExecution.getVariable("ncpID").toString();
                    String _SET_NCP_STATUS = delegateExecution.hasVariableLocal("_SET_NCP_STATUS") ? delegateExecution.getVariableLocal("_SET_NCP_STATUS").toString() : "";
                    String _SET_GEN_STATUS = delegateExecution.hasVariableLocal("_SET_GEN_STATUS") ? delegateExecution.getVariableLocal("_SET_GEN_STATUS").toString() : "";
                    String _SET_RR_STATUS = delegateExecution.hasVariableLocal("_SET_RR_STATUS") ? delegateExecution.getVariableLocal("_SET_RR_STATUS").toString() : "";
                    String _SET_POWER_STATUS = delegateExecution.hasVariableLocal("_SET_POWER_STATUS") ? delegateExecution.getVariableLocal("_SET_POWER_STATUS").toString() : "";
                    String _SET_INST_STATUS = delegateExecution.hasVariableLocal("_SET_INST_STATUS") ? delegateExecution.getVariableLocal("_SET_INST_STATUS").toString() : "";
                    String _SET_FE_STATUS = delegateExecution.hasVariableLocal("_SET_FE_STATUS") ? delegateExecution.getVariableLocal("_SET_FE_STATUS").toString() : "";
                    String _SET_FE_STATUS_NCP = delegateExecution.hasVariableLocal("_SET_FE_STATUS_NCP") ? delegateExecution.getVariableLocal("_SET_FE_STATUS_NCP").toString() : _SET_FE_STATUS;
                    String _SET_CAND_STATUS = delegateExecution.hasVariableLocal("_SET_CAND_STATUS") ? delegateExecution.getVariableLocal("_SET_CAND_STATUS").toString() : "";

                    log.info("ncpId");
                    log.info(ncpId);
                    int i;

                    if (_SET_NCP_STATUS != null && !_SET_NCP_STATUS.equals("")) {
                        log.info("_SET_NCP_STATUS");
                        log.info(_SET_NCP_STATUS);
                        //UPDATE NCP
                        String UPDATENCPstatus = "update NCP_CREATION set NCP_STATUS = ?, NCP_STATUS_DATE = ? where NCPID = ?";
                        PreparedStatement updateNCPstatusPreparedStatement = udbConnect.prepareStatement(UPDATENCPstatus);

                        i = 1;
                        log.info("_SET_NCP_STATUS preparedStatement SQL UPDATE VALUES");
                        // set values to update
                        updateNCPstatusPreparedStatement.setLong(i++, Integer.parseInt(_SET_NCP_STATUS)); // NCP_status
                        updateNCPstatusPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // NCP_STATUS_DATE
                        updateNCPstatusPreparedStatement.setString(i++, ncpId); // NCPID

                        updateNCPstatusPreparedStatement.executeUpdate();
                        log.info("successfull NCP_STATUS updated!");
                    }
                    if (_SET_GEN_STATUS != null && !_SET_GEN_STATUS.equals("")) {
                        log.info("_SET_GEN_STATUS");
                        log.info(_SET_GEN_STATUS);
                        //UPDATE NCP
                        String UPDATE_NCP_CREATION_GEN_STATUS = "update NCP_CREATION set GEN_STATUS = ? where ARTEFACTID = ?";
                        String UPDATE_ARTEFACT_GEN_STATUS = "update ARTEFACT_CURRENT_STATE set GS_STATUS = ? where ARTEFACTID = ?";
                        PreparedStatement updateGENstatusInNCPPreparedStatement = udbConnect.prepareStatement(UPDATE_NCP_CREATION_GEN_STATUS);
                        PreparedStatement updateGENstatusInArtefactPreparedStatement = udbConnect.prepareStatement(UPDATE_ARTEFACT_GEN_STATUS);

                        log.info("GEN_STATUS preparedStatement SQL UPDATE VALUES");
                        // set values to update
                        i = 1;
                        updateGENstatusInNCPPreparedStatement.setLong(i++, Integer.parseInt(_SET_GEN_STATUS)); // GEN_STATUS
                        updateGENstatusInNCPPreparedStatement.setLong(i++, ncpCreatedId); // ARTEFACTID
                        updateGENstatusInNCPPreparedStatement.executeUpdate();
                        i = 1;
                        updateGENstatusInArtefactPreparedStatement.setLong(i++, Integer.parseInt(_SET_GEN_STATUS)); // GS_STATUS
                        updateGENstatusInArtefactPreparedStatement.setLong(i++, createdArtefactId); // ARTEFACTID
                        updateGENstatusInArtefactPreparedStatement.executeUpdate();
                        log.info("successfull GEN_STATUS updated!");
                    }
                    if (_SET_RR_STATUS != null && !_SET_RR_STATUS.equals("")) {
                        log.info("_SET_RR_STATUS");
                        log.info(_SET_RR_STATUS);
                        //UPDATE NCP
                        String UPDATE_ArtRRst_CREATION_RR_STATUS = "update ARTEFACT_RR_STATUS set RR_STATUS_ID = ?, DATEOFPERFORM = ? where ID = ?";
                        String UPDATE_ARTEFACT_RR_STATUS = "update ARTEFACT_CURRENT_STATE set RR_STATUS = ?, RR_STATUS_DATE = ? where ARTEFACTID = ?";
                        PreparedStatement updateRRstatusInArtRRstPreparedStatement = udbConnect.prepareStatement(UPDATE_ArtRRst_CREATION_RR_STATUS);
                        PreparedStatement updateRRstatusInArtefactPreparedStatement = udbConnect.prepareStatement(UPDATE_ARTEFACT_RR_STATUS);

                        log.info("RR_STATUS preparedStatement SQL UPDATE VALUES");
                        // set values to update
                        i = 1;
                        updateRRstatusInArtRRstPreparedStatement.setLong(i++, Integer.parseInt(_SET_RR_STATUS)); // RR_STATUS
                        updateRRstatusInArtRRstPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // DATEOFPERFORM
                        updateRRstatusInArtRRstPreparedStatement.setLong(i++, createdArtefactRRStatusId); // Artefact RRStatus ID
                        updateRRstatusInArtRRstPreparedStatement.executeUpdate();
                        i = 1;
                        updateRRstatusInArtefactPreparedStatement.setLong(i++, Integer.parseInt(_SET_RR_STATUS)); // GS_STATUS
                        updateRRstatusInArtefactPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // RR_STATUS_DATE
                        updateRRstatusInArtefactPreparedStatement.setLong(i++, createdArtefactId); // ARTEFACTID
                        updateRRstatusInArtefactPreparedStatement.executeUpdate();
                        log.info("ArtefactID: " + createdArtefactId + " successfully RR_STATUS updated to " + _SET_RR_STATUS);
                    }
                    if (_SET_CAND_STATUS != null && !_SET_CAND_STATUS.equals("")) {
                        log.info("_SET_CAND_STATUS");
                        log.info(_SET_CAND_STATUS);
                        //UPDATE NCP
                        String UPDATE_Cand_Apr__CAND_STATUS = "update CANDAPPROVAL set STATUSID = ?, DESDATE = ? where ARTEFACTID = ?";
                        String UPDATE_ArtCurSt__CAND_STATUS = "update ARTEFACT_CURRENT_STATE set CAND_STATUS = ?, CAND_STATUS_DATE = ? where ARTEFACTID = ?";
                        PreparedStatement updateCand_AprCandStatusPreparedStatement = udbConnect.prepareStatement(UPDATE_Cand_Apr__CAND_STATUS);
                        PreparedStatement updateCandStatusInCandAprPreparedStatement = udbConnect.prepareStatement(UPDATE_ArtCurSt__CAND_STATUS);

                        log.info("CAND_STATUS preparedStatement SQL UPDATE VALUES");
                        // set values to update
                        i = 1;
                        updateCand_AprCandStatusPreparedStatement.setLong(i++, Integer.parseInt(_SET_CAND_STATUS)); // STATUSID
                        updateCand_AprCandStatusPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // DESDATE
                        updateCand_AprCandStatusPreparedStatement.setLong(i++, createdArtefactId); // ARTEFACTID
                        updateCand_AprCandStatusPreparedStatement.executeUpdate();
                        i = 1;
                        updateCandStatusInCandAprPreparedStatement.setLong(i++, Integer.parseInt(_SET_CAND_STATUS)); // CAND_STATUS
                        updateCandStatusInCandAprPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // CAND_STATUS_DATE
                        updateCandStatusInCandAprPreparedStatement.setLong(i++, createdArtefactId); // ARTEFACTID
                        updateCandStatusInCandAprPreparedStatement.executeUpdate();
                        log.info("successfull CAND_STATUS updated!");
                    }
                    if (_SET_INST_STATUS != null && !_SET_INST_STATUS.equals("")) {
                        log.info("_SET_INST_STATUS");
                        log.info(_SET_INST_STATUS);
                        //UPDATE NCP
                        String UPDATE_ArtCurSt__INSTALLATION_STATUS = "update ARTEFACT_CURRENT_STATE set INST_STATUS = ?, INST_STATUS_DATE = ? where ARTEFACTID = ?";
                        PreparedStatement updateInstStatusInArtCurStPreparedStatement = udbConnect.prepareStatement(UPDATE_ArtCurSt__INSTALLATION_STATUS);

                        log.info("INSTALLATION_STATUS preparedStatement SQL UPDATE VALUES");
                        // set values to update
                        i = 1;
                        updateInstStatusInArtCurStPreparedStatement.setLong(i++, Integer.parseInt(_SET_INST_STATUS)); // INSTALLATION_STATUS
                        updateInstStatusInArtCurStPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // CAND_STATUS_DATE
                        updateInstStatusInArtCurStPreparedStatement.setLong(i++, createdArtefactId); // ARTEFACTID
                        updateInstStatusInArtCurStPreparedStatement.executeUpdate();
                        log.info("successfull INSTALLATION_STATUS updated!");
                    }
                    if (_SET_POWER_STATUS != null && !_SET_POWER_STATUS.equals("")) {
                        log.info("_SET_POWER_STATUS");
                        log.info(_SET_POWER_STATUS);
                        //UPDATE NCP
                        String UPDATE_ArtCurSt_POWER_STATUS = "update ARTEFACT_CURRENT_STATE set POWER_STATUS = ? where ARTEFACTID = ?";
                        PreparedStatement updateArtCurStPowerStatusPreparedStatement = udbConnect.prepareStatement(UPDATE_ArtCurSt_POWER_STATUS);

                        log.info("POWER_STATUS preparedStatement SQL UPDATE VALUES");
                        // set values to update
                        i = 1;
                        updateArtCurStPowerStatusPreparedStatement.setLong(i++, Integer.parseInt(_SET_POWER_STATUS)); // POWER_STATUS
                        updateArtCurStPowerStatusPreparedStatement.setLong(i++, createdArtefactId); // ARTEFACTID
                        updateArtCurStPowerStatusPreparedStatement.executeUpdate();
                        log.info("successfull POWER_STATUS updated!");
                    }
                    if (_SET_FE_STATUS != null && !_SET_FE_STATUS.equals("")) {
                        log.info("_SET_FE_STATUS");
                        log.info(_SET_FE_STATUS);
                        log.info("_SET_FE_STATUS_NCP");
                        log.info(_SET_FE_STATUS_NCP);

                        //UPDATE NCP
                        String UPDATE_ArtCurSt_FE_STATUS = "update ARTEFACT_CURRENT_STATE set TR_STATUS = ?, TR_STATUS_DATE = ?, fe_status = ?, fe_status_date = ? where ARTEFACTID = ?";
                        PreparedStatement updateArtCurStFeStatusPreparedStatement = udbConnect.prepareStatement(UPDATE_ArtCurSt_FE_STATUS);

                        log.info("FE_STATUS ARTEFACT_CURRENT_STATE table preparedStatement SQL UPDATE VALUES");
                        // set values to update
                        i = 1;
                        updateArtCurStFeStatusPreparedStatement.setLong(i++, Integer.parseInt(_SET_FE_STATUS)); // POWER_STATUS
                        updateArtCurStFeStatusPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // CAND_STATUS_DATE
                        updateArtCurStFeStatusPreparedStatement.setLong(i++, Integer.parseInt(_SET_FE_STATUS)); // POWER_STATUS
                        updateArtCurStFeStatusPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // CAND_STATUS_DATE
                        updateArtCurStFeStatusPreparedStatement.setLong(i++, createdArtefactId); // ARTEFACTID
                        updateArtCurStFeStatusPreparedStatement.executeUpdate();

                        String UPDATE_NCP_CREATION_TR_STATUS = "update NCP_CREATION set TR_STATUS = ? where NCPID = ?";
                        PreparedStatement updateNcpCreationFeStatusPreparedStatement = udbConnect.prepareStatement(UPDATE_NCP_CREATION_TR_STATUS);

                        log.info("FE_STATUS NCP_CREATION table preparedStatement SQL UPDATE VALUES");
                        // set values to update
                        i = 1;
                        updateNcpCreationFeStatusPreparedStatement.setLong(i++, Integer.parseInt(_SET_FE_STATUS_NCP)); // TR_STATUS
                        updateNcpCreationFeStatusPreparedStatement.setString(i++, ncpId); // NCPID
                        updateNcpCreationFeStatusPreparedStatement.executeUpdate();

                        //UPDATE NCP
                        String UPDATE_ArtTsdExt_FE_STATUS = "update ARTEFACT_TSD_EXT set TSD_STATUS = ? where ARTEFACTID = ?";
                        PreparedStatement updateArtTsdStFeStatusPreparedStatement = udbConnect.prepareStatement(UPDATE_ArtTsdExt_FE_STATUS);

                        log.info("FE_STATUS ARTEFACT_TSD_EXT preparedStatement SQL UPDATE VALUES");
                        // set values to update
                        i = 1;
                        updateArtTsdStFeStatusPreparedStatement.setLong(i++, Integer.parseInt(_SET_FE_STATUS)); // TSD_STATUS
                        updateArtTsdStFeStatusPreparedStatement.setLong(i++, createdArtefactId); // ARTEFACTID
                        updateArtTsdStFeStatusPreparedStatement.executeUpdate();

                        log.info("successfull FE_STATUS updated!");

                    }

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
