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
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextColorCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.ColorSelectorDialog;
import org.telegram.ui.Components.NumberPicker;

import java.util.ArrayList;
import java.util.List;

import static org.telegram.ui.Components.ColorSelectorDialog.OnColorChangedListener;

public class ThemingContactsActivity extends BaseFragment {

    private ListView listView;
    private ListAdapter listAdapter;

    private int headerSection2Row;
    private int headerColorRow;
    private int headerTitleColorRow;
    private int headerIconsColorRow;

    private int rowsSectionRow;
    private int rowsSection2Row;
    private int rowColorRow;
    private int avatarRadiusRow;
    private int nameColorRow;
    private int nameSizeRow;
    private int statusColorRow;
    private int statusSizeRow;
    private int onlineColorRow;
    private int iconsColorRow;

    private int rowGradientRow;
    private int rowGradientColorRow;
    private int rowGradientListCheckRow;

    private int headerGradientRow;
    private int headerGradientColorRow;

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
        headerTitleColorRow = rowCount++;
        headerIconsColorRow = rowCount++;

        rowsSectionRow = rowCount++;
        rowsSection2Row = rowCount++;
        rowColorRow = rowCount++;
        rowGradientRow = rowCount++;
        rowGradientColorRow = rowCount++;
        //rowGradientListCheckRow = rowCount++;
        avatarRadiusRow  = rowCount++;
        iconsColorRow = rowCount++;
        nameColorRow = rowCount++;
        nameSizeRow = rowCount++;
        statusColorRow = rowCount++;
        statusSizeRow = rowCount++;
        onlineColorRow = rowCount++;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
        showPrefix = preferences.getBoolean("contactsShowPrefix", true);
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
            actionBar.setTitle(LocaleController.getString("ContactsScreen", R.string.ContactsScreen));

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
                    editor.putBoolean("contactsShowPrefix", showPrefix).apply();
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

