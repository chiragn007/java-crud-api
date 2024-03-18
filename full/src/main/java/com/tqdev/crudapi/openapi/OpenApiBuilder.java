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
    private OpenApiDefinition openapi;
    private DatabaseReflection reflection;
    private LinkedHashMap<String, String> operations;
    private LinkedHashMap<String, LinkedHashMap<String, String>> types;

    private LinkedHashMap<String, String> createType(String type, String format) {
        LinkedHashMap<String, String> item = new LinkedHashMap<>();
        item.put("type", type);
        if (format != null) {
            item.put("format", format);
        }
        return item;
    }

    public OpenApiBuilder(DatabaseReflection reflection, OpenApiDefinition base) {
        operations = new LinkedHashMap<>();
        operations.put("list", "getTable");
        operations.put("create", "post");
        operations.put("read", "getTable");
        operations.put("update", "put");
        operations.put("delete", "delete");
        operations.put("increment", "patch");
        types = new LinkedHashMap<>();
        types.put("integer", createType("integer", "int32"));
        types.put("bigint", createType("integer", "int64"));
        types.put("varchar", createType("string", null));
        types.put("clob", createType("string", null));
        types.put("varbinary", createType("string", "byte"));
        types.put("blob", createType("string", "byte"));
        types.put("decimal", createType("string", null));
        types.put("float", createType("number", "float"));
        types.put("double", createType("number", "double"));
        types.put("time", createType("string", "date-time"));
        types.put("timestamp", createType("string", "date-time"));
        types.put("geometry", createType("string", null));
        types.put("boolean", createType("boolean", null));
        this.reflection = reflection;
        openapi = new OpenApiDefinition(base);
    }

    public OpenApiDefinition build() {
        openapi.set("openapi", "3.0.0");
        Set<String> tableNames = reflection.getTableNames();
        for (String tableName : tableNames) {
            setPath(tableName);
        }
        openapi.set("components|responses|pk_integer|description", "inserted primary key value (integer)");
        openapi.set("components|responses|pk_integer|content|application/json|schema|type", "integer");
        openapi.set("components|responses|pk_integer|content|application/json|schema|format", "int64");
        openapi.set("components|responses|pk_string|description", "inserted primary key value (string)");
        openapi.set("components|responses|pk_string|content|application/json|schema|type", "string");
        openapi.set("components|responses|pk_string|content|application/json|schema|format", "uuid");
        openapi.set("components|responses|rows_affected|description", "number of rows affected (integer)");
        openapi.set("components|responses|rows_affected|content|application/json|schema|type", "integer");
        openapi.set("components|responses|rows_affected|content|application/json|schema|format", "int64");
        for (String tableName : tableNames) {
            setComponentSchema(tableName);
            setComponentResponse(tableName);
            setComponentRequestBody(tableName);
        }
        setComponentParameters();
        int i = 0;
        for (String tableName : tableNames) {
            setTag(i, tableName);
            i++;
        }
        return openapi;
    }

    private boolean isOperationOnTableAllowed(String operation, String tableName) {
        return true;
    }

    private boolean isOperationOnColumnAllowed(String operation, String tableName, String columnName) {
        return true;
    }

    private String urlencode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

    private void setPath(String tableName) {
        // Logic for setting path
    }

    private void setComponentSchema(String tableName) {
        // Logic for setting component schema
    }

    private void setComponentResponse(String tableName) {
        // Logic for setting component response
    }

    private void setComponentRequestBody(String tableName) {
        // Logic for setting component request body
    }

    private void setComponentParameters() {
        // Logic for setting component parameters
    }

    private void setTag(int index, String tableName) {
        // Logic for setting tag
    }
}