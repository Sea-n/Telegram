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
import android.content.pm.PackageInfo;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
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

import java.io.File;
import java.util.ArrayList;

import static android.R.attr.name;
import static android.R.attr.text;
import static android.R.attr.value;
import static org.telegram.messenger.Utilities.findPrefFolder;
import static org.telegram.ui.ActionBar.Theme.applyTheme;
import static org.telegram.ui.Components.ColorSelectorDialog.OnColorChangedListener;

public class ThemingActivity extends BaseFragment {

    private ListView listView;
    private ListAdapter listAdapter;

    private static final String TAG = "ThemingActivity";

    private boolean reseting = false;
    private boolean saving = false;

    private int generalSection2Row;
    private int themeColorRow;
    private int screensSectionRow;
    private int screensSection2Row;
    private int chatsRow;
    private int chatRow;
    private int contactsRow;
    private int drawerRow;
    private int profileRow;
    private int settingsRow;

    private int themesSectionRow;
    private int themesSection2Row;
    private int resetThemeRow;
    private int saveThemeRow;
    private int applyThemeRow;

    private int dialogColorRow;

    private int usePlusThemeRow;

    private int rowCount;

    public final static int CENTER = 0;

    private boolean showPrefix;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        rowCount = 0;
        usePlusThemeRow = rowCount++;
        generalSection2Row = rowCount++;
        themeColorRow = rowCount++;
        dialogColorRow = rowCount++;

        screensSectionRow = rowCount++;
        screensSection2Row = rowCount++;
        chatsRow = rowCount++;
        chatRow = rowCount++;
        contactsRow = rowCount++;
        drawerRow = rowCount++;
        profileRow = rowCount++;
        settingsRow = rowCount++;

