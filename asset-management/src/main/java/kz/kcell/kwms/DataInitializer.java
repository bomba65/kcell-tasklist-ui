package kz.kcell.kwms;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import kz.kcell.kwms.model.*;
import kz.kcell.kwms.repository.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
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
    PowerSourceRepository powerSourceRepository;

    @EventListener
    public void init(ApplicationReadyEvent event) throws ParseException {

        if (siteRepository.count() != 0) {
            return;
        }

        WKTReader wktReader = new WKTReader();

        FacilityDefinition facilityDefinition1 = FacilityDefinition.builder()
                .id("BUILDING")
                .schema("{}")
                .name("Building")
                .build();

        FacilityDefinition facilityDefinition2 = FacilityDefinition.builder()
                .id("TOWER")
                .schema("{}")
                .name("Tower")
                .build();

        FacilityDefinition facilityDefinition3 = FacilityDefinition.builder()
                .id("MAST")
                .schema("{}")
                .name("Mast")
                .build();

        facilityDefinitionRepository.save(Arrays.asList(facilityDefinition1, facilityDefinition2, facilityDefinition3));

        FacilityInstance facilityInstance1 = FacilityInstance.builder()
                .definition(facilityDefinition1)
                .location((Point) wktReader.read("POINT(10.0 10.0)"))
                .params("{}")
                .build();

        FacilityInstance facilityInstance2 = FacilityInstance.builder()
                .definition(facilityDefinition1)
                .location((Point) wktReader.read("POINT(-10.0 10.0)"))
                .params("{}")
                .build();

        Site site1 = Site.builder()
                .id("SITE1")
                .name("Site 1")
                .params("{}")
                .facilities(Stream.of(facilityInstance1).collect(Collectors.toSet()))
                .build();

        Site site2 = Site.builder()
                .id("SITE2")
                .name("Site 2")
                .params("{}")
                .facilities(Stream.of(facilityInstance2).collect(Collectors.toSet()))
                .build();

        facilityInstanceRepository.save(Arrays.asList(facilityInstance1, facilityInstance2));
        siteRepository.save(Arrays.asList(site1, site2));


        EquipmentDefinition equipmentDefinition1 = EquipmentDefinition.builder()
                .id("AIRCONDITIONER")
                .name("Air conditioner")
                .schema("{}")
                .build();

        EquipmentDefinition equipmentDefinition2 = EquipmentDefinition.builder()
                .id("MICROWAVE")
                .name("Microwave antenna")
                .schema("{}")
                .build();

        EquipmentDefinition equipmentDefinition3 = EquipmentDefinition.builder()
                .id("CABINET")
                .name("Cabinets to BSC/RNC Connection")
                .schema("{}")
                .build();

        EquipmentDefinition equipmentDefinition4 = EquipmentDefinition.builder()
                .id("RU")
                .name("Radio Unit")
                .schema("{}")
                .build();

        EquipmentDefinition equipmentDefinition5 = EquipmentDefinition.builder()
                .id("SUPPLEMENTARY")
                .name("Supplementary equipment")
                .schema("{}")
                .build();

        EquipmentDefinition equipmentDefinition6 = EquipmentDefinition.builder()
                .id("ANTENNA")
                .name("Supplementary equipment")
                .schema("{}")
                .build();

        EquipmentDefinition equipmentDefinition7 = EquipmentDefinition.builder()
                .id("DU")
                .name("Digital Unit")
                .schema("{}")
                .build();

        EquipmentInstance equipmentInstance1 = EquipmentInstance.builder()
                .sn("1000")
                .definition(equipmentDefinition1)
                .params("{}")
                .build();

        EquipmentInstance equipmentInstance2 = EquipmentInstance.builder()
                .sn("2000")
                .definition(equipmentDefinition2)
                .params("{}")
                .build();

        EquipmentInstance equipmentInstance3 = EquipmentInstance.builder()
                .sn("444rrrwer")
                .definition(equipmentDefinition3)
                .params("{}")
                .build();

        EquipmentInstance equipmentInstance4 = EquipmentInstance.builder()
                .sn("2001")
                .definition(equipmentDefinition2)
                .params("{}")
                .build();

        EquipmentInstance equipmentInstance5 = EquipmentInstance.builder()
                .sn("2001")
                .definition(equipmentDefinition4)
                .params("{}")
                .build();

        EquipmentInstance equipmentInstance6 = EquipmentInstance.builder()
                .sn("2001")
                .definition(equipmentDefinition5)
                .params("{}")
                .build();

        EquipmentInstance equipmentInstance7 = EquipmentInstance.builder()
                .sn("2001")
                .definition(equipmentDefinition6)
                .params("{}")
                .build();

        EquipmentInstance equipmentInstance8 = EquipmentInstance.builder()
                .sn("2001")
                .definition(equipmentDefinition7)
                .params("{}")
                .build();

        equipmentDefinitionRepository.save(Arrays.asList(equipmentDefinition1, equipmentDefinition2, equipmentDefinition3, equipmentDefinition4, equipmentDefinition5, equipmentDefinition6, equipmentDefinition7));
        equipmentInstanceRepository.save(Arrays.asList(equipmentInstance1, equipmentInstance2, equipmentInstance3, equipmentInstance4, equipmentInstance5, equipmentInstance6, equipmentInstance7, equipmentInstance8));

        InstallationDefinition installationDefinition1 = InstallationDefinition.builder()
                .id("AIRCONDITIONER")
                .name("Air Conditioner Installation")
                .schema("{}")
                .build();

        InstallationInstance installationInstance1 = InstallationInstance.builder()
                .definition(installationDefinition1)
                .equipment(equipmentInstance1)
                .facility(facilityInstance1)
                .site(site1)
                .params("{}")
                .build();

        InstallationDefinition installationDefinition2 = InstallationDefinition.builder()
                .id("MICROWAVE")
                .name("Microwave Installation")
                .schema("{}")
                .build();

        InstallationInstance installationInstance2 = InstallationInstance.builder()
                .definition(installationDefinition2)
                .equipment(equipmentInstance2)
                .facility(facilityInstance2)
                .site(site2)
                .params("{\"farEndSites\":[\"SITE2\"]}")
                .build();

        InstallationDefinition installationDefinition3 = InstallationDefinition.builder()
                .id("CABINET")
                .name("Cabinet Installation")
                .schema("{}")
                .build();

        InstallationInstance installationInstance3 = InstallationInstance.builder()
                .definition(installationDefinition3)
                .equipment(equipmentInstance3)
                .facility(facilityInstance2)
                .site(site2)
                .params("{}")
                .build();

        InstallationInstance installationInstance4 = InstallationInstance.builder()
                .definition(installationDefinition2)
                .equipment(equipmentInstance4)
                .facility(facilityInstance1)
                .site(site1)
                .params("{\"farEndSites\":[\"SITE2\"]}")
                .build();

        InstallationDefinition installationDefinition4 = InstallationDefinition.builder()
                .id("RU")
                .name("RU Installation")
                .schema("{}")
                .build();

        InstallationInstance installationInstance5 = InstallationInstance.builder()
                .definition(installationDefinition4)
                .equipment(equipmentInstance5)
                .facility(facilityInstance1)
                .site(site1)
                .params("{}")
                .build();

        InstallationDefinition installationDefinition5 = InstallationDefinition.builder()
                .id("SUPPLEMENTARY")
                .name("Supplementary Installation")
                .schema("{}")
                .build();

        InstallationInstance installationInstance6 = InstallationInstance.builder()
                .definition(installationDefinition5)
                .equipment(equipmentInstance7)
                .facility(facilityInstance1)
                .site(site1)
                .params("{}")
                .build();

        InstallationDefinition installationDefinition6 = InstallationDefinition.builder()
                .id("ANTENNA")
                .name("Antenna system")
                .schema("{}")
                .build();

        InstallationInstance installationInstance7 = InstallationInstance.builder()
                .definition(installationDefinition6)
                .equipment(equipmentInstance6)
                .facility(facilityInstance1)
                .site(site1)
                .params("{}")
                .build();

        InstallationDefinition installationDefinition7 = InstallationDefinition.builder()
                .id("DU")
                .name("DU Installation")
                .schema("{}")
                .build();

        InstallationInstance installationInstance8 = InstallationInstance.builder()
                .definition(installationDefinition7)
                .equipment(equipmentInstance8)
                .facility(facilityInstance1)
                .site(site1)
                .params("{}")
                .build();

        installationDefinitionRepository.save(Arrays.asList(installationDefinition1, installationDefinition2, installationDefinition3, installationDefinition4, installationDefinition5, installationDefinition6, installationDefinition7));
        installationInstanceRepository.save(Arrays.asList(installationInstance1, installationInstance2, installationInstance3, installationInstance4, installationInstance5, installationInstance6, installationInstance7, installationInstance8));

        PowerSource powerSource1 = PowerSource.builder()
                .site(site1)
                .params("{}")
                .build();

        PowerSource powerSource2 = PowerSource.builder()
                .site(site2)
                .params("{}")
                .build();
        powerSourceRepository.save(Arrays.asList(powerSource1, powerSource2));
    }
}
