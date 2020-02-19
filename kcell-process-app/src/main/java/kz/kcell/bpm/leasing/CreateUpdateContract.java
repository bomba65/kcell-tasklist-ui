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
                    
                    String starter = delegateExecution.getVariable("starter").toString();
                    Long createdArtefactId = (Long) delegateExecution.getVariable("createdArtefactId");
                    SpinJsonNode contractInformations = JSON(delegateExecution.getVariable("contractInformations"));
//                    SpinJsonNode contractInformationsFE = JSON(delegateExecution.getVariable("contractInformationsFE"));

                    SpinJsonNode contractInformationsFeJSON = JSON(delegateExecution.getVariable("contractInformationsFE"));
                    SpinList contractInformationsFEs = contractInformationsFeJSON.elements();
                    for (int j=0; j<contractInformationsFEs.size(); j++) {
                        SpinJsonNode ci = (SpinJsonNode) contractInformationsFEs.get(j);
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
                            String ct_cid = ci.prop("ct_cid").stringValue();
                            String ct_legal_name = ci.prop("ct_legal_name").stringValue();
                            Number ct_rent_power = ci.prop("ct_rent_power").numberValue();
    //                        Number legalType = ci.prop("legalType").numberValue();
                            Number legalType = 0;
                            String contractid = "";
                            if (ct_cid != null) {
                                contractid = ci.prop("ct_contractid_old").stringValue();
                            } else {
                                contractid = ci.prop("ct_contractid").stringValue();
                            }

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
                            Long ct_contract_sap = ci.prop("ct_contract_sap").numberValue().longValue();
                            Long ct_vendor_sap = ci.prop("ct_vendor_sap").numberValue().longValue();
                            String vat = ci.prop("ct_access_status").stringValue() == "No" ? "Not" : "With";

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
                            INSERT_CONTRACTSPreparedStatement.setLong(i++, ct_contract_sap);  // CONTRACT_SAP_NO
                            INSERT_CONTRACTSPreparedStatement.setLong(i++, ct_vendor_sap);  // VENDOR_SAP_NO
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
                            System.out.println("successfull NCP_STATUS updated!");

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
