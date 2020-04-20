package kz.kcell.bpm.leasing;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.camunda.spin.Spin.JSON;

@Service("CreateUpdateContract")
public class CreateUpdateContract implements JavaDelegate {

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
                    System.out.println("Connected to the database!");

                    // proc vars

                    String _CONTRACT_APPROVAL_TYPE = delegateExecution.hasVariableLocal("_CONTRACT_APPROVAL_TYPE") ? delegateExecution.getVariable("_CONTRACT_APPROVAL_TYPE").toString() : null;

                    String starter = delegateExecution.getVariable("starter").toString();
                    Long createdArtefactId = (Long) delegateExecution.getVariable("createdArtefactId");
//                    String contractVariableName =  _CONTRACT_APPROVAL_TYPE == "CN" ? "contractInformations" : "contractInformationsFE";
                    String contractVariableName =  _CONTRACT_APPROVAL_TYPE.equals("CN")  ? "contractInformations" : "contractInformationsFE";
//
//                    System.out.println("_CONTRACT_APPROVAL_TYPE:");
//                    System.out.println(delegateExecution.hasVariableLocal("_CONTRACT_APPROVAL_TYPE") ? delegateExecution.getVariable("_CONTRACT_APPROVAL_TYPE").toString() : null);
//                    System.out.println("END _CONTRACT_APPROVAL_TYPE");
//                    System.out.println("contractVariableName: " + contractVariableName + " end contractVariableName");
//                    System.out.println("contractVariableName2: " + contractVariableName2 + " end contractVariableName2");

