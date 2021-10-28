package com.podginator.twitter.notifier.consumer;

import com.podginator.twitter.notifier.model.TweetData;
import com.podginator.twitter.notifier.model.TwitterUser;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SpringBootTest(classes =  { TwitterDataPrinter.class })
public class TwitterDataPrinterTest {

    @MockBean
    private PrintStream out;

    @Autowired
    private TwitterDataPrinter dataPrinter;

    @Captor
    private ArgumentCaptor<Map<TwitterUser, List<TweetData>>> twitterCapture;

    @Test
    void shouldPrintAfterThirtySeconds() throws InterruptedException {
        final Flux<TweetData> tweetDataFlux = Flux.create((fluxSink) -> {
            fluxSink.next(getGenericTweetData());
        });

        dataPrinter.consumeDataStream(tweetDataFlux);
        Thread.sleep(31 * 1000);


        Mockito.verify(out, Mockito.times(1)).println(twitterCapture.capture());

    }

    @Test
    void shouldPrintAfter100Messages() throws InterruptedException {
        final Flux<TweetData> tweetDataFlux = Flux.create((fluxSink) -> {
            for (int i = 0; i < 100; i++) {
                fluxSink.next(getGenericTweetData());
            }
        });

        dataPrinter.consumeDataStream(tweetDataFlux);
        Thread.sleep(1000);


        Mockito.verify(out, Mockito.times(1)).println(twitterCapture.capture());
        final Map<TwitterUser, List<TweetData>> data = twitterCapture.getValue();
        Assert.isTrue(data.keySet().size() == 100);
    }

    @Test
    void shouldPrintInUsersCreationDateAscending() throws InterruptedException {
        final Flux<TweetData> tweetDataFlux = Flux.create((fluxSink) -> {
            for (int i = 0; i < 100; i++) {
                fluxSink.next(getTweetData(getTwitterUser(UUID.randomUUID().toString(), (long) 100 - i)));
            }
        });

        dataPrinter.consumeDataStream(tweetDataFlux);
        Thread.sleep(1000);


        Mockito.verify(out, Mockito.times(1)).println(twitterCapture.capture());
        final Map<TwitterUser, List<TweetData>> data = twitterCapture.getValue();
        Assert.isTrue(data.keySet().size() == 100);
    }

    @Test
    void shouldCombineMessagesToASingleUser() throws InterruptedException {
        final Flux<TweetData> tweetDataFlux = Flux.create((fluxSink) -> {
            for (int i = 0; i < 100; i++) {
                fluxSink.next(getTweetData(getTwitterUser("Combined", 100L)));
            }
        });

        dataPrinter.consumeDataStream(tweetDataFlux);
        Thread.sleep(1000);


        Mockito.verify(out, Mockito.times(1)).println(twitterCapture.capture());
        final Map<TwitterUser, List<TweetData>> data = twitterCapture.getValue();
        Assert.isTrue(data.keySet().size() == 1);
    }

    @Test
    void shouldCombineMessagesToASingleUserSortedByMessageDateAscending() throws InterruptedException {
        final Flux<TweetData> tweetDataFlux = Flux.create((fluxSink) -> {
            final TwitterUser twitterUser = getTwitterUser("combined", 100L);
            for (int i = 100; i > 0; i--) {
                fluxSink.next(getTweetData(twitterUser, (long) i));
            }
        });

        dataPrinter.consumeDataStream(tweetDataFlux);
        Thread.sleep(1000);


        Mockito.verify(out, Mockito.times(1)).println(twitterCapture.capture());
        final Map<TwitterUser, List<TweetData>> data = twitterCapture.getValue();
        Assert.isTrue(data.keySet().size() == 1);

        // the list has been reversed
        final List<TweetData> tweetData = data.entrySet().stream().map(Map.Entry::getValue).findFirst().get();
        for (int i = 0; i < 100; i++) {
            Assert.isTrue(tweetData.get(i).getCreationDate().equals((long) i + 1));
        }

    }

    private TwitterUser getTwitterUser(final String userId, final Long epoch) {
        return new TwitterUser(
                userId,
                epoch,
                "Tom",
                "Thomas"
        );
    }

    private TweetData getGenericTweetData() {
        final TwitterUser twitterUser = getTwitterUser(UUID.randomUUID().toString(), 100L);
        return getTweetData(twitterUser);
    }


    private TweetData getTweetData(final TwitterUser twitterUser) {
        return getTweetData(twitterUser, 100L);
    }

    private TweetData getTweetData(final TwitterUser twitterUser, final Long epochTime) {
        return new TweetData(UUID.randomUUID().toString(), epochTime, "Ha", twitterUser);
    }

}
