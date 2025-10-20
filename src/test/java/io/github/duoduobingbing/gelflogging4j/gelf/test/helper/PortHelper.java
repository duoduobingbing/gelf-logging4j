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

    private static final int PORT_RANGE_PLUS_ONE = PORT_RANGE_MAX - PORT_RANGE_MIN + 1;

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
        int candidatePort;
        int searchCounter = 0;
        do {
            if (++searchCounter > MAX_ATTEMPTS) {
                throw new RuntimeException(
                        String.format(
                                "Could not find an available TCP port in the range [%d, %d] after %d attempts",
                                PORT_RANGE_MIN, PORT_RANGE_MAX, MAX_ATTEMPTS
                        )
                );
            }

            candidatePort = PORT_RANGE_MIN + random.nextInt(PORT_RANGE_PLUS_ONE);
        }
        while (!isPortAvailable(candidatePort));

        return candidatePort;
    }


}
