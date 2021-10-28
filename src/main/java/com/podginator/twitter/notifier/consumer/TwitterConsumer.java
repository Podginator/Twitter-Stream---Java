package com.podginator.twitter.notifier.consumer;

import com.podginator.twitter.notifier.model.TweetData;
import reactor.core.publisher.Flux;

@FunctionalInterface
public interface TwitterConsumer {

    void consumeDataStream(final Flux<TweetData> tweetDataFlux);

}
