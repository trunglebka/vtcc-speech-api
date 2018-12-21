package speech.asr.rest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class HttpMultipartUploader {
    public static String uploadFile(String url, String fieldName, File file, Map<String, String> uriParams, Map<String, String> headers) throws IOException, URISyntaxException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        if (!url.startsWith("http://") && !url.startsWith("https://"))
            throw new IllegalArgumentException("Please specify request scheme in URL. Maybe it is: " + String.format("%s%s or %s%s?", "http://", url, "https://", url));

        String scheme;
        if (url.startsWith("http://"))
            scheme = "http";
        else
            scheme = "https";
        URI uri = new URI(url);
        URIBuilder uriBuilder = new URIBuilder().setScheme(scheme).setHost(uri.getHost()).setPort(uri.getPort()).setPath(uri.getPath());
        if (uriParams != null)
            for (Map.Entry<String, String> entry : uriParams.entrySet()) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
        SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
        sslContextBuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslContextBuilder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        httpclient = HttpClients.createDefault();

        HttpPost post = new HttpPost(uriBuilder.build());
        if (headers != null)
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                post.addHeader(entry.getKey(), entry.getValue());
            }
        FileBody fileBody = new FileBody(file, ContentType.DEFAULT_BINARY);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart(fieldName, fileBody);
        HttpEntity entity = builder.build();
        post.setEntity(entity);
        HttpResponse response = httpclient.execute(post);
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        br.close();
        return sb.toString();
    }

}
