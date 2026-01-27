package annopick.helm_proxy.service;

import annopick.helm_proxy.dto.HelmRepositoryDTO;
import annopick.helm_proxy.entity.HelmRepository;
import annopick.helm_proxy.repository.HelmRepositoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HelmRepositoryService {

    private final HelmRepositoryRepository repositoryRepository;

    public List<HelmRepository> findAll() {
        return repositoryRepository.findAll();
    }

    public Optional<HelmRepository> findById(Long id) {
        return repositoryRepository.findById(id);
    }

    @Transactional
    public HelmRepository create(HelmRepositoryDTO dto) {
        if (repositoryRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Repository name already exists: " + dto.getName());
        }

        HelmRepository repo = new HelmRepository();
        repo.setName(dto.getName());
        repo.setUrl(dto.getUrl());
        repo.setDescription(dto.getDescription());
        repo.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
        repo.setSyncStatus(HelmRepository.SyncStatus.PENDING);

        log.info("Creating repository: {}", dto.getName());
        return repositoryRepository.save(repo);
    }

    @Transactional
    public HelmRepository update(Long id, HelmRepositoryDTO dto) {
        HelmRepository repo = repositoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Repository not found: " + id));

        if (repositoryRepository.existsByNameAndIdNot(dto.getName(), id)) {
            throw new IllegalArgumentException("Repository name already exists: " + dto.getName());
        }

        repo.setName(dto.getName());
        repo.setUrl(dto.getUrl());
        repo.setDescription(dto.getDescription());
        if (dto.getEnabled() != null) {
            repo.setEnabled(dto.getEnabled());
        }

        log.info("Updating repository: {}", dto.getName());
        return repositoryRepository.save(repo);
    }

    @Transactional
    public void delete(Long id) {
        HelmRepository repo = repositoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Repository not found: " + id));

        log.info("Deleting repository: {}", repo.getName());
        repositoryRepository.delete(repo);
    }

    @Transactional
    public HelmRepository toggleEnabled(Long id, boolean enabled) {
        HelmRepository repo = repositoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Repository not found: " + id));

        repo.setEnabled(enabled);
        log.info("Repository {} enabled: {}", repo.getName(), enabled);
        return repositoryRepository.save(repo);
    }
}
