package com.roche.partners.poc.core.services.impl;

import com.roche.partners.poc.core.services.HTMLParserService;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

@Component(immediate = true, service = HTMLParserService.class, property = {
        Constants.SERVICE_DESCRIPTION + "= Parses HTML using JSOUP"})
public class HTMLParserServiceImpl implements HTMLParserService {
}
