package com.zmj.springboot;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.sql.*;

public class ExportAllTableStructureToExcel {

    public static void main(String[] args) {
        String url = "jdbc:oracle:thin:@10.159.141.38:1521/zncbs";
        String user = "zncs_hx_cs0122";
        String password = "iss!@#123qwe";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            Workbook workbook = new XSSFWorkbook();

            // 获取当前用户下的所有表名
            String sql = " SELECT TABLE_NAME FROM ALL_TABLES WHERE OWNER = ? ";
            String schema = null;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                schema = conn.getMetaData().getUserName(); // 当前登录用户
                ps.setString(1, schema);

                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    createSheetForTable(workbook, conn, schema, tableName);
                }
            }

            // 保存文件
            try (FileOutputStream fos = new FileOutputStream("D:\\"+schema + "_all_table_structure.xlsx")) {
                workbook.write(fos);
            }

            System.out.println("✅ 所有表结构已成功导出至 " + schema + "_all_table_structure.xlsx");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createSheetForTable(Workbook workbook, Connection conn, String schema, String tableName) throws Exception {
        Sheet sheet = workbook.createSheet(tableName); // 每个表一个sheet

        int rowNum = 0;

        // 第一行：表注释
        String tableComment = getTableComment(conn, schema, tableName);
        Row commentRow = sheet.createRow(rowNum++);
        commentRow.createCell(0).setCellValue("表名：" + tableName + "，表注释：" + tableComment);

        // 第二行：表头
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"列名", "数据类型", "长度/精度", "是否可为空", "默认值", "列注释"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        // 查询列结构与注释
        String sql = "SELECT utc.COLUMN_NAME, utc.DATA_TYPE, " +
                "CASE WHEN utc.DATA_TYPE IN ('VARCHAR2', 'CHAR') THEN '' || utc.DATA_LENGTH " +
                "WHEN utc.DATA_TYPE IN ('NUMBER') THEN COALESCE(utc.DATA_PRECISION, 0) || ',' || COALESCE(utc.DATA_SCALE, 0) " +
                "ELSE '' END AS DATA_SIZE, utc.NULLABLE, utc.DATA_DEFAULT, acc.COMMENTS " +
                "FROM ALL_TAB_COLUMNS utc LEFT JOIN ALL_COL_COMMENTS acc ON utc.OWNER = acc.OWNER " +
                "AND utc.TABLE_NAME = acc.TABLE_NAME AND utc.COLUMN_NAME = acc.COLUMN_NAME " +
                "WHERE utc.OWNER = ? AND utc.TABLE_NAME = ? ORDER BY utc.COLUMN_ID ";


        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, schema);
            ps.setString(2, tableName);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rs.getString("COLUMN_NAME"));
                row.createCell(1).setCellValue(rs.getString("DATA_TYPE"));

                String dataSize = rs.getString("DATA_SIZE");
                row.createCell(2).setCellValue(dataSize == null ? "" : dataSize);

                row.createCell(3).setCellValue(rs.getString("NULLABLE"));
                row.createCell(4).setCellValue(rs.getObject("DATA_DEFAULT", String.class));
                row.createCell(5).setCellValue(rs.getString("COMMENTS"));
            }
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static String getTableComment(Connection conn, String schema, String tableName) throws SQLException {
        String sql = "SELECT COMMENTS FROM ALL_TAB_COMMENTS WHERE TABLE_NAME = ? AND OWNER = ? ";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, schema);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("COMMENTS");
            }
        }
        return "";
    }
}