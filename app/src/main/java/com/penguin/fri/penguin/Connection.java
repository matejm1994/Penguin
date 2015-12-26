package com.penguin.fri.penguin;

import android.net.Uri;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by matej on 26. 12. 2015.
 */

public class Connection {

    /**
     * Method used for PUT connection to HTTP server
     *
     * @param URL
     * @return answer from database
     * @throws IOException
     */
    public static String putConnection(String URL) throws IOException {
        // To encode URL, so it works over POST connection also with spaces and things like this
        final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";
        URL = Uri.encode(URL, ALLOWED_URI_CHARS);
        HttpClient hc = new DefaultHttpClient();
        HttpPut httpPutRequest = new HttpPut(URL);
        HttpResponse httpResponse = hc.execute(httpPutRequest);
        HttpEntity httpEntity = httpResponse.getEntity();
        return EntityUtils.toString(httpEntity);
    }

    /**
     * Method used for POST connection to HTTP server
     *
     * @param URL
     * @return answer from database
     * @throws IOException
     */
    public static String postConnection(String URL) throws IOException {
        // To encode URL, so it works over POST connection also with spaces and things like this
        final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";
        URL = Uri.encode(URL, ALLOWED_URI_CHARS);
        HttpClient hc = new DefaultHttpClient();
        HttpPost httpPostRequest = new HttpPost(URL);
        HttpResponse httpResponse = hc.execute(httpPostRequest);
        HttpEntity httpEntity = httpResponse.getEntity();
        return EntityUtils.toString(httpEntity);
    }

    /**
     * Method used for GET connection to HTTP server
     *
     * @param URL
     * @return resoult from database
     * @throws IOException
     */
    public static String getConnection(String URL) throws IOException {
        HttpClient hc = new DefaultHttpClient();
        HttpGet httpOffers = new HttpGet(URL);
        HttpResponse httpResponse = hc.execute(httpOffers);
        HttpEntity httpEntity = httpResponse.getEntity();
        return EntityUtils.toString(httpEntity);
    }


}
