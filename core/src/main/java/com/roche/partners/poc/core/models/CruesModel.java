package com.roche.partners.poc.core.models;

import com.roche.partners.poc.core.beans.Cures;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class CruesModel {

    @ChildResource
    private Resource curesmultifield;

    private List<Cures> curesList;

    @PostConstruct
    protected void init() {

        curesList = new ArrayList<>();
        if (curesmultifield != null) {
            curesmultifield.listChildren().forEachRemaining(item -> {
                ValueMap valueMap = item.getValueMap();
                String heading = valueMap.get("heading", StringUtils.EMPTY);
                String description = valueMap.get("description", StringUtils.EMPTY);

                Cures cures = new Cures(heading, description);
                curesList.add(cures);

            });
        }
    }

    public List<Cures> getCuresList() {
        return curesList;
    }
}
