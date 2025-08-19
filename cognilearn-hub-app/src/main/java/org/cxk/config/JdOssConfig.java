package org.cxk.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JdOssConfig {

    @Value("${jdoss.access-key}")
    private String accessKey;

    @Value("${jdoss.secret-key}")
    private String secretKey;

    @Value("${jdoss.endpoint}")
    private String endpoint;   // 比如 oss.cn-north-1.jcloudcs.com

    @Value("${jdoss.region}")
    private String region;     // 比如 cn-north-1

    @Value("${jdoss.bucket-name}")
    private String bucket;

    @Bean
    public AmazonS3 amazonS3() {
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(endpoint, region)
                )
                .withPathStyleAccessEnabled(true) // 京东云OSS一般需要这个
                .build();
    }

    public String getBucket() {
        return bucket;
    }

    public String getEndpoint() {
        return endpoint;
    }
}
