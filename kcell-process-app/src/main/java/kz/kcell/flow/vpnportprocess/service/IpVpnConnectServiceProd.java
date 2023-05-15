package kz.kcell.flow.vpnportprocess.service;

import kz.kcell.flow.assets.dto.VpnOutputDto;
import kz.kcell.flow.utils.Pair;
import kz.kcell.flow.vpnportprocess.variable.VpnCamVar;
import lombok.RequiredArgsConstructor;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "ipvpn.connect.file.enabled", havingValue = "true")
@RequiredArgsConstructor
public class IpVpnConnectServiceProd implements IpVpnConnectService {

    private final SambaService sambaService;
    private final static List<String> DISTRICTS = new ArrayList<>(Arrays.asList("Кокшетау (г.а.)", "Степногорск (г.а.)",
        "Актобе (г.а.)", "Талдыкорган (г.а.)", "Конаев (г.а.)", "Текели (г.а.)", "Атырау (г.а.)", "Уральск (г.а.)",
        "Тараз (г.а.)", "Караганда (г.а.)", "Балхаш (г.а.)", "Жезказган (г.а.)", "Каражал (г.а.)", "Приозерск (г.а.)",
        "Сарань (г.а.)", "Сатпаев (г.а.)", "Темиртау (г.а.)", "Шахтинск (г.а.)", "Костанай (г.а.)", "Аркалык (г.а.)",
        "Лисаковск (г.а.)", "Рудный (г.а.)", "Кызылорда (г.а.)", "Байконыр (г.а.)", "Актау (г.а.)", "Жанаозен (г.а.)",
        "Павлодар (г.а.)", "Аксу (г.а.)", "Экибастуз (г.а.)", "Петропавловск (г.а.)", "Туркестан (г.а.)", "Арысь (г.а.)",
        "Кентау (г.а.)", "Усть-Каменогорск (г.а.)", "Курчатов (г.а.)", "Риддер (г.а.)", "Семей (г.а.)", "Нур-Султан (г.а.)",
        "Алматы (г.а.)", "Шымкент (г.а.)"));

    @Override
    public String addNewVlanToIpVpnConnectFile(String serviceType) {
        ZipSecureFile.setMinInflateRatio(0);

        InputStream in = sambaService.readIpVpnConnect();
        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Sheet sheet = workbook.getSheet("L2 VLAN");

        // get a new vlan value
        Row row = sheet.getRow(sheet.getLastRowNum());
        Cell cell = row.getCell(0);
        int vlan = ((Double) cell.getNumericCellValue()).intValue() + 1;

        CellStyle cellStyle = workbook.createCellStyle();
        setBordersAndHorizontalAlignmentToCenter(cellStyle);

        // add a new row
        row = sheet.createRow(sheet.getLastRowNum() + 1);
        cell = row.createCell(0); //VLAN
        cell.setCellValue(vlan);
        cell = row.createCell(1); // VPN
        cell.setCellValue(serviceType);

        for (int i = 0; i <= 4; i++) {
            cell = row.getCell(i) != null ? row.getCell(i) : row.createCell(i);
            cell.setCellStyle(cellStyle);
        }


        // write the changes back to the SMB/CIFS server
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            workbook.write(out);
            out.close();
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        sambaService.writeIpVpnConnect(out);

        return String.valueOf(vlan);
    }

