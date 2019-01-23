package com.payline.payment.samsung.pay.utils.http;

import com.payline.payment.samsung.pay.exception.ExternalCommunicationException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import com.payline.pmapi.logger.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static com.payline.payment.samsung.pay.utils.SamsungPayConstants.CONTENT_TYPE;
import static com.payline.payment.samsung.pay.utils.SamsungPayConstants.X_REQUEST_ID;

/**
 * This utility class provides a basic HTTP client to send requests, using OkHttp library.
 * It must be extended to match each payment method needs.
 */
public abstract class AbstractHttpClient {
    private static final Logger LOGGER = LogManager.getLogger(AbstractHttpClient.class);

    private CloseableHttpClient client;

    /**
     * Instantiate a HTTP client.
     *
     * @param connectTimeout Determines the timeout in milliseconds until a connection is established
     * @param requestTimeout The timeout in milliseconds used when requesting a connection from the connection manager
     * @param socketTimeout  Defines the socket timeout (SO_TIMEOUT) in milliseconds, which is the timeout for waiting for data or, put differently, a maximum period inactivity between two consecutive data packets)
     */
    protected AbstractHttpClient(int connectTimeout, int requestTimeout, int socketTimeout) {

        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectTimeout * 1000)
                .setConnectionRequestTimeout(requestTimeout * 1000)
                .setSocketTimeout(socketTimeout * 1000)
                .build();

        this.client = HttpClientBuilder.create()
                .useSystemProperties()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCredentialsProvider(new BasicCredentialsProvider())
                .setSSLSocketFactory(new SSLConnectionSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory(), SSLConnectionSocketFactory.getDefaultHostnameVerifier())).build();

    }

    /**
     * Send a POST request.
     *
     * @param url         URL to call
     * @param path        URL path
     * @param body        Request body
     * @param contentType The content type of the request body
     * @param requestId   The unique identifier for the request
     * @return The response returned from the HTTP call
     * @throws IOException
     */
    protected StringResponse doPost(String url, String path, HttpEntity body, String contentType, String requestId) throws URISyntaxException, ExternalCommunicationException {

        URI uri = new URI(url + path);

        Header[] headers = new Header[2];
        headers[0] = new BasicHeader(CONTENT_TYPE, contentType);
        headers[1] = new BasicHeader(X_REQUEST_ID, requestId);

        final HttpPost httpPostRequest = new HttpPost(uri);
        httpPostRequest.setHeaders(headers);
        httpPostRequest.setEntity(body);

        final long start = System.currentTimeMillis();
        int count = 0;
        StringResponse strResponse = null;

        while (count < 3 && strResponse == null) {
            try (CloseableHttpResponse httpResponse = this.client.execute(httpPostRequest)) {

                strResponse = new StringResponse();
                strResponse.setCode(httpResponse.getStatusLine().getStatusCode());
                strResponse.setMessage(httpResponse.getStatusLine().getReasonPhrase());

                if (httpResponse.getEntity() != null) {
                    final String responseAsString = EntityUtils.toString(httpResponse.getEntity());
                    strResponse.setContent(responseAsString);
                }

            } catch (final IOException e) {
                LOGGER.error("Error while partner call [T: {}ms]", System.currentTimeMillis() - start, e);
                strResponse = null;
            } finally {
                count++;
            }
        }

        if (strResponse == null) {
            throw new ExternalCommunicationException("Partner response empty");
        }

        return strResponse;
    }

    /**
     * Send a GET request
     *
     * @param url             URL to call
     * @param path            URL path
     * @param queryAttributes Query attribute map
     * @param contentType     The content type of the request body
     * @param requestId       The unique identifier for the request
     * @return The response returned from the HTTP call
     * @throws IOException
     */
    protected StringResponse doGet(String url, String path, Map<String, String> queryAttributes, String contentType, String requestId) throws URISyntaxException, ExternalCommunicationException {

        URIBuilder builder = new URIBuilder(url + path);
        if (queryAttributes != null && !queryAttributes.isEmpty()) {
            for (String key : queryAttributes.keySet()) {
                builder.setParameter(key, queryAttributes.get(key));
            }
        }
        final URI uri = builder.build();

        Header[] headers = new Header[2];
        headers[0] = new BasicHeader(CONTENT_TYPE, contentType);
        headers[1] = new BasicHeader(X_REQUEST_ID, requestId);

        final HttpGet httpGetRequest = new HttpGet(uri);
        httpGetRequest.setHeaders(headers);

        final long start = System.currentTimeMillis();
        int count = 0;
        StringResponse strResponse = null;

        while (count < 3 && strResponse == null) {
            try (CloseableHttpResponse httpResponse = this.client.execute(httpGetRequest)) {

                strResponse = new StringResponse();
                strResponse.setCode(httpResponse.getStatusLine().getStatusCode());
                strResponse.setMessage(httpResponse.getStatusLine().getReasonPhrase());

                if (httpResponse.getEntity() != null) {
                    final String responseAsString = EntityUtils.toString(httpResponse.getEntity());
                    strResponse.setContent(responseAsString);
                }

            } catch (final IOException e) {
                LOGGER.error("Error while partner call [T: {}ms]", System.currentTimeMillis() - start, e);
                strResponse = null;
            } finally {
                count++;
            }
        }

        if (strResponse == null) {
            throw new ExternalCommunicationException("Partner response empty");
        }

        return strResponse;
    }

}
