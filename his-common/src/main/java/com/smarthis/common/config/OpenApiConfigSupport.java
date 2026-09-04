package com.smarthis.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

public final class OpenApiConfigSupport {

    private OpenApiConfigSupport() {}

    public static OpenAPI createOpenApi(String title, String description) {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .description(description)
                        .version("1.0.0")
                        .contact(new Contact().name("Smart HIS Team")));
    }
}