    @Override
    public Pair<String, Integer> addNewVpnToIpVpnConnectFile(VpnOutputDto vpn, String serviceType, String vlan) {
        ZipSecureFile.setMinInflateRatio(0);

        InputStream in = sambaService.readIpVpnConnect();
        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // search for port number in VPN sheet
        Sheet sheet = workbook.getSheet("VPN");
        int rowIndexPortNumberFoundInVpnSheet = searchForValueAtCell(sheet, 19, vpn.getPort().getPortNumber());

        // search for port number in 3G_REGIONS sheet
        sheet = workbook.getSheet("3G_REGIONS");
        int rowIndexPortNumberFoundIn3gRegionsSheet = searchForValueAtCell(sheet, 19, vpn.getPort().getPortNumber());

        Row row;
        if (rowIndexPortNumberFoundInVpnSheet != -1) {
            sheet = workbook.getSheet("VPN");
            sheet.shiftRows(rowIndexPortNumberFoundInVpnSheet + 1, sheet.getLastRowNum(), 1);
            row = sheet.createRow(rowIndexPortNumberFoundInVpnSheet + 1);
        } else if (rowIndexPortNumberFoundIn3gRegionsSheet != -1) {
            sheet = workbook.getSheet("3G_REGIONS");
            sheet.shiftRows(rowIndexPortNumberFoundIn3gRegionsSheet + 1, sheet.getLastRowNum(), 1, true, true);
            row = sheet.createRow(rowIndexPortNumberFoundIn3gRegionsSheet + 1);
        } else if (DISTRICTS.contains(vpn.getNearEndAddress().getCityId().getDistrictId().getName())) {
            sheet = workbook.getSheet("VPN");
            row = sheet.createRow(findLastNonBlankRow(sheet) + 1);
        } else {
            sheet = workbook.getSheet("3G_REGIONS");
            row = sheet.createRow(findLastNonBlankRow(sheet) + 1);
        }

        CellStyle cellStyle = workbook.createCellStyle();
        setBordersAndHorizontalAlignmentToCenter(cellStyle);

        Cell cell = row.createCell(1); // VPN
        cell.setCellValue(serviceType);
        cell = row.createCell(2);// ID
        cell.setCellValue("ORDERED");
        cell = row.createCell(3);// Network / 30
        cell = row.createCell(4);// IP address KZT
        cell = row.createCell(5);// IP address Kcell
        cell = row.createCell(6);// Vlan
        cell.setCellValue(vlan);
        cell = row.createCell(7);// Capacity, Mbps
        cell.setCellValue(vpn.getServiceCapacity());
        cell = row.createCell(8);// AS KZT
        cell.setCellValue(vpn.getProviderAs());
        cell = row.createCell(9);// AS Kcell
        cell.setCellValue(vpn.getKcellAs());
        cell = row.createCell(10);// Termination point
        cell = row.createCell(11);// Interface
        cell.setCellValue(vpn.getPort().getPortCapacity() + vpn.getPort().getPortCapacityUnit());
        cell = row.createCell(19);// City
        cell.setCellValue(vpn.getPort().getPortNumber());
        cell = row.createCell(20);// Status
        cell.setCellValue("Ordered");

        for (int i = 0; i <= 12; i++) {
            cell = row.getCell(i) != null ? row.getCell(i) : row.createCell(i);
            cell.setCellStyle(cellStyle);
        }
        for (int i = 19; i <= 20; i++) {
            cell = row.getCell(i) != null ? row.getCell(i) : row.createCell(i);
            cell.setCellStyle(cellStyle);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            workbook.write(out);
            out.close();
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        sambaService.writeIpVpnConnect(out);

        return new Pair<>(sheet.getSheetName(), row.getRowNum());
    }

    @Override
    public void changeStatus(String vpnNumber, String status) {
        changeStatusAndCapacity(vpnNumber, status, null);
    }

    public void changeAddedServiceStatus(VpnCamVar vpn, Pair<String, Integer> rowNumber, String status) {
        ZipSecureFile.setMinInflateRatio(0);

        InputStream in = sambaService.readIpVpnConnect();
        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Sheet sheet = workbook.getSheet(rowNumber.getKey());
        Row row = sheet.getRow(rowNumber.getValue());

        // check if row is the same as VpnCamVar
        if (row.getCell(7).getNumericCellValue() != vpn.getServiceCapacity() // Capacity, Mbps
            || !row.getCell(11).getStringCellValue().equals(vpn.getPort().getPortCapacity() + vpn.getPort().getPortCapacityUnit()) // Interface
            || !row.getCell(19).getStringCellValue().equals(vpn.getPort().getPortNumber())) { // City
            throw new RuntimeException("Row number of VPN:" + vpn + " has been changed!");
        }

        // set row status
        Cell cell = row.getCell(20);
        cell.setCellValue(status);


        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            workbook.write(out);
            out.close();
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        sambaService.writeIpVpnConnect(out);
    }


    @Override
    public void changeStatusAndCapacity(String vpnNumber, String status, Integer modifiedCapacity) {
        ZipSecureFile.setMinInflateRatio(0);

        InputStream in = sambaService.readIpVpnConnect();
        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // search for port number in VPN sheet
        Sheet sheet = workbook.getSheet("VPN");
        int rowIndexVpnNumberFoundInVpnSheet = searchForValueAtCell(sheet, 2, vpnNumber);

        // search for port number in 3G_REGIONS sheet
        sheet = workbook.getSheet("3G_REGIONS");
        int rowIndexVpnNumberFoundIn3gRegionsSheet = searchForValueAtCell(sheet, 2, vpnNumber);

        int rowIndexToChangeValueAt = -1;
        if (rowIndexVpnNumberFoundInVpnSheet != -1) {
            sheet = workbook.getSheet("VPN");
            rowIndexToChangeValueAt = rowIndexVpnNumberFoundInVpnSheet;
        } else if (rowIndexVpnNumberFoundIn3gRegionsSheet != -1) {
            sheet = workbook.getSheet("3G_REGIONS");
            rowIndexToChangeValueAt = rowIndexVpnNumberFoundIn3gRegionsSheet;
        } else {
            throw new RuntimeException("VPN Number:" + vpnNumber + " not found in IPVPN CONNECT.xlsm");
        }

        Row row = sheet.getRow(rowIndexToChangeValueAt);

        Cell cell = row.getCell(20) != null ? row.getCell(20) : row.createCell(20);
        cell.setCellValue(status);

        if (modifiedCapacity != null) {
            cell = row.getCell(7) != null ? row.getCell(7) : row.createCell(7);
            cell.setCellValue(modifiedCapacity);

        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            workbook.write(out);
            out.close();
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        sambaService.writeIpVpnConnect(out);
    }

    @Override
    public void changeStatusAndCapacityVpn(String portNumber, String status, Integer modifiedCapacity) {
        ZipSecureFile.setMinInflateRatio(0);

        InputStream in = sambaService.readIpVpnConnect();
        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // search for port number in VPN sheet
        Sheet sheet = workbook.getSheet("VPN");
        int rowIndexVpnNumberFoundInVpnSheet = searchForValueAtCell(sheet, 19, portNumber);//Cyrilic Name

        // search for port number in 3G_REGIONS sheet
        sheet = workbook.getSheet("3G_REGIONS");
        int rowIndexVpnNumberFoundIn3gRegionsSheet = searchForValueAtCell(sheet, 19, portNumber);//Cyrilic Name

        int rowIndexToChangeValueAt = -1;
        if (rowIndexVpnNumberFoundInVpnSheet != -1) {
            sheet = workbook.getSheet("VPN");
            rowIndexToChangeValueAt = rowIndexVpnNumberFoundInVpnSheet;
        } else if (rowIndexVpnNumberFoundIn3gRegionsSheet != -1) {
            sheet = workbook.getSheet("3G_REGIONS");
            rowIndexToChangeValueAt = rowIndexVpnNumberFoundIn3gRegionsSheet;
        } else {
            throw new RuntimeException("Port Number:" + portNumber + " not found in IPVPN CONNECT.xlsm");
        }

        Row row = sheet.getRow(rowIndexToChangeValueAt);

        Cell cell = row.getCell(20) != null ? row.getCell(20) : row.createCell(20);
        cell.setCellValue(status);

        if (modifiedCapacity != null) {
            cell = row.getCell(7) != null ? row.getCell(7) : row.createCell(7);
            cell.setCellValue(modifiedCapacity);

        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            workbook.write(out);
            out.close();
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        sambaService.writeIpVpnConnect(out);
    }

    @Override
    public boolean checkUtilization(String vpnNumber, String serviceType) {
        ZipSecureFile.setMinInflateRatio(0);

        InputStream in = sambaService.readIpVpnStatistics();
        Workbook workbook;
        try {
            workbook = new XSSFWorkbook(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<String> serviceTypes = new ArrayList<>(Arrays.asList("Abis", "IuB", "ABIS", "IUB", "MuB", "MUB", "GB", "GB1", "PORT"));
        double utilizationMax = serviceTypes.contains(serviceType) ? 0.80 : 0.70;
        Sheet sheet = workbook.getSheet("All data");
        int rowIndex = searchForValueAtCell(sheet, 1, vpnNumber);
        if (rowIndex != 1) {
            Row row = sheet.getRow(rowIndex);
            Cell cell = row.getCell(64);
            double utilization = cell.getNumericCellValue();
            return utilization < utilizationMax;
        } else {
            throw new RuntimeException("VPN Number not found in IPVPN's yyyy.MM.dd.xlsx");
        }
    }

    @Override
    public Map<String, Double> findVpnNumbersThatMeetUtilizationCriteria() {
        ZipSecureFile.setMinInflateRatio(0);

        InputStream in = sambaService.readIpVpnStatistics();
        Workbook workbook;
        try {
            workbook = new XSSFWorkbook(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<String> serviceTypes = new ArrayList<>(Arrays.asList("Abis", "IuB", "ABIS", "IUB", "MuB", "MUB", "GB", "GB1", "PORT"));
        Map<String, Double> result = new HashMap<>();
        Sheet sheet = workbook.getSheet("All data");

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            Cell cell = row.getCell(64);
            if (cell != null && cell.getCellType() == CellType.FORMULA && cell.getCachedFormulaResultType() == CellType.NUMERIC) {
                double utilization = cell.getNumericCellValue();
                String vpnNumber = row.getCell(1).getCellType() == CellType.STRING ? row.getCell(1).getStringCellValue() : String.valueOf(row.getCell(1).getNumericCellValue());
                String serviceType = row.getCell(4).getStringCellValue();
                double utilizationMax = serviceTypes.contains(serviceType) ? 0.80 : 0.70;

                if (utilization >= utilizationMax && !vpnNumber.isEmpty()) {
                    result.put(vpnNumber.trim(), utilization);
                }
            }

        }

        return result;
    }

    @Override
    public void makeChangesToAddedService(VpnOutputDto vpn, Pair<String, Integer> rowNumber) {
        ZipSecureFile.setMinInflateRatio(0);

        InputStream in = sambaService.readIpVpnConnect();
        Workbook workbook;
        try {
            workbook = new XSSFWorkbook(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // get row
        Sheet sheet = workbook.getSheet(rowNumber.getKey());
        Row row = sheet.getRow(rowNumber.getValue());

        // check if row is the same as VpnOutputDto
        if (row.getCell(7).getNumericCellValue() != vpn.getServiceCapacity() // Capacity, Mbps
            || !row.getCell(11).getStringCellValue().equals(vpn.getPort().getPortCapacity() + vpn.getPort().getPortCapacityUnit()) // Interface
            || !row.getCell(19).getStringCellValue().equals(vpn.getPort().getPortNumber())// City
            || !row.getCell(20).getStringCellValue().equals(vpn.getStatus())) { // Status
            throw new RuntimeException("Row number of VPN:" + vpn + " has been changed!");
        }

        //vlan, provider_ip, kcell_ip, provider_as, kcell_as
        Cell cell = row.getCell(4);// IP address KZT
        cell.setCellValue(vpn.getProviderIp());
        cell = row.getCell(5);// IP address Kcell
        cell.setCellValue(vpn.getKcellIp());
        cell = row.getCell(6);// Vlan
        cell.setCellValue(vpn.getVlan());
        cell = row.getCell(8);// AS KZT
        cell.setCellValue(vpn.getProviderAs());
        cell = row.getCell(9);// AS Kcell
        cell.setCellValue(vpn.getKcellAs());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            workbook.write(out);
            out.close();
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        sambaService.writeIpVpnConnect(out);
    }

    @Override
    public void deleteAddedService(VpnCamVar vpn, Pair<String, Integer> rowNumber) {
        ZipSecureFile.setMinInflateRatio(0);

        InputStream in = sambaService.readIpVpnConnect();
        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Sheet sheet = workbook.getSheet("L2 VLAN");

        // find add vlan value
        int rowIndex = searchForNumericValueAtCell(sheet, 0, Double.parseDouble(vpn.getVlan()));
        // remove row with shift up all rows
        if (rowIndex == sheet.getLastRowNum()) {
            sheet.removeRow(sheet.getRow(rowIndex));
        } else {
            sheet.shiftRows(rowIndex + 1, sheet.getLastRowNum(), -1);
        }

        sheet = workbook.getSheet(rowNumber.getKey());
        Row row = sheet.getRow(rowNumber.getValue());

        // check if row is the same as VpnCamVar
        if (row.getCell(7).getNumericCellValue() != vpn.getServiceCapacity() // Capacity, Mbps
            || !row.getCell(11).getStringCellValue().equals(vpn.getPort().getPortCapacity() + vpn.getPort().getPortCapacityUnit()) // Interface
            || !row.getCell(19).getStringCellValue().equals(vpn.getPort().getPortNumber())) { // City
            throw new RuntimeException("Row number of VPN:" + vpn + " has been changed!");
        }

        // delete row with shift up all rows
        sheet.shiftRows(rowNumber.getValue() + 1, sheet.getLastRowNum(), -1);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            workbook.write(out);
            out.close();
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        sambaService.writeIpVpnConnect(out);
    }

    public void deleteDisbandedVpn(String vpnNumber) {
        ZipSecureFile.setMinInflateRatio(0);

        InputStream in = sambaService.readIpVpnConnect();
        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // search for port number in VPN sheet
        Sheet sheet = workbook.getSheet("VPN");
        int rowIndexVpnNumberFoundInVpnSheet = searchForValueAtCell(sheet, 2, vpnNumber);

        // search for port number in 3G_REGIONS sheet
        sheet = workbook.getSheet("3G_REGIONS");
        int rowIndexVpnNumberFoundIn3gRegionsSheet = searchForValueAtCell(sheet, 2, vpnNumber);

        int rowIndexToChangeValueAt = -1;
        if (rowIndexVpnNumberFoundInVpnSheet != -1) {
            sheet = workbook.getSheet("VPN");
            rowIndexToChangeValueAt = rowIndexVpnNumberFoundInVpnSheet;
        } else if (rowIndexVpnNumberFoundIn3gRegionsSheet != -1) {
            sheet = workbook.getSheet("3G_REGIONS");
            rowIndexToChangeValueAt = rowIndexVpnNumberFoundIn3gRegionsSheet;
        } else {
            throw new RuntimeException("VPN Number:" + vpnNumber + " not found in IPVPN CONNECT.xlsm");
        }

        // delete row with shift up all rows
        sheet.shiftRows(rowIndexToChangeValueAt + 1, sheet.getLastRowNum(), -1);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            workbook.write(out);
            out.close();
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        sambaService.writeIpVpnConnect(out);
    }

    @Override
    public void changePortCapacity(String portNumber, String portCapacity, String status) {
        ZipSecureFile.setMinInflateRatio(0);

        InputStream in = sambaService.readIpVpnConnect();
        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // search for port number in VPN sheet
        Sheet sheet = workbook.getSheet("VPN");
        List<Integer> foundRowNumbersInVpnSheet = searchForValuesAtCell(sheet, 19, portNumber);
        for (int rowNumber : foundRowNumbersInVpnSheet) {
            Row row = sheet.getRow(rowNumber);
            if (status != null) {
                Cell cell = row.getCell(20) != null ? row.getCell(20) : row.createCell(20);
                cell.setCellValue(status);
            }
            if (portCapacity != null) {
                Cell cell = row.getCell(11) != null ? row.getCell(11) : row.createCell(11);
                cell.setCellValue(portCapacity);
            }
        }

        // search for port number in 3G_REGIONS sheet
        sheet = workbook.getSheet("3G_REGIONS");
        List<Integer> foundRowNumbersIn3gSheet = searchForValuesAtCell(sheet, 19, portNumber);
        for (int rowNumber : foundRowNumbersIn3gSheet) {
            if (status != null) {
                Row row = sheet.getRow(rowNumber);
                Cell cell = row.getCell(20) != null ? row.getCell(20) : row.createCell(20);
                cell.setCellValue(status);
            }
            if (portCapacity == null) {
                Row row = sheet.getRow(rowNumber);
                Cell cell = row.getCell(11) != null ? row.getCell(11) : row.createCell(11);
                cell.setCellValue(portCapacity);
            }
        }

        if (foundRowNumbersInVpnSheet.isEmpty() && foundRowNumbersIn3gSheet.isEmpty()) {
            throw new RuntimeException("Port number:" + portNumber + " not found in IPVPN CONNECT.xlsm");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            workbook.write(out);
            out.close();
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        sambaService.writeIpVpnConnect(out);
    }

    private void setBordersAndHorizontalAlignmentToCenter(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
    }

    private int searchForValueAtCell(Sheet sheet, int cellIndex, String value) {
        for (Row row : sheet) {
            Cell cell = row.getCell(cellIndex);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                if (cell.getStringCellValue().equals(value)) {
                    return row.getRowNum();
                }
            }
        }
        return -1;
    }

    private int searchForNumericValueAtCell(Sheet sheet, int cellIndex, double value) {
        for (Row row : sheet) {
            Cell cell = row.getCell(cellIndex);
            if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                if (cell.getNumericCellValue() == value) {
                    return row.getRowNum();
                }
            }
        }
        return -1;
    }

    private List<Integer> searchForValuesAtCell(Sheet sheet, int cellIndex, String value) {
        List<Integer> result = new ArrayList<>();
        for (Row row : sheet) {
            Cell cell = row.getCell(cellIndex);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                if (cell.getStringCellValue().equals(value)) {
                    result.add(row.getRowNum());
                }
            }
        }
        return result;
    }

    private int findLastNonBlankRow(Sheet sheet) {
        for (int i = sheet.getLastRowNum(); i >= 0; i--) {
            Row row = sheet.getRow(i);
            if (row != null) {
                for (Cell cell : row) {
                    if (cell != null && cell.getCellType() != CellType.BLANK) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }
}
