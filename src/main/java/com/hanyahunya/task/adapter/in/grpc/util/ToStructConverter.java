package com.hanyahunya.task.adapter.in.grpc.util;

import com.google.protobuf.ListValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;

import java.util.List;
import java.util.Map;

public class ToStructConverter {
    /**
     * Java의 Map<String, Object> -> Protobuf Struct로 변환
     */
    public static Struct toStruct(Map<String, Object> map) {
        if (map == null) {
            return Struct.newBuilder().build();
        }
        Struct.Builder structBuilder = Struct.newBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            structBuilder.putFields(entry.getKey(), toValue(entry.getValue()));
        }
        return structBuilder.build();
    }

    /**
     * Java Object -> Protobuf Value로 재귀적으로 변환
     */
    @SuppressWarnings("unchecked")
    private static Value toValue(Object obj) {
        if (obj == null) {
            return Value.newBuilder()
                    .setNullValue(com.google.protobuf.NullValue.NULL_VALUE)
                    .build();
        }
        if (obj instanceof String s) {
            return Value.newBuilder()
                    .setStringValue(s)
                    .build();
        }
        if (obj instanceof Number n) {
            return Value.newBuilder()
                    .setNumberValue(n.doubleValue())
                    .build();
        }
        if (obj instanceof Boolean b) {
            return Value.newBuilder()
                    .setBoolValue(b)
                    .build();
        }
        if (obj instanceof Map) {
            // Map<String, Object>라고 가정
            return Value.newBuilder()
                    .setStructValue(toStruct((Map<String, Object>) obj))
                    .build();
        }
        if (obj instanceof List) {
            ListValue.Builder listBuilder = ListValue.newBuilder();
            for (Object item : (List<?>) obj) {
                listBuilder.addValues(toValue(item));
            }
            return Value.newBuilder().setListValue(listBuilder).build();
        }
        // 지원하지 않는 타입은 문자열로 변환
        return Value.newBuilder().setStringValue(obj.toString()).build();
    }
}
