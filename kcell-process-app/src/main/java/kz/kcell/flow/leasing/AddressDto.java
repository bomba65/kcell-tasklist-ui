package kz.kcell.flow.leasing;

public class AddressDto {

    private CatalogDto city_id;
    private String street;
    private String building;
    private String cadastral_number;
    private String note;
    private CatalogDto region_id;

    public CatalogDto getCity_id() {
        return city_id;
    }

    public void setCity_id(CatalogDto city_id) {
        this.city_id = city_id;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getCadastral_number() {
        return cadastral_number;
    }

    public void setCadastral_number(String cadastral_number) {
        this.cadastral_number = cadastral_number;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public CatalogDto getRegion_id() {
        return region_id;
    }

    public void setRegion_id(CatalogDto region_id) {
        this.region_id = region_id;
    }
}
