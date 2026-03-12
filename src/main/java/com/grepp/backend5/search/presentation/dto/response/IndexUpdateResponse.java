package com.grepp.backend5.search.presentation.dto.response;

public record IndexUpdateResponse(boolean created, boolean settingsUpdated, boolean mappingUpdated) {
}
