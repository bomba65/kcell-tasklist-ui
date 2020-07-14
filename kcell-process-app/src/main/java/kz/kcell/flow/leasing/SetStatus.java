package kz.kcell.flow.leasing;

import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Date;
import java.util.TimeZone;

@Log
@Service("SetStatus")
public class SetStatus implements JavaDelegate {

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
                    log.info("SET STATUS Connected to the database!");

                    // proc vars
                    int cn_rbs_location = 2;
                    int cn_altitude = 689;
                    //generated vars
                    Long ncpCreatedId = (Long) delegateExecution.getVariable("ncpCreatedId");
//                    Long createdNcpStatusId = (Long) delegateExecution.getVariable("createdNcpStatusId");
                    Long createdArtefactId = (Long) delegateExecution.getVariable("createdArtefactId");
                    Long createdArtefactRSDId = (Long) delegateExecution.getVariable("createdArtefactRSDId");
                    Long createdArtefactRRId = (Long) delegateExecution.getVariable("createdArtefactRRId");
//                    Long createdArtefactVSDId = (Long) delegateExecution.getVariable("createdArtefactVSDId");
                    Long createdCandApprovalId = (Long) delegateExecution.getVariable("createdCandApprovalId");
//                    Long createdArtefactRRStatusId = (Long) delegateExecution.getVariable("createdArtefactRRStatusId");

                    String ncpId = delegateExecution.getVariable("ncpID").toString();
                    String starter = delegateExecution.getVariable("starter").toString();
                    String _SET_NCP_STATUS = delegateExecution.hasVariableLocal("_SET_NCP_STATUS") ? delegateExecution.getVariableLocal("_SET_NCP_STATUS").toString() : "";
                    String _SET_GEN_STATUS = delegateExecution.hasVariableLocal("_SET_GEN_STATUS") ? delegateExecution.getVariableLocal("_SET_GEN_STATUS").toString() : "";
                    String _SET_RR_STATUS = delegateExecution.hasVariableLocal("_SET_RR_STATUS") ? delegateExecution.getVariableLocal("_SET_RR_STATUS").toString() : "";
                    String _SET_POWER_STATUS = delegateExecution.hasVariableLocal("_SET_POWER_STATUS") ? delegateExecution.getVariableLocal("_SET_POWER_STATUS").toString() : "";
                    String _SET_INST_STATUS = delegateExecution.hasVariableLocal("_SET_INST_STATUS") ? delegateExecution.getVariableLocal("_SET_INST_STATUS").toString() : "";
                    String _SET_FE_STATUS = delegateExecution.hasVariableLocal("_SET_FE_STATUS") ? delegateExecution.getVariableLocal("_SET_FE_STATUS").toString() : "";
                    String _SET_FE_STATUS_NCP = delegateExecution.hasVariableLocal("_SET_FE_STATUS_NCP") ? delegateExecution.getVariableLocal("_SET_FE_STATUS_NCP").toString() : _SET_FE_STATUS;
                    String _SET_CAND_STATUS = delegateExecution.hasVariableLocal("_SET_CAND_STATUS") ? delegateExecution.getVariableLocal("_SET_CAND_STATUS").toString() : "";
                    String _SET_RSD_STATUS = delegateExecution.hasVariableLocal("_SET_RSD_STATUS") ? delegateExecution.getVariableLocal("_SET_RSD_STATUS").toString() : "";
                    String _SET_ARTEFACT_RSD_EXIST = delegateExecution.hasVariableLocal("_SET_ARTEFACT_RSD_EXIST") ? delegateExecution.getVariableLocal("_SET_ARTEFACT_RSD_EXIST").toString() : "";
                    String _SET_TR_STATUS = delegateExecution.hasVariableLocal("_SET_TR_STATUS") ? delegateExecution.getVariableLocal("_SET_TR_STATUS").toString() : "";
                    String _TEMP_NOT_USE_LIST = delegateExecution.hasVariableLocal("_TEMP_NOT_USE_LIST") ? delegateExecution.getVariableLocal("_TEMP_NOT_USE_LIST").toString() : "";

                    log.info("ncpId");
                    log.info(ncpId);
                    int i;

