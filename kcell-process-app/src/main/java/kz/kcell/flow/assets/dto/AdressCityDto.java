package kz.kcell.flow.assets.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdressCityDto {
    public Long id;
    public String name;
    public String kato;
    public CatalogDto city_type_id;
    public AdressDistrictDto district_id;
}
