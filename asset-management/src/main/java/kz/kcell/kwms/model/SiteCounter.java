package kz.kcell.kwms.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "site_counter")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteCounter {

    @Id
    Long id;

    @Column(columnDefinition = "int default 1")
    int value;

    @MapsId
    @OneToOne
    @JoinColumn(name = "id")
    Site site;
}
