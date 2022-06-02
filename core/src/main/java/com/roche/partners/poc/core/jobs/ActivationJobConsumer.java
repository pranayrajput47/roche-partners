package com.roche.partners.poc.core.jobs;

import com.day.cq.replication.ReplicationAction;
import com.day.cq.wcm.api.Page;
import lombok.extern.slf4j.Slf4j;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * ActivationJobConsumer
 */
@Component(immediate = true, service = JobConsumer.class, property = {
        Constants.SERVICE_DESCRIPTION + "=Sling Job to Offload Page Activation Events",
        JobConsumer.PROPERTY_TOPICS + "=roche-partners/replication/job"
})
@Slf4j
public class ActivationJobConsumer implements JobConsumer {

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Activate
    protected void activate() {
        log.info("Activated ActivationJobConsumer");
    }

    @Override
    public JobResult process(Job job) {
        boolean status = false;
        try (ResourceResolver resourceResolver = null) {
            ReplicationAction action = (ReplicationAction) job.getProperty("resourcePath");
            log.info("ActivationJobConsumer : process() : The path in which the replication is triggered and passed to the Job is " +
                    "{}", action.getPath());
            Resource activatedPageResource = resourceResolver.getResource(action.getPath());
            if (activatedPageResource instanceof Resource) {
                Page activatedPage = activatedPageResource.adaptTo(Page.class);
                if (activatedPage != null) {
                }
            }
            log.info("JobConsumer Status : {}", status);
        } catch (Exception e) {
            log.error("Exception : ActivationJobConsumer : process() :: {}", e);
            return JobResult.CANCEL;
        }
        if (status)
            return JobConsumer.JobResult.OK;
        else
            return JobResult.CANCEL;
    }

    @Deactivate
    protected void deactivate() {
        log.info("Deactivated ActivationJobConsumer");
    }
}