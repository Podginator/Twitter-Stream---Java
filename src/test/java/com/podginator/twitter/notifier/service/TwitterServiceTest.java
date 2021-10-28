package com.podginator.twitter.notifier.service;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.podginator.twitter.notifier.model.TwitterAuthenticationException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;


@SpringBootTest(classes = { TwitterService.class, TwitterOAuthService.class })
public class TwitterServiceTest {

    @MockBean
    private TwitterOAuthService oAuthService;

    @Autowired
    private TwitterService twitterService;

    @Captor
    private ArgumentCaptor<GenericUrl> genericUrlCaptor;

    @Test
    public void shouldCallToOAuthBeforeCallingTwitter() throws TwitterAuthenticationException, IOException, URISyntaxException {
        HttpRequestFactory factory = new MockHttpTransport().createRequestFactory();
        Mockito.when(oAuthService.getAuthorizedHttpRequestFactory())
                .thenReturn(factory);
        twitterService.filteredTwitterStream(List.of());

        Mockito.verify(oAuthService, Mockito.times(1)).getAuthorizedHttpRequestFactory();
    }

    @Test
    public void shouldAddTrackingParameters() throws TwitterAuthenticationException, IOException, URISyntaxException {
        MockHttpTransport transport = new MockHttpTransport();
        HttpRequestFactory factory = transport.createRequestFactory();

        Mockito.when(oAuthService.getAuthorizedHttpRequestFactory())
                .thenReturn(factory);
        twitterService.filteredTwitterStream(List.of("Tom"));

        Assert.isTrue(transport.getLowLevelHttpRequest().getUrl().contains("&track=Tom"));
    }

}
