package kz.kcell.flow.assets.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class CatalogObjectDto {

    @NotNull(message = "Please provide catalog_id")
    public Long catalog_id;

    @NotNull(message = "Please provide catalog selected id")
    public Long id;
}
