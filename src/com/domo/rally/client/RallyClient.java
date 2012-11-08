/**
 * RallyClient.java
 */
package com.domo.rally.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * RallyClient 
 * 
 * @author Richard Hogue
 * @version 1.0
 */
public class RallyClient
{
    public final static String  PROPERTY_QUERY_RESULT = "QueryResult";
    public final static String  PROPERTY_TOTAL_RESULT_COUNT = "TotalResultCount";
    public final static String  PROPERTY_RESULTS = "Results";
    public final static String  PROPERTY_REF = "_ref";
    
    public final static String  GET = "GET";
    
    private String          host;
    private String          version;
    private String          username;
    private String          password;

    /**
     * Default constructor
     * @param host 
     * @param version 
     * @param username the rally service user name
     * @param password the rally service password
     */
    public RallyClient(final String host, final String version, final String username, final String password)
    {
        this.host = host;
        this.version = version;
        this.username = username;
        this.password = password;
    }
    
    /**
     * Gets a report of objects based on the type
     * @param objectType te object type to retrieve
     * @return the list of objects and their name/value pairs
     * @throws MalformedURLException
     * @throws JSONException
     * @throws IOException
     * @throws AuthenticationException
     * @throws URISyntaxException
     */
    public List<OrderedMap> getReport(final String objectType) throws MalformedURLException, JSONException, IOException, AuthenticationException, URISyntaxException
    {
        return getReport(null, objectType, null);  
    }
    
    /**
     * Gets a report of objects based on the type and query for the default workspace
     * @param objectType te object type to retrieve
     * @param query the query to apply to the object type
     * @return the list of objects and their name/value pairs
     * @throws MalformedURLException
     * @throws JSONException
     * @throws IOException
     * @throws AuthenticationException
     * @throws URISyntaxException
     */
    public List<OrderedMap> getReport(final String objectType, final String query) throws MalformedURLException, JSONException, IOException, AuthenticationException, URISyntaxException
    {
        return getReport(null, objectType, query);  
    }
    
    /**
     * Gets a report of objects based on the type and query
     * @param workspace the workspace to run the query in
     * @param objectType te object type to retrieve
     * @param query the query to apply to the object type
     * @return the list of objects and their name/value pairs
     * @throws MalformedURLException
     * @throws JSONException
     * @throws IOException
     * @throws AuthenticationException
     * @throws URISyntaxException
     */
    public List<OrderedMap> getReport(final String workspace, final String objectType, final String query) throws MalformedURLException, JSONException, IOException, AuthenticationException, URISyntaxException
    {
        List<OrderedMap>    objects = new ArrayList<OrderedMap>();
        Integer             start = 1;
        Integer             pageSize = 50;
        Integer             totalCount = getReportTotalCount(workspace, objectType, query);
        Integer             count = 0;
        
        while (count < totalCount)
        {
            List<OrderedMap>    pageObjects = getReport(workspace, objectType, query, start, pageSize);
            
            objects.addAll(pageObjects);
            
            count += pageObjects.size();
            start = count + 1;
        }
        
        return objects;  
    }
    
