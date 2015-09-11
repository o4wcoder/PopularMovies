package com.android.fourthwardcoder.popularmovies.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.android.fourthwardcoder.popularmovies.interfaces.Constants;
import com.android.fourthwardcoder.popularmovies.helpers.DBUtil;
import com.android.fourthwardcoder.popularmovies.adapters.MovieImageAdapter;
import com.android.fourthwardcoder.popularmovies.R;
import com.android.fourthwardcoder.popularmovies.models.SimpleMovie;
import com.android.fourthwardcoder.popularmovies.activities.MovieDetailActivity;

import java.util.ArrayList;

/**
 * Class PopularMoviesMainFragment
 * Author: Chris Hare
 * Created: 7/25/2015
 *
 * Main Fragment of the Popular Movies App
 */
public class PopularMoviesMainFragment extends Fragment implements Constants {


    /**************************************************/
	/*                 Constants                      */
    /**************************************************/
    //Log tag used for debugging
    private static final String TAG = PopularMoviesMainFragment.class.getSimpleName();


    //Shared Preference for storing sort type
    private static final String PREF_SORT = "sort";
    //Tag for the time Sort Dialog
    private static final String DIALOG_SORT = "dialogSort";

    //Constant for request code to Sort Dialog
    public static final int REQUEST_SORT = 0;

    //Default sort type
    private static final int DEFAULT_SORT = 0;


    /**************************************************/
	/*                Local Data                      */
    /**************************************************/
    GridView mGridView;
    ArrayList<SimpleMovie> mMovieList = null;
    //ArrayList<Pair<Integer,String>> mGenreList;
    int mSortOrder;
    SharedPreferences.Editor prefsEditor;

    public PopularMoviesMainFragment() {
    }

    /**************************************************/
    /*               Override Methods                 */
    /**************************************************/
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //Retain fragment across Activity re-creation
        setRetainInstance(true);

        //Set Option Menu
        setHasOptionsMenu(true);

