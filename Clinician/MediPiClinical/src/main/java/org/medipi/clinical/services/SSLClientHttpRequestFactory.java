/*
 Copyright 2016  Richard Robinson @ NHS Digital <rrobinson@nhs.net>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.medipi.clinical.services;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.medipi.clinical.logging.MediPiLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;

/**
 * Class to create a ClientHttpRequestFactory from the trust and keystores and configured passwords
 * @author rick@robinsonhq.com
 */
@Component
public class SSLClientHttpRequestFactory {

    @Value("${medipi.clinical.keystore.location}")
    private String keystoreLocation;
    @Value("${medipi.clinical.keystore.password}")
    private String keystorePass;
    @Value("${medipi.clinical.truststore.location}")
    private String truststoreLocation;
    @Value("${medipi.clinical.truststore.password}")
    private String truststorePass;

    public SSLClientHttpRequestFactory() {

    }

    public ClientHttpRequestFactory getClientHttpRequestFactory() throws Exception {
        //Create RestTemplate to send message to MediPiConcentrator
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(createSSLContext(), NoopHostnameVerifier.INSTANCE);
        HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();
        ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return requestFactory;
    }

    private SSLContext createSSLContext() throws Exception {
        if (truststoreLocation == null || truststoreLocation.trim().equals("")) {
            MediPiLogger.getInstance().log(SSLClientHttpRequestFactory.class
                    .getName() + "constructor", "MediPi truststore is not set");
            System.out.println("MediPi truststore is not set and secure transmission of data will not work");

        }
        if (truststorePass == null || truststorePass.trim().equals("")) {
            MediPiLogger.getInstance().log(SSLClientHttpRequestFactory.class
                    .getName() + "constructor", "MediPi truststore password is not set");
            System.out.println("MediPi truststore password is not set and secure transmission of data will not work");

        }
        if (keystoreLocation == null || keystoreLocation.trim().equals("")) {
            MediPiLogger.getInstance().log(SSLClientHttpRequestFactory.class
                    .getName() + "constructor", "MediPi keystore is not set");
            System.out.println("MediPi keystore is not set and secure transmission of data will not work");

        }
        if (keystorePass == null || keystorePass.trim().equals("")) {
            MediPiLogger.getInstance().log(SSLClientHttpRequestFactory.class
                    .getName() + "constructor", "MediPi keystore password is not set");
            System.out.println("MediPi keystore password is not set and secure transmission of data will not work");
        }
        KeyStore trustStore = loadStore(truststoreLocation, truststorePass);

        TrustManagerFactory tmf
                = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        KeyStore keyStore = loadStore(keystoreLocation, keystorePass);

        KeyManagerFactory kmf
                = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keystorePass.toCharArray());
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

        return sslContext;
    }

    private KeyStore loadStore(String trustStoreFile, String password) throws Exception {
        KeyStore store = KeyStore.getInstance("JKS");
        store.load(new FileInputStream(trustStoreFile), password.toCharArray());
        return store;
    }
}
