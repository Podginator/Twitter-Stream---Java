package com.podginator.twitter.notifier.config;

import com.podginator.twitter.notifier.consumer.TwitterConsumer;
import com.podginator.twitter.notifier.consumer.TwitterDataPrinter;
import com.podginator.twitter.notifier.consumer.TwitterStatsPrinter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.PrintStream;
import java.util.List;

@Configuration
public class TwitterNotifierConfiguration {

    @Bean
    public PrintStream out() {
        return System.out;
    }

    @Bean
    public List<TwitterConsumer> subscriberList(final PrintStream stream) {
        return List.of(new TwitterDataPrinter(stream), new TwitterStatsPrinter(stream));
    }

}
