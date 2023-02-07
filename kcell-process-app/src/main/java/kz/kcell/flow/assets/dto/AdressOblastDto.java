package kz.kcell.flow.assets.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdressOblastDto {
    public Long id;
    public String name;
    public String kato;
    public CatalogDto region_id;
}
