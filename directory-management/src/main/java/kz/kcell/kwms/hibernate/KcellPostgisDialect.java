package kz.kcell.kwms.hibernate;

import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.spatial.dialect.postgis.PostgisPG95Dialect;
import org.hibernate.tool.schema.internal.StandardForeignKeyExporter;
import org.hibernate.tool.schema.spi.Exporter;

import java.sql.Types;

@SuppressWarnings("unused")
public class KcellPostgisDialect extends PostgisPG95Dialect {
    private StandardForeignKeyExporter foreignKeyExporter = new PostgresqlForeignKeyExporter( this );

    public KcellPostgisDialect() {
        super();
        this.registerColumnType( Types.JAVA_OBJECT, "jsonb" );
        this.registerColumnType( Types.VARCHAR, "text" );
        this.registerFunction("point", new StandardSQLFunction("st_point"));
    }

    @Override
    public Exporter<ForeignKey> getForeignKeyExporter() {
        return this.foreignKeyExporter;
    }


}
