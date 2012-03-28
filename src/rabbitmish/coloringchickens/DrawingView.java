/* -*- mode:java; coding:utf-8; -*- Time-stamp: <DrawingView.java - root> */

package rabbitmish.coloringchickens;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.*;
import android.widget.ImageView;
import java.util.HashMap;

public class DrawingView extends ImageView
{
    private Bitmap _overlay_bitmap;
    private Canvas _overlay_canvas;
    private final Paint _paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final HashMap<Integer, PointF> _points = new HashMap<Integer, PointF>();

    private boolean eraseToPointer(MotionEvent e, int index, boolean remove)
    {
        final int pid = e.getPointerId(index);
        final PointF point = new PointF(e.getX(index), e.getY(index));
        final PointF prev = remove ? _points.remove(pid) : _points.put(pid, point);

        if (prev == null)
        {
            return false;
        }
        _overlay_canvas.drawLine(prev.x, prev.y, point.x, point.y, _paint);
        return true;
    }

    private void init()
    {
        _paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        _paint.setColor(Color.TRANSPARENT);
        _paint.setMaskFilter(new BlurMaskFilter(15, BlurMaskFilter.Blur.NORMAL));
        _paint.setStrokeCap(Paint.Cap.ROUND);
        _paint.setStrokeJoin(Paint.Join.ROUND);
        _paint.setStrokeWidth(30);
    }

    public DrawingView(Context context)
    {
        super(context);
        init();
    }

    public DrawingView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public DrawingView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        if (_overlay_bitmap == null)
        {
            final Drawable drawable = getDrawable();
            final Rect r = drawable.getBounds();

            _overlay_bitmap = Bitmap.createBitmap(r.right, r.bottom, Bitmap.Config.ARGB_8888);
            _overlay_canvas = new Canvas(_overlay_bitmap);

            super.onDraw(_overlay_canvas);
        }
        canvas.drawBitmap(_overlay_bitmap, 0, 0, null);
    }

    @Override
    public void onSizeChanged(int w, int h, int old_w, int old_h)
    {
        super.onSizeChanged(w, h, old_w, old_h);

        _overlay_bitmap = null;
        _overlay_canvas = null;
        _points.clear();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        if (_overlay_canvas == null)
        {
            return true;
        }
        switch (e.getActionMasked())
        {
        case MotionEvent.ACTION_DOWN:
            {
                final int pid = e.getPointerId(0);
                final PointF point = new PointF(e.getX(0), e.getY(0));
                _points.clear();
                _points.put(pid, point);
            }
            break;

        case MotionEvent.ACTION_POINTER_DOWN:
            {
                final int index = e.getActionIndex();
                final int pid = e.getPointerId(index);
                final PointF point = new PointF(e.getX(index), e.getY(index));
                _points.put(pid, point);
            }
            break;

        case MotionEvent.ACTION_MOVE:
            {
                final int count = e.getPointerCount();
                boolean erased = false;

                for (int i = 0; i < count; i++)
                {
                    erased = eraseToPointer(e, i, false) || erased;
                }
                if (erased)
                {
                    invalidate();
                }
            }
            break;

        case MotionEvent.ACTION_POINTER_UP:
            if (eraseToPointer(e, e.getActionIndex(), true))
            {
                invalidate();
            }
            break;

        case MotionEvent.ACTION_UP:
            if (eraseToPointer(e, 0, true))
            {
                invalidate();
            }
            // fall through

        case MotionEvent.ACTION_CANCEL:
            _points.clear();
            break;
        }
        return true;
    }
}