        //Get instanace of Shared Preferences
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity()
                .getApplicationContext());
        prefsEditor = sharedPrefs.edit();

        //Get stored sort preference from shared resources. If non exists set it to sort by
        //popularity.
        mSortOrder = sharedPrefs.getInt(PREF_SORT, DEFAULT_SORT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        //Get main Gridview and set up click listener
        mGridView = (GridView)view.findViewById(R.id.gridView);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Get selected movie from the GridView
                SimpleMovie movie = mMovieList.get(position);
                //Start intent to bring up Details Activity
                Intent i = new Intent(getActivity(),MovieDetailActivity.class);
                i.putExtra(EXTRA_MOVIE_ID, movie.getId());
                startActivity(i);
            }
        });

        //Make sure we have a gridview
        if(mGridView != null) {

            //We don't have any movies, go fetch them
            if (mMovieList == null)
                //Start up thread to pull in movie data. Send in sort
                //type.
                new FetchPhotosTask().execute(mSortOrder);
            else {
                //Hit this when we retained our instance of the fragment on a rotation.
                //Just apply the current list of movies
                Log.e(TAG,"Apply current movie list");
                MovieImageAdapter adapter = new MovieImageAdapter(getActivity().getApplicationContext(),mMovieList);
                mGridView.setAdapter(adapter);
            }
        }
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //super.onCreateOptionsMenu(menu, inflater);

        //Pass the resource ID of the menu and populate the Menu
        //instance with the items defined in the xml file
        inflater.inflate(R.menu.menu_sort, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.menu_item_sort:
                //Get support Fragment Manager
                android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
                SortDialogFragment dialog = SortDialogFragment.newInstance(mSortOrder);
                //Make PopularMoviesMainFragment the target fragment of the SortDialogFragment instance
                dialog.setTargetFragment(PopularMoviesMainFragment.this, REQUEST_SORT);
                dialog.show(fm, DIALOG_SORT);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Get results from Dialog boxes and other Activities
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK)
            return;

        if (requestCode == REQUEST_SORT) {
            //Get change in sort from dialog and store it in Shared Preferences
            mSortOrder = data.getIntExtra(SortDialogFragment.EXTRA_SORT,DEFAULT_SORT);
            prefsEditor.putInt(PREF_SORT, mSortOrder);
            prefsEditor.commit();

            //Fetch new set of movies based on sort order
            if(mGridView != null)
                new FetchPhotosTask().execute(mSortOrder);

        }
    }
    /*****************************************************/
    /*                Inner Classes                      */
    /*****************************************************/
    /**
     * Class FetchPhotosTask
     *
     * Inner-class that extend AysncTask to pull movie data over the network.
     * That data is return from the Movie DB API as JSON data. It is then parsed
     * and stored in Movie objects that are put into and ArrayList. This is then displayed
     * on a GridView.
     *
     * Input: Integer sort type
     * Return: ArrayList of Movies
     */
    private class FetchPhotosTask extends AsyncTask<Integer,Void,ArrayList<SimpleMovie>> {

        //ProgressDialog to be displayed while the data is being fetched and parsed
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {

            //Start ProgressDialog on Main Thread UI before precessing begins
            progressDialog = ProgressDialog.show(getActivity(),"",getString(R.string.progress_downloading_movies),true);
        }

        @Override
        protected ArrayList<SimpleMovie> doInBackground(Integer... params) {

            Uri movieUri = null;
            //We only pass one param for the sort order, so get the first one.
            int sortPos = params[0];

            //Get the sord order parameter from the sort order type
            Resources res = getResources();
            String[] sortList = res.getStringArray(R.array.sort_url_list);
            String sortOrder = sortList[sortPos];

            switch(sortPos) {

                //Sort by Popularity
                case 0:
                    //Build URI String to query the database for a list of popular movies
                    movieUri = Uri.parse(DBUtil.BASE_MOVIE_DB_URL).buildUpon()
                            .appendPath(DBUtil.PATH_MOVIE)
                            .appendPath(DBUtil.PATH_POPULAR)
                            .appendQueryParameter(DBUtil.PARAM_SORT, sortOrder)
                            .appendQueryParameter(DBUtil.PARAM_API_KEY, DBUtil.API_KEY_MOVIE_DB)
                            .build();
                    break;
                //Sort by Upcoming
                case 1:
                    //Build URI String to query the database for a list of upcoming movies
                    movieUri = Uri.parse(DBUtil.BASE_MOVIE_DB_URL).buildUpon()
                            .appendPath(DBUtil.PATH_MOVIE)
                            .appendPath(DBUtil.PATH_UPCOMING)
                            .appendQueryParameter(DBUtil.PARAM_SORT, sortOrder)
                            .appendQueryParameter(DBUtil.PARAM_API_KEY, DBUtil.API_KEY_MOVIE_DB)
                            .build();
                    break;
                //Sort by Now Playing
                case 2:
                    //Build URI String to query the database for a list of now playing movies
                    movieUri = Uri.parse(DBUtil.BASE_MOVIE_DB_URL).buildUpon()
                            .appendPath(DBUtil.PATH_MOVIE)
                            .appendPath(DBUtil.PATH_NOW_PLAYING)
                            .appendQueryParameter(DBUtil.PARAM_SORT, sortOrder)
                            .appendQueryParameter(DBUtil.PARAM_API_KEY, DBUtil.API_KEY_MOVIE_DB)
                            .build();
                    break;
                //Sort by All Time Top Grossing
                case 3:
                    //Build URI String to query the database for the list of top grossing movies
                    movieUri = Uri.parse(DBUtil.BASE_DISCOVER_URL).buildUpon()
                            .appendQueryParameter(DBUtil.PARAM_SORT, sortOrder)
                            .appendQueryParameter(DBUtil.PARAM_API_KEY, DBUtil.API_KEY_MOVIE_DB)
                            .build();
                    break;
            }


            //Log.e(TAG,movieUri.toString());
            //Get full Json result from querying the Movie DB
            String movieJsonStr = DBUtil.queryMovieDatabase(movieUri);

            //Error pulling movies, return null
            if(movieJsonStr == null)
                return null;

            //Part data and return list of movies
            return DBUtil.parseJsonMovieList(movieJsonStr);

        }




        @Override
        protected void onPostExecute(ArrayList<SimpleMovie> movieList) {

            //Done processing the movie query, kill Progress Dialog on main UI
            progressDialog.dismiss();

            if(getActivity() != null && mGridView != null) {

                //If we've got movies in the list, then send them to the adapter from the
                //GridView
                if(movieList != null) {

                    //Store global copy
                    mMovieList = movieList;
                    MovieImageAdapter adapter = new MovieImageAdapter(getActivity().getApplicationContext(),movieList);
                    mGridView.setAdapter(adapter);
                }
                else {

                    //If we get here, then the movieList was empty and something went wrong.
                    //Most likely a network connection problem
                    Toast connectToast = Toast.makeText(getActivity().getApplicationContext(),
                            getString(R.string.toast_network_error), Toast.LENGTH_LONG);
                    connectToast.show();
                }

            }
        }
    }





}