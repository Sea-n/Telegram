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
import android.util.Log;
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
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextColorCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.ColorSelectorDialog;
import org.telegram.ui.Components.NumberPicker;

import java.util.ArrayList;
import java.util.List;

import static org.telegram.ui.Components.ColorSelectorDialog.OnColorChangedListener;

public class ThemingProfileActivity extends BaseFragment {

    private ListView listView;
    private ListAdapter listAdapter;

    private int headerSection2Row;
    private int headerColorRow;
    private int headerIconsColorRow;

    private int rowsSectionRow;
    private int rowsSection2Row;
    private int rowColorRow;

    private int nameColorRow;
    private int nameSizeRow;
    private int statusColorRow;
    private int statusSizeRow;

    private int titleColorRow;
    private int summaryColorRow;
    private int headerAvatarRadiusRow;
    private int iconsColorRow;
    private int onlineColorRow;

    private int rowGradientRow;
    private int rowGradientColorRow;
    private int rowGradientListCheckRow;
    private int headerGradientRow;
    private int headerGradientColorRow;
    private int avatarRadiusRow;
    private int creatorStarColorRow;
    private int adminStarColorRow;

    private int rowCount;

    public final static int CENTER = 0;

    private boolean showPrefix;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        rowCount = 0;
        headerSection2Row = rowCount++;
        headerColorRow = rowCount++;
        headerGradientRow = rowCount++;
        headerGradientColorRow = rowCount++;
        headerIconsColorRow = rowCount++;
        headerAvatarRadiusRow = rowCount++;

        nameSizeRow = rowCount++;
        nameColorRow = rowCount++;
        statusSizeRow = rowCount++;
        statusColorRow = rowCount++;

        rowsSectionRow = rowCount++;
        rowsSection2Row = rowCount++;
        rowColorRow = rowCount++;
        rowGradientRow = rowCount++;
        rowGradientColorRow = rowCount++;
        //rowGradientListCheckRow = rowCount++;
        avatarRadiusRow = rowCount++;
        titleColorRow = rowCount++;
        summaryColorRow = rowCount++;
        onlineColorRow = rowCount++;
        iconsColorRow = rowCount++;
        creatorStarColorRow = rowCount++;
        adminStarColorRow = rowCount++;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
        showPrefix = preferences.getBoolean("profileShowPrefix", true);
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
            actionBar.setTitle(LocaleController.getString("ProfileScreen", R.string.ProfileScreen));

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
                    editor.putBoolean("profileShowPrefix", showPrefix).apply();
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
                    final String key = view.getTag() != null ? view.getTag().toString() : "";

