package com.alcegory.mescloud.utility.export;

import com.alcegory.mescloud.model.entity.ComposedSummaryEntity;
import com.alcegory.mescloud.model.entity.ProductionOrderSummaryEntity;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyleInfo;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MultiExcelExport {

    private static final String SHEET_NAME_PRODUCTION_ORDERS = "Ordens de Produção";
    private static final String SHEET_NAME_COMPOSED = "Produções Compostas";
    private static final String TABLE_NAME_PRODUCTION = "ProductionOrdersTable";
    private static final String TABLE_NAME_COMPOSED = "ComposedProductionOrdersTable";
    private static final String DECIMAL_FORMAT_PATTERN = "#0.00%";
    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    protected final XSSFWorkbook workbook;
    private final boolean withHits;
    private final boolean isCompleted;

    public MultiExcelExport(boolean withHits, boolean isCompleted) {
        this.withHits = withHits;
        this.isCompleted = isCompleted;
        this.workbook = new XSSFWorkbook();
    }

    public void exportDataToExcel(HttpServletResponse response, List<ComposedSummaryEntity> composedList,
                                  List<ProductionOrderSummaryEntity> productionOrders,
                                  boolean withHits, boolean isCompleted)
            throws IOException {

        XSSFSheet composedSheet = createSheet(SHEET_NAME_COMPOSED);
        createHeaderRow(composedSheet, getComposedHeaders(withHits, isCompleted));
        writeDataToComposed(composedSheet, composedList, isCompleted, withHits);

        XSSFSheet productionSheet = createSheet(SHEET_NAME_PRODUCTION_ORDERS);
        createHeaderRow(productionSheet, getProductionOrderHeaders(isCompleted));
        writeDataToProduction(productionSheet, productionOrders, isCompleted);

        createTable(composedSheet, TABLE_NAME_COMPOSED, "TableStyleMedium9", getComposedHeaders(withHits, isCompleted).length);
        createTable(productionSheet, TABLE_NAME_PRODUCTION, "TableStyleMedium9", getProductionOrderHeaders(isCompleted).length);

        writeWorkbookToResponse(response);
    }

    private XSSFSheet createSheet(String sheetName) {
        return workbook.createSheet(sheetName);
    }

    private void createHeaderRow(XSSFSheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle();
        for (int i = 0; i < headers.length; i++) {
            createCell(headerRow, i, headers[i], headerStyle);
        }
    }

    protected void writeDataToComposed(XSSFSheet sheet, List<ComposedSummaryEntity> composedList,
                                       boolean isCompleted, boolean withHits) {
        int rowCount = 1; // Start from row 1 (row 0 is header)
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);

        for (ComposedSummaryEntity composed : composedList) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            if (isCompleted) {
                createCell(row, columnCount++, composed.getBatchCode(), style);
            }

            createCell(row, columnCount++, composed.getCode(), style);
            createCell(row, columnCount++, composed.getInputBatch(), style);
            createCell(row, columnCount++, composed.getSource(), style);
            createCell(row, columnCount++, composed.getGauge(), style);
            createCell(row, columnCount++, composed.getCategory(), style);
            createCell(row, columnCount++, composed.getWashingProcess(), style);
            createCell(row, columnCount++, composed.getValidAmount(), style);
            createCell(row, columnCount++, composed.getSampleAmount(), style);
            createCell(row, columnCount++, composed.getCreatedAt(), style);

            if (withHits) {
                createCell(row, columnCount++, composed.getAmountOfHits(), style);
                createCell(row, columnCount++, composed.getReliability(), style);
                createCell(row, columnCount++, composed.getHitInsertedAt(), style);
            }

            if (isCompleted) {
                createCell(row, columnCount++, composed.getIsBatchApproved(), style);
                createCell(row, columnCount++, composed.getApprovedAt(), style);
            }
        }
    }

    protected void writeDataToProduction(XSSFSheet sheet, List<ProductionOrderSummaryEntity> productionOrders, boolean isCompleted) {
        int rowCount = 1; // Start from row 1 (row 0 is header)
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);

        for (ProductionOrderSummaryEntity po : productionOrders) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            createCell(row, columnCount++, po.getEquipment() != null ? po.getEquipment().getAlias() : null, style);

            if (isCompleted) {
                createCell(row, columnCount++, po.getComposedProductionOrder() != null ? po.getComposedProductionOrder()
                        .getCode() : null, style);
            }

            createCell(row, columnCount++, po.getCode(), style);
            createCell(row, columnCount++, po.getIms() != null ? po.getIms().getCode() : null, style);
            createCell(row, columnCount++, po.getInputBatch(), style);
            createCell(row, columnCount++, po.getSource(), style);
            createCell(row, columnCount++, po.getGauge(), style);
            createCell(row, columnCount++, po.getCategory(), style);
            createCell(row, columnCount++, po.getWashingProcess(), style);
            createCell(row, columnCount++, po.getValidAmount(), style);
            createCell(row, columnCount++, po.getCreatedAt(), style);
            createCell(row, columnCount++, po.getCompletedAt(), style);
        }
    }

    private void createTable(XSSFSheet sheet, String tableName, String tableStyleStr, int lastCol) {
        int firstRow = 0;
        int lastRow = sheet.getLastRowNum();
        int firstCol = 0;

        XSSFTable table = createTableObject(sheet, firstRow, lastRow, firstCol, lastCol);
        setTableProperties(table, tableName, tableStyleStr);
        addAutoFilter(table, firstRow, lastCol);
        showStripes(table);
    }

    private XSSFTable createTableObject(XSSFSheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {
        AreaReference areaReference = new AreaReference(new CellReference(firstRow, firstCol),
                new CellReference(lastRow, lastCol), sheet.getWorkbook().getSpreadsheetVersion());
        return sheet.createTable(areaReference);
    }

    private void setTableProperties(XSSFTable table, String tableName, String tableStyle) {
        CTTable ctTable = table.getCTTable();
        CTTableStyleInfo tableStyleInfo = ctTable.addNewTableStyleInfo();
        tableStyleInfo.setName(tableStyle);
        ctTable.setTableStyleInfo(tableStyleInfo);
        table.setDisplayName(tableName);
        table.setName(tableName);
    }

    private void addAutoFilter(XSSFTable table, int firstRow, int lastCol) {
        CTTable ctTable = table.getCTTable();
        String range = "A1:" + CellReference.convertNumToColString(lastCol) + (firstRow + 1);
        ctTable.addNewAutoFilter().setRef(range);
    }

    private void showStripes(XSSFTable table) {
        CTTable ctTable = table.getCTTable();
        CTTableStyleInfo tableStyle = ctTable.getTableStyleInfo();

        if (tableStyle == null) {
            tableStyle = ctTable.addNewTableStyleInfo();
        }

        tableStyle.setShowRowStripes(true);
    }

    private CellStyle createHeaderStyle() {
        XSSFWorkbook wb = workbook;
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    protected void createCell(Row row, int columnCount, Object value, CellStyle style) {
        Cell cell = row.createCell(columnCount);

        if (value instanceof Double doubleValue) {
            double numericValue = doubleValue / 100.0;
            DecimalFormat decimalFormat = new DecimalFormat(DECIMAL_FORMAT_PATTERN);
            cell.setCellValue(decimalFormat.format(numericValue));
        } else if (value instanceof Number numberValue) {
            cell.setCellValue(numberValue.doubleValue());
        } else if (value instanceof Date dateValue) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN);
            cell.setCellValue(dateFormat.format(dateValue));
        } else if (value instanceof Boolean booleanValue) {
            cell.setCellValue(Boolean.TRUE.equals(booleanValue) ? "Aprovado" : "Reprovado");
        } else {
            cell.setCellValue(value != null ? value.toString() : "");
        }

        style.setAlignment(HorizontalAlignment.LEFT);
        cell.setCellStyle(style);
    }

    private void writeWorkbookToResponse(HttpServletResponse response) throws IOException {
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            workbook.write(outputStream);
        } finally {
            workbook.close();
        }
    }

    private static String[] getComposedHeaders(boolean withHits, boolean isCompleted) {
        List<String> headersList = new ArrayList<>();

        if (isCompleted) {
            headersList.add("Lote Final");
        }

        String[] commonHeaders = {
                "Produção Composta",
                "Lote de Entrada",
                "Proveniência",
                "Calibre",
                "Classe",
                "Lavação",
                "Quantidade",
                "Amostra",
                "Criação da composta"
        };

        headersList.addAll(Arrays.asList(commonHeaders));

        if (withHits) {
            headersList.addAll(Arrays.asList("Hits", "Fiabilidade", "Hits inseridos em"));
        }

        if (isCompleted) {
            headersList.add("Status");
            headersList.add("Resolvido em");
        }

        return headersList.toArray(new String[0]);
    }

    private static String[] getProductionOrderHeaders(boolean isCompleted) {
        List<String> headersList = new ArrayList<>();

        String[] commonHeaders = {
                "Equipamento",
                "Ordem de Produção",
                "IMS",
                "Lote de Entrada",
                "Proveniência",
                "Calibre",
                "Classe",
                "Lavação",
                "Quantidade",
                "Início de Produção",
                "Conclusão de Produção"
        };

        // Add common headers
        headersList.addAll(Arrays.asList(commonHeaders));

        if (isCompleted) {
            headersList.add(1, "Produção Composta");
        }

        return headersList.toArray(new String[0]);
    }
}