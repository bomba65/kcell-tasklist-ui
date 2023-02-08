package kz.kcell.flow.assets.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AddressCityDto {
    public Long id;
    public String name;
    public String kato;
    public CatalogDto cityTypeId;
    public AddressDistrictDto districtId;
}
