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
                    String contractVariableName =  _CONTRACT_APPROVAL_TYPE == "CN" ? "contractInformations" : "contractInformationsFE";

                    SpinJsonNode contractInformationsJSON = JSON(delegateExecution.getVariable(contractVariableName));
                    SpinList contractInformations = contractInformationsJSON.elements();
                    String contractid = "";
                    int ct_vendor_sap;
                    int ct_agreement_type;
                    for (int j=0; j<contractInformations.size(); j++) {
                        SpinJsonNode ci = (SpinJsonNode) contractInformations.get(j);
                        if (ci != null) {

                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-dd-MM"); //2020-01-02T18:00:00.000Z
                            String ct_acceptance_act_date = ci.prop("ct_acceptance_act_date").stringValue().substring(0,9);
                            String ct_contract_start_date = ci.prop("ct_contract_start_date").stringValue().substring(0,9);
                            String ct_contract_end_date = ci.prop("ct_contract_end_date").stringValue().substring(0,9);

                            Date formated_ct_acceptance_act_date = formatter.parse(ct_acceptance_act_date);
                            Date formated_ct_contract_start_date = formatter.parse(ct_contract_start_date);
                            Date formated_ct_contract_end_date = formatter.parse(ct_contract_end_date);

                            Number ct_rent = ci.prop("ct_rent").numberValue();
                            String ct_bin = ci.prop("ct_bin").numberValue().toString();
                            String ct_iban = ci.prop("ct_iban").stringValue();
                            String ct_legal_name = ci.prop("ct_legal_name").stringValue();
                            Number ct_rent_power = ci.prop("ct_rent_power").numberValue();
    //                        Number legalType = ci.prop("legalType").numberValue();
                            Number legalType = 0;

                            Number ct_contract_type = 0;

                            String ci_address = "" + (ci.prop("address").hasProp("cn_addr_oblast") ? ci.prop("address").prop("cn_addr_oblast").stringValue() : "") +
                                (ci.prop("address").hasProp("cn_addr_district") ? ", " + ci.prop("address").prop("cn_addr_district").stringValue() : "") +
                                (ci.prop("address").hasProp("cn_addr_city") ? ", " + ci.prop("address").prop("cn_addr_city").stringValue() : "") +
                                (ci.prop("address").hasProp("cn_addr_street") ? ", " + ci.prop("address").prop("cn_addr_street").stringValue() : "") +
                                (ci.prop("address").hasProp("cn_addr_building") ? ", " + ci.prop("address").prop("cn_addr_building").stringValue() : "") +
                                (ci.prop("address").hasProp("cn_addr_cadastral_number") ? ", " + ci.prop("address").prop("cn_addr_cadastral_number").stringValue() : "") +
                                (ci.prop("address").hasProp("cn_addr_note") ? ", " + ci.prop("address").prop("cn_addr_note").stringValue() : "");

                            String ct_contact_person = ci.prop("ct_contact_person").stringValue();
                            String ct_contact_phone = ci.prop("ct_contact_phone").stringValue();
                            String ct_access_status = ci.prop("ct_access_status").stringValue();
                            String ct_autoprolongation = ci.prop("ct_autoprolongation").stringValue();
//                            String ct_contract_sap = ci.prop("ct_contract_sap").stringValue();
                            Long ct_contract_sap = ci.prop("ct_contract_sap").numberValue().longValue();

//                            String ct_contract_type = ci.prop("ct_contract_type").stringValue();
                            String vat = ci.prop("ct_access_status").stringValue() == "No" ? "Not" : "With";

                            if (ci.hasProp("ct_cid")) {
                                System.out.println("OLD CONTRACT....");
                                Long ct_cid = ci.prop("ct_cid").numberValue().longValue();

                                contractid = ci.prop("ct_contractid_old").stringValue();
                                ct_vendor_sap = ci.prop("ct_vendor_sap").numberValue().intValue();
//                                ct_agreement_type = ci.prop("ct_agreement_type").numberValue().intValue();
                                ct_agreement_type = Integer.parseInt(ci.prop("ct_agreement_type").stringValue());
                                //UPDATE NCP
                                if (ci.hasProp("ct_action_id")) {
                                    Long action_id = ci.prop("ct_action_id").numberValue().longValue();
                                    String UPDATE_CONTRACT_AA_STATUS_GEN_STATUS = "update CONTRACT_AA_STATUS set STATUSID = ?, STATUSDATE=?, STATUSPERSON=? where ACTION_ID = ?";

                                    PreparedStatement updateCONTRACT_AA_STATUSPreparedStatement = udbConnect.prepareStatement(UPDATE_CONTRACT_AA_STATUS_GEN_STATUS);

                                    System.out.println("CONTRACT_AA_STATUS preparedStatement SQL UPDATE VALUES....");
                                    // set values to update
                                    int i = 1;
                                    updateCONTRACT_AA_STATUSPreparedStatement.setLong(i++, 40); // STATUSID
                                    updateCONTRACT_AA_STATUSPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // STATUSDATE
                                    updateCONTRACT_AA_STATUSPreparedStatement.setString(i++, starter); // STATUSPERSON
                                    updateCONTRACT_AA_STATUSPreparedStatement.setLong(i++, action_id ); //  //ACTION_ID
                                    updateCONTRACT_AA_STATUSPreparedStatement.executeUpdate();
                                    System.out.println("...CONTRACT_AA_STATUS updated");

                                }

                            } else {
                                System.out.println("NEW CONTRACT....");
                                contractid = ci.prop("ct_contractid").stringValue();
                                ct_vendor_sap = ci.prop("ct_vendor_sap").numberValue().intValue();
//                                ct_vendor_sap = Integer.parseInt(ci.prop("ct_vendor_sap").stringValue());
                                ct_agreement_type = 0;
                            }

                            Long createdContractCID = null;
                            String returnStatus[] = { "CID" };
                            String INSERT_CONTRACTS = "INSERT INTO APP_APEXUDB_CAMUNDA.CONTRACTS (CID, RENTSUM, RENTSUM_VAT, CONTRACTID, INCOMINGDATE, INCOMINGWEEK, CONTRACTTYPE, POWERSUPPLY, LEGALTYPE, LEGALNAME, LEGALADDRESS, CONTACTPERSON, CONTACTPHONE, ACCESS_STATUS, CONTRACT_SAP_NO, VENDOR_SAP_NO, CONTRACT_EXECUTOR, VAT, NEEDVAT, PAYMENTPERIOD, PAYMENTWAY, CONTRACTSTARTDATE, CONTRACTENDDATE, AUTOPROLONGATION, USERNAME, OBLAST_VILLAGEID, AREA_ACT_ACCEPT_DATE, RNN, INN, IBAN) VALUES (CONTRACTS_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                            PreparedStatement INSERT_CONTRACTSPreparedStatement = udbConnect.prepareStatement(INSERT_CONTRACTS, returnStatus);

                            int i = 1;
                            System.out.println("_SET_NCP_STATUS preparedStatement SQL UPDATE VALUES");
                            // set values to update
                            INSERT_CONTRACTSPreparedStatement.setLong(i++, ct_rent.longValue());  // RENTSUM
                            INSERT_CONTRACTSPreparedStatement.setLong(i++, 0);  // RENTSUM_VAT .. ct_vat неету
                            INSERT_CONTRACTSPreparedStatement.setString(i++, contractid);  // CONTRACTID
                            INSERT_CONTRACTSPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // INCOMINGDATE
                            INSERT_CONTRACTSPreparedStatement.setString(i++, "12");  // INCOMINGWEEK
                            INSERT_CONTRACTSPreparedStatement.setLong(i++, ct_contract_type.longValue());  // CONTRACTTYPE net spravochnika
                            INSERT_CONTRACTSPreparedStatement.setLong(i++, ct_rent_power.longValue());  // POWERSUPPLY
                            INSERT_CONTRACTSPreparedStatement.setLong(i++, legalType.longValue());  // LEGALTYPE
                            INSERT_CONTRACTSPreparedStatement.setString(i++, ct_legal_name);  // LEGALNAME
                            INSERT_CONTRACTSPreparedStatement.setString(i++, ci_address);  // LEGALADDRESS
                            INSERT_CONTRACTSPreparedStatement.setString(i++, ct_contact_person);  // CONTACTPERSON
                            INSERT_CONTRACTSPreparedStatement.setString(i++, ct_contact_phone);  // CONTACTPHONE
                            INSERT_CONTRACTSPreparedStatement.setLong(i++, Integer.parseInt(ct_access_status));  // ACCESS_STATUS
                            INSERT_CONTRACTSPreparedStatement.setString(i++, ct_contract_sap.toString());  // CONTRACT_SAP_NO
                            INSERT_CONTRACTSPreparedStatement.setInt(i++,  ct_vendor_sap);  // VENDOR_SAP_NO
                            INSERT_CONTRACTSPreparedStatement.setLong(i++, 0);  // CONTRACT_EXECUTOR netu
                            INSERT_CONTRACTSPreparedStatement.setString(i++, vat);  // VAT ??
                            INSERT_CONTRACTSPreparedStatement.setString(i++, vat);  // NEEDVAT ??
                            INSERT_CONTRACTSPreparedStatement.setLong(i++, 3);  // PAYMENTPERIOD ??
                            INSERT_CONTRACTSPreparedStatement.setLong(i++, 2);  // PAYMENTWAY ???
                            INSERT_CONTRACTSPreparedStatement.setDate(i++, new java.sql.Date(formated_ct_contract_start_date.getTime())); // CONTRACTSTARTDATE
                            INSERT_CONTRACTSPreparedStatement.setDate(i++, new java.sql.Date(formated_ct_contract_end_date.getTime())); // CONTRACTENDDATE
                            INSERT_CONTRACTSPreparedStatement.setString(i++, ct_autoprolongation);  // AUTOPROLONGATION
                            INSERT_CONTRACTSPreparedStatement.setString(i++, starter);  // USERNAME
                            INSERT_CONTRACTSPreparedStatement.setLong(i++, 1);  // OBLAST_VILLAGEID // ???
                            INSERT_CONTRACTSPreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime()));  // AREA_ACT_ACCEPT_DATE
                            INSERT_CONTRACTSPreparedStatement.setString(i++, ct_bin);  // RNN
                            INSERT_CONTRACTSPreparedStatement.setString(i++, ct_bin);  // INN
                            INSERT_CONTRACTSPreparedStatement.setString(i++, ct_iban);  // IBAN

                            INSERT_CONTRACTSPreparedStatement.executeUpdate();
                            System.out.println("successfull CONTRACTS created!");

                            ResultSet statusGeneratedIdResultSet = INSERT_CONTRACTSPreparedStatement.getGeneratedKeys();
                            statusGeneratedIdResultSet.next();
                            createdContractCID = statusGeneratedIdResultSet.getLong(1);
                            System.out.println("createdContractCID:");
                            System.out.println(createdContractCID);


                            Long createdContractArtefactID = null;
                            String returnContractArtefactID[] = { "ID" };
                            String INSERT_CONTRACT_ARTEFACT = "INSERT INTO APP_APEXUDB_CAMUNDA.CONTRACT_ARTEFACT (ID, CID, ARTEFACTID) VALUES (CONTRACT_ARTEFACT_SEQ.nextval, ?, ?)";
                            PreparedStatement InsertContractArtefactPreparedStatement = udbConnect.prepareStatement(INSERT_CONTRACT_ARTEFACT, returnContractArtefactID);

                            i = 1;
                            System.out.println("_SET_NCP_STATUS preparedStatement SQL UPDATE VALUES");
                            // set values to update
                            InsertContractArtefactPreparedStatement.setLong(i++, createdContractCID);  // createdContractCID
                            InsertContractArtefactPreparedStatement.setLong(i++, createdArtefactId);  // createdArtefactId

                            InsertContractArtefactPreparedStatement.executeUpdate();
                            System.out.println("successfull NCP_STATUS updated!");

                            ResultSet InsertContractArtefactResultSet = InsertContractArtefactPreparedStatement.getGeneratedKeys();
                            InsertContractArtefactResultSet.next();
                            createdContractArtefactID = InsertContractArtefactResultSet.getLong(1);
                            System.out.println("createdContractArtefactID:");
                            System.out.println(createdContractArtefactID);

                            if (ci.hasProp("ct_action_id")) {
                                Long createdContractAA = null;
                                String returnContractAA[] = { "CID" };
                                String INSERT_CONTRACT_AA = "INSERT INTO APP_APEXUDB_CAMUNDA.CONTRACT_AA (AAID, CID, ARTEFACTID, AA_TYPE, CONTRACT_TYPE, RENTSUM, RENTSUM_VAT, POWERSUPPLY, LEGAL_TYPE, LEGAL_NAME, LEGAL_ADDRESS, CONTACT_PERSON, CONTACT_PHONE, PAYMENT_PERIOD, PAYMENT_WAY, VAT, NEEDVAT, CONTRACT_START_DATE, CONTRACT_END_DATE, AUTOPROLONGATION, OBLAST_VILLAGE_ID, INSERT_DATE, INSERT_PERSON) VALUES (CONTRACT_AA_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                                PreparedStatement INSERT_CONTRACT_AA_PreparedStatement = udbConnect.prepareStatement(INSERT_CONTRACT_AA, returnStatus);

                                i = 1;
                                System.out.println("_SET_NCP_STATUS preparedStatement SQL UPDATE VALUES");
                                // set values to update


                                INSERT_CONTRACT_AA_PreparedStatement.setLong(i++, createdContractCID); // CID
                                INSERT_CONTRACT_AA_PreparedStatement.setLong(i++, createdArtefactId); // ARTEFACTID
                                INSERT_CONTRACT_AA_PreparedStatement.setLong(i++, ct_agreement_type); // AA_TYPE
                                INSERT_CONTRACTSPreparedStatement.setLong(i++, ct_contract_type.longValue());  // CONTRACTTYPE net spravochnika
                                INSERT_CONTRACT_AA_PreparedStatement.setLong(i++, ct_rent.longValue());  // RENTSUM
                                INSERT_CONTRACT_AA_PreparedStatement.setLong(i++, 0);  // RENTSUM_VAT .. ct_vat неету
                                INSERT_CONTRACT_AA_PreparedStatement.setLong(i++, ct_rent.longValue()); // POWERSUPPLY ?
                                INSERT_CONTRACT_AA_PreparedStatement.setLong(i++, legalType.longValue());  // LEGALTYPE
                                INSERT_CONTRACT_AA_PreparedStatement.setString(i++, ct_legal_name);  // LEGALNAME
                                INSERT_CONTRACT_AA_PreparedStatement.setString(i++, ci_address);  // LEGALADDRESS
                                INSERT_CONTRACT_AA_PreparedStatement.setString(i++, ct_contact_person);  // CONTACTPERSON
                                INSERT_CONTRACT_AA_PreparedStatement.setString(i++, ct_contact_phone);  // CONTACTPHONE
                                INSERT_CONTRACT_AA_PreparedStatement.setLong(i++, 3);  // PAYMENTPERIOD ??
    //                            INSERT_CONTRACT_AA_PreparedStatement.setLong(i++, ct_rent.longValue()); // PAYMENT_WAY
                                INSERT_CONTRACT_AA_PreparedStatement.setString(i++, vat);  // VAT ??
                                INSERT_CONTRACT_AA_PreparedStatement.setString(i++, vat);  // NEEDVAT ??
                                INSERT_CONTRACT_AA_PreparedStatement.setDate(i++, new java.sql.Date(formated_ct_contract_start_date.getTime())); // CONTRACTSTARTDATE
                                INSERT_CONTRACT_AA_PreparedStatement.setDate(i++, new java.sql.Date(formated_ct_contract_end_date.getTime())); // CONTRACTENDDATE
                                INSERT_CONTRACT_AA_PreparedStatement.setString(i++, ct_autoprolongation);  // AUTOPROLONGATION
                                INSERT_CONTRACT_AA_PreparedStatement.setLong(i++, 1);  // OBLAST_VILLAGEID // ???
                                INSERT_CONTRACT_AA_PreparedStatement.setDate(i++, new java.sql.Date(new Date().getTime())); // INSERT_DATE
                                INSERT_CONTRACT_AA_PreparedStatement.setString(i++, starter); // INSERT_PERSON

                                INSERT_CONTRACT_AA_PreparedStatement.executeUpdate();
                                System.out.println("successfull CONTRACTS created!");

                                ResultSet createdContracAaIdResultSet = INSERT_CONTRACT_AA_PreparedStatement.getGeneratedKeys();
                                createdContracAaIdResultSet.next();
                                createdContractAA = createdContracAaIdResultSet.getLong(1);
                                System.out.println("createdContractAA:");
                                System.out.println(createdContractAA);
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
        } catch (Exception e) {
            System.out.println("testConnect Exception!");
            e.printStackTrace();
        }

    }
}