    /**
     * Gets a report of objects based on the type and query
     * @param workspace the workspace to run the query in
     * @param objectType te object type to retrieve
     * @param query the query to apply to the object type
     * @param start the first object index to return
     * @param pageSize the number of objects to return
     * @return the list of objects and their name/value pairs
     * @throws MalformedURLException
     * @throws JSONException
     * @throws IOException
     * @throws AuthenticationException
     * @throws URISyntaxException
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<OrderedMap> getReport(final String workspace, final String objectType, final String query, final Integer start, final Integer pageSize) throws MalformedURLException, JSONException, IOException, AuthenticationException, URISyntaxException
    {
        List<OrderedMap>    objects = new ArrayList<OrderedMap>();
        
        JSONObject  jsonDoc = getJSONDocument(createURI(workspace, objectType, query, start, pageSize));
        JSONObject  jsonQueryResult = jsonDoc.getJSONObject(PROPERTY_QUERY_RESULT);
        
        JSONArray   jsonResults = jsonQueryResult.getJSONArray(PROPERTY_RESULTS);
        
        for (int index = 0; index < jsonResults.length(); index++)
        {
            JSONObject  jsonResult = jsonResults.getJSONObject(index);
            Iterator    keys = jsonResult.keys();
            OrderedMap  propertyMap = new ListOrderedMap();
            
            while (keys.hasNext())
            {
                String  key = (String)keys.next();
                
                if (jsonResult.get(key) instanceof JSONObject)
                {
                    JSONObject  jsonObject = jsonResult.getJSONObject(key);
                     
                    if (jsonObject.has(PROPERTY_REF))
                    {
                        String  ref = jsonObject.getString(PROPERTY_REF);
                        
                        ref = ref.substring(ref.lastIndexOf('/') + 1, ref.lastIndexOf(".js"));
                        
                        propertyMap.put(key, ref);
                    }
                }
                else if (jsonResult.get(key) instanceof JSONArray)
                {
                    JSONArray       jsonArray = jsonResult.getJSONArray(key);
                    List<String>    refs = new ArrayList<String>();
                    
                    for (int arrayIndex = 0; arrayIndex < jsonArray.length(); arrayIndex++)
                    {
                        JSONObject  jsonArrayObj = jsonArray.getJSONObject(arrayIndex);
                        
                        if (jsonArrayObj.has(PROPERTY_REF))
                        {
                            String  ref = jsonArrayObj.getString(PROPERTY_REF);
                            
                            refs.add(ref.substring(ref.lastIndexOf('/') + 1, ref.lastIndexOf(".js")));
                        }                        
                    }
                    
                    propertyMap.put(key, refs);
                }
                else if (jsonResult.get(key) == null || jsonResult.isNull(key))
                {
                        propertyMap.put(key, "");
                }
                else
                {
                    propertyMap.put(key, (jsonResult.get(key).toString().length() > 255) ? jsonResult.get(key).toString().substring(0, 255) : jsonResult.get(key).toString());
                }
            }
            
            objects.add(propertyMap);
        }
        
        return objects;  
    }
    /**
     * Gets the total number of objects for a particular report
     * @param workspace the workspace to run the query in
     * @param objectType the object type to retrieve
     * @param query the query to apply to the object type
     * @return the report total count
     * @throws MalformedURLException
     * @throws JSONException
     * @throws IOException
     * @throws AuthenticationException
     * @throws URISyntaxException
     */
    public Integer getReportTotalCount(final String workspace, final String objectType, final String query) throws MalformedURLException, JSONException, IOException, AuthenticationException, URISyntaxException
    {
        JSONObject  jsonDoc = getJSONDocument(createURI(workspace, objectType, query, 1, 1));
        JSONObject  jsonQueryResult = jsonDoc.getJSONObject(PROPERTY_QUERY_RESULT);
            
        return  jsonQueryResult.getInt(PROPERTY_TOTAL_RESULT_COUNT);
    }
    
    /**
     * Creates the url to get the requested information
     * @param method the method which defines the information to request
     * @param restData the JSON string of parameter data
     * @return the request URL object
     * @throws JSONException
     * @throws MalformedURLException
     * @throws URISyntaxException 
     */
    private URI createURI(final String workspace, final String objectType, final String query, final Integer start, final Integer pageSize) throws JSONException, MalformedURLException, URISyntaxException
    {
        StringBuffer    stringBuffer = new StringBuffer();
                
        stringBuffer.append("https://");
        stringBuffer.append(host);
        stringBuffer.append("/slm/webservice/");
        stringBuffer.append(version);
        stringBuffer.append("/");
        stringBuffer.append(objectType);
        stringBuffer.append(".js?workspace=");
        
        if (!StringUtils.isEmpty(workspace))
        {
            stringBuffer.append(workspace);            
        }
        
        stringBuffer.append("&query=");
        
        if (!StringUtils.isEmpty(workspace))
        {
            stringBuffer.append(query);
        }
        
        stringBuffer.append("&start=");
        stringBuffer.append(start);
        stringBuffer.append("&pagesize=");
        stringBuffer.append(pageSize);
        stringBuffer.append("&fetch=true");

        return new URI(stringBuffer.toString());
    }
        
    /**
     * @param url
     * @return
     * @throws JSONException
     * @throws IOException
     * @throws AuthenticationException 
     */
    private JSONObject getJSONDocument(final URI uri) throws JSONException, IOException, AuthenticationException
    {
        return new JSONObject(getDocument(uri));
    }
    
    /**
     * Gets the document data associated with the specified URL
     * @param url the URL of the document data
     * @return the data retrieved from the URL
     * @throws IOException if unable to connect and retrieve data
     * @throws AuthenticationException 
     */
    private String getDocument(final URI uri) throws IOException, AuthenticationException
    {
        DefaultHttpClient   httpClient = new DefaultHttpClient();
        HttpGet             httpGet = new HttpGet(uri);

        httpClient.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, "Basic"), new UsernamePasswordCredentials(username, password));
        httpGet.addHeader(new BasicScheme().authenticate(new UsernamePasswordCredentials(username, password), httpGet));
        
        HttpResponse    httpResponse = httpClient.execute(httpGet);
        HttpEntity      httpEntity = httpResponse.getEntity();
        
        if (httpEntity != null)
        {
            try
            {
                String line;
                StringBuilder builder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpEntity.getContent()));
                
                while ((line = reader.readLine()) != null)
                {
                    if (line.startsWith("throw"))
                    {
                        continue;
                    }
                    
                    builder.append(line);
                }

                return builder.toString();
            }
            finally
            {
                httpEntity.getContent().close();
                httpClient.getConnectionManager().shutdown();
            }
        }
        
        return null;
    }
}
