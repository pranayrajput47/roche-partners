package com.roche.partners.poc.core.services.impl;

import com.day.cq.contentsync.handler.util.RequestResponseFactory;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.WCMMode;
import com.roche.partners.poc.core.services.HTMLParserService;
import com.roche.partners.poc.core.services.S3BucketPushService;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.engine.SlingRequestProcessor;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Session;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component(immediate = true, service = HTMLParserService.class, property = {
        Constants.SERVICE_DESCRIPTION + "= Parses HTML using JSOUP"})
@Slf4j
public class HTMLParserServiceImpl implements HTMLParserService {

    @Reference
    private RequestResponseFactory requestResponseFactory;

    @Reference
    private SlingRequestProcessor requestProcessor;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private S3BucketPushService s3BucketPushService;

    @Activate
    protected final void activate() {
        log.info("Activated HTMLParserServiceImpl");
    }

    @Deactivate
    protected void deactivate() {
        log.info("Deactivated HTMLParserServiceImpl");
    }

    @Override
    public void fetchHTMLDocument(ResourceResolver resourceResolver, String activatedPage) throws ServletException, IOException {
        String htmlString = "";
        try {
            HttpServletRequest request = requestResponseFactory.createRequest("GET", activatedPage+".html");
            request.setAttribute(WCMMode.REQUEST_ATTRIBUTE_NAME, WCMMode.DISABLED);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            HttpServletResponse response = requestResponseFactory.createResponse(out);

            requestProcessor.processRequest(request, response, resourceResolver);
            htmlString=  out.toString(response.getCharacterEncoding());
            s3BucketPushService.pushContentToS3(htmlString);
            log.info("htmlString :: {}",htmlString);
        } catch (Exception e) {
            log.error("Excepion in activate method of HTMLParserServiceImpl :: " + e.getMessage());
        }
    }
}