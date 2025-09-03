package com.zmj.springboot.XiDian_starrocks;

import com.zmj.springboot.MyFileUtils;

import java.io.IOException;
import java.sql.*;

/**
 * 数据汇聚任务创建，更新关联关系
 *
 */
public class AMysqldata_category_table_rel {

    public static void main(String[] args) throws Exception {
        String functionName = "dwf2dwd";
        String host = "10.159.140.111";
        int port = 3306;
        String userName = "root";
        String password = "Xdcs@0901";

        String databaseName = "dip_ai_platform";//当前数据库名称
        String replaceDbName = null;//要替换成的数据库名称
        String currTablePrefix = null;//当前表前缀
        String replaceTablePrefix = null;//要替换成的数据库表前缀
        String columnOtherSplicing = null;//要替换成的数据库表创建语句字段部分其他拼接
        String querySQL = "select id from data_set " +
                "where data_set.`describe` like '数据汇聚任务创建' and data_set.name_en like 'ODS_CBS_%'";


        String url = "jdbc:mysql://" + host + ":" + port + "/" + databaseName;
        Connection conn = null;

        Statement stmt = null;
        StringBuffer sqlResult = new StringBuffer("update dip_ai_platform.data_set s set release_status = 1 where s.`describe` like '%数据汇聚任务创建%' and s.name_en like 'ODS_CBS_%';\n");
        try {
            conn = DriverManager.getConnection(url, userName, password);
            stmt = conn.createStatement();

            try (
                    ResultSet rs = stmt.executeQuery(querySQL)
            ) {//所有的表 所有的表字段

                while (rs.next()) {
                    String id = rs.getString("id");
                    sqlResult.append("INSERT INTO dip_ai_platform.data_category_table_rel (category_id, table_id, create_user, create_time, update_user, update_time) " +
                            "VALUES(19, "+id+", 1, '2025-07-17 11:04:50', NULL, NULL);\n");
                    sqlResult.append("INSERT INTO dip_ai_platform.data_category_table_rel (category_id, table_id, create_user, create_time, update_user, update_time) " +
                            "VALUES(282, "+id+", 1, '2025-07-17 11:04:50', NULL, NULL);\n");
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


        try {
            MyFileUtils.print2File(sqlResult,"D:\\MyOutputFile\\"+functionName+"_Result2.sql",false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}