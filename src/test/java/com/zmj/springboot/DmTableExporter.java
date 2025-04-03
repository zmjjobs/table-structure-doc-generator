package com.zmj.springboot;

import java.sql.*;
import java.io.FileWriter;
import java.io.IOException;

public class DmTableExporter {
    // 数据库连接信息
    private static final String URL = "jdbc:dm://localhost:5236/IAF_DEV";
    private static final String USER = "DMUSER";
    private static final String PASSWORD = "123456789";

    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            // 加载 JDBC 驱动
            Class.forName("dm.jdbc.driver.DmDriver");

            // 建立连接
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            stmt = conn.createStatement();

            // 查询表名
            String tableQuery = "SELECT TABLE_NAME FROM USER_TABLES";
            rs = stmt.executeQuery(tableQuery);

            // 导出为 HTML 文件
            exportToHtml(conn, "DMUSER库v20250403.html");

            // 导出为 Markdown 文件
            exportToMarkdown(conn, "DMUSER库v20250403.md");

            System.out.println("导出完成！HTML 文件已生成：table_structure.html");
            System.out.println("导出完成！Markdown 文件已生成：table_structure.md");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 导出为 HTML 文件
     */
    private static void exportToHtml(Connection conn, String filePath) throws SQLException, IOException {
        FileWriter writer = new FileWriter(filePath);

        // 写入 HTML 头部
        writer.write("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
        writer.write("<meta charset=\"UTF-8\">\n<title>达梦数据库表结构</title>\n");
        writer.write("<style>\n");
        writer.write("body { font-family: Arial, sans-serif; }\n");
        writer.write("h1, h2 { color: #333; }\n");
        writer.write("table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }\n");
        writer.write("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
        writer.write("th { background-color: #f4f4f4; }\n");
        writer.write("</style>\n</head>\n<body>\n<h1>达梦数据库表结构</h1>\n");

        // 查询所有表并写入内容
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT TABLE_NAME FROM USER_TABLES");
        while (rs.next()) {
            String tableName = rs.getString("TABLE_NAME");
            exportTableStructureToHtml(conn, tableName, writer);
        }

        // 写入 HTML 结尾
        writer.write("</body>\n</html>");
        writer.close();
    }

    /**
     * 导出单个表的结构和注释到 HTML
     */
    private static void exportTableStructureToHtml(Connection conn, String tableName, FileWriter writer) throws SQLException, IOException {
        Statement stmt = conn.createStatement();

        // 查询表注释
        String tableCommentQuery = "SELECT COMMENTS FROM USER_TAB_COMMENTS WHERE TABLE_NAME = '" + tableName + "'";
        ResultSet tableCommentRs = stmt.executeQuery(tableCommentQuery);
        String tableComment = "";
        if (tableCommentRs.next()) {
            tableComment = tableCommentRs.getString("COMMENTS");
        }
        tableCommentRs.close();

        // 写入表名和注释
        writer.write("<h2>表名：" + tableName + "</h2>\n<p><strong>表注释：</strong>" + tableComment + "</p>\n");

        // 查询字段信息和注释
        String columnQuery = "SELECT c.COLUMN_NAME, DATA_TYPE, DATA_LENGTH, NULLABLE, COMMENTS " +
                "FROM USER_TAB_COLUMNS c " +
                "LEFT JOIN USER_COL_COMMENTS cc ON c.TABLE_NAME = cc.TABLE_NAME AND c.COLUMN_NAME = cc.COLUMN_NAME " +
                "WHERE c.TABLE_NAME = '" + tableName + "'";
        ResultSet columnRs = stmt.executeQuery(columnQuery);

        writer.write("<table>\n<tr>\n<th>字段名</th><th>数据类型</th><th>长度</th><th>是否为空</th><th>注释</th>\n</tr>\n");
        while (columnRs.next()) {
            String columnName = columnRs.getString("COLUMN_NAME");
            String dataType = columnRs.getString("DATA_TYPE");
            int dataLength = columnRs.getInt("DATA_LENGTH");
            String nullable = columnRs.getString("NULLABLE");
            String comments = columnRs.getString("COMMENTS");

            writer.write("<tr>\n");
            writer.write("<td>" + columnName + "</td>\n");
            writer.write("<td>" + dataType + "</td>\n");
            writer.write("<td>" + dataLength + "</td>\n");
            writer.write("<td>" + nullable + "</td>\n");
            writer.write("<td>" + (comments == null ? "" : comments) + "</td>\n");
            writer.write("</tr>\n");
        }
        writer.write("</table>\n");
        columnRs.close();
        stmt.close();
    }

    /**
     * 导出为 Markdown 文件
     */
    private static void exportToMarkdown(Connection conn, String filePath) throws SQLException, IOException {
        FileWriter writer = new FileWriter(filePath);

        // 写入标题
        writer.write("# 达梦数据库表结构\n\n");

        // 查询所有表并写入内容
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT TABLE_NAME FROM USER_TABLES");
        while (rs.next()) {
            String tableName = rs.getString("TABLE_NAME");
            exportTableStructureToMarkdown(conn, tableName, writer);
        }

        writer.close();
    }

    /**
     * 导出单个表的结构和注释到 Markdown
     */
    private static void exportTableStructureToMarkdown(Connection conn, String tableName, FileWriter writer) throws SQLException, IOException {
        Statement stmt = conn.createStatement();

        // 查询表注释
        String tableCommentQuery = "SELECT COMMENTS FROM USER_TAB_COMMENTS WHERE TABLE_NAME = '" + tableName + "'";
        ResultSet tableCommentRs = stmt.executeQuery(tableCommentQuery);
        String tableComment = "";
        if (tableCommentRs.next()) {
            tableComment = tableCommentRs.getString("COMMENTS");
        }
        tableCommentRs.close();

        // 写入表名和注释
        writer.write("## 表名：" + tableName + "\n\n");
        writer.write("**表注释：** " + tableComment + "\n\n");

        // 查询字段信息和注释
        String columnQuery = "SELECT c.COLUMN_NAME, DATA_TYPE, DATA_LENGTH, NULLABLE, COMMENTS " +
                "FROM USER_TAB_COLUMNS c " +
                "LEFT JOIN USER_COL_COMMENTS cc ON c.TABLE_NAME = cc.TABLE_NAME AND c.COLUMN_NAME = cc.COLUMN_NAME " +
                "WHERE c.TABLE_NAME = '" + tableName + "'";
        ResultSet columnRs = stmt.executeQuery(columnQuery);

        writer.write("| 字段名 | 数据类型 | 长度 | 是否为空 | 注释 |\n");
        writer.write("|--------|----------|------|----------|------|\n");
        while (columnRs.next()) {
            String columnName = columnRs.getString("COLUMN_NAME");
            String dataType = columnRs.getString("DATA_TYPE");
            int dataLength = columnRs.getInt("DATA_LENGTH");
            String nullable = columnRs.getString("NULLABLE");
            String comments = columnRs.getString("COMMENTS");

            writer.write("| " + columnName + " | " + dataType + " | " + dataLength + " | " + nullable + " | " + (comments == null ? "" : comments) + " |\n");
        }
        writer.write("\n");
        columnRs.close();
        stmt.close();
    }
}