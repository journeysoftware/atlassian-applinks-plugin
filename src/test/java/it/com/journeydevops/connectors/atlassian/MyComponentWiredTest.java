package it.com.journeydevops.connectors.atlassian;

import org.junit.Test;
import org.junit.runner.RunWith;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.journeydevops.connectors.atlassian.JourneyConnector;

import static org.junit.Assert.assertEquals;

@RunWith(AtlassianPluginsTestRunner.class)
public class MyComponentWiredTest
{

    private final JourneyConnector journeyConnector;

    public MyComponentWiredTest(JourneyConnector journeyConnector)
    {
        this.journeyConnector = journeyConnector;
    }

    // @Test
    // public void testMyName()
    // {
    //     assertEquals("names do not match!", "myComponent:" + applicationProperties.getDisplayName(),myPluginComponent.getName());
    // }
}