package kz.kcell.flow.assets.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;

@Getter
@Setter
@Builder
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AddressesInputDto {

    @Valid
    public CatalogObjectDto cityId; // Village
    public Boolean notFullAddress;
    public String street;
    public String building;
    public String cadastralNumber;
    public String note;
    public CatalogObjectDto regionId;
}
