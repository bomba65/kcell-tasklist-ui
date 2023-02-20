package kz.kcell.kwms.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
    name = "vpn_port_counter",
    uniqueConstraints = @UniqueConstraint(columnNames={"year", "channel"})
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VpnPortCounter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    String id;

    @Column
    int value;

    @Column
    int year;

    @Column
    String channel;
}
