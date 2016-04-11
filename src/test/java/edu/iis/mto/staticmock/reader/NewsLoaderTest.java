package edu.iis.mto.staticmock.reader;

import edu.iis.mto.staticmock.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ConfigurationLoader.class, NewsReaderFactory.class, PublishableNews.class})
public class NewsLoaderTest {
    private static final String INCOMING_NEWS_SUBBED = "sub";
    private static final String INCOMING_NEWS_NONE = "none";
    private static final String INCOMING_NEWS_MIXED = "mixed";

    private NewsLoader newsLoader;

    private PublishableNews publishableNewsMock;

    private Configuration configurationMock;

    private IncomingNews incomingNewsWithSub, incomingNewsWithoutSub, incomingNewsMixed;

    private NewsReader newsReaderMock;

    @Before
    public void setUp() {
        newsLoader = new NewsLoader();

        IncomingInfo incomingInfoSubscription =
                new IncomingInfo("Req sub", SubsciptionType.A);
        IncomingInfo incomingInfoWithoutSubscription =
                new IncomingInfo("Doesn't req sub", SubsciptionType.NONE);

        incomingNewsWithSub = new IncomingNews();
        incomingNewsWithSub.add(incomingInfoSubscription);

        incomingNewsWithoutSub = new IncomingNews();
        incomingNewsWithoutSub.add(incomingInfoWithoutSubscription);

        incomingNewsMixed = new IncomingNews();
        incomingNewsMixed.add(incomingInfoSubscription);
        incomingNewsMixed.add(incomingInfoWithoutSubscription);

        newsReaderMock = mock(NewsReader.class);

        configurationMock = mock(Configuration.class);
        when(configurationMock.getReaderType()).thenReturn("");

        mockStatic(ConfigurationLoader.class);
        ConfigurationLoader configurationLoaderMock = mock(ConfigurationLoader.class);
        when(ConfigurationLoader.getInstance()).thenReturn(configurationLoaderMock);
        when(configurationLoaderMock.loadConfiguration()).thenReturn(configurationMock);

        mockStatic(NewsReaderFactory.class);
        when(NewsReaderFactory.getReader(anyString())).thenReturn(newsReaderMock);

        publishableNewsMock = new PublishableNews();
        mockStatic(PublishableNews.class);
        when(PublishableNews.create()).thenReturn(publishableNewsMock);
    }

    @Test
    public void publishableNewsShouldContainSubscribedContent() {
        setIncomingNews(INCOMING_NEWS_SUBBED);

        String newsContent = incomingNewsWithSub.elems().get(0).getContent();

        PublishableNews publishableNews = newsLoader.loadNews();

        String publishableNewsContent = null;

        if (publishableNews.getSubscribentContent().size() > 0) {
            publishableNewsContent = publishableNews.getSubscribentContent().get(0);
        }

        assertThat(newsContent, is(publishableNewsContent));
    }

    @Test
    public void publishableNewsShouldContainPublicContent() {
        setIncomingNews(INCOMING_NEWS_NONE);

        String newsContent = incomingNewsWithoutSub.elems().get(0).getContent();

        PublishableNews publishableNews = newsLoader.loadNews();

        String publishableNewsContent = null;

        if (publishableNews.getPublicContent().size() > 0) {
            publishableNewsContent = publishableNews.getPublicContent().get(0);
        }

        assertThat(newsContent, is(publishableNewsContent));
    }

    private void setIncomingNews(String type) {
        switch (type) {
            case INCOMING_NEWS_SUBBED:
                when(newsReaderMock.read()).thenReturn(incomingNewsWithSub);
                break;
            case INCOMING_NEWS_NONE:
                when(newsReaderMock.read()).thenReturn(incomingNewsWithoutSub);
                break;
            case INCOMING_NEWS_MIXED:
                when(newsReaderMock.read()).thenReturn(incomingNewsMixed);
                break;
        }
    }
}
