package kz.kcell.flow.assets.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@NoArgsConstructor
public class AdressesOutputDto {
    public Long id;
    public AdressCityDto city_id; // Village
    public String street;
    public String building;
    public String cadastral_number;
    public String note;
    public String status;
    public Date date_created;
    public Map<String, Object> region_id;
}
