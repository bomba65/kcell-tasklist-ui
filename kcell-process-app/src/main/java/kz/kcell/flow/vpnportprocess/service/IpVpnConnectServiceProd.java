package kz.kcell.flow.vpnportprocess.service;

import kz.kcell.flow.assets.dto.PortOutputDto;
import kz.kcell.flow.assets.dto.VpnOutputDto;
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
@ConditionalOnProperty(name="ipvpn.connect.file.enabled", havingValue = "true")
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
        
        for(int i = 0; i <= 4; i++) {
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
    public void addNewVpnToIpVpnConnectFile(VpnOutputDto vpn, String serviceType, String vlan) {
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
        int rowIndexPortNumberFoundInVpnSheet = searchForValueAtCell(sheet, 0, vpn.getPort().getPortNumber());

        // search for port number in 3G_REGIONS sheet
        sheet = workbook.getSheet("3G_REGIONS");
        int rowIndexPortNumberFoundIn3gRegionsSheet = searchForValueAtCell(sheet, 0, vpn.getPort().getPortNumber());

        if (rowIndexPortNumberFoundInVpnSheet != -1) {
            sheet = workbook.getSheet("VPN");
        } else if (rowIndexPortNumberFoundIn3gRegionsSheet != -1) {
            sheet = workbook.getSheet("3G_REGIONS");
        } else if (DISTRICTS.contains(vpn.getNearEndAddress().getCityId().getDistrictId().getName())) {
            sheet = workbook.getSheet("VPN");
        } else {
            sheet = workbook.getSheet("3G_REGIONS");
        }

        CellStyle cellStyle = workbook.createCellStyle();
        setBordersAndHorizontalAlignmentToCenter(cellStyle);

        Row row = sheet.createRow(findLastNonBlankRow(sheet) + 1);
        Cell cell = row.createCell(0); // City
        cell.setCellValue(vpn.getPort().getPortNumber());
        cell = row.createCell(1); // VPN
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
        cell.setCellValue(vpn.getPort().getPortCapacity()+vpn.getPort().getPortCapacityUnit());
        cell = row.createCell(12);// Status
        cell.setCellValue("Ordered");

        for(int i = 0; i <= 12; i++) {
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
    }

    @Override
    public void changeStatus(String vpnNumber, String status) {
        changeStatusAndCapacity(vpnNumber, status, null);
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
            throw new RuntimeException("VPN Number:" + vpnNumber + " not found in IPVPN CONNECT.xlsm" );
        }

        Row row = sheet.getRow(rowIndexToChangeValueAt);

        Cell cell = row.getCell(12);
        if (cell != null) {
            cell.setCellValue("In process");
        } else {
            throw new RuntimeException("There is no Status cell in IPVPN CONNECT.xlsm for VPN Number:" + vpnNumber);
        }

        if (modifiedCapacity != null) {
            cell = row.getCell(7);
            if (cell != null) {
                cell.setCellValue(modifiedCapacity);
            } else {
                throw new RuntimeException("There is no Capacity cell in IPVPN CONNECT.xlsm for VPN Number:" + vpnNumber);
            }
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

        InputStream in = sambaService.readIpVpnUtilization();
        Workbook workbook;
        try {
            workbook = new XSSFWorkbook(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<String> serviceTypes = new ArrayList<>(Arrays.asList("Abis","IuB","ABIS","IUB","MuB","MUB","GB","GB1","PORT"));
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

        InputStream in = sambaService.readIpVpnUtilization();
        Workbook workbook;
        try {
            workbook = new XSSFWorkbook(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<String> serviceTypes = new ArrayList<>(Arrays.asList("Abis","IuB","ABIS","IUB","MuB","MUB","GB","GB1","PORT"));
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

                if (utilization >= utilizationMax) {
                    result.put(vpnNumber.trim(), utilization);
                }
            }

        }

        return result;
    }

    @Override
    public void makeChangesToAddedService(VpnOutputDto vpn) {}

    @Override
    public void deleteVpn(VpnCamVar vpn) {}

    @Override
    public void changePortCapacity(PortOutputDto port) {
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
