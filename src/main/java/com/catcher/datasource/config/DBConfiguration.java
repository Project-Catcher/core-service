package com.catcher.datasource.config;

import com.catcher.infrastructure.utils.KmsUtils;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class DBConfiguration {
    private final KmsUtils kmsUtils;
    /**
     * DB
     */
    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUsername;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    /**
     * SSH
     */
    @Value("${ssh.host}")
    private String sshHost;

    @Value("${ssh.port}")
    private int sshPort;

    @Value("${ssh.username}")
    private String sshUsername;

    @Value("${ssh.password}")
    private String sshPassword;

    @Value("${ssh.datasource.origin}")
    private String originUrl;

    @Value("${ssh.local-port}")
    private int localPort;

    @Bean
    public DataSource dataSource() throws Exception {

        JSch jsch = new JSch();
        Session session = jsch.getSession(
                kmsUtils.decrypt(sshUsername),
                kmsUtils.decrypt(sshHost),
                sshPort
        );
        session.setPassword(kmsUtils.decrypt(sshPassword));
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        int assignedPort = session.setPortForwardingL(0,
                kmsUtils.decrypt(originUrl),
                localPort
        );

        return DataSourceBuilder.create()
                .url(kmsUtils.decrypt(databaseUrl).replace(Integer.toString(localPort), Integer.toString(assignedPort)))
                .username(kmsUtils.decrypt(databaseUsername))
                .password(kmsUtils.decrypt(databasePassword))
                .build();
    }
}
