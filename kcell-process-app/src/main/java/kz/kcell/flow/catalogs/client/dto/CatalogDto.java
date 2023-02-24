package kz.kcell.flow.catalogs.client.dto;

import lombok.Data;

@Data
public class CatalogDto {
private String[] processIds;
    private long createdAt;
    private CatalogDataDto data;
    private String[] writerUserIds;
    private String[] readerUserIds;
    private int id;
    private String name;
    private String userId;
}
