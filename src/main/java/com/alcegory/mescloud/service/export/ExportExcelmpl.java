package com.alcegory.mescloud.service.export;

import com.alcegory.mescloud.model.dto.composed.ComposedInfoDto;
import com.alcegory.mescloud.model.dto.production.ProductionInstructionDto;
import com.alcegory.mescloud.model.dto.production.ProductionOrderDto;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.lang.reflect.Field;
import java.util.List;

import static com.alcegory.mescloud.service.export.ExcelConstants.*;

public class ExportExcelmpl extends AbstractExcelExport {

    public ExportExcelmpl() {
        super(null, null, null, null);
        this.workbook = new XSSFWorkbook();
    }

    public void exportDataToExcel(HttpServletResponse response, List<ComposedInfoDto> composedList,
                                  List<ProductionOrderDto> productionOrders) {

        XSSFSheet composedSheet = createSheet(SHEET_NAME_COMPOSED);
        createComposedHeaderRow(composedSheet);

        if (!composedList.isEmpty()) {
            createTable(composedSheet, TABLE_NAME_COMPOSED, getComposedHeaders().length - 1);
            writeDataToComposed(composedSheet, composedList);
        }

        XSSFSheet productionSheet = createSheet(SHEET_NAME_PRODUCTION_ORDERS);
        createProductionHeaderRow(productionSheet);

        if (!productionOrders.isEmpty()) {
            createTable(productionSheet, TABLE_NAME_PRODUCTION, getProductionOrderHeaders().length - 1);
            writeDataToProduction(productionSheet, productionOrders);
        }

        writeWorkbookToResponse(response);
    }

    private XSSFSheet createSheet(String sheetName) {
        return workbook.createSheet(sheetName);
    }

    private void createComposedHeaderRow(XSSFSheet sheet) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle();

        Class<?> composedClass = ComposedInfoDto.class;
        Field[] fields = composedClass.getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            String fieldName = fields[i].getName();
            createCell(headerRow, i, fieldName, headerStyle);
        }
    }

    private void createProductionHeaderRow(XSSFSheet sheet) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle();

        Class<?> composedClass = ProductionOrderDto.class;
        Field[] fields = composedClass.getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            String fieldName = fields[i].getName();
            createCell(headerRow, i, fieldName, headerStyle);
        }
    }

    protected void writeDataToComposed(XSSFSheet sheet, List<ComposedInfoDto> composedList) {
        int rowCount = 1;
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);

        for (ComposedInfoDto composed : composedList) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row, columnCount++, composed.getId(), style);
            createCell(row, columnCount++, composed.getBatchCode(), style);
            createCell(row, columnCount++, composed.getCode(), style);

            for (ProductionInstructionDto instruction : composed.getInstructions()) {
                createCell(row, columnCount++, instruction.getValue(), style);
            }

            createCell(row, columnCount++, composed.getValidAmount(), style);
            createCell(row, columnCount++, composed.getSampleAmount(), style);
            createCell(row, columnCount++, composed.getCreatedAt(), style);
            createCell(row, columnCount++, composed.getAmountOfHits(), style);
            createCell(row, columnCount++, composed.getReliability(), style);
            createCell(row, columnCount++, composed.getHitInsertedAt(), style);
            createCell(row, columnCount++, composed.getIsBatchApproved(), style);
            createCell(row, columnCount++, composed.getApprovedAt(), style);
        }
    }

    protected void writeDataToProduction(XSSFSheet sheet, List<ProductionOrderDto> productionOrders) {
        int rowCount = 1;
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);

        for (ProductionOrderDto po : productionOrders) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row, columnCount++, po.getEquipment() != null ? po.getEquipment().getAlias() : null, style);
            createCell(row, columnCount++, po.getComposedCode(), style);
            createCell(row, columnCount++, po.getCode(), style);
            createCell(row, columnCount++, po.getIms() != null ? po.getIms().getCode() : null, style);

            List<ProductionInstructionDto> instructionList = po.getInstructions();
            for (ProductionInstructionDto instruction : instructionList) {
                createCell(row, columnCount++, instruction.getValue(), style);
            }

            createCell(row, columnCount++, po.getValidAmount(), style);
            createCell(row, columnCount++, po.getCreatedAt(), style);
            createCell(row, columnCount++, po.getCompletedAt(), style);
        }
    }

    private void createTable(XSSFSheet sheet, String tableName, int lastCol) {
        int firstRow = 0;
        int lastRow = sheet.getLastRowNum();
        int firstCol = 0;

        XSSFTable table = createTableObject(sheet, firstRow, lastRow, firstCol, lastCol);
        setTableProperties(table, tableName, TABLE_STYLE);
        addAutoFilter(table, firstRow, lastCol);
        showStripes(table);
    }

    @Override
    protected XSSFTable createTableObject(XSSFSheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {
        AreaReference areaReference = new AreaReference(new CellReference(firstRow, firstCol),
                new CellReference(lastRow, lastCol), sheet.getWorkbook().getSpreadsheetVersion());
        return sheet.createTable(areaReference);
    }
}
