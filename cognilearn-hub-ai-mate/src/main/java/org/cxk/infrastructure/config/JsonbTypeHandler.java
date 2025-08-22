package org.cxk.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;
import java.util.Map;

@MappedTypes(Map.class)        // 告诉 MyBatis 这是处理 Map 类型的
@MappedJdbcTypes(JdbcType.OTHER) // 对应 PostgreSQL jsonb 类型
public class JsonbTypeHandler extends BaseTypeHandler<Map<String, Object>> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Map<String, Object> parameter, JdbcType jdbcType) throws SQLException {
        try {
            // 序列化 Map 为 JSON 字符串
            ps.setObject(i, objectMapper.writeValueAsString(parameter), Types.OTHER);
        } catch (Exception e) {
            throw new SQLException("Failed to convert Map to JSON", e);
        }
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        try {
            return json == null ? null : objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new SQLException("Failed to convert JSON to Map", e);
        }
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        try {
            return json == null ? null : objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new SQLException("Failed to convert JSON to Map", e);
        }
    }

    @Override
    public Map<String, Object> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        try {
            return json == null ? null : objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new SQLException("Failed to convert JSON to Map", e);
        }
    }
}
