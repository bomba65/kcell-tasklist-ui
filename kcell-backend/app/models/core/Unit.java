package models.core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "A_UNIT")
public class Unit {

    private String value;
    private String description;
    private String dictionaryCategory;


    @Id
    @Column(name = "VALUE_")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Column(name = "DESCRIPTION_")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "DICTIONARY_CATEGORY_")
    public String getDictionaryCategory() {
        return dictionaryCategory;
    }

    public void setDictionaryCategory(String dictionaryCategory) {
        this.dictionaryCategory = dictionaryCategory;
    }
}
