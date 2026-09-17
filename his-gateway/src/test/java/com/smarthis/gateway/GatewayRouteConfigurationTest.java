package com.smarthis.gateway;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayRouteConfigurationTest {

    @ParameterizedTest
    @ValueSource(strings = {"resource", "collaboration", "cdss", "drg", "emergency", "platform"})
    void extensionServiceRoutesAreAbsent(String service) {
        List<Map<String, Object>> routes = routes();

        assertThat(routes).noneMatch(route -> ("his-" + service).equals(route.get("id"))
                || ("his-" + service + "-health").equals(route.get("id")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"auth", "patient", "clinical", "operations", "pharma"})
    void legacyHealthEndpointIsAvailableThroughServiceApiPrefix(String service) {
        List<Map<String, Object>> routes = routes();

        Map<String, Object> healthRoute = routes.stream()
                .filter(route -> ("his-" + service + "-health").equals(route.get("id")))
                .findFirst()
                .orElseThrow();

        assertThat(healthRoute.get("uri")).isEqualTo("lb://his-" + service);
        assertThat(healthRoute.get("order")).isEqualTo(-1);
        assertThat(healthRoute.get("predicates")).isEqualTo(List.of("Path=/api/" + service + "/health"));
        String expectedPath = "pharma".equals(service) ? "/api/pharma/health" : "/health";
        assertThat(healthRoute.get("filters")).isEqualTo(List.of("SetPath=" + expectedPath));
    }

    private List<Map<String, Object>> routes() {
        InputStream yamlStream = getClass().getResourceAsStream("/application.yml");
        assertThat(yamlStream).isNotNull();

        Map<String, Object> root = new Yaml().load(yamlStream);
        Map<String, Object> spring = child(root, "spring");
        Map<String, Object> cloud = child(spring, "cloud");
        Map<String, Object> gateway = child(cloud, "gateway");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> routes = (List<Map<String, Object>>) gateway.get("routes");

        return routes;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> child(Map<String, Object> parent, String key) {
        return (Map<String, Object>) parent.get(key);
    }
}
