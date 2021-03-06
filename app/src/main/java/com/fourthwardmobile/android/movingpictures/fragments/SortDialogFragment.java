package com.fourthwardmobile.android.movingpictures.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;

/**
 * Class SortDialogFragment
 * Author: Chris Hare
 * Created: 8/6/2015
 * <p/>
 * Dialog Fragment used for selecting the sort order of the movies.
 */
public class SortDialogFragment extends DialogFragment {

    /******************************************************************************/
    /*                               Constants                                    */
    /******************************************************************************/
    //Log tag from debugging
    private static final String TAG = SortDialogFragment.class.getSimpleName();

    //Extra for passing the sort order
    public static final String EXTRA_SORT = "com.fourthwardmobile.android.movingpictures.extra_sort";

    /******************************************************************************/
    /*                                Local Data                                  */
    /******************************************************************************/
    int mSortOrder;

    public static SortDialogFragment newInstance(int sortOrder) {
        //Store sortOder as extra to pass to the dialog
        Bundle args = new Bundle();
        args.putInt(EXTRA_SORT, sortOrder);

        //Start new fragment dialog and pass sortOrder
        SortDialogFragment fragment = new SortDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //Get sort order that was passed as an extra
        mSortOrder = (int) getArguments().getInt(EXTRA_SORT);

        View v = getActivity().getLayoutInflater()
                .inflate(com.fourthwardmobile.android.movingpictures.R.layout.dialog_sort, null);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(com.fourthwardmobile.android.movingpictures.R.string.dialog_sort)
                .setSingleChoiceItems(com.fourthwardmobile.android.movingpictures.R.array.sort_list, mSortOrder, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Resources res = getResources();
                        mSortOrder = which;

                        sendResult(Activity.RESULT_OK);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_CANCELED);

                    }
                })
                .create();
    }

    /***************************************************************/
    /*                   Private Methods                           */
    /***************************************************************/
    /**
     * Sends the result of the dialog back to calling fragment
     *
     * @param resultCode code of result of dialog. In this case "OK"
     */
    private void sendResult(int resultCode) {
        if (getTargetFragment() == null)
            return;

        //Create intent and put extra on it
        Intent i = new Intent();
        i.putExtra(EXTRA_SORT, mSortOrder);

        //Send result to MainFragment
        //Request code to tell the target who is returning the result
        //result code to determine what action to take
        //An intent that can have extra data
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);
    }
}
