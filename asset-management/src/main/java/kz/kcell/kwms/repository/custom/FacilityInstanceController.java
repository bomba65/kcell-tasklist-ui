package kz.kcell.kwms.repository.custom;

import kz.kcell.kwms.model.FacilityInstance;
import kz.kcell.kwms.repository.FacilityInstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.rest.webmvc.RepositorySearchesResource;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RepositoryRestController
public class FacilityInstanceController implements ResourceProcessor<RepositorySearchesResource> {

    @Autowired
    private PagedResourcesAssembler<FacilityInstance> assembler;

    @Autowired
    private FacilityInstanceRepository repository;

    @Override
    public RepositorySearchesResource process(RepositorySearchesResource resource) {
        if (FacilityInstance.class.equals(resource.getDomainType())) {
            resource.add(new Link(resource.getId().getHref() + "/findByLocationNear{?long,lat,page,size}", "findByLocationNear"));
        }
        return resource;
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(method = GET, value = "/facilityInstances/search/findByLocationNear")
    public @ResponseBody
    PagedResources<Resource<FacilityInstance>> findByLocationNear(
            @RequestParam("long") double longitude,
            @RequestParam("lat") double latitude,
            @PageableDefault Pageable pageable,
            PersistentEntityResourceAssembler entityAssembler
    ) {
        Page<FacilityInstance> facilities = repository.findByLocationNear(longitude, latitude, pageable);
        return assembler.toResource(facilities, (ResourceAssembler) entityAssembler);
    }

}
