package kz.kcell.flow.vpnportprocess.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public interface SambaService {
    InputStream readIpVpnConnect();

    InputStream readIpVpnStatistics();

    void writeIpVpnConnect(ByteArrayOutputStream byteArrayOutputStream);
}
