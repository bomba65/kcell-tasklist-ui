package kz.kcell.kwms;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import kz.kcell.kwms.model.*;
import kz.kcell.kwms.repository.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Log
public class DataInitializer {

    final
    @NonNull
    SiteRepository siteRepository;

    final
    @NonNull
    FacilityInstanceRepository facilityInstanceRepository;

    final
    @NonNull
    FacilityDefinitionRepository facilityDefinitionRepository;

    final
    @NonNull
    EquipmentDefinitionRepository equipmentDefinitionRepository;

    final
    @NonNull
    EquipmentInstanceRepository equipmentInstanceRepository;

    final
    @NonNull
    InstallationDefinitionRepository installationDefinitionRepository;

    final
    @NonNull
    InstallationInstanceRepository installationInstanceRepository;

    final
    @NonNull
    ConnectionDefinitionRepository connectionDefinitionRepository;

    final
    @NonNull
    ConnectionInstanceRepository connectionInstanceRepository;

    final
    @NonNull
    PowerSourceRepository powerSourceRepository;

    @EventListener
    public void init(ApplicationReadyEvent event) throws ParseException {

        if (siteRepository.count() != 0) {
            log.info("Database not empty, skip initialization");
            return;
        }

        log.info("Start initialization");

        WKTReader wktReader = new WKTReader();

        FacilityDefinition facilityDefinitionBUILDING = facilityDefinitionRepository.save(FacilityDefinition.builder()
                .id("BUILDING")
                .schema("{}")
                .name("Building")
                .build());

        facilityDefinitionRepository.save(FacilityDefinition.builder()
                .id("TOWER")
                .schema("{}")
                .name("Tower")
                .build());

        FacilityDefinition facilityDefinitionMAST = facilityDefinitionRepository.save(FacilityDefinition.builder()
                .id("MAST")
                .schema("{}")
                .name("Mast")
                .build());

        facilityDefinitionRepository.save(FacilityDefinition.builder()
                .id("CONCRETE_BASE")
                .schema("{}")
                .name("Concrete Base")
                .build());

        facilityDefinitionRepository.save(FacilityDefinition.builder()
                .id("CONTAINER")
                .schema("{}")
                .name("Container")
                .build());

        FacilityInstance facilityInstanceBUILDING = facilityInstanceRepository.save(FacilityInstance.builder()
                .definition(facilityDefinitionBUILDING)
                .location((Point) wktReader.read("POINT(-10.0 10.0 10.0)"))
                .params("{\"name\":\"Near shop 'Nurlan'\",\"region\":\"Almaty\",\"city\":\"Almaty\",\"street\":\"Furmanova\",\"building_number\":127,\"cadastral_number\":\"01-2032-032832\",\"latitude\":\"43.252540\",\"longitude\":\"76.946774\",\"altitude\":911,\"owner\":\"Kcell\",\"height\":43,\"max_neighbor_height\":20,\"building_type\":\"Administrative\",\"roof_type\":\"Профнастил\",\"roof_degrees\":13,\"ceiling_type\":\"железобетон\",\"has_technical_floor\":\"No\",\"additional_info\":\"г. Алматы, Фурманова 127 над магазином 'Нурлан'\", \"contacts\":[]}")
                .build());

        FacilityInstance facilityInstanceMAST = facilityInstanceRepository.save(FacilityInstance.builder()
                .definition(facilityDefinitionMAST)
                .location((Point) wktReader.read("POINT(-10.0 10.0 10.0)"))
                .params("{\"name\":\"Near apteka 'Tair'\",\"region\":\"Almaty\",\"city\":\"Almaty\",\"street\":\"Furmanova\",\"building_number\":127,\"cadastral_number\":\"01-2032-032832\",\"latitude\":\"43.252540\",\"longitude\":\"76.946774\",\"altitude\":911,\"owner\":\"Kcell\",\"height\":43,\"max_neighbor_height\":20,\"building_type\":\"Administrative\",\"roof_type\":\"Профнастил\",\"roof_degrees\":13,\"ceiling_type\":\"железобетон\",\"has_technical_floor\":\"No\",\"additional_info\":\"г. Алматы, Фурманова 127 над магазином 'Нурлан'\", \"contacts\":[]}")
                .build());

        Site site1 = siteRepository.save(Site.builder()
                .name("00SITE1")
                .params("{\"site_name\":\"00SITE1\"}")
                .facilities(Stream.of(facilityInstanceBUILDING).collect(Collectors.toCollection(TreeSet::new)))
                .build());

        for (int i = 2; i <= 10; i++) {
            FacilityInstance facilityInstance = facilityInstanceRepository.save(FacilityInstance.builder()
                    .definition(facilityDefinitionBUILDING)
                    .location((Point) wktReader.read("POINT(-10.0 10.0 10.0)"))
                    .params("{\"name\":\"Near shop 'Nurlan'\",\"region\":\"South\",\"city\":\"Almaty\",\"street\":\"Furmanova\",\"building_number\":127,\"cadastral_number\":\"01-2032-032832\",\"latitude\":\"43.252540\",\"longitude\":\"76.946774\",\"altitude\":911,\"owner\":\"Kcell\",\"height\":43,\"max_neighbor_height\":20,\"building_type\":\"Administrative\",\"roof_type\":\"Профнастил\",\"roof_degrees\":13,\"ceiling_type\":\"железобетон\",\"has_technical_floor\":\"No\",\"additional_info\":\"г. Алматы, Фурманова 127 над магазином 'Нурлан'\", \"contacts\":[]}")
                    .build());
            siteRepository.save(
                    Site.builder()
                            .name((i % 9) + "0SITE" + i)
                            .params("{\"site_name\":\"" + (i % 9) + "0SITE" + i + "\"}")
                            .facilities(Stream.of(facilityInstance).collect(Collectors.toCollection(TreeSet::new)))
                            .build()
            );
        }

        EquipmentDefinition equipmentDefinitionAIRCONDITIONER = equipmentDefinitionRepository.save(EquipmentDefinition.builder()
                .id("AIRCONDITIONER")
                .name("Air conditioner")
                .schema("{}")
                .build());

        EquipmentDefinition equipmentDefinitionMICROWAVE = equipmentDefinitionRepository.save(EquipmentDefinition.builder()
                .id("MICROWAVE")
                .name("Microwave antenna")
                .schema("{}")
                .build());

        EquipmentDefinition equipmentDefinitionCABINET = equipmentDefinitionRepository.save(EquipmentDefinition.builder()
                .id("CABINET")
                .name("Cabinets to BSC/RNC Connection")
                .schema("{}")
                .build());

        EquipmentDefinition equipmentDefinitionRU = equipmentDefinitionRepository.save(EquipmentDefinition.builder()
                .id("RU")
                .name("Radio Unit")
                .schema("{}")
                .build());

        EquipmentDefinition equipmentDefinitionSUPPLEMENTARY = equipmentDefinitionRepository.save(EquipmentDefinition.builder()
                .id("SUPPLEMENTARY")
                .name("Supplementary equipment")
                .schema("{}")
                .build());

        EquipmentDefinition equipmentDefinitionANTENNA = equipmentDefinitionRepository.save(EquipmentDefinition.builder()
                .id("ANTENNA")
                .name("Supplementary equipment")
                .schema("{}")
                .build());

        EquipmentDefinition equipmentDefinitionDU = equipmentDefinitionRepository.save(EquipmentDefinition.builder()
                .id("DU")
                .name("Digital Unit")
                .schema("{}")
                .build());

        EquipmentDefinition equipmentDefinitionAU = equipmentDefinitionRepository.save(EquipmentDefinition.builder()
                .id("AU")
                .name("Hop Antenna Unit")
                .schema("{}")
                .build());

        EquipmentDefinition equipmentDefinitionOU = equipmentDefinitionRepository.save(EquipmentDefinition.builder()
                .id("OU")
                .name("Hop Outdoor Unit")
                .schema("{}")
                .build());

        EquipmentDefinition equipmentDefinitionIU = equipmentDefinitionRepository.save(EquipmentDefinition.builder()
                .id("IU")
                .name("Hop Indoor Unit")
                .schema("{}")
                .build());

        EquipmentDefinition equipmentDefinitionANTENNALINK = equipmentDefinitionRepository.save(EquipmentDefinition.builder()
                .id("ANTENNA_LINK")
                .name("Transmission Microwave antenna link")
                .schema("{}")
                .build());

        EquipmentDefinition equipmentDefinitionANTENNACONSTRUCTION = equipmentDefinitionRepository.save(EquipmentDefinition.builder()
                .id("ANTENNA_CONSTRUCTION")
                .name("Transmission Microwave antenna construction")
                .schema("{}")
                .build());

        EquipmentInstance equipmentInstanceAIRCONDITIONER = equipmentInstanceRepository.save(EquipmentInstance.builder()
                .sn("1000")
                .definition(equipmentDefinitionAIRCONDITIONER)
                .params("{}")
                .build());

        EquipmentInstance equipmentInstanceMICROWAVE = equipmentInstanceRepository.save(EquipmentInstance.builder()
                .sn("2000")
                .definition(equipmentDefinitionMICROWAVE)
                .params("{}")
                .build());

        EquipmentInstance equipmentInstanceCABINET = equipmentInstanceRepository.save(EquipmentInstance.builder()
                .sn("444rrrwer")
                .definition(equipmentDefinitionCABINET)
                .params("{\"bsc\": \"AlmBSC8\",\"rnc\":\"AlmRNC5\",\"company_vendor_id\":\"Ericsson\",\"cabinet_type\": \"2216\",\"voltage\": \"220\"}")
                .build());

        EquipmentInstance equipmentInstanceRU = equipmentInstanceRepository.save(EquipmentInstance.builder()
                .sn("2001")
                .definition(equipmentDefinitionRU)
                .params("{\"type\":\"RRU\",\"serial_number\":\"SN23782713FY\",\"sector\":\"A\",\"band\":\"800\",\"rrus_construction_type\":\"RRU\",\"trx_quantity\":5,\"carrier_quantity\":6,\"voltage\":\"220\",\"rat\":{\"2G\":true,\"3G\":true,\"WiMAX\":true}}")
                .build());

        EquipmentInstance equipmentInstanceSUPPLEMENTARY = equipmentInstanceRepository.save(EquipmentInstance.builder()
                .sn("2001")
                .definition(equipmentDefinitionSUPPLEMENTARY)
                .params("{\"type\":\"Air conditioner\",\"vendor\":\"Samsung\",\"serial_number\":\"SN89217821KZ\",\"location_type\":\"indoor\",\"voltage\":\"220\"}")
                .build());

        EquipmentInstance equipmentInstanceANTENNA = equipmentInstanceRepository.save(EquipmentInstance.builder()
                .sn("2001")
                .definition(equipmentDefinitionANTENNA)
                .params("{\"rat\":{\"2G\":true,\"3G\":true},\"sector\":\"A\",\"model\":\"23466XYZ\",\"serial_number\":\"SN18238213KZ\",\"duplex_filter\":\"Yes\",\"hcu\":\"Yes\",\"power_splitter\":\"Yes\",\"tma\":\"Yes\",\"extended_range\":\"Yes\",\"retu\":\"Yes\",\"beam_width\":\"214\",\"gain\":\"421\",\"max_wind_velocity\":\"4124\",\"weight\":\"412421\",\"length_of_cable\":\"23\"}")
                .build());

        EquipmentInstance equipmentInstanceDU = equipmentInstanceRepository.save(EquipmentInstance.builder()
                .sn("2001")
                .definition(equipmentDefinitionDU)
                .params("{\"type\":\"DUS3101\",\"serial_number\":\"SN217832UIRWWW73\",\"rat\":{\"2G\":true,\"WiMAX\":true,\"3G\":true,\"LTE\":true}}")
                .build());

        InstallationDefinition installationDefinitionAIRCONDITIONER = installationDefinitionRepository.save(InstallationDefinition.builder()
                .id("AIRCONDITIONER")
                .name("Air Conditioner Installation")
                .schema("{}")
                .build());

        InstallationInstance installationInstanceAIRCONDITIONER = installationInstanceRepository.save(InstallationInstance.builder()
                .definition(installationDefinitionAIRCONDITIONER)
                .equipment(equipmentInstanceAIRCONDITIONER)
                .facility(facilityInstanceBUILDING)
                .site(site1)
                .params("{}")
                .build());

        InstallationDefinition installationDefinitionMICROWAVE = installationDefinitionRepository.save(InstallationDefinition.builder()
                .id("MICROWAVE")
                .name("Microwave Installation")
                .schema("{}")
                .build());

        InstallationInstance installationInstanceMICROWAVE = installationInstanceRepository.save(InstallationInstance.builder()
                .definition(installationDefinitionMICROWAVE)
                .equipment(equipmentInstanceMICROWAVE)
                .facility(facilityInstanceBUILDING)
                .site(site1)
                .params("{}")
                .build());

        InstallationDefinition installationDefinitionCABINET = installationDefinitionRepository.save(InstallationDefinition.builder()
                .id("CABINET")
                .name("Cabinet Installation")
                .schema("{}")
                .build());

        InstallationInstance installationInstanceCABINET = installationInstanceRepository.save(InstallationInstance.builder()
                .definition(installationDefinitionCABINET)
                .equipment(equipmentInstanceCABINET)
                .facility(facilityInstanceBUILDING)
                .site(site1)
                .params("{\"facility\":\"BUILDING\",\"construction_type\":\"indoor\"}")
                .build());

        InstallationDefinition installationDefinitionRU = installationDefinitionRepository.save(InstallationDefinition.builder()
                .id("RU")
                .name("RU Installation")
                .schema("{}")
                .build());

        InstallationInstance installationInstanceRU = installationInstanceRepository.save(InstallationInstance.builder()
                .definition(installationDefinitionRU)
                .equipment(equipmentInstanceRU)
                .facility(facilityInstanceBUILDING)
                .site(site1)
                .params("{\"facility\":\"BUILDING\",\"rbs_number\":\"" + installationInstanceCABINET.getId() + "\"}")
                .build());

        InstallationDefinition installationDefinitionSUPPLEMENTARY = installationDefinitionRepository.save(InstallationDefinition.builder()
                .id("SUPPLEMENTARY")
                .name("Supplementary Installation")
                .schema("{}")
                .build());

        InstallationInstance installationInstanceSUPPLEMENTARY = installationInstanceRepository.save(InstallationInstance.builder()
                .definition(installationDefinitionSUPPLEMENTARY)
                .equipment(equipmentInstanceSUPPLEMENTARY)
                .facility(facilityInstanceBUILDING)
                .site(site1)
                .params("{\"facility\": \"BUILDING\"}")
                .build());

        InstallationDefinition installationDefinitionANTENNA = installationDefinitionRepository.save(InstallationDefinition.builder()
                .id("ANTENNA")
                .name("Antenna system")
                .schema("{}")
                .build());

        InstallationInstance installationInstanceANTENNA = installationInstanceRepository.save(InstallationInstance.builder()
                .definition(installationDefinitionANTENNA)
                .equipment(equipmentInstanceANTENNA)
                .facility(facilityInstanceBUILDING)
                .site(site1)
                .params("{\"facility\":\"BUILDING\",\"height_in_top\":3,\"distance_from_roof_level\":3,\"min_horiz_distance_from_roof_edge\":\"4\",\"height_of_phasecenter_from_grnd_lvl\":\"2\",\"direction\":\"4\",\"m_tilt_value\":\"4\",\"e_tilt_value\":\"4\",\"location_type\":\"Roof\",\"placement_type\":\"Frontal\",\"feeder_type\":\"8/9\",\"number_of_feeders\":\"4\",\"free_space_for_addition_rru\":\"Yes\",\"carrying_capacity_for_additional_rru\":\"Yes\",\"diversity\":\"Yes\",\"rbs_number\":\"" + installationInstanceCABINET.getId() + "\"}")
                .build());

        InstallationDefinition installationDefinitionDU = installationDefinitionRepository.save(InstallationDefinition.builder()
                .id("DU")
                .name("DU Installation")
                .schema("{}")
                .build());

        InstallationDefinition installationDefinitionIU = installationDefinitionRepository.save(InstallationDefinition.builder()
                .id("IU")
                .name("Indoor Unit Installation")
                .schema("{}")
                .build());

        InstallationDefinition installationDefinitionOU = installationDefinitionRepository.save(InstallationDefinition.builder()
                .id("OU")
                .name("Outdoor Unit Installation")
                .schema("{}")
                .build());

        InstallationDefinition installationDefinitionAU = installationDefinitionRepository.save(InstallationDefinition.builder()
                .id("AU")
                .name("Antenna Unit Installation")
                .schema("{}")
                .build());

        InstallationDefinition installationDefinitionANTENNALINK = installationDefinitionRepository.save(InstallationDefinition.builder()
                .id("ANTENNA_LINK")
                .name("Antenna Link Installation")
                .schema("{}")
                .build());

        InstallationDefinition installationDefinitionANTENNACONSTRUCTION = installationDefinitionRepository.save(InstallationDefinition.builder()
                .id("ANTENNA_CONSTRUCTION")
                .name("Antenna CONSTRUCTION Installation")
                .schema("{}")
                .build());

        InstallationInstance installationInstanceDU = installationInstanceRepository.save(InstallationInstance.builder()
                .definition(installationDefinitionDU)
                .equipment(equipmentInstanceDU)
                .facility(facilityInstanceBUILDING)
                .site(site1)
                .params("{\"facility\": \"BUILDING\",\"rbs_number\": \"" + installationInstanceCABINET.getId() + "\"}")
                .build());

        ConnectionDefinition connectionDefinitionRU2RBS = connectionDefinitionRepository.save(
                ConnectionDefinition.builder()
                        .id("RU2RBS")
                        .name("Radio Unit to Cabinet")
                        .schema("{}")
                        .build()
        );

        ConnectionDefinition connectionDefinitionDU2RBS = connectionDefinitionRepository.save(
                ConnectionDefinition.builder()
                        .id("DU2RBS")
                        .name("Radio Unit to Cabinet")
                        .schema("{}")
                        .build()
        );

        ConnectionDefinition connectionDefinitionANTENNA2RBS = connectionDefinitionRepository.save(
                ConnectionDefinition.builder()
                        .id("ANTENNA2RBS")
                        .name("Radio Unit to Cabinet")
                        .schema("{}")
                        .build()
        );

        ConnectionDefinition connectionDefinitionTRANSMISSION_RADIO_RELAY = connectionDefinitionRepository.save(
                ConnectionDefinition.builder()
                        .id("TRANSMISSION_RADIO_RELAY")
                        .name("TRANSMISSION_RADIO_RELAY")
                        .schema("{}")
                        .build()
        );

        ConnectionInstance connectionInstanceRU2RBS = connectionInstanceRepository.save(
                ConnectionInstance.builder()
                        .definition(connectionDefinitionRU2RBS)
                        .equipments(Stream.of(equipmentInstanceRU, equipmentInstanceCABINET).collect(Collectors.toCollection(TreeSet::new)))
                        .params("{}")
                        .build()
        );

        PowerSource powerSource1 = powerSourceRepository.save(PowerSource.builder()
                .site(site1)
                .params("{\"object_for_supply\":\"Container\",\"necessary_qty_of_phases\":\"2\",\"supplier\":\"Rented\",\"necessary_level\":10,\"need_tech_conditions\":\"Yes\",\"need_transformer\":\"No\",\"transformer_owner\":\"RES\",\"voltage_quality\":\"low\",\"connection_point_cable_length\":32,\"cable_way\":{\"type\":\"Under ground\",\"cable_run_ladders_length\":4,\"space_for_opticcable_in_cable_run_ladder\":\"Yes\",\"space_for_opticcable_in_metalhose\":\"Yes\",\"need_to_lay_opticcable_in_fiberclamps\":\"Yes\",\"qty_of_separated_pipes_for_antennas\":42},\"is_cable_runladders_conn_btw_electr_circuit\":\"Yes\",\"is_cable_runladders_conn_to_exist_lighting_sys\":\"Yes\",\"need_addtional_work_and_materials\":\"No\",\"additional_work_desc\":\"DESCRIPTION OF\",\"dc_power_system_model\":\"Emerson\",\"rru_circuit_breakers_25\":2,\"rru_circuit_breakers_32\":3,\"batteries_qty\":4,\"batteries_model\":\"GFM\",\"each_rectifier_module_power\":1233,\"qty_of_rectifiers\":42,\"free_space_for_dcpd\":\"Yes\",\"cable_type\":\"ВВГ\",\"cable_cross_section\":\"4x10\",\"grounding_sys\":{\"is_serviceable\":\"Yes\"},\"lightning_sys\":{\"is_serviceable\":\"Yes\"},\"disel_generator\":{\"model\":\"Aksa\",\"voltage\":\"220\",\"capacity\":4},\"oil_tank\":{\"type\":\"DIESEL\",\"volume\":424,\"location_type\":\"underground\"},\"electricity_counter\":{\"condition\":\"working\",\"need_upgrade\":\"Yes\",\"need_add_work\":\"Yes\",\"location\":\"в подвальном помещении\",\"cable_information\":\"Info\",\"location_place\":\"outside of our room or container\"},\"cable_ways\":[{\"cable_length\":2,\"free_space_for_addition_rru\":\"Yes\",\"carrying_capacity_metal_construction\":\"No\"},{\"cable_length\":4,\"free_space_for_addition_rru\":\"No\",\"carrying_capacity_metal_construction\":\"Yes\"},{\"cable_length\":4,\"free_space_for_addition_rru\":\"Yes\",\"carrying_capacity_metal_construction\":\"No\"}]}")
                .build());
        powerSourceRepository.save(Arrays.asList(powerSource1));

        log.info("End initialization");
    }

}
