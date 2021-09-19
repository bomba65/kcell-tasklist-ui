package kz.kcell.flow.leasing;

import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.camunda.spin.Spin.JSON;

@Log
@Service("UpdateAssets")
public class UpdateAssets implements JavaDelegate {

    @Autowired
    DataSource dataSource;

    @Value("${udb.oracle.url:jdbc:oracle:thin:@//apexudb-pmy:1521/apexudb}")
    private String udbOracleUrl;

    @Value("${udb.oracle.username:udbrnd}")
    private String udbOracleUsername;

    @Value("${udb.oracle.password:udb}")
    private String udbOraclePassword;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        //address
        if (delegateExecution.getVariable("currentAction").toString().equals("update_address")) {
            SpinJsonNode address = delegateExecution.getVariable("address") != null ? JSON(delegateExecution.getVariable("address")) : null;
            AddressDto addressDto = new AddressDto();
            addressDto.setBuilding(address != null && address.hasProp("cn_addr_building") && address.prop("cn_addr_building") != null ? address.prop("cn_addr_building").value().toString() : "");
            addressDto.setStreet(address != null && address.hasProp("cn_addr_street") && address.prop("cn_addr_street") != null ? address.prop("cn_addr_street").value().toString() : "");
            addressDto.setCadastral_number(address != null && address.hasProp("cn_addr_cadastral_number") && address.prop("cn_addr_cadastral_number") != null ? address.prop("cn_addr_cadastral_number").value().toString() : "");
            addressDto.setNote(address != null && address.hasProp("cn_addr_note") && address.prop("cn_addr_note") != null ? address.prop("cn_addr_note").value().toString() : "");
            CatalogDto city_id = new CatalogDto();
            city_id.setId(address != null && address.hasProp("cn_addr_city_id") && address.prop("cn_addr_city_id") != null ? Long.parseLong(address.prop("cn_addr_city_id").value().toString()) : null);
            city_id.setCatalog_id(32L);
            addressDto.setCity_id(city_id);
        }
        //cellAntenna
        if (delegateExecution.getVariable("currentAction").toString().equals("update_cell_antenna")) {
            SpinJsonNode address = delegateExecution.getVariable("cellAntenna") != null ? JSON(delegateExecution.getVariable("cellAntenna")) : null;
            address = address != null && address.hasProp("address") && address.prop("address") != null ? address.prop("address") : null;
            AddressDto addressDto = new AddressDto();
            addressDto.setBuilding(address != null && address.hasProp("cn_addr_building") && address.prop("cn_addr_building") != null ? address.prop("cn_addr_building").value().toString() : "");
            addressDto.setStreet(address != null && address.hasProp("cn_addr_street") && address.prop("cn_addr_street") != null ? address.prop("cn_addr_street").value().toString() : "");
            addressDto.setCadastral_number(address != null && address.hasProp("cn_addr_cadastral_number") && address.prop("cn_addr_cadastral_number") != null ? address.prop("cn_addr_cadastral_number").value().toString() : "");
            addressDto.setNote(address != null && address.hasProp("cn_addr_note") && address.prop("cn_addr_note") != null ? address.prop("cn_addr_note").value().toString() : "");
            CatalogDto city_id = new CatalogDto();
            city_id.setId(address != null && address.hasProp("cn_addr_city_id") && address.prop("cn_addr_city_id") != null ? Long.parseLong(address.prop("cn_addr_city_id").value().toString()) : null);
            city_id.setCatalog_id(32L);
            addressDto.setCity_id(city_id);
        }
        //transmissionAntenna
        if (delegateExecution.getVariable("currentAction").toString().equals("update_transmission_antenna")) {
            SpinJsonNode address = delegateExecution.getVariable("transmissionAntenna") != null ? JSON(delegateExecution.getVariable("transmissionAntenna")) : null;
            address = address != null && address.hasProp("address") && address.prop("address") != null ? address.prop("address") : null;
            AddressDto addressDto = new AddressDto();
            addressDto.setBuilding(address != null && address.hasProp("cn_addr_building") && address.prop("cn_addr_building") != null ? address.prop("cn_addr_building").value().toString() : "");
            addressDto.setStreet(address != null && address.hasProp("cn_addr_street") && address.prop("cn_addr_street") != null ? address.prop("cn_addr_street").value().toString() : "");
            addressDto.setCadastral_number(address != null && address.hasProp("cn_addr_cadastral_number") && address.prop("cn_addr_cadastral_number") != null ? address.prop("cn_addr_cadastral_number").value().toString() : "");
            addressDto.setNote(address != null && address.hasProp("cn_addr_note") && address.prop("cn_addr_note") != null ? address.prop("cn_addr_note").value().toString() : "");
            CatalogDto city_id = new CatalogDto();
            city_id.setId(address != null && address.hasProp("cn_addr_city_id") && address.prop("cn_addr_city_id") != null ? Long.parseLong(address.prop("cn_addr_city_id").value().toString()) : null);
            city_id.setCatalog_id(32L);
            addressDto.setCity_id(city_id);
        }
    }
}
