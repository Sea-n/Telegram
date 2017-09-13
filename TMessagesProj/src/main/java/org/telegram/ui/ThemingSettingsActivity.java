/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package org.telegram.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextColorCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.ColorSelectorDialog;
import org.telegram.ui.Components.NumberPicker;

import static org.telegram.ui.Components.ColorSelectorDialog.OnColorChangedListener;

public class ThemingSettingsActivity extends BaseFragment {

    private ListView listView;
    private ListAdapter listAdapter;

    private int sectionColorRow;
    private int titleColorRow;
    private int summaryColorRow;
    private int backgroundColorRow;
    private int dividerColorRow;
    private int shadowColorRow;

    private int headerSection2Row;
    private int headerColorRow;
    private int headerTitleColorRow;
    private int headerIconsColorRow;

    private int rowsSectionRow;
    private int rowsSection2Row;
    private int avatarColorRow;
    private int avatarRadiusRow;
    private int avatarSizeRow;
    private int headerStatusColorRow;

    private int rowCount;

    public final static int CENTER = 0;

    private boolean showPrefix;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        rowCount = 0;

        headerSection2Row = rowCount++;
        headerColorRow  = rowCount++;
        headerTitleColorRow = rowCount++;
        headerStatusColorRow = rowCount++;
        headerIconsColorRow = rowCount++;
        avatarColorRow  = rowCount++;
        avatarRadiusRow  = rowCount++;
        avatarSizeRow = rowCount++;

