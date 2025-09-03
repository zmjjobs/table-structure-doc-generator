package com.zmj.springboot.ZheNeng_TiDB;

import com.zmj.springboot.MyFileUtils;
import com.zmj.springboot.StringTwo;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * TiDB库将ODS层的表数据插入到对应的DWF表中
 * 前提是 DWF表确定只比ODS层的表多一个字段 DATA_DT
 */
public class TidbODSinsertDWF {

    public static void main(String[] args) throws Exception {
        String host = "10.159.139.4";
        int port = 8000;
        String userName = "root";
        String password = "P@ssw0rd@ZNCW";

        String databaseName = "ods";;//当前数据库名称
        String replaceDbName = "dwf";//要替换成的数据库名称
        String currTablePrefix = "ODS_";//当前表前缀
        String replaceTablePrefix = "DWF_";//要替换成的数据库表前缀
        String columnOtherSplicing = null;//要替换成的数据库表创建语句字段部分其他拼接
        String tableNamePart = "BS_ACCTHISBALANCE";

        String querySQL = "SELECT TABLE_NAME,COLUMN_NAME FROM information_schema.columns "
                 + "WHERE table_schema = '" + databaseName +"' "
               // + " and table_name in ('ODS_CBS_CLIENT_SHAREHOLDER') "
                +"ORDER BY ordinal_position";

        String url = "jdbc:mysql://" + host + ":" + port + "/" + databaseName;
        Connection conn = null;

        Statement stmt = null;
        Map<String, StringTwo> columnDataMap = new HashMap<>();//key:表名称  value:表字段拼接,逗号分隔，a为默认全部字段，b为加了表别名的全部字段
        try {
            conn = DriverManager.getConnection(url, userName, password);
            stmt = conn.createStatement();

            try (
                    ResultSet rs = stmt.executeQuery(querySQL)
            ) {//所有的表 所有的表字段
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    String columnName = rs.getString("COLUMN_NAME");
                    StringTwo stringTwo = columnDataMap.get(tableName);
                    if (stringTwo == null) {
                        stringTwo = new StringTwo();
                        stringTwo.setA(columnName+",");
                        stringTwo.setB("g."+columnName+",");
                        columnDataMap.put(tableName,stringTwo);
                    } else {
                        stringTwo.setA(stringTwo.getA() + columnName+",");
                        stringTwo.setB(stringTwo.getB() + "g."+columnName+",");
                    }
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
        for (Map.Entry<String, StringTwo> entry : columnDataMap.entrySet()) {
            String tableName = entry.getKey();
            StringTwo two = entry.getValue();
            sqlResult.append("delete from ").append(replaceDbName).append(".").append(tableName.replaceFirst(currTablePrefix,replaceTablePrefix))
                    .append(" where DATA_DT = '${V_DATE}';").append("\n");
            sqlResult.append("insert into ").append(replaceDbName).append(".").append(tableName.replaceFirst(currTablePrefix,replaceTablePrefix))
                    .append("(DATA_DT,").append(two.getA().substring(0,two.getA().length()-1)).append(") select '${V_DATE}',")
                    .append(two.getB().substring(0,two.getB().length()-1)).append(" from ").append(databaseName).append(".").append(tableName).append(" g;\n\n");
        }
        //System.out.println(sqlResult);
        try {
            MyFileUtils.print2File(sqlResult,"D:\\MyOutputFile\\TiDB_ODSinsertDWF_v20250901.sql",false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}