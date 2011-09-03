package AnimationWidget.com;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class AnimationWidget extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		Toast.makeText(context, "onUpdate", Toast.LENGTH_SHORT).show();
		if (appWidgetIds == null) {
			appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(
					context, AnimationWidget.class));
		}
 		AnimationService.requestWidgetStart(appWidgetIds);

		Intent intent = new Intent(context, AnimationService.class).setAction(AnimationService.ANIMATION_WIDGET_START);
		context.startService(intent);
	}

    /**
     * @brief called when all widgets are removed
     */
	@Override
	public void onDisabled(Context context) {
		Toast.makeText(context, "onDisabled", Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(context, AnimationService.class).setAction(AnimationService.ANIMATION_WIDGET_DELETED);
		context.startService(intent);
	}

	@Override
	public void onEnabled(Context context) {
		Toast.makeText(context, "onEnabled", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		
		// v1.5 fix that doesn't call onDelete Action
		final String action = intent.getAction();
		if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
			final int appWidgetId = intent.getExtras().getInt(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
			if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				this.onDeleted(context, new int[] { appWidgetId });
			}
		} else {
			super.onReceive(context, intent);
		}
	}
	
    /**
     * @brief called when one widget is removed
     */
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		Toast.makeText(context, "onDelete", Toast.LENGTH_SHORT).show();
		super.onDeleted(context, appWidgetIds);
	}

}
