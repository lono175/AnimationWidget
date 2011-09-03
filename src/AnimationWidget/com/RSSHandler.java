package AnimationWidget.com;

import java.io.IOException;

import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.util.Log;


/**
 * @brief retrieve the rss feed from the Internet 
 */
public class RSSHandler extends DefaultHandler {
	public class SAXParsingDone extends SAXException {}

	// Used to define what elements we are currently in
	private boolean inItem = false;
	private boolean inTitle = false;
	private boolean inLink = false;

	// Feed and Article objects to use for temporary storage
	private Article currentArticle = new Article();
	private Feed currentFeed = new Feed();
    
    //
    private List<Article> articles = new List<Article>();
    private List<Feed> feeds = new List<Feed>();

	// Number of articles added so far
	private int articlesAdded = 0;

	// Number of articles to download
	private static final int ARTICLES_LIMIT = 15;

	// The possible values for targetFlag
	private static final int TARGET_FEED = 0;
	private static final int TARGET_ARTICLES = 1;

	// A flag to know if looking for Articles or Feed name
	private int targetFlag;

	public void startElement(String uri, String name, String qName,
			Attributes atts) {
		DebugLog.log("-->startElement");
		if (name.trim().equals("title"))
			inTitle = true;
		else if (name.trim().equals("item"))
			inItem = true;
		else if (name.trim().equals("link"))
			inLink = true;
		DebugLog.log("<--startElement");
	}

	public void endElement(String uri, String name, String qName)
			throws SAXException {
		if (name.trim().equals("title"))
			inTitle = false;
		else if (name.trim().equals("item"))
			inItem = false;
		else if (name.trim().equals("link"))
			inLink = false;

		// Check if looking for feed, and if feed is complete
		if (targetFlag == TARGET_FEED && currentFeed.url != null
				&& currentFeed.title != null) {

			// We know everything we need to know, so insert feed and exit
            this.feeds.insert(currentFeed.clone());
            //droidDB.insertFeed(currentFeed.title, currentFeed.url);
			throw new SAXParsingDone();
		}

		// Check if looking for article, and if article is complete
		if (targetFlag == TARGET_ARTICLES && currentArticle.url != null
				&& currentArticle.title != null) {
            //droidDB.insertArticle(currentFeed.feedId, currentArticle.title, currentArticle.url);
            this.articles.insert(currentArticle.clone());
			currentArticle.title = null;
			currentArticle.url = null;

			// Lets check if we've hit our limit on number of articles
			articlesAdded++;
			if (articlesAdded >= ARTICLES_LIMIT)
				throw new SAXParsingDone();
		}

	}

	public void characters(char ch[], int start, int length) {

		String chars = (new String(ch).substring(start, start + length));

		try {
			// If not in item, then title/link refers to feed
			if (!inItem) {
				if (inTitle)
					currentFeed.title = chars;
			} else {
				if (inLink)
					currentArticle.url = new URL(chars);
				if (inTitle)
					currentArticle.title = chars;
			}
		} catch (MalformedURLException e) {
			Log.e("NewsDroid", e.toString());
		}

	}

	public List<Feed> getFeed(URL url) {
		try {
			targetFlag = TARGET_FEED;
            //droidDB = new NewsDroidDB(context);
			currentFeed.url = url;

			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			xr.setContentHandler(this);
			InputSource src = new InputSource(url.openStream());

            //TODO: do we have other encoding type
			src.setEncoding("UTF-8");
            //DebugLog.log("Encoding: "+ src.getEncoding());
			
            xr.parse(src);
            return this.feeds;
			
		} catch (IOException e) {
			DebugLog.log(e.toString());
		} catch (SAXParsingDone e) {
		    //find the feed	
		} catch (SAXException e){
            //TODO: handle bad rss feed location
		    DebugLog.log(e.toString());

        } catch (ParserConfigurationException e) {
			DebugLog.log(e.toString());
			
		} catch (Exception e){
			DebugLog.log(e.toString());
		}
		
	}
    //public List<Feed> getFeeds()
    //{
        //return droidDB.getFeeds();

    //}
    //public List<Article> getArticles()
    //{
        //return droidDB.getArticles();
    //}
///*
	public void getArticles(Feed feed) {
		try {
			targetFlag = TARGET_ARTICLES;
			currentFeed = feed;

			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			xr.setContentHandler(this);
			xr.parse(new InputSource(currentFeed.url.openStream()));
			
		} catch (IOException e) {
			Log.e("NewsDroid", e.toString());
        } catch (SAXParsingDone e) {
            //reach the article limit
		} catch (SAXException e) {
			Log.e("NewsDroid", e.toString());
		} catch (ParserConfigurationException e) {
			Log.e("NewsDroid", e.toString());
		} 
	}
//*/
}
