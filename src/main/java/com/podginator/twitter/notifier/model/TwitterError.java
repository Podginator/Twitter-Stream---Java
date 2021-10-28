package com.podginator.twitter.notifier.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PUBLIC)
@AllArgsConstructor
public class TwitterError {
    String title;
    String disconnect_type;
    String detail;
    String type;
}
