package com.reliaquest.api.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.reliaquest.api.dto.ResponseDTO;
import java.util.Collections;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ResponseUtil {

    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static <T> T extractData(ResponseDTO<?> response, Class<T> targetClass) {
        if (response == null || response.getData() == null) {
            return null;
        }
        return OBJECT_MAPPER.convertValue(response.getData(), targetClass);
    }

    public static <T> List<T> extractListData(ResponseDTO<?> response, Class<T> targetClass) {
        if (response == null || response.getData() == null) {
            return Collections.emptyList();
        }
        TypeFactory typeFactory = OBJECT_MAPPER.getTypeFactory();
        return OBJECT_MAPPER.convertValue(
                response.getData(), typeFactory.constructCollectionType(List.class, targetClass));
    }
}
