package com.roche.partners.poc.core.services.impl;
import com.day.cq.contentsync.handler.util.RequestResponseFactory;
import com.day.cq.wcm.api.WCMMode;
import com.roche.partners.poc.core.services.HTMLParserService;
import com.roche.partners.poc.core.services.S3BucketPushService;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.engine.SlingRequestProcessor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.util.List;


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
    public void fetchHTMLDocument(ResourceResolver resourceResolver, String activatedPage, String pageName, List<String> tagNames) throws ServletException, IOException {
        try {
            log.info("Inside HtmlParser :: {}",activatedPage);

            String fileType="";
            String jsonString="";
            String navigationItems = "";

            String htmlString = sendRequest(resourceResolver,activatedPage,"html");
            if(pageName == "navigation") {
                log.info("Inside nav :: {}");
                jsonString = sendRequest(resourceResolver, activatedPage+"/jcr:content/root/container/container/list", "json");

                log.info("jsonString :: {}",jsonString);
            }
            else
                //jsonString = sendRequest(resourceResolver,activatedPage,"json");


            for (String partnerName : tagNames) {
            Document document = Jsoup.parse(htmlString, "UTF-8");
            Elements sectionElements = document.getElementsByTag("section");

            if(!activatedPage.equalsIgnoreCase("/content/roche-partners/naviagtion")){
                fileType="";
            }

                // checkDirectory("",partnerName);
                s3BucketPushService.createBucket(partnerName);

                String path = "";
                for (Element src : sectionElements.select("[src]")) {
                    if (src.normalName().equals("img")) {
                        String absUrl = "http://15.207.109.174:4503" + src.attr("src");
                        path = src.attr("src").replaceAll("/content/dam/roche-partners", "https://"+partnerName+".s3.ap-south-1.amazonaws.com/assets/images");
                        src.attr("src", path);
                        getFiles(absUrl, tagNames);
                    } else if (src.normalName().equals("script")) {
                        String jsString = sendRequest(resourceResolver, src.attr("src"), "");
                        path = src.attr("src").replaceAll("/etc.clientlibs/roche-partners/clientlibs", "https://"+partnerName+".s3.ap-south-1.amazonaws.com/assets/js");
                        src.attr("src", path);

                        updateFile(jsString, path, pageName, "js", partnerName);

                    }
                }
                for (Element src : sectionElements.select("[href]")) {
                    if (src.normalName().equals("link")) {
                        String cssString = sendRequest(resourceResolver, src.attr("href"), "");
                        path = src.attr("href").replaceAll("/etc.clientlibs/roche-partners/clientlibs", "https://"+partnerName+".s3.ap-south-1.amazonaws.com/assets/css");
                        src.attr("href", path);

                        updateFile(cssString, path, pageName, "css", partnerName);

                    } else if (src.normalName().equals("div")) {
                        if (pageName == "navigation") {
                            path = src.attr("href").replaceAll("/content/roche-partners/us/en/nsclc/", "https://"+partnerName+".s3.ap-south-1.amazonaws.com/us/en/nsclc/about-the-disease/view/");
                            src.attr("href", path);
                        }
                    }

                }
                if(pageName!= "navigation") {
                    int count = 0;
                    for (Element src : sectionElements) {
                        updateFile(src.toString(), path, pageName + "_component_" + count, "component", partnerName);
                        count++;
                    }
                }

                updateFile(sectionElements.toString(), path, pageName, "html", partnerName);
                updateFile(jsonString, path, pageName, "json", partnerName);
            }
        } catch (Exception e) {
            log.error("Excepion in fetchHTMLDocument method of HTMLParserServiceImpl :: " + e);
        }
    }
