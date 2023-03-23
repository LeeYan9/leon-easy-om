package com.lyon.easy.async.task.config.mybatis.handler;

import cn.hutool.core.util.ReflectUtil;
import com.lyon.easy.async.task.annotation.EnumValue;
import com.lyon.easy.common.utils.CollUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.EnumTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * @author Lyon
 */
public class InEnumTypeHandler<E extends Enum<E>> extends EnumTypeHandler<E> {

    private final Class<E> type;

    public InEnumTypeHandler(Class<E> type) {
        super(type);
        //noinspection ConstantConditions
        if (Objects.isNull(type)) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
    }
    // todo reflect  optimization need

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
        Object enumValue = tryGetEnumValueOrDefault(parameter, parameter.name());
        if (jdbcType == null) {
            ps.setString(i, enumValue.toString());
        } else {
            // see r3589
            ps.setObject(i, enumValue, jdbcType.TYPE_CODE);
        }
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String s = rs.getString(columnName);
        return s == null ? null : tryGetEnumOrDefault(s, Enum.valueOf(type, s));
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String s = rs.getString(columnIndex);
        return s == null ? null : tryGetEnumOrDefault(s, Enum.valueOf(type, s));
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String s = cs.getString(columnIndex);
        return s == null ? null : Enum.valueOf(type, s);
    }

    private Object tryGetEnumValueOrDefault(E parameter, String defaultValue) {
        if (Objects.isNull(parameter)) {
            return defaultValue;
        }
        final List<Field> fields = FieldUtils.getFieldsListWithAnnotation(type, EnumValue.class);
        Field enumValueField = CollUtils.isEmpty(fields) ? null : fields.get(0);
        if (Objects.isNull(enumValueField)) {
            return defaultValue;
        }
        final Object enumValue = ReflectUtil.getFieldValue(parameter, enumValueField);
        return Objects.isNull(enumValue) ? defaultValue : enumValue;
    }

    private E tryGetEnumOrDefault(String s, E defaultValue) {
        if (Objects.isNull(s)) {
            return defaultValue;
        }
        final List<Field> fields = FieldUtils.getFieldsListWithAnnotation(type, EnumValue.class);
        Field enumValueField = CollUtils.isEmpty(fields) ? null : fields.get(0);
        if (Objects.isNull(enumValueField)) {
            return defaultValue;
        }
        final E[] constants = type.getEnumConstants();
        for (E constant : constants) {
            final Object fieldValue = ReflectUtil.getFieldValue(constant, enumValueField);
            if (Objects.equals(fieldValue, s)) {
                return constant;
            }
        }
        return defaultValue;
    }
}
