package annopick.helm_proxy.scheduler;

import annopick.helm_proxy.service.HelmChartsSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class HelmSyncScheduler {

    private final HelmChartsSyncService chartsSyncService;

    @Scheduled(cron = "${helm.sync.cron:0 0 * * * ?}")
    public void chartsSync() {
        log.info("Starting scheduled sync...");
        try {
            chartsSyncService.syncAllRepositories();
            log.info("Scheduled sync completed successfully");
        } catch (Exception e) {
            log.error("Scheduled sync failed", e);
        }
    }

}
