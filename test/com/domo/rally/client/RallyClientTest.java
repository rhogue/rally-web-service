/**
 * RallyClientTest.java
 */
package com.domo.rally.client;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.collections.OrderedMap;
import org.apache.http.auth.AuthenticationException;
import org.codehaus.jettison.json.JSONException;
import org.junit.Test;

/**
 * RallyClientTest 
 * 
 * @author Richard Hogue
 * @version 1.0
 */
public class RallyClientTest
{
    public static final String HOST = "rally1.rallydev.com";
    public static final String VERSION = "1.39";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    @Test
    public void test()
    {
        fail("Not yet implemented");
    }

    @Test
    public void testGetReport()
    {
        RallyClient rallyClient = new RallyClient(HOST, VERSION, USERNAME, PASSWORD);
        
        try
        {
            List<OrderedMap>  objects = rallyClient.getReport("iteration", "");
            
            assertNotNull(objects);
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (AuthenticationException e)
        {
            e.printStackTrace();
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetReleaseReport()
    {
        RallyClient rallyClient = new RallyClient(HOST, VERSION, USERNAME, PASSWORD);
        
        try
        {
            List<OrderedMap>  objects = rallyClient.getReport("release", "");
            
            assertNotNull(objects);
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (AuthenticationException e)
        {
            e.printStackTrace();
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
    }
}
