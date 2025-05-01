package ru.visionary.mixing.mind_broker.entity;

import lombok.Builder;

@Builder
public record Style(
    Integer id,
    String name,
    String icon,
    Boolean active
) {}
