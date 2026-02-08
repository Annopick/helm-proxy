package annopick.helm_proxy.repository;

import annopick.helm_proxy.entity.HelmCharts;
import annopick.helm_proxy.entity.HelmChartsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HelmChartsRepository extends JpaRepository<HelmCharts, HelmChartsId> {

    @Query("SELECT c FROM HelmCharts c WHERE c.id.repoId = :repo_id")
    List<HelmCharts> findByRepositoryId(@Param("repo_id") Long repo_id);

    @Query("SELECT c FROM HelmCharts c WHERE c.enabled = true")
    List<HelmCharts> findAllByRepositoryEnabled();

    @Modifying
    @Query("DELETE FROM HelmCharts c WHERE c.id.repoId = :repo_id")
    void deleteByRepositoryId(@Param("repo_id") Long repo_id);
}
