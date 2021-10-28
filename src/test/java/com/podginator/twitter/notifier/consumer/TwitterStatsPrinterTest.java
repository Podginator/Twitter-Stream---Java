package com.podginator.twitter.notifier.consumer;

import com.podginator.twitter.notifier.model.TweetData;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;

import java.io.PrintStream;


@SpringBootTest(classes =  { TwitterStatsPrinter.class })
public class TwitterStatsPrinterTest {

    private Flux<TweetData> tweetDataFlux;

    @MockBean
    private PrintStream out;

    @Autowired
    private TwitterStatsPrinter statsPrinter;

    public void populateFlux(int times) {
        tweetDataFlux = Flux.<TweetData>create((fluxSink) -> {
            for (int i = 0; i  < times ; i++) {
                fluxSink.next(new TweetData());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Test
    public void shouldProcessMessagesAndPrintMessagesPerSecond() throws InterruptedException {
        populateFlux(30);
        statsPrinter.consumeDataStream(tweetDataFlux);

        Thread.sleep(62000);
        Mockito.verify(out, Mockito.times(1)).println( "There have been 30 tweets in the last minute");
    }

    @Test
    public void shouldProcessMessagesAndPrintMessagesPerSecondWithDifferingCadence() throws InterruptedException {
        populateFlux(55);
        statsPrinter.consumeDataStream(tweetDataFlux);

        Thread.sleep(120000);
        Mockito.verify(out, Mockito.times(2))
                .println(Mockito.<String>any());
    }

}
