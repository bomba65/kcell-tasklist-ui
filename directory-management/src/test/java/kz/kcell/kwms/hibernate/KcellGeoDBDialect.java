package kz.kcell.kwms.hibernate;

import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.spatial.dialect.h2geodb.GeoDBDialect;

@SuppressWarnings("unused")
public class KcellGeoDBDialect extends GeoDBDialect {
    public KcellGeoDBDialect() {
        super();
        this.registerFunction("point", new StandardSQLFunction("st_makepoint"));
    }

}