        themesSectionRow = rowCount++;
        themesSection2Row = rowCount++;
        saveThemeRow = rowCount++;
        applyThemeRow = rowCount++;
        resetThemeRow = rowCount++;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
        showPrefix = preferences.getBoolean("showPrefix", true);
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if(AndroidUtilities.needRestart){
            //AndroidUtilities.needRestart = false;
            Utilities.restartApp();
        }
    }

    @Override
    public View createView(Context context) {
        if (fragmentView == null) {

            //actionBar.setItemsBackground(AvatarDrawable.getButtonColorForId(5));
            actionBar.setBackButtonImage(R.drawable.ic_ab_back);

            if (AndroidUtilities.isTablet()) {
                actionBar.setOccupyStatusBar(false);
            }
            actionBar.setTitle(LocaleController.getString("Theming", R.string.Theming));

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
                    editor.putBoolean("showPrefix", showPrefix).apply();
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

                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
                    int defColor = preferences.getInt("themeColor", AndroidUtilities.defColor);

                    if (i == themeColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt(color);
                                Theme.updateAllColors();
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateDialogsTheme, Theme.UPDATE_DIALOGS_ALL_COLOR);
                            }

                        }, defColor, CENTER, 0, false);
                        colorDialog.show();
                    } else if (i == dialogColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        //SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.dialogColor = color;
                                commitInt("dialogColor", color);
                            }
                        },preferences.getInt("dialogColor", defColor), CENTER, 0, false);
                        colorDialog.show();
                    } else if(i == saveThemeRow){
                        File file = new File (Utilities.findPrefFolder(getParentActivity()), AndroidUtilities.THEME_PREFS + ".xml");
                        if(!file.exists() || (file.exists() && file.length() < 100)){
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getParentActivity(), LocaleController.getString("SaveErrorMsg0", R.string.SaveErrorMsg0) ,Toast.LENGTH_LONG ).show();
                                }
                            });
                        } else {
                            LayoutInflater li = LayoutInflater.from(getParentActivity());
                            View promptsView = li.inflate(R.layout.editbox_dialog, null);
                            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                            builder.setView(promptsView);
                            final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
                            userInput.setHint(LocaleController.getString("EnterName", R.string.EnterName));
                            userInput.setHintTextColor(0xff979797);
                            userInput.getBackground().setColorFilter(preferences.getInt("dialogColor", defColor), PorterDuff.Mode.SRC_IN);
                            AndroidUtilities.clearCursorDrawable(userInput);
                            //builder.setMessage(LocaleController.getString("EnterName", R.string.EnterName));
                            builder.setTitle(LocaleController.getString("SaveTheme", R.string.SaveTheme));
                            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (saving) {
                                        return;
                                    }
                                    final String pName = userInput.getText().toString();
                                    if (pName.length() < 1) {
                                        Toast.makeText(getParentActivity(), LocaleController.getString("NameTooShort", R.string.NameTooShort), Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    saving = true;
                                    AndroidUtilities.runOnUIThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            saving = false;
                                            if (getParentActivity() != null) {
                                                //String pName = userInput.getText().toString();
                                                AndroidUtilities.setStringPref(getParentActivity(), "themeName", pName);
                                                try {
                                                    PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                                                    AndroidUtilities.setStringPref(getParentActivity(), "version", pInfo.versionName);
                                                } catch (Exception e) {
                                                    FileLog.e(e);
                                                }
                                                AndroidUtilities.setStringPref(getParentActivity(), "model", android.os.Build.MODEL + "/" + android.os.Build.VERSION.RELEASE);
                                                AndroidUtilities.setStringPref(getParentActivity(), "date", System.currentTimeMillis() + "");
                                                Utilities.savePreferencesToSD(getParentActivity(), "/Telegram/Themes", AndroidUtilities.THEME_PREFS + ".xml", pName + ".xml", true);
                                                Utilities.copyWallpaperToSD(getParentActivity(), pName, true);
                                            }
                                        }
                                    });
                                }
                            });

                            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                            showDialog(builder.create());
                        }
                    } else if (i == applyThemeRow) {
                        DocumentSelectActivity fragment = new DocumentSelectActivity();
                        fragment.fileFilter = ".xml";
                        fragment.arrayFilter = new String[] {".xml"};
                        fragment.setDelegate(new DocumentSelectActivity.DocumentSelectActivityDelegate() {
                            @Override
                            public void didSelectFiles(DocumentSelectActivity activity, ArrayList<String> files) {
                                final String xmlFile = files.get(0);
                                File themeFile = new File(xmlFile);
                                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                                builder.setTitle(LocaleController.getString("ApplyThemeFile", R.string.ApplyThemeFile));
                                builder.setMessage(themeFile.getName());
                                final String wName = xmlFile.substring(0, xmlFile.lastIndexOf(".")) + "_wallpaper.jpg";
                                File wFile = new File(wName);
                                if(wFile.exists()){
                                    builder.setMessage(themeFile.getName()+"\n"+wFile.getName());
                                    //Change Stock Background to set Custom Wallpaper
                                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                                    int selectedBackground = preferences.getInt("selectedBackground", 1000001);
                                    if (selectedBackground == 1000001) {
                                        //File toFile = new File(ApplicationLoader.applicationContext.getFilesDir(), "wallpaper.jpg");
                                        //if (!toFile.exists()) {
                                            SharedPreferences.Editor editor = preferences.edit();
                                            editor.putInt("selectedBackground", 113);
                                            editor.putInt("selectedColor", 0);
                                            editor.commit();
                                        //}
                                    }
                                }
                                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        AndroidUtilities.runOnUIThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                    if( Utilities.loadPrefFromSD(getParentActivity(), xmlFile) == 4){
                                                        //Utilities.loadWallpaperFromSDPath(getParentActivity(), wName);
                                                        Utilities.applyWallpaper(wName);
                                                        Utilities.restartApp();
                                                    }
                                            }
                                        });
                                    }
                                });
                                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                                showDialog(builder.create());
                            }

                            @Override
                            public void startDocumentSelectActivity() {}
                        });
                        presentFragment(fragment);
                    } else if(i == resetThemeRow){
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setMessage(LocaleController.getString("AreYouSure", R.string.AreYouSure));
                        builder.setTitle(LocaleController.getString("ResetThemeSettings", R.string.ResetThemeSettings));
                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (reseting) {
                                    return;
                                }
                                reseting = true;
                                AndroidUtilities.runOnUIThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                reseting = false;
                                                SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
                                                SharedPreferences.Editor editor = themePrefs.edit();
                                                editor.clear();
                                                editor.commit();
                                                //Stock Background
                                                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                                                editor = preferences.edit();
                                                editor.putInt("selectedBackground", 1000001);
                                                editor.putInt("selectedColor", 0);
                                                editor.commit();
                                                File toFile = new File(ApplicationLoader.applicationContext.getFilesDir(), "wallpaper.jpg");
                                                if (toFile.exists()) {
                                                    toFile.delete();
                                                }
                                                if (getParentActivity() != null) {
                                                    Toast toast = Toast.makeText(getParentActivity(), LocaleController.getString("ResetThemeToastText", R.string.ResetThemeToastText), Toast.LENGTH_SHORT);
                                                    toast.show();
                                                }
                                                Theme.updateAllColors();
                                                if(listAdapter != null){
                                                    listAdapter.notifyDataSetChanged();
                                                }
                                                if(Theme.usePlusTheme)updateTheme();
                                                fixLayout();
                                            }
                                });
                                AndroidUtilities.needRestart = true;
                                AndroidUtilities.runOnUIThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (getParentActivity() != null) {
                                            Toast toast = Toast.makeText(getParentActivity(), LocaleController.getString("AppWillRestart", R.string.AppWillRestart), Toast.LENGTH_SHORT);
                                            toast.show();
                                        }
                                    }
                                });
                            }
                        });
                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        showDialog(builder.create());
                    } else if (i == chatsRow) {
                        presentFragment(new ThemingChatsActivity());
                    } else if (i == chatRow) {
                        presentFragment(new ThemingChatActivity());
                    } else if (i == contactsRow) {
                        presentFragment(new ThemingContactsActivity());
                    } else if (i == drawerRow) {
                        presentFragment(new ThemingDrawerActivity());
                    } else if (i == profileRow) {
                        presentFragment(new ThemingProfileActivity());
                    } else if (i == settingsRow) {
                        presentFragment(new ThemingSettingsActivity());
                    } else if (i == usePlusThemeRow) {
                        Theme.usePlusTheme = !Theme.usePlusTheme;
                        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
                        SharedPreferences.Editor editor = themePrefs.edit();
                        editor.putBoolean("usePlusTheme", Theme.usePlusTheme);
                        editor.apply();
                        if (view instanceof TextCheckCell) {
                            ((TextCheckCell) view).setChecked(Theme.usePlusTheme);
                        }
                        if(Theme.usePlusTheme){
                            //Log.e("ThemingActivity", ":");
                            if(Theme.getCurrentTheme() != Theme.getDefaultTheme()){
                                SharedPreferences prefs = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                                SharedPreferences.Editor edit = prefs.edit();
                                edit.putString("prevTheme", Theme.getCurrentTheme().name);
                                edit.commit();
                                //Log.e("ThemingActivity", "IN: " + Theme.getCurrentTheme().name);
                                applyTheme(Theme.getDefaultTheme());
                            }
                        } else{
                            //if(Theme.getCurrentTheme() != Theme.getDefaultTheme()){
                                //applyTheme(Theme.getCurrentTheme());
                                Theme.ThemeInfo applyingTheme = null;
                                try {
                                    SharedPreferences prefs = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                                    String theme = prefs.getString("prevTheme", prefs.getString("theme", null));
                                    if (theme != null) {
                                        applyingTheme = Theme.getThemeList().get(theme);
                                    }
                                } catch (Exception e) {
                                    FileLog.e(e);
                                }
                                if (applyingTheme == null) {
                                    applyingTheme = Theme.getDefaultTheme();
                                }
                                if(BuildConfig.DEBUG) {
                                    final String name = applyingTheme.name;
                                    AndroidUtilities.runOnUIThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (getParentActivity() != null) {
                                                Toast toast = Toast.makeText(getParentActivity(), name, Toast.LENGTH_SHORT);
                                                toast.show();
                                            }
                                        }
                                    });
                                }
                                Theme.applyTheme(applyingTheme);
                            //}
                        }
                        //if(Theme.usePlusTheme)Theme.updateAllColors();
                        //refreshTheme();
                        Theme.applyPlusTheme(true);
                        if (parentLayout != null) {
                            parentLayout.rebuildAllFragmentViews(false, false);
                        }
                        if (listView != null) {
                            listView.invalidateViews();
                        }
                    }
                }
            });

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (getParentActivity() == null) {
                        return false;
                    }
                    if (i == themeColorRow) {
                        commitInt(AndroidUtilities.defColor);
                    } else if(i == dialogColorRow){
                        resetPref("dialogColor");
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
        Theme.updateMainColors();
        refreshTheme();
        /*if (listView != null) {
            listView.invalidateViews();
        }*/
    }

    private void commitInt(String key, int value){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.commit();
        refreshTheme();
    }

    private void commitInt(int i){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("themeColor", i);
        AndroidUtilities.themeColor = i;
        editor.commit();
        //Reset Theme Colors
        int darkColor = AndroidUtilities.setDarkColor(i, 0x15);
        editor.putInt("chatsHeaderColor", i);
        editor.putInt("chatsCountBGColor", i);
        editor.putInt("chatsChecksColor", i);
        editor.putInt("chatsMemberColor", darkColor);
        editor.putInt("chatsMediaColor", preferences.getInt("chatsMemberColor", darkColor));
        editor.putInt("chatsFloatingBGColor", i);

        editor.putInt("chatHeaderColor", i);
        editor.putInt("chatRBubbleColor", AndroidUtilities.getDefBubbleColor());
        editor.putInt("chatStatusColor", AndroidUtilities.setDarkColor(i, -0x40));
        editor.putInt("chatRTimeColor", darkColor);
        editor.putInt("chatEmojiViewTabColor", AndroidUtilities.setDarkColor(i, -0x15));
        editor.putInt("chatChecksColor", i);
        editor.putInt("chatSendIconColor", i);
        editor.putInt("chatMemberColor", darkColor);
        editor.putInt("chatForwardColor", darkColor);

        editor.putInt("contactsHeaderColor", i);
        editor.putInt("contactsOnlineColor", darkColor);

        editor.putInt("prefHeaderColor", i);

        editor.putInt("dialogColor", i);

        editor.commit();
        fixLayout();
        AndroidUtilities.themeColor = i;
        refreshTheme();
    }

    private void refreshTheme(){
        if(!Theme.usePlusTheme){
            Theme.usePlusTheme = true;
            SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
            SharedPreferences.Editor editor = themePrefs.edit();
            editor.putBoolean("usePlusTheme", true);
            editor.commit();
        }
        Theme.applyPlusTheme();
        if (parentLayout != null) {
            parentLayout.rebuildAllFragmentViews(false, false);
        }
        if (listView != null) {
            listView.invalidateViews();
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
        actionBar.setTitleColor(Theme.prefActionbarTitleColor);
        actionBar.setBackgroundColor(Theme.prefActionbarColor);
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
        listView.setAdapter(listAdapter);
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
            return  i == themeColorRow || i == dialogColorRow || i == chatsRow || i == chatRow || i == contactsRow || i == drawerRow ||
                    i == profileRow || i == settingsRow || i == resetThemeRow || i == saveThemeRow || i == applyThemeRow || i == usePlusThemeRow;
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
            String prefix = showPrefix ? (i - screensSection2Row) + " " : "";
            if (type == 0) {
                if (view == null) {
                    view = new ShadowSectionCell(mContext);
                }
            }
            else if (type == 1) {
                if (view == null) {
                    view = new HeaderCell(mContext);
                    //view.setBackgroundColor(0xffffffff);
                }
                if (i == generalSection2Row) {
                    ((HeaderCell) view).setText(LocaleController.getString("General", R.string.General));
                } else if (i == screensSection2Row) {
                    ((HeaderCell) view).setText(LocaleController.getString("Screens", R.string.Screens));
                } else if (i == themesSection2Row) {
                    ((HeaderCell) view).setText(LocaleController.getString("Themes", R.string.Themes));
                }
            }
            else if (type == 2) {
                if (view == null) {
                    view = new TextSettingsCell(mContext);
                }
                TextSettingsCell textCell = (TextSettingsCell) view;
                if (i == chatsRow) {
                    textCell.setText(prefix + LocaleController.getString("MainScreen", R.string.MainScreen), true);
                } else if (i == chatRow) {
                    textCell.setText(prefix + LocaleController.getString("ChatScreen", R.string.ChatScreen), true);
                } else if (i == contactsRow) {
                    textCell.setText(prefix + LocaleController.getString("ContactsScreen", R.string.ContactsScreen), true);
                } else if (i == drawerRow) {
                    textCell.setText(prefix + LocaleController.getString("NavigationDrawer", R.string.NavigationDrawer), true);
                } else if (i == profileRow) {
                    textCell.setText(prefix + LocaleController.getString("ProfileScreen", R.string.ProfileScreen), true);
                } else if (i == settingsRow) {
                    textCell.setText(prefix + LocaleController.getString("SettingsScreen", R.string.SettingsScreen), false);
                }
            }
            else if (type == 3) {
                if (view == null) {
                    view = new TextDetailSettingsCell(mContext);
                }
                TextDetailSettingsCell textCell = (TextDetailSettingsCell) view;
                //textCell.setBackgroundColor(0xffffffff);
                if (i == saveThemeRow) {
                    textCell.setMultilineDetail(true);
                    textCell.setMultilineDetail(true);
                    String text = LocaleController.getString("SaveTheme", R.string.SaveTheme).toLowerCase();
                    if (text.length() > 0) {
                        text = String.valueOf(text.charAt(0)).toUpperCase() + text.subSequence(1, text.length());
                    }
                    textCell.setTextAndValue(text, LocaleController.getString("SaveThemeSum", R.string.SaveThemeSum), true);
                } else if (i == applyThemeRow) {
                    textCell.setMultilineDetail(true);
                    String text = LocaleController.getString("ApplyThemeFile", R.string.ApplyThemeFile);
                    //if (text.length() > 0) {
                    //    text = String.valueOf(text.charAt(0)).toUpperCase() + text.subSequence(1, text.length());
                    //}
                    textCell.setTextAndValue(text, LocaleController.getString("ApplyThemeSum", R.string.ApplyThemeSum), true);
                } else if (i == resetThemeRow) {
                    textCell.setMultilineDetail(true);
                    textCell.setTextAndValue(LocaleController.getString("ResetThemeSettings", R.string.ResetThemeSettings), LocaleController.getString("ResetThemeSettingsSum", R.string.ResetThemeSettingsSum), false);
                }
            }
            else if (type == 4){
                if (view == null) {
                    view = new TextColorCell(mContext);
                }
                TextColorCell textCell = (TextColorCell) view;

                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
                int defColor = preferences.getInt("themeColor", AndroidUtilities.defColor);

                if (i == themeColorRow) {
                    textCell.setTextAndColor(LocaleController.getString("themeColor", R.string.themeColor), defColor, true);
                } else if (i == dialogColorRow) {
                    textCell.setTextAndColor(LocaleController.getString("DialogColor", R.string.DialogColor), preferences.getInt("dialogColor", defColor), false);
                }
            } else if (type == 5) {
                if (view == null) {
                    view = new TextCheckCell(mContext);
                }
                TextCheckCell textCell = (TextCheckCell) view;
                if (i == usePlusThemeRow) {
                    textCell.setTag("usePlusTheme");
                    textCell.setTextAndCheck(LocaleController.getString("UsePlusTheme", R.string.UsePlusTheme), Theme.usePlusTheme, true);
                }
            }
            if(view != null){
                view.setBackgroundColor(Theme.usePlusTheme ? Theme.prefBGColor : Theme.getColor(Theme.key_windowBackgroundWhite));
            }
            return view;
        }

        @Override
        public int getItemViewType(int i) {
            if ( i == screensSectionRow || i == themesSectionRow ) {
                return 0;
            }
            else if ( i == generalSection2Row || i == screensSection2Row || i == themesSection2Row) {
                return 1;
            }
            else if ( i == chatsRow ) {
                return 2;
            }
            else if ( i == resetThemeRow || i == saveThemeRow || i == applyThemeRow) {
                return 3;
            }
            else if ( i == themeColorRow || i == dialogColorRow) {
                return 4;
            } else if ( i == usePlusThemeRow) {
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
