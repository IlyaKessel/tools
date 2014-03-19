package com.wizzardo.tools.http;

import com.wizzardo.tools.security.Base64;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.util.*;

/**
 * @author: wizzardo
 * Date: 3/1/14
 */
public class RequestArguments<T extends RequestArguments> {

    protected int maxRetryCount = 0;
    protected long pauseBetweenRetries = 0;
    protected ConnectionMethod method = ConnectionMethod.GET;
    protected Map<String, List<String>> params = new LinkedHashMap<String, List<String>>();
    protected Map<String, String> headers = new HashMap<String, String>();
    protected Map<String, byte[]> dataArrays = new HashMap<String, byte[]>();
    protected Map<String, String> dataTypes = new HashMap<String, String>();
    protected boolean multipart = false;
    protected String charsetForEncoding = "utf-8";
    protected Proxy proxy;
    protected boolean redirects = true;
    protected byte[] data;
    protected HostnameVerifier hostnameVerifier;
    protected SSLSocketFactory sslFactory;

    public Request createRequest(String url) {
        Request request = new Request(url)
                .setHeaders(headers)
                .addParameters(params)
                .setPauseBetweenRetries(pauseBetweenRetries)
                .setMaxRetryCount(maxRetryCount)
                .setProxy(proxy)
                .setSSLSocketFactory(sslFactory)
                .setHostnameVerifier(hostnameVerifier)
                .method(method)
                .setUrlEncoding(charsetForEncoding);

        request.data = data;
        request.dataTypes = dataTypes;
        request.dataArrays = dataArrays;
        request.redirects = redirects;
        request.multipart = multipart;

        return request;
    }

    protected T self() {
        return (T) this;
    }

    public T setMaxRetryCount(int n) {
        maxRetryCount = n;
        return self();
    }

    public T setBasicAuthentication(String user, String password) {
        header("Authorization", "Basic " + Base64.encodeToString((user + ":" + password).getBytes()));
        return self();
    }

    public T setProxy(Proxy proxy) {
        this.proxy = proxy;
        return self();
    }

    public T maxRetryCount(int n) {
        maxRetryCount = n;
        return self();
    }

    public T setPauseBetweenRetries(long pause) {
        pauseBetweenRetries = pause;
        return self();
    }

    public T pauseBetweenRetries(long pause) {
        pauseBetweenRetries = pause;
        return self();
    }

    public T setMethod(ConnectionMethod method) {
        this.method = method;
        return self();
    }

    public T method(ConnectionMethod method) {
        this.method = method;
        return self();
    }

    public T setCookies(String cookie) {
        headers.put("Cookie", cookie);
        return self();
    }

    public T cookies(String cookie) {
        headers.put("Cookie", cookie);
        return self();
    }

    public T cookies(List<Cookie> cookies) {
        StringBuilder sb = new StringBuilder();
        for (Cookie c : cookies) {
            if (sb.length() > 0)
                sb.append("; ");
            sb.append(c.key).append("=").append(c.value);
        }
        headers.put("Cookie", sb.toString());
        return self();
    }

    public T setCookies(List<Cookie> cookies) {
        return cookies(cookies);
    }

    public T setReferer(String referer) {
        headers.put("Referer", referer);
        return self();
    }

    public T referer(String referer) {
        headers.put("Referer", referer);
        return self();
    }

    public T setJson(String json) {
        return json(json);
    }

    public T json(String json) {
        try {
            data = json.getBytes("utf-8");
        } catch (UnsupportedEncodingException ignored) {
        }
        setContentType(ContentType.JSON);
        return self();
    }

    public T setXml(String xml) {
        return xml(xml);
    }

    public T xml(String xml) {
        try {
            data = xml.getBytes("utf-8");
        } catch (UnsupportedEncodingException ignored) {
        }
        setContentType(ContentType.XML);
        return self();
    }

    public T setData(byte[] data, String contentType) {
        return data(data, contentType);
    }

    public T data(byte[] data, String contentType) {
        this.data = data;
        setContentType(contentType);
        return self();
    }

    public T addParameter(String key, String value) {
        List<String> l = params.get(key);
        if (l == null) {
            l = new ArrayList<String>();
            params.put(key, l);
        }
        l.add(value);
        return self();
    }

    public T addParameters(String key, List<String> values) {
        List<String> l = params.get(key);
        if (l == null) {
            l = new ArrayList<String>();
            params.put(key, l);
        }
        l.addAll(values);
        return self();
    }

    public T addParameters(Map<String, List<String>> params) {
        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            List<String> l = this.params.get(entry.getKey());
            if (l == null) {
                l = new ArrayList<String>();
                this.params.put(entry.getKey(), l);
            }
            l.addAll(entry.getValue());
        }
        return self();
    }

    public T setUrlEncoding(String charset) {
        charsetForEncoding = charset;
        return self();
    }

    public T disableRedirects() {
        redirects = false;
        return self();
    }

    public T addFile(String key, File value) {
        return addFile(key, value.getAbsolutePath());
    }

    public T addFile(String key, String path) {
        multipart = true;
        method = ConnectionMethod.POST;
        addParameter(key, "file://" + path);
        return self();
    }

    public T addByteArray(String key, byte[] array, String name) {
        return addByteArray(key, array, name, null);
    }

    public T addByteArray(String key, byte[] array, String name, String type) {
        multipart = true;
        method = ConnectionMethod.POST;
        addParameter(key, "array://" + name);
        dataArrays.put(key, array);
        if (type != null) {
            dataTypes.put(key, type);
        }
        return self();
    }

    public T data(String key, String value) {
        addParameter(key, value);
        return self();
    }

    public T data(String key, List<String> values) {
        addParameters(key, values);
        return self();
    }

    public T setHeader(String key, String value) {
        headers.put(key, value);
        return self();
    }

    public T setHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
        return self();
    }

    public T header(String key, String value) {
        headers.put(key, value);
        return self();
    }

    public T setHostnameVerifier(HostnameVerifier hv) {
        this.hostnameVerifier = hv;
        return self();
    }

    public T setSSLSocketFactory(SSLSocketFactory sslFactory) {
        this.sslFactory = sslFactory;
        return self();
    }


    public T setContentType(String contentType) {
        setHeader("Content-Type", contentType);
        return self();
    }

    public T setContentType(ContentType contentType) {
        setHeader("Content-Type", contentType.text);
        return self();
    }
}