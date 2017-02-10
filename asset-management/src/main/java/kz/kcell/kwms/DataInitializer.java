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
                .facilities(Stream.of(facilityInstance1).collect(Collectors.toSet()))
                .build();

        Site site2 = Site.builder()
                .id("SITE2")
                .name("Site 2")
                .facilities(Stream.of(facilityInstance2).collect(Collectors.toSet()))
                .build();

        facilityInstanceRepository.save(Arrays.asList(facilityInstance1, facilityInstance2));
        siteRepository.save(Arrays.asList(site1, site2));


        EquipmentDefinition equipmentDefinition1 = EquipmentDefinition.builder()
                .name("Air conditioner")
                .gtin("12345")
                .vendor("Samsung")
                .schema("{}")
                .build();

        EquipmentDefinition equipmentDefinition2 = EquipmentDefinition.builder()
                .name("Microwave antenna")
                .gtin("23456")
                .vendor("Ericsson")
                .schema("{}")
                .build();

        EquipmentDefinition equipmentDefinition3 = EquipmentDefinition.builder()
                .name("Cabinets to BSC/RNC Connection")
                .gtin("444rrrwer")
                .vendor("Ericsson")
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

        equipmentDefinitionRepository.save(Arrays.asList(equipmentDefinition1, equipmentDefinition2, equipmentDefinition3));
        equipmentInstanceRepository.save(Arrays.asList(equipmentInstance1, equipmentInstance2, equipmentInstance3));

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
                .params("{}")
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

        installationDefinitionRepository.save(Arrays.asList(installationDefinition1, installationDefinition2, installationDefinition3));
        installationInstanceRepository.save(Arrays.asList(installationInstance1, installationInstance2, installationInstance3));
    }
}
