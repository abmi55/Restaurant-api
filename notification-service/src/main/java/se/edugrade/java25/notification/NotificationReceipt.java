package se.edugrade.java25.notification;

import java.time.Instant;

/**
 * What this service answers.
 *
 * @param messageId  this service's id for the queued message
 * @param status     {@code "ACCEPTED"} when this service takes responsibility
 *                   for the message; the field the API depends on
 * @param acceptedAt when this service accepted the message
 */
public record NotificationReceipt(String messageId, String status, Instant acceptedAt) {
}
