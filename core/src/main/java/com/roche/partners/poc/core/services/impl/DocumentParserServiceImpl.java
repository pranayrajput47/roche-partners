package com.roche.partners.poc.core.services.impl;
import com.day.cq.contentsync.handler.util.RequestResponseFactory;
import com.day.cq.wcm.api.WCMMode;
import com.roche.partners.poc.core.constants.RocheConstants;
import com.roche.partners.poc.core.services.DocumentParserService;
import com.roche.partners.poc.core.services.S3BucketPushService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.engine.SlingRequestProcessor;
import org.apache.sling.models.factory.ModelFactory;
import org.json.JSONException;
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
import java.util.Iterator;
import java.util.List;

@Component(immediate = true, service = DocumentParserService.class, property = {
        Constants.SERVICE_DESCRIPTION + "= Parses HTML using JSOUP"})
@Slf4j
public class DocumentParserServiceImpl implements DocumentParserService {

    @Reference
    private RequestResponseFactory requestResponseFactory;

    @Reference
    private SlingRequestProcessor requestProcessor;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private S3BucketPushService s3BucketPushService;

    @Reference
    ModelFactory modelFactory;


    @Activate
    protected final void activate() {
        log.info("Activated DocumentParserServiceImpl");
    }

    @Deactivate
    protected void deactivate() {
        log.info("Deactivated DocumentParserServiceImpl");
    }

    @Override
    public void fetchHTMLDocument(ResourceResolver resourceResolver, String activatedPage, String pageName,
                                  List<String> tagNames, Iterator<Resource> components) throws ServletException, IOException {
        try {
            log.info("Inside HtmlParser :: {}",activatedPage);

            String htmlString = sendRequest(resourceResolver,activatedPage+"."+ RocheConstants.HTML);
            String jsonString= fetchJson(activatedPage);

            for (String partnerName : tagNames) {
            Document document = Jsoup.parse(htmlString, "UTF-8");
            Elements sectionElements = document.getElementsByTag("section");

                s3BucketPushService.createBucket(partnerName);

                String path = "";
                for (Element src : sectionElements.select("[src]")) {
                    if (src.normalName().equals("img")) {
                        String absUrl = "http://15.207.109.174:4503" + src.attr("src");
                        path = src.attr("src").replaceAll("/content/dam/roche-partners", "https://"+partnerName+".s3.ap-south-1.amazonaws.com/assets/images");
                        src.attr("src", path);
                        getFiles(absUrl, tagNames);
                    } else if (src.normalName().equals("script")) {
                        String jsString = sendRequest(resourceResolver, src.attr("src"));
                        path = src.attr("src").replaceAll("/etc.clientlibs/roche-partners/clientlibs", "https://"+partnerName+".s3.ap-south-1.amazonaws.com/assets/js");
                        src.attr("src", path);

                        updateFile(jsString, path, "js", partnerName);

                    }
                }
                for (Element src : sectionElements.select("[href]")) {
                    if (src.normalName().equals("link")) {
                        String cssString = sendRequest(resourceResolver, src.attr(RocheConstants.HREF));
                        path = src.attr(RocheConstants.HREF).replaceAll("/etc.clientlibs/roche-partners/clientlibs", "https://"+partnerName+".s3.ap-south-1.amazonaws.com/assets/css");
                        src.attr(RocheConstants.HREF, path);

                        updateFile(cssString, path, RocheConstants.CSS, partnerName);

                    } else if (src.normalName().equals("div")) {
                        if (pageName == "navigation") {
                            path = src.attr(RocheConstants.HREF).replaceAll("/content/roche-partners/us/en/nsclc/", "https://"+partnerName+".s3.ap-south-1.amazonaws.com/us/en/nsclc/about-the-disease/view/");
                            src.attr(RocheConstants.HREF, path);
                        }
                    }

                }
                path=activatedPage.replaceAll("/content/roche-partners/","");
                if(pageName!= "navigation") {
                    int count = 0;
                    for (Element src : sectionElements) {
                        updateFile(src.toString(), path + "/components/component-" + count, RocheConstants.HTML, partnerName);
                        count++;
                    }
                    count = 0;
                    while(components.hasNext()) {
                        String componentJsonString= "";
                        log.info("children :: {}", components.next().getPath());
                        componentJsonString= fetchJson(components.next().getPath());
                        updateFile(componentJsonString, path + "/components/component-" + count, RocheConstants.JSON, partnerName);
                        count++;
                    }
                }

                updateFile(sectionElements.toString(), path, RocheConstants.HTML, partnerName);
                updateFile(jsonString, path, RocheConstants.JSON, partnerName);
            }
        } catch (Exception e) {
            log.error("Excepion in fetchHTMLDocument method of DocumentParserServiceImpl :: " + e);
        }
    }

    private void updateFile(String section, String path, String fileType, String partnerName) {
        File file;
        String resourcePath= "";
        String fileName= "";
        if(fileType != RocheConstants.CSS && fileType != RocheConstants.JS)
            resourcePath= path+"."+fileType;
        else
            resourcePath= path;

        log.info("Elseif :: {}", path);

        if(!resourcePath.contains("/")) {
            resourcePath= "/"+resourcePath;
        }
            int indexName = resourcePath.lastIndexOf("/");
            fileName = resourcePath.substring(indexName, resourcePath.length());
        try {
             file = new File(fileName);

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
            if(fileType == RocheConstants.CSS || fileType == RocheConstants.JS)
                s3BucketPushService.pushContentToS3(partnerName, fileName, "assets/"+fileType+fileName);
            else if(fileType == RocheConstants.JSON)
                s3BucketPushService.pushContentToS3(partnerName, fileName, path+"/data"+fileName);
            else
                s3BucketPushService.pushContentToS3(partnerName, fileName, path+"/view"+fileName);
            boolean fileDeleted = file.delete();
            log.info("fileDeleted :: {}", fileDeleted);

        } catch (Exception e) {
        log.error("Exception in updateFile method of DocumentParserServiceImpl :: " + e);
    }
    }

    private void getFiles(String src, List<String> tagNames) throws IOException {

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

    private String sendRequest(ResourceResolver resourceResolver, String resource) {
        String responseString= "";

        try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                HttpServletRequest request = requestResponseFactory.createRequest("GET", resource);
                request.setAttribute(WCMMode.REQUEST_ATTRIBUTE_NAME, WCMMode.DISABLED);

                HttpServletResponse response = requestResponseFactory.createResponse(out);

                requestProcessor.processRequest(request, response, resourceResolver);
            responseString = out.toString(response.getCharacterEncoding());

        } catch (IOException e) {
            log.error("IOException in updateFile method of DocumentParserServiceImpl :: " + e);
        } catch (ServletException e) {
            log.error("ServletException in updateFile method of DocumentParserServiceImpl :: " + e);
        } catch (Exception e) {
            log.error("Excepion in updateFile method of DocumentParserServiceImpl :: " + e);
        }

        return responseString;
    }

    private String fetchJson(String resourcePath) throws IOException, ServletException, JSONException {

        String uri= "http://15.207.109.174:4503"+resourcePath+".infinity.json";

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(uri);
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);

        log.info("GET Response Status:: "
                + httpResponse.getStatusLine().getStatusCode());

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                httpResponse.getEntity().getContent()));

        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = reader.readLine()) != null) {
            response.append(inputLine);
        }
        reader.close();

        // print result
        log.info("response :: {}",response.toString());
        httpClient.close();

        return  response.toString();
    }
}