package com.podginator.twitter.notifier.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.podginator.twitter.notifier.model.TweetData;
import com.podginator.twitter.notifier.model.TwitterAuthenticationException;
import com.podginator.twitter.notifier.model.TwitterDisconnectException;
import com.podginator.twitter.notifier.model.TwitterErrors;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

@Service
public class TwitterReactiveService {

    private final TwitterService twitterService;
    private Flux<TweetData> twitterFlux;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Logger logger = Logger.getLogger(TwitterReactiveService.class.getName());

    private final static Set<String> DISCONNECT_ERRORS = Set.of("operational-disconnect", "ConnectionException");

    public TwitterReactiveService(final TwitterService twitterService) {
        this.twitterService = twitterService;
    }

    private void handleErrors(final TwitterErrors errors) throws TwitterDisconnectException {
        // check if there's a connection error.
        logger.severe("Errors detected: " + errors.getErrors());

        final boolean connectionError = errors.getErrors().stream().anyMatch(
                error -> DISCONNECT_ERRORS.contains(error.getTitle())
        );

        if (connectionError) {
            throw new TwitterDisconnectException("Disconnected via error");
        }

        throw new IllegalArgumentException("Undefined Twitter Error");
    }

    // We need to know if we are getting a Tweet, or if we're getting a Warning.
    private TweetData convertToTweetData(final String json) throws JsonProcessingException, TwitterDisconnectException, ParseException {
        final TypeReference<HashMap<String,Object>> typeRef
                = new TypeReference<>() {};

        final HashMap<String ,Object> tweetData = mapper.readValue(json, typeRef);

        // We are a status, so let's extract.
        if (tweetData.containsKey("created_at")) {
            return TweetData.fromMap(tweetData);
        }

        if (tweetData.containsKey("errors")) {
            handleErrors(mapper.readValue(json, TwitterErrors.class));
        }

        return null;
    }

    /**
     * A Flux sink that consumes from an input stream, and converts to the correct messages
     * @param inputStream The InputStream from Twitter
     * @return A Flux Sink that produces messages
     */
    private Consumer<FluxSink<TweetData>> twitterMessageSink(final InputStream inputStream) {
        return (sink) -> {
            try {
                int currentByte;
                do {
                    StringBuilder byteCountString = new StringBuilder();
                    currentByte = inputStream.read();
                    if (currentByte == -1) {
                        throw new TwitterDisconnectException("Stream has disconnected by sending a negative byte");
                    }

                    while (currentByte != '\r' && currentByte != '\n') {
                        byteCountString.append((char) currentByte);
                        currentByte = inputStream.read();
                    }
                    //\r\n
                    inputStream.skip(1);
                    //Get Buffer size
                    final int bufferSize = Integer.parseInt(byteCountString.toString());

                    byte[] buffer = new byte[bufferSize];
                    inputStream.read(buffer);

                    // We also must handle the reconnect clause. We should possibly throw an exception for this and then handle
                    // it there.
                    TweetData data = convertToTweetData(new String(buffer, "UTF-8"));
                    if (data != null) {
                        sink.next(data);
                    }

                } while (true);

            } catch (Exception e) {
                sink.error(e);
            }
        };
    }

    private Flux<TweetData> connectToTweetStream() throws IOException, URISyntaxException, TwitterAuthenticationException {
        final InputStream jsonStream = twitterService.filteredTwitterStream(List.of("bieber"));
        return Flux.create(twitterMessageSink(jsonStream))
                .onErrorResume(reconnectOnError(5))
                .onErrorStop()
                .subscribeOn(Schedulers.newSingle("Tweet Stream"))
                .share();
    }

    /**
     * Returns a multicast stream of Twitter Messages.
     * @return A Multicast stream that we can subscribe to
     */
    public Flux<TweetData> getLatestTweetStream() throws TwitterAuthenticationException, IOException, URISyntaxException {
        if (twitterFlux == null) {
            twitterFlux = connectToTweetStream();
        }

        return twitterFlux;
    }

    private Function<Throwable, Flux<TweetData>> reconnectOnError(final Integer maxTimes) {
        AtomicInteger timesThrown = new AtomicInteger();
        return (Throwable err) -> {
            if (err instanceof TwitterDisconnectException & timesThrown.getAndIncrement() <= maxTimes) {
                try {
                    return connectToTweetStream();
                } catch (Exception e) {
                    return Flux.error(e);
                }
            }

            logger.severe("Error Thrown " + err.toString());
            return Flux.error(err);
        };
    }
}
