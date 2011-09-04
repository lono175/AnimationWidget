package AnimationWidget.com;

import java.net.URL;

class Feed extends Object implements java.lang.Cloneable{
    public long feedId;
    public String title;
    public URL url;
	public Feed clone()
	{
		Feed res = new Feed();
		res.feedId = feedId;
		res.title = title;		
		res.url = url;
		return res;
	}
		
}