//
//    private void checkDirectory (String fileType, String partnerName) {
//        File dir = new File("");
//        if(fileType == "css")
//            dir = new File("C:\\Users\\prana\\Desktop\\roche\\"+partnerName+"\\css\\");
//        else if(fileType == "js")
//            dir = new File("C:\\Users\\prana\\Desktop\\roche\\"+partnerName+"\\js\\");
//        else if(fileType == "asset")
//            dir = new File("C:\\Users\\prana\\Desktop\\roche\\"+partnerName+"\\assets");
//        else
//            dir = new File("C:\\Users\\prana\\Desktop\\roche\\"+partnerName+"\\");
//
//        if (!dir.exists()){
//            dir.mkdirs();
//        }
//    }

    private void updateFile(String section, String path, String fileName, String fileType, String partnerName) {
        //checkDirectory(fileType, partnerName);
        File file = new File("");
        int indexname = path.lastIndexOf("/");
        String name = path.substring(indexname, path.length());
        try {
            if(fileType == "css")
                file = new File(name);
            else if(fileType == "js")
                file = new File(name);
            else if(fileType == "json")
                file = new File(fileName+".json");
            else {
                file = new File(fileName + ".html");
            }

            if (!file.exists()) {
                file.createNewFile();
                log.info("file created :: {}",file.getPath());
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            // Write in file
            bw.write(String.valueOf(section));

            // Close connection
            bw.close();
            log.info("file written :: {}", file);
            if(fileType == "css")
                s3BucketPushService.pushContentToS3(partnerName, name, "assets/css"+name);
            else if(fileType == "js")
                s3BucketPushService.pushContentToS3(partnerName, name, "assets/js"+name);
            else if(fileType == "json" && fileName != "navigation")
                s3BucketPushService.pushContentToS3(partnerName, fileName+".json", "us/en/nsclc/"+fileName+"/data/"+fileName+".json");
            else if(fileType == "component")
                s3BucketPushService.pushContentToS3(partnerName, fileName+".html", "us/en/nsclc/components/"+fileName+".html");
            else if(fileType == "json" && fileName == "navigation")
                s3BucketPushService.pushContentToS3(partnerName, fileName+".json", "navigation/"+fileName+".json");
            else if(fileType == "html"  && fileName == "navigation")
                s3BucketPushService.pushContentToS3(partnerName, fileName+".html", "navigation/"+fileName+".html");
            else
                s3BucketPushService.pushContentToS3(partnerName, fileName+".html", "us/en/nsclc/"+fileName+"/view/"+fileName+".html");
            boolean fileDeleted = file.delete();
            log.info("fileDeleted :: {}", fileDeleted);

        } catch (Exception e) {
        log.error("Excepion in updateFile method of HTMLParserServiceImpl :: " + e);
    }
    }

    private void getFiles(String src, List<String> tagNames) throws IOException {

        String folder = null;

        //Exctract the name of the image from the src attribute
        int indexname = src.lastIndexOf("/");

        if (indexname == src.length()) {
            src = src.substring(1, indexname);
        }
        indexname = src.lastIndexOf("/");
        String name = src.substring(indexname, src.length());

        //Open a URL Stream
        URL url = new URL(src);
        InputStream in = url.openStream();

        OutputStream out = new BufferedOutputStream(new FileOutputStream(name));

        for (int b; (b = in.read()) != -1;) {
            out.write(b);
        }
        out.close();
        in.close();

        for (String partnerName : tagNames) {
            s3BucketPushService.pushContentToS3(partnerName, name, "assets/images"+name);
        }

    }

    private String sendRequest(ResourceResolver resourceResolver, String resource, String resourceType) throws IOException, ServletException {
        String string= "";
        String requestPath = "";
        if(resourceType == "json")
             requestPath = resource+".infinity.json";
            else if(resourceType == "html")
          requestPath = resource+".html";
            else
                requestPath = resource;

        try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                HttpServletRequest request = requestResponseFactory.createRequest("GET", requestPath);
                request.setAttribute(WCMMode.REQUEST_ATTRIBUTE_NAME, WCMMode.DISABLED);

                HttpServletResponse response = requestResponseFactory.createResponse(out);

                requestProcessor.processRequest(request, response, resourceResolver);
                string = out.toString(response.getCharacterEncoding());


        } catch (IOException e) {
            log.error("IOException in updateFile method of HTMLParserServiceImpl :: " + e);
        } catch (ServletException e) {
            log.error("ServletException in updateFile method of HTMLParserServiceImpl :: " + e);
        } catch (Exception e) {
            log.error("Excepion in updateFile method of HTMLParserServiceImpl :: " + e);
        }

        return string;

    }

//    private String fetchJson(ResourceResolver resourceResolver, String resource, String resourceType) throws IOException, ServletException, JSONException {
//        Resource componentResource = resourceResolver.getResource("/content/roche-partners/naviagtion/jcr:content/root/container/container/list");
//
//
//        ModelFactory modelFactory = null;
//         com.adobe.cq.wcm.core.components.models.List model= modelFactory.createModel(componentResource, com.adobe.cq.wcm.core.components.models.List.class);
//
//
//        JSONObject completeObj = new JSONObject();
//
//        completeObj.put("title", model.getId());
//
//        Map<String, ComponentExporter> componentMap = (Map<String, ComponentExporter>) model.getData();
//
//        Set<String> childKeyList = model.getData().keySet();
//
//        JSONArray keys = new JSONArray();
//
//        for (String childKey : childKeyList) {
//
//            keys.put(((com.adobe.cq.wcm.core.components.models.List ) componentMap.get(childKey)).getElements());
//
//        }
//
//        completeObj.put("items", keys);
//
//        String modelJson = completeObj.toString();
//
//
//        return modelJson;
//    }
}