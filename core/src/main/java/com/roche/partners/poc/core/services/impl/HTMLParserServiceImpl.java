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

            String htmlString = sendRequest(resourceResolver,activatedPage,"html");
            String fileType = "listing-html";

            Document document = Jsoup.parse(htmlString, "UTF-8");
            Elements sectionElements = document.getElementsByTag("section");
//            if(!activatedPage.equalsIgnoreCase("/content/roche-partners/us/en/roche-naviagtion")){
//                checkDirectory("");
//                fileType = "detail-html";
//            }
            for (String partnerName : tagNames) {
                checkDirectory("",partnerName);
            }

                String path= "";
            for (Element src : sectionElements.select("[src]")) {
                if (src.normalName().equals("img")) {
                    String absUrl= "http://localhost:4503"+src.attr("src");
                    path = src.attr("src").replaceAll("/content/dam/roche-partners", "http://127.0.0.1:5500/articles/assets");
                    src.attr("src", path);
                    getFiles(absUrl, tagNames);
                } else if (src.normalName().equals("script")){
                    String jsString = sendRequest(resourceResolver,src.attr("src"),"");
                    path = src.attr("src").replaceAll("/etc.clientlibs/roche-partners/clientlibs", "http://127.0.0.1:5500/articles/js");
                    src.attr("src", path);
                    for (String name : tagNames) {
                        if (name != null){
                            updateFile(jsString,path, pageName, "js", name);
                        }
                    }
                }
            }
            for (Element src : sectionElements.select("[href]")) {
                String cssString = sendRequest(resourceResolver,src.attr("href"),"");
                if (src.normalName().equals("link")){
                    path= src.attr("href").replaceAll("/etc.clientlibs/roche-partners/clientlibs","http://127.0.0.1:5500/articles/css");
                    src.attr("href", path);
                    for (String name : tagNames) {
                        if (name != null){
                            updateFile(cssString,path, pageName, "css", name);
                        }
                    }

                }else if (src.normalName().equals("a")){
                    if(src.toString().contains("/content/roche-partners/us/en/roche-naviagtion/"))
                        path= src.attr("href").replaceAll("/content/roche-partners/us/en/roche-naviagtion/","articles/");
                        src.attr("href", path);
                }

            }
            int count = 0;
            for (Element src : sectionElements) {
                    for (String name : tagNames) {
                        if (name != null){
                            updateFile(src.toString(),path, pageName+"_component_"+count, fileType, name);
                        }
                    }
                    count++;
            }
            log.info("sectionElements :: {}",sectionElements);

            for (String name : tagNames) {
                if (name != null){
                    updateFile(sectionElements.toString(),path, pageName, fileType, name);
                    s3BucketPushService.pushContentToS3(name);
                }
            }
        } catch (Exception e) {
            log.error("Excepion in fetchHTMLDocument method of HTMLParserServiceImpl :: " + e);
        }
    }

    private void checkDirectory (String fileType, String partnerName) {
        File dir = new File("");
        if(fileType == "css")
            dir = new File("C:\\Users\\prana\\Desktop\\roche\\"+partnerName+"\\css\\");
        else if(fileType == "js")
            dir = new File("C:\\Users\\prana\\Desktop\\roche\\"+partnerName+"\\js\\");
        else if(fileType == "asset")
            dir = new File("C:\\Users\\prana\\Desktop\\roche\\"+partnerName+"\\assets");
        else
            dir = new File("C:\\Users\\prana\\Desktop\\roche\\"+partnerName+"\\");

        if (!dir.exists()){
            dir.mkdirs();
        }
    }

    private void updateFile(String section, String path, String fileName, String fileType, String partnerName) {
        checkDirectory(fileType, partnerName);
        File file = new File("");
        int indexname = path.lastIndexOf("/");
        String name = path.substring(indexname, path.length());
        try {
            if(fileType == "css")
                file = new File("C:\\Users\\prana\\Desktop\\roche\\"+partnerName+"\\css\\"+name);
            else if(fileType == "js")
                file = new File("C:\\Users\\prana\\Desktop\\roche\\"+partnerName+"\\js\\"+name);
//            else if(fileType == "listing-html")
//                 file = new File("C:\\Users\\prana\\Desktop\\roche\\"+fileName+".html");
            else
                 file = new File("C:\\Users\\prana\\Desktop\\roche\\"+partnerName+"\\"+fileName+".html");


            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            // Write in file
            bw.write(String.valueOf(section));

            // Close connection
            bw.close();

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

        for (String partnerName : tagNames) {
            if (partnerName != null){
                checkDirectory("asset", partnerName);
            }
        }
        OutputStream out = new BufferedOutputStream(new FileOutputStream( "C:\\Users\\prana\\Desktop\\roche\\articles\\assets\\"+ name));

        for (int b; (b = in.read()) != -1;) {
            out.write(b);
        }
        out.close();
        in.close();
    }

    private String sendRequest(ResourceResolver resourceResolver, String resource, String resourceType) throws IOException, ServletException {
        String string= "";
        String requestPath;
        if(resourceType != "html")
             requestPath = resource;
            else
          requestPath = resource+".html";

        HttpServletRequest request = requestResponseFactory.createRequest("GET", requestPath);
        request.setAttribute(WCMMode.REQUEST_ATTRIBUTE_NAME, WCMMode.DISABLED);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpServletResponse response = requestResponseFactory.createResponse(out);

        requestProcessor.processRequest(request, response, resourceResolver);
        string=  out.toString(response.getCharacterEncoding());

        out.close();

        return string;

    }
}