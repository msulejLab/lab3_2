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

        String newsContent = getIncomingNewsContent(incomingNewsWithSub, 0);

        PublishableNews publishableNews = newsLoader.loadNews();

        String publishableNewsContent = getSubscribedContent(publishableNews);

        assertThat(newsContent, is(publishableNewsContent));
    }

    @Test
    public void publishableNewsShouldContainPublicContent() {
        setIncomingNews(INCOMING_NEWS_NONE);

        String newsContent = getIncomingNewsContent(incomingNewsWithoutSub, 0);

        PublishableNews publishableNews = newsLoader.loadNews();

        String publishableNewsContent = getPublicContent(publishableNews);

        assertThat(newsContent, is(publishableNewsContent));
    }

    @Test
    public void publishableNewsShouldContainPublicAndSubscribedContent() {
        setIncomingNews(INCOMING_NEWS_MIXED);

        String newsContentSubbed = getIncomingNewsContent(incomingNewsMixed, 0);
        String newsContentPublic = getIncomingNewsContent(incomingNewsMixed, 1);

        PublishableNews publishableNews = newsLoader.loadNews();

        String publishableNewsContentSubbed = getSubscribedContent(publishableNews);
        String publishableNewsContentPublic = getPublicContent(publishableNews);

        assertThat(publishableNewsContentSubbed, is(newsContentSubbed));
        assertThat(publishableNewsContentPublic, is(newsContentPublic));
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

    private String getPublicContent(PublishableNews publishableNews) {
        if (publishableNews.getPublicContent().size() > 0) {
            return publishableNews.getPublicContent().get(0);
        }

        return null;
    }

    private String getSubscribedContent(PublishableNews publishableNews) {
        if (publishableNews.getSubscribentContent().size() > 0) {
            return publishableNews.getSubscribentContent().get(0);
        }

        return null;
    }

    private String getIncomingNewsContent(IncomingNews incomingNews, int index) {
        if (incomingNews.elems().size() > index) {
            return incomingNews.elems().get(index).getContent();
        }

        return null;
    }
}
