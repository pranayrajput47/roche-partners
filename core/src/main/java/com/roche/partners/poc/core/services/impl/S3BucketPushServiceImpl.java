package com.roche.partners.poc.core.services.impl;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
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

    @Activate
    protected final void activate() {
        log.info("Activated S3BucketPushServiceImpl");
    }

    @Deactivate
    protected void deactivate() {
        log.info("Deactivated S3BucketPushServiceImpl");
    }

    @Override
    public void pushContentToS3(String bucketName) {
        String fileName = "article1";
        try {
            log.info("Inside Push Content to s3");

            AWSCredentials credentials = new BasicAWSCredentials(
                    "AKIA6E6SUNG5Q6YFTP7V",
                    "S/2qdKP+NI+yIkrJ7Bi+3V1bOyw7v+vdOiljt60z"
            );
            AmazonS3 client =
                    AmazonS3ClientBuilder.standard()
                            .withCredentials(new AWSStaticCredentialsProvider(credentials))
                            .withRegion("ap-south-1") // The first region to try your request against
                            .build();

            log.info("client :: {}",client );

            if(client.doesBucketExist(bucketName)) {
                log.info("Bucket name is not available."
                        + " Try again with a different Bucket name.");
                return;
            }
            client.createBucket(bucketName);

        } catch (AmazonS3Exception e) {
            log.error("Excepion in Amazon S3 Bucket :: " + e.getMessage());
        } catch (AmazonClientException e) {
            log.error("Excepion in Amazon Client :: " + e.getMessage());
        } catch (Exception e) {
            log.error("Excepion in pushContentToS3 method of S3BucketPushServiceImpl :: " + e.getMessage());
        }
    }

}
