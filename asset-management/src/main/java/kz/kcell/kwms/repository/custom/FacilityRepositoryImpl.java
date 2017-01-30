package kz.kcell.kwms.repository.custom;

import kz.kcell.kwms.model.Facility;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.*;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Iterator;
import java.util.List;

public class FacilityRepositoryImpl implements FacilityRepositoryCustom {
    @PersistenceContext
    EntityManager em;

    @Override
    public Page<Facility> findByLocationNear(double x, double y, Pageable p) {
        String query = "from Facility f order by distance(f.location, point(:x, :y))";
        String countQuery = "select count(f) from Facility f";

        Long total = em.createQuery(countQuery, Long.class).getSingleResult();

        List<Facility> facilities = em.createQuery(query, Facility.class)
                .setParameter("x", x)
                .setParameter("y", y)
                .setFirstResult(p.getOffset())
                .setMaxResults(p.getPageSize())
                .getResultList();

        Page<Facility> facilityPage = new PageImpl<Facility>(facilities, p, total);

        return facilityPage;
    }

}
