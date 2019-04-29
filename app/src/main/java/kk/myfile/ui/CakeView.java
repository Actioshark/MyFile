package kk.myfile.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CakeView extends View {
	public static class Arc {
		public float ratio;
		public int color;
	}

	public static final int STROKE_WIDTH = 6;

	private final Paint mPaint = new Paint();
	private final RectF mRect = new RectF();
	private final List<Arc> mArcs = new ArrayList<>();

	public CakeView(Context context) {
		super(context);
	}

	public CakeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public CakeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}

	private void init(AttributeSet attrs) {
		mPaint.setAntiAlias(true);
	}

	public void setArcs(List<Arc> arcs) {
		mArcs.clear();
		mArcs.addAll(arcs);
	}

	@Override
	public void onDraw(Canvas canvas) {
		int width = getWidth();
		int height = getHeight();
		int len = Math.min(width, height);

		mRect.left = (width - len + STROKE_WIDTH) / 2f;
		mRect.right = (width + len - STROKE_WIDTH) / 2f;
		mRect.top = (height - len + STROKE_WIDTH) / 2f;
		mRect.bottom = (height + len - STROKE_WIDTH) / 2f;

		mPaint.setColor(0xff000000);
		mPaint.setStyle(Style.STROKE);
		mPaint.setStrokeWidth(STROKE_WIDTH);

		canvas.drawArc(mRect, -90, 360, false, mPaint);

		mRect.left = (width - len) / 2f + STROKE_WIDTH;
		mRect.right = (width + len) / 2f - STROKE_WIDTH;
		mRect.top = (height - len) / 2f + STROKE_WIDTH;
		mRect.bottom = (height + len) / 2f - STROKE_WIDTH;

		mPaint.setStyle(Style.FILL);

		float start = -90f;
		for (Arc arc : mArcs) {
			if (arc.ratio <= 0) {
				continue;
			}
			
			float sweep = 360f * arc.ratio;

			mPaint.setColor(arc.color);

			canvas.drawArc(mRect, start, sweep, true, mPaint);

			start += sweep;
		}
	}
}
