package com.fypgrading.notificationservice.live;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Open Server-Sent Event streams, per demo session and user. New notifications go to their recipient; activity
 * goes to the coordinators of that demo copy. Single instance by design (the demo runs one replica).
 */
@Component
public class LiveHub {

    private static final Logger log = LoggerFactory.getLogger(LiveHub.class);
    static final Duration STREAM_LIFETIME = Duration.ofMinutes(30);

    private final Map<UUID, List<Subscriber>> bySession = new ConcurrentHashMap<>();

    public SseEmitter open(UUID session, UUID user, boolean admin) {
        SseEmitter emitter = new SseEmitter(STREAM_LIFETIME.toMillis());
        Subscriber subscriber = new Subscriber(user, admin, emitter);
        bySession.computeIfAbsent(session, s -> new CopyOnWriteArrayList<>()).add(subscriber);
        Runnable remove = () -> remove(session, subscriber);
        emitter.onCompletion(remove);
        emitter.onTimeout(remove);
        emitter.onError(e -> remove.run());
        send(session, subscriber, SseEmitter.event().comment("connected"));
        return emitter;
    }

    public void toUser(UUID session, UUID user, String event, String id, Object data) {
        subscribers(session).stream()
                .filter(s -> s.user().equals(user))
                .forEach(s -> send(session, s, SseEmitter.event().name(event).id(id).data(data, MediaType.APPLICATION_JSON)));
    }

    public void toAdmins(UUID session, String event, String id, Object data) {
        subscribers(session).stream()
                .filter(Subscriber::admin)
                .forEach(s -> send(session, s, SseEmitter.event().name(event).id(id).data(data, MediaType.APPLICATION_JSON)));
    }

    /** Ends every stream of a demo copy (reset or purge); the client reconnects and refetches. */
    public void closeSession(UUID session) {
        List<Subscriber> subscribers = bySession.remove(session);
        if (subscribers != null) {
            subscribers.forEach(s -> s.emitter().complete());
        }
    }

    public void closeAll() {
        List.copyOf(bySession.keySet()).forEach(this::closeSession);
    }

    @Scheduled(fixedRate = 25_000)
    void keepAlive() {
        bySession.forEach((session, subscribers) ->
                subscribers.forEach(s -> send(session, s, SseEmitter.event().comment("keepalive"))));
    }

    int openStreams() {
        return bySession.values().stream().mapToInt(List::size).sum();
    }

    private List<Subscriber> subscribers(UUID session) {
        return Objects.requireNonNullElse(bySession.get(session), List.of());
    }

    private void send(UUID session, Subscriber subscriber, SseEmitter.SseEventBuilder event) {
        try {
            subscriber.emitter().send(event);
        } catch (IOException | IllegalStateException e) {
            log.debug("Dropping a closed stream: {}", e.getMessage());
            remove(session, subscriber);
        }
    }

    private void remove(UUID session, Subscriber subscriber) {
        List<Subscriber> subscribers = bySession.get(session);
        if (subscribers != null) {
            subscribers.remove(subscriber);
            if (subscribers.isEmpty()) {
                bySession.remove(session, subscribers);
            }
        }
    }

    private record Subscriber(UUID user, boolean admin, SseEmitter emitter) {
    }
}
