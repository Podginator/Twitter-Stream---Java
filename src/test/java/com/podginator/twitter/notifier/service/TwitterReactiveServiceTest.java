package com.podginator.twitter.notifier.service;

import com.podginator.twitter.notifier.consumer.TweetSubscriber;
import com.podginator.twitter.notifier.model.TweetData;
import com.podginator.twitter.notifier.model.TwitterAuthenticationException;
import com.podginator.twitter.notifier.model.TwitterUser;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.test.StepVerifier;
import java.io.*;
import java.net.URISyntaxException;

@SpringBootTest
public class TwitterReactiveServiceTest {

    @MockBean
    private TweetSubscriber tweetSubscriber;

    private final TwitterService twitterService = Mockito.mock(TwitterService.class);

    public TwitterReactiveService mockWithFile(final String file) throws TwitterAuthenticationException, IOException, URISyntaxException {
        final File jsonFile = new File("src/test/resources/" + file);
        Mockito.when(twitterService.filteredTwitterStream(Mockito.<String>anyList())).thenReturn(new FileInputStream(jsonFile));

        return new TwitterReactiveService(twitterService);
    }

    @Test
    public void shouldBeginConvertingStreamToTweetData() throws TwitterAuthenticationException, IOException, URISyntaxException {
        final TwitterUser twitterUser = new TwitterUser("1227250641912750080", 0L, "andrea ఌ", "andrealavallet");
        final TweetData expectedData = new TweetData("1450492481515229184", 1634659331097L, "RT @salmaovallem: buen momento para ver el capítulo de la rosa de guadalupe de mi virginidad x un boleto de justin bieber", twitterUser);

        // So we want to return an input stream that streams some data.
        final TwitterReactiveService twitterReactiveService = mockWithFile("filteredStream.txt");
        StepVerifier.create(twitterReactiveService.getLatestTweetStream())
                .expectNext(expectedData)
                .thenCancel()
                .verify();
    }

    @Test
    public void shouldReconnectIfDisconnectOccurs() throws TwitterAuthenticationException, IOException, URISyntaxException, InterruptedException {
        final TwitterReactiveService twitterReactiveService = mockWithFile("filteredStream.txt");
        twitterReactiveService.getLatestTweetStream().subscribe();

        Thread.sleep(10000);

        Mockito.verify(twitterService, Mockito.atLeast(2)).filteredTwitterStream(Mockito.anyList());
    }

    @Test
    public void shouldReconnectIfErrorOccurs() throws TwitterAuthenticationException, IOException, URISyntaxException, InterruptedException {
        final TwitterReactiveService twitterReactiveService = mockWithFile("errorStream.txt");
        twitterReactiveService.getLatestTweetStream().subscribe();

        Thread.sleep(10000);

        Mockito.verify(twitterService, Mockito.atLeast(2)).filteredTwitterStream(Mockito.anyList());
    }

}
