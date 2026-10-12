package com.paytaxihub.config;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * Columns keep Spring's usual camelCase -> snake_case mapping (driverId -> driver_id), but TABLE names are used
 * exactly as written in the entities (User, FleetOwner, SOSEvent ...). Without this, the default strategy turns
 * "FleetOwner" into fleet_owner and "SOSEvent" into sosevent, which do not match the tables created by the SQL
 * scripts and Flyway migrations. Names are quoted so reserved words such as User/Transaction are always safe.
 */
public class PayTaxiNamingStrategy extends CamelCaseToUnderscoresNamingStrategy {
    @Override
    public Identifier toPhysicalTableName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        return logicalName == null ? null : new Identifier(logicalName.getText(), true);
    }
}
