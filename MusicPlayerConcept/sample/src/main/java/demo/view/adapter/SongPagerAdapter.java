package demo.view.adapter;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hiteshsahu.musicplayerconcept.visualizer.utils.ColorGenerator;
import com.hiteshsahu.musicplayerconcept.visualizer.utils.TextDrawable;

import demo.R;
import demo.model.DemoDataProvider;


// TODO: Auto-generated Javadoc

/**
 * The Class SongPagerAdapter.
 */
public class SongPagerAdapter extends PagerAdapter {
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private TextDrawable.IBuilder mDrawableBuilder;
    private TextDrawable drawable;


    public SongPagerAdapter(Context context) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return DemoDataProvider.getInstance().getSongCount();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((FrameLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        final View itemView = mLayoutInflater.inflate(R.layout.view_media_page, container,
                false);

        mDrawableBuilder = TextDrawable.builder().beginConfig().withBorder(4)
                .endConfig().roundRect(10);

        drawable = mDrawableBuilder.build(String.valueOf("" + position), mColorGenerator
                .getColor("" + position));

        ((TextView) itemView
                .findViewById(R.id.songName)).setText(DemoDataProvider.getInstance().getSongName(position));


        ((TextView) itemView
                .findViewById(R.id.albumName)).setText(DemoDataProvider.getInstance().getAlbumName(position));

        Glide.with(mContext).load(ContentUris.withAppendedId(Uri
                        .parse("content://media/external/audio/albumart"),
                DemoDataProvider.getInstance().getSongID(position))).
                placeholder(R.drawable.album_back).
                centerCrop().crossFade()
                .into(((ImageView) itemView
                        .findViewById(R.id.image)));

        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((FrameLayout) object);
    }
}