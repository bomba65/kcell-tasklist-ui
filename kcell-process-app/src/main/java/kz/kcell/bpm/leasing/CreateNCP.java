package kz.kcell.bpm.leasing;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.json.SpinJsonNode;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Date;

import static org.camunda.spin.Spin.JSON;

@Service("createNCP")
public class CreateNCP implements JavaDelegate {

    @Autowired
    DataSource dataSource;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        try {
            Class.forName ("oracle.jdbc.OracleDriver");
            Connection udbConnect = DriverManager.getConnection(
                "jdbc:oracle:thin:@//sc2-appcl010406:1521/apexudb", "app_apexudb_camunda", "p28zt#7C");
            if (udbConnect != null) {
                udbConnect.setAutoCommit(false);

                // proc vars
                String ncpId = delegateExecution.getVariable("ncpId").toString();
                String region = delegateExecution.getVariable("region").toString(); // not number
                String longitude = delegateExecution.getVariable("longitude").toString();
                String latitude = delegateExecution.getVariable("latitude").toString();
                String reason = delegateExecution.getVariable("reason").toString(); // it's string value, not dictionaries id
                String starter = delegateExecution.getVariable("starter").toString();
                String initiator = delegateExecution.getVariable("initiator").toString();


                System.out.println("companiesPreparedStatement");
//                PreparedStatement preparedStatement = udbConnect.prepareStatement(companiesInsert, Statement.RETURN_GENERATED_KEYS);
//                PreparedStatement preparedStatement = udbConnect.prepareStatement(companiesInsert, generatedColumns);

                //insert NCP
                Long ncpCreatedId = null;
                String returnCols[] = { "ARTEFACTID" };
                String insertNCP = "INSERT INTO APP_APEXUDB_CAMUNDA.NCP_CREATION ( ARTEFACTID, NCPID, REGION, LONGITUDE, LATITUDE, REASON, PROJECT, CREATOR, DATEOFINSERT, COMMENTS, CABINETID, TARGET_COVERAGE, TYPE, GEN_STATUS, NCP_STATUS, NCP_STATUS_DATE, BAND, INITIATOR, PART) VALUES ( NCP_CREATION_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 7, 1, ?, ?, ?, ?)";
                PreparedStatement preparedStatement = udbConnect.prepareStatement(insertNCP, returnCols);

                int i = 1;
                System.out.println("companiesPreparedStatement.setString");
                // set values to insert
                preparedStatement.setString(i++, ncpId); // NCPID
                preparedStatement.setInt(i++, 1); // REGION
                preparedStatement.setString(i++, longitude); // LONGITUDE ex. E 76,890775
                preparedStatement.setString(i++, latitude); // LATITUDE
                preparedStatement.setInt(i++, 150); // REASON
                preparedStatement.setInt(i++, 343); // PROJECT
                preparedStatement.setString(i++, initiator); // CREATOR 'SERGEI.ZAITSEV'
                preparedStatement.setDate(i++, java.sql.Date.valueOf(java.time.LocalDate.now())); // DATEOFINSERT
                preparedStatement.setString(i++, "TEST TEST TEST TEST"); // COMMENTS
                preparedStatement.setInt(i++, 25); // CABINETID
                preparedStatement.setString(i++, "г.Алматы, альтернатива (ул. Тажибаева 184 (угол ул. Березовского)) для демонтируемого сайта 01830XTAZHIPIPE по адресу: ул. Тажибаева 155a, pipe.  M"); // TARGET_COVERAGE
                preparedStatement.setInt(i++, 6); // TYPE ncp_type (Подставлять ID согласно справочнику Site Type) ex: 6
                preparedStatement.setDate(i++, java.sql.Date.valueOf(java.time.LocalDate.now())); // NCP_STATUS_DATE
                preparedStatement.setString(i++, "1"); // BAND ex:'1'   ncp_band	(Подставлять ID согласно справочнику Bands)
                preparedStatement.setString(i++, starter); // INITIATOR (Подставлять ID согласно справочнику Part)
                preparedStatement.setInt(i++, 61); // PART

                System.out.println("companiesPreparedStatement.executeUpdate()");
                int status = preparedStatement.executeUpdate();
                System.out.println("insert status:");
                System.out.println(status); //1
                System.out.println("successfull insert to database!");

                ResultSet headGeneratedIdResultSet = preparedStatement.getGeneratedKeys();
                headGeneratedIdResultSet.next();
                ncpCreatedId = headGeneratedIdResultSet.getLong(1);
                System.out.println("artefactGeneratedId:");
                System.out.println(ncpCreatedId);


                //insert new status
                Long createdNcpStatusId = null;
                String returnStatus[] = { "STATUS_ACTION_ID" };
                String insertNewStatus = "insert into NCP_CREATION_STATUS_ACTION values ( NCP_CREATION_STATUS_ACTIO_SEQ.nextval, ?, 2, 'demo', TO_DATE('2013-08-09 09:25:55', 'YYYY-MM-DD HH24:MI:SS'), null)";
                preparedStatement = udbConnect.prepareStatement(insertNewStatus, returnStatus);

                i = 1;
                System.out.println("companiesPreparedStatement.setString");
                preparedStatement.setString(i, ncpCreatedId.toString());
                System.out.println("companiesPreparedStatement.executeUpdate()");
                status = preparedStatement.executeUpdate();
                System.out.println("insert status:");
                System.out.println(status); //1
                System.out.println("successfull insert to database!");

                ResultSet statusGeneratedIdResultSet = preparedStatement.getGeneratedKeys();
                statusGeneratedIdResultSet.next();
                createdNcpStatusId = statusGeneratedIdResultSet.getLong(1);
                System.out.println("createdNcpStatusId:");
                System.out.println(createdNcpStatusId);

                udbConnect.commit();

//                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
//                    if (generatedKeys.next()) {
//                        System.out.println(generatedKeys.getLong(1));
//                        System.out.println(generatedKeys.getLong(2));
//                    }
//                    else {
//                        throw new SQLException("Creating user failed, no ID obtained.");
//                    }
//                }

                ResultSet rs = preparedStatement.getGeneratedKeys();

                if (rs.next()) {
                    System.out.println("rs.next()");
                    long id = rs.getLong(1);
                    System.out.println("Inserted ID -" + id); // display inserted record
                }
                udbConnect.close();
                System.out.println("udbConnection closed!");
            } else {
                System.out.println("Failed to make connection!");
            }
        } catch (SQLException e) {
            System.out.println("testConnect SQLException!");
            System.out.println(e.toString());
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        } catch (Exception e) {
            System.out.println("testConnect Exception!");
            e.printStackTrace();
        }
            // ================= OLD ===============
//            Class.forName ("oracle.jdbc.OracleDriver");
//            Connection c = DriverManager.getConnection(
//                "jdbc:oracle:thin:@//sc2-appcl010406:1521/apexudb", "app_apexudb_camunda", "p28zt#7C");
//                //dataSource.getConnection();
//            Statement s = c.createStatement();

