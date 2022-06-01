package com.roche.partners.poc.core.models;

import com.roche.partners.poc.core.beans.Roche;
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
public class StagesModel {
    @ChildResource
   private Resource rochemultifield;
    private List<Roche> rocheList;

    @PostConstruct
    protected void init(){

        rocheList = new ArrayList<>();
        if (rochemultifield != null){
            rochemultifield.listChildren().forEachRemaining(item-> {
               ValueMap valueMap = item.getValueMap();
                String description =  valueMap.get("description", StringUtils.EMPTY);
                String image=   valueMap.get("image", StringUtils.EMPTY);
                String link =  valueMap.get("link", StringUtils.EMPTY);

                Roche roche = new Roche(description,image,link);

                rocheList.add(roche);
            });
        }
    }

    public List<Roche> getRocheList() {
        return rocheList;
    }
}
