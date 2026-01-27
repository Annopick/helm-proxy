package annopick.helm_proxy.service;

import annopick.helm_proxy.entity.HelmCharts;
import annopick.helm_proxy.repository.HelmChartsRepository;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HelmIndexService {

    private final HelmChartsRepository chartsRepository;
    private final ObjectMapper jsonMapper = new ObjectMapper(new JsonFactory());

    private final ObjectMapper yamlMapper = new ObjectMapper(
            new YAMLFactory()
                    .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
    );

    public String generateUnifiedIndex(String baseUrl) {

        List<HelmCharts> charts = chartsRepository.findAllByRepositoryEnabled();

        log.info("仓库目前可用charts数为{}", charts.size());

        Map<String, List<Map<String, Object>>> entries = charts.stream()
                .collect(Collectors.groupingBy(
                        HelmCharts::getLocalAppName,
                        Collectors.mapping(chart -> buildChartEntry(chart), Collectors.toList())
                ));

        entries.forEach((key, list) ->
                list.sort((a, b) -> compareVersions((String) b.get("version"), (String) a.get("version")))
        );

        Map<String, Object> index = new LinkedHashMap<>();
        index.put("apiVersion", "v1");
        index.put("entries", entries);
        index.put("generated", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        try {
            return yamlMapper.writeValueAsString(index);
        } catch (Exception e) {
            log.error("Failed to generate index.yaml", e);
            throw new RuntimeException("Failed to generate index.yaml", e);
        }
    }

    private int compareVersions(String v1, String v2) {
        String[] parts1 = v1.split("[.-]");
        String[] parts2 = v2.split("[.-]");

        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            String p1 = i < parts1.length ? parts1[i] : "0";
            String p2 = i < parts2.length ? parts2[i] : "0";

            try {
                int n1 = Integer.parseInt(p1);
                int n2 = Integer.parseInt(p2);
                if (n1 != n2) {
                    return Integer.compare(n1, n2);
                }
            } catch (NumberFormatException e) {
                int cmp = p1.compareTo(p2);
                if (cmp != 0) {
                    return cmp;
                }
            }
        }
        return 0;
    }

    private Map<String, Object> buildChartEntry(HelmCharts chart) {
        Map<String, Object> entry = new LinkedHashMap<>();
        try {
            entry = jsonMapper.readValue(chart.getDetailInfo(), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Failed to build chart entry", e);
            throw new RuntimeException("Failed to build chart entry", e);
        }
        return entry;
    }
}
