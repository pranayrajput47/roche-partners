package com.roche.partners.poc.core.services.impl;

import com.roche.partners.poc.core.services.S3BucketPushService;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component(immediate = true, service = S3BucketPushService.class, property = {
        Constants.SERVICE_DESCRIPTION + "= Pushes Content to S3 Bucket"})
@Slf4j
public class S3BucketPushServiceImpl implements S3BucketPushService {

    String bucketName = "roche-zs-bucket";

    @Activate
    protected final void activate() {
        log.info("Activated S3BucketPushServiceImpl");
    }

    @Deactivate
    protected void deactivate() {
        log.info("Deactivated S3BucketPushServiceImpl");
    }

    @Override
    public void pushContentToS3(String html) {
        String fileName = "article1";
        try {
            S3Client client = S3Client.builder().build();

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName).key(fileName).build();
        } catch (Exception e) {
            log.error("Excepion in activate method of S3BucketPushServiceImpl :: " + e.getMessage());
        }
    }

}