        rowsSectionRow = rowCount++;
        rowsSection2Row = rowCount++;
        backgroundColorRow = rowCount++;
        shadowColorRow = rowCount++;
        sectionColorRow = rowCount++;
        titleColorRow = rowCount++;
        summaryColorRow = rowCount++;
        dividerColorRow = rowCount++;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
        showPrefix = preferences.getBoolean("prefShowPrefix", true);
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();

    }

    @Override
    public View createView(Context context) {
        if (fragmentView == null) {

            //actionBar.setItemsBackground(AvatarDrawable.getButtonColorForId(5));
            actionBar.setBackButtonImage(R.drawable.ic_ab_back);


            if (AndroidUtilities.isTablet()) {
                actionBar.setOccupyStatusBar(false);
            }
            actionBar.setTitle(LocaleController.getString("SettingsScreen", R.string.SettingsScreen));

            actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
                @Override
                public void onItemClick(int id) {
                    if (id == -1) {
                        finishFragment();
                    }
                }
            });

            actionBar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPrefix = !showPrefix;
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("prefShowPrefix", showPrefix).apply();
                    if (listAdapter != null) {
                        listAdapter.notifyDataSetChanged();
                    }
                }
            });

            listAdapter = new ListAdapter(context);

            fragmentView = new FrameLayout(context);
            FrameLayout frameLayout = (FrameLayout) fragmentView;

            listView = new ListView(context);
            //SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
            if(Theme.usePlusTheme)listView.setBackgroundColor(Theme.prefBGColor);
            listView.setDivider(null);
            listView.setDividerHeight(0);
            listView.setVerticalScrollBarEnabled(false);

            //int def = preferences.getInt("themeColor", AndroidUtilities.defColor);
            //int hColor = preferences.getInt("prefHeaderColor", def);
            AndroidUtilities.setListViewEdgeEffectColor(listView, /*AvatarDrawable.getProfileBackColorForId(5)*/ Theme.prefActionbarColor);
            frameLayout.addView(listView);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) listView.getLayoutParams();
            layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
            layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
            layoutParams.gravity = Gravity.TOP;
            listView.setLayoutParams(layoutParams);
            listView.setAdapter(listAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                    //SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
                    //int defColor = themePrefs.getInt("themeColor", AndroidUtilities.defColor);
                    if (i == headerColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.prefActionbarColor = color;
                                commitInt("prefHeaderColor", color);
                            }
                        }, Theme.prefActionbarColor, CENTER, 0, false);
                        colorDialog.show();
                    } else if (i == headerTitleColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.prefActionbarTitleColor = color;
                                commitInt("prefHeaderTitleColor", color);
                            }
                        }, Theme.prefActionbarTitleColor, CENTER, 0, false);
                        colorDialog.show();
                    } else if (i == headerStatusColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.prefActionbarStatusColor = color;
                                commitInt("prefHeaderStatusColor", color);
                            }
                        }, Theme.prefActionbarStatusColor, CENTER, 0, false);
                        colorDialog.show();
                    } else if (i == headerIconsColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.prefActionbarIconsColor = color;
                                commitInt( "prefHeaderIconsColor", color);
                            }
                        }, Theme.prefActionbarIconsColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == avatarColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.prefAvatarColor = color;
                                commitInt( "prefAvatarColor", color);
                            }
                        }, Theme.prefAvatarColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == avatarRadiusRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("AvatarRadius", R.string.AvatarRadius));
                        final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                        //final int currentValue = themePrefs.getInt("prefAvatarRadius", 32);
                        numberPicker.setMinValue(1);
                        numberPicker.setMaxValue(32);
                        numberPicker.setValue(Theme.prefAvatarRadius);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (numberPicker.getValue() != Theme.prefAvatarRadius) {
                                    Theme.prefAvatarRadius = numberPicker.getValue();
                                    commitInt("prefAvatarRadius", numberPicker.getValue());
                                }
                            }
                        });
                        showDialog(builder.create());
                    } else if (i == avatarSizeRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("AvatarSize", R.string.AvatarSize));
                        final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                        //final int currentValue = themePrefs.getInt("prefAvatarSize", 42);
                        numberPicker.setMinValue(0);
                        numberPicker.setMaxValue(48);
                        numberPicker.setValue(Theme.prefAvatarSize);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (numberPicker.getValue() != Theme.prefAvatarSize) {
                                    Theme.prefAvatarSize = numberPicker.getValue();
                                    commitInt("prefAvatarSize", numberPicker.getValue());
                                }
                            }
                        });
                        showDialog(builder.create());
                    }
                    else if (i == backgroundColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.prefBGColor = color;
                                commitInt("prefBGColor", color);
                            }
                        }, Theme.prefBGColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == shadowColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.prefShadowColor = color;
                                commitInt("prefShadowColor", color);
                            }
                        }, Theme.prefShadowColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == sectionColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.prefSectionColor = color;
                                commitInt("prefSectionColor", color);
                            }
                        }, Theme.prefSectionColor, CENTER, 0, false);
                        colorDialog.show();
                    } else if (i == titleColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.prefTitleColor = color;
                                commitInt("prefTitleColor", color);
                            }
                        }, Theme.prefTitleColor, CENTER, 0, false);
                        colorDialog.show();
                    } else if (i == summaryColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.prefSummaryColor = color;
                                commitInt("prefSummaryColor", color);
                            }
                        }, Theme.prefSummaryColor, CENTER, 0, false);
                        colorDialog.show();
                    } else if (i == dividerColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.prefDividerColor = color;
                                commitInt("prefDividerColor", color);
                            }
                        }, Theme.prefDividerColor, CENTER, 0, true);
                        colorDialog.show();
                    }
                }
            });

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (getParentActivity() == null) {
                        return false;
                    }
                    if(BuildConfig.DEBUG){
                        final String key = view.getTag() != null ? view.getTag().toString() : "";
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                if (getParentActivity() != null) {
                                    Toast toast = Toast.makeText(getParentActivity(), key, Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            }
                        });
                    }
                    if (i == headerColorRow) {
                        resetInt("prefHeaderColor");
                    } else if (i == headerTitleColorRow) {
                        resetInt("prefHeaderTitleColor");
                    } else if (i == headerStatusColorRow) {
                        resetInt("prefHeaderStatusColor");
                    } else if (i == headerIconsColorRow) {
                        resetInt("prefHeaderIconsColor");
                    } else if (i == avatarColorRow) {
                        resetInt("prefAvatarColor");
                    } else if (i == avatarRadiusRow) {
                        resetInt("prefAvatarRadius");
                    } else if (i == avatarSizeRow) {
                        resetInt("prefAvatarSize");
                    } else if (i == backgroundColorRow) {
                        resetInt("prefBGColor");
                    } else if (i == shadowColorRow) {
                        resetInt("prefShadowColor");
                    } else if (i == sectionColorRow) {
                        resetInt("prefSectionColor");
                    } else if (i == titleColorRow) {
                        resetInt("prefTitleColor");
                    } else if (i == summaryColorRow) {
                        resetInt("prefSummaryColor");
                    } else if (i == dividerColorRow) {
                        resetInt("prefDividerColor");
                    }
                    return true;
                }
            });

            frameLayout.addView(actionBar);
        } else {
            ViewGroup parent = (ViewGroup)fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }
        if(Theme.usePlusTheme)updateTheme();
        return fragmentView;
    }

    private void resetInt(String key){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(key);
        editor.commit();
        if (listView != null) {
            listView.invalidateViews();
        }
        if(Theme.usePlusTheme)updateTheme();
        refreshTheme();
    }

    private void commitInt(String key, int value){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.commit();
        if (listView != null) {
            listView.invalidateViews();
        }
        if(Theme.usePlusTheme)updateTheme();
        refreshTheme();
    }

    private void refreshTheme(){
        Theme.applyPlusTheme();
        if (parentLayout != null) {
            parentLayout.rebuildAllFragmentViews(false, false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
        fixLayout();
    }

    private void updateTheme(){
        actionBar.setBackgroundColor(Theme.prefActionbarColor);
        actionBar.setTitleColor(Theme.prefActionbarTitleColor);
        Drawable back = getParentActivity().getResources().getDrawable(R.drawable.ic_ab_back);
        back.setColorFilter(Theme.prefActionbarIconsColor, PorterDuff.Mode.MULTIPLY);
        actionBar.setBackButtonDrawable(back);
        actionBar.setItemsColor(Theme.prefActionbarIconsColor, false);
        fixLayout();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        fixLayout();
    }

    private void fixLayout() {
        if (fragmentView == null) {
            return;
        }
        fragmentView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (fragmentView != null) {
                    //needLayout();
                    fragmentView.getViewTreeObserver().removeOnPreDrawListener(this);
                }
                return false;
            }
        });
        //listView.setAdapter(listAdapter);
    }

    private class ListAdapter extends BaseAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int i) {
            return  i == headerColorRow || i == headerTitleColorRow || i == headerStatusColorRow  || i == headerIconsColorRow || i == avatarColorRow || i == avatarRadiusRow || i == avatarSizeRow || i == backgroundColorRow || i == shadowColorRow || i == sectionColorRow || i == titleColorRow || i == summaryColorRow || i == dividerColorRow;
        }

        @Override
        public int getCount() {
            return rowCount;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            int type = getItemViewType(i);
            String prefix = "";
            if(showPrefix) {
                prefix = "6.";
                if (i == headerSection2Row) {
                    prefix = prefix + "1 ";
                } else if (i == rowsSection2Row) {
                    prefix = prefix + "2 ";
                } else if (i < rowsSection2Row) {
                    prefix = prefix + "1." + i + " ";
                } else {
                    prefix = prefix + "2." + (i - rowsSection2Row) + " ";
                }
            }
            if (type == 0) {
                if (view == null) {
                    view = new ShadowSectionCell(mContext);
                }
            }
            else if (type == 1) {
                if (view == null) {
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(0xffffffff);
                }
                if (i == headerSection2Row) {
                    ((HeaderCell) view).setText(prefix + LocaleController.getString("Header", R.string.Header));
                } else if (i == rowsSection2Row) {
                    ((HeaderCell) view).setText(prefix + LocaleController.getString("OptionsList", R.string.OptionsList));
                }
            }
            else if (type == 2){
                if (view == null) {
                    view = new TextColorCell(mContext);
                }
                TextColorCell textCell = (TextColorCell) view;
                //int defColor = themePrefs.getInt("themeColor", AndroidUtilities.defColor);
                if (i == headerColorRow) {
                    textCell.setTag("prefActionbarColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("HeaderColor", R.string.HeaderColor), Theme.prefActionbarColor, true);
                } else if (i == headerTitleColorRow) {
                    textCell.setTag("prefActionbarTitleColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("HeaderTitleColor", R.string.HeaderTitleColor), Theme.prefActionbarTitleColor, true);
                } else if (i == headerStatusColorRow) {
                    textCell.setTag("prefActionbarStatusColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("StatusColor", R.string.StatusColor), Theme.prefActionbarStatusColor, true);
                } else if (i == headerIconsColorRow) {
                    textCell.setTag("prefActionbarIconsColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("HeaderIconsColor", R.string.HeaderIconsColor), Theme.prefActionbarIconsColor, true);
                } else if (i == avatarColorRow) {
                    textCell.setTag("prefAvatarColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("AvatarColor", R.string.AvatarColor), Theme.prefAvatarColor, true);
                } else if (i == backgroundColorRow) {
                    textCell.setTag("prefBGColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("BackgroundColor", R.string.BackgroundColor), Theme.prefBGColor, true);
                } else if (i == shadowColorRow) {
                    textCell.setTag("prefShadowColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("ShadowColor", R.string.ShadowColor), Theme.prefShadowColor, true);
                } else if (i == sectionColorRow) {
                    textCell.setTag("prefSectionColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("SectionColor", R.string.SectionColor), Theme.prefSectionColor, true);
                } else if (i == titleColorRow) {
                    textCell.setTag("prefTitleColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("TitleColor", R.string.TitleColor), Theme.prefTitleColor, true);
                } else if (i == summaryColorRow) {
                    textCell.setTag("prefSummaryColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("SummaryColor", R.string.SummaryColor), Theme.prefSummaryColor, true);
                } else if (i == dividerColorRow) {
                    textCell.setTag("prefDividerColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("DividerColor", R.string.DividerColor), Theme.prefDividerColor, true);
                }
            } else if (type == 3) {
                if (view == null) {
                    view = new TextSettingsCell(mContext);
                }
                TextSettingsCell textCell = (TextSettingsCell) view;
                SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
                if (i == avatarRadiusRow) {
                    textCell.setTag("prefAvatarRadius");
                    int size = themePrefs.getInt("prefAvatarRadius", AndroidUtilities.isTablet() ? 35 : 32);
                    textCell.setTextAndValue(prefix + LocaleController.getString("AvatarRadius", R.string.AvatarRadius), String.format("%d", size), true);
                } else if (i == avatarSizeRow) {
                    textCell.setTag("prefAvatarSize");
                    int size = themePrefs.getInt("prefAvatarSize", AndroidUtilities.isTablet() ? 45 : 42);
                    textCell.setTextAndValue(prefix + LocaleController.getString("AvatarSize", R.string.AvatarSize), String.format("%d", size), false);
                }

            }
            if(view != null){
                view.setBackgroundColor(Theme.usePlusTheme ? Theme.prefBGColor : Theme.getColor(Theme.key_windowBackgroundWhite));
            }
            return view;
        }

        @Override
        public int getItemViewType(int i) {
            if ( i == avatarRadiusRow || i == avatarSizeRow) {
                return 3;
            }
            if ( i == headerColorRow || i == headerTitleColorRow || i == headerStatusColorRow || i == headerIconsColorRow || i == avatarColorRow || i == backgroundColorRow || i == shadowColorRow || i == sectionColorRow || i == titleColorRow || i == summaryColorRow || i == dividerColorRow) {
                return 2;
            }
            else if ( i == headerSection2Row || i == rowsSection2Row ) {
                return 1;
            }
            else {
                return 0;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 4;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}