                    if (_SET_NCP_STATUS != null && !_SET_NCP_STATUS.equals("") && !_TEMP_NOT_USE_LIST.contains("_SET_NCP_STATUS")) {
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
                    if (_SET_GEN_STATUS != null && !_SET_GEN_STATUS.equals("") && !_TEMP_NOT_USE_LIST.contains("_SET_GEN_STATUS")) {
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
                    if (_SET_RSD_STATUS != null && !_SET_RSD_STATUS.equals("") && !_TEMP_NOT_USE_LIST.contains("_SET_RSD_STATUS")) {
                        log.info("_SET_RSD_STATUS");
                        log.info(_SET_RSD_STATUS);
                        //UPDATE NCP
                        String UPDATE_RSD_STATUS = "update ARTEFACT_CURRENT_STATE set RSD_EXIST = ? where ARTEFACTID = ?";
                        PreparedStatement updateRSDstatusInArtefactPreparedStatement = udbConnect.prepareStatement(UPDATE_RSD_STATUS);

                        log.info("RSD_STATUS preparedStatement SQL UPDATE VALUES");
                        // set values to update
                        i = 1;
                        updateRSDstatusInArtefactPreparedStatement.setLong(i++, Integer.parseInt(_SET_RSD_STATUS)); // GS_STATUS
                        updateRSDstatusInArtefactPreparedStatement.setLong(i++, createdArtefactId); // ARTEFACTID
                        updateRSDstatusInArtefactPreparedStatement.executeUpdate();
                        log.info("successfull RSD_STATUS updated!");
                    }
                    if (_SET_ARTEFACT_RSD_EXIST != null && !_SET_ARTEFACT_RSD_EXIST.equals("") && !_TEMP_NOT_USE_LIST.contains("_SET_ARTEFACT_RSD_EXIST")) {
                        log.info("_SET_ARTEFACT_RSD_EXIST");
                        log.info(_SET_ARTEFACT_RSD_EXIST);
                        //UPDATE NCP
                        String UPDATE_ARTEFACT_RSD_STATUS = "update ARTEFACT_RSD_EXIST set RSD_EXIST = ? where ARTEFACTID = ?";
                        PreparedStatement updateARTEFACT_RSDstatusInArtefactPreparedStatement = udbConnect.prepareStatement(UPDATE_ARTEFACT_RSD_STATUS);

                        log.info("ARTEFACT_RSD_EXIST STATUS preparedStatement SQL UPDATE VALUES");
                        // set values to update
                        i = 1;
                        updateARTEFACT_RSDstatusInArtefactPreparedStatement.setLong(i++, Integer.parseInt(_SET_ARTEFACT_RSD_EXIST)); // RSD_EXIST
                        updateARTEFACT_RSDstatusInArtefactPreparedStatement.setLong(i++, createdArtefactId); // ARTEFACTID
                        updateARTEFACT_RSDstatusInArtefactPreparedStatement.executeUpdate();
                        log.info("successfull ARTEFACT_RSD_EXIST STATUS updated!");
                    }
                    if (_SET_TR_STATUS != null && !_SET_TR_STATUS.equals("") && !_TEMP_NOT_USE_LIST.contains("_SET_TR_STATUS")) {
                        log.info("_SET_TR_STATUS");
                        log.info(_SET_TR_STATUS);
                        //UPDATE NCP
                        String UPDATE_NCP_CREATION = "update NCP_CREATION set TR_STATUS = ? where ARTEFACTID = ?";
                        PreparedStatement updateTrStatusInNcpCreationPreparedStatement = udbConnect.prepareStatement(UPDATE_NCP_CREATION);

                        log.info("TR STATUS preparedStatement SQL UPDATE VALUES");
                        // set values to update
                        i = 1;
                        updateTrStatusInNcpCreationPreparedStatement.setLong(i++, Integer.parseInt(_SET_TR_STATUS)); // TR_STATUS
                        updateTrStatusInNcpCreationPreparedStatement.setLong(i++, ncpCreatedId); // ARTEFACTID
                        updateTrStatusInNcpCreationPreparedStatement.executeUpdate();
                        log.info("successfull TR STATUS updated!");
                    }
                    if (_SET_RR_STATUS != null && !_SET_RR_STATUS.equals("") && !_TEMP_NOT_USE_LIST.contains("_SET_RR_STATUS")) {
                        log.info("_SET_RR_STATUS");
                        log.info(_SET_RR_STATUS);
                        //UPDATE NCP
//                        String UPDATE_ArtRRst_CREATION_RR_STATUS = "update ARTEFACT_RR_STATUS set RR_STATUS_ID = ?, DATEOFPERFORM = ? where ID = ?";
                        String UPDATE_ARTEFACT_RR_STATUS = "update ARTEFACT_CURRENT_STATE set RR_STATUS = ?, RR_STATUS_DATE = ? where ARTEFACTID = ?";
//                        PreparedStatement updateRRstatusInArtRRstPreparedStatement = udbConnect.prepareStatement(UPDATE_ArtRRst_CREATION_RR_STATUS);
                        PreparedStatement updateRRstatusInArtefactPreparedStatement = udbConnect.prepareStatement(UPDATE_ARTEFACT_RR_STATUS);

                        log.info("RR_STATUS preparedStatement SQL UPDATE VALUES");
                        // set values to update
//                        i = 1;
//                        updateRRstatusInArtRRstPreparedStatement.setLong(i++, Integer.parseInt(_SET_RR_STATUS)); // RR_STATUS
//                        updateRRstatusInArtRRstPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // DATEOFPERFORM
//                        updateRRstatusInArtRRstPreparedStatement.setLong(i++, createdArtefactRRStatusId); // Artefact RRStatus ID
//                        updateRRstatusInArtRRstPreparedStatement.executeUpdate();
                        i = 1;
                        updateRRstatusInArtefactPreparedStatement.setLong(i++, Integer.parseInt(_SET_RR_STATUS)); // GS_STATUS
                        updateRRstatusInArtefactPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // RR_STATUS_DATE
                        updateRRstatusInArtefactPreparedStatement.setLong(i++, createdArtefactId); // ARTEFACTID
                        updateRRstatusInArtefactPreparedStatement.executeUpdate();


//                      insert ARTEFACT_RR_STATUS
                        String insertNewArtefactRRStatus = "insert into ARTEFACT_RR_STATUS(ID, ARTEFACTID, RR_ID, RR_STATUS_ID,  DATEOFPERFORM) values (ARTEFACT_RR_STATUS_SEQ.nextval, ?, ?, ?, SYSDATE)";
                        PreparedStatement newArtefactRRStatusPreparedStatement = udbConnect.prepareStatement(insertNewArtefactRRStatus);

                        i = 1;
                        log.info("newArtefactRRStatusPreparedStatement.setString");
                        if (createdArtefactId != null) {
                            newArtefactRRStatusPreparedStatement.setLong(i++, createdArtefactId);
                        } else {
                            newArtefactRRStatusPreparedStatement.setNull(i++, Types.BIGINT);
                        }
                        if (createdArtefactRRId != null) {
                            newArtefactRRStatusPreparedStatement.setLong(i++, createdArtefactRRId);
                        } else {
                            newArtefactRRStatusPreparedStatement.setNull(i++, Types.BIGINT);
                        }
                        newArtefactRRStatusPreparedStatement.setLong(i++, Integer.parseInt(_SET_RR_STATUS)); // RR_STATUS
    //                    newArtefactRRStatusPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime()));
                        log.info("newArtefactRRStatusPreparedStatement.executeUpdate()");
                        newArtefactRRStatusPreparedStatement.executeUpdate();
                        log.info("successfull insert to database!");

                        log.info("ArtefactID: " + createdArtefactId + " successfully RR_STATUS updated to " + _SET_RR_STATUS);
                    }
                    if (_SET_CAND_STATUS != null && !_SET_CAND_STATUS.equals("") && !_TEMP_NOT_USE_LIST.contains("_SET_CAND_STATUS")) {
                        log.info("_SET_CAND_STATUS");
                        log.info(_SET_CAND_STATUS);
                        //UPDATE NCP
//                        String UPDATE_Cand_Apr__CAND_STATUS = "update CANDAPPROVAL set STATUSID = ?, DESDATE = ? where ARTEFACTID = ?";
                        String INSERT_Cand_Apr__CAND_STATUS = "insert into CANDAPPROVAL(ID, ARTEFACTID, RSDID, STATUSID, DESDATE, APPROVER) values (CANDAPPROVAL_SEQ.nextval, ?, ?, ?, SYSDATE, ?)";
                        String UPDATE_ArtCurSt__CAND_STATUS = "update ARTEFACT_CURRENT_STATE set CAND_STATUS = ?, CAND_STATUS_DATE = ? where ARTEFACTID = ?";
                        PreparedStatement updateCand_AprCandStatusPreparedStatement = udbConnect.prepareStatement(INSERT_Cand_Apr__CAND_STATUS);
                        PreparedStatement updateCandStatusInCandAprPreparedStatement = udbConnect.prepareStatement(UPDATE_ArtCurSt__CAND_STATUS);

                        log.info("CAND_STATUS preparedStatement SQL UPDATE VALUES");
                        // set values to update
                        i = 1;

                        updateCand_AprCandStatusPreparedStatement.setLong(i++, createdArtefactId); // ARTEFACTID
                        updateCand_AprCandStatusPreparedStatement.setLong(i++, createdArtefactRSDId); // RSDID
                        updateCand_AprCandStatusPreparedStatement.setLong(i++, Integer.parseInt(_SET_CAND_STATUS)); // STATUSID
                        updateCand_AprCandStatusPreparedStatement.setString(i++, starter); // ARTEFACTID
                        updateCand_AprCandStatusPreparedStatement.executeUpdate();
                        i = 1;
                        updateCandStatusInCandAprPreparedStatement.setLong(i++, Integer.parseInt(_SET_CAND_STATUS)); // CAND_STATUS
                        updateCandStatusInCandAprPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // CAND_STATUS_DATE
                        updateCandStatusInCandAprPreparedStatement.setLong(i++, createdArtefactId); // ARTEFACTID
                        updateCandStatusInCandAprPreparedStatement.executeUpdate();
                        log.info("successfull CAND_STATUS updated!");
                    }
                    if (_SET_INST_STATUS != null && !_SET_INST_STATUS.equals("") && !_TEMP_NOT_USE_LIST.contains("_SET_INST_STATUS")) {
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
                    if (_SET_POWER_STATUS != null && !_SET_POWER_STATUS.equals("") && !_TEMP_NOT_USE_LIST.contains("_SET_POWER_STATUS")) {
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
                    if (_SET_FE_STATUS != null && !_SET_FE_STATUS.equals("") && !_TEMP_NOT_USE_LIST.contains("_SET_FE_STATUS")) {
                        log.info("_SET_FE_STATUS");
                        log.info(_SET_FE_STATUS);
                        log.info("_SET_FE_STATUS_NCP");
                        log.info(_SET_FE_STATUS_NCP);

                        //UPDATE NCP
                        String UPDATE_ArtCurSt_FE_STATUS = "update ARTEFACT_CURRENT_STATE set fe_status = ?, fe_status_date = ? where ARTEFACTID = ?";
                        PreparedStatement updateArtCurStFeStatusPreparedStatement = udbConnect.prepareStatement(UPDATE_ArtCurSt_FE_STATUS);

                        log.info("FE_STATUS ARTEFACT_CURRENT_STATE table preparedStatement SQL UPDATE VALUES");
                        // set values to update
                        i = 1;
                        updateArtCurStFeStatusPreparedStatement.setLong(i++, Integer.parseInt(_SET_FE_STATUS)); // fe_status
                        updateArtCurStFeStatusPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // fe_status_date
                        updateArtCurStFeStatusPreparedStatement.setLong(i++, createdArtefactId); // ARTEFACTID
                        updateArtCurStFeStatusPreparedStatement.executeUpdate();

                        String INSERT_ARTEFACT_TSD_LEASING_ACTION = "INSERT INTO ARTEFACT_TSD_LEASING_ACTION (LEASING_STATUS_ACTION_ID, ARTEFACTID, LEASING_STATUS_ID, INSERTDATE) VALUES (ARTEFACT_TSD_LEASING_ACTI_SEQ.nextval, ?, ?, SYSDATE)";
                        PreparedStatement insertArtefactTsdLeasingActionPreparedStatement = udbConnect.prepareStatement(INSERT_ARTEFACT_TSD_LEASING_ACTION);

                        log.info("FE_STATUS NCP_CREATION table preparedStatement SQL UPDATE VALUES");
                        // set values to update
                        i = 1;
                        insertArtefactTsdLeasingActionPreparedStatement.setLong(i++, createdArtefactId); // ARTEFACTID
                        insertArtefactTsdLeasingActionPreparedStatement.setLong(i++, Integer.parseInt(_SET_FE_STATUS)); // LEASING_STATUS_ID
                        insertArtefactTsdLeasingActionPreparedStatement.executeUpdate();

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
