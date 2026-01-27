package annopick.helm_proxy.controller;

import annopick.helm_proxy.dto.ApiResponse;
import annopick.helm_proxy.dto.HelmRepositoryDTO;
import annopick.helm_proxy.entity.HelmRepository;
import annopick.helm_proxy.service.HelmChartsSyncService;
import annopick.helm_proxy.service.HelmRepositoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repositories")
@RequiredArgsConstructor
public class HelmRepositoryController {

    private final HelmRepositoryService repositoryService;
    private final HelmChartsSyncService  chartsSyncService;

    @GetMapping
    public ApiResponse<List<HelmRepository>> findAll() {
        return ApiResponse.success(repositoryService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HelmRepository>> findById(@PathVariable Long id) {
        return repositoryService.findById(id)
                .map(repo -> ResponseEntity.ok(ApiResponse.success(repo)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HelmRepository>> create(@RequestBody HelmRepositoryDTO dto) {
        try {
            HelmRepository repo = repositoryService.create(dto);
            return ResponseEntity.ok(ApiResponse.success("Repository created", repo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HelmRepository>> update(@PathVariable Long id,
                                                               @RequestBody HelmRepositoryDTO dto) {
        try {
            HelmRepository repo = repositoryService.update(id, dto);
            return ResponseEntity.ok(ApiResponse.success("Repository updated", repo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            repositoryService.delete(id);
            return ResponseEntity.ok(ApiResponse.success("Repository deleted", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<HelmRepository>> toggleEnabled(@PathVariable Long id,
                                                                      @RequestParam boolean enabled) {
        try {
            HelmRepository repo = repositoryService.toggleEnabled(id, enabled);
            return ResponseEntity.ok(ApiResponse.success("Repository status updated", repo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/sync")
    public ResponseEntity<ApiResponse<Void>> syncRepository(@PathVariable Long id) {
        try {
            chartsSyncService.syncRepository(id);
            return ResponseEntity.ok(ApiResponse.success("Sync completed", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Sync failed: " + e.getMessage()));
        }
    }
}
