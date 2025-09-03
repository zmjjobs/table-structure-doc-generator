package com.zmj.springboot.XiDian_starrocks;

import com.zmj.springboot.ColumnData;
import com.zmj.springboot.MyFileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.sql.*;
import java.util.*;

/**
 * 生成StarRocks.mysql 转Tidb
 */
public class StarRocksCreateTableGenerator2TiDB {

    public static void main(String[] args) throws Exception {
        String functionName = "ods2dwf";
        String host = "10.159.139.4";
        int port = 8000;
        String userName = "root";
        String password = "P@ssw0rd@ZNCW";

        String databaseName = null;//当前数据库名称
        String replaceDbName = null;//要替换成的数据库名称
        String currTablePrefix = null;//当前表前缀
        String replaceTablePrefix = null;//要替换成的数据库表前缀
        String columnOtherSplicing = null;//要替换成的数据库表创建语句字段部分其他拼接
        String  outputFileName = "D:\\MyOutputFile\\tidb8000_";
        if (functionName.equals("ods")) {
            databaseName = "ods";
        } else if (functionName.equals("dwf")) {
            databaseName = "dwf";
        }else if (functionName.equals("dwd")) {
            databaseName = "dwd";
        }else if (functionName.equals("anm")) {
            databaseName = "anm";
        }else if (functionName.equals("ods2dwf")) {//ODS库到DWF库，表名字前缀改成OWF, 加了一个DATA_DT字段，且主键部分前也补充这个字段，其他不变
            databaseName = "ods";
            currTablePrefix = "ODS_";
            replaceTablePrefix = "DWF_";
            replaceDbName="dwf";//要替换的数据库名称
            columnOtherSplicing = "  DATA_DT varchar(10) NOT NULL COMMENT '数据日期',\n";
        } else
        if (functionName.equals("dwf2dwd")) {//DWF库到DWD库，表名字前缀改成DWD，其他不变。暂时有问题，如果需要，直接在ods2dwf文件基础上修改！！！！！！
            databaseName = "dwf";
            currTablePrefix = "DWF_";
            replaceTablePrefix = "DWD_";
        }
        outputFileName += (StringUtils.isNotBlank(replaceDbName) ?  replaceDbName: databaseName)+"_v20250806.sql";

        String querySQL = "SELECT TABLE_NAME,COLUMN_NAME,COLUMN_TYPE,IS_NULLABLE,COLUMN_DEFAULT,COLUMN_COMMENT FROM information_schema.columns "
                 + "WHERE table_schema = '" + databaseName +"' "
                //+ " and table_name in ('ODS_CBS_BS_BANKINSTRUCTIONINFO') "
                +"ORDER BY ordinal_position";


        String url = "jdbc:mysql://" + host + ":" + port + "/" + databaseName;
        Connection conn = null;

        Statement stmt = null;
        Map<String,List<ColumnData>> columnDataMap = new HashMap<>();
        Map<String,String> tableZSMap = new HashMap<>();//表名字 表注释
        Map<String,List<String>> tablePRIMap = new HashMap<>();  //表名字 表主键（可能有多个）
        try {
            conn = DriverManager.getConnection(url, userName, password);
            stmt = conn.createStatement();

            try (
                    ResultSet rs = stmt.executeQuery(querySQL)
            ) {//所有的表 所有的表字段

                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    List<ColumnData> columnDataList = columnDataMap.get(tableName);
                    if (columnDataList == null) {
                        columnDataList = new ArrayList<>();
                        columnDataMap.put(tableName,columnDataList);
                    }
                    ColumnData columnData = new ColumnData();
                    columnData.setTableName(tableName);
                    columnData.setColumnName(rs.getString("COLUMN_NAME"));
                    columnData.setColumnType(rs.getString("COLUMN_TYPE"));
                    columnData.setNullable(rs.getString("IS_NULLABLE"));
                    columnData.setDefaultValue(rs.getString("COLUMN_DEFAULT"));
                    columnData.setColumnComment(rs.getString("COLUMN_COMMENT"));
                    columnDataList.add(columnData);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try (
                    ResultSet rs = stmt.executeQuery(
                            "SELECT  TABLE_NAME,TABLE_COMMENT FROM  information_schema.TABLES  " +
                                    "WHERE table_schema = '" + databaseName + "' "
                    )) {//所有的表 所有的表注释

                while (rs.next()) {
                    tableZSMap.put(rs.getString("TABLE_NAME"),rs.getString("TABLE_COMMENT"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try (
                    ResultSet rs = stmt.executeQuery(
                            "SELECT TABLE_NAME,COLUMN_NAME  FROM information_schema.columns " +
                                    "WHERE  table_schema = '" + databaseName + "' " +
                                    "and information_schema.`columns`.COLUMN_KEY = 'PRI'"
                    )) {

                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    List<String> tableList = tablePRIMap.get(tableName);
                    if (tableList == null) {
                        tableList = new ArrayList<>();
                        tablePRIMap.put(tableName,tableList);
                    }
                    tableList.add(rs.getString("COLUMN_NAME"));
                }


            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        StringBuffer sqlResult = new StringBuffer();
        String currDbName = null;
        boolean isReplace = false;
        if (StringUtils.isNotBlank(replaceDbName)) {
            isReplace = true;
            currDbName = replaceDbName;
        } else {
            currDbName = databaseName;
        }
        for (Map.Entry<String, List<ColumnData>> entry : columnDataMap.entrySet()) {
            String tableName = entry.getKey();
            List<ColumnData> columnDataList = entry.getValue();
            List<String> pKeyNames = tablePRIMap.get(tableName);
            if (pKeyNames == null || pKeyNames.size() == 0) {
                throw new Exception("!!!此表没有主键!! tableName="+tableName);
            }
            String pKeyNamePart = "";
            for (int i = 0;i < pKeyNames.size();i++) {
                if (!columnDataList.get(i).getColumnName().equals(pKeyNames.get(i))) {
                    throw new Exception("!!!此表 tableName="+tableName+",字段"+columnDataList.get(i).getColumnName()+"应该在所有列的第"+(i+1)+"位");
                }
                if (i > 0) {
                    pKeyNamePart += ",";
                }
                pKeyNamePart += "`" + pKeyNames.get(i) + "`";
            }

            if (isReplace) {
                tableName = tableName.toUpperCase(Locale.ROOT).replaceFirst(currTablePrefix, replaceTablePrefix);
            }

            StringBuffer sbCreateTable = new StringBuffer();
            sbCreateTable.append("DROP TABLE IF EXISTS `").append(currDbName).append("`.`") .append(tableName).append("`;\n");
            sbCreateTable.append("CREATE TABLE `").append(currDbName).append("`.`");
            sbCreateTable.append(tableName).append("` (\n");
            if (isReplace) sbCreateTable.append(columnOtherSplicing);

            List<String> columnDefs = new ArrayList<>();
            for (ColumnData columnData : columnDataList) {
                StringBuffer sbColumns = new StringBuffer();
                sbColumns.append("  `").append(columnData.getColumnName()).append("` ").append(columnData.getColumnType());

                if ("NO".equalsIgnoreCase(columnData.getNullable())) {
                    sbColumns.append(" NOT NULL");
                } else {
                    sbColumns.append(" NULL");
                }

                if (columnData.getDefaultValue() != null) {
                    // 处理 CURRENT_TIMESTAMP 等特殊值
                    if (columnData.getDefaultValue().equals("CURRENT_TIMESTAMP")) {
                        sbColumns.append(" DEFAULT ").append(columnData.getDefaultValue());
                    } else {
                        sbColumns.append(" DEFAULT '").append(columnData.getDefaultValue()).append("'");
                    }
                }

                if (columnData.getColumnComment() != null && !columnData.getColumnComment().isEmpty()) {
                    sbColumns.append(" COMMENT '").append(columnData.getColumnComment().replaceAll("'","")).append("'");
                }
                columnDefs.add(sbColumns.toString());
            }

            sbCreateTable.append(String.join(",\n", columnDefs));
            if (isReplace) {
                sbCreateTable.append(",\n").append("PRIMARY KEY(`DATA_DT`,").append(pKeyNamePart) .append(")");
            } else {
                sbCreateTable.append(",\n").append("PRIMARY KEY(").append(pKeyNamePart) .append(")");
            }
            sbCreateTable.append(")");
            sbCreateTable.append(" ").append("COMMENT '").append(tableZSMap.get(tableName)).append("';\n");
            sqlResult.append(sbCreateTable).append("\n");
        }

        //System.out.println(sqlResult);
        try {
            MyFileUtils.print2File(sqlResult,outputFileName,false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}