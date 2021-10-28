package com.podginator.twitter.notifier.consumer;

import com.podginator.twitter.notifier.model.TweetData;
import com.podginator.twitter.notifier.model.TwitterUser;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.PrintStream;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class TwitterDataPrinter implements TwitterConsumer {

    private final PrintStream out;

    public TwitterDataPrinter(final PrintStream out) {
        this.out = out;
    }

    /**
     * groups messages by user, sorted chronologically by user, and by message.
     *
     * @param tweetData: List of Tweets
     * @return sorted map of User, to their Tweets
     */
    private Map<TwitterUser, List<TweetData>> groupByUsers(final List<TweetData> tweetData) {
        return tweetData.stream()
                .sorted(
                        Comparator.comparing(TweetData::getCreationDate)
                            .thenComparing(data -> data.getUser().getEpoch())
                )
                .collect(Collectors.groupingBy(TweetData::getUser,
                        LinkedHashMap::new,
                        Collectors.toList()));
    }

    /**
     * Print Tweets to the output
     * @param tweetData a map sorted by TwitterUser, chronologically ascending, and messages from that user.
     */
    private void printTweets(final Map<TwitterUser, List<TweetData>> tweetData) {
        // Your application should return the messages grouped by user (users sorted chronologically, ascending)
        // The messages per user should also be sorted chronologically, ascending
        out.println(tweetData);
    }

    @Override
    public void consumeDataStream(final Flux<TweetData> tweetDataFlux) {
        // Have a consumer that will subscribe to the data, and then process it
        tweetDataFlux
            .bufferTimeout(100, Duration.ofSeconds(30))
            .map(this::groupByUsers)
            .subscribe(this::printTweets);
    }
}
