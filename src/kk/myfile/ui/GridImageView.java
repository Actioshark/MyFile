package kk.myfile.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import kk.myfile.R;

public class GridImageView extends ImageView {
	public static enum SizeType {
		Default, FitWidth, FitHeight,
	}
	
	private SizeType mSizeType = SizeType.Default;
	
	public GridImageView(Context context) {
		super(context);
	}

	public GridImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}
	
	public GridImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}
	
	private void init(AttributeSet attrs) {
		TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.myfile);
		mSizeType = SizeType.values()[ta.getInteger(R.styleable.myfile_size_type, mSizeType.ordinal())];
		ta.recycle();
	}
	
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();
		
		if (mSizeType == SizeType.FitWidth) {
			height = width;
		} else if (mSizeType == SizeType.FitHeight) {
			width = height;
		}
		
		setMeasuredDimension(width, height);
	}
}
