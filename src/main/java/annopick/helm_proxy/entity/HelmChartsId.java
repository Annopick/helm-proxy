package annopick.helm_proxy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class HelmChartsId implements Serializable {
    private static final long serialVersionUID = 5642174176275010895L;
    @Column(name = "app_name", nullable = false, length = 200)
    private String appName;

    @Column(name = "version", nullable = false, length = 200)
    private String version;

    @Column(name = "repo_id", nullable = false)
    private Long repoId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        HelmChartsId entity = (HelmChartsId) o;
        return Objects.equals(this.repoId, entity.repoId) &&
                Objects.equals(this.appName, entity.appName) &&
                Objects.equals(this.version, entity.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repoId, appName, version);
    }

}