package com.payline.payment.samsung.pay.utils.http;

import com.payline.payment.samsung.pay.exception.ExternalCommunicationException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

public class SamsungPayHttpClient extends AbstractHttpClient {
    private static final SamsungPayHttpClient instance = new SamsungPayHttpClient();

    private SamsungPayHttpClient() {
        super(10, 10, 15);
    }

    public static SamsungPayHttpClient getInstance() {
        return instance;
    }

    /**
     * Send a POST request, with a JSON content type.
     *
     * @param host        URL host
     * @param path        URL path
     * @param jsonContent The JSON content, as a string
     * @return The response returned from the HTTP call
     * @throws IOException
     */
    public StringResponse doPost(String host, String path, String jsonContent, String xRequestId) throws IOException, URISyntaxException, ExternalCommunicationException {
        StringEntity entity = new StringEntity(jsonContent);

        return super.doPost(host, path, entity, ContentType.APPLICATION_JSON.toString(), xRequestId);
    }

    /**
     * Send a GET request.
     *
     * @param host   URL host
     * @param path   URL path
     * @return The response returned from the HTTP call
     * @throws IOException
     */
    public StringResponse doGet(String host, String path, Map<String, String> queryAttributes, String xRequestId) throws URISyntaxException, ExternalCommunicationException {
        return super.doGet(host, path, queryAttributes, ContentType.APPLICATION_JSON.toString(), xRequestId);
    }

}
