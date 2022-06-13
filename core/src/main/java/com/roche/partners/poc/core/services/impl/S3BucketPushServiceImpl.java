package com.roche.partners.poc.core.services.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.roche.partners.poc.core.services.S3BucketPushService;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

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
            AmazonS3 client =
                    AmazonS3ClientBuilder.standard()
                            .withRegion("ap-south-1") // The first region to try your request against
                            .build();

            log.info("client :: {}",client );

        } catch (Exception e) {
            log.error("Excepion in activate method of S3BucketPushServiceImpl :: " + e.getMessage());
        }
    }

}
