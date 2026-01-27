package annopick.helm_proxy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "helm_charts", schema = "helm-proxy")
@AllArgsConstructor
@NoArgsConstructor
public class HelmCharts {
    @EmbeddedId
    private HelmChartsId id;

    @Lob
    @Column(name = "detail_info", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String detailInfo;

    @Column(name = "local_app_name", nullable = false, length = 200)
    private String localAppName;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
}