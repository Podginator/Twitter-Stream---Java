package com.podginator.twitter.notifier.consumer;

import com.podginator.twitter.notifier.model.TwitterAuthenticationException;
import com.podginator.twitter.notifier.service.TwitterReactiveService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.net.URISyntaxException;


@SpringBootTest()
public class TwitterSubscriberTest {

    @MockBean
    private TwitterStatsPrinter statsPrinter;

    @MockBean
    private TwitterDataPrinter dataPrinter;

    @MockBean
    private TwitterReactiveService reactiveService;

    @Autowired
    private TweetSubscriber tweetSubscriber;

    @Test
    public void shouldAddAllSubscribers() throws TwitterAuthenticationException, IOException, URISyntaxException {
        Mockito.verify(reactiveService, Mockito.times(1)).getLatestTweetStream();
        Mockito.verify(statsPrinter, Mockito.times(1)).consumeDataStream(Mockito.any());
        Mockito.verify(dataPrinter, Mockito.times(1)).consumeDataStream(Mockito.any());
    }
}
