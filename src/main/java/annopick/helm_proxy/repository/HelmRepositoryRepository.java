package annopick.helm_proxy.repository;

import annopick.helm_proxy.entity.HelmRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HelmRepositoryRepository extends JpaRepository<HelmRepository, Long> {

    Optional<HelmRepository> findByName(String name);

    List<HelmRepository> findByEnabledTrue();

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
