package com.zmj.springboot;

import lombok.Data;

@Data
public class ColumnData {
        private String tableName;
        private String columnName;
        private String columnType;
        private String nullable;
        private String defaultValue;
        private String columnComment;
    }