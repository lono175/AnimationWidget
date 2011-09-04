package AnimationWidget.com;

import java.net.URL;

class Article extends Object implements java.lang.Cloneable{
	public long articleId;
	public long feedId;
	public String title;
	public URL url;
	public Article clone()
	{
		Article res = new Article();
		res.articleId = articleId;
		res.feedId = feedId;
		res.title = title;
		res.url = url;
		return res;
	}
	
}

