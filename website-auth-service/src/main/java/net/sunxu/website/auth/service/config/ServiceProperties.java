package net.sunxu.website.auth.service.config;

import java.util.Set;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ServiceProperties {

    private Set<String> redirectUrls;

}
