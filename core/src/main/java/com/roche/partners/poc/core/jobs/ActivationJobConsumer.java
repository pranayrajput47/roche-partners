package com.roche.partners.poc.core.jobs;

import com.day.cq.replication.ReplicationAction;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import com.roche.partners.poc.core.services.DocumentParserService;
import lombok.extern.slf4j.Slf4j;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.util.*;

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

    @Reference
    private DocumentParserService documentParserService;

    @Activate
    protected void activate() {
        log.info("Activated ActivationJobConsumer");
    }

    @Override
    public JobResult process(Job job) {
        boolean status = false;
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put(ResourceResolverFactory.SUBSERVICE, "replicationService");
        try (ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(paramMap)) {
            ReplicationAction action = (ReplicationAction) job.getProperty("resourcePath");
            log.info("ActivationJobConsumer : process() : The path in which the replication is triggered and passed to the Job is " +
                    "{}", action.getPath());

            Resource activatedPageResource = resourceResolver.getResource(action.getPath());
            Resource jcrResource = activatedPageResource.getChild("jcr:content");

            Resource componentResource = resourceResolver.getResource(action.getPath()+"/jcr:content/root/container/container");

            Iterator<Resource> components = null;
            if (activatedPageResource != null) {
                components = componentResource.listChildren();
            }

            Resource parentResource = activatedPageResource.getParent();
            String parentPath= "";
            if (parentResource instanceof Resource) {
                 parentPath = parentResource.getPath();
            }
            if (activatedPageResource instanceof Resource) {
                Page activatedPage = activatedPageResource.adaptTo(Page.class);
                ValueMap pageProperties = activatedPage.getProperties();
                String[] tags = pageProperties.get("cq:tags", String[].class);
                final TagManager tagManager = resourceResolver.adaptTo(TagManager.class);
                final List<String> tagNames = new ArrayList<String>();
                for (Tag tag : tagManager.getTags(jcrResource)) {
                    log.info("Name :: {}, Title :: {}",tag.getName(), tag.getTitle());
                    tagNames.add(tag.getName());
                }

                log.info("tagNames :: {}", tagNames);
                String pageName = activatedPage.getName();
                if (activatedPage != null) {
                    documentParserService.fetchHTMLDocument(resourceResolver, action.getPath(), pageName, tagNames, components);
                }
                if (parentPath != null) {
                    documentParserService.fetchHTMLDocument(resourceResolver, "/content/roche-partners/naviagtion", "navigation", tagNames, components);
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