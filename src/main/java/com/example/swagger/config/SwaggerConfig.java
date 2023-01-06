package com.example.swagger.config;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@PropertySource(value = "classpath:swagger.yaml", factory = YamlPropertySourceFactory.class, ignoreResourceNotFound = true)
public class SwaggerConfig {

    /* To override description, title and version
    There are default values without overriding
    */
    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(new Info()
                        .description("Description")
                        .title("Title")
                        .version("Version"));
    }

    /* To customize errors
    */
    @Bean
    public GlobalOpenApiCustomizer globalOpenApiCustomizer() {
        return openApi -> {
            moveCommonVersionPrefixToServerUrl(openApi);

            var defaultResponses = Map.of(
                    "400", apiResponse(openApi, "Неверные параметры запроса"),
                    "401", apiResponse(openApi, "Ошибка аутентификации"),
                    "403", apiResponse(openApi, "Отсутствие прав"),
                    "404", apiResponse(openApi, "Данные не найдены"),
                    "422", apiResponse(openApi, "Бизнес-ошибка"),
                    "500", apiResponse(openApi, "Внутренняя ошибка сервера"),
                    "502", apiResponse(openApi, "Ошибка шлюза"),
                    "503", apiResponse(openApi, "Сервис временно недоступен"),
                    "504", apiResponse(openApi, "Таймаут шлюза")
            );

            openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(operation ->
                            defaultResponses.forEach((code, response) ->
                                    operation.getResponses().addApiResponse(code, response))
                    )
            );
        };
    }

    private void moveCommonVersionPrefixToServerUrl(OpenAPI openApi) {
        var paths = openApi.getPaths();
        var version = openApi.getInfo().getVersion();
        var versionPart = "/" + version;

        if (paths.keySet().stream().allMatch(path -> path.startsWith(versionPart))) {
            openApi.getServers().forEach(server ->
                    server.setUrl(server.getUrl() + versionPart)
            );

            var oldPaths = new LinkedHashMap<>(paths);

            paths.clear();

            oldPaths.forEach((path, pathItem) -> {
                paths.addPathItem(path.substring(versionPart.length()), pathItem);
            });
        }
    }

    private static ApiResponse apiResponse(OpenAPI openApi, String description) {
        return apiResponse(openApi, description, null);
    }

    private static ApiResponse apiResponse(OpenAPI openApi, String description, Class<?> errorClass) {
        if (errorClass == null) {
            return new ApiResponse().description(description);
        }

        registerClass(openApi, errorClass);

        var className = errorClass.getSimpleName();
        var mediaType = new MediaType().schema(new Schema<>()
                .name(className)
                .$ref("#/components/schemas/" + className));
        var content = new Content()
                .addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE, mediaType);

        return new ApiResponse()
                .description(description)
                .content(content);
    }

    private static void registerClass(OpenAPI openApi, Class<?> errorClass) {
        var schemas = openApi.getComponents().getSchemas();
        if (schemas == null) {
            schemas = new LinkedHashMap<>();
            openApi.getComponents().setSchemas(schemas);
        }
        schemas.putAll(ModelConverters.getInstance().read(errorClass));
    }
}
