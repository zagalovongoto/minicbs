package org.gimuemoa.minicbs.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "minicbs.init")
@Data
public class BankInitProperties {

    private AdminUser admin;
    private List<String> roles;
    private List<SystemParamInput> parameters;

    @Data
    public static class AdminUser {
        private String nom;
        private String prenom;
        private String email;
        private String telephone;
        private String password;
    }

    @Data
    public static class SystemParamInput {
        private String key;
        private String value;
        private String description;
        private boolean editable;
    }
}
