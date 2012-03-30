/* -*- mode:java; coding:utf-8; -*- Time-stamp: <EraseView.java - root> */

package rabbitmish.coloringchickens;

import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

public class EraseView extends ImageView
{
    private final HashMap<Integer, float[]> _coords = new HashMap<Integer, float[]>();
    private Matrix _matrix;
    private final Paint _paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Canvas _canvas;
    private int[] _pixels;

    private boolean eraseToPointer(MotionEvent e, int index, boolean remove)
    {
        final int pid = e.getPointerId(index);
        final float[] coords = getPointerCoords(e, index);
        final float[] coords_prev = remove ? _coords.remove(pid) : _coords.put(pid, coords);

        if (coords_prev == null)
        {
            return false;
        }
        if (_canvas == null)
        {
            final Bitmap bitmap = ((BitmapDrawable)getDrawable()).getBitmap();

            if (bitmap != null)
            {
                _canvas = new Canvas(bitmap);
            }
        }
        if (_canvas != null)
        {
            _canvas.drawLine(coords_prev[0], coords_prev[1], coords[0], coords[1], _paint);
        }
        return true;
    }

    private final float[] getPointerCoords(MotionEvent e, int index)
    {
        final float[] coords = new float[] { e.getX(index), e.getY(index) };

        if (_matrix == null)
        {
            _matrix = new Matrix();
            getImageMatrix().invert(_matrix);
            _matrix.postTranslate(getScrollX(), getScrollY());
        }
        _matrix.mapPoints(coords);
        return coords;
    }

    private void init()
    {
        _paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        _paint.setColor(Color.TRANSPARENT);
        _paint.setStrokeCap(Paint.Cap.ROUND);
        _paint.setStrokeJoin(Paint.Join.ROUND);
        _paint.setStrokeWidth(90);
    }

    private boolean is_erased()
    {
        final Bitmap bitmap = ((BitmapDrawable)getDrawable()).getBitmap();

        if (bitmap == null)
        {
            return true;
        }

        final int AUTO_COMPLETE_PERCENT = 25;
        final int w = bitmap.getWidth();
        final int h = bitmap.getHeight();
        final int length = w * h;

        int count = 0;

        if (_pixels == null || _pixels.length != length)
        {
            _pixels = new int[length];
        }
        bitmap.getPixels(_pixels, 0, w, 0, 0, w, h);

        for (int i = 0; i < length; i++)
        {
            final int alpha = Color.alpha(_pixels[i]);

            if (alpha > 0)
            {
                count++;
            }
        }
        return count * 100 < length * AUTO_COMPLETE_PERCENT;
    }

    public EraseView(Context context)
    {
        super(context);
        init();
    }

    public EraseView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public EraseView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    public void onSizeChanged(int w, int h, int old_w, int old_h)
    {
        super.onSizeChanged(w, h, old_w, old_h);

        _canvas = null;
        _pixels = null;
        _coords.clear();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        switch (e.getActionMasked())
        {
        case MotionEvent.ACTION_DOWN:
            _coords.clear();
            // fall through

        case MotionEvent.ACTION_POINTER_DOWN:
            {
                final int index = e.getActionIndex();
                final int pid = e.getPointerId(index);
                final float[] coords = getPointerCoords(e, index);
                _coords.put(pid, coords);
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
            if (is_erased())
            {
                Log.d("--------------------------------------------------", "----------------------------------");
            }
            // fall through

        case MotionEvent.ACTION_CANCEL:
            _coords.clear();
            break;
        }
        return true;
    }
}