                    SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);

                    if (i == headerColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }

                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.contactsHeaderColor = color;
                                commitInt("contactsHeaderColor", color);
                            }
                        }, Theme.contactsHeaderColor, CENTER, 0, false);
                        colorDialog.show();
                    } else if (i == headerGradientColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt("contactsHeaderGradientColor", color);
                            }

                        },themePrefs.getInt("contactsHeaderGradientColor", Theme.defColor), CENTER, 0, true);
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
                                Theme.contactsHeaderTitleColor = color;
                                commitInt( "contactsHeaderTitleColor", color);
                            }
                        }, Theme.contactsHeaderTitleColor, CENTER, 0, false);
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
                                Theme.contactsHeaderIconsColor = color;
                                commitInt( "contactsHeaderIconsColor", color);
                            }
                        }, Theme.contactsHeaderIconsColor, CENTER, 0, true);
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
                                Theme.contactsIconsColor = color;
                                commitInt( "contactsIconsColor", color);
                            }
                        }, Theme.contactsIconsColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == rowColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.contactsRowColor = color;
                                commitInt("contactsRowColor", color);
                            }

                        }, Theme.contactsRowColor, CENTER, 0, true);
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
                                commitInt("contactsRowGradientColor", color);
                            }
                        },themePrefs.getInt( "contactsRowGradientColor", 0xffffffff), CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == rowGradientListCheckRow) {
                        boolean b = themePrefs.getBoolean("contactsRowGradientListCheck", false);
                        SharedPreferences.Editor editor = themePrefs.edit();
                        editor.putBoolean("contactsRowGradientListCheck", !b);
                        editor.commit();
                        if (view instanceof TextCheckCell) {
                            ((TextCheckCell) view).setChecked(!b);
                        }

                    } else if (i == headerGradientRow) {
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
                                SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
                                themePrefs.edit().putInt("contactsHeaderGradient", which).commit();
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
                                SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
                                themePrefs.edit().putInt("contactsRowGradient", which).commit();
                                if (listView != null) {
                                    listView.invalidateViews();
                                }
                            }
                        });
                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        showDialog(builder.create());
                    } else if (i == nameColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.contactsRowColor = color;
                                commitInt("contactsNameColor", color);
                            }

                        }, Theme.contactsRowColor, CENTER, 0, false);
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
                                Theme.contactsStatusColor = color;
                                commitInt("contactsStatusColor", color);
                            }

                        }, Theme.contactsStatusColor, CENTER, 0, false);
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
                                Theme.contactsOnlineColor = color;
                                commitInt("contactsOnlineColor", color);
                            }
                        }, Theme.contactsOnlineColor, CENTER, 0, false);
                        colorDialog.show();
                    } else if (i == avatarRadiusRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("AvatarRadius", R.string.AvatarRadius));
                        final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                        //final int currentValue = themePrefs.getInt("contactsAvatarRadius", 32);
                        numberPicker.setMinValue(1);
                        numberPicker.setMaxValue(32);
                        numberPicker.setValue(Theme.contactsAvatarRadius);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (numberPicker.getValue() != Theme.contactsAvatarRadius) {
                                    Theme.contactsAvatarRadius = numberPicker.getValue();
                                    commitInt("contactsAvatarRadius", numberPicker.getValue());
                                }
                            }
                        });
                        showDialog(builder.create());
                    } else if (i == nameSizeRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("NameSize", R.string.NameSize));
                        final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                        //final int currentValue = themePrefs.getInt("contactsNameSize", 17);
                        numberPicker.setMinValue(12);
                        numberPicker.setMaxValue(30);
                        numberPicker.setValue(Theme.contactsNameSize);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (numberPicker.getValue() != Theme.contactsNameSize) {
                                    Theme.contactsNameSize = numberPicker.getValue();
                                    commitInt("contactsNameSize", numberPicker.getValue());
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
                        //final int currentValue = themePrefs.getInt("contactsStatusSize", 14);
                        numberPicker.setMinValue(10);
                        numberPicker.setMaxValue(20);
                        numberPicker.setValue(Theme.contactsStatusSize);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(numberPicker.getValue() != Theme.contactsStatusSize){
                                    Theme.contactsStatusSize = numberPicker.getValue();
                                    commitInt("contactsStatusSize", numberPicker.getValue());
                                }
                            }
                        });
                        showDialog(builder.create());
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
                        resetInt("contactsHeaderColor");
                    } else if (i == headerGradientColorRow) {
                        resetInt("contactsHeaderGradientColor");
                    } else if (i == headerTitleColorRow) {
                        resetInt("contactsHeaderTitleColor");
                    } else if (i == headerIconsColorRow) {
                        resetInt("contactsHeaderIconsColor");
                    } else if (i == iconsColorRow) {
                        resetInt("contactsIconsColor");
                    } else if (i == rowColorRow) {
                        resetInt("contactsRowColor");
                    } else if (i == rowGradientColorRow) {
                        resetInt("contactsRowGradientColor");
                    } else if (i == headerGradientRow) {
                        resetInt("contactsHeaderGradient");
                    } else if (i == rowGradientRow) {
                        resetInt("contactsRowGradient");
                    } else if (i == avatarRadiusRow) {
                        resetInt("contactsAvatarRadius");
                    } else if (i == nameColorRow) {
                        resetInt("contactsNameColor");
                    } else if (i == nameSizeRow) {
                        resetInt("contactsNameSize");
                    } else if (i == statusColorRow) {
                        resetInt("contactsStatusColor");
                    } else if (i == statusSizeRow) {
                        resetInt("contactsStatusSize");
                    } else if (i == onlineColorRow) {
                        resetInt("contactsOnlineColor");
                    } else{
                        if(view.getTag() != null)resetPref(view.getTag().toString());
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
        if(key != null)editor.remove(key);
        editor.commit();
        if (listView != null) {
            listView.invalidateViews();
        }
        refreshTheme();
    }

    private void resetInt(String key){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(key);
        editor.commit();
        if (listView != null) {
            listView.invalidateViews();
        }
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
        //actionBar.setBackgroundColor(AndroidUtilities.getIntColor("themeColor"));
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
            int g = AndroidUtilities.getIntDef("contactsRowGradient", 0);
            return  i == headerColorRow || i == headerGradientRow || AndroidUtilities.getIntDef("contactsHeaderGradient", 0) != 0 && i == headerGradientColorRow || i == headerTitleColorRow || i == headerIconsColorRow || i == iconsColorRow || i == rowColorRow || i == rowGradientRow || (g != 0 &&  i == rowGradientColorRow) || (g != 0 && i == rowGradientListCheckRow) || i == avatarRadiusRow || i == nameColorRow || i == nameSizeRow || i == statusColorRow || i == statusSizeRow ||
                    i == onlineColorRow ;
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
                prefix = "3.";
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
            SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
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
                    ((HeaderCell) view).setText(prefix + LocaleController.getString("ContactsList", R.string.ContactsList));
                }
            }
            else if (type == 2) {
                if (view == null) {
                    view = new TextSettingsCell(mContext);
                }
                TextSettingsCell textCell = (TextSettingsCell) view;
                if (i == avatarRadiusRow) {
                    //int size = themePrefs.getInt("contactsAvatarRadius", AndroidUtilities.isTablet() ? 35 : 32);
                    textCell.setTextAndValue(prefix + LocaleController.getString("AvatarRadius", R.string.AvatarRadius), String.format("%d", Theme.contactsAvatarRadius), true);
                } else if (i == nameSizeRow) {
                    //int size = themePrefs.getInt("contactsNameSize", AndroidUtilities.isTablet() ? 19 : 17);
                    textCell.setTextAndValue(prefix + LocaleController.getString("NameSize", R.string.NameSize), String.format("%d", Theme.contactsNameSize), true);
                } else if (i == statusSizeRow) {
                    //int size = themePrefs.getInt("contactsStatusSize", AndroidUtilities.isTablet() ? 16 : 14);
                    textCell.setTextAndValue(prefix + LocaleController.getString("StatusSize", R.string.StatusSize), String.format("%d", Theme.contactsStatusSize), true);
                }

            }
            else if (type == 3){
                if (view == null) {
                    view = new TextColorCell(mContext);
                }

                TextColorCell textCell = (TextColorCell) view;

                if (i == headerColorRow) {
                    textCell.setTextAndColor(prefix + LocaleController.getString("HeaderColor", R.string.HeaderColor), Theme.contactsHeaderColor, false);
                } else if (i == headerGradientColorRow) {
                    textCell.setTextAndColor(prefix + LocaleController.getString("RowGradientColor", R.string.RowGradientColor), themePrefs.getInt("contactsHeaderGradient", 0) == 0 ? 0x00000000 : themePrefs.getInt("contactsHeaderGradientColor", Theme.defColor), true);
                } else if (i == headerTitleColorRow) {
                    textCell.setTextAndColor(prefix + LocaleController.getString("HeaderTitleColor", R.string.HeaderTitleColor), Theme.contactsHeaderTitleColor, true);
                } else if (i == headerIconsColorRow) {
                    textCell.setTextAndColor(prefix + LocaleController.getString("HeaderIconsColor", R.string.HeaderIconsColor), Theme.contactsHeaderIconsColor, false);
                } else if (i == iconsColorRow) {
                    textCell.setTextAndColor(prefix + LocaleController.getString("IconsColor", R.string.IconsColor), Theme.contactsIconsColor, true);
                } else if (i == rowColorRow) {
                    textCell.setTextAndColor(prefix + LocaleController.getString("RowColor", R.string.RowColor), Theme.contactsRowColor, false);
                } else if (i == rowGradientColorRow) {
                    textCell.setTextAndColor(prefix + LocaleController.getString("RowGradientColor", R.string.RowGradientColor), themePrefs.getInt("contactsRowGradient", 0) == 0 ? 0x00000000 : themePrefs.getInt("contactsRowGradientColor", 0xffffffff), true);
                } else if (i == nameColorRow) {
                    textCell.setTextAndColor(prefix + LocaleController.getString("NameColor", R.string.NameColor), Theme.contactsNameColor, true);
                } else if (i == statusColorRow) {
                    textCell.setTextAndColor(prefix + LocaleController.getString("StatusColor", R.string.StatusColor), Theme.contactsStatusColor, true);
                } else if (i == onlineColorRow) {
                    textCell.setTextAndColor(prefix + LocaleController.getString("OnlineColor", R.string.OnlineColor), Theme.contactsOnlineColor, false);
                }
            } else if (type == 4) {
                if (view == null) {
                    view = new TextCheckCell(mContext);
                }
                TextCheckCell textCell = (TextCheckCell) view;
                if (i == rowGradientListCheckRow) {
                    textCell.setTag("contactsRowGradientListCheck");
                    int value = AndroidUtilities.getIntDef("contactsRowGradient", 0);
                    textCell.setTextAndCheck(prefix + LocaleController.getString("RowGradientList", R.string.RowGradientList), value == 0 ? false : themePrefs.getBoolean("contactsRowGradientListCheck", false), true);
                }
            } else if (type == 5) {
                if (view == null) {
                    view = new TextDetailSettingsCell(mContext);
                }

                TextDetailSettingsCell textCell = (TextDetailSettingsCell) view;
                if(i == headerGradientRow){
                    textCell.setTag("contactsHeaderGradient");
                    textCell.setMultilineDetail(false);
                    int value = themePrefs.getInt("contactsHeaderGradient", 0);
                    if (value == 0) {
                        textCell.setTextAndValue(prefix + LocaleController.getString("RowGradient", R.string.RowGradient), LocaleController.getString("RowGradientDisabled", R.string.RowGradientDisabled), false);
                    } else if (value == 1) {
                        textCell.setTextAndValue(prefix + LocaleController.getString("RowGradient", R.string.RowGradient), LocaleController.getString("RowGradientTopBottom", R.string.RowGradientTopBottom), false);
                    } else if (value == 2) {
                        textCell.setTextAndValue(prefix + LocaleController.getString("RowGradient", R.string.RowGradient), LocaleController.getString("RowGradientLeftRight", R.string.RowGradientLeftRight), false);
                    } else if (value == 3) {
                        textCell.setTextAndValue(prefix + LocaleController.getString("RowGradient", R.string.RowGradient), LocaleController.getString("RowGradientTLBR", R.string.RowGradientTLBR), false);
                    } else if (value == 4) {
                        textCell.setTextAndValue(prefix + LocaleController.getString("RowGradient", R.string.RowGradient), LocaleController.getString("RowGradientBLTR", R.string.RowGradientBLTR), false);
                    }
                } else if(i == rowGradientRow){
                    textCell.setTag("contactsRowGradient");
                    textCell.setMultilineDetail(false);
                    int value = themePrefs.getInt("contactsRowGradient", 0);
                    if (value == 0) {
                        textCell.setTextAndValue(prefix + LocaleController.getString("RowGradient", R.string.RowGradient), LocaleController.getString("RowGradientDisabled", R.string.RowGradientDisabled), false);
                    } else if (value == 1) {
                        textCell.setTextAndValue(prefix + LocaleController.getString("RowGradient", R.string.RowGradient), LocaleController.getString("RowGradientTopBottom", R.string.RowGradientTopBottom), false);
                    } else if (value == 2) {
                        textCell.setTextAndValue(prefix + LocaleController.getString("RowGradient", R.string.RowGradient), LocaleController.getString("RowGradientLeftRight", R.string.RowGradientLeftRight), false);
                    } else if (value == 3) {
                        textCell.setTextAndValue(prefix + LocaleController.getString("RowGradient", R.string.RowGradient), LocaleController.getString("RowGradientTLBR", R.string.RowGradientTLBR), false);
                    } else if (value == 4) {
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
            else if ( i == avatarRadiusRow || i == nameSizeRow ||  i == statusSizeRow ) {
                return 2;
            }
            else if ( i == headerColorRow || i == headerGradientColorRow || i == headerTitleColorRow || i == headerIconsColorRow || i == iconsColorRow || i == rowColorRow || i == rowGradientColorRow || i == nameColorRow || i == statusColorRow || i == onlineColorRow) {
                return 3;
            }
            else if (i == rowGradientListCheckRow) {
                return 4;
            }
            else if ( i == headerGradientRow || i == rowGradientRow) {
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
