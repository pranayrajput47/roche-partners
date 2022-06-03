package com.roche.partners.poc.core.services;

import org.apache.sling.api.resource.ResourceResolver;

import javax.servlet.ServletException;
import java.io.IOException;

public interface HTMLParserService {

    void fetchHTMLDocument(ResourceResolver resourceResolver, String activatedPage) throws ServletException, IOException;
}
