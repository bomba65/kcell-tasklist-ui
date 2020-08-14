package kz.kcell.flow;

import kz.kcell.flow.UserTaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserTaskHistoryRepository extends JpaRepository<UserTaskHistory, Long> {

    @Query(value = "select row_number () over (order by id_) as id,timestamp_ + interval '6h' as assigned_date, " +
        "    user_id_ as assignee, " +
        "    case when operation_type_ = 'add' then 'Claimed/Assigned' when operation_type_ ='delete' then 'Unclaimed' else '-' end as operation, " +
        "    assigner_id_ as operation_responsible " +
        "from public.act_hi_identitylink " +
        "where type_ = 'assignee' " +
        "and task_id_= ?1 " +
        "order by assigned_date asc", nativeQuery = true)
    List<UserTaskHistory> findByTaskId(String task_id);
}
