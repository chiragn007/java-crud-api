java
package com.tqdev.crudapi.openapi;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tqdev.crudapi.column.definition.ColumnDefinition;
import com.tqdev.crudapi.column.reflection.DatabaseReflection;
import com.tqdev.crudapi.column.reflection.ReflectedTable;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.impl.DefaultDataType;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Set;

public class OpenApiBuilder {
    // (unchanged fields and constructor)

    // (unchanged methods and utility functions)

    // Additional function to create endpoint for Create
    private void createEndpointForCreate(String tableName) {
        String path = String.format("/records/%s", tableName);
        openapi.set(String.format("paths|%s|post|tags|0", path), tableName);
        openapi.set(String.format("paths|%s|post|description", path), String.format("Create %s", tableName));

        openapi.set(String.format("paths|%s|post|responses|200|\\$ref", path), "#/components/responses/pk_integer");
        openapi.set(String.format("paths|%s|post|requestBody|\\$ref", path), String.format("#/components/requestBodies/create-%s", urlencode(tableName)));
    }

    // Additional function to create endpoint for Update
    private void createEndpointForUpdate(String tableName, String pkName) {
        String path = String.format("/records/%s/{%s}", tableName, pkName);
        openapi.set(String.format("paths|%s|put|tags|0", path), tableName);
        openapi.set(String.format("paths|%s|put|description", path), String.format("Update %s", tableName));

        openapi.set(String.format("paths|%s|put|responses|200|\\$ref", path), "#/components/responses/rows_affected");
        openapi.set(String.format("paths|%s|put|requestBody|\\$ref", path), String.format("#/components/requestBodies/update-%s", urlencode(tableName)));
    }

    // Additional function to create endpoint for Delete
    private void createEndpointForDelete(String tableName, String pkName) {
        String path = String.format("/records/%s/{%s}", tableName, pkName);
        openapi.set(String.format("paths|%s|delete|tags|0", path), tableName);
        openapi.set(String.format("paths|%s|delete|description", path), String.format("Delete %s", tableName));

        openapi.set(String.format("paths|%s|delete|responses|200|\\$ref", path), "#/components/responses/rows_affected");
    }

    public OpenApiDefinition build() {
        // (unchanged code)
        
        Set<String> tableNames = reflection.getTableNames();
        for (String tableName : tableNames) {
            ReflectedTable table = reflection.getTable(tableName);
            Field<?> pk = table.getPk();
            String pkName = pk != null ? pk.getName() : null;

            // Create endpoints for Create, Update, and Delete
            if (pkName != null) {
                createEndpointForCreate(tableName);
                createEndpointForUpdate(tableName, pkName);
                createEndpointForDelete(tableName, pkName);
            }
        }

        // (unchanged code)
        
        return openapi;
    }

    // (unchanged code)
}