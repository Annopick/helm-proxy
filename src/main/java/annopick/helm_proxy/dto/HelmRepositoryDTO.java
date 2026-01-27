package annopick.helm_proxy.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HelmRepositoryDTO {
    private String name;
    private String url;
    private String description;
    private Boolean enabled;
}