            // SpinJsonNode responseJson = JSON(delegateExecution.getVariable("response"));

            // String responseString = responseJson.toString();
            // responseString = "'" + responseString + "'";

            // SpinJsonNode bin = responseJson.prop("result").prop("bin");
            // String binString = bin.toString();
            // binString = binString.substring(1, binString.length() - 1);

            // String elicense = "null";
            // SpinJsonNode hasElicense = responseJson.prop("result").prop("elicense").prop("has_elicense");
            // String hasElicenseString = hasElicense.toString();
            // boolean hasElicenseBoolean = Boolean.parseBoolean(hasElicenseString);
            // if (hasElicenseBoolean) {
            //     SpinJsonNode elicenseJson = responseJson.prop("result").prop("elicense");
            //     elicense = elicenseJson.toString();
            //     elicense = "'" + elicense + "'";
            // }

            // String enforcementDebt = "null";
            // SpinJsonNode hasDebts = responseJson.prop("result").prop("enforcement_debt").prop("has_debts");
            // String hasDebtsString = hasDebts.toString();
            // boolean hasDebtsBoolean = Boolean.parseBoolean(hasDebtsString);
            // if (hasDebtsBoolean) {
            //     SpinJsonNode enforcementDebtJson = responseJson.prop("result").prop("enforcement_debt");
            //     enforcementDebt = enforcementDebtJson.toString();
            //     enforcementDebt = "'" + enforcementDebt + "'";
            // }