                    if (i == headerColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.profileActionbarColor = color;
                                commitInt(key, color);
                            }

                        }, Theme.profileActionbarColor, CENTER, 0, false);

                        colorDialog.show();
                    }  else if (i == headerGradientRow) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("RowGradient", R.string.RowGradient));
                        List<CharSequence> array = new ArrayList<>();
                        array.add( LocaleController.getString("RowGradientDisabled", R.string.RowGradientDisabled));
                        array.add(LocaleController.getString("RowGradientTopBottom", R.string.RowGradientTopBottom));
                        array.add( LocaleController.getString("RowGradientLeftRight", R.string.RowGradientLeftRight));
                        array.add( LocaleController.getString("RowGradientTLBR", R.string.RowGradientTLBR));
                        array.add( LocaleController.getString("RowGradientBLTR", R.string.RowGradientBLTR));
                        String[] simpleArray = new String[ array.size() ];
                        array.toArray( new String[ array.size() ]);
                        builder.setItems(array.toArray(simpleArray), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                commitInt("profileHeaderGradient", which);
                                Theme.profileActionbarGradientList = which;
                                if (listView != null) {
                                    listView.invalidateViews();
                                }
                            }
                        });
                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        showDialog(builder.create());
                    } else if (i == rowGradientRow) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("RowGradient", R.string.RowGradient));
                        List<CharSequence> array = new ArrayList<>();
                        array.add( LocaleController.getString("RowGradientDisabled", R.string.RowGradientDisabled));
                        array.add(LocaleController.getString("RowGradientTopBottom", R.string.RowGradientTopBottom));
                        array.add( LocaleController.getString("RowGradientLeftRight", R.string.RowGradientLeftRight));
                        array.add( LocaleController.getString("RowGradientTLBR", R.string.RowGradientTLBR));
                        array.add( LocaleController.getString("RowGradientBLTR", R.string.RowGradientBLTR));
                        String[] simpleArray = new String[ array.size() ];
                        array.toArray( new String[ array.size() ]);
                        builder.setItems(array.toArray(simpleArray), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                commitInt("profileRowGradient", which);
                                Theme.profileRowGradientList = which;
                                if (listView != null) {
                                    listView.invalidateViews();
                                }
                            }
                        });
                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        showDialog(builder.create());
                    } /*else if (i == rowGradientListCheckRow) {
                        boolean b = themePrefs.getBoolean( "profileRowGradientListCheck", false);
                        SharedPreferences.Editor editor = themePrefs.edit();
                        editor.putBoolean("profileRowGradientListCheck", !b);
                        editor.commit();
                        if (view instanceof TextCheckCell) {
                            ((TextCheckCell) view).setChecked(!b);
                        }

                    }*/ else if (i == headerGradientColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.profileActionbarGradientColor = color;
                                commitInt(key, color);
                            }

                        }, Theme.profileActionbarGradientColor, CENTER, 0, false);
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
                                Theme.profileActionbarIconsColor = color;
                                commitInt( key, color);
                            }
                        }, Theme.profileActionbarIconsColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == iconsColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.profileRowIconsColor = color;
                                commitInt( key, color);
                            }
                        }, Theme.profileRowIconsColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == creatorStarColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.profileRowCreatorStarColor = color;
                                commitInt( key, color);
                            }
                        }, Theme.profileRowCreatorStarColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == adminStarColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.profileRowAdminStarColor = color;
                                commitInt( key, color);
                            }
                        }, Theme.profileRowAdminStarColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == nameColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.profileActionbarNameColor = color;
                                commitInt(key, color);
                            }
                        }, Theme.profileActionbarNameColor, CENTER, 0, false);
                        colorDialog.show();
                    } else if (i == statusColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.profileActionbarStatusColor = color;
                                commitInt(key, color);
                            }

                        }, Theme.profileActionbarStatusColor, CENTER, 0, false);

                        colorDialog.show();
                    }  else if (i == nameSizeRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("NameSize", R.string.NameSize));
                        final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                        //final int currentValue = themePrefs.getInt(key, 18);
                        numberPicker.setMinValue(12);
                        numberPicker.setMaxValue(30);
                        numberPicker.setValue(Theme.profileActionbarNameSize);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (numberPicker.getValue() != Theme.profileActionbarNameSize) {
                                    Theme.profileActionbarNameSize = numberPicker.getValue();
                                    commitInt(key, numberPicker.getValue());
                                }
                            }
                        });
                        showDialog(builder.create());
                    } else if (i == statusSizeRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("StatusSize", R.string.StatusSize));
                        final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                        //final int currentValue = themePrefs.getInt(key, 14);
                        numberPicker.setMinValue(8);
                        numberPicker.setMaxValue(22);
                        numberPicker.setValue(Theme.profileActionbarStatusSize);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (numberPicker.getValue() != Theme.profileActionbarStatusSize) {
                                    Theme.profileActionbarStatusSize = numberPicker.getValue();
                                    commitInt(key, numberPicker.getValue());
                                }
                            }
                        });
                        showDialog(builder.create());
                    } else if (i == rowColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.profileRowColor = color;
                                commitInt( key, color);
                            }

                        }, Theme.profileRowColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == rowGradientColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.profileRowGradientColor = color;
                                commitInt( key, color);
                            }

                        }, Theme.profileRowGradientColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == headerAvatarRadiusRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("AvatarRadius", R.string.AvatarRadius));
                        final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                        //final int currentValue = themePrefs.getInt( "profileAvatarRadius", 32);
                        numberPicker.setMinValue(1);
                        numberPicker.setMaxValue(32);
                        numberPicker.setValue(Theme.profileActionbarAvatarRadius);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (numberPicker.getValue() != Theme.profileActionbarAvatarRadius) {
                                    Theme.profileActionbarAvatarRadius = numberPicker.getValue();
                                    commitInt("profileAvatarRadius", numberPicker.getValue());
                                }
                            }
                        });
                        showDialog(builder.create());
                    } else if (i == avatarRadiusRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("AvatarRadius", R.string.AvatarRadius));
                        final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                        //final int currentValue = themePrefs.getInt( "profileRowAvatarRadius", 32);
                        numberPicker.setMinValue(1);
                        numberPicker.setMaxValue(32);
                        numberPicker.setValue(Theme.profileRowAvatarRadius);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (numberPicker.getValue() != Theme.profileRowAvatarRadius) {
                                    Theme.profileRowAvatarRadius = numberPicker.getValue();
                                    commitInt("profileRowAvatarRadius", numberPicker.getValue());
                                }
                            }
                        });
                        showDialog(builder.create());
                    } else if (i == titleColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.profileRowTitleColor = color;
                                commitInt(key, color);
                            }
                        }, Theme.profileRowTitleColor, CENTER, 0, false);
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
                                Theme.profileRowStatusColor = color;
                                commitInt(key, color);
                            }
                        }, Theme.profileRowStatusColor, CENTER, 0, false);
                        colorDialog.show();
                    } else if (i == onlineColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.profileRowOnlineColor = color;
                                commitInt(key, color);
                            }
                        }, Theme.profileRowOnlineColor, CENTER, 0, false);
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
                    if(view.getTag() != null){
                        resetPref(view.getTag().toString());
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

    private void resetPref(String key){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(key);
        editor.commit();
        //Log.e("Theme", "resetPref " + key);
        if (listView != null) {
            listView.invalidateViews();
        }
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
            return  i == headerColorRow || i == headerGradientRow || Theme.profileActionbarGradientList > 0 && i == headerGradientColorRow || i == headerIconsColorRow ||
                    i == iconsColorRow || i == nameColorRow || i == nameSizeRow || i == statusColorRow || i == statusSizeRow || i == rowColorRow || i == rowGradientRow ||
                    Theme.profileRowGradientList != 0 && i == rowGradientColorRow || Theme.profileRowGradientList != 0 && i == rowGradientListCheckRow ||
                    i == titleColorRow || i == summaryColorRow || i == onlineColorRow || i == headerAvatarRadiusRow || i == avatarRadiusRow || i == creatorStarColorRow ||
                    i == adminStarColorRow;
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
                prefix = "5.";
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
            else if (type == 2) {
                if (view == null) {
                    view = new TextSettingsCell(mContext);
                }
                TextSettingsCell textCell = (TextSettingsCell) view;
                if (i == nameSizeRow) {
                    textCell.setTag("profileNameSize");
                    textCell.setTextAndValue(prefix + LocaleController.getString("NameSize", R.string.NameSize), String.format("%d", Theme.profileActionbarNameSize), true);
                } else if (i == statusSizeRow) {
                    textCell.setTag("profileStatusSize");
                    textCell.setTextAndValue(prefix + LocaleController.getString("StatusSize", R.string.StatusSize), String.format("%d", Theme.profileActionbarStatusSize), true);
                } else if (i == headerAvatarRadiusRow) {
                    textCell.setTag("profileAvatarRadius");
                    textCell.setTextAndValue(prefix + LocaleController.getString("AvatarRadius", R.string.AvatarRadius), String.format("%d", Theme.profileActionbarAvatarRadius), true);
                } else if (i == avatarRadiusRow) {
                    textCell.setTag("profileRowAvatarRadius");
                    textCell.setTextAndValue(prefix + LocaleController.getString("AvatarRadius", R.string.AvatarRadius), String.format("%d", Theme.profileRowAvatarRadius), true);
                }
            }
            else if (type == 3){
                if (view == null) {
                    view = new TextColorCell(mContext);
                }

                TextColorCell textCell = (TextColorCell) view;
                if (i == headerColorRow) {
                    textCell.setTag("profileHeaderColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("HeaderColor", R.string.HeaderColor), Theme.profileActionbarColor, false);
                } else if (i == headerGradientColorRow) {
                    textCell.setTag("profileHeaderGradientColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("RowGradientColor", R.string.RowGradientColor), Theme.profileActionbarGradientList == 0 ? 0x00000000 : Theme.profileActionbarGradientColor, true);
                } else if (i == headerIconsColorRow) {
                    textCell.setTag("profileHeaderIconsColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("HeaderIconsColor", R.string.HeaderIconsColor), Theme.profileActionbarIconsColor, true);
                } else if (i == iconsColorRow) {
                    textCell.setTag("profileIconsColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("IconsColor", R.string.IconsColor), Theme.profileRowIconsColor, true);
                } else if (i == creatorStarColorRow) {
                    textCell.setTag("profileCreatorStarColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("CreatorStarColor", R.string.CreatorStarColor), Theme.profileRowCreatorStarColor, true);
                } else if (i == adminStarColorRow) {
                    textCell.setTag("profileAdminStarColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("AdminStarColor", R.string.AdminStarColor), Theme.profileRowAdminStarColor, false);
                } else if (i == nameColorRow) {
                    textCell.setTag("profileNameColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("NameColor", R.string.NameColor), Theme.profileActionbarNameColor, true);
                } else if (i == statusColorRow) {
                    textCell.setTag("profileStatusColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("StatusColor", R.string.StatusColor), Theme.profileActionbarStatusColor, false);
                } else if (i == rowColorRow) {
                    textCell.setTag("profileRowColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("RowColor", R.string.RowColor), Theme.profileRowColor, false);
                } else if (i == rowGradientColorRow) {
                    textCell.setTag("profileRowGradientColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("RowGradientColor", R.string.RowGradientColor), Theme.profileRowGradientList == 0 ? 0x00000000 : Theme.profileRowGradientColor, true);
                } else if (i == titleColorRow) {
                    textCell.setTag("profileTitleColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("NameColor", R.string.NameColor), Theme.profileRowTitleColor, true);
                } else if (i == summaryColorRow) {
                    textCell.setTag("profileSummaryColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("StatusColor", R.string.StatusColor), Theme.profileRowStatusColor, true);
                } else if (i == onlineColorRow) {
                    textCell.setTag("profileOnlineColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("OnlineColor", R.string.OnlineColor), Theme.profileRowOnlineColor, true);
                }
            } else if (type == 4) {
                if (view == null) {
                    view = new TextCheckCell(mContext);
                }
                /*TextCheckCell textCell = (TextCheckCell) view;

                if (i == rowGradientListCheckRow) {
                    textCell.setTag("profileRowGradientListCheck");
                    int value = AndroidUtilities.getIntDef("profileRowGradient", 0);
                    textCell.setTextAndCheck(prefix + LocaleController.getString("RowGradientList", R.string.RowGradientList), value == 0 ? false : themePrefs.getBoolean("profileRowGradientListCheck", false), true);
                }*/
            } else if (type == 5) {
                if (view == null) {
                    view = new TextDetailSettingsCell(mContext);
                }
                TextDetailSettingsCell textCell = (TextDetailSettingsCell) view;

                if(i == headerGradientRow){
                    textCell.setTag("profileHeaderGradient");
                    textCell.setMultilineDetail(false);
                    if (Theme.profileActionbarGradientList == 0) {
                        textCell.setTextAndValue(prefix + LocaleController.getString("RowGradient", R.string.RowGradient), LocaleController.getString("RowGradientDisabled", R.string.RowGradientDisabled), false);
                    } else if (Theme.profileActionbarGradientList == 1) {
                        textCell.setTextAndValue(prefix + LocaleController.getString("RowGradient", R.string.RowGradient), LocaleController.getString("RowGradientTopBottom", R.string.RowGradientTopBottom), false);
                    } else if (Theme.profileActionbarGradientList == 2) {
                        textCell.setTextAndValue(prefix + LocaleController.getString("RowGradient", R.string.RowGradient), LocaleController.getString("RowGradientLeftRight", R.string.RowGradientLeftRight), false);
                    } else if (Theme.profileActionbarGradientList == 3) {
                        textCell.setTextAndValue(prefix + LocaleController.getString("RowGradient", R.string.RowGradient), LocaleController.getString("RowGradientTLBR", R.string.RowGradientTLBR), false);
                    } else if (Theme.profileActionbarGradientList == 4) {
                        textCell.setTextAndValue(prefix + LocaleController.getString("RowGradient", R.string.RowGradient), LocaleController.getString("RowGradientBLTR", R.string.RowGradientBLTR), false);
                    }
                } else if(i == rowGradientRow){
                    textCell.setTag("profileRowGradient");
                    textCell.setMultilineDetail(false);
                    if (Theme.profileRowGradientList == 0) {
                        textCell.setTextAndValue(prefix + LocaleController.getString("RowGradient", R.string.RowGradient), LocaleController.getString("RowGradientDisabled", R.string.RowGradientDisabled), false);
                    } else if (Theme.profileRowGradientList == 1) {
                        textCell.setTextAndValue(prefix + LocaleController.getString("RowGradient", R.string.RowGradient), LocaleController.getString("RowGradientTopBottom", R.string.RowGradientTopBottom), false);
                    } else if (Theme.profileRowGradientList == 2) {
                        textCell.setTextAndValue(prefix + LocaleController.getString("RowGradient", R.string.RowGradient), LocaleController.getString("RowGradientLeftRight", R.string.RowGradientLeftRight), false);
                    } else if (Theme.profileRowGradientList == 3) {
                        textCell.setTextAndValue(prefix + LocaleController.getString("RowGradient", R.string.RowGradient), LocaleController.getString("RowGradientTLBR", R.string.RowGradientTLBR), false);
                    } else if (Theme.profileRowGradientList == 4) {
                        textCell.setTextAndValue(prefix + LocaleController.getString("RowGradient", R.string.RowGradient), LocaleController.getString("RowGradientBLTR", R.string.RowGradientBLTR), false);
                    }
                }
            }
            if(view != null){
                view.setBackgroundColor(Theme.usePlusTheme ? Theme.prefBGColor : Theme.getColor(Theme.key_windowBackgroundWhite));
            }
            return view;
        }

        @Override
        public int getItemViewType(int i) {
            if ( i == rowsSectionRow ) {
                return 0;
            }
            else if ( i == headerSection2Row || i == rowsSection2Row ) {
                return 1;
            }
            else if ( i == nameSizeRow ||  i == statusSizeRow || i == headerAvatarRadiusRow || i == avatarRadiusRow) {
                return 2;
            }
            else if ( i == headerColorRow || i == headerGradientColorRow || i == headerIconsColorRow || i == iconsColorRow || i == nameColorRow || i == statusColorRow ||
                      i == rowColorRow ||  i == rowGradientColorRow || i == titleColorRow || i == summaryColorRow || i == onlineColorRow || i == creatorStarColorRow ||
                      i == adminStarColorRow) {
                return 3;
            }
            else if (i == rowGradientListCheckRow) {
                return 4;
            }
            else if (i == headerGradientRow || i == rowGradientRow) {
                return 5;
            }
            else {
                return 2;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 6;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}
