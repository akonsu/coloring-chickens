/* -*- mode:java; coding:utf-8; -*- Time-stamp: <DrawingView.java - root> */

package rabbitmish.coloringchickens;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.*;
import android.widget.ImageView;

public class DrawingView extends ImageView
{
    private Bitmap _overlay_bitmap;
    private Canvas _overlay_canvas;
    private Paint _paint = null;

    public DrawingView(Context context)
    {
        super(context);
    }

    public DrawingView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public DrawingView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        if (_overlay_bitmap != null)
        {
            canvas.drawBitmap(_overlay_bitmap, 0, 0, null);
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int old_w, int old_h)
    {
        super.onSizeChanged(w, h, old_w, old_h);

        _overlay_bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        _overlay_canvas = new Canvas(_overlay_bitmap);

        final Drawable drawable = getDrawable();

        //_overlay_bitmap.setHasAlpha(true);
        drawable.setBounds(0, 0, _overlay_canvas.getWidth(), _overlay_canvas.getHeight());
        drawable.draw(_overlay_canvas);

        if (_paint == null)
        {
            _paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            _paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
            _paint.setColor(Color.TRANSPARENT);
            _paint.setMaskFilter(new BlurMaskFilter(15, BlurMaskFilter.Blur.NORMAL));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        final int action = e.getAction();

        switch (action & MotionEvent.ACTION_MASK)
        {
        case MotionEvent.ACTION_MOVE:
            if (_overlay_canvas != null)
            {
                final int count = e.getPointerCount();

                for (int i = 0; i < count; i++)
                {
                    final float x = e.getX(i);
                    final float y = e.getY(i);

                    _overlay_canvas.drawCircle(x, y, 15, _paint);
                    /*
                      final RectF r = new RectF(x, y, x + e.getToolMajor(i), y + e.getToolMinor(i));
                      _overlay_canvas.drawOval(r, _paint);
                    */
                }
                invalidate();
            }
            break;

        case MotionEvent.ACTION_UP:
            break;
        }
        return true;
    }
}
