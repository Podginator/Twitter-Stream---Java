package com.podginator.twitter.notifier.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PUBLIC)
@AllArgsConstructor
public class TweetData {
    String messageId;
    Long creationDate;
    String text;
    TwitterUser user;

    public static TweetData fromMap(final Map<String, Object> tweetDataMap) throws ParseException {
        final Map<String, Object> userMap = ((Map<String, Object>) tweetDataMap.getOrDefault("user", new HashMap<>()));
        final Long epochDate =
                new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy").parse(userMap.get("created_at").toString()).getTime();

        final TwitterUser user = new TwitterUser(
            userMap.get("id_str").toString(),
            epochDate,
            userMap.get("name").toString(),
            userMap.get("screen_name").toString()
        );

        return new TweetData(
            tweetDataMap.get("id_str").toString(),
            Long.parseLong(tweetDataMap.get("timestamp_ms").toString()),
            tweetDataMap.get("text").toString(),
            user
        );
    }
}
