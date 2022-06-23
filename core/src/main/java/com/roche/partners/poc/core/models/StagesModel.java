package com.roche.partners.poc.core.models;

import com.adobe.cq.export.json.ExporterConstants;
import com.roche.partners.poc.core.beans.Roche;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.ExporterOption;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION,
        options = {
                @ExporterOption(name = "MapperFeature.SORT_PROPERTIES_ALPHABETICALLY", value = "true"),
                @ExporterOption(name = "SerializationFeature.WRITE_DATES_AS_TIMESTAMPS", value = "false")
        })
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
                String imageAltText =  valueMap.get("imageAltText", StringUtils.EMPTY);

                List<String> subStagesList = new ArrayList<>();

                // Extract the Resource object for the nested multi-field
                Resource stagesResource = item.getChild("subStages");
                if(stagesResource != null) {
                    stagesResource.listChildren().forEachRemaining(child -> {
                        // Iterate the children and get the data in Java Bean
                        ValueMap childValueMap = child.getValueMap();
                        String summary =  childValueMap.get("summary", StringUtils.EMPTY);
                        subStagesList.add(summary);
                    });
                }

                Roche roche = new Roche(description,image,imageAltText, subStagesList);

                rocheList.add(roche);
            });
        }
    }

    public List<Roche> getRocheList() {
        return rocheList;
    }
}
