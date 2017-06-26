package kz.kcell.bpm;

import org.apache.commons.mail.MultiPartEmail;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.junit.Test;

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;

public class KcellRtfTest {

    //@Test
    public void test() {
        try {
            FileInputStream fileInputStream = new FileInputStream(new File("jr-blank.xlsx"));
            XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
            fileInputStream.close();
            XSSFSheet sheet = workbook.getSheetAt(0);
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                if (sheet.getRow(i) != null) {
                    for (int j = 0; j < sheet.getRow(i).getLastCellNum(); j++) {
                        if (sheet.getRow(i).getCell(j) != null) {
                            if (sheet.getRow(i).getCell(j).getStringCellValue() != null) {
                                if (sheet.getRow(i).getCell(j).getStringCellValue().equals("${siteNumber}")) {
                                    sheet.getRow(i).getCell(j).setCellValue("THIS IS SITENUM!");
                                } else if (sheet.getRow(i).getCell(j).getStringCellValue().equals("${jrNumber}")) {
                                    sheet.getRow(i).getCell(j).setCellValue("THIS IS JRNUM");
                                } else if (sheet.getRow(i).getCell(j).getStringCellValue().equals("${jobDescription}")) {
                                    sheet.getRow(i).getCell(j).setCellValue("THIS IS JOB DESCRIPTION");
                                } else if (sheet.getRow(i).getCell(j).getStringCellValue().equals("${explanation}")) {
                                    sheet.getRow(i).getCell(j).setCellValue("THIS IS EXPLANATION");
                                }
                            }
                        }
                    }
                }
            }
            List<String> works = Arrays.asList("31. TRU/TRX addition installation and testing.(Дополнительная установка и тестирование TRU/TRX.) - 1;",
                    "38. GSM Antenna installation.(Установка GSM антенны) - 1;",
                    "115. PCM/UTP/KVSM/optical patchcord Cable installation 176-up m() - 1");
            int worksRow = -1;
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                if (sheet.getRow(i) != null) {
                    if (sheet.getRow(i).getCell(0) != null) {
                        if (sheet.getRow(i).getCell(0).getStringCellValue() != null && sheet.getRow(i).getCell(0).getStringCellValue().equals("${worksRow}")) {
                            worksRow = i;
                        }
                    }
                }
            }
            XSSFFont arial9 = workbook.createFont();
            arial9.setFontName(HSSFFont.FONT_ARIAL);
            arial9.setFontHeightInPoints((short) 9);

            CellStyle autoWrap = workbook.createCellStyle(); //Create new style
            autoWrap.setFont(arial9);
            autoWrap.setWrapText(true);

            if (worksRow >= 0) {
                for (int i = 0; i < works.size() - 1; i++) {
                    copyRow(workbook, sheet, worksRow, worksRow + i + 1);
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            out.close();
            ByteArrayInputStream is = new ByteArrayInputStream(out.toByteArray());

            DataSource source = new ByteArrayDataSource(is, "application/pdf");

            MultiPartEmail email = new MultiPartEmail();
            email.setHostName("localhost");
            email.setSmtpPort(1025);
            email.addTo("nurlan@muldashev.kz", "Nurlan Muldashev");
            email.setFrom("test_flow@kcell.kz", "Me");
            email.setSubject("Job Request created");
            email.setMsg("Hey! Please look at this JR document! It's amazing");
            email.attach(source, "jr-blank.xlsx", "Job Request blank");
            email.send();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyRow(XSSFWorkbook workbook, XSSFSheet worksheet, int sourceRowNum, int destinationRowNum) {
        // Get the source / new row
        XSSFRow newRow = worksheet.getRow(destinationRowNum);
        XSSFRow sourceRow = worksheet.getRow(sourceRowNum);

        // If the row exist in destination, push down all rows by 1 else create a new row
        if (newRow != null) {
            worksheet.shiftRows(destinationRowNum, worksheet.getLastRowNum(), 1);
        } else {
            newRow = worksheet.createRow(destinationRowNum);
        }

        // Loop through source columns to add to new row
        for (int i = 0; sourceRow != null && i < sourceRow.getLastCellNum(); i++) {
            System.out.println(i);
            // Grab a copy of the old/new cell
            XSSFCell oldCell = sourceRow.getCell(i);
            XSSFCell newCell = newRow.createCell(i);

            // If the old cell is null jump to next cell
            if (oldCell == null) {
                newCell = null;
                continue;
            }

            // Copy style from old cell and apply to new cell
            XSSFCellStyle newCellStyle = workbook.createCellStyle();
            newCellStyle.cloneStyleFrom(oldCell.getCellStyle());
            newCell.setCellStyle(newCellStyle);

            // If there is a cell comment, copy
            if (oldCell.getCellComment() != null) {
                newCell.setCellComment(oldCell.getCellComment());
            }

            // If there is a cell hyperlink, copy
            if (oldCell.getHyperlink() != null) {
                newCell.setHyperlink(oldCell.getHyperlink());
            }

            // Set the cell data type
            newCell.setCellType(oldCell.getCellTypeEnum());

            // Set the cell data value
            if (oldCell.getCellTypeEnum().equals(CellType.BLANK)) {
                newCell.setCellValue(oldCell.getStringCellValue());
            } else if (oldCell.getCellTypeEnum().equals(CellType.BOOLEAN)) {
                newCell.setCellValue(oldCell.getBooleanCellValue());
            } else if (oldCell.getCellTypeEnum().equals(CellType.ERROR)) {
                newCell.setCellErrorValue(oldCell.getErrorCellValue());
            } else if (oldCell.getCellTypeEnum().equals(CellType.FORMULA)) {
                newCell.setCellFormula(oldCell.getCellFormula());
            } else if (oldCell.getCellTypeEnum().equals(CellType.NUMERIC)) {
                newCell.setCellValue(oldCell.getNumericCellValue());
            } else if (oldCell.getCellTypeEnum().equals(CellType.STRING)) {
                newCell.setCellValue("COPIED: " + oldCell.getRichStringCellValue());
            }
        }

        // If there are are any merged regions in the source row, copy to new row
        for (int i = 0; i < worksheet.getNumMergedRegions(); i++) {
            CellRangeAddress cellRangeAddress = worksheet.getMergedRegion(i);
            if (cellRangeAddress != null && sourceRow != null && cellRangeAddress.getFirstRow() == sourceRow.getRowNum()) {
                CellRangeAddress newCellRangeAddress = new CellRangeAddress(newRow.getRowNum(),
                        (newRow.getRowNum() +
                                (cellRangeAddress.getLastRow() - cellRangeAddress.getFirstRow()
                                )),
                        cellRangeAddress.getFirstColumn(),
                        cellRangeAddress.getLastColumn());
                worksheet.addMergedRegion(newCellRangeAddress);
            }
        }
    }
}
