package com.roche.partners.poc.core.services.impl;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
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
import com.amazonaws.services.s3.model.BucketCrossOriginConfiguration;
import com.amazonaws.services.s3.model.CORSRule;
import com.roche.partners.poc.core.services.S3BucketPushService;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Designate(ocd = S3BucketPushServiceImpl.Config.class)
@Component(immediate = true, service = S3BucketPushService.class, property = {
        Constants.SERVICE_DESCRIPTION + "= Pushes Content to S3 Bucket"})
@Slf4j
public class S3BucketPushServiceImpl implements S3BucketPushService {

    private String region="";
    private String accessKeyId="";
    private String accessSecretKey="";

    @Activate
    protected final void activate(Config config) {
        region= config.s3_Region();
        accessKeyId= config.s3_access_key_id();
        accessSecretKey= config.s3_secret_key();
        log.info("Activated S3BucketPushServiceImpl");
    }

    @Deactivate
    protected void deactivate() {
        log.info("Deactivated S3BucketPushServiceImpl");
    }

    @Override
    public void pushContentToS3(String bucketName, String filePath, String fileS3Path) {
        try {
            AmazonS3 client = buildS3Client();
            client.putObject(
                    bucketName,
                    fileS3Path,
                    new File(filePath));

        } catch (AmazonS3Exception e) {
            log.error("Excepion in Amazon S3 Bucket :: " + e.getMessage());
        } catch (AmazonClientException e) {
            log.error("Exception in Amazon Client :: " + e.getMessage());
        } catch (Exception e) {
            log.error("Exception in pushContentToS3 method of S3BucketPushServiceImpl :: " + e.getMessage());
        }
    }

    @Override
    public void createBucket(String bucketName) {
        try {
            AmazonS3 client = buildS3Client();

            if(client.doesBucketExist(bucketName)) {
                log.debug("Bucket already exists.");
                return;
            }
            client.createBucket(bucketName);
            Policy bucketPolicy = new Policy().withStatements(
                    new Statement(Statement.Effect.Allow)
                            .withPrincipals(Principal.AllUsers)
                            .withActions(S3Actions.GetObject)
                            .withResources(new Resource(
                                    "arn:aws:s3:::" + bucketName + "/*")));
            log.debug("bucket policy :: {}", bucketPolicy.toJson());
            client.setBucketPolicy(bucketName, bucketPolicy.toJson());

            List<CORSRule.AllowedMethods> rule2AM = new ArrayList<CORSRule.AllowedMethods>();
            rule2AM.add(CORSRule.AllowedMethods.GET);
            rule2AM.add(CORSRule.AllowedMethods.HEAD);
            CORSRule rule2 = new CORSRule().withId("CORSRule2").withAllowedHeaders(Arrays.asList("*")).withAllowedMethods(rule2AM)
                    .withAllowedOrigins(Arrays.asList("*")).withMaxAgeSeconds(3000)
                    .withExposedHeaders(Arrays.asList());

            List<CORSRule> rules = new ArrayList<CORSRule>();
            rules.add(rule2);

            // Add the rules to a new CORS configuration.
            BucketCrossOriginConfiguration configuration = new BucketCrossOriginConfiguration();
            configuration.setRules(rules);

            // Add the configuration to the bucket.
            client.setBucketCrossOriginConfiguration(bucketName, configuration);


        } catch (AmazonServiceException e) {
            log.error("Exception in Amazon S3 Bucket :: " + e.getMessage());
        } catch (AmazonClientException e) {
            log.error("Exception in Amazon Client :: " + e.getMessage());
        } catch (Exception e) {
            log.error("Exception in create bucket method of S3BucketPushServiceImpl :: " + e.getMessage());
        }
    }

    private AmazonS3 buildS3Client() {
        AmazonS3 client = AmazonS3ClientBuilder.defaultClient();
        try {
            AWSCredentials credentials = new BasicAWSCredentials(accessKeyId, accessSecretKey);
             client =
                    AmazonS3ClientBuilder.standard()
                            .withCredentials(new AWSStaticCredentialsProvider(credentials))
                            .withRegion(region) // The first region to try your request against
                            .build();

            return client;

        } catch (AmazonClientException e) {
            log.error("Exception in Amazon Client :: " + e.getMessage());
        } catch (Exception e) {
            log.error("Exception in generateS3CLient method of S3BucketPushServiceImpl :: " + e.getMessage());
        }
        return client;
    }

    @ObjectClassDefinition(name = "S3 Bucket Push Service")
    public @interface Config {

        @AttributeDefinition(name = "S3 Region")
        String s3_Region() default "ap-south-1";

        @AttributeDefinition(name = "S3 Access Key ID")
        String s3_access_key_id() default "";

        @AttributeDefinition(
                name = "S3 Secret Access Key",
                type = AttributeType.PASSWORD)
        String s3_secret_key() default "";
    }

    @Override
    public String getRegion() {
        return region;
    }
}
