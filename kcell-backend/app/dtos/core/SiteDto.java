package dtos.core;

import models.core.Site;

public class SiteDto {

    public String name;
    public String code;

    public SiteDto(Site site) {
        if (site != null) {
            this.name = site.getName();
            this.code = site.getCode();
        }
    }
}