            // String taxDebt = "null";
            // SpinJsonNode totalArrear = responseJson.prop("result").prop("tax_debt").prop("total_arrear");
            // String totalArrearString = totalArrear.toString();
            // int totalArrearInt = Integer.parseInt(totalArrearString.toString());
            // if (totalArrearInt > 0) {
            //     SpinJsonNode taxDebtJson = responseJson.prop("result").prop("tax_debt");
            //     taxDebt = taxDebtJson.toString();
            //     taxDebt = "'" + taxDebt + "'";
            // }

            // String gzContract = "null";
            // SpinJsonNode count = responseJson.prop("result").prop("gz_contract").prop("count");
            // String countString = count.toString();
            // int countInt = Integer.parseInt(countString.toString());
            // if (countInt > 0) {
            //     SpinJsonNode gzContractJson = responseJson.prop("result").prop("gz_contract");
            //     gzContract = gzContractJson.toString();
            //     gzContract = "'" + gzContract + "'";
            // }

            // String gzRNU = "null";
            // SpinJsonNode isInRNU = responseJson.prop("result").prop("gz_rnu").prop("is_in_rnu");
            // String isInRNUString = isInRNU.toString();
            // boolean isInRNUBoolean = Boolean.parseBoolean(isInRNUString);
            // if (isInRNUBoolean) {
            //     SpinJsonNode gzRNUJson = responseJson.prop("result").prop("gz_rnu");
            //     gzRNU = gzRNUJson.toString();
            //     gzRNU = "'" + gzRNU + "'";
            // }

