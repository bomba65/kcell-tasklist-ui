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
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class DataInitializer {

    final @NonNull
    SiteRepository siteRepository;

    final @NonNull
    FacilityRepository facilityRepository;

    final @NonNull
    EquipmentDefinitionRepository equipmentDefinitionRepository;

    final @NonNull
    EquipmentInstanceRepository equipmentInstanceRepository;

    final @NonNull
    InstallationDefinitionRepository installationDefinitionRepository;

    final @NonNull
    InstallationInstanceRepository installationInstanceRepository;

    @EventListener
    public void init(ApplicationReadyEvent event) throws ParseException {

        if (siteRepository.count() != 0) {
            return;
        }

        Site site1 = Site.builder()
                .id("SITE1")
                .name("Site 1")
                .build();

        Site site2 = Site.builder()
                .id("SITE2")
                .name("Site 2")
                .build();

        siteRepository.save(Arrays.asList(site1, site2));

        WKTReader wktReader = new WKTReader();

        Facility facility1 = Facility.builder()
                .name("Facility 1")
                .location((Point)wktReader.read("POINT(10.0 10.0)"))
                .sites(new HashSet<>(Arrays.asList(site1)))
                .build();

        Facility facility2 = Facility.builder()
                .name("Facility 2")
                .location((Point)wktReader.read("POINT(-10.0 10.0)"))
                .sites(new HashSet<>(Arrays.asList(site2)))
                .build();

        facilityRepository.save(Arrays.asList(facility1, facility2));

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

        equipmentDefinitionRepository.save(Arrays.asList(equipmentDefinition1, equipmentDefinition2));
        equipmentInstanceRepository.save(Arrays.asList(equipmentInstance1, equipmentInstance2));

        InstallationDefinition installationDefinition1 = InstallationDefinition.builder()
                .id("AIRCONDITIONER")
                .name("Air Conditioner Installation")
                .schema("{}")
                .build();

        InstallationInstance installationInstance1 = InstallationInstance.builder()
                .definition(installationDefinition1)
                .equipment(equipmentInstance1)
                .facility(facility1)
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
                .facility(facility2)
                .site(site2)
                .params("{}")
                .build();

        installationDefinitionRepository.save(Arrays.asList(installationDefinition1, installationDefinition2));
        installationInstanceRepository.save(Arrays.asList(installationInstance1, installationInstance2));

    }
}
