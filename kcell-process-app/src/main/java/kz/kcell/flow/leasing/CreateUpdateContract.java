package kz.kcell.flow.leasing;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.SpinValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.camunda.spin.Spin.JSON;
import lombok.extern.java.Log;

@Log
@Service("CreateUpdateContract")
public class CreateUpdateContract implements JavaDelegate {

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

                    String _CONTRACT_APPROVAL_TYPE = delegateExecution.hasVariableLocal("_CONTRACT_APPROVAL_TYPE") ? delegateExecution.getVariable("_CONTRACT_APPROVAL_TYPE").toString() : null;

                    String starter = delegateExecution.getVariable("starter").toString();
                    Long createdArtefactId = (Long) delegateExecution.getVariable("createdArtefactId");

                    // check FE or CN: _CONTRACT_APPROVAL_TYPE.equals("CN")
                    String contractVariableName =  _CONTRACT_APPROVAL_TYPE.equals("CN")  ? "contractInformations" : "contractInformationsFE";

                    SpinJsonNode contractInformationsJSON = JSON(delegateExecution.getVariable(contractVariableName));
                    SpinList contractInformations = contractInformationsJSON.elements();
                    String contractid;
                    String ct_contractid_old = "";
                    int ct_vendor_sap = 0;
                    int ct_agreement_type = 0;
                    JSONArray notCreatedContractArtefacts = new JSONArray();
                    JSONObject notCreatedContractArtefact = new JSONObject();

