package kz.kcell.flow.catalogs.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CatalogDataDto {
    @JsonProperty("$list")
    private List<CatalogListDto> list;
}
