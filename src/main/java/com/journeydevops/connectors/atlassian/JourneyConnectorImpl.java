package com.journeydevops.connectors.atlassian;

import com.atlassian.sal.api.message.I18nResolver;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.application.jira.JiraApplicationType;

import com.atlassian.applinks.spi.application.ApplicationIdUtil;
import com.atlassian.applinks.spi.util.TypeAccessor;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.applinks.spi.link.ApplicationLinkDetails;

import com.atlassian.oauth.serviceprovider.ServiceProviderConsumerStore;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.util.RSAKeys;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.event.api.EventListener;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.event.events.PluginModuleDisabledEvent;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

@ExportAsService({ JourneyConnector.class })
@Component
public class JourneyConnectorImpl implements JourneyConnector, InitializingBean, DisposableBean
{

    private EventPublisher eventPublisher;

    private MutatingApplicationLinkService applicationLinkService;

    private TypeAccessor typeAccessor;

    private ServiceProviderConsumerStore serviceProviderConsumerStore;

    private I18nResolver i18nResolver;

    private static final String APPLINK_NAME = "applinks-journey-plugin.application.link.name";
    private static final String APPLINK_URL = "applinks-journey-plugin.application.link.url";
    private static final String CONSUMER_DESCRIPTION = "applinks-journey-plugin.consumer.description";
    private static final String CONSUMER_NAME = "applinks-journey-plugin.consumer.name";
    private static final String CONSUMER_KEY = "applinks-journey-plugin.consumer.key";
    private static final String CONSUMER_PUBLIC_KEY = "applinks-journey-plugin.consumer.public.key";
    private static final String CONSUMER_CALLBACK_URL = "applinks-journey-plugin.consumer.callback.url";

    @Autowired
    public JourneyConnectorImpl(@ComponentImport EventPublisher eventPublisher,
    @ComponentImport MutatingApplicationLinkService applicationLinkService,
    @ComponentImport TypeAccessor typeAccessor,
    @ComponentImport ServiceProviderConsumerStore serviceProviderConsumerStore,
    @ComponentImport I18nResolver i18nResolver)
    {
        this.eventPublisher = eventPublisher;
        this.applicationLinkService = applicationLinkService;
        this.typeAccessor = typeAccessor;
        this.serviceProviderConsumerStore = serviceProviderConsumerStore;
        this.i18nResolver = i18nResolver;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        eventPublisher.register(this);
    }

    @Override
    public void destroy() throws Exception {
        eventPublisher.unregister(this);
    }

    @EventListener
    public void onPluginEvent(@ComponentImport PluginEnabledEvent pluginEnabledEvent) throws InvalidKeySpecException, NoSuchAlgorithmException {
        if (!isApplicationLinkInstalled()) {
            installApplicationLink();
        }
    }

    @EventListener
    public void onPluginEvent(@ComponentImport PluginModuleDisabledEvent pluginModuleDisabledEvent) throws InvalidKeySpecException {
        if (isApplicationLinkInstalled()) {
            uninstallApplicationLink();
        }
    }

    private boolean isApplicationLinkInstalled() {
        URI linkUri = URI.create(i18nResolver.getText(APPLINK_URL));

        for (ApplicationLink applicationLink : applicationLinkService.getApplicationLinks()) {
            if (applicationLink.getDisplayUrl().equals(linkUri)) {
                return true;
            }
        }

        return false;
    }

    private void installApplicationLink() throws InvalidKeySpecException, NoSuchAlgorithmException {
        String linkName = i18nResolver.getText(APPLINK_NAME);
        URI linkUri = URI.create(i18nResolver.getText(APPLINK_URL));

        ApplicationId applicationId = ApplicationIdUtil.generate(linkUri);
        ApplicationType type = findGenericApplicationType();
        ApplicationLinkDetails details = ApplicationLinkDetails.builder().name(linkName).displayUrl(linkUri).build();

        ApplicationLink link = applicationLinkService.addApplicationLink(applicationId, type, details);

        String consumerName = i18nResolver.getText(CONSUMER_NAME);
        String consumerDescription = i18nResolver.getText(CONSUMER_DESCRIPTION);
        String consumerKey = i18nResolver.getText(CONSUMER_KEY);
        String consumerPublicKey = i18nResolver.getText(CONSUMER_PUBLIC_KEY);
        URI consumerCallbackUri = URI.create(i18nResolver.getText(CONSUMER_CALLBACK_URL));

        PublicKey publicKey = RSAKeys.fromPemEncodingToPublicKey(consumerPublicKey);
        Consumer consumer = Consumer.key(consumerKey).name(consumerName).description(consumerDescription).publicKey(publicKey).callback(consumerCallbackUri).build();
        serviceProviderConsumerStore.put(consumer);
        link.putProperty("oauth.incoming.consumerkey" , consumer.getKey());
    }

    private void uninstallApplicationLink() throws InvalidKeySpecException {
        String consumerKey = i18nResolver.getText(CONSUMER_KEY);
        URI linkUri = URI.create(i18nResolver.getText(APPLINK_URL));

        serviceProviderConsumerStore.remove(consumerKey);

        for (ApplicationLink applicationLink : applicationLinkService.getApplicationLinks()) {
            if (applicationLink.getDisplayUrl().equals(linkUri)) {
                applicationLinkService.deleteApplicationLink(applicationLink);
                break;
            }
        }
    }

    private ApplicationType findGenericApplicationType() {
        ApplicationType applicationType = typeAccessor.getApplicationType(JiraApplicationType.class);

        for (ApplicationType enabledApplicationType : typeAccessor.getEnabledApplicationTypes()) {
            if (enabledApplicationType.getClass().getName().contains("GenericApplicationType")) {
                applicationType = enabledApplicationType;
                break;
            }
        }

        return applicationType;
    }
  
}