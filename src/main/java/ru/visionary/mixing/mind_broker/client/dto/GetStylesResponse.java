package ru.visionary.mixing.mind_broker.client.dto;

import java.util.List;

public record GetStylesResponse(
        List<String> styles
) {}
