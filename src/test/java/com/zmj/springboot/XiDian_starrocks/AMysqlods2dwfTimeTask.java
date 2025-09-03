package com.zmj.springboot.XiDian_starrocks;

import com.zmj.springboot.MyFileUtils;

import java.io.IOException;
import java.sql.*;

/**
 * 从ODS到DWF的定时任务填写
 *
 */
public class AMysqlods2dwfTimeTask {

    public static void main(String[] args) throws Exception {
        String host = "10.159.140.111";
        int port = 9030;
        String userName = "root";
        String password = "Xdcs@0901";

        String databaseName = "ods";//当前数据库名称
        String replaceDbName = "dwf";//要替换成的数据库名称
        String currTablePrefix = "ODS_";//当前表前缀
        String replaceTablePrefix = "DWF_";//要替换成的数据库表前缀
        String columnOtherSplicing = null;//要替换成的数据库表创建语句字段部分其他拼接

        String url = "jdbc:mysql://" + host + ":" + port + "/" + databaseName;
        Connection conn = null;
        StringBuffer sqlResult = new StringBuffer();
        Statement stmt = null;
        try {
            conn = DriverManager.getConnection(url, userName, password);
            stmt = conn.createStatement();

            try (
                    ResultSet rs = stmt.executeQuery("SELECT  TABLE_NAME FROM  information_schema.TABLES WHERE table_schema = '"+databaseName+"'")
            ) {//所有的表 所有的表字段

                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    String replaceName = tableName.replaceFirst(currTablePrefix,replaceTablePrefix);
                    sqlResult.append("delete from ").append(replaceDbName).append(".").append(replaceName).append(" where DATA_DT = '${V_DATE}';\n");
                    sqlResult.append("insert into ").append(replaceDbName).append(".").append(replaceName).append(" select '${V_DATE}',g.* from ").append(databaseName).append(".").append(tableName).append(" g;\n");
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
            MyFileUtils.print2File(sqlResult,"D:\\MyOutputFile\\ods2dwfTimeTask.sql",false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}