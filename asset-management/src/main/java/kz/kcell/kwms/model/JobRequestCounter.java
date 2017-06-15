package kz.kcell.kwms.model;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "jobrequest_counter")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRequestCounter {

    @Id
    String id;

    @Column(columnDefinition = "int default 1")
    int value;
}
