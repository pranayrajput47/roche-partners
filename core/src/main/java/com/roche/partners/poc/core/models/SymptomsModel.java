package com.roche.partners.poc.core.models;
import com.roche.partners.poc.core.beans.Symptoms;
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
public class SymptomsModel {
    @ChildResource
    private Resource symptomsmultifield;

    private List<Symptoms> symptomsList;

    @PostConstruct
    protected void init() {

        symptomsList = new ArrayList<>();
        if (symptomsmultifield != null) {
            symptomsmultifield.listChildren().forEachRemaining(item -> {
                ValueMap valueMap = item.getValueMap();
                String heading = valueMap.get("heading", StringUtils.EMPTY);
                String description = valueMap.get("description", StringUtils.EMPTY);


                Symptoms symptoms = new Symptoms(heading, description);
                symptomsList.add(symptoms);

            });
        }
    }


    public List<Symptoms> getSymptomsList() {
        return symptomsList;
    }
}
