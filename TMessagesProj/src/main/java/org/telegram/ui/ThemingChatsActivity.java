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
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
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

import static android.R.attr.key;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static org.telegram.ui.ActionBar.Theme.chatsFloatingPencilColor;
import static org.telegram.ui.Components.ColorSelectorDialog.OnColorChangedListener;

public class ThemingChatsActivity extends BaseFragment {

    private ListView listView;
    private ListAdapter listAdapter;

    private int headerSection2Row;
    private int headerColorRow;
    private int headerTitleColorRow;
    private int headerTitleRow;
    private int headerIconsColorRow;

    private int rowsSectionRow;
    private int rowsSection2Row;
    private int rowColorRow;
    private int dividerColorRow;
    private int nameSizeRow;
    private int nameColorRow;
    private int checksColorRow;
    private int muteColorRow;
    private int avatarRadiusRow;
    private int messageColorRow;
    private int memberColorRow;
    private int typingColorRow;
    private int messageSizeRow;
    private int timeColorRow;
    private int timeSizeRow;
    private int countColorRow;
    private int countSizeRow;
    private int countBGColorRow;
    private int countSilentBGColorRow;
    private int floatingPencilColorRow;
    private int floatingBGColorRow;
    private int avatarSizeRow;
    private int avatarMarginLeftRow;
    private int unknownNameColorRow;
    private int groupNameColorRow;
    private int groupNameSizeRow;
    private int mediaColorRow;
    private int groupIconColorRow;
    private int rowGradientRow;
    private int rowGradientColorRow;
    private int rowGradientListCheckRow;
    private int headerGradientRow;
    private int headerGradientColorRow;
    private int highlightSearchColorRow;

    private int hideStatusIndicatorCheckRow;
    private int headerTabIconColorRow;
    private int headerTabUnselectedIconColorRow;
    private int headerTabCounterColorRow;
    private int headerTabCounterBGColorRow;
    private int headerTabCounterSilentBGColorRow;
    private int tabsCounterSizeRow;
    private int tabsBGColorRow;
    private int favIndicatorColorRow;
    private int tabsToBottomRow;
    private int hideHeaderShadow;
    private int tabsTextModeRow;
    private int tabsTextSizeRow;
    private int pinnedMsgBGColorRow;

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
        headerTitleRow = rowCount++;
        headerIconsColorRow = rowCount++;
        hideHeaderShadow = rowCount++;
        tabsBGColorRow = rowCount++;
        headerTabIconColorRow = rowCount++;
        headerTabUnselectedIconColorRow = rowCount++;
        headerTabCounterColorRow = rowCount++;
        tabsCounterSizeRow = rowCount++;
        headerTabCounterBGColorRow = rowCount++;
        headerTabCounterSilentBGColorRow = rowCount++;
        tabsToBottomRow = rowCount++;
        tabsTextModeRow = rowCount++;
        tabsTextSizeRow = rowCount++;

        rowsSectionRow = rowCount++;
        rowsSection2Row = rowCount++;

        rowColorRow = rowCount++;
        rowGradientRow = rowCount++;
        rowGradientColorRow = rowCount++;
        //rowGradientListCheckRow = rowCount++;

        pinnedMsgBGColorRow = rowCount++;
        dividerColorRow = rowCount++;

        avatarRadiusRow = rowCount++;
        avatarSizeRow = rowCount++;
        avatarMarginLeftRow = rowCount++;
        hideStatusIndicatorCheckRow = rowCount++;
        favIndicatorColorRow = rowCount++;
        nameColorRow = rowCount++;
        unknownNameColorRow = rowCount++;
        nameSizeRow = rowCount++;
        groupNameColorRow = rowCount++;
        groupNameSizeRow = rowCount++;
        groupIconColorRow = rowCount++;
        muteColorRow = rowCount++;
        checksColorRow = rowCount++;

        messageColorRow = rowCount++;
        messageSizeRow = rowCount++;
        memberColorRow = rowCount++;
        mediaColorRow = rowCount++;
        typingColorRow = rowCount++;
        timeColorRow = rowCount++;
        timeSizeRow = rowCount++;
        countColorRow = rowCount++;
        countSizeRow = rowCount++;
        countBGColorRow = rowCount++;
        //countSilentColorRow = rowCount++;
        countSilentBGColorRow = rowCount++;

        floatingPencilColorRow = rowCount++;
        floatingBGColorRow = rowCount++;

