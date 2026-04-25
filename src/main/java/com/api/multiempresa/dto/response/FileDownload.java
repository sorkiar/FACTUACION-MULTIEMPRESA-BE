package com.api.multiempresa.dto.response;

public record FileDownload(String filename, String contentType, byte[] content) {}
