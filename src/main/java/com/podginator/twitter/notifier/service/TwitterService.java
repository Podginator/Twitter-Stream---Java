package com.podginator.twitter.notifier.service;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.podginator.twitter.notifier.model.TwitterAuthenticationException;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TwitterService {

    private final TwitterOAuthService client;
    private final static String BASE_STREAM_URL = "https://stream.twitter.com/1.1/statuses/filter.json?delimited=length";

    public TwitterService(final TwitterOAuthService oauth) {
        client = oauth;
    }

    /**
     * Return the open http stream connection
     * @param tracking The list of items we wish to track against.
     * @return InputStream of incoming tweets
     */
    public InputStream filteredTwitterStream(final List<String> tracking) throws IOException, TwitterAuthenticationException, URISyntaxException {
        final List<NameValuePair> nameValuePairs = tracking.stream()
                .map(name -> new BasicNameValuePair("track", name))
                .collect(Collectors.toList());
        final HttpRequestFactory httpClient = client.getAuthorizedHttpRequestFactory();
        final URIBuilder uriBuilder = new URIBuilder(BASE_STREAM_URL);
        uriBuilder.addParameters(nameValuePairs);
        final HttpResponse response = httpClient.buildGetRequest(new GenericUrl(uriBuilder.build())).execute();
        return response.getContent();
    }
}
