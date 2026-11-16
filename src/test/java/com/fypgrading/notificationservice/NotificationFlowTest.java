package com.fypgrading.notificationservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fypgrading.notificationservice.common.demo.DemoSession;
import com.fypgrading.notificationservice.demo.DemoCopyService;
import com.fypgrading.notificationservice.notification.NotificationRepository;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class NotificationFlowTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:4.0-management");

    static final UUID JANE = UUID.fromString("00000000-0000-4000-a000-000000000002");
    static final UUID JOHN = UUID.fromString("00000000-0000-4000-a000-000000000003");
    static final UUID RANIA = UUID.fromString("00000000-0000-4000-a000-000000000001");

    @Autowired
    MockMvc mvc;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    NotificationRepository notifications;

    @Autowired
    DemoCopyService copies;

    UUID session = UUID.randomUUID();

    static JwtRequestPostProcessor user(UUID id, String role) {
        return jwt().jwt(j -> j.subject(id.toString())).authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    private void publish(String exchange, String json) {
        MessageProperties props = new MessageProperties();
        props.setContentType(MediaType.APPLICATION_JSON_VALUE);
        rabbitTemplate.send(exchange, "#", new Message(json.getBytes(), props));
    }

    private long count(UUID recipient) {
        return DemoSession.callAs(session, () -> notifications.countByRecipientIdAndReadFalse(recipient));
    }

    @Test
    void anEventBecomesOnePersonalNotificationPerRecipientEvenIfRedelivered() throws Exception {
        String event = """
                {"eventKey": "grade-final:beta:FINAL_REPORT", "demoSession": "%s", "type": "GRADE_FINAL",
                 "text": "Final report for Team Beta is final: 76.3.", "at": "2026-06-10T10:15:00Z",
                 "link": {"kind": "TEAM", "teamId": "%s", "assessment": "FINAL_REPORT"},
                 "recipients": ["%s", "%s"]}""".formatted(session, UUID.randomUUID(), JANE, RANIA);
        publish("assay.notifications", event);
        publish("assay.notifications", event);

        await().atMost(Duration.ofSeconds(15)).until(() -> count(JANE) == 1 && count(RANIA) == 1);
        Thread.sleep(500);
        assertThat(count(JANE)).isEqualTo(1);

        String body = mvc.perform(get("/api/notifications")
                        .header("X-Demo-Session", session)
                        .with(user(JANE, "REVIEWER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(1))
                .andExpect(jsonPath("$.content[0].type").value("GRADE_FINAL"))
                .andExpect(jsonPath("$.content[0].link.assessment").value("FINAL_REPORT"))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String id = com.jayway.jsonpath.JsonPath.read(body, "$.content[0].id");

        // Read state is per person: John sees nothing, and Jane reading hers leaves Rania's unread.
        mvc.perform(get("/api/notifications").header("X-Demo-Session", session).with(user(JOHN, "REVIEWER")))
                .andExpect(jsonPath("$.content.length()").value(0));
        mvc.perform(patch("/api/notifications/" + id)
                        .header("X-Demo-Session", session)
                        .with(user(RANIA, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"read\": true}"))
                .andExpect(status().isNotFound());
        mvc.perform(patch("/api/notifications/" + id)
                        .header("X-Demo-Session", session)
                        .with(user(JANE, "REVIEWER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"read\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
        assertThat(count(JANE)).isZero();
        assertThat(count(RANIA)).isEqualTo(1);

        mvc.perform(post("/api/notifications/read-all")
                        .header("X-Demo-Session", session)
                        .with(user(RANIA, "ADMIN")))
                .andExpect(status().isNoContent());
        assertThat(count(RANIA)).isZero();
    }

    @Test
    void activityEventsFeedTheCoordinatorsFeedOnly() throws Exception {
        publish("assay.activity", """
                {"eventId": "%s", "demoSession": "%s", "kind": "SUBMITTED", "actorId": "%s", "actorName": "Jane Smith",
                 "text": "submitted the Final report for Team Beta (3 of 3).", "at": "2026-06-10T10:20:00Z",
                 "link": {"kind": "TEAM", "teamId": null, "assessment": "FINAL_REPORT"}}""".formatted(UUID.randomUUID(), session, JANE));
        await().atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> mvc.perform(get("/api/activity")
                                .header("X-Demo-Session", session)
                                .with(user(RANIA, "ADMIN")))
                        .andExpect(jsonPath("$.content[0].actor.displayName").value("Jane Smith"))
                        .andExpect(jsonPath("$.content[0].kind").value("SUBMITTED")));
        mvc.perform(get("/api/activity").header("X-Demo-Session", session).with(user(JANE, "REVIEWER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void theSeedGivesEachPersonaTheirOwnInboxInTheirOwnCopy() throws Exception {
        copies.replace(session);
        mvc.perform(get("/api/notifications").header("X-Demo-Session", session).with(user(JANE, "REVIEWER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").isNumber())
                .andExpect(jsonPath("$.content.length()").value(org.hamcrest.Matchers.greaterThan(3)));
        mvc.perform(get("/api/activity?size=50")
                        .header("X-Demo-Session", session)
                        .with(user(RANIA, "ADMIN")))
                .andExpect(jsonPath("$.content.length()").value(org.hamcrest.Matchers.greaterThan(10)));
        mvc.perform(get("/api/notifications")
                        .header("X-Demo-Session", UUID.randomUUID())
                        .with(user(JANE, "REVIEWER")))
                .andExpect(jsonPath("$.content.length()").value(0));
    }
}
