package com.fypgrading.notificationservice.live;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import com.fypgrading.notificationservice.common.web.ApiException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/** Review M10: open streams are bounded per user and in total. */
class LiveHubTest {

    @Test
    void aFourthStreamForTheSameUserClosesTheOldest() {
        LiveHub hub = new LiveHub(3, 500);
        UUID session = UUID.randomUUID();
        UUID jane = UUID.randomUUID();
        for (int i = 0; i < 4; i++) {
            hub.open(session, jane, false);
        }
        assertThat(hub.streamsOf(session, jane)).isEqualTo(3);
        hub.open(session, UUID.randomUUID(), false);
        assertThat(hub.openStreams()).isEqualTo(4);
    }

    @Test
    void theServiceRefusesStreamsAboveItsTotal() {
        LiveHub hub = new LiveHub(3, 2);
        UUID session = UUID.randomUUID();
        hub.open(session, UUID.randomUUID(), false);
        hub.open(session, UUID.randomUUID(), false);
        ApiException e = catchThrowableOfType(ApiException.class, () -> hub.open(session, UUID.randomUUID(), false));
        assertThat(e.status()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(e.code()).isEqualTo("too-many-streams");
    }

    @Test
    void closingASessionForgetsItsStreams() {
        LiveHub hub = new LiveHub(3, 500);
        UUID session = UUID.randomUUID();
        hub.open(session, UUID.randomUUID(), true);
        hub.closeSession(session);
        assertThat(hub.openStreams()).isZero();
    }
}
