package annopick.helm_proxy.controller;

import annopick.helm_proxy.service.HelmIndexService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequiredArgsConstructor
public class HelmIndexController {

    private final HelmIndexService indexService;

    private static final Pattern CHART_FILENAME_PATTERN =
            Pattern.compile("^(.+)-([0-9]+\\.[0-9]+\\.[0-9]+.*)$");

    @GetMapping(value = "/index.yaml", produces = "text/yaml")
    public ResponseEntity<String> getIndex(HttpServletRequest request) {
        String baseUrl = getBaseUrl(request);
        String yaml = indexService.generateUnifiedIndex(baseUrl);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/yaml; charset=utf-8"))
                .body(yaml);
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String host = request.getHeader("Host");
        if (host == null) {
            host = request.getServerName() + ":" + request.getServerPort();
        }

        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        if (forwardedProto != null) {
            scheme = forwardedProto;
        }

        return scheme + "://" + host;
    }
}
