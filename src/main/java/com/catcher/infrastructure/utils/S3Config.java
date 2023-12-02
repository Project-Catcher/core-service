package com.catcher.infrastructure.utils;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {

    @Bean
    public AmazonS3Client amazonS3Client(){
        return (AmazonS3Client) AmazonS3ClientBuilder
                .standard()
                .withRegion(Regions.AP_NORTHEAST_2)
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .build();
    }
}