                    for (int j=0; j<contractInformations.size(); j++) {
                        SpinJsonNode ci = (SpinJsonNode) contractInformations.get(j);
                        if (ci != null) {

                            Number fe_artefact_id = 0;

                            contractid = ci.hasProp("ct_contractid") ? ci.prop("ct_contractid").value().toString() : "";
                            String ct_acquisitionType = ci.prop("ct_acquisitionType").stringValue();
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); //2020-01-02T18:00:00.000Z
                            String ct_acceptance_act_date = ci.prop("ct_acceptance_act_date").stringValue().substring(0,10);
                            String ct_contract_start_date = ci.prop("ct_contract_start_date").stringValue().substring(0,10);
                            String ct_contract_end_date = ci.prop("ct_contract_end_date").stringValue().substring(0,10);

                            if(_CONTRACT_APPROVAL_TYPE.equals("FE")){
                                String ct_fe_sitename = ci.prop("ct_sitename").value().toString();
                                //find artefact_id by ct_fe_sitename

                                String SelectArtefactBySite = "select * from ARTEFACT where SITENAME = ?";
                                PreparedStatement selectArtefactBySitePreparedStatement = udbConnect.prepareStatement(SelectArtefactBySite);
                                int i = 1;
                                log.info("get artefact_id by ct_fe_sitename...");
                                log.info(ct_fe_sitename);
                                selectArtefactBySitePreparedStatement.setString(i++, ct_fe_sitename); // sitename
                                ResultSet resultSet = selectArtefactBySitePreparedStatement.executeQuery();

                                if (resultSet.next() == false ) {
                                    log.info("not Found");
                                    if (!ct_acquisitionType.equals("additionalAgreement")){
                                        notCreatedContractArtefact.put("contractid", contractid);
                                        notCreatedContractArtefact.put("ct_sitename", ct_fe_sitename);
                                        notCreatedContractArtefact.put("ct_acquisitionType", ct_acquisitionType);
                                        notCreatedContractArtefacts.put(notCreatedContractArtefact);
                                    }
                                } else {
                                    fe_artefact_id = resultSet.getInt("ARTEFACTID");
                                }
                                log.info("fe_artefact_id:");
                                log.info(fe_artefact_id.toString());

                            }



                            Date formated_ct_acceptance_act_date = formatter.parse(ct_acceptance_act_date);
                            Date formated_ct_contract_start_date = formatter.parse(ct_contract_start_date);
                            Date formated_ct_contract_end_date = formatter.parse(ct_contract_end_date);

                            Number ct_rent = ci.prop("ct_rent").numberValue();
                            Number ct_rent_all = ci.prop("ct_rent_all").numberValue();
                            Number ct_rent_area = ci.prop("ct_rent_area").numberValue();
                            String ct_bin = ci.prop("ct_bin").numberValue().toString();
                            String ct_iban = ci.prop("ct_iban").stringValue();
                            String ct_legal_name = ci.prop("ct_legal_name").stringValue();
                            Number ct_rent_power = ci.hasProp("ct_rent_power") && !ci.prop("ct_rent_power").isNull() ? ci.prop("ct_rent_power").numberValue() : 0; //notReq
//                            Number legalType = ci.prop("legalType").numberValue();
                            Number legalType = 0;

                            Number ct_contract_type = 0;
                            Number ct_executor = 0;
                            Number ct_payment_period = 0;
                            Number ct_currency_type = 0;
                            Number ct_bank_id = 0;
                            Number ct_legal_type = 0;

                            String ci_address = "" + (ci.prop("address").hasProp("cn_addr_oblast") ? ci.prop("address").prop("cn_addr_oblast").stringValue() : "") +
                                (ci.prop("address").hasProp("cn_addr_district") ? ", " + ci.prop("address").prop("cn_addr_district").stringValue() : "") +
                                (ci.prop("address").hasProp("cn_addr_city") ? ", " + ci.prop("address").prop("cn_addr_city").stringValue() : "") +
                                (ci.prop("address").hasProp("cn_addr_street") ? ", " + ci.prop("address").prop("cn_addr_street").stringValue() : "") +
                                (ci.prop("address").hasProp("cn_addr_building") ? ", " + ci.prop("address").prop("cn_addr_building").stringValue() : "") +
                                (ci.prop("address").hasProp("cn_addr_cadastral_number") ? ", " + ci.prop("address").prop("cn_addr_cadastral_number").stringValue() : "") +
                                (ci.prop("address").hasProp("cn_addr_note") ? ", " + ci.prop("address").prop("cn_addr_note").stringValue() : "");

                            String ct_contact_person = ci.hasProp("ct_contact_person") ? ci.prop("ct_contact_person").stringValue() : ""; // notReq
                            String ct_contact_phone = ci.prop("ct_contact_phone").stringValue();
                            String ct_access_status = ci.prop("ct_access_status").value().toString();
                            String ct_autoprolongation = ci.prop("ct_autoprolongation").stringValue();
//                            String ct_contract_sap = ci.prop("ct_contract_sap").stringValue();
                            Long ct_contract_sap = ci.prop("ct_contract_sap").numberValue().longValue();

                            ct_contract_type = Integer.parseInt(ci.hasProp("ct_contract_type") ? ci.prop("ct_contract_type").value().toString() : "0");
                            ct_payment_period = Integer.parseInt(ci.hasProp("ct_payment_period") ? ci.prop("ct_payment_period").value().toString() : "0");
                            ct_currency_type = Integer.parseInt(ci.hasProp("ct_currency_type") ? ci.prop("ct_currency_type").value().toString() : "0");
                            ct_executor = Integer.parseInt(ci.hasProp("ct_executor") ? ci.prop("ct_executor").value().toString() : "0");
                            if (ci.hasProp("ct_bankname")) {
                                String bankidString = ci.prop("ct_bankname").hasProp("id") ? ci.prop("ct_bankname").prop("id").stringValue() : "0";
                                ct_bank_id = Integer.parseInt(bankidString);
                            }

                            if (ci.hasProp("legalType")) {
                                log.info("ct_legal_type:");
//                                log.info(ci.prop("legalType").value().toString());
//                                ct_bank_id = Integer.parseInt(ci.hasProp("ct_bankname") ? ci.prop("ct_bankname").stringValue() : "0");
                                String legalTypeString = ci.prop("legalType").hasProp("udbid") ? ci.prop("legalType").prop("udbid").value().toString() : "0";
                                ct_legal_type = Integer.parseInt(legalTypeString);
//                                log.info("----legalTypeString:");
//                                log.info(legalTypeString);
                                log.info(ct_legal_type.toString());
//                                log.info("END ct_legal_type");
                            }

//                            log.info("legalType:");
                            
//                            legalType = Integer.parseInt(ci.hasProp("legalType") && ci.prop("legalType").hasProp("udbid") ? ci.prop("legalType").prop("udbid").stringValue() : "0");
//                            if (ci.hasProp("legalType")) {
//                                log.info("ci has prop legalType");
//                                log.info(ci.prop("legalType").value().toString());
//                                SpinJsonNode ltJson = ci.prop("legalType");
//                                String ltId = ltJson.hasProp("udbid") ? ltJson.prop("udbid").value().toString() : " ";
//                                log.info("ltJson.hasProp(\"udbid\"):");
//                                log.info(ltJson.hasProp("udbid"));
//                                log.info("ltId:");
//                                log.info(ltId);
//
//                                if (ci.prop("legalType").hasProp("udbid")) {
//                                    log.info("legalType has prop udbid");
//                                    log.info(ci.prop("legalType").prop("udbid").value().toString());
//                                    legalType = Integer.parseInt(ci.prop("legalType").prop("udbid").stringValue());
//                                }
//                            }

//                            log.info(ci.hasProp("legalType") && ci.prop("legalType").hasProp("udbid") ? ci.prop("legalType").prop("udbid").stringValue() : "0");
//                            log.info(legalType);
//                            log.info("end legalType:");

                            String ct_checkvat = ci.prop("ct_checkvat").stringValue() == "No" ? "no" : "yes";
                            Long ct_cid = new Long(1);
                            int i = 1;

                            if (!ct_acquisitionType.equals("newContract")) {
                                log.info("OLD CONTRACT....");
                                ct_cid = ci.prop("ct_cid").numberValue().longValue();

                                ct_contractid_old = ci.prop("ct_contractid_old").stringValue();
                                ct_vendor_sap = ci.prop("ct_vendor_sap").numberValue().intValue();
//                                ct_agreement_type = ci.prop("ct_agreement_type").numberValue().intValue();


//                                if (ct_acquisitionType.equals("additionalAgreement")) {
//                                    ct_agreement_type = Integer.parseInt(ci.hasProp("ct_agreement_type") && !ci.prop("ct_agreement_type").value().equals(null) ? ci.prop("ct_agreement_type").stringValue() : "0");
//                                    String ct_agreement_reason= ci.hasProp("ct_agreement_type") && !ci.prop("ct_agreement_type").value().equals(null) ? ci.prop("ct_agreement_type").stringValue() : "";
//                                    Number ct_agreement_number = ci.hasProp("ct_agreement_number") && !ci.prop("ct_agreement_number").value().equals(null) ? ci.prop("ct_agreement_number").numberValue() : 0;
//                                    String ct_aa_date = ci.prop("ct_aa_date").stringValue().substring(0,9);
//                                    Date formated_ct_aa_date = formatter.parse(ct_aa_date);
//                                    Number ct_agreement_executor = Integer.parseInt(ci.hasProp("ct_agreement_executor") && !ci.prop("ct_agreement_executor").value().equals(null) ? ci.prop("ct_agreement_executor").stringValue() : "0");
//                                }

                                //UPDATE NCP
                                // && ((fe_artefact_id.longValue() > 0 && _CONTRACT_APPROVAL_TYPE.equals("FE")) || _CONTRACT_APPROVAL_TYPE.equals("CN"))

                                if (ct_acquisitionType.equals("existingContract") && ((fe_artefact_id.longValue() > 0 && _CONTRACT_APPROVAL_TYPE.equals("FE")) || _CONTRACT_APPROVAL_TYPE.equals("CN"))) {
                                    String UPDATE_CONTRACT_STATUS_REL = "update CONTRACT_STATUS_REL set STATUSID = 40, csreldate=? where CONTRACTID = ?";
                                    PreparedStatement UPDATE_CONTRACT_STATUS_RELPreparedStatement = udbConnect.prepareStatement(UPDATE_CONTRACT_STATUS_REL);
                                    log.info("CONTRACT_STATUS_REL preparedStatement SQL UPDATE VALUES....");
                                    // set values to update
                                    i = 1;
                                    UPDATE_CONTRACT_STATUS_RELPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // csreldate
                                    UPDATE_CONTRACT_STATUS_RELPreparedStatement.setLong(i++, ct_cid.longValue() ); //  //ct_cid
                                    UPDATE_CONTRACT_STATUS_RELPreparedStatement.executeUpdate();
                                    log.info("...CONTRACT_STATUS_REL updated");

                                    String UPDATE_CONTRACTS_STATUSES = "update CONTRACTS_STATUSES set LEASING_STATUS = ? where CID = ?";
                                    PreparedStatement UPDATE_CONTRACTS_STATUSESPreparedStatement = udbConnect.prepareStatement(UPDATE_CONTRACTS_STATUSES);
                                    log.info("CONTRACTS_STATUSES preparedStatement SQL UPDATE VALUES....");
                                    // set values to update
                                    i = 1;
                                    UPDATE_CONTRACTS_STATUSESPreparedStatement.setLong(i++, 40); // STATUSID
                                    UPDATE_CONTRACTS_STATUSESPreparedStatement.setLong(i++, ct_cid.longValue() ); //  //ct_cid
                                    UPDATE_CONTRACTS_STATUSESPreparedStatement.executeUpdate();
                                    log.info("...CONTRACTS_STATUSES updated");
                                }
                            } else if (ct_acquisitionType.equals("newContract")) {
                                log.info("NEW CONTRACT....");
//                                contractid = ci.prop("ct_contractid").stringValue();
                                ct_vendor_sap = ci.prop("ct_vendor_sap").numberValue().intValue();
//                                ct_vendor_sap = Integer.parseInt(ci.prop("ct_vendor_sap").stringValue());
                                ct_agreement_type = 0;
                            }

                            i = 1;
                            String returnStatus[] = { "CID" };
                            Long createdContractCID = null;
                            if (!ct_acquisitionType.equals("additionalAgreement") && ((fe_artefact_id.longValue() > 0 && _CONTRACT_APPROVAL_TYPE.equals("FE")) || _CONTRACT_APPROVAL_TYPE.equals("CN"))) {
                                //INSERT_CONTRACTS
                                String INSERT_CONTRACTS = "";
                                if (ct_acquisitionType.equals("existingContract")) {
                                    INSERT_CONTRACTS = "INSERT INTO CONTRACTS (CID, OLD_CID, RENTSUM, RENTAREA, CONTRACTID, INCOMINGDATE, INCOMINGWEEK, CONTRACTTYPE, POWERSUPPLY, LEGALTYPE, LEGALNAME, LEGALADDRESS, CONTACTPERSON, CONTACTPHONE, ACCESS_STATUS, CONTRACT_SAP_NO, VENDOR_SAP_NO, CONTRACT_EXECUTOR, NEEDVAT, PAYMENTPERIOD, PAYMENTWAY, CONTRACTSTARTDATE, CONTRACTENDDATE, AUTOPROLONGATION, USERNAME, AREA_ACT_ACCEPT_DATE, RNN, INN, IBAN, BANK_ID) VALUES (CONTRACTS_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                                } else {
                                    INSERT_CONTRACTS = "INSERT INTO CONTRACTS (CID, RENTSUM, RENTAREA, CONTRACTID, INCOMINGDATE, INCOMINGWEEK, CONTRACTTYPE, POWERSUPPLY, LEGALTYPE, LEGALNAME, LEGALADDRESS, CONTACTPERSON, CONTACTPHONE, ACCESS_STATUS, CONTRACT_SAP_NO, VENDOR_SAP_NO, CONTRACT_EXECUTOR, NEEDVAT, PAYMENTPERIOD, PAYMENTWAY, CONTRACTSTARTDATE, CONTRACTENDDATE, AUTOPROLONGATION, USERNAME, AREA_ACT_ACCEPT_DATE, RNN, INN, IBAN, BANK_ID) VALUES (CONTRACTS_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                                }

                                PreparedStatement INSERT_CONTRACTSPreparedStatement = udbConnect.prepareStatement(INSERT_CONTRACTS, returnStatus);

                                log.info("INSERT_CONTRACTS preparedStatement SQL UPDATE VALUES");
                                // set values to update
                                if (ct_acquisitionType.equals("existingContract")) {
                                    INSERT_CONTRACTSPreparedStatement.setFloat(i++, ct_cid.longValue());  // OLD_CID
                                }
                                INSERT_CONTRACTSPreparedStatement.setFloat(i++, ct_rent_all.floatValue());  // RENTSUM
                                INSERT_CONTRACTSPreparedStatement.setFloat(i++, ct_rent_area.floatValue());  // RENTAREA
                                INSERT_CONTRACTSPreparedStatement.setString(i++, contractid);  // CONTRACTID
                                INSERT_CONTRACTSPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // INCOMINGDATE
                                INSERT_CONTRACTSPreparedStatement.setString(i++, "12");  // INCOMINGWEEK
                                INSERT_CONTRACTSPreparedStatement.setLong(i++, ct_contract_type.longValue());  // CONTRACTTYPE +
                                INSERT_CONTRACTSPreparedStatement.setLong(i++, ct_rent_power.longValue());  // POWERSUPPLY
                                INSERT_CONTRACTSPreparedStatement.setLong(i++, ct_legal_type.longValue());  // LEGALTYPE
                                INSERT_CONTRACTSPreparedStatement.setString(i++, ct_legal_name);  // LEGALNAME
                                INSERT_CONTRACTSPreparedStatement.setString(i++, ci_address);  // LEGALADDRESS
                                INSERT_CONTRACTSPreparedStatement.setString(i++, ct_contact_person);  // CONTACTPERSON
                                INSERT_CONTRACTSPreparedStatement.setString(i++, ct_contact_phone);  // CONTACTPHONE
                                INSERT_CONTRACTSPreparedStatement.setLong(i++, Integer.parseInt(ct_access_status));  // ACCESS_STATUS
                                INSERT_CONTRACTSPreparedStatement.setString(i++, ct_contract_sap.toString());  // CONTRACT_SAP_NO
                                INSERT_CONTRACTSPreparedStatement.setInt(i++,  ct_vendor_sap);  // VENDOR_SAP_NO
                                INSERT_CONTRACTSPreparedStatement.setLong(i++, ct_executor.longValue());  // CONTRACT_EXECUTOR netu ct_executor
                                INSERT_CONTRACTSPreparedStatement.setString(i++, ct_checkvat);  // NEEDVAT
                                INSERT_CONTRACTSPreparedStatement.setLong(i++, ct_payment_period.longValue());  // PAYMENTPERIOD
                                INSERT_CONTRACTSPreparedStatement.setLong(i++, ct_currency_type.longValue());  // PAYMENTWAY
                                INSERT_CONTRACTSPreparedStatement.setDate(i++, new java.sql.Date(formated_ct_contract_start_date.getTime())); // CONTRACTSTARTDATE
                                INSERT_CONTRACTSPreparedStatement.setDate(i++, new java.sql.Date(formated_ct_contract_end_date.getTime())); // CONTRACTENDDATE
                                INSERT_CONTRACTSPreparedStatement.setString(i++, ct_autoprolongation);  // AUTOPROLONGATION
                                INSERT_CONTRACTSPreparedStatement.setString(i++, starter);  // USERNAME
                                INSERT_CONTRACTSPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime()));  // AREA_ACT_ACCEPT_DATE
                                INSERT_CONTRACTSPreparedStatement.setString(i++, ct_bin);  // RNN
                                INSERT_CONTRACTSPreparedStatement.setString(i++, ct_bin);  // INN ??
                                INSERT_CONTRACTSPreparedStatement.setString(i++, ct_iban);  // IBAN
                                INSERT_CONTRACTSPreparedStatement.setLong(i++, ct_bank_id.longValue());  // BANK_ID
                                // bank_id ct_bankname

                                INSERT_CONTRACTSPreparedStatement.executeUpdate();
                                log.info("successfull CONTRACTS created!");

                                ResultSet statusGeneratedIdResultSet = INSERT_CONTRACTSPreparedStatement.getGeneratedKeys();
                                statusGeneratedIdResultSet.next();
                                createdContractCID = statusGeneratedIdResultSet.getLong(1);
                                log.info("createdContractCID:");
                                log.info(createdContractCID.toString());

                                //INSERT_CONTRACT_ARTEFACT

                                Long createdContractArtefactID = null;
                                String returnContractArtefactID[] = { "ID" };
                                String INSERT_CONTRACT_ARTEFACT = "INSERT INTO CONTRACT_ARTEFACT (ID, CID, ARTEFACTID) VALUES (CONTRACT_ARTEFACT_SEQ.nextval, ?, ?)";
                                PreparedStatement InsertContractArtefactPreparedStatement = udbConnect.prepareStatement(INSERT_CONTRACT_ARTEFACT, returnContractArtefactID);

                                i = 1;
                                log.info("INSERT_CONTRACT_ARTEFACT preparedStatement SQL Insert VALUES");
                                // set values to update
                                InsertContractArtefactPreparedStatement.setLong(i++, createdContractCID);  // createdContractCID
                                if (_CONTRACT_APPROVAL_TYPE.equals("FE")) {
                                    InsertContractArtefactPreparedStatement.setLong(i++, fe_artefact_id.longValue());  // fe_artefact_id
                                } else {
                                    InsertContractArtefactPreparedStatement.setLong(i++, createdArtefactId);  // createdArtefactId
                                }


                                InsertContractArtefactPreparedStatement.executeUpdate();
                                log.info("successfull INSERT_CONTRACT_ARTEFACT created!");

                                ResultSet InsertContractArtefactResultSet = InsertContractArtefactPreparedStatement.getGeneratedKeys();
                                InsertContractArtefactResultSet.next();
                                createdContractArtefactID = InsertContractArtefactResultSet.getLong(1);
                                log.info("createdContractArtefactID:");
                                log.info(createdContractArtefactID.toString());


                                //INSERT_CONTRACT_STATUS_REL
                                log.info("INSERT_CONTRACT_STATUS_REL preparedStatement");
                                Long createdContractStatusRelID = null;
                                String returnContractStatusRelID[] = { "CSRELID" };
                                String INSERT_CONTRACT_STATUS_REL = "INSERT INTO CONTRACT_STATUS_REL (csrelid, contractid, statusid, csreldate) VALUES (CONTRACT_STATUS_REL_SEQ.nextval, ?, 41, ?)";
                                PreparedStatement InsertContractStatusRelPreparedStatement = udbConnect.prepareStatement(INSERT_CONTRACT_STATUS_REL, returnContractStatusRelID);

                                i = 1;
                                // set values to update
                                InsertContractStatusRelPreparedStatement.setLong(i++, createdContractCID);  // createdContractCID
                                InsertContractStatusRelPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // csreldate

                                InsertContractStatusRelPreparedStatement.executeUpdate();
                                log.info("successfull INSERT_CONTRACT_STATUS_REL!");

                                ResultSet InsertContractStatusRelResultSet = InsertContractStatusRelPreparedStatement.getGeneratedKeys();
                                InsertContractStatusRelResultSet.next();
                                createdContractStatusRelID = InsertContractStatusRelResultSet.getLong(1);
                                log.info("createdContractStatusRelID:");
                                log.info(createdContractStatusRelID.toString());


                                //INSERT_CONTRACT_STATUSES
                                String INSERT_CONTRACT_STATUSES = "INSERT INTO CONTRACTS_STATUSES (cid, leasing_status) VALUES (?, 41)";
                                PreparedStatement InsertContractStatusesPreparedStatement = udbConnect.prepareStatement(INSERT_CONTRACT_STATUSES);

                                i = 1;
                                log.info("INSERT_CONTRACT_STATUSES preparedStatement SQL UPDATE VALUES");
                                // set values to update
                                InsertContractStatusesPreparedStatement.setLong(i++, createdContractCID);  // createdContractCID

                                InsertContractStatusesPreparedStatement.executeUpdate();
                                log.info("successfull INSERT_CONTRACT_STATUSES updated!");
                            } else if (ct_acquisitionType.equals("additionalAgreement")){
                                ct_contract_type = Integer.parseInt(ci.hasProp("ct_contract_type") ? ci.prop("ct_contract_type").value().toString() : "0");

                                ct_agreement_type = Integer.parseInt(ci.hasProp("ct_agreement_type") && !ci.prop("ct_agreement_type").value().equals(null) ? ci.prop("ct_agreement_type").value().toString() : "0");
                                String ct_agreement_reason= ci.hasProp("ct_agreement_type") && !ci.prop("ct_agreement_type").value().equals(null) ? ci.prop("ct_agreement_type").stringValue() : "";
                                String ct_agreement_number= ci.hasProp("ct_agreement_number") && !ci.prop("ct_agreement_number").value().equals(null) ? ci.prop("ct_agreement_number").stringValue() : "";
//                                Number ct_agreement_number = ci.hasProp("ct_agreement_number") && !ci.prop("ct_agreement_number").value().equals(null) ? ci.prop("ct_agreement_number").numberValue() : 0;

                                Date formated_ct_aa_date = new Date();
                                String ct_aa_date = "";

                                if (ci.hasProp("ct_aa_date") && !ci.prop("ct_aa_date").isNull()) {
                                    ct_aa_date = ci.prop("ct_aa_date").stringValue().substring(0,10);
                                    formated_ct_aa_date = formatter.parse(ct_aa_date);
                                }

                                Number ct_agreement_executor = Integer.parseInt(ci.hasProp("ct_agreement_executor") && !ci.prop("ct_agreement_executor").value().equals(null) ? ci.prop("ct_agreement_executor").value().toString() : "0");

                                Long createdContractAA = null;
                                String createdContractAAID[] = { "AAID" };
                                String INSERT_CONTRACT_AA = "INSERT INTO CONTRACT_AA (AAID, CID, AA_NUMBER, AA_DATE, AA_EXECUTOR, ARTEFACTID, AA_TYPE, AA_REASON, CONTRACT_TYPE, RENTSUM, RENT_AREA, POWERSUPPLY, LEGAL_TYPE, LEGAL_NAME, LEGAL_ADDRESS, CONTACT_PERSON, CONTACT_PHONE, PAYMENT_PERIOD, PAYMENT_WAY, NEEDVAT, CONTRACT_START_DATE, CONTRACT_END_DATE, AUTOPROLONGATION, AA_STATUS, INSERT_DATE, INSERT_PERSON) VALUES (CONTRACT_AA_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                                PreparedStatement INSERT_CONTRACT_AA_PreparedStatement = udbConnect.prepareStatement(INSERT_CONTRACT_AA, createdContractAAID);

                                i = 1;
                                log.info("INSERT_CONTRACT_AA preparedStatement SQL UPDATE VALUES");
                                // set values to update

                                INSERT_CONTRACT_AA_PreparedStatement.setLong(i++, ct_cid); // CID
                                INSERT_CONTRACT_AA_PreparedStatement.setString(i++, ct_agreement_number); // AA_NUMBER
                                if (!ct_aa_date.equals("")) {
                                    INSERT_CONTRACT_AA_PreparedStatement.setDate(i++, new java.sql.Date(formated_ct_aa_date.getTime())); // AA_DATE
                                } else {
                                    INSERT_CONTRACT_AA_PreparedStatement.setNull (i++, Types.TIMESTAMP); // AA_DATE
                                }
                                //&& ((fe_artefact_id.longValue() > 0 && _CONTRACT_APPROVAL_TYPE.equals("FE")) || _CONTRACT_APPROVAL_TYPE.equals("CN"))
                                INSERT_CONTRACT_AA_PreparedStatement.setLong(i++, ct_agreement_executor.longValue()); // AA_EXECUTOR
                                if ((fe_artefact_id.longValue() > 0 && _CONTRACT_APPROVAL_TYPE.equals("FE")) || _CONTRACT_APPROVAL_TYPE.equals("CN")){
                                    INSERT_CONTRACT_AA_PreparedStatement.setLong(i++, _CONTRACT_APPROVAL_TYPE.equals("CN") ? createdArtefactId : fe_artefact_id.longValue()); // ARTEFACTID
                                } else {
                                    INSERT_CONTRACT_AA_PreparedStatement.setNull(i++, Types.INTEGER); //setLong(i++, _CONTRACT_APPROVAL_TYPE.equals("CN") ? createdArtefactId : fe_artefact_id.longValue()); // ARTEFACTID
                                }

                                INSERT_CONTRACT_AA_PreparedStatement.setLong(i++, ct_agreement_type); // AA_TYPE
                                INSERT_CONTRACT_AA_PreparedStatement.setLong(i++, 0); // AA_REASON // not needed
                                INSERT_CONTRACT_AA_PreparedStatement.setLong(i++, ct_contract_type.longValue()); // CONTRACT_TYPE
                                INSERT_CONTRACT_AA_PreparedStatement.setLong(i++, ct_rent.longValue()); // RENTSUM
                                INSERT_CONTRACT_AA_PreparedStatement.setFloat(i++, ct_rent_area.floatValue());  // RENTAREA
                                INSERT_CONTRACT_AA_PreparedStatement.setLong(i++, ct_rent_power.longValue());  // POWERSUPPLY
                                INSERT_CONTRACT_AA_PreparedStatement.setLong(i++, ct_legal_type.longValue());  // LEGALTYPE
                                INSERT_CONTRACT_AA_PreparedStatement.setString(i++, ct_legal_name); // LEGAL_NAME
                                INSERT_CONTRACT_AA_PreparedStatement.setString(i++, ci_address); // LEGAL_ADDRESS
                                INSERT_CONTRACT_AA_PreparedStatement.setString(i++, ct_contact_person); // CONTACT_PERSON
                                INSERT_CONTRACT_AA_PreparedStatement.setString(i++, ct_contact_phone); // CONTACT_PHONE
                                INSERT_CONTRACT_AA_PreparedStatement.setLong(i++, ct_payment_period.longValue()); // PAYMENT_PERIOD
                                INSERT_CONTRACT_AA_PreparedStatement.setLong(i++, ct_currency_type.longValue());  // PAYMENTWAY
                                INSERT_CONTRACT_AA_PreparedStatement.setString(i++, ct_checkvat); // NEEDVAT
                                INSERT_CONTRACT_AA_PreparedStatement.setDate(i++, new java.sql.Date(formated_ct_contract_start_date.getTime())); // CONTRACT_START_DATE
                                INSERT_CONTRACT_AA_PreparedStatement.setDate(i++, new java.sql.Date(formated_ct_contract_end_date.getTime())); // CONTRACT_END_DATE
                                INSERT_CONTRACT_AA_PreparedStatement.setString(i++, ct_autoprolongation); // AUTOPROLONGATION
                                INSERT_CONTRACT_AA_PreparedStatement.setLong(i++, 41); // AA_STATUS
                                INSERT_CONTRACT_AA_PreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // INSERT_DATE
                                INSERT_CONTRACT_AA_PreparedStatement.setString(i++, starter); // INSERT_PERSON

                                INSERT_CONTRACT_AA_PreparedStatement.executeUpdate();
                                log.info("successfull INSERT_CONTRACT_AA created!");

                                ResultSet createdContracAaIdResultSet = INSERT_CONTRACT_AA_PreparedStatement.getGeneratedKeys();
                                createdContracAaIdResultSet.next();
                                createdContractAA = createdContracAaIdResultSet.getLong(1);
                                log.info("createdContractAA:");
                                log.info(createdContractAA.toString());

                                //CONTRACT_AA_STATUS
                                log.info("CONTRACT_AA_STATUS preparedStatement");
                                Long createdContractAAStatusID = null;
                                String returnContractAAStatusID[] = { "ACTION_ID" };
                                String INSERT_CONTRACT_AA_STATUS = "INSERT INTO CONTRACT_AA_STATUS (ACTION_ID, AAID, statusid, STATUSDATE ) VALUES (CONTRACT_AA_STATUS_SEQ.nextval, ?, 41, ?)";
                                PreparedStatement InsertContractAaStatusPreparedStatement = udbConnect.prepareStatement(INSERT_CONTRACT_AA_STATUS, returnContractAAStatusID);

                                i = 1;
                                // set values to update
                                InsertContractAaStatusPreparedStatement.setLong(i++, createdContractAA);  // createdContractAA
                                InsertContractAaStatusPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // csreldate

                                InsertContractAaStatusPreparedStatement.executeUpdate();
                                log.info("successfull CONTRACT_AA_STATUS!");

                                ResultSet InsertContractStatusRelResultSet = InsertContractAaStatusPreparedStatement.getGeneratedKeys();
                                InsertContractStatusRelResultSet.next();
                                createdContractAAStatusID = InsertContractStatusRelResultSet.getLong(1);
                                log.info("createdContractAAStatusID:");
                                log.info(createdContractAAStatusID.toString());
                            }
                        }
                    }

                    if(_CONTRACT_APPROVAL_TYPE.equals("FE")){
                        log.info("notCreatedContractArtefacts:");
                        log.info(notCreatedContractArtefacts.toString());
                        log.info("notCreatedContractArtefacts length: " + notCreatedContractArtefacts.length());
                        delegateExecution.setVariable("notCreatedContractArtefacts", SpinValues.jsonValue(notCreatedContractArtefacts.toString()));
                    }

                    udbConnect.commit();
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
                log.warning(e.toString());
                throw e;
            }
        } catch (SQLException e) {
            log.warning("testConnect SQLException!");
            log.warning(e.toString());
            log.warning("SQL State: %s\n%s");
            log.warning(e.getSQLState());
            log.warning(e.getMessage());
            delegateExecution.createIncident("SQLException", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.warning("testConnect Exception!");
            e.printStackTrace();
            delegateExecution.createIncident("Exception", e.getMessage());
            throw e;
        }

    }
}
