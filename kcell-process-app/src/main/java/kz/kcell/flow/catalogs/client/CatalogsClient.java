package kz.kcell.flow.catalogs.client;

import feign.Param;
import feign.RequestLine;
import kz.kcell.flow.catalogs.client.dto.CatalogDto;

public interface CatalogsClient {

    @RequestLine("GET /camunda/catalogs/api/get/id/{id}")
    CatalogDto getCatalog(@Param("id") int id);
}
