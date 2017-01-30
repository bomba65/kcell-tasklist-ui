package kz.kcell.kwms;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import kz.kcell.kwms.model.EquipmentDefinition;
import kz.kcell.kwms.model.EquipmentInstance;
import kz.kcell.kwms.model.Facility;
import kz.kcell.kwms.model.Site;
import kz.kcell.kwms.repository.EquipmentDefinitionRepository;
import kz.kcell.kwms.repository.EquipmentInstanceRepository;
import kz.kcell.kwms.repository.FacilityRepository;
import kz.kcell.kwms.repository.SiteRepository;
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
    SiteRepository sites;

    final @NonNull
    FacilityRepository facilities;

    final @NonNull
    EquipmentDefinitionRepository definitions;

    final @NonNull
    EquipmentInstanceRepository instances;

    @EventListener
    public void init(ApplicationReadyEvent event) throws ParseException {

        if (sites.count() != 0) {
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

        sites.save(Arrays.asList(site1, site2));

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

        facilities.save(Arrays.asList(facility1, facility2));

        EquipmentDefinition definition1 = EquipmentDefinition.builder()
                .name("Air conditioner")
                .gtin("12345")
                .vendor("Samsung")
                .schema("{}")
                .build();

        EquipmentInstance instance1 = EquipmentInstance.builder()
                .sn("1000")
                .definition(definition1)
                .params("{}")
                .build();

        EquipmentInstance instance2 = EquipmentInstance.builder()
                .sn("2000")
                .definition(definition1)
                .params("{}")
                .build();

        definitions.save(definition1);
        instances.save(Arrays.asList(instance1, instance2));

    }
}