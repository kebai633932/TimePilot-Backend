package org.cxk.infrastructure.config;

import com.pgvector.PGvector;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PGvectorTypeHandler extends BaseTypeHandler<PGvector> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, PGvector parameter, JdbcType jdbcType) throws SQLException {
        // pgvector JDBC 驱动已经支持 PGvector → SQL vector
        ps.setObject(i, parameter);
    }

    @Override
    public PGvector getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Object obj = rs.getObject(columnName);
        return obj == null ? null : (PGvector) obj;
    }

    @Override
    public PGvector getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Object obj = rs.getObject(columnIndex);
        return obj == null ? null : (PGvector) obj;
    }

    @Override
    public PGvector getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Object obj = cs.getObject(columnIndex);
        return obj == null ? null : (PGvector) obj;
    }
}
