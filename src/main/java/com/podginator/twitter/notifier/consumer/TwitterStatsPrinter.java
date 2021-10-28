package com.podginator.twitter.notifier.consumer;

import com.podginator.twitter.notifier.model.TweetData;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.PrintStream;
import java.time.Duration;

@Component
public class TwitterStatsPrinter implements TwitterConsumer {

    private final PrintStream out;

    public TwitterStatsPrinter(final PrintStream out) {
        this.out = out;
    }

    private void printTweetsMinute(final int tweetPerMinute) {
        // Your application should return the messages grouped by user (users sorted chronologically, ascending)
        // The messages per user should also be sorted chronologically, ascending
        out.println("There have been " + tweetPerMinute + " tweets in the last minute");
    }

    @Override
    public void consumeDataStream(final Flux<TweetData> tweetDataFlux) {
        // Have a consumer that will subscribe to the data, and then process it
        tweetDataFlux
            .scan(0, (accumulator, current) -> accumulator + 1)
            .takeUntilOther(Mono.delay(Duration.ofSeconds(60)))
            .last()
            .repeat()
            .subscribe(this::printTweetsMinute);
        ;
    }

}