        highlightSearchColorRow = rowCount++;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
        showPrefix = preferences.getBoolean("chatsShowPrefix", true);
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
            actionBar.setTitle(LocaleController.getString("MainScreen", R.string.MainScreen));

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
                    editor.putBoolean("chatsShowPrefix", showPrefix).apply();
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
                    //int defColor = themePrefs.getInt("themeColor", AndroidUtilities.defColor);
                    //int darkColor = AndroidUtilities.getIntDarkerColor("themeColor", 0x15);
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
                                Theme.chatsHeaderColor = color;
                                commitInt( key, color);
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateDialogsTheme, Theme.UPDATE_DIALOGS_HEADER_COLOR);
                            }
                        }, Theme.chatsHeaderColor, CENTER, 0, false);
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
                                Theme.chatsHeaderGradientColor = color;
                                commitInt( key, color);
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateDialogsTheme, Theme.UPDATE_DIALOGS_HEADER_COLOR);
                            }
                        }, Theme.chatsHeaderGradientColor , CENTER, 0, true);
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
                                Theme.chatsHeaderTitleColor = color;
                                commitInt(key, color);
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateDialogsTheme, Theme.UPDATE_DIALOGS_HEADER_COLOR);
                            }
                        },themePrefs.getInt(key, 0xffffffff), CENTER, 0, true);
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
                                Theme.chatsHeaderIconsColor = color;
                                commitInt(key, color);
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateDialogsTheme, Theme.UPDATE_DIALOGS_HEADER_COLOR);
                            }
                        },themePrefs.getInt(key, Theme.chatsHeaderIconsColor), CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == headerTabIconColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatsHeaderTabIconColor = color;
                                commitInt(key, color);
                                SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
                                Theme.chatsHeaderTabUnselectedIconColor = themePrefs.getInt("chatsHeaderTabUnselectedIconColor", AndroidUtilities.getIntAlphaColor("chatsHeaderTabIconColor", Theme.chatsHeaderIconsColor, 0.35f));
                                //commitInt("chatsHeaderTabUnselectedIconColor", Theme.chatsHeaderTabUnselectedIconColor);
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateDialogsTheme, Theme.UPDATE_DIALOGS_HEADER_COLOR);
                            }
                        }, Theme.chatsHeaderTabIconColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == headerTabUnselectedIconColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatsHeaderTabUnselectedIconColor = color;
                                commitInt(key, color);
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateDialogsTheme, Theme.UPDATE_DIALOGS_HEADER_COLOR);
                            }
                        }, Theme.chatsHeaderTabUnselectedIconColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == headerTabCounterColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatsTabCounterColor = color;
                                commitInt(key, color);
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateDialogsTheme, Theme.UPDATE_DIALOGS_HEADER_COLOR);
                            }
                        }, Theme.chatsTabCounterColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == headerTabCounterBGColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatsTabCounterBGColor = color;
                                commitInt(key, color);
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateDialogsTheme, Theme.UPDATE_DIALOGS_HEADER_COLOR);
                            }
                        }, Theme.chatsTabCounterBGColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == headerTabCounterSilentBGColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatsTabCounterSilentBGColor = color;
                                commitInt(key, color);
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateDialogsTheme, Theme.UPDATE_DIALOGS_HEADER_COLOR);
                            }
                        }, Theme.chatsTabCounterSilentBGColor, CENTER, 0, true);
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
                                Theme.chatsRowColor = color;
                                commitInt( key, color);
                                //NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload);
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateDialogsTheme, Theme.UPDATE_DIALOGS_ROW_COLOR);
                            }
                        }, Theme.chatsRowColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == tabsBGColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatsTabsBGColor = color;
                                commitInt( key, color);
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateDialogsTheme, Theme.UPDATE_DIALOGS_HEADER_COLOR);
                            }

                        }, Theme.chatsTabsBGColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == favIndicatorColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatsFavIndicatorColor = color;
                                commitInt( key, color);
                            }

                        }, Theme.chatsFavIndicatorColor, CENTER, 0, true);
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
                                Theme.chatsRowGradientColor = color;
                                commitInt(key, color);
                                //NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload);
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateDialogsTheme, Theme.UPDATE_DIALOGS_ROW_COLOR);
                            }
                        }, Theme.chatsRowGradientColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == rowGradientListCheckRow) {
                        boolean b = themePrefs.getBoolean( key, false);
                        SharedPreferences.Editor editor = themePrefs.edit();
                        editor.putBoolean(key, !b);
                        editor.commit();
                        if (view instanceof TextCheckCell) {
                            ((TextCheckCell) view).setChecked(!b);
                        }

                        //NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload);
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateDialogsTheme, Theme.UPDATE_DIALOGS_ROW_COLOR);
                    } else if (i == pinnedMsgBGColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatsPinnedMsgBGColor = color;
                                commitInt( key, color);
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateDialogsTheme, Theme.UPDATE_DIALOGS_HEADER_COLOR);
                            }

                        }, Theme.chatsPinnedMsgBGColor, CENTER, 0, true);
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
                                Theme.chatsDividerColor = color;
                                commitInt( key, color);
                            }

                        },themePrefs.getInt( key, 0xffdcdcdc), CENTER, 0, true);
                        colorDialog.show();
                    } /*else if (i == usernameTitleRow) {
                        boolean b = themePrefs.getBoolean( key, true);
                        SharedPreferences.Editor editor = themePrefs.edit();
                        editor.putBoolean( key, !b);
                        editor.commit();
                        if (view instanceof TextCheckCell) {
                            ((TextCheckCell) view).setChecked(!b);
                        }
                    }*/ else if (i == headerTitleRow) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("HeaderTitle", R.string.HeaderTitle));
                        int user_id = UserConfig.getClientUserId();
                        TLRPC.User user = MessagesController.getInstance().getUser(user_id);
                        List<CharSequence> array = new ArrayList<>();
                        array.add( LocaleController.getString("AppName", R.string.AppName));
                        array.add( LocaleController.getString("ShortAppName", R.string.ShortAppName) );
                        String usr = "";
                        if (user != null && (user.first_name != null || user.last_name != null)) {
                            usr = ContactsController.formatName(user.first_name, user.last_name);
                            array.add(usr);
                        }
                        if (user != null && user.username != null && user.username.length() != 0) {
                            usr = "@" + user.username;
                            array.add(usr);
                        }
                        array.add("");
                        String[] simpleArray = new String[ array.size() ];
                        array.toArray( new String[ array.size() ]);
                        builder.setItems(array.toArray(simpleArray), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
                                themePrefs.edit().putInt("chatsHeaderTitle", which).commit();
                                if (listView != null) {
                                    listView.invalidateViews();
                                }
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 11);
                            }
                        });
                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        showDialog(builder.create());
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
                                themePrefs.edit().putInt("chatsHeaderGradient", which).commit();
                                Theme.chatsHeaderGradient = which;
                                //NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload);
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateDialogsTheme, Theme.UPDATE_DIALOGS_HEADER_COLOR);
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
                                Theme.chatsRowGradient = which;
                                themePrefs.edit().putInt("chatsRowGradient", Theme.chatsRowGradient).commit();

                                //NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload);
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateDialogsTheme, Theme.UPDATE_DIALOGS_ROW_COLOR);
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
                                Theme.chatsNameColor = color;
                                commitInt( key, color);
                            }
                        }, Theme.chatsNameColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == groupNameColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt( key, color);
                            }

                        },themePrefs.getInt( key, Theme.chatsNameColor), CENTER, 0, true);

                        colorDialog.show();
                    } else if (i == unknownNameColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt( key, color);
                            }

                        },themePrefs.getInt( key, Theme.chatsNameColor), CENTER, 0, true);

                        colorDialog.show();
                    } else if (i == groupIconColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt( key, color);
                            }

                        },themePrefs.getInt( key, themePrefs.getInt("chatsGroupNameColor", 0xff000000)), CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == muteColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt( key, color);
                            }

                        },themePrefs.getInt( key, 0xffa8a8a8), CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == checksColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatsChecksColor = color;
                                commitInt( key, color);
                            }

                        },themePrefs.getInt( key, Theme.defColor), CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == messageColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatsMessageColor = color;
                                commitInt( key, color);
                            }
                        },themePrefs.getInt( key, 0xff808080), CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == highlightSearchColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt( key, color);
                            }

                        },themePrefs.getInt( key, Theme.lightColor), CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == memberColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatsMemberColor = color;
                                commitInt( key, color);
                            }

                        },themePrefs.getInt( key, Theme.darkColor), CENTER, 0, true);

                        colorDialog.show();
                    } else if (i == mediaColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatsMediaColor = color;
                                commitInt( "chatsMediaColor", color);
                            }

                        },themePrefs.getInt( "chatsMediaColor", themePrefs.getInt("chatsMemberColor", Theme.darkColor)), CENTER, 0, true);

                        colorDialog.show();
                    } else if (i == typingColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt( key, color);
                            }

                        },themePrefs.getInt( key, Theme.defColor), CENTER, 0, true);

                        colorDialog.show();
                    } else if (i == timeColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt( key, color);
                            }

                        },themePrefs.getInt( key, 0xff999999), CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == countColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt( key, color);
                            }

                        },themePrefs.getInt( key, 0xffffffff), CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == countBGColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.dialogs_countPaint.setColor(color);
                                commitInt( key, color);
                            }

                        },themePrefs.getInt( key, Theme.defColor), CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == countSilentBGColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.dialogs_countGrayPaint.setColor(color);
                                commitInt( key, color);
                            }

                        },themePrefs.getInt( key, themePrefs.getInt("chatsCountBGColor", 0xffb9b9b9)), CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == avatarRadiusRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("AvatarRadius", R.string.AvatarRadius));
                        final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                        //final int currentValue = themePrefs.getInt( key, 32);
                        numberPicker.setMinValue(1);
                        numberPicker.setMaxValue(32);
                        numberPicker.setValue(Theme.chatsAvatarRadius);
                        builder.setView(numberPicker);

                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (numberPicker.getValue() != Theme.chatsAvatarRadius) {
                                    Theme.chatsAvatarRadius = numberPicker.getValue();
                                    commitInt( key, numberPicker.getValue());
                                }
                            }
                        });

                        showDialog(builder.create());
                    } else if (i == tabsTextSizeRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("TabsTextSize", R.string.TabsTextSize));
                        final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                        numberPicker.setMinValue(8);
                        numberPicker.setMaxValue(18);
                        numberPicker.setValue(Theme.chatsTabsTextSize);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (numberPicker.getValue() != Theme.chatsTabsTextSize) {
                                    Theme.chatsTabsTextSize = Theme.plusTabsTextSize = numberPicker.getValue();
                                    commitInt(key, Theme.chatsTabsTextSize);
                                    SharedPreferences.Editor editorPlus = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE).edit();
                                    editorPlus.putInt("tabsTextSize", Theme.chatsTabsTextSize);
                                    editorPlus.apply();
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 15);
                                }
                            }
                        });
                        showDialog(builder.create());
                    } else if (i == tabsCounterSizeRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("CountSize", R.string.CountSize));
                        final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                        //final int currentValue = themePrefs.getInt( key, 11);
                        numberPicker.setMinValue(8);
                        numberPicker.setMaxValue(14);
                        numberPicker.setValue(Theme.chatsTabCounterSize);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (numberPicker.getValue() != Theme.chatsTabCounterSize) {
                                    Theme.chatsTabCounterSize = numberPicker.getValue();
                                    commitInt( key, Theme.chatsTabCounterSize);
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 15);
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
                        //final int currentValue = themePrefs.getInt( key, 52);
                        numberPicker.setMinValue(0);
                        numberPicker.setMaxValue(72);
                        numberPicker.setValue(Theme.chatsAvatarSize);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (numberPicker.getValue() != Theme.chatsAvatarSize) {
                                    Theme.chatsAvatarSize = numberPicker.getValue();
                                    commitInt( key, numberPicker.getValue());
                                }
                            }
                        });
                        showDialog(builder.create());
                    } else if (i == avatarMarginLeftRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("AvatarMarginLeft", R.string.AvatarMarginLeft));
                        final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                        //final int currentValue = themePrefs.getInt( key, AndroidUtilities.isTablet() ? 13 : 9);
                        numberPicker.setMinValue(0);
                        numberPicker.setMaxValue(18);
                        numberPicker.setValue(Theme.chatsAvatarMarginLeft);
                        builder.setView(numberPicker);

                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (numberPicker.getValue() != Theme.chatsAvatarMarginLeft) {
                                    Theme.chatsAvatarMarginLeft = numberPicker.getValue();
                                    commitInt(key, numberPicker.getValue());
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
                        //final int currentValue = themePrefs.getInt( key, 17);
                        numberPicker.setMinValue(12);
                        numberPicker.setMaxValue(30);
                        numberPicker.setValue(Theme.chatsNameSize);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (numberPicker.getValue() != Theme.chatsNameSize) {
                                    Theme.chatsNameSize = numberPicker.getValue();
                                    commitInt( key, numberPicker.getValue());
                                }
                            }
                        }).create();

                        //dialog.show();
                        //Button btn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                        //btn.setTextColor(0xff0000ff);
                        showDialog(builder.create());

                    } else if (i == groupNameSizeRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("GroupNameSize", R.string.GroupNameSize));
                        final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                        final int currentValue = themePrefs.getInt( key, Theme.chatsNameSize);
                        numberPicker.setMinValue(12);
                        numberPicker.setMaxValue(30);
                        numberPicker.setValue(currentValue);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (numberPicker.getValue() != currentValue) {
                                    commitInt( key, numberPicker.getValue());
                                }
                            }
                        }).create();
                        showDialog(builder.create());

                    } else if (i == messageSizeRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("MessageSize", R.string.MessageSize));
                        final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                        final int currentValue = themePrefs.getInt( key, 16);
                        numberPicker.setMinValue(12);
                        numberPicker.setMaxValue(30);
                        numberPicker.setValue(currentValue);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(numberPicker.getValue() != currentValue){
                                    commitInt( key, numberPicker.getValue());
                                }
                            }
                        });
                        showDialog(builder.create());
                    } else if (i == timeSizeRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("TimeDateSize", R.string.TimeDateSize));
                        final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                        final int currentValue = themePrefs.getInt( key, 13);
                        numberPicker.setMinValue(5);
                        numberPicker.setMaxValue(25);
                        numberPicker.setValue(currentValue);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (numberPicker.getValue() != currentValue) {
                                    commitInt(key, numberPicker.getValue());
                                }
                            }
                        });
                        showDialog(builder.create());
                    } else if (i == countSizeRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("CountSize", R.string.CountSize));
                        final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                        //final int currentValue = themePrefs.getInt( key, 13);
                        numberPicker.setMinValue(8);
                        numberPicker.setMaxValue(20);
                        numberPicker.setValue(Theme.chatsCountSize);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (numberPicker.getValue() != Theme.chatsCountSize) {
                                    Theme.chatsCountSize = numberPicker.getValue();
                                    commitInt(key, numberPicker.getValue());
                                }
                            }
                        });
                        showDialog(builder.create());
                    } else if (i == floatingPencilColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatsFloatingPencilColor = color;
                                commitInt( key, color);
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateDialogsTheme, Theme.UPDATE_DIALOGS_HEADER_COLOR);
                            }
                        },themePrefs.getInt( key, 0xffffffff), CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == floatingBGColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatsFloatingBGColor = color;
                                commitInt( key, color);
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateDialogsTheme, Theme.UPDATE_DIALOGS_HEADER_COLOR);
                            }
                        },themePrefs.getInt( key, Theme.defColor), CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == hideStatusIndicatorCheckRow) {
                        //boolean b = themePrefs.getBoolean( key, false);
                        Theme.chatsHideStatusIndicator = !Theme.chatsHideStatusIndicator;
                        SharedPreferences.Editor editor = themePrefs.edit();
                        editor.putBoolean( key, Theme.chatsHideStatusIndicator);
                        editor.commit();
                        if (view instanceof TextCheckCell) {
                            ((TextCheckCell) view).setChecked(Theme.chatsHideStatusIndicator);
                        }
                    } else if (i == tabsToBottomRow) {
                        Theme.chatsTabsToBottom = Theme.plusTabsToBottom = !Theme.chatsTabsToBottom;
                        SharedPreferences.Editor editor = themePrefs.edit();
                        editor.putBoolean( key, Theme.chatsTabsToBottom);
                        editor.apply();
                        SharedPreferences.Editor editorPlus = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE).edit();
                        editorPlus.putBoolean("tabsToBottom", Theme.chatsTabsToBottom);
                        editorPlus.apply();
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 14);
                        if (view instanceof TextCheckCell) {
                            ((TextCheckCell) view).setChecked(Theme.chatsTabsToBottom);
                        }
                    } else if (i == tabsTextModeRow) {
                        Theme.chatsTabTitlesMode = Theme.plusTabTitlesMode = !Theme.chatsTabTitlesMode;
                        SharedPreferences.Editor editor = themePrefs.edit();
                        editor.putBoolean(key, Theme.chatsTabTitlesMode);
                        editor.apply();
                        SharedPreferences.Editor editorPlus = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE).edit();
                        editorPlus.putBoolean("tabTitlesMode", Theme.chatsTabTitlesMode);
                        editorPlus.apply();
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 15);
                        if (view instanceof TextCheckCell) {
                            ((TextCheckCell) view).setChecked(Theme.chatsTabTitlesMode);
                        }
                    } else if (i == hideHeaderShadow) {
                        Theme.chatsHideHeaderShadow = !Theme.chatsHideHeaderShadow;
                        SharedPreferences.Editor editor = themePrefs.edit();
                        editor.putBoolean( key, Theme.chatsHideHeaderShadow);
                        editor.commit();
                        if (view instanceof TextCheckCell) {
                            ((TextCheckCell) view).setChecked(Theme.chatsHideHeaderShadow);
                        }
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateDialogsTheme, Theme.UPDATE_DIALOGS_HEADER_COLOR);
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
                    resetPref(view.getTag().toString());
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
        Theme.updateChatsColors();
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
            return  i == headerColorRow || i == headerGradientRow || (Theme.chatsHeaderGradient != 0 && i == headerGradientColorRow) ||
                    i == headerTitleColorRow || i == headerIconsColorRow || i == headerTabIconColorRow || i == headerTabUnselectedIconColorRow || i == headerTitleRow ||
                    i == headerTabCounterColorRow || i == headerTabCounterBGColorRow || i == headerTabCounterSilentBGColorRow || i == tabsCounterSizeRow ||
                    i == rowColorRow || i == rowGradientRow || (Theme.chatsRowGradient != 0 &&  i == rowGradientColorRow) || (Theme.chatsRowGradient != 0 && i == rowGradientListCheckRow) || i == dividerColorRow ||
                    i == avatarRadiusRow ||  i == avatarSizeRow ||   i == avatarMarginLeftRow || i == hideHeaderShadow || i == hideStatusIndicatorCheckRow || i == nameColorRow ||
                    i == groupNameColorRow || i == unknownNameColorRow || i == groupIconColorRow || i == muteColorRow || i == checksColorRow || i == nameSizeRow ||
                    i == groupNameSizeRow || i == messageColorRow || i == highlightSearchColorRow || i == memberColorRow || i == mediaColorRow || i == typingColorRow ||
                    i == messageSizeRow || i == timeColorRow || i == timeSizeRow || i == countColorRow || i == countSizeRow || i == countBGColorRow /*|| i == countSilentColorRow*/ ||
                    i == countSilentBGColorRow || i == floatingPencilColorRow || i == floatingBGColorRow || i == tabsBGColorRow || i == favIndicatorColorRow || i == tabsToBottomRow ||
                    i == tabsTextModeRow || i == tabsTextSizeRow || i == pinnedMsgBGColorRow;
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
                prefix = "1.";
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
            //int defColor = themePrefs.getInt("themeColor", AndroidUtilities.defColor);
            //int darkColor = AndroidUtilities.getIntDarkerColor("themeColor", 0x15);
            if (type == 0) {
                if (view == null) {
                    view = new ShadowSectionCell(mContext);
                }
            } else if (type == 1) {
                if (view == null) {
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(0xffffffff);
                }
                if (i == headerSection2Row) {
                    ((HeaderCell) view).setText(prefix + LocaleController.getString("Header", R.string.Header));
                } else if (i == rowsSection2Row) {
                    ((HeaderCell) view).setText(prefix + LocaleController.getString("ChatsList", R.string.ChatsList));
                }
            } else if (type == 2) {
                if (view == null) {
                    view = new TextSettingsCell(mContext);
                }
                TextSettingsCell textCell = (TextSettingsCell) view;
                if (i == avatarRadiusRow) {
                    textCell.setTag("chatsAvatarRadius");
                    textCell.setTextAndValue(prefix + LocaleController.getString("AvatarRadius", R.string.AvatarRadius), String.format("%d", Theme.chatsAvatarRadius), true);
                } else if (i == tabsCounterSizeRow) {
                    textCell.setTag("chatsHeaderTabCounterSize");
                    textCell.setTextAndValue(prefix + LocaleController.getString("CountSize", R.string.CountSize), String.format("%d", Theme.chatsTabCounterSize), true);
                } else if (i == avatarSizeRow) {
                    textCell.setTag("chatsAvatarSize");
                    textCell.setTextAndValue(prefix + LocaleController.getString("AvatarSize", R.string.AvatarSize), String.format("%d", Theme.chatsAvatarSize), true);
                } else if (i == avatarMarginLeftRow) {
                    textCell.setTag("chatsAvatarMarginLeft");
                    textCell.setTextAndValue(prefix + LocaleController.getString("AvatarMarginLeft", R.string.AvatarMarginLeft), String.format("%d", Theme.chatsAvatarMarginLeft), true);
                } else if (i == nameSizeRow) {
                    textCell.setTag("chatsNameSize");
                    textCell.setTextAndValue(prefix + LocaleController.getString("NameSize", R.string.NameSize), String.format("%d", Theme.chatsNameSize), true);
                } else if (i == groupNameSizeRow) {
                    textCell.setTag("chatsGroupNameSize");
                    int size = themePrefs.getInt("chatsGroupNameSize", Theme.chatsNameSize);
                    textCell.setTextAndValue(prefix + LocaleController.getString("GroupNameSize", R.string.GroupNameSize), String.format("%d", size), true);
                } else if (i == messageSizeRow) {
                    textCell.setTag("chatsMessageSize");
                    int size = themePrefs.getInt("chatsMessageSize", AndroidUtilities.isTablet() ? 18 : 16);
                    textCell.setTextAndValue(prefix + LocaleController.getString("MessageSize", R.string.MessageSize), String.format("%d", size), true);
                } else if (i == timeSizeRow) {
                    textCell.setTag("chatsTimeSize");
                    int size = themePrefs.getInt("chatsTimeSize", AndroidUtilities.isTablet() ? 15 : 13);
                    textCell.setTextAndValue(prefix + LocaleController.getString("TimeDateSize", R.string.TimeDateSize), String.format("%d", size), true);
                } else if (i == countSizeRow) {
                    textCell.setTag("chatsCountSize");
                    textCell.setTextAndValue(prefix + LocaleController.getString("CountSize", R.string.CountSize), String.format("%d", Theme.chatsCountSize), true);
                } else if (i == tabsTextSizeRow) {
                    textCell.setTag("chatsTabsTextSize");
                    textCell.setTextAndValue(prefix + LocaleController.getString("TabsTextSize", R.string.TabsTextSize), String.format("%d", Theme.chatsTabsTextSize), false);
                }
            } else if (type == 3){
                if (view == null) {
                    view = new TextColorCell(mContext);
                }

                TextColorCell textCell = (TextColorCell) view;

                if (i == headerColorRow) {
                    textCell.setTag("chatsHeaderColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("HeaderColor", R.string.HeaderColor), Theme.chatsHeaderColor, false);
                } else if (i == headerGradientColorRow) {
                    textCell.setTag("chatsHeaderGradientColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("RowGradientColor", R.string.RowGradientColor), Theme.chatsHeaderGradientColor, true);
                } else if (i == headerTitleColorRow) {
                    textCell.setTag("chatsHeaderTitleColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("HeaderTitleColor", R.string.HeaderTitleColor), Theme.chatsHeaderTitleColor, true);
                } else if (i == headerIconsColorRow) {
                    textCell.setTag("chatsHeaderIconsColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("HeaderIconsColor", R.string.HeaderIconsColor), Theme.chatsHeaderIconsColor, true);
                } else if (i == headerTabIconColorRow) {
                    Theme.chatsHeaderTabIconColor = themePrefs.getInt("chatsHeaderTabIconColor", Theme.chatsHeaderIconsColor);
                    textCell.setTag("chatsHeaderTabIconColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("HeaderTabIconColor", R.string.HeaderTabIconColor), Theme.chatsHeaderTabIconColor, true);
                } else if (i == headerTabUnselectedIconColorRow) {
                    Theme.chatsHeaderTabUnselectedIconColor = themePrefs.getInt("chatsHeaderTabUnselectedIconColor", AndroidUtilities.getIntAlphaColor("chatsHeaderTabIconColor", Theme.chatsHeaderIconsColor, 0.35f));
                    textCell.setTag("chatsHeaderTabUnselectedIconColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("HeaderTabUnselectedIconColor", R.string.HeaderTabUnselectedIconColor), Theme.chatsHeaderTabUnselectedIconColor, true);
                } else if (i == headerTabCounterColorRow) {
                    textCell.setTag("chatsHeaderTabCounterColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("HeaderTabCounterColor", R.string.HeaderTabCounterColor), Theme.chatsTabCounterColor, true);
                } else if (i == headerTabCounterBGColorRow) {
                    textCell.setTag("chatsHeaderTabCounterBGColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("HeaderTabCounterBGColor", R.string.HeaderTabCounterBGColor), Theme.chatsTabCounterBGColor, true);
                } else if (i == headerTabCounterSilentBGColorRow) {
                    textCell.setTag("chatsHeaderTabCounterSilentBGColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("CountSilentBGColor", R.string.CountSilentBGColor), Theme.chatsTabCounterSilentBGColor, false);
                } else if (i == rowColorRow) {
                    textCell.setTag("chatsRowColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("RowColor", R.string.RowColor), Theme.chatsRowColor, false);
                } else if (i == tabsBGColorRow) {
                    textCell.setTag("chatsTabsBGColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("TabsBGColor", R.string.TabsBGColor), themePrefs.getInt("chatsTabsBGColor", Theme.defColor), true);
                } else if (i == favIndicatorColorRow) {
                    textCell.setTag("chatsFavIndicatorColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("FavIndicatorColor", R.string.FavIndicatorColor), themePrefs.getInt("chatsFavIndicatorColor", Theme.FAV_INDICATOR_COLOR_DEF), false);
                } else if (i == rowGradientColorRow) {
                    textCell.setTag("chatsRowGradientColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("RowGradientColor", R.string.RowGradientColor), Theme.chatsRowGradient == 0 ? 0x00000000 : Theme.chatsRowGradientColor, true);
                } else if (i == pinnedMsgBGColorRow) {
                    textCell.setTag("chatsPinnedMsgBGColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("PinnedMsgBgColor", R.string.PinnedMsgBgColor), themePrefs.getInt("chatsPinnedMsgBGColor", Theme.chatsRowColor), true);
                } else if (i == dividerColorRow) {
                    textCell.setTag("chatsDividerColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("DividerColor", R.string.DividerColor), Theme.chatsDividerColor, true);
                } else if (i == nameColorRow) {
                    textCell.setTag("chatsNameColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("NameColor", R.string.NameColor), Theme.chatsNameColor, true);
                } else if (i == groupNameColorRow) {
                    textCell.setTag("chatsGroupNameColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("GroupNameColor", R.string.GroupNameColor), themePrefs.getInt("chatsGroupNameColor", Theme.chatsNameColor), true);
                } else if (i == unknownNameColorRow) {
                    textCell.setTag("chatsUnknownNameColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("UnknownNameColor", R.string.UnknownNameColor), themePrefs.getInt("chatsUnknownNameColor", Theme.chatsNameColor), true);
                } else if (i == groupIconColorRow) {
                    textCell.setTag("chatsGroupIconColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("GroupIconColor", R.string.GroupIconColor), themePrefs.getInt("chatsGroupIconColor", themePrefs.getInt("chatsGroupNameColor", 0xff000000)), true);
                } else if (i == muteColorRow) {
                    textCell.setTag("chatsMuteColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("MuteColor", R.string.MuteColor), themePrefs.getInt("chatsMuteColor", 0xffa8a8a8), true);
                } else if (i == checksColorRow) {
                    textCell.setTag("chatsChecksColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("ChecksColor", R.string.ChecksColor), Theme.chatsChecksColor, true);
                } else if (i == messageColorRow) {
                    textCell.setTag("chatsMessageColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("MessageColor", R.string.MessageColor), Theme.chatsMessageColor, true);
                } else if (i == memberColorRow) {
                    textCell.setTag("chatsMemberColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("MemberColor", R.string.MemberColor), Theme.chatsMemberColor, true);
                } else if (i == mediaColorRow) {
                    textCell.setTag("chatsMediaColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("MediaColor", R.string.MediaColor), themePrefs.getInt("chatsMediaColor", Theme.chatsMemberColor), true);
                } else if (i == typingColorRow) {
                    textCell.setTag("chatsTypingColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("TypingColor", R.string.TypingColor), themePrefs.getInt(textCell.getTag().toString(), Theme.defColor), true);
                } else if (i == timeColorRow) {
                    textCell.setTag("chatsTimeColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("TimeDateColor", R.string.TimeDateColor), themePrefs.getInt("chatsTimeColor", 0xff999999), true);
                } else if (i == countColorRow) {
                    textCell.setTag("chatsCountColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("CountColor", R.string.CountColor), themePrefs.getInt("chatsCountColor", 0xffffffff), true);
                } else if (i == countBGColorRow) {
                    textCell.setTag("chatsCountBGColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("CountBGColor", R.string.CountBGColor), themePrefs.getInt("chatsCountBGColor", Theme.defColor), true);
                } /*else if (i == countSilentColorRow) {
                    textCell.setTag("chatsCountSilentColor");
                    textCell.setTextAndColor(LocaleController.getString("CountSilentColor", R.string.CountSilentColor), themePrefs.getInt("chatsCountSilentColor", themePrefs.getInt("chatsCountColor", 0xffffffff)), true);
                }*/ else if (i == countSilentBGColorRow) {
                    textCell.setTag("chatsCountSilentBGColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("CountSilentBGColor", R.string.CountSilentBGColor), themePrefs.getInt("chatsCountSilentBGColor", themePrefs.getInt("chatsCountBGColor", 0xffb9b9b9)), true);
                } else if (i == floatingPencilColorRow) {
                    textCell.setTag("chatsFloatingPencilColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("FloatingPencilColor", R.string.FloatingPencilColor), Theme.chatsFloatingPencilColor, true);
                } else if (i == floatingBGColorRow) {
                    textCell.setTag("chatsFloatingBGColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("FloatingBGColor", R.string.FloatingBGColor), Theme.chatsFloatingBGColor, true);
                } else if (i == highlightSearchColorRow) {
                    textCell.setTag("chatsHighlightSearchColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("HighlightSearchColor", R.string.HighlightSearchColor), themePrefs.getInt("chatsHighlightSearchColor", Theme.lightColor), false);
                }
            } else if (type == 4) {
                if (view == null) {
                    view = new TextCheckCell(mContext);
                }
                TextCheckCell textCell = (TextCheckCell) view;
                if (i == rowGradientListCheckRow) {
                    textCell.setTag("chatsRowGradientListCheck");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("RowGradientList", R.string.RowGradientList), Theme.chatsRowGradient != 0 && themePrefs.getBoolean("chatsRowGradientListCheck", false), true);
                } else if (i == hideHeaderShadow) {
                    textCell.setTag("chatsHideHeaderShadow");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("HideHeaderShadow", R.string.HideHeaderShadow), Theme.chatsHideHeaderShadow, true);
                } else if (i == hideStatusIndicatorCheckRow) {
                    textCell.setTag("chatsHideStatusIndicator");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("HideStatusIndicator", R.string.HideStatusIndicator), Theme.chatsHideStatusIndicator, true);
                } else if (i == tabsToBottomRow) {
                    textCell.setTag("chatsTabsToBottom");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("TabsToBottom", R.string.TabsToBottom), Theme.chatsTabsToBottom, true);
                } else if (i == tabsTextModeRow) {
                    textCell.setTag("chatsTabTitlesMode");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("ShowTabTitle", R.string.ShowTabTitle), Theme.chatsTabTitlesMode, true);
                }
            } else if (type == 5) {
                if (view == null) {
                    view = new TextDetailSettingsCell(mContext);
                }

                TextDetailSettingsCell textCell = (TextDetailSettingsCell) view;
                if (i == headerTitleRow) {
                    textCell.setTag("chatsHeaderTitle");
                    textCell.setMultilineDetail(false);
                    int value = themePrefs.getInt("chatsHeaderTitle", 0);
                    int user_id = UserConfig.getClientUserId();
                    TLRPC.User user = MessagesController.getInstance().getUser(user_id);
                    String text;
                    if (user != null && user.username != null && user.username.length() != 0) {
                        text = "@" + user.username;
                    } else {
                        text = "-";
                    }
                    if (value == 0) {
                        textCell.setTextAndValue(prefix + LocaleController.getString("HeaderTitle", R.string.HeaderTitle), LocaleController.getString("AppName", R.string.AppName), true);
                    } else if (value == 1) {
                        textCell.setTextAndValue(prefix + LocaleController.getString("HeaderTitle", R.string.HeaderTitle), LocaleController.getString("ShortAppName", R.string.ShortAppName), true);
                    } else if (value == 2) {
                        textCell.setTextAndValue(prefix + LocaleController.getString("HeaderTitle", R.string.HeaderTitle), ContactsController.formatName(user.first_name, user.last_name), true);
                    } else if (value == 3) {
                        textCell.setTextAndValue(prefix + LocaleController.getString("HeaderTitle", R.string.HeaderTitle), text, true);
                    } else if (value == 4) {
                        textCell.setTextAndValue(prefix + LocaleController.getString("HeaderTitle", R.string.HeaderTitle), "", true);
                    }
                } else if(i == headerGradientRow){
                    textCell.setTag("chatsHeaderGradient");
                    textCell.setMultilineDetail(false);
                    int value = Theme.chatsHeaderGradient;
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
                    textCell.setTag("chatsRowGradient");
                    textCell.setMultilineDetail(false);
                    if (Theme.chatsRowGradient == 0) {
                        textCell.setTextAndValue(prefix + LocaleController.getString("RowGradient", R.string.RowGradient), LocaleController.getString("RowGradientDisabled", R.string.RowGradientDisabled), false);
                    } else if (Theme.chatsRowGradient == 1) {
                        textCell.setTextAndValue(prefix + LocaleController.getString("RowGradient", R.string.RowGradient), LocaleController.getString("RowGradientTopBottom", R.string.RowGradientTopBottom), false);
                    } else if (Theme.chatsRowGradient == 2) {
                        textCell.setTextAndValue(prefix + LocaleController.getString("RowGradient", R.string.RowGradient), LocaleController.getString("RowGradientLeftRight", R.string.RowGradientLeftRight), false);
                    } else if (Theme.chatsRowGradient == 3) {
                        textCell.setTextAndValue(prefix + LocaleController.getString("RowGradient", R.string.RowGradient), LocaleController.getString("RowGradientTLBR", R.string.RowGradientTLBR), false);
                    } else if (Theme.chatsRowGradient == 4) {
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
            } else if ( i == headerSection2Row || i == rowsSection2Row ) {
                return 1;
            } else if ( i == avatarRadiusRow || i == avatarSizeRow || i == avatarMarginLeftRow || i == nameSizeRow || i == groupNameSizeRow ||  i == messageSizeRow ||
                        i == timeSizeRow || i == countSizeRow || i == tabsCounterSizeRow || i == tabsTextSizeRow) {
                return 2;
            } else if ( i == headerColorRow || i == headerGradientColorRow || i == headerTitleColorRow || i == headerIconsColorRow || i == headerTabIconColorRow ||
                        i == headerTabUnselectedIconColorRow || i == headerTabCounterColorRow || i == headerTabCounterBGColorRow || i == headerTabCounterSilentBGColorRow ||
                        i == rowColorRow || i == rowGradientColorRow || i == dividerColorRow || i == nameColorRow || i == groupNameColorRow || i == unknownNameColorRow ||
                        i == groupIconColorRow || i == muteColorRow || i == checksColorRow || i == messageColorRow || i == highlightSearchColorRow || i == memberColorRow ||
                        i == mediaColorRow || i == typingColorRow || i == timeColorRow || i == countColorRow || i == countBGColorRow /*|| i == countSilentColorRow*/ ||
                        i == countSilentBGColorRow || i == floatingPencilColorRow || i == floatingBGColorRow || i == tabsBGColorRow || i == favIndicatorColorRow ||
                        i == pinnedMsgBGColorRow) {
                return 3;
            } else if (i == rowGradientListCheckRow || i == hideHeaderShadow || i == hideStatusIndicatorCheckRow || i == tabsToBottomRow || i == tabsTextModeRow) {
                return 4;
            } else if (i == headerTitleRow || i == headerGradientRow || i == rowGradientRow) {
                return 5;
            } else {
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
