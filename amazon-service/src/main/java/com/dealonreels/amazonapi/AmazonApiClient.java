package com.dealonreels.amazonapi;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.StringEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.stream.Collectors;

public class AmazonApiClient {

    private static final Dotenv dotenv = Dotenv.load();

    private static final String MODE = dotenv.get("AMAZON_API_MODE", "mock").toLowerCase();
    private static final String ACCESS_KEY = dotenv.get("AMAZON_API_KEY");
    private static final String SECRET_KEY = dotenv.get("AMAZON_API_SECRET");
    private static final String HOST = dotenv.get("AMAZON_API_HOST");
    private static final String REGION = dotenv.get("AMAZON_API_REGION");
    private static final String SERVICE = "ProductAdvertisingAPI";
    private static final String ENDPOINT = "https://" + HOST + "/paapi5/getitems";

    public JsonNode getItems(String[] itemIds) throws Exception {
        JsonNode response;
        if ("mock".equals(MODE)) {
            response = loadMockResponse();
        } else {
            response = callAmazonApi(itemIds);
        }
        
        // Save products to database
        ProductService productService = new ProductService();
        productService.processAndSaveProducts(response);
        
        return response;
    }
    
    private JsonNode callAmazonApi(String[] itemIds) throws Exception {

        String method = "POST";
        String amzDate = getAmzDate();
        String dateStamp = getDateStamp();

        String payload = RequestBuilder.buildGetItemsPayload(itemIds);
        String payloadHash = sha256Hex(payload);

        String canonicalUri = "/paapi5/getitems";

        Map<String, String> headers = new TreeMap<>();
        headers.put("content-encoding", "amz-1.0");
        headers.put("content-type", "application/json; charset=utf-8");
        headers.put("host", HOST);
        headers.put("x-amz-date", amzDate);

        String canonicalHeaders = buildCanonicalHeaders(headers);
        String signedHeaders = String.join(";", headers.keySet());

        String canonicalRequest = method + "\n" +
                canonicalUri + "\n" +
                "" + "\n" +
                canonicalHeaders + "\n" +
                "\n" +
                signedHeaders + "\n" +
                payloadHash;

        String algorithm = "AWS4-HMAC-SHA256";
        String credentialScope = dateStamp + "/" + REGION + "/" + SERVICE + "/" + "aws4_request";
        String stringToSign = algorithm + "\n" +
                amzDate + "\n" +
                credentialScope + "\n" +
                sha256Hex(canonicalRequest);

        byte[] signingKey = AuthSigner.getSignatureKey(SECRET_KEY, dateStamp, REGION, SERVICE);
        String signature = AuthSigner.bytesToHex(AuthSigner.hmacSHA256(stringToSign, signingKey));

        String authorizationHeader = algorithm + " " +
                "Credential=" + ACCESS_KEY + "/" + credentialScope + ", " +
                "SignedHeaders=" + signedHeaders + ", " +
                "Signature=" + signature;

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(ENDPOINT);
            request.addHeader("Authorization", authorizationHeader);
            request.addHeader("x-amz-date", amzDate);
            request.addHeader("Content-Encoding", "amz-1.0");
            request.addHeader("Content-Type", "application/json; charset=utf-8");
            request.setEntity(new StringEntity(payload, StandardCharsets.UTF_8));

            ClassicHttpResponse response = client.execute(request);
            int statusCode = response.getCode();

            if (statusCode == 200) {
                InputStream content = response.getEntity().getContent();
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readTree(content);
            } else {
                throw new RuntimeException("Amazon API call failed: HTTP " + statusCode);
            }
        }
    }

    private JsonNode loadMockResponse() throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("mock_getitems_response.json");
        if (is == null) {
            throw new IOException("Mock response resource not found");
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String json = br.lines().collect(Collectors.joining("\n"));
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(json);
        }
    }

    private String buildCanonicalHeaders(Map<String, String> headers) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey().toLowerCase()).append(":").append(entry.getValue().trim()).append("\n");
        }
        return sb.toString();
    }

    private String getAmzDate() {
        return ZonedDateTime.now(java.time.ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'", Locale.US));
    }

    private String getDateStamp() {
        return ZonedDateTime.now(java.time.ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyyMMdd", Locale.US));
    }

    private String sha256Hex(String data) throws Exception {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(data.getBytes(StandardCharsets.UTF_8));
        return AuthSigner.bytesToHex(digest);
    }
}
