package kz.kcell.kwms.repository.custom;

import kz.kcell.kwms.model.FacilityInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

public class FacilityInstanceRepositoryImpl implements FacilityInstanceRepositoryCustom {
    @PersistenceContext
    EntityManager em;

    @Override
    public Page<FacilityInstance> findByLocationNear(double x, double y, Pageable p) {
        String query = "from Facility f order by distance(f.location, point(:x, :y))";
        String countQuery = "select count(f) from Facility f";

        Long total = em.createQuery(countQuery, Long.class).getSingleResult();

        List<FacilityInstance> facilities = em.createQuery(query, FacilityInstance.class)
                .setParameter("x", x)
                .setParameter("y", y)
                .setFirstResult(p.getOffset())
                .setMaxResults(p.getPageSize())
                .getResultList();

        Page<FacilityInstance> facilityPage = new PageImpl<FacilityInstance>(facilities, p, total);

        return facilityPage;
    }

}
