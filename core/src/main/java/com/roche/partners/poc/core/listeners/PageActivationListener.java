/*
 *  Copyright 2018 ICICI Hfc
 */
package com.roche.partners.poc.core.listeners;

import com.day.cq.replication.ReplicationAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * PageActivationListener
 */
@Component(immediate = true,
        service = EventHandler.class,
        property = {
                EventConstants.EVENT_TOPIC + "=" + ReplicationAction.EVENT_TOPIC
        })
@Slf4j
public class PageActivationListener implements EventHandler {

    @Reference
    private SlingSettingsService slingSettingsService;

    @Reference
    private JobManager jobManager;

    @Activate
    protected final void activate() {
        log.info("Activated PageActivationListener");
    }

    @Override
    public void handleEvent(Event event) {
        try {
            ReplicationAction action = ReplicationAction.fromEvent(event);
            if (action != null) {
                log.info("Replication action {} occurred on {}", action.getType().getName(), action.getPath());
                boolean isAuthor = slingSettingsService.getRunModes().contains("author");
                if (isAuthor) {
                    Map<String, Object> jobProperties = new HashMap<>();
                    jobProperties.put("resourcePath", action);
                    jobManager.addJob("roche-partners/replication/job", jobProperties);
                    log.debug("PageActivationListener : handleEvent() : The job has been started for: {}", jobProperties);
                }
            }
        } catch (Exception e) {
            log.error("Exception : PageActivationListener : handleEvent() :: ", e);
        }
    }

    @Deactivate
    protected final void deactivate() {
        log.info("Deactivated PageActivationListener");
    }
}