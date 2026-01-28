package annopick.helm_proxy.service;

import annopick.helm_proxy.entity.HelmCharts;
import annopick.helm_proxy.entity.HelmChartsId;
import annopick.helm_proxy.entity.HelmRepository;
import annopick.helm_proxy.repository.HelmChartsRepository;
import annopick.helm_proxy.repository.HelmRepositoryRepository;
import annopick.helm_proxy.utils.URLProcessorUtil;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class HelmChartsSyncService {

    private final HelmRepositoryRepository repositoryRepository;
    private final HelmChartsRepository chartsRepository;
    private final RestTemplate restTemplate;

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final ObjectMapper jsonMapper = new ObjectMapper(new JsonFactory());

    public void syncAllRepositories() {
        List<HelmRepository> repos = repositoryRepository.findByEnabledTrue();
        log.info("Starting sync for {} repositories", repos.size());

        for (HelmRepository repo : repos) {
            try {
                syncRepositoryInternal(repo);
            } catch (Exception e) {
                log.error("Failed to sync repository {}: {}", repo.getName(), e.getMessage());
            }
        }

        log.info("Sync completed for all repositories");
    }

    @Transactional
    public void syncRepository(Long repositoryId) {
        HelmRepository repo = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new IllegalArgumentException("Repository not found: " + repositoryId));

        syncRepositoryInternal(repo);
    }

    @Transactional
    public void syncRepositoryInternal(HelmRepository repo) {
        log.info("从[{}]同步仓库: {}", repo.getUrl(), repo.getName());

        try {
            String yamlContent = restTemplate.getForObject(repo.getUrl() + "/index.yaml", String.class);
            if (yamlContent == null || yamlContent.isEmpty()) {
                throw new RuntimeException("Empty index.yaml received");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> indexData = yamlMapper.readValue(yamlContent, Map.class);

            @SuppressWarnings("unchecked")
            Map<String, List<Map<String, Object>>> entries =
                    (Map<String, List<Map<String, Object>>>) indexData.get("entries");

            if (entries == null) {
                throw new RuntimeException("No entries found in index.yaml");
            }

            chartsRepository.deleteByRepositoryId(repo.getId());

            List<HelmCharts> charts = new ArrayList<>();
            for (Map.Entry<String, List<Map<String, Object>>> entry : entries.entrySet()) {
                String chartName = entry.getKey();
                List<Map<String, Object>> versions = entry.getValue();

                for (Map<String, Object> versionData : versions) {
                    HelmCharts chart = new HelmCharts();
                    HelmChartsId id = new HelmChartsId(chartName, (String) versionData.get("version"), repo.getId());
                    chart.setId(id);
                    chart.setLocalAppName(repo.getName() + "-" + chartName);

                    // 应对charts下载链接不是完整URL的情况，实现自动补全路径
                    URLProcessorUtil.processUrls(versionData, repo);
                    chart.setDetailInfo(jsonMapper.writeValueAsString(versionData));

                    charts.add(chart);
                }
            }

            chartsRepository.saveAll(charts);
            log.info("成功从[{}]同步{}个制品", repo.getName(), charts.size());
            repo.setLastSyncTime(LocalDateTime.now());
            repo.setSyncStatus(HelmRepository.SyncStatus.SUCCESS);
            repo.setSyncError(null);
            repositoryRepository.save(repo);

            log.info("Successfully synced {} charts from {}", charts.size(), repo.getName());

        } catch (Exception e) {
            log.error("Failed to sync repository {}: {}", repo.getName(), e.getMessage(), e);
            repo.setLastSyncTime(LocalDateTime.now());
            repo.setSyncStatus(HelmRepository.SyncStatus.FAILED);
            repo.setSyncError(e.getMessage());
            repositoryRepository.save(repo);
            throw new RuntimeException("Sync failed: " + e.getMessage(), e);
        }
    }
}
