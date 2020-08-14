package kz.kcell.flow;

import lombok.*;
import org.hibernate.envers.Audited;
import org.springframework.data.domain.Auditable;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class UserTaskHistory  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name="assigned_date")
    private Date assigned_date;

    @Column(name="assignee")
    private String assignee;

    @Column(name="operation")
    private String operation;

    @Column(name="operation_responsible")
    private String operation_responsible;

}
