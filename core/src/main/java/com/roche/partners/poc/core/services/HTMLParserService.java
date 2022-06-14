package com.roche.partners.poc.core.services;

import org.apache.sling.api.resource.ResourceResolver;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;

public interface HTMLParserService {

    void fetchHTMLDocument(ResourceResolver resourceResolver, String activatedPage, String pageName, List<String> tagNames) throws ServletException, IOException;
}
