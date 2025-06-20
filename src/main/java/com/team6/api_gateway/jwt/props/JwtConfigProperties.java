package com.team6.api_gateway.jwt.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(value="jwt", ignoreUnknownFields = true)
@Getter
@Setter
public class JwtConfigProperties {
    private String header;
    private String secretKey;
}
