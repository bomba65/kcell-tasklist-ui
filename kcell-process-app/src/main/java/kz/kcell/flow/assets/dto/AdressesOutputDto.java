package kz.kcell.flow.assets.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AdressesOutputDto {
    public Long id;
    public AddressCityDto cityId; // Village
    public String street;
    public String building;
    public String cadastralNumber;
    public Boolean notFullAddress;
    public String note;
    public String status;
    public Date dateCreated;
    public Map<String, Object> regionId;
}
