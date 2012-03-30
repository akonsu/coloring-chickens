/* -*- mode:java; coding:utf-8; -*- Time-stamp: <MainActivity.java - root> */

package rabbitmish.coloringchickens;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

public class MainActivity extends Activity
{

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    public void onDestroy()
    {
        ImageView view = (ImageView)findViewById(R.id.eraseView);

        if (view != null)
        {
            view.setImageDrawable(null);
        }
        super.onDestroy();
    }
}
