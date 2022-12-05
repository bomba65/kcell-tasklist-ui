package kz.kcell.kwms.hibernate;

import org.hibernate.boot.Metadata;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.tool.schema.internal.StandardForeignKeyExporter;

public class PostgresqlForeignKeyExporter extends StandardForeignKeyExporter {

    private final Dialect dialect;

    public PostgresqlForeignKeyExporter(Dialect dialect) {
        super(dialect);
        this.dialect = dialect;
    }

    @Override
    public String[] getSqlDropStrings(ForeignKey foreignKey, Metadata metadata) {
        final JdbcEnvironment jdbcEnvironment = metadata.getDatabase().getJdbcEnvironment();
        final String sourceTableName = jdbcEnvironment.getQualifiedObjectNameFormatter().format(
                foreignKey.getTable().getQualifiedTableName(),
                dialect
        );
        return new String[] {
                "alter table if exists " + sourceTableName + " drop constraint if exists " + foreignKey.getName()
        };
    }
}
