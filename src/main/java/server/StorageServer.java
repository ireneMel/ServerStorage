package server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import database.StorageDB;
import lombok.SneakyThrows;

import javax.net.ssl.*;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.InetSocketAddress;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

public class StorageServer {
    private HttpServer server;
    private HttpsServer httpsServer;
    private SSLContext sslContext;
    private final StorageDB db;

    StorageServer(int port) {
        db = new StorageDB();
        db.initialization("OurDb");

        try {
//            server = HttpServer.create(new InetSocketAddress(port), 0);
            httpsServer = HttpsServer.create(new InetSocketAddress(port), 0);
            sslContext = SSLContext.getInstance("TLS");
            configure();
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    private void configure() {        // initialise the keystore

        // initialise the keystore
        char[] Password = "123123".toCharArray();

        KeyStore Key_Store = KeyStore.getInstance("JKS");
        FileInputStream Input_Stream = new FileInputStream("cacerts.jks");
        Key_Store.load(Input_Stream, Password);

//        final PrivateKey key = createPrivateKey(new File("key.pem"));
//        Key_Store.setKeyEntry("key.pem", key, Password, cert);

        // set up the key manager factory
        KeyManagerFactory Key_Manager = KeyManagerFactory.getInstance("SunX509");
        Key_Manager.init(Key_Store, Password);

        // set up the trust manager factory
        TrustManagerFactory Trust_Manager = TrustManagerFactory.getInstance("SunX509");
        Trust_Manager.init(Key_Store);

        // set up the HTTPS context and parameters
        sslContext.init(Key_Manager.getKeyManagers(), Trust_Manager.getTrustManagers(), null);
        httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
            public void configure(HttpsParameters params) {
                try {
                    // initialise the SSL context
                    SSLContext SSL_Context = getSSLContext();
                    SSLEngine SSL_Engine = SSL_Context.createSSLEngine();
                    params.setNeedClientAuth(false);
                    params.setCipherSuites(SSL_Engine.getEnabledCipherSuites());
                    params.setProtocols(SSL_Engine.getEnabledProtocols());

                    // Set the SSL parameters
                    SSLParameters SSL_Parameters = SSL_Context.getSupportedSSLParameters();
                    params.setSSLParameters(SSL_Parameters);
                    System.out.println("The HTTPS server is connected");

                } catch (Exception ex) {
                    System.out.println("Failed to create the HTTPS port");
                }
            }
        });
    }

    private void startServer() {
        server.createContext("/api", new ApiHandler(db));
        server.setExecutor(null);
        server.start();
    }

    private void startHttpsServer() {
        httpsServer.createContext("/api", new ApiHandler(db));
        httpsServer.setExecutor(null); // creates a default executor
        httpsServer.start();
    }

    public static void main(String[] args) {
//        new StorageServer(8765).startServer();
        new StorageServer(8765).startHttpsServer();
    }
}
