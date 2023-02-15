package kz.kcell.flow.vpnportprocess.service;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class SambaService {

    InputStream readIpVpnConnect() {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        };
    }

    InputStream readIpVpnUtilization() {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        };
    }

    void writeIpVpnConnect(ByteArrayOutputStream out) {}
}
