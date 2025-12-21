package com.pitisha.project.accountservice.domain.entity.mapper;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.format.FormatMapper;
import tools.jackson.databind.json.JsonMapper;

@SuppressWarnings("unused")
public class CustomJsonFormatMapper implements FormatMapper {

    private static final JsonMapper JSON_MAPPER = new JsonMapper();

    @Override
    public <T> T fromString(final CharSequence charSequence, final JavaType<T> javaType, final WrapperOptions wrapperOptions) {
        return JSON_MAPPER.readValue(charSequence.toString(), javaType.getJavaTypeClass());
    }

    @Override
    public <T> String toString(final T t, final JavaType<T> javaType, final WrapperOptions wrapperOptions) {
        return JSON_MAPPER.writeValueAsString(t);
    }
}