                    SpinJsonNode contractInformationsJSON = JSON(delegateExecution.getVariable(contractVariableName));
                    SpinList contractInformations = contractInformationsJSON.elements();
                    String contractid = "";
                    String ct_contractid_old = "";
                    int ct_vendor_sap = 0;
                    int ct_agreement_type = 0;
                    for (int j=0; j<contractInformations.size(); j++) {
                        SpinJsonNode ci = (SpinJsonNode) contractInformations.get(j);
                        if (ci != null) {

                            contractid = ci.hasProp("ct_contractid") ? ci.prop("ct_contractid").value().toString() : "";
                            String ct_acquisitionType = ci.prop("ct_acquisitionType").stringValue();
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-dd-MM"); //2020-01-02T18:00:00.000Z
                            String ct_acceptance_act_date = ci.prop("ct_acceptance_act_date").stringValue().substring(0,9);
                            String ct_contract_start_date = ci.prop("ct_contract_start_date").stringValue().substring(0,9);
                            String ct_contract_end_date = ci.prop("ct_contract_end_date").stringValue().substring(0,9);

                            Date formated_ct_acceptance_act_date = formatter.parse(ct_acceptance_act_date);
                            Date formated_ct_contract_start_date = formatter.parse(ct_contract_start_date);
                            Date formated_ct_contract_end_date = formatter.parse(ct_contract_end_date);

                            Number ct_rent = ci.prop("ct_rent").numberValue();
                            Number ct_rent_all = ci.prop("ct_rent_all").numberValue();
                            Number ct_rent_area = ci.prop("ct_rent_area").numberValue();
                            String ct_bin = ci.prop("ct_bin").numberValue().toString();
                            String ct_iban = ci.prop("ct_iban").stringValue();
                            String ct_legal_name = ci.prop("ct_legal_name").stringValue();
                            Number ct_rent_power = ci.hasProp("ct_rent_power") ? ci.prop("ct_rent_power").numberValue() : 0;
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

                            String ct_contact_person = ci.prop("ct_contact_person").stringValue();
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
                                System.out.println("ct_legal_type:");
//                                System.out.println(ci.prop("legalType").value().toString());
//                                ct_bank_id = Integer.parseInt(ci.hasProp("ct_bankname") ? ci.prop("ct_bankname").stringValue() : "0");
                                String legalTypeString = ci.prop("legalType").hasProp("udbid") ? ci.prop("legalType").prop("udbid").value().toString() : "0";
                                ct_legal_type = Integer.parseInt(legalTypeString);
//                                System.out.println("----legalTypeString:");
//                                System.out.println(legalTypeString);
                                System.out.println("----ct_legal_type:");
                                System.out.println(ct_legal_type);
                                System.out.println("END ct_legal_type");
                            }

//                            System.out.println("legalType:");
                            
//                            legalType = Integer.parseInt(ci.hasProp("legalType") && ci.prop("legalType").hasProp("udbid") ? ci.prop("legalType").prop("udbid").stringValue() : "0");
//                            if (ci.hasProp("legalType")) {
//                                System.out.println("ci has prop legalType");
//                                System.out.println(ci.prop("legalType").value().toString());
//                                SpinJsonNode ltJson = ci.prop("legalType");
//                                String ltId = ltJson.hasProp("udbid") ? ltJson.prop("udbid").value().toString() : " ";
//                                System.out.println("ltJson.hasProp(\"udbid\"):");
//                                System.out.println(ltJson.hasProp("udbid"));
//                                System.out.println("ltId:");
//                                System.out.println(ltId);
//
//                                if (ci.prop("legalType").hasProp("udbid")) {
//                                    System.out.println("legalType has prop udbid");
//                                    System.out.println(ci.prop("legalType").prop("udbid").value().toString());
//                                    legalType = Integer.parseInt(ci.prop("legalType").prop("udbid").stringValue());
//                                }
//                            }

//                            System.out.println(ci.hasProp("legalType") && ci.prop("legalType").hasProp("udbid") ? ci.prop("legalType").prop("udbid").stringValue() : "0");
//                            System.out.println(legalType);
//                            System.out.println("end legalType:");

                            String ct_checkvat = ci.prop("ct_checkvat").stringValue() == "No" ? "no" : "yes";
                            Long ct_cid = new Long(1);
                            int i = 1;

                            if (!ct_acquisitionType.equals("newContract")) {
                                System.out.println("OLD CONTRACT....");
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
                                if (ct_acquisitionType.equals("existingContract")) {
                                    String UPDATE_CONTRACT_STATUS_REL = "update CONTRACT_STATUS_REL set STATUSID = 40, csreldate=? where CONTRACTID = ?";
                                    PreparedStatement UPDATE_CONTRACT_STATUS_RELPreparedStatement = udbConnect.prepareStatement(UPDATE_CONTRACT_STATUS_REL);
                                    System.out.println("CONTRACT_STATUS_REL preparedStatement SQL UPDATE VALUES....");
                                    // set values to update
                                    i = 1;
                                    UPDATE_CONTRACT_STATUS_RELPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // csreldate
                                    UPDATE_CONTRACT_STATUS_RELPreparedStatement.setLong(i++, ct_cid.longValue() ); //  //ct_cid
                                    UPDATE_CONTRACT_STATUS_RELPreparedStatement.executeUpdate();
                                    System.out.println("...CONTRACT_STATUS_REL updated");

                                    String UPDATE_CONTRACTS_STATUSES = "update CONTRACTS_STATUSES set LEASING_STATUS = ? where CID = ?";
                                    PreparedStatement UPDATE_CONTRACTS_STATUSESPreparedStatement = udbConnect.prepareStatement(UPDATE_CONTRACTS_STATUSES);
                                    System.out.println("CONTRACTS_STATUSES preparedStatement SQL UPDATE VALUES....");
                                    // set values to update
                                    i = 1;
                                    UPDATE_CONTRACTS_STATUSESPreparedStatement.setLong(i++, 40); // STATUSID
                                    UPDATE_CONTRACTS_STATUSESPreparedStatement.setLong(i++, ct_cid.longValue() ); //  //ct_cid
                                    UPDATE_CONTRACTS_STATUSESPreparedStatement.executeUpdate();
                                    System.out.println("...CONTRACTS_STATUSES updated");
                                }
                            } else if (ct_acquisitionType.equals("newContract")) {
                                System.out.println("NEW CONTRACT....");
//                                contractid = ci.prop("ct_contractid").stringValue();
                                ct_vendor_sap = ci.prop("ct_vendor_sap").numberValue().intValue();
//                                ct_vendor_sap = Integer.parseInt(ci.prop("ct_vendor_sap").stringValue());
                                ct_agreement_type = 0;
                            }

                            i = 1;
                            String returnStatus[] = { "CID" };
                            Long createdContractCID = null;
                            if (!ct_acquisitionType.equals("additionalAgreement")) {
                                //INSERT_CONTRACTS
                                String INSERT_CONTRACTS = "";
                                if (ct_acquisitionType.equals("existingContract")) {
                                    INSERT_CONTRACTS = "INSERT INTO APP_APEXUDB_CAMUNDA.CONTRACTS (CID, OLD_CID, RENTSUM, RENTAREA, CONTRACTID, INCOMINGDATE, INCOMINGWEEK, CONTRACTTYPE, POWERSUPPLY, LEGALTYPE, LEGALNAME, LEGALADDRESS, CONTACTPERSON, CONTACTPHONE, ACCESS_STATUS, CONTRACT_SAP_NO, VENDOR_SAP_NO, CONTRACT_EXECUTOR, NEEDVAT, PAYMENTPERIOD, PAYMENTWAY, CONTRACTSTARTDATE, CONTRACTENDDATE, AUTOPROLONGATION, USERNAME, AREA_ACT_ACCEPT_DATE, RNN, IBAN, BANK_ID) VALUES (CONTRACTS_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                                } else {
                                    INSERT_CONTRACTS = "INSERT INTO APP_APEXUDB_CAMUNDA.CONTRACTS (CID, RENTSUM, RENTAREA, CONTRACTID, INCOMINGDATE, INCOMINGWEEK, CONTRACTTYPE, POWERSUPPLY, LEGALTYPE, LEGALNAME, LEGALADDRESS, CONTACTPERSON, CONTACTPHONE, ACCESS_STATUS, CONTRACT_SAP_NO, VENDOR_SAP_NO, CONTRACT_EXECUTOR, NEEDVAT, PAYMENTPERIOD, PAYMENTWAY, CONTRACTSTARTDATE, CONTRACTENDDATE, AUTOPROLONGATION, USERNAME, AREA_ACT_ACCEPT_DATE, RNN, IBAN, BANK_ID) VALUES (CONTRACTS_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                                }

                                PreparedStatement INSERT_CONTRACTSPreparedStatement = udbConnect.prepareStatement(INSERT_CONTRACTS, returnStatus);

                                System.out.println("INSERT_CONTRACTS preparedStatement SQL UPDATE VALUES");
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
//                                INSERT_CONTRACTSPreparedStatement.setString(i++, ct_bin);  // INN ??
                                INSERT_CONTRACTSPreparedStatement.setString(i++, ct_iban);  // IBAN
                                INSERT_CONTRACTSPreparedStatement.setLong(i++, ct_bank_id.longValue());  // BANK_ID
                                // bank_id ct_bankname

                                INSERT_CONTRACTSPreparedStatement.executeUpdate();
                                System.out.println("successfull CONTRACTS created!");

                                ResultSet statusGeneratedIdResultSet = INSERT_CONTRACTSPreparedStatement.getGeneratedKeys();
                                statusGeneratedIdResultSet.next();
                                createdContractCID = statusGeneratedIdResultSet.getLong(1);
                                System.out.println("createdContractCID:");
                                System.out.println(createdContractCID);
                                Number fe_artefact_id = 0;

                                //INSERT_CONTRACT_ARTEFACT
                                if(_CONTRACT_APPROVAL_TYPE.equals("FE")){
                                    String ct_fe_sitename = ci.prop("ct_sitename").value().toString();
                                    //find artefact_id by ct_fe_sitename

                                    String SelectArtefactBySite = "select * from ARTEFACT where SITENAME = ?";
                                    PreparedStatement selectArtefactBySitePreparedStatement = udbConnect.prepareStatement(SelectArtefactBySite);
                                    i = 1;
                                    System.out.println("get artefact_id by ct_fe_sitename...");
                                    System.out.println(ct_fe_sitename);
                                    selectArtefactBySitePreparedStatement.setString(i++, ct_fe_sitename); // sitename
                                    ResultSet resultSet = selectArtefactBySitePreparedStatement.executeQuery();

                                    if (resultSet.next() == false) {
                                        System.out.println("not Found");
                                    } else {
                                        fe_artefact_id = resultSet.getInt("ARTEFACTID");
                                    }
                                    System.out.println("fe_artefact_id:");
                                    System.out.println(fe_artefact_id);

                                }

                                Long createdContractArtefactID = null;
                                String returnContractArtefactID[] = { "ID" };
                                String INSERT_CONTRACT_ARTEFACT = "INSERT INTO APP_APEXUDB_CAMUNDA.CONTRACT_ARTEFACT (ID, CID, ARTEFACTID) VALUES (CONTRACT_ARTEFACT_SEQ.nextval, ?, ?)";
                                PreparedStatement InsertContractArtefactPreparedStatement = udbConnect.prepareStatement(INSERT_CONTRACT_ARTEFACT, returnContractArtefactID);

                                i = 1;
                                System.out.println("INSERT_CONTRACT_ARTEFACT preparedStatement SQL Insert VALUES");
                                // set values to update
                                InsertContractArtefactPreparedStatement.setLong(i++, createdContractCID);  // createdContractCID
                                if (_CONTRACT_APPROVAL_TYPE.equals("FE")) {
                                    InsertContractArtefactPreparedStatement.setLong(i++, fe_artefact_id.longValue());  // fe_artefact_id
                                } else {
                                    InsertContractArtefactPreparedStatement.setLong(i++, createdArtefactId);  // createdArtefactId
                                }


                                InsertContractArtefactPreparedStatement.executeUpdate();
                                System.out.println("successfull INSERT_CONTRACT_ARTEFACT created!");

                                ResultSet InsertContractArtefactResultSet = InsertContractArtefactPreparedStatement.getGeneratedKeys();
                                InsertContractArtefactResultSet.next();
                                createdContractArtefactID = InsertContractArtefactResultSet.getLong(1);
                                System.out.println("createdContractArtefactID:");
                                System.out.println(createdContractArtefactID);


                                //INSERT_CONTRACT_STATUS_REL
                                System.out.println("INSERT_CONTRACT_STATUS_REL preparedStatement");
                                Long createdContractStatusRelID = null;
                                String returnContractStatusRelID[] = { "CSRELID" };
                                String INSERT_CONTRACT_STATUS_REL = "INSERT INTO APP_APEXUDB_CAMUNDA.CONTRACT_STATUS_REL (csrelid, contractid, statusid, csreldate) VALUES (CONTRACT_STATUS_REL_SEQ.nextval, ?, 41, ?)";
                                PreparedStatement InsertContractStatusRelPreparedStatement = udbConnect.prepareStatement(INSERT_CONTRACT_STATUS_REL, returnContractStatusRelID);

                                i = 1;
                                // set values to update
                                InsertContractStatusRelPreparedStatement.setLong(i++, createdContractCID);  // createdContractCID
                                InsertContractStatusRelPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // csreldate

                                InsertContractStatusRelPreparedStatement.executeUpdate();
                                System.out.println("successfull INSERT_CONTRACT_STATUS_REL!");

                                ResultSet InsertContractStatusRelResultSet = InsertContractStatusRelPreparedStatement.getGeneratedKeys();
                                InsertContractStatusRelResultSet.next();
                                createdContractStatusRelID = InsertContractStatusRelResultSet.getLong(1);
                                System.out.println("createdContractStatusRelID:");
                                System.out.println(createdContractStatusRelID);


                                //INSERT_CONTRACT_STATUSES
                                String INSERT_CONTRACT_STATUSES = "INSERT INTO APP_APEXUDB_CAMUNDA.CONTRACTS_STATUSES (cid, leasing_status) VALUES (?, 41)";
                                PreparedStatement InsertContractStatusesPreparedStatement = udbConnect.prepareStatement(INSERT_CONTRACT_STATUSES);

                                i = 1;
                                System.out.println("INSERT_CONTRACT_STATUSES preparedStatement SQL UPDATE VALUES");
                                // set values to update
                                InsertContractStatusesPreparedStatement.setLong(i++, createdContractCID);  // createdContractCID

                                InsertContractStatusesPreparedStatement.executeUpdate();
                                System.out.println("successfull INSERT_CONTRACT_STATUSES updated!");
                            } else {
                                ct_contract_type = Integer.parseInt(ci.hasProp("ct_contract_type") ? ci.prop("ct_contract_type").value().toString() : "0");

                                ct_agreement_type = Integer.parseInt(ci.hasProp("ct_agreement_type") && !ci.prop("ct_agreement_type").value().equals(null) ? ci.prop("ct_agreement_type").value().toString() : "0");
                                String ct_agreement_reason= ci.hasProp("ct_agreement_type") && !ci.prop("ct_agreement_type").value().equals(null) ? ci.prop("ct_agreement_type").stringValue() : "";
                                String ct_agreement_number= ci.hasProp("ct_agreement_number") && !ci.prop("ct_agreement_number").value().equals(null) ? ci.prop("ct_agreement_number").stringValue() : "";
//                                Number ct_agreement_number = ci.hasProp("ct_agreement_number") && !ci.prop("ct_agreement_number").value().equals(null) ? ci.prop("ct_agreement_number").numberValue() : 0;
                                String ct_aa_date = ci.prop("ct_aa_date").stringValue().substring(0,9);
                                Date formated_ct_aa_date = formatter.parse(ct_aa_date);
                                Number ct_agreement_executor = Integer.parseInt(ci.hasProp("ct_agreement_executor") && !ci.prop("ct_agreement_executor").value().equals(null) ? ci.prop("ct_agreement_executor").value().toString() : "0");

                                Long createdContractAA = null;
                                String createdContractAAID[] = { "AAID" };
                                String INSERT_CONTRACT_AA = "INSERT INTO APP_APEXUDB_CAMUNDA.CONTRACT_AA (AAID, CID, AA_NUMBER, AA_DATE, AA_EXECUTOR, ARTEFACTID, AA_TYPE, AA_REASON, CONTRACT_TYPE, RENTSUM, RENT_AREA, POWERSUPPLY, LEGAL_TYPE, LEGAL_NAME, LEGAL_ADDRESS, CONTACT_PERSON, CONTACT_PHONE, PAYMENT_PERIOD, PAYMENT_WAY, NEEDVAT, CONTRACT_START_DATE, CONTRACT_END_DATE, AUTOPROLONGATION, AA_STATUS, INSERT_DATE, INSERT_PERSON) VALUES (CONTRACT_AA_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                                PreparedStatement INSERT_CONTRACT_AA_PreparedStatement = udbConnect.prepareStatement(INSERT_CONTRACT_AA, createdContractAAID);

                                i = 1;
                                System.out.println("INSERT_CONTRACT_AA preparedStatement SQL UPDATE VALUES");
                                // set values to update

                                INSERT_CONTRACT_AA_PreparedStatement.setLong(i++, ct_cid); // CID
                                INSERT_CONTRACT_AA_PreparedStatement.setString(i++, ct_agreement_number); // AA_NUMBER
                                INSERT_CONTRACT_AA_PreparedStatement.setDate(i++, new java.sql.Date(formated_ct_aa_date.getTime())); // AA_DATE
                                INSERT_CONTRACT_AA_PreparedStatement.setLong(i++, ct_agreement_executor.longValue()); // AA_EXECUTOR
                                INSERT_CONTRACT_AA_PreparedStatement.setLong(i++, createdArtefactId); // ARTEFACTID
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
                                System.out.println("successfull INSERT_CONTRACT_AA created!");

                                ResultSet createdContracAaIdResultSet = INSERT_CONTRACT_AA_PreparedStatement.getGeneratedKeys();
                                createdContracAaIdResultSet.next();
                                createdContractAA = createdContracAaIdResultSet.getLong(1);
                                System.out.println("createdContractAA:");
                                System.out.println(createdContractAA);

                                //CONTRACT_AA_STATUS
                                System.out.println("CONTRACT_AA_STATUS preparedStatement");
                                Long createdContractAAStatusID = null;
                                String returnContractAAStatusID[] = { "ACTION_ID" };
                                String INSERT_CONTRACT_AA_STATUS = "INSERT INTO APP_APEXUDB_CAMUNDA.CONTRACT_AA_STATUS (ACTION_ID, AAID, statusid, STATUSDATE ) VALUES (CONTRACT_AA_STATUS_SEQ.nextval, ?, 41, ?)";
                                PreparedStatement InsertContractAaStatusPreparedStatement = udbConnect.prepareStatement(INSERT_CONTRACT_AA_STATUS, returnContractAAStatusID);

                                i = 1;
                                // set values to update
                                InsertContractAaStatusPreparedStatement.setLong(i++, createdContractAA);  // createdContractAA
                                InsertContractAaStatusPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // csreldate

                                InsertContractAaStatusPreparedStatement.executeUpdate();
                                System.out.println("successfull CONTRACT_AA_STATUS!");

                                ResultSet InsertContractStatusRelResultSet = InsertContractAaStatusPreparedStatement.getGeneratedKeys();
                                InsertContractStatusRelResultSet.next();
                                createdContractAAStatusID = InsertContractStatusRelResultSet.getLong(1);
                                System.out.println("createdContractAAStatusID:");
                                System.out.println(createdContractAAStatusID);
                            }
                        }
                    }

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
            delegateExecution.createIncident("SQLException", e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("testConnect Exception!");
            e.printStackTrace();
            delegateExecution.createIncident("Exception", e.getMessage());
            throw e;
        }

    }
}