            // SpinJsonNode result = responseJson.prop("result");
            // String companiesInsert = "insert into b2b_companies.companies(id, bin, main_info, elicense_info, enforcement_debt_info, tax_dept_info, gz_contract_info, gz_rnu, current_date, rnn, name_ru, name_kz, registered_at, is_unregistered, date_of_unregestration, reason_of_unregistration, in_the_market, kato, full_address, size_code, okpo, oked_code, has_parent, parent_bin, parent_rnn, parent_name, is_bankrupt, bankrupt_date, bankrupt_document_number, is_inactive, inactive_date, inactive_document_number, is_wrong_address, wrong_address_date, wrong_address_document_date, is_pseudo_company, pseudo_company_date, pseudo_company_document_number, is_invalid_registration, invalid_registration_date, invalid_registration_document_number, is_vat_payer, vat_registration_date, vat_unregistration_date, vat_unregistration_reason, is_gz_rnu, gz_rnu_count, gz_contract_total_sum, gz_contract_count, has_elicense, tax_dept_id, has_enforcement_dept, enforcement_dept_total_sum, enforcement_dept_count, gz_supplier_id, head_id, tax_id) values(nextval('companies_id_seq')?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
//            String companiesInsert = " INSERT INTO APP_APEXUDB_CAMUNDA.NCP_CREATION ( ARTEFACTID, NCPID, REGION, LONGITUDE, LATITUDE, REASON, PROJECT, PLANNEDBY, BAND_OLD, CREATOR, DATEOFINSERT, OBLAST_VILLAGE_ID, LASTEDITOR,  COMMENTS, OBLAST_OBJECT_ID, CABINETID, TARGET_CELL, TARGET_COVERAGE, TYPE, GEN_STATUS, NCP_STATUS, NCP_STATUS_DATE, CANDIDATE_ID, BAND, INITIATOR, VIP_INITIATOR, PART, CBR_ID, TR_STATUS) VALUES ( NCP_CREATION_SEQ.nextval, ?, 1, 'E 76,890775', 'N 43,210375', 150, 343, 3, null, 'SERGEI.ZAITSEV', TO_DATE('2013-08-09 09:25:55', 'YYYY-MM-DD HH24:MI:SS'), 750000000, 'VLADIMIR.GRACHYOV', 'TEST TEST TEST TEST', null, 25, null, 'г.Алматы, альтернатива (ул. Тажибаева 184 (угол ул. Березовского)) для демонтируемого сайта 01830XTAZHIPIPE по адресу: ул. Тажибаева 155a, pipe.  M',                   --TARGET_COVERAGE 6, 3, 2, TO_DATE('2013-08-09 09:25:55', 'YYYY-MM-DD HH24:MI:SS'), 60623, '1', 3, null, 61, null, null);";
//            PreparedStatement companiesPreparedStatement = c.prepareStatement(companiesInsert);

//            Long gzSupplierGeneratedId = null;
//            int i = 1;
//
//            companiesPreparedStatement.setString(i++, okedCode);
//            companiesPreparedStatement.setBoolean(i++, (result.hasProp("parent") && result.prop("parent").hasProp("has_parent") && result.prop("parent").prop("has_parent") != null ? result.prop("parent").prop("has_parent").boolValue() : null));
//            companiesPreparedStatement.setString(i++, (result.hasProp("parent") && result.prop("parent").hasProp("parent_bin") && result.prop("parent").prop("parent_bin") != null ? result.prop("parent").prop("parent_bin").stringValue() : null));
//            companiesPreparedStatement.setString(i++, (result.hasProp("parent") && result.prop("parent").hasProp("parent_rnn") && result.prop("parent").prop("parent_rnn") != null ? result.prop("parent").prop("parent_rnn").stringValue() : null));
//            companiesPreparedStatement.setString(i++, (result.hasProp("parent") && result.prop("parent").hasProp("parent_name") && result.prop("parent").prop("parent_name") != null ? result.prop("parent").prop("parent_name").stringValue() : null));
//            companiesPreparedStatement.setBoolean(i++, (result.hasProp("bankrupt") && result.prop("bankrupt").hasProp("is_unreliable") && result.prop("bankrupt").prop("is_unreliable") != null ? result.prop("bankrupt").prop("is_unreliable").boolValue() : null));
//            companiesPreparedStatement.setString(i++, (result.hasProp("bankrupt") && result.prop("bankrupt").hasProp("document_date") && result.prop("bankrupt").prop("document_date") != null ? result.prop("bankrupt").prop("document_date").stringValue() : null));
//            companiesPreparedStatement.setString(i++, (result.hasProp("bankrupt") && result.prop("bankrupt").hasProp("document_number") && result.prop("bankrupt").prop("document_number") != null ? result.prop("bankrupt").prop("document_number").stringValue() : null));
//            companiesPreparedStatement.setBoolean(i++, (result.hasProp("inactive") && result.prop("inactive").hasProp("is_unreliable") && result.prop("inactive").prop("is_unreliable") != null ? result.prop("inactive").prop("is_unreliable").boolValue() : null));
//            companiesPreparedStatement.setString(i++, (result.hasProp("inactive") && result.prop("inactive").hasProp("document_date") && result.prop("inactive").prop("document_date") != null ? result.prop("inactive").prop("document_date").stringValue() : null));
//            companiesPreparedStatement.setString(i++, (result.hasProp("inactive") && result.prop("inactive").hasProp("document_number") && result.prop("inactive").prop("document_number") != null ? result.prop("inactive").prop("document_number").stringValue() : null));
//            companiesPreparedStatement.setBoolean(i++, (result.hasProp("wrong_address") && result.prop("wrong_address").hasProp("is_unreliable") && result.prop("wrong_address").prop("is_unreliable") != null ? result.prop("wrong_address").prop("is_unreliable").boolValue() : null));
//            companiesPreparedStatement.setString(i++, (result.hasProp("wrong_address") && result.prop("wrong_address").hasProp("document_date") && result.prop("wrong_address").prop("document_date") != null ? result.prop("wrong_address").prop("document_date").stringValue() : null));
//            companiesPreparedStatement.setString(i++, (result.hasProp("wrong_address") && result.prop("wrong_address").hasProp("document_number") && result.prop("wrong_address").prop("document_number") != null ? result.prop("wrong_address").prop("document_number").stringValue() : null));
//            companiesPreparedStatement.setBoolean(i++, (result.hasProp("pseudo_company") && result.prop("pseudo_company").hasProp("is_unreliable") && result.prop("pseudo_company").prop("is_unreliable") != null ? result.prop("pseudo_company").prop("is_unreliable").boolValue() : null));
//            companiesPreparedStatement.setString(i++, (result.hasProp("pseudo_company") && result.prop("pseudo_company").hasProp("document_date") && result.prop("pseudo_company").prop("document_date") != null ? result.prop("pseudo_company").prop("document_date").stringValue() : null));
//            companiesPreparedStatement.setString(i++, (result.hasProp("pseudo_company") && result.prop("pseudo_company").hasProp("document_number") && result.prop("pseudo_company").prop("document_number") != null ? result.prop("pseudo_company").prop("document_number").stringValue() : null));
//            companiesPreparedStatement.setBoolean(i++, (result.hasProp("invalid_registration") && result.prop("invalid_registration").hasProp("is_unreliable") && result.prop("invalid_registration").prop("is_unreliable") != null ? result.prop("invalid_registration").prop("is_unreliable").boolValue() : null));
//            companiesPreparedStatement.setString(i++, (result.hasProp("invalid_registration") && result.prop("invalid_registration").hasProp("document_date") && result.prop("invalid_registration").prop("document_date") != null ? result.prop("invalid_registration").prop("document_date").stringValue() : null));
//            companiesPreparedStatement.setString(i++, (result.hasProp("invalid_registration") && result.prop("invalid_registration").hasProp("document_number") && result.prop("invalid_registration").prop("document_number") != null ? result.prop("invalid_registration").prop("document_number").stringValue() : null));
//            companiesPreparedStatement.setBoolean(i++, (result.hasProp("vat") && result.prop("vat").hasProp("is_vat_payer") && result.prop("vat").prop("is_vat_payer") != null ? result.prop("vat").prop("is_vat_payer").boolValue() : null));
//            companiesPreparedStatement.setString(i++, (result.hasProp("vat") && result.prop("vat").hasProp("registration_date") && result.prop("vat").prop("registration_date") != null ? result.prop("vat").prop("registration_date").stringValue() : null));
//            companiesPreparedStatement.setString(i++, (result.hasProp("vat") && result.prop("vat").hasProp("unregistration_date") && result.prop("vat").prop("unregistration_date") != null ? result.prop("vat").prop("unregistration_date").stringValue() : null));
//            companiesPreparedStatement.setString(i++, (result.hasProp("vat") && result.prop("vat").hasProp("unregistration_reason") && result.prop("vat").prop("unregistration_reason") != null ? result.prop("vat").prop("unregistration_reason").stringValue() : null));
//            companiesPreparedStatement.setBoolean(i++, (result.hasProp("gz_rnu") && result.prop("gz_rnu").hasProp("is_in_rnu") && result.prop("gz_rnu").prop("is_in_rnu") != null ? result.prop("gz_rnu").prop("is_in_rnu").boolValue() : null));
//            companiesPreparedStatement.setString(i++, (result.hasProp("gz_rnu") && result.prop("gz_rnu").hasProp("count") && result.prop("gz_rnu").prop("count") != null ? result.prop("gz_rnu").prop("count").stringValue() : null));
//            companiesPreparedStatement.setString(i++, (result.hasProp("gz_contract") && result.prop("gz_contract").hasProp("total_sum") && result.prop("gz_contract").prop("total_sum") != null ? result.prop("gz_contract").prop("total_sum").stringValue() : null));
//            companiesPreparedStatement.setString(i++, (result.hasProp("gz_contract") && result.prop("gz_contract").hasProp("count") && result.prop("gz_contract").prop("count") != null ? result.prop("gz_contract").prop("count").stringValue() : null));
//            companiesPreparedStatement.setBoolean(i++, (result.hasProp("elicense") && result.prop("elicense").hasProp("has_elicense") && result.prop("elicense").prop("has_elicense") != null ? result.prop("elicense").prop("has_elicense").boolValue() : null));
//            companiesPreparedStatement.setString(i++, (result.hasProp("tax_debt") && result.prop("tax_debt").hasProp("total_arrear") && result.prop("tax_debt").prop("total_arrear") != null ? result.prop("tax_debt").prop("total_arrear").stringValue() : null));
//            companiesPreparedStatement.setBoolean(i++, (result.hasProp("enforcement_debt") && result.prop("enforcement_debt").hasProp("has_debts") && result.prop("enforcement_debt").prop("has_debts") != null ? result.prop("enforcement_debt").prop("has_debts").boolValue() : null));
//            companiesPreparedStatement.setString(i++, (result.hasProp("enforcement_debt") && result.prop("enforcement_debt").hasProp("total_sum") && result.prop("enforcement_debt").prop("total_sum") != null ? result.prop("enforcement_debt").prop("total_sum").stringValue() : null));
//            companiesPreparedStatement.setString(i++, (result.hasProp("enforcement_debt") && result.prop("enforcement_debt").hasProp("count") && result.prop("enforcement_debt").prop("count") != null ? result.prop("enforcement_debt").prop("count").stringValue() : null));
//            companiesPreparedStatement.setLong(i++, gzSupplierGeneratedId);
//            companiesPreparedStatement.setLong(i++, headGeneratedId);
//            companiesPreparedStatement.setLong(i++, taxGeneratedId);

//            companiesPreparedStatement.executeUpdate();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
    }
}
