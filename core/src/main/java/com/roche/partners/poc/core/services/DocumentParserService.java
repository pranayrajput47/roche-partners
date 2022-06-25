package com.roche.partners.poc.core.services;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public interface DocumentParserService {

    void fetchHTMLDocument(ResourceResolver resourceResolver, String activatedPage, String pageName,
                           List<String> tagNames, Iterator<Resource> components) throws ServletException, IOException;
}
