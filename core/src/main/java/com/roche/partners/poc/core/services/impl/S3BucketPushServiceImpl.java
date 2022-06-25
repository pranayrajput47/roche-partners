package com.roche.partners.poc.core.services.impl;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Principal;
import com.amazonaws.auth.policy.Resource;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.actions.S3Actions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.roche.partners.poc.core.services.S3BucketPushService;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.io.File;

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
    public void pushContentToS3(String bucketName, String filePath, String fileS3Path) {
        try {
            log.info("Inside Push Content to s3");

            AmazonS3 client = buildS3Client();

            client.putObject(
                    bucketName,
                    fileS3Path,
                    new File(filePath));

        } catch (AmazonS3Exception e) {
            log.error("Excepion in Amazon S3 Bucket :: " + e.getMessage());
        } catch (AmazonClientException e) {
            log.error("Excepion in Amazon Client :: " + e.getMessage());
        } catch (Exception e) {
            log.error("Excepion in pushContentToS3 method of S3BucketPushServiceImpl :: " + e.getMessage());
        }
    }

    @Override
    public void createBucket(String bucketName) {
        try {
            AmazonS3 client = buildS3Client();

            if(client.doesBucketExist(bucketName)) {
                log.info("Bucket already exists.");
                return;
            }
            client.createBucket(bucketName);
            Policy bucket_policy = new Policy().withStatements(
                    new Statement(Statement.Effect.Allow)
                            .withPrincipals(Principal.AllUsers)
                            .withActions(S3Actions.GetObject)
                            .withResources(new Resource(
                                    "arn:aws:s3:::" + bucketName + "/*")));
            client.setBucketPolicy(bucketName, bucket_policy.toJson());

        } catch (AmazonS3Exception e) {
            log.error("Excepion in Amazon S3 Bucket :: " + e.getMessage());
        } catch (AmazonClientException e) {
            log.error("Excepion in Amazon Client :: " + e.getMessage());
        } catch (Exception e) {
            log.error("Excepion in pushContentToS3 method of S3BucketPushServiceImpl :: " + e.getMessage());
        }
    }

    private AmazonS3 buildS3Client() {
        AmazonS3 client = AmazonS3ClientBuilder.defaultClient();
        try {
            AWSCredentials credentials = new BasicAWSCredentials(
                    "AKIA6E6SUNG5Q6YFTP7V",
                    "S/2qdKP+NI+yIkrJ7Bi+3V1bOyw7v+vdOiljt60z"
            );
             client =
                    AmazonS3ClientBuilder.standard()
                            .withCredentials(new AWSStaticCredentialsProvider(credentials))
                            .withRegion("ap-south-1") // The first region to try your request against
                            .build();

            return client;

        } catch (AmazonClientException e) {
            log.error("Excepion in Amazon Client :: " + e.getMessage());
        } catch (Exception e) {
            log.error("Excepion in generateS3CLient method of S3BucketPushServiceImpl :: " + e.getMessage());
        }
        return client;
    }

}
