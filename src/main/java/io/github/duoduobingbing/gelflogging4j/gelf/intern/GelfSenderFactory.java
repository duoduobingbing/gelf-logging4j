package io.github.duoduobingbing.gelflogging4j.gelf.intern;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Supplier;

import io.github.duoduobingbing.gelflogging4j.gelf.intern.sender.DefaultGelfSenderProvider;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.sender.KafkaGelfSenderProvider;
import io.github.duoduobingbing.gelflogging4j.gelf.intern.sender.RedisGelfSenderProvider;

/**
 * Factory to create a {@link GelfSender} based on the host and protocol details. This factory uses Java's {@link ServiceLoader}
 * mechanism to discover classes implementing {@link GelfSenderProvider}.
 *
 * @author Mark Paluch
 * @author Aleksandar Stojadinovic
 * @author Rifat Döver
 * @author duoduobingbing
 * @since 26.09.13 15:12
 */
public final class GelfSenderFactory {

    private GelfSenderFactory() {
        // no instance allowed
    }

    static record LazyGelfSenderConfiguration(
            Supplier<String> hostSupplier,
            Supplier<Integer> portSupplier,
            Supplier<ErrorReporter> errorReporterSupplier,
            Supplier<Map<String, Object>> configSupplier
    )
            implements GelfSenderConfiguration {

        @Override
        public String getHost() {
            return hostSupplier.get();
        }

        @Override
        public int getPort() {
            return portSupplier.get();
        }

        @Override
        public ErrorReporter getErrorReporter() {
            return errorReporterSupplier.get();
        }

        @Override
        public Map<String, Object> getSpecificConfigurations() {
            return configSupplier.get();
        }
    }

    /**
     * Create a GelfSender based on the configuration.
     *
     * @param hostAndPortProvider          the host and port
     * @param errorReporter                the error reporter
     * @param senderSpecificConfigurations configuration map
     * @return a new {@link GelfSender} instance
     */
    public static GelfSender createSender(
            final HostAndPortProvider hostAndPortProvider,
            final ErrorReporter errorReporter,
            final Map<String, Object> senderSpecificConfigurations
    ) {

        final GelfSenderConfiguration senderConfiguration = new LazyGelfSenderConfiguration(
                hostAndPortProvider::getHost,
                hostAndPortProvider::getPort,
                () -> errorReporter,
                () -> senderSpecificConfigurations
        );

        return createSender(senderConfiguration);
    }

    /**
     * Create a GelfSender based on the configuration.
     *
     * @param senderConfiguration the configuration
     * @return a new {@link GelfSender} instance
     */
    public static GelfSender createSender(GelfSenderConfiguration senderConfiguration) {

        ErrorReporter errorReporter = senderConfiguration.getErrorReporter();
        if (senderConfiguration.getHost() == null) {
            senderConfiguration.getErrorReporter().reportError("GELF server hostname is empty!", null);
            return null;
        }


        try {
            for (GelfSenderProvider provider : SenderProviderHolder.getSenderProvider()) {
                if (provider.supports(senderConfiguration.getHost())) {
                    return provider.create(senderConfiguration);
                }
            }

            senderConfiguration
                    .getErrorReporter()
                    .reportError("No sender found for host " + senderConfiguration.getHost(),null);

        } catch (UnknownHostException e) {
            errorReporter.reportError("Unknown GELF server hostname:" + senderConfiguration.getHost(), e);
        } catch (SocketException e) {
            errorReporter.reportError("Socket exception: " + e.getMessage(), e);
        } catch (IOException e) {
            errorReporter.reportError("IO exception: " + e.getMessage(), e);
        }


        return null;
    }

    public static void addGelfSenderProvider(GelfSenderProvider provider) {
        SenderProviderHolder.addSenderProvider(provider);
    }

    public static void removeGelfSenderProvider(GelfSenderProvider provider) {
        SenderProviderHolder.removeSenderProvider(provider);
    }

    public static void removeAllAddedSenderProviders() {
        SenderProviderHolder.removeAllAddedSenderProviders();
    }

    // For thread safe lazy intialization of provider list
    private static class SenderProviderHolder {

        private static ServiceLoader<GelfSenderProvider> gelfSenderProvider = ServiceLoader.load(GelfSenderProvider.class);
        private static List<GelfSenderProvider> providerList = new ArrayList<>();
        private static List<GelfSenderProvider> addedProviders = new ArrayList<>();

        static {
            Iterator<GelfSenderProvider> iter = gelfSenderProvider.iterator();
            while (iter.hasNext()) {
                providerList.add(iter.next());
            }
            providerList.add(new RedisGelfSenderProvider());
            providerList.add(new KafkaGelfSenderProvider());
            providerList.add(new DefaultGelfSenderProvider());
        }

        static List<GelfSenderProvider> getSenderProvider() {
            return providerList;
        }

        static void addSenderProvider(GelfSenderProvider provider) {
            synchronized (providerList) {
                addedProviders.add(provider);
                if (!providerList.contains(provider)) {
                    providerList.add(0, provider); // To take precedence over built-in providers
                }
            }
        }

        static void removeAllAddedSenderProviders() {
            synchronized (providerList) {
                providerList.removeAll(addedProviders);
                addedProviders.clear();
            }
        }

        static void removeSenderProvider(GelfSenderProvider provider) {
            synchronized (providerList) {
                addedProviders.remove(provider);
                providerList.remove(provider);
            }
        }
    }
}
