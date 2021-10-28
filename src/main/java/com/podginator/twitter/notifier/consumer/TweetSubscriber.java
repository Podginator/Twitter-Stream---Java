package com.podginator.twitter.notifier.consumer;

import com.podginator.twitter.notifier.model.TweetData;
import com.podginator.twitter.notifier.model.TwitterAuthenticationException;
import com.podginator.twitter.notifier.service.TwitterReactiveService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@Component
public class TweetSubscriber {

    private final TwitterReactiveService twitterReactiveService;

    public TweetSubscriber(final List<TwitterConsumer> consumers, final TwitterReactiveService reactiveService) throws TwitterAuthenticationException, IOException, URISyntaxException {
        twitterReactiveService = reactiveService;
        subscribeConsumers(consumers);
    }

    private void subscribeConsumers(final List<TwitterConsumer> consumers) throws TwitterAuthenticationException, IOException, URISyntaxException {
        final Flux<TweetData> tweetDataFlux = twitterReactiveService.getLatestTweetStream();
        consumers.forEach(twitterConsumer -> twitterConsumer.consumeDataStream(tweetDataFlux));
    }

}
