package com.roche.partners.poc.core.services.impl;


import com.roche.partners.poc.core.services.S3BucketPushService;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

@Component(immediate = true, service = S3BucketPushService.class, property = {
        Constants.SERVICE_DESCRIPTION + "= Pushes Content to S3 Bucket"})
public class S3BucketPushServiceImpl {

}
