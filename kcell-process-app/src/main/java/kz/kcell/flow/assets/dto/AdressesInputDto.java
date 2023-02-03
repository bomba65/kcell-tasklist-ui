package kz.kcell.flow.assets.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;

@Getter
@Setter
public class AdressesInputDto {

    @Valid
    public CatalogObjectDto city_id; // Village
    public Boolean not_full_address;
    public String street;
    public String building;
    public String cadastral_number;
    public String note;
    public CatalogObjectDto region_id;
}
