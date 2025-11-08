package io.github.duoduobingbing.gelflogging4j.gelf.test.helper;

import javax.net.ServerSocketFactory;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

public class PortHelper {

    /**
     * The minimum value for port ranges used when finding an available TCP port.
     */
    static final int PORT_RANGE_MIN = 1024;

    /**
     * The maximum value for port ranges used when finding an available TCP port.
     */
    static final int PORT_RANGE_MAX = 65535;

    private static final int MAX_ATTEMPTS = 1_000;

    private static final Random random;

    static {
        try {
            random = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isPortAvailable(int port) {
        try {
            ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(port, 1, InetAddress.getByName("localhost"));
            serverSocket.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static int findAvailableTcpPort() {
        return findAvailableTcpPort(PORT_RANGE_MIN, PORT_RANGE_MAX);
    }

    public static int findAvailableTcpPort(int minPort, int maxPort) {
        int candidatePort;
        int searchCounter = 0;
        do {
            if (++searchCounter > MAX_ATTEMPTS) {
                throw new RuntimeException(
                        String.format(
                                "Could not find an available TCP port in the range [%d, %d] after %d attempts",
                                minPort, maxPort, MAX_ATTEMPTS
                        )
                );
            }

            int portRangePlusOne = maxPort - minPort + 1;
            candidatePort = minPort + random.nextInt(portRangePlusOne);
        }
        while (!isPortAvailable(candidatePort));

        return candidatePort;
    }


}
