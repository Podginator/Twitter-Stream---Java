package com.podginator.twitter.notifier.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PACKAGE)
@AllArgsConstructor
public class TwitterUser {
    /**
     * The user ID
     * The creation date of the user as epoch value
     * The name of the user
     * The screen name of the user
     */
    String userId;
    Long epoch;
    String name;
    String screenName;
}
