package kz.kcell.bpm;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class ChangeStartTime implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) throws Exception {

        System.out.println(execution.getProcessInstanceId());
        System.out.println(execution.getProcessEngine().getProcessEngineConfiguration().getJpaEntityManagerFactory().getClass());
        EntityManagerFactory jpaEntityManagerFactory = (EntityManagerFactory) execution.getProcessEngine().getProcessEngineConfiguration().getJpaEntityManagerFactory();

        EntityManager entityManager = jpaEntityManagerFactory.createEntityManager();

        if (execution.hasVariable("contractor") && execution.getVariable("contractor").toString().equals("4")) {
            entityManager.getTransaction().begin();
            entityManager.createNativeQuery("update act_hi_procinst set start_time_ = '2020-06-29 09:00:00' where proc_inst_id_ = '" + execution.getProcessInstanceId() + "'").executeUpdate();
            entityManager.getTransaction().commit();
        }
    }
}
