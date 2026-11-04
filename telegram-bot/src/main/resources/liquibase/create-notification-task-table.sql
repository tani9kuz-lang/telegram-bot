-- liquibase formatted sql

-- changeset tkuznetsova:1
CREATE TABLE notification_task(
    id             BIGSERIAL PRIMARY KEY,
    chat_id        BIGINT    NOT NULL,
    text           TEXT      NOT NULL,
    send_date_time TIMESTAMP NOT NULL
);
