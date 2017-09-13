package org.telegram.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;

import static org.telegram.ui.ActionBar.Theme.bubblesNamesArray;
import static org.telegram.ui.ActionBar.Theme.checkid;
import static org.telegram.ui.ActionBar.Theme.checksNamesArray;
import static org.telegram.ui.ActionBar.Theme.imgid;

public class ImageListActivity extends BaseFragment {

    private int arrayId;

    public ImageListActivity(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        arrayId = arguments.getInt("array_id", 0);

        if (arrayId != 0) {

        }
        //Log.e("ImageListActivity","arrayId " + arrayId);
        super.onFragmentCreate();
        return true;
    }
    //private ListView list;
    private CustomListAdapter listAdapter;

    /*public static String getBubbleName(int i){
        return Theme.bubblesNamesArray[i];
    }

    public static String getCheckName(int i){
        return Theme.checksNamesArray[i];
    }*/

    @Override
    public View createView(Context context){
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(arrayId == 0 ? LocaleController.getString("BubbleStyle", R.string.BubbleStyle) : LocaleController.getString("CheckStyle", R.string.CheckStyle));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        fragmentView = getParentActivity().getLayoutInflater().inflate(R.layout.imagelistlayout, null, false);

        listAdapter = new CustomListAdapter(context, arrayId == 0 ? bubblesNamesArray : checksNamesArray, arrayId == 0 ? Theme.imgid : Theme.checkid);
        ListView list = (ListView) fragmentView.findViewById(R.id.list);
        list.setAdapter(listAdapter);
        list.setDivider(null);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String selectedItem = arrayId == 0 ? bubblesNamesArray[+position] : checksNamesArray[+position];
                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
                String key = arrayId == 0 ? "chatBubbleStyle" : "chatCheckStyle";
                String oldVal = preferences.getString(key, "");
                if(!oldVal.equals(selectedItem)){
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(key, selectedItem);
                    editor.apply();
                    if(arrayId == 0) {
                        Theme.setBubbles(getParentActivity());
                    } else{
                        Theme.setChecks(getParentActivity());
                    }
                    Theme.applyChatTheme(false);
                    Theme.applyDialogsTheme();
                }
                listAdapter.notifyDataSetChanged();
                finishFragment();
            }
        });

        return fragmentView;
    }

    private class CustomListAdapter extends ArrayAdapter<String> {

        private final Context mContext;
        private final String[] itemname;
        private final Integer[] imgid;

        public CustomListAdapter(Context context, String[] itemname, Integer[] imgid) {
            super(context, R.layout.imagelist, itemname);

            this.mContext = context;
            this.itemname = itemname;
            this.imgid = imgid;
        }

        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
            String name = themePrefs.getString(arrayId == 0 ? "chatBubbleStyle" : "chatCheckStyle", itemname[0]);
            view = inflater.inflate(R.layout.imagelist, parent, false);
            if(name.equals(itemname[position]) ){
                view.setBackgroundColor(0xffd0d0d0);
            } else{
                view.setBackgroundColor(0xfff0f0f0);
            }

            TextView txtTitle = (TextView) view.findViewById(R.id.bubble_title);
            ImageView inImageView = (ImageView) view.findViewById(R.id.bubble_in);
            ImageView outImageView = (ImageView) view.findViewById(R.id.bubble_out);

            txtTitle.setText(itemname[position]);
            inImageView.setImageResource(imgid[position]);
            outImageView.setImageResource(imgid[position + itemname.length]);

            if(arrayId == 1){
                view.setPadding(50, 0, 0, 0);
                //inImageView.getLayoutParams().height = 70;
                inImageView.getLayoutParams().width = 70;
                inImageView.setColorFilter(0, PorterDuff.Mode.SRC_ATOP);
                //outImageView.getLayoutParams().height = 70;
                outImageView.getLayoutParams().width = 70;
                outImageView.setColorFilter(0, PorterDuff.Mode.SRC_ATOP);

                inImageView.setColorFilter(Theme.chatChecksColor, PorterDuff.Mode.SRC_IN);
                outImageView.setColorFilter(Theme.chatChecksColor, PorterDuff.Mode.SRC_IN);
            } else{
                inImageView.setColorFilter(Theme.chatLBubbleColor, PorterDuff.Mode.SRC_IN);
                outImageView.setColorFilter(Theme.chatRBubbleColor, PorterDuff.Mode.SRC_IN);
            }

            return view;

        };
    }
}
