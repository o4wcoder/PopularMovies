package com.android.fourthwardcoder.popularmovies.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.fourthwardcoder.popularmovies.R;
import com.android.fourthwardcoder.popularmovies.models.Video;

import java.util.ArrayList;

/**
 * Class VideosListAdapter
 * Author: Chris Hare
 * Created: 8/28/2015
 * <p/>
 * List Adapater for Videos of a Movie or TV Show.
 */
public class VideosListAdapter extends ArrayAdapter<Video> {

    /****************************************************************/
    /*                         Constants                            */
    /****************************************************************/
    private final static String TAG = VideosListAdapter.class.getSimpleName();
    /****************************************************************/
    /*                         Local Data                           */
    /****************************************************************/
    Context mContext;

    public VideosListAdapter(Context context, ArrayList<Video> videoList) {
        super(context, 0, videoList);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.video_list_item, parent, false);
            //Get Video TextView info
            holder.nameTextView = (TextView) convertView.findViewById(R.id.videoNameTextView);
            holder.infoTextView = (TextView) convertView.findViewById(R.id.videoInfoTextView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //Get each Movie using the position in the ArrayAdapter
        Video video = getItem(position);

        holder.nameTextView.setText(video.getName());
        holder.infoTextView.setText(video.getType() + ": " + String.valueOf(video.getSize()) + "p");

        return convertView;
    }

    /**********************************************************************/
    /*                          Inner Classes                             */

    /**********************************************************************/
    private static class ViewHolder {

        TextView nameTextView;
        TextView infoTextView;
    }
}
