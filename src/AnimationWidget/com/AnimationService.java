package AnimationWidget.com;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.RemoteViews;
import android.widget.Toast;

public class AnimationService extends Service {

	private static List<Integer> mqWidgetIdList = new ArrayList<Integer>();

	public static final String ANIMATION_WIDGET_START = "AnimationService.ANIMATION_WIDGET_START";
	public static final String ANIMATION_WIDGET_DELETED = "AnimationService.ANIMATION_WIDGET_DELETED";
	public static final String ANIMATION_WIDGET_UPDATE = "AnimationService.ANIMATION_WIDGET_UPDATE";
	public static final String ANIMATION_WIDGET_SHOW = "AnimationService.ANIMATION_WIDGET_SHOW";

	private int layoutIdx = 0;
	private Handler serviceHandler;
	private static final int HANDLER_MSG_SHOW_ANIMATION = 1;
	private RSSHandler rss;
	private NewsDroidDB droidDB = null; //store all RSS feeds

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		try {
			Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show();
			serviceHandler = new Handler()
			{
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					/*handle UI thread */
					switch (msg.what) {
					case HANDLER_MSG_SHOW_ANIMATION:
						updateWidget(ANIMATION_WIDGET_UPDATE);
						break;
					}
				}
			};
            droidDB = new NewsDroidDB(this, "RSS_DB");
			rss = new RSSHandler();
			rss.createFeed(this, new URL("http://feeds.feedburner.com/cnet/pRza?format=xml"));
			//rss.createFeed(this, new URL("http://tw.rd.yahoo.com/referurl/news/rss2/ind/pol/*http://tw.news.yahoo.com/rss/politics"));

		} catch (Exception ex) {
			DebugLog.log(ex.toString());
			Toast.makeText(getApplicationContext(),
					"start AnimationWidget failed! [" + ex.getMessage() + "]", Toast.LENGTH_LONG)
					.show();
		}

	}

	@Override
	public void onStart(Intent intent, int startId) {
		Toast.makeText(this, "onStart", Toast.LENGTH_SHORT).show();
		super.onStart(intent, startId);
		String s = intent.getAction();
		updateWidget(s);
	}

	@Override
	public void onDestroy() {

		super.onDestroy();
	}


	public static void requestWidgetStart(int[] appWidgetIds) {
		for (int appWidgetId : appWidgetIds) {
			if (!mqWidgetIdList.contains(appWidgetId))
				mqWidgetIdList.add(appWidgetId);
		}
	}

	synchronized private void updateWidget(String action) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		if (ANIMATION_WIDGET_START.equals(action)) {
			makeAnimationWidgetViewSlide(appWidgetManager);
		} else if (ANIMATION_WIDGET_DELETED.equals(action)) {
			mqWidgetIdList.clear();
            if (droidDB != null) {
               droidDB.removeDB(); 
            }
			this.stopSelf();
		}else if(ANIMATION_WIDGET_UPDATE.equals(action)){
			makeAnimationWidgetViewSlide(appWidgetManager);
		}else if(ANIMATION_WIDGET_SHOW.equals(action)){
			serviceHandler.removeMessages(HANDLER_MSG_SHOW_ANIMATION);
			serviceHandler.sendEmptyMessage(HANDLER_MSG_SHOW_ANIMATION);
		}
	}

	private RemoteViews buildWidgetUpdate() {

		RemoteViews RViews;
		
		if(layoutIdx == 0)
		{
			RViews= new RemoteViews(this.getPackageName(),R.layout.layout_a);
			layoutIdx = 1;
		}
		else
		{
			RViews= new RemoteViews(this.getPackageName(),R.layout.layout_b);
			layoutIdx = 0;
		}

		PendingIntent pending = PendingIntent.getService(this, 0, new Intent(this, AnimationService.class)
		.setAction(AnimationService.ANIMATION_WIDGET_SHOW), 0);
		
		RViews.setOnClickPendingIntent(R.id.start, pending);

		return RViews;
	}

	synchronized private void makeAnimationWidgetViewSlide(AppWidgetManager appWidgetManager)
	{
		RemoteViews updateViews = buildWidgetUpdate();
		Bitmap in_bmp,out_bmp;

		out_bmp = makeBitmap(layoutIdx == 0 ? 0 : 1);
		in_bmp = makeBitmap(layoutIdx == 0 ? 1 : 0);


		updateViews.setImageViewBitmap(R.id.Move_InImage, in_bmp); 
		updateViews.setImageViewBitmap(R.id.Move_OutImage, out_bmp);

		for (int appWidgetId : mqWidgetIdList) {
			appWidgetManager.updateAppWidget(appWidgetId, updateViews);
		}				
	}

	private Bitmap makeBitmap(int idx){
		int imageId[]= {R.drawable.robot,R.drawable.star};
		String[] src = {"Hello","World"}; 
		Bitmap bmp;
		
		List<Feed> res = rss.getFeed();
		src[0] = res.get(0).title;
		bmp = Bitmap.createBitmap(270, 60,Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		Paint brush = new Paint(Paint.ANTI_ALIAS_FLAG);
		brush.setStyle(Paint.Style.FILL_AND_STROKE);
		brush.setTextSize(30);
		brush.setColor(idx == 0 ? Color.RED : Color.BLUE);
		canvas.drawText(src[idx], 80 , 40, brush);
		canvas.drawBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getResources(), imageId[idx]), 60, 60, true), 0, 0, null);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();// restore the canvas setting

		return bmp;
	} 
}