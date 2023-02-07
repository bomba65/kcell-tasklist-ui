package kz.kcell.flow.assets.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdressDistrictDto {
    public Long id;
    public String name;
    public String kato;
    public AdressOblastDto oblast_id;
}
