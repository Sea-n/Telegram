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
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
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
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
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

import static org.telegram.ui.ActionBar.Theme.chatStatusColor;
import static org.telegram.ui.ActionBar.Theme.getServiceMessageColor;
import static org.telegram.ui.ActionBar.Theme.lightColor;
import static org.telegram.ui.Components.ColorSelectorDialog.OnColorChangedListener;

public class ThemingChatActivity extends BaseFragment {

    private ListView listView;
    private ListAdapter listAdapter;

    private int headerSection2Row;
    private int muteColorRow;
    private int headerColorRow;
    private int headerIconsColorRow;
    private int headerAvatarRadiusRow;

    private int rowsSectionRow;
    private int rowsSection2Row;
    private int solidBGColorCheckRow;
    private int solidBGColorRow;
    private int rBubbleColorRow;
    private int lBubbleColorRow;
    private int rTextColorRow;
    private int rLinkColorRow;
    private int textSizeRow;
    private int lTextColorRow;
    private int lLinkColorRow;
    private int rTimeColorRow;
    private int lTimeColorRow;
    private int checksColorRow;
    private int dateBubbleColorRow;
    private int nameColorRow;
    private int nameSizeRow;
    private int statusColorRow;
    private int statusSizeRow;
    private int dateColorRow;
    private int dateSizeRow;
    private int timeSizeRow;
    private int editTextColorRow;
    private int editTextSizeRow;
    private int editTextBGColorRow;
    private int editTextIconsColorRow;
    private int emojiViewBGColorRow;
    private int emojiViewTabColorRow;
    private int emojiViewTabIconColorRow;
    private int sendColorRow;
    private int memberColorCheckRow;
    private int memberColorRow;
    private int forwardRightNameColorRow;
    private int forwardLeftNameColorRow;
    private int avatarRadiusRow;
    private int bubblesRow;
    private int avatarSizeRow;
    private int avatarAlignTopRow;
    private int ownAvatarAlignTopRow;
    private int avatarMarginLeftRow;
    private int contactNameColorRow;
    private int attachBGColorRow;
    private int attachTextColorRow;
    private int showContactAvatar;
    private int showOwnAvatar;
    private int showOwnAvatarGroup;
    private int showUsernameCheckRow;
    private int gradientBGRow;
    private int gradientBGColorRow;

    private int headerGradientRow;
    private int headerGradientColorRow;
    private int onlineColorRow;
    private int typingColorRow;

    private int editTextBGGradientRow;
    private int editTextBGGradientColorRow;
    private int attachBGGradientRow;
    private int attachBGGradientColorRow;
    private int emojiViewBGGradientRow;
    private int emojiViewBGGradientColorRow;

    private int quickBarColorRow;
    private int quickBarNamesColorRow;

    private int commandColorRow;
    private int commandColorCheckRow;

    private int hideStatusIndicatorCheckRow;

    private int checksRow;
    private int selectedMessageBGColorRow;

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
        //muteColorRow = rowCount++;

        nameSizeRow = rowCount++;
        nameColorRow = rowCount++;
        statusSizeRow = rowCount++;
        statusColorRow = rowCount++;
        onlineColorRow = rowCount++;
        typingColorRow = rowCount++;

        rowsSectionRow = rowCount++;
        rowsSection2Row = rowCount++;

        solidBGColorCheckRow = rowCount++;
        solidBGColorRow = rowCount++;

        gradientBGRow = rowCount++;
        gradientBGColorRow = rowCount++;

        showContactAvatar = rowCount++;
        avatarAlignTopRow = rowCount++;
        showOwnAvatar = rowCount++;
        showOwnAvatarGroup = rowCount++;
        ownAvatarAlignTopRow = rowCount++;
        avatarRadiusRow = rowCount++;
        avatarSizeRow = rowCount++;
        avatarMarginLeftRow = rowCount++;
        hideStatusIndicatorCheckRow = rowCount++;

        textSizeRow = rowCount++;
        rTextColorRow = rowCount++;
        rLinkColorRow = rowCount++;
        lTextColorRow = rowCount++;
        lLinkColorRow = rowCount++;
        selectedMessageBGColorRow = rowCount++;

        commandColorCheckRow = rowCount++;
        commandColorRow = rowCount++;

        timeSizeRow = rowCount++;
        rTimeColorRow = rowCount++;
        lTimeColorRow = rowCount++;
        checksColorRow = rowCount++;

        dateSizeRow = rowCount++;
        dateColorRow = rowCount++;

        bubblesRow = rowCount++;
        checksRow = rowCount++;
        rBubbleColorRow = rowCount++;
        lBubbleColorRow = rowCount++;
        dateBubbleColorRow = rowCount++;

        memberColorCheckRow = rowCount++;
        memberColorRow = rowCount++;
        contactNameColorRow = rowCount++;
        forwardRightNameColorRow = rowCount++;
        forwardLeftNameColorRow = rowCount++;

        showUsernameCheckRow = rowCount++;

        sendColorRow = rowCount++;
        editTextSizeRow = rowCount++;
        editTextColorRow = rowCount++;
        editTextBGColorRow = rowCount++;
        editTextBGGradientRow = rowCount++;
        editTextBGGradientColorRow = rowCount++;
        editTextIconsColorRow = rowCount++;

        attachBGColorRow = rowCount++;
        attachBGGradientRow = rowCount++;
        attachBGGradientColorRow = rowCount++;
        attachTextColorRow = rowCount++;

        emojiViewBGColorRow = rowCount++;
        emojiViewBGGradientRow = rowCount++;
        emojiViewBGGradientColorRow = rowCount++;
        emojiViewTabIconColorRow = rowCount++;
        emojiViewTabColorRow = rowCount++;

        quickBarColorRow = rowCount++;
        quickBarNamesColorRow = rowCount++;

        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
        showPrefix = preferences.getBoolean("chatShowPrefix", true);
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
            actionBar.setTitle(LocaleController.getString("ChatScreen", R.string.ChatScreen));

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
                    editor.putBoolean("chatShowPrefix", showPrefix).apply();
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
                    //int lightColor = AndroidUtilities.getIntDarkerColor("themeColor", -0x40);
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
                                Theme.chatHeaderColor = color;
                                commitInt("chatHeaderColor", color);
                            }
                        }, Theme.chatHeaderColor, CENTER, 0, false);

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
                                commitInt("chatHeaderGradientColor", color);
                            }

                        },themePrefs.getInt("chatHeaderGradientColor",Theme.defColor), CENTER, 0, false);
                        colorDialog.show();
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
                                themePrefs.edit().putInt("chatHeaderGradient", which).commit();
                                if (listView != null) {
                                    listView.invalidateViews();
                                }
                            }
                        });
                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        showDialog(builder.create());
                    } else if (i == editTextBGGradientRow) {
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
                                themePrefs.edit().putInt("chatEditTextBGGradient", which).commit();
                                if (listView != null) {
                                    listView.invalidateViews();
                                }
                            }
                        });
                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        showDialog(builder.create());
                    } else if (i == attachBGGradientRow) {
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
                                themePrefs.edit().putInt("chatAttachBGGradient", which).commit();
                                if (listView != null) {
                                    listView.invalidateViews();
                                }
                            }
                        });
                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        showDialog(builder.create());
                    } else if (i == emojiViewBGGradientRow) {
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
                                themePrefs.edit().putInt("chatEmojiViewBGGradient", which).commit();
                                if (listView != null) {
                                    listView.invalidateViews();
                                }
                            }
                        });
                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        showDialog(builder.create());
                    } else if (i == commandColorCheckRow) {
                        boolean b = themePrefs.getBoolean( key, false);
                        SharedPreferences.Editor editor = themePrefs.edit();
                        editor.putBoolean(key, !b);
                        editor.apply();
                        if (view instanceof TextCheckCell) {
                            ((TextCheckCell) view).setChecked(!b);
                        }
                        if (listView != null) {
                            listView.invalidateViews();
                        }

                    } else if (i == solidBGColorCheckRow) {
                        //boolean b = themePrefs.getBoolean( key, false);
                        Theme.chatSolidBGColorCheck = !Theme.chatSolidBGColorCheck;
                        SharedPreferences.Editor editor = themePrefs.edit();
                        editor.putBoolean(key, Theme.chatSolidBGColorCheck);
                        editor.apply();
                        Theme.reloadWallpaper();
                        if (view instanceof TextCheckCell) {
                            ((TextCheckCell) view).setChecked(Theme.chatSolidBGColorCheck);
                        }
                        if (listView != null) {
                            listView.invalidateViews();
                        }
                    } else if (i == memberColorCheckRow) {
                        boolean b = themePrefs.getBoolean( key, false);
                        Theme.chatMemberColorCheck = !Theme.chatMemberColorCheck;
                        SharedPreferences.Editor editor = themePrefs.edit();
                        editor.putBoolean( key, Theme.chatMemberColorCheck);
                        editor.apply();
                        if (view instanceof TextCheckCell) {
                            ((TextCheckCell) view).setChecked(Theme.chatMemberColorCheck);
                        }
                        if (listView != null) {
                            listView.invalidateViews();
                        }
                    } else if (i == showUsernameCheckRow) {
                        //boolean b = themePrefs.getBoolean( key, false);
                        Theme.chatShowUsernameCheck = !Theme.chatShowUsernameCheck;
                        SharedPreferences.Editor editor = themePrefs.edit();
                        editor.putBoolean( key, Theme.chatShowUsernameCheck);
                        editor.apply();
                        if (view instanceof TextCheckCell) {
                            ((TextCheckCell) view).setChecked(Theme.chatShowUsernameCheck);
                        }
                        if (listView != null) {
                            listView.invalidateViews();
                        }
                    } else if (i == avatarAlignTopRow) {
                        //boolean b = themePrefs.getBoolean( key, false);
                        Theme.chatAvatarAlignTop = !Theme.chatAvatarAlignTop;
                        SharedPreferences.Editor editor = themePrefs.edit();
                        editor.putBoolean( key, Theme.chatAvatarAlignTop);
                        editor.apply();
                        if (view instanceof TextCheckCell) {
                            ((TextCheckCell) view).setChecked(Theme.chatAvatarAlignTop);
                        }

                    } else if (i == ownAvatarAlignTopRow) {
                        //boolean b = themePrefs.getBoolean( key, false);
                        Theme.chatOwnAvatarAlignTop = !Theme.chatOwnAvatarAlignTop;
                        SharedPreferences.Editor editor = themePrefs.edit();
                        editor.putBoolean( key, Theme.chatOwnAvatarAlignTop);
                        editor.apply();
                        if (view instanceof TextCheckCell) {
                            ((TextCheckCell) view).setChecked(Theme.chatOwnAvatarAlignTop);
                        }

                    } else if (i == showContactAvatar) {
                        //boolean b = themePrefs.getBoolean( key, false);
                        Theme.chatShowContactAvatar = !Theme.chatShowContactAvatar;
                        SharedPreferences.Editor editor = themePrefs.edit();
                        editor.putBoolean( key, Theme.chatShowContactAvatar);
                        editor.apply();
                        if (view instanceof TextCheckCell) {
                            ((TextCheckCell) view).setChecked(Theme.chatShowContactAvatar);
                        }

                    } else if (i == showOwnAvatar) {
                        //boolean b = themePrefs.getBoolean( key, false);
                        Theme.chatShowOwnAvatar = !Theme.chatShowOwnAvatar;
                        SharedPreferences.Editor editor = themePrefs.edit();
                        editor.putBoolean( key, Theme.chatShowOwnAvatar);
                        editor.apply();
                        if (view instanceof TextCheckCell) {
                            ((TextCheckCell) view).setChecked(Theme.chatShowOwnAvatar);
                        }
                    } else if (i == showOwnAvatarGroup) {
                        //boolean b = themePrefs.getBoolean( key, false);
                        Theme.chatShowOwnAvatarGroup = !Theme.chatShowOwnAvatarGroup;
                        SharedPreferences.Editor editor = themePrefs.edit();
                        editor.putBoolean( key, Theme.chatShowOwnAvatarGroup);
                        editor.apply();
                        if (view instanceof TextCheckCell) {
                            ((TextCheckCell) view).setChecked(Theme.chatShowOwnAvatarGroup);
                        }
                    } else if (i == hideStatusIndicatorCheckRow) {
                        //boolean b = themePrefs.getBoolean( key, false);
                        Theme.chatHideStatusIndicator = !Theme.chatHideStatusIndicator;
                        SharedPreferences.Editor editor = themePrefs.edit();
                        editor.putBoolean( key, Theme.chatHideStatusIndicator);
                        editor.apply();
                        if (view instanceof TextCheckCell) {
                            ((TextCheckCell) view).setChecked(Theme.chatHideStatusIndicator);
                        }
                    } else if (i == solidBGColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt("chatSolidBGColor", color);
                                Theme.reloadWallpaper();
                                /*AndroidUtilities.runOnUIThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.wallpaperChanged);
                                    }
                                });*/
                            }

                        },themePrefs.getInt("chatSolidBGColor", 0xffffffff), CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == gradientBGColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt("chatGradientBGColor", color);
                                /*ApplicationLoader.reloadWallpaper();
                                AndroidUtilities.runOnUIThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.wallpaperChanged);
                                    }
                                });*/
                            }
                        },themePrefs.getInt( "chatGradientBGColor", 0xffffffff), CENTER, 0, false);
                        colorDialog.show();
                    } else if (i == gradientBGRow) {
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
                                themePrefs.edit().putInt("chatGradientBG", which).commit();
                                /*ApplicationLoader.reloadWallpaper();
                                AndroidUtilities.runOnUIThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.wallpaperChanged);
                                    }
                                });*/
                                if (listView != null) {
                                    listView.invalidateViews();
                                }
                            }
                        });
                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        showDialog(builder.create());
                    } else if (i == memberColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatMemberColor = color;
                                commitInt("chatMemberColor", color);
                            }
                        }, Theme.chatMemberColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == contactNameColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatContactNameColor = color;
                                commitInt("chatContactNameColor", color);
                            }
                        }, Theme.chatContactNameColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == forwardRightNameColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatForwardRColor = color;
                                commitInt(key, color);
                            }
                        }, Theme.chatForwardRColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == forwardLeftNameColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatForwardLColor = color;
                                commitInt(key, color);
                            }

                        }, Theme.chatForwardLColor, CENTER, 0, true);
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

                        },themePrefs.getInt( key, 0xffffffff), CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == rBubbleColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatRBubbleColor = color;

                                Theme.updateChatDrawablesColor();
                                commitInt(key, color);
                            }

                        }, Theme.chatRBubbleColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == lBubbleColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatLBubbleColor = color;
                                Theme.updateChatDrawablesColor();
                                commitInt(key, color);
                            }
                        }, Theme.chatLBubbleColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == rTextColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatRTextColor = color;
                                commitInt("chatRTextColor", color);
                            }

                        }, Theme.chatRTextColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == lTextColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatLTextColor = color;
                                commitInt("chatLTextColor", color);
                            }
                        }, Theme.chatLTextColor , CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == selectedMessageBGColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                int alpha = Color.alpha(color);
                                //Log.e("ThemingChat", "selectedMessageBGColorRow alpha " + alpha);
                                Theme.chatSelectedMsgBGColor = color;
                                commitInt("chatSelectedMsgBGColor", color);
                            }
                        },themePrefs.getInt("chatSelectedMsgBGColor", Theme.SELECTED_MDG_BACKGROUND_COLOR_DEF), CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == rLinkColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatRLinkColor = color;
                                commitInt("chatRLinkColor", color);
                            }
                        }, Theme.chatRLinkColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == lLinkColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatLLinkColor = color;
                                commitInt("chatLLinkColor", color);
                            }
                        }, Theme.chatLLinkColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == rTimeColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatRTimeColor = color;
                                commitInt("chatRTimeColor", color);
                            }
                        }, Theme.chatRTimeColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == lTimeColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatLTimeColor = color;
                                commitInt("chatLTimeColor", color);
                            }
                        }, Theme.chatLTimeColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == dateBubbleColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatDateBubbleColor = color;
                                commitInt("chatDateBubbleColor", color);
                            }

                        }, Theme.chatDateBubbleColor, CENTER, 0, true);

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
                                Theme.chatHeaderIconsColor = color;
                                commitInt( key, color);
                            }
                        }, Theme.chatHeaderIconsColor, CENTER, 0, true);
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
                                commitInt("chatNameColor", color);
                            }

                        },themePrefs.getInt("chatNameColor", 0xffffffff), CENTER, 0, false);

                        colorDialog.show();
                    } else if (i == sendColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }

                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                        li.inflate(R.layout.colordialog, null, false);

                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt("chatSendIconColor", color);
                            }

                        },themePrefs.getInt("chatSendIconColor", AndroidUtilities.getIntColor("chatEditTextIconsColor")), CENTER, 0, true);

                        colorDialog.show();
                    } else if (i == editTextColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }

                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                        li.inflate(R.layout.colordialog, null, false);

                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt("chatEditTextColor", color);
                            }

                        },themePrefs.getInt("chatEditTextColor", 0xff000000), CENTER, 0, false);

                        colorDialog.show();
                    } else if (i == editTextBGColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt("chatEditTextBGColor", color);
                            }

                        },themePrefs.getInt("chatEditTextBGColor", 0xffffffff), CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == editTextBGGradientColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt("chatEditTextBGGradientColor", color);
                            }

                        },themePrefs.getInt("chatEditTextBGGradientColor", 0xffffffff), CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == attachBGColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatAttachBGColor = color;
                                commitInt("chatAttachBGColor", color);
                            }
                        },themePrefs.getInt("chatAttachBGColor", 0xffffffff), CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == attachBGGradientColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt("chatAttachBGGradientColor", color);
                            }

                        },themePrefs.getInt("chatAttachBGGradientColor", 0xffffffff), CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == attachTextColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt("chatAttachTextColor", color);
                            }

                        }, themePrefs.getInt("chatAttachTextColor", 0xff757575), CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == editTextIconsColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatEditTextIconsColor = color;
                                commitInt( key, color);
                            }
                        }, Theme.chatEditTextIconsColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == emojiViewBGColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt("chatEmojiViewBGColor", color);
                            }

                        },themePrefs.getInt("chatEmojiViewBGColor", 0xfff5f6f7), CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == emojiViewBGGradientColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt("chatEmojiViewBGGradientColor", color);
                            }
                        },themePrefs.getInt("chatEmojiViewBGGradientColor", 0xfff5f6f7), CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == emojiViewTabIconColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt("chatEmojiViewTabIconColor", color);
                            }
                        },themePrefs.getInt("chatEmojiViewTabIconColor", 0xffa8a8a8), CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == emojiViewTabColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt("chatEmojiViewTabColor", color);
                            }
                        },themePrefs.getInt("chatEmojiViewTabColor", Theme.darkColor), CENTER, 0, true);

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
                                chatStatusColor = color;
                                commitInt("chatStatusColor", color);
                            }

                        }, chatStatusColor, CENTER, 0, false);

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
                                Theme.chatOnlineColor = color;
                                commitInt("chatOnlineColor", color);
                            }
                        }, Theme.chatOnlineColor, CENTER, 0, false);
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
                                Theme.chatTypingColor = color;
                                commitInt("chatTypingColor", color);
                            }
                        }, Theme.chatTypingColor, CENTER, 0, false);
                        colorDialog.show();
                    } else if (i == commandColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                commitInt("chatCommandColor", color);
                            }

                        },themePrefs.getInt("chatCommandColor",Theme.defColor), CENTER, 0, false);
                        colorDialog.show();
                    } else if (i == dateColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatDateColor = color;
                                commitInt("chatDateColor", color);
                            }
                        }, Theme.chatDateColor, CENTER, 0, false);
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
                                Theme.chatChecksColor = color;
                                //Theme.updateChatChecksColor();
                                commitInt("chatChecksColor", color);
                            }
                        }, Theme.chatChecksColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == headerAvatarRadiusRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("AvatarRadius", R.string.AvatarRadius));
                        final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                        final int currentValue = themePrefs.getInt( key, 32);
                        numberPicker.setMinValue(1);
                        numberPicker.setMaxValue(32);
                        numberPicker.setValue(currentValue);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (numberPicker.getValue() != currentValue) {
                                    commitInt( key, numberPicker.getValue());
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
                        //final int currentValue = Theme.chatAvatarRadius;//themePrefs.getInt( key, 32);
                        numberPicker.setMinValue(1);
                        numberPicker.setMaxValue(AndroidUtilities.isTablet() ? 35 : 32);
                        numberPicker.setValue(Theme.chatAvatarRadius);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (numberPicker.getValue() != Theme.chatAvatarRadius) {
                                    Theme.chatAvatarRadius = numberPicker.getValue();
                                    commitInt( key, numberPicker.getValue());
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
                        //final int currentValue = Theme.chatAvatarSize;
                        numberPicker.setMinValue(0);
                        numberPicker.setMaxValue(56);
                        numberPicker.setValue(Theme.chatAvatarSize);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (numberPicker.getValue() != Theme.chatAvatarSize) {
                                    Theme.chatAvatarSize = numberPicker.getValue();
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
                        //final int currentValue = Theme.chatAvatarMarginLeft;
                        numberPicker.setMinValue(0);
                        numberPicker.setMaxValue(12);
                        numberPicker.setValue(Theme.chatAvatarMarginLeft);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (numberPicker.getValue() != Theme.chatAvatarMarginLeft) {
                                    Theme.chatAvatarMarginLeft = numberPicker.getValue();
                                    commitInt( key, Theme.chatAvatarMarginLeft);
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
                        final int currentValue = themePrefs.getInt("chatNameSize", 18);
                        numberPicker.setMinValue(12);
                        numberPicker.setMaxValue(30);
                        numberPicker.setValue(currentValue);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (numberPicker.getValue() != currentValue) {
                                    commitInt("chatNameSize", numberPicker.getValue());
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
                        numberPicker.setMinValue(8);
                        numberPicker.setMaxValue(22);
                        numberPicker.setValue(Theme.chatStatusSize);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(numberPicker.getValue() != Theme.chatStatusSize){
                                    Theme.chatStatusSize = numberPicker.getValue();
                                    commitInt("chatStatusSize", numberPicker.getValue());
                                }
                            }
                        });
                        showDialog(builder.create());
                    } else if (i == textSizeRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("TextSize", R.string.TextSize));
                        final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                        final int currentValue = themePrefs.getInt("chatTextSize", 16);
                        numberPicker.setMinValue(12);
                        numberPicker.setMaxValue(30);
                        numberPicker.setValue(currentValue);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (numberPicker.getValue() != currentValue) {
                                    commitInt("chatTextSize", numberPicker.getValue());
                                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putInt("fons_size", numberPicker.getValue());
                                    MessagesController.getInstance().fontSize = numberPicker.getValue();
                                    editor.apply();
                                }
                            }
                        });
                        showDialog(builder.create());
                    } else if (i == timeSizeRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("TimeSize", R.string.TimeSize));
                        final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                        //final int currentValue = themePrefs.getInt("chatTimeSize", 12);
                        numberPicker.setMinValue(8);
                        numberPicker.setMaxValue(20);
                        numberPicker.setValue(Theme.chatTimeSize);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (numberPicker.getValue() != Theme.chatTimeSize) {
                                    Theme.chatTimeSize = numberPicker.getValue();
                                    commitInt("chatTimeSize", Theme.chatTimeSize);
                                }
                            }
                        });
                        showDialog(builder.create());
                    } else if (i == dateSizeRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("DateSize", R.string.DateSize));
                        final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                        //final int currentValue = themePrefs.getInt("chatDateSize", MessagesController.getInstance().fontSize - 2);
                        numberPicker.setMinValue(8);
                        numberPicker.setMaxValue(20);
                        numberPicker.setValue(Theme.chatDateSize);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (numberPicker.getValue() != Theme.chatDateSize) {
                                    Theme.chatDateSize = numberPicker.getValue();
                                    commitInt("chatDateSize", Theme.chatDateSize);
                                }
                            }
                        });
                        showDialog(builder.create());
                    }  else if (i == editTextSizeRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("EditTextSize", R.string.EditTextSize));
                        final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                        final int currentValue = themePrefs.getInt("chatEditTextSize", 18);
                        numberPicker.setMinValue(12);
                        numberPicker.setMaxValue(28);
                        numberPicker.setValue(currentValue);
                        builder.setView(numberPicker);
                        builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (numberPicker.getValue() != currentValue) {
                                    commitInt("chatEditTextSize", numberPicker.getValue());
                                }
                            }
                        });
                        showDialog(builder.create());
                    } else if (i == bubblesRow) {
                        Bundle args = new Bundle();
                        args.putInt("array_id", 0);
                        presentFragment(new ImageListActivity(args));
                    } else if (i == checksRow) {
                        Bundle args = new Bundle();
                        args.putInt("array_id", 1);
                        presentFragment(new ImageListActivity(args));
                    } else if (i == quickBarColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatQuickBarColor = color;
                                commitInt("chatQuickBarColor", color);
                            }
                        }, Theme.chatQuickBarColor, CENTER, 0, true);
                        colorDialog.show();
                    } else if (i == quickBarNamesColorRow) {
                        if (getParentActivity() == null) {
                            return;
                        }
                        LayoutInflater li = (LayoutInflater)getParentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        li.inflate(R.layout.colordialog, null, false);
                        ColorSelectorDialog colorDialog = new ColorSelectorDialog(getParentActivity(), new OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Theme.chatQuickBarNamesColor = color;
                                commitInt("chatQuickBarNamesColor", color);
                            }
                        }, Theme.chatQuickBarNamesColor, CENTER, 0, true);
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
                        resetPref("chatHeaderColor");
                    } else if (i == headerGradientRow) {
                        resetPref("chatHeaderGradient");
                    } else if (i == headerGradientColorRow) {
                        resetPref("chatHeaderGradientColor");
                    } else if (i == solidBGColorRow) {
                        resetPref("chatSolidBGColor");
                        Theme.reloadWallpaper();
                    } else if (i == gradientBGColorRow) {
                        resetPref("chatGradientBGColor");
                        Theme.reloadWallpaper();
                    } else if (i == gradientBGRow) {
                        resetPref("chatGradientBG");
                    } else if (i == memberColorRow) {
                        resetPref("chatMemberColor");
                    } else if (i == contactNameColorRow) {
                        resetPref("chatContactNameColor");
                    } else if (i == rTextColorRow) {
                        resetPref("chatRTextColor");
                    } else if (i == lTextColorRow) {
                        resetPref("chatLTextColor");
                    } else if (i == selectedMessageBGColorRow) {
                        resetPref("chatSelectedMsgBGColor");
                    } else if (i == nameColorRow) {
                        resetPref("chatNameColor");
                    } else if (i == nameSizeRow) {
                        resetPref("chatNameSize");
                    } else if (i == statusColorRow) {
                        resetPref("chatStatusColor");
                    } else if (i == onlineColorRow) {
                        resetPref("chatOnlineColor");
                    } else if (i == typingColorRow) {
                        resetPref("chatTypingColor");
                    } else if (i == statusSizeRow) {
                        resetPref("chatStatusSize");
                    } else if (i == rTimeColorRow) {
                        resetPref("chatRTimeColor");
                    } else if (i == lTimeColorRow) {
                        resetPref("chatLTimeColor");
                    } else if (i == commandColorRow) {
                        resetPref("chatCommandColor");
                    } else if (i == dateColorRow) {
                        resetPref("chatDateColor");
                    } else if (i == checksColorRow) {
                        resetPref("chatChecksColor");
                    } else if (i == textSizeRow) {
                        resetInt("chatTextSize", 16);
                    } else if (i == timeSizeRow) {
                        resetPref("chatTimeSize");
                    } else if (i == dateSizeRow) {
                        resetPref("chatDateSize");
                    } else if (i == dateBubbleColorRow) {
                        resetPref("chatDateBubbleColor");
                    } else if (i == sendColorRow) {
                        resetPref("chatSendIconColor");
                    } else if (i == editTextColorRow) {
                        resetPref("chatEditTextColor");
                    } else if (i == editTextSizeRow) {
                        resetPref("chatEditTextSize");
                    } else if (i == editTextBGColorRow) {
                        resetPref("chatEditTextBGColor");
                    } else if (i == editTextBGGradientColorRow) {
                        resetPref("chatEditTextBGGradentColor");
                    } else if (i == editTextBGGradientRow) {
                        resetPref("chatEditTextBGGradient");
                    } else if (i == attachBGColorRow) {
                        resetPref("chatAttachBGColor");
                    } else if (i == attachBGGradientRow) {
                        resetPref("chatAttachBGGradient");
                    } else if (i == attachBGGradientColorRow) {
                        resetPref("chatAttachBGGradientColor");
                    } else if (i == attachTextColorRow) {
                        resetPref("chatAttachTextColor");
                    } else if (i == emojiViewBGColorRow) {
                        resetPref("chatEmojiViewBGColor");
                    } else if (i == emojiViewBGGradientRow) {
                        resetPref("chatEmojiViewBGGradient");
                    } else if (i == emojiViewBGGradientColorRow) {
                        resetPref("chatEmojiViewBGGradientColor");
                    } else if (i == emojiViewTabIconColorRow) {
                        resetPref("chatEmojiViewTabIconColor");
                    } else if (i == emojiViewTabColorRow) {
                        resetPref("chatEmojiViewTabColor");
                    } else if (i == quickBarColorRow) {
                        resetPref("chatQuickBarColor");
                    } else if (i == quickBarNamesColorRow) {
                        resetPref("chatQuickBarNamesColor");
                    } else{
                        if(view.getTag() != null){
                            resetPref(view.getTag().toString());
                        }
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
       //("ThemingChat", "resetPref " + key);
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(key);
        editor.apply();
        if (listView != null) {
            listView.invalidateViews();
        }
        Theme.updateChatColors();
        refreshTheme();
    }
    
    private void resetInt(String key, int value){
        Theme.setPlusColor(key, value, true);
        resetPref(key);
        if(key.equals("chatTextSize")){
            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("fons_size", value);
            MessagesController.getInstance().fontSize = value;
            editor.apply();
        }
    }

    private void commitInt(String key, int value){
        Theme.setPlusColor(key, value, false);
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.apply();
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
            //boolean b = AndroidUtilities.getBoolPref("chatSolidBGColorCheck");
            int g = AndroidUtilities.getIntDef("chatGradientBG", 0);
            return  i == headerColorRow || i == headerGradientRow || AndroidUtilities.getIntDef("chatHeaderGradient", 0) != 0 && i == headerGradientColorRow || i == muteColorRow || i == headerIconsColorRow || i == headerAvatarRadiusRow || i == rBubbleColorRow || i == lBubbleColorRow ||  i == bubblesRow || i == checksRow ||
                    i == solidBGColorCheckRow || Theme.chatSolidBGColorCheck && i == solidBGColorRow || Theme.chatSolidBGColorCheck && i == gradientBGRow || (g != 0 &&  i == gradientBGColorRow) || i == avatarRadiusRow || i == avatarSizeRow || i == avatarMarginLeftRow || i == avatarAlignTopRow || i == ownAvatarAlignTopRow || i == showContactAvatar || i == showOwnAvatar || i == showOwnAvatarGroup || i == hideStatusIndicatorCheckRow || i == nameColorRow || i == nameSizeRow || i == statusColorRow || i == onlineColorRow || i == typingColorRow || i == statusSizeRow ||
                    i == textSizeRow || i == timeSizeRow || AndroidUtilities.getBoolPref("chatCommandColorCheck") && i == commandColorRow || i == commandColorCheckRow || i == dateColorRow || i == dateSizeRow || i == dateBubbleColorRow || i == rTextColorRow || i == rLinkColorRow || i == lTextColorRow || i == lLinkColorRow ||
                    i == rTimeColorRow|| i == lTimeColorRow || i == checksColorRow || i == memberColorCheckRow || Theme.chatMemberColorCheck && i == memberColorRow || i == contactNameColorRow || i == forwardRightNameColorRow || i == forwardLeftNameColorRow || i == showUsernameCheckRow ||
                    i == editTextSizeRow || i == editTextColorRow || i == editTextIconsColorRow || i == sendColorRow || i == editTextBGColorRow || i == editTextBGGradientRow || AndroidUtilities.getIntDef("chatEditTextBGGradient", 0) != 0 && i == editTextBGGradientColorRow || i == attachBGColorRow || i == attachBGGradientRow || AndroidUtilities.getIntDef("chatAttachBGGradient", 0) != 0 && i == attachBGGradientColorRow || i == attachTextColorRow ||
                    i == emojiViewBGColorRow || i == emojiViewBGGradientRow || AndroidUtilities.getIntDef("chatEmojiViewBGGradient", 0) != 0 && i == emojiViewBGGradientColorRow || i == emojiViewTabIconColorRow || i == emojiViewTabColorRow || i == selectedMessageBGColorRow ||
                    i == quickBarColorRow || i == quickBarNamesColorRow;
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
                prefix = "2.";
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
                    ((HeaderCell) view).setText(prefix + LocaleController.getString("ChatList", R.string.ChatList));
                }
            }
            else if (type == 2) {
                if (view == null) {
                    view = new TextSettingsCell(mContext);
                }
                TextSettingsCell textCell = (TextSettingsCell) view;
                if (i == headerAvatarRadiusRow) {
                    textCell.setTag("chatHeaderAvatarRadius");
                    int size = themePrefs.getInt("chatHeaderAvatarRadius", AndroidUtilities.isTablet() ? 35 : 32);
                    textCell.setTextAndValue(prefix + LocaleController.getString("AvatarRadius", R.string.AvatarRadius), String.format("%d", size), true);
                } else if (i == avatarRadiusRow) {
                    textCell.setTag("chatAvatarRadius");
                    int size = themePrefs.getInt("chatAvatarRadius", AndroidUtilities.isTablet() ? 35 : 32);
                    textCell.setTextAndValue(prefix + LocaleController.getString("AvatarRadius", R.string.AvatarRadius), String.format("%d", size), true);
                } else if (i == avatarSizeRow) {
                    textCell.setTag("chatAvatarSize");
                    int size = themePrefs.getInt("chatAvatarSize", AndroidUtilities.isTablet() ? 45 : 42);
                    textCell.setTextAndValue(prefix + LocaleController.getString("AvatarSize", R.string.AvatarSize), String.format("%d", size), true);
                } else if (i == avatarMarginLeftRow) {
                    textCell.setTag("chatAvatarMarginLeft");
                    int size = themePrefs.getInt("chatAvatarMarginLeft", 6);
                    textCell.setTextAndValue(prefix + LocaleController.getString("AvatarMarginLeft", R.string.AvatarMarginLeft), String.format("%d", size), true);
                } else if (i == nameSizeRow) {
                    textCell.setTag("chatNameSize");
                    int size = themePrefs.getInt("chatNameSize", AndroidUtilities.isTablet() ? 20 : 18);
                    textCell.setTextAndValue(prefix + LocaleController.getString("NameSize", R.string.NameSize), String.format("%d", size), true);
                } else if (i == statusSizeRow) {
                    textCell.setTag("chatStatusSize");
                    int size = themePrefs.getInt("chatStatusSize", AndroidUtilities.isTablet() ? 16 : 14);
                    textCell.setTextAndValue(prefix + LocaleController.getString("StatusSize", R.string.StatusSize), String.format("%d", size), true);
                } else if (i == textSizeRow) {
                    textCell.setTag("chatTextSize");
                    int size = themePrefs.getInt("chatTextSize", AndroidUtilities.isTablet() ? 18 : 16);
                    textCell.setTextAndValue(prefix + LocaleController.getString("TextSize", R.string.TextSize), String.format("%d", size), true);
                } else if (i == timeSizeRow) {
                    textCell.setTag("chatTimeSize");
                    int size = themePrefs.getInt("chatTimeSize", AndroidUtilities.isTablet() ? 14 : 12);
                    textCell.setTextAndValue(prefix + LocaleController.getString("TimeSize", R.string.TimeSize), String.format("%d", size), true);
                } else if (i == dateSizeRow) {
                    textCell.setTag("chatDateSize");
                    int size = themePrefs.getInt("chatDateSize", AndroidUtilities.isTablet() ? 18 : MessagesController.getInstance().fontSize - 2);
                    textCell.setTextAndValue(prefix + LocaleController.getString("DateSize", R.string.DateSize), String.format("%d", size), true);
                }  else if (i == editTextSizeRow) {
                    textCell.setTag("chatEditTextSize");
                    int size = themePrefs.getInt("chatEditTextSize", AndroidUtilities.isTablet() ? 20 : 18);
                    textCell.setTextAndValue(prefix + LocaleController.getString("EditTextSize", R.string.EditTextSize), String.format("%d", size), true);
                } else if (i == bubblesRow) {
                    textCell.setTag("chatBubbleStyle");
                    Theme.chatBubbleStyle = themePrefs.getString("chatBubbleStyle", Theme.bubblesNamesArray[0]);
                    textCell.setTextAndValue(prefix + LocaleController.getString("BubbleStyle", R.string.BubbleStyle), Theme.chatBubbleStyle, true);
                } else if (i == checksRow) {
                    textCell.setTag("chatCheckStyle");
                    Theme.chatCheckStyle = themePrefs.getString("chatCheckStyle", Theme.checksNamesArray[0]);
                    textCell.setTextAndValue(prefix + LocaleController.getString("CheckStyle", R.string.CheckStyle), Theme.chatCheckStyle, true);
                }
            } else if (type == 4) {
                if (view == null) {
                    view = new TextCheckCell(mContext);
                }
                TextCheckCell textCell = (TextCheckCell) view;
                if (i == solidBGColorCheckRow) {
                    textCell.setTag("chatSolidBGColorCheck");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("SetSolidBGColor", R.string.SetSolidBGColor), Theme.chatSolidBGColorCheck, false);
                } else if (i == commandColorCheckRow) {
                    textCell.setTag("chatCommandColorCheck");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("CommandColorCheck", R.string.CommandColorCheck), themePrefs.getBoolean("chatCommandColorCheck", false), false);
                } else if (i == memberColorCheckRow) {
                    textCell.setTag("chatMemberColorCheck");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("SetMemberColor", R.string.SetMemberColor), Theme.chatMemberColorCheck, false);
                } else if (i == showUsernameCheckRow) {
                    textCell.setTag("chatShowUsernameCheck");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("ShowUsername", R.string.ShowUsername), Theme.chatShowUsernameCheck, true);
                } else if (i == avatarAlignTopRow) {
                    textCell.setTag("chatAvatarAlignTop");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("AvatarAlignTop", R.string.AvatarAlignTop), Theme.chatAvatarAlignTop, true);
                } else if (i == ownAvatarAlignTopRow) {
                    textCell.setTag("chatOwnAvatarAlignTop");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("OwnAvatarAlignTop", R.string.OwnAvatarAlignTop), Theme.chatOwnAvatarAlignTop, true);
                } else if (i == showContactAvatar) {
                    textCell.setTag("chatShowContactAvatar");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("ShowContactAvatar", R.string.ShowContactAvatar), Theme.chatShowContactAvatar, true);
                } else if (i == showOwnAvatar) {
                    textCell.setTag("chatShowOwnAvatar");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("ShowOwnAvatar", R.string.ShowOwnAvatar), Theme.chatShowOwnAvatar, true);
                } else if (i == showOwnAvatarGroup) {
                    textCell.setTag("chatShowOwnAvatarGroup");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("ShowOwnAvatarGroup", R.string.ShowOwnAvatarGroup), Theme.chatShowOwnAvatarGroup, true);
                } else if (i == hideStatusIndicatorCheckRow) {
                    textCell.setTag("chatHideStatusIndicator");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("HideStatusIndicator", R.string.HideStatusIndicator), Theme.chatHideStatusIndicator, true);
                }
            }
            else if (type == 3){
                if (view == null) {
                    view = new TextColorCell(mContext);
                }

                TextColorCell textCell = (TextColorCell) view;

                int defColor = themePrefs.getInt("themeColor", AndroidUtilities.defColor);
                int darkColor = AndroidUtilities.getIntDarkerColor("themeColor", 0x15);
                int lightColor = AndroidUtilities.getIntDarkerColor("themeColor", -0x40);
                if (i == headerColorRow) {
                    textCell.setTag("chatHeaderColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("HeaderColor", R.string.HeaderColor), themePrefs.getInt("chatHeaderColor", defColor), false);
                } else if (i == headerGradientColorRow) {
                    textCell.setTag("chatHeaderGradientColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("HeaderColor", R.string.RowGradientColor), themePrefs.getInt("chatHeaderGradient", 0) == 0 ? 0x00000000 : themePrefs.getInt("chatHeaderGradientColor", Theme.defColor), true);
                } else if (i == headerIconsColorRow) {
                    textCell.setTag("chatHeaderIconsColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("HeaderIconsColor", R.string.HeaderIconsColor), themePrefs.getInt("chatHeaderIconsColor", 0xffffffff), true);
                } else if (i == solidBGColorRow) {
                    textCell.setTag("chatSolidBGColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("SolidBGColor", R.string.SolidBGColor), Theme.chatSolidBGColorCheck ? themePrefs.getInt("chatSolidBGColor", 0xffffffff) : 0x00000000, false);
                } else if (i == gradientBGColorRow) {
                    textCell.setTag("chatGradientBGColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("RowGradientColor", R.string.RowGradientColor), themePrefs.getInt("chatGradientBG", 0) == 0 || !Theme.chatSolidBGColorCheck ? 0x00000000 : themePrefs.getInt("chatGradientBGColor", 0xffffffff), true);
                } else if (i == memberColorRow) {
                    textCell.setTag("chatMemberColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("MemberColor", R.string.MemberColor), themePrefs.getBoolean("chatMemberColorCheck", false) ? themePrefs.getInt("chatMemberColor", darkColor) : 0x00000000, true);
                } else if (i == contactNameColorRow) {
                    textCell.setTag("chatContactNameColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("SharedContactNameColor", R.string.SharedContactNameColor), themePrefs.getInt("chatContactNameColor", defColor), true);
                } else if (i == forwardRightNameColorRow) {
                    textCell.setTag("chatForwardRColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("ForwardRightNameColor", R.string.ForwardRightNameColor), themePrefs.getInt("chatForwardRColor", darkColor), true);
                } else if (i == forwardLeftNameColorRow) {
                    textCell.setTag("chatForwardLColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("ForwardLeftNameColor", R.string.ForwardLeftNameColor), themePrefs.getInt("chatForwardLColor", darkColor), true);
                } else if (i == muteColorRow) {
                    textCell.setTag("chatMuteColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("MuteColor", R.string.MuteColor), themePrefs.getInt("chatMuteColor", 0xffffffff), true);
                } else if (i == rBubbleColorRow) {
                    textCell.setTag("chatRBubbleColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("RBubbleColor", R.string.RBubbleColor), themePrefs.getInt("chatRBubbleColor", AndroidUtilities.getDefBubbleColor()), true);
                } else if (i == lBubbleColorRow) {
                    textCell.setTag("chatLBubbleColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("LBubbleColor", R.string.LBubbleColor), themePrefs.getInt("chatLBubbleColor", 0xffffffff), true);
                } else if (i == rTextColorRow) {
                    textCell.setTag("chatRTextColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("RTextColor", R.string.RTextColor), themePrefs.getInt("chatRTextColor", 0xff000000), true);
                } else if (i == lTextColorRow) {
                    textCell.setTag("chatLTextColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("LTextColor", R.string.LTextColor), themePrefs.getInt("chatLTextColor", 0xff000000), true);
                } else if (i == selectedMessageBGColorRow) {
                    textCell.setTag("chatSelectedMsgBGColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("SelectedMsgBGColor", R.string.SelectedMsgBGColor), themePrefs.getInt("chatSelectedMsgBGColor", Theme.SELECTED_MDG_BACKGROUND_COLOR_DEF), true);
                } else if (i == rLinkColorRow) {
                    textCell.setTag("chatRLinkColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("RLinkColor", R.string.RLinkColor), themePrefs.getInt("chatRLinkColor", defColor), true);
                } else if (i == lLinkColorRow) {
                    textCell.setTag("chatLLinkColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("LLinkColor", R.string.LLinkColor), themePrefs.getInt("chatLLinkColor", defColor), true);
                } else if (i == nameColorRow) {
                    textCell.setTag("chatNameColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("NameColor", R.string.NameColor), themePrefs.getInt("chatNameColor", 0xffffffff), true);
                } else if (i == statusColorRow) {
                    textCell.setTag("chatStatusColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("StatusColor", R.string.StatusColor), themePrefs.getInt("chatStatusColor", lightColor), true);
                } else if (i == onlineColorRow) {
                    textCell.setTag("chatOnlineColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("OnlineColor", R.string.OnlineColor), themePrefs.getInt("chatOnlineColor", themePrefs.getInt("chatStatusColor", lightColor)), true);
                } else if (i == typingColorRow) {
                    textCell.setTag("chatTypingColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("TypingColor", R.string.TypingColor), themePrefs.getInt("chatTypingColor", themePrefs.getInt("chatStatusColor", lightColor)), false);
                } else if (i == rTimeColorRow) {
                    textCell.setTag("chatRTimeColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("RTimeColor", R.string.RTimeColor), themePrefs.getInt("chatRTimeColor", darkColor), true);
                } else if (i == lTimeColorRow) {
                    textCell.setTag("chatLTimeColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("LTimeColor", R.string.LTimeColor), themePrefs.getInt("chatLTimeColor", 0xffa1aab3), true);
                } else if (i == checksColorRow) {
                    textCell.setTag("chatChecksColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("ChecksColor", R.string.ChecksColor), themePrefs.getInt("chatChecksColor", defColor), true);
                } else if (i == commandColorRow) {
                    textCell.setTag("chatCommandColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("CommandColor", R.string.CommandColor), themePrefs.getBoolean("chatCommandColorCheck", false) ? themePrefs.getInt("chatCommandColor",Theme.defColor) : 0x00000000, true);
                } else if (i == dateColorRow) {
                    textCell.setTag("chatDateColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("DateColor", R.string.DateColor), themePrefs.getInt("chatDateColor", 0xffffffff), true);
                } else if (i == dateBubbleColorRow) {
                    textCell.setTag("chatDateBubbleColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("DateBubbleColor", R.string.DateBubbleColor), themePrefs.getInt("chatDateBubbleColor", 0x66768993), true);
                } else if (i == sendColorRow) {
                    textCell.setTag("chatSendIconColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("SendIcon", R.string.SendIcon), themePrefs.getInt("chatSendIconColor", themePrefs.getInt("chatEditTextIconsColor", defColor)), true);
                } else if (i == editTextColorRow) {
                    textCell.setTag("chatEditTextColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("EditTextColor", R.string.EditTextColor), themePrefs.getInt("chatEditTextColor", 0xff000000), true);
                } else if (i == editTextBGColorRow) {
                    textCell.setTag("chatEditTextBGColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("EditTextBGColor", R.string.EditTextBGColor), themePrefs.getInt("chatEditTextBGColor", 0xffffffff), false);
                } else if (i == editTextBGGradientColorRow) {
                    textCell.setTag("chatEditTextBGGradient");
                    textCell.setTextAndColor(prefix + LocaleController.getString("RowGradientColor", R.string.RowGradientColor), themePrefs.getInt("chatEditTextBGGradient", 0) == 0 ? 0x00000000 : themePrefs.getInt("chatEditTextBGGradientColor", 0xffffffff), true);
                } else if (i == attachBGColorRow) {
                    textCell.setTag("chatAttachBGColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("AttachBGColor", R.string.AttachBGColor), themePrefs.getInt("chatAttachBGColor", 0xffffffff), false);
                } else if (i == attachBGGradientColorRow) {
                    textCell.setTag("chatAttachBGGradient");
                    textCell.setTextAndColor(prefix + LocaleController.getString("RowGradientColor", R.string.RowGradientColor), themePrefs.getInt("chatAttachBGGradient", 0) == 0 ? 0x00000000 : themePrefs.getInt("chatAttachBGGradientColor", 0xffffffff), true);
                } else if (i == attachTextColorRow) {
                    textCell.setTag("chatAttachTextColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("AttachTextColor", R.string.AttachTextColor), themePrefs.getInt("chatAttachTextColor", 0xff757575), true);
                } else if (i == editTextIconsColorRow) {
                    textCell.setTag("chatEditTextIconsColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("EditTextIconsColor", R.string.EditTextIconsColor), themePrefs.getInt("chatEditTextIconsColor", 0xffadadad), true);
                } else if (i == emojiViewBGColorRow) {
                    textCell.setTag("chatEmojiViewBGColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("EmojiViewBGColor", R.string.EmojiViewBGColor), themePrefs.getInt("chatEmojiViewBGColor", 0xfff5f6f7), false);
                } else if (i == emojiViewBGGradientColorRow) {
                    textCell.setTag("chatEmojiViewBGGradient");
                    textCell.setTextAndColor(prefix + LocaleController.getString("RowGradientColor", R.string.RowGradientColor), themePrefs.getInt("chatEmojiViewBGGradient", 0) == 0 ? 0x00000000 : themePrefs.getInt("chatEmojiViewBGGradientColor", 0xfff5f6f7), true);
                } else if (i == emojiViewTabIconColorRow) {
                    textCell.setTag("chatEmojiViewTabIconColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("EmojiViewTabIconColor", R.string.EmojiViewTabIconColor), themePrefs.getInt("chatEmojiViewTabIconColor", 0xffa8a8a8), true);
                } else if (i == emojiViewTabColorRow) {
                    textCell.setTag("chatEmojiViewTabColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("EmojiViewTabColor", R.string.EmojiViewTabColor), themePrefs.getInt("chatEmojiViewTabColor", AndroidUtilities.getIntDarkerColor("themeColor",-0x15)), true);
                } else if (i == quickBarColorRow) {
                    textCell.setTag("chatQuickBarColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("QuickBarColor", R.string.QuickBarColor), themePrefs.getInt("chatQuickBarColor", 0xffffffff), true);
                } else if (i == quickBarNamesColorRow) {
                    textCell.setTag("chatQuickBarNamesColor");
                    textCell.setTextAndColor(prefix + LocaleController.getString("QuickBarNamesColor", R.string.QuickBarNamesColor), themePrefs.getInt("chatQuickBarNamesColor", 0xff212121), false);
                }
            } else if (type == 5) {
                if (view == null) {
                    view = new TextDetailSettingsCell(mContext);
                }

                TextDetailSettingsCell textCell = (TextDetailSettingsCell) view;
                 if(i == gradientBGRow){
                    textCell.setMultilineDetail(false);
                    int value = themePrefs.getInt("chatGradientBG", 0);
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
                } else if(i == headerGradientRow){
                     textCell.setMultilineDetail(false);
                     int value = themePrefs.getInt("chatHeaderGradient", 0);
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
                 } else if(i == editTextBGGradientRow){
                     textCell.setMultilineDetail(false);
                     int value = themePrefs.getInt("chatEditTextBGGradient", 0);
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
                 } else if(i == attachBGGradientRow){
                     textCell.setMultilineDetail(false);
                     int value = themePrefs.getInt("chatAttachBGGradient", 0);
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
                 } else if(i == emojiViewBGGradientRow){
                     textCell.setMultilineDetail(false);
                     int value = themePrefs.getInt("chatEmojiViewBGGradient", 0);
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
            else if ( i == headerAvatarRadiusRow || i == avatarRadiusRow || i == avatarSizeRow || i == avatarMarginLeftRow  || i == nameSizeRow ||  i == statusSizeRow || i == textSizeRow || i == timeSizeRow || i == dateSizeRow  || i == editTextSizeRow || i == bubblesRow || i == checksRow) {
                return 2;
            }

            else if ( i == headerColorRow || i == headerGradientColorRow || i == muteColorRow || i == headerIconsColorRow ||
                    i == solidBGColorRow || i == gradientBGColorRow || i == rBubbleColorRow || i == lBubbleColorRow || i == nameColorRow || i == statusColorRow || i == onlineColorRow || i == typingColorRow || i == commandColorRow || i == dateColorRow || i == dateBubbleColorRow ||
                    i == rTextColorRow || i == rLinkColorRow || i == lTextColorRow || i == lLinkColorRow || i == rTimeColorRow || i == lTimeColorRow || i == checksColorRow || i == memberColorRow || i == contactNameColorRow || i == forwardRightNameColorRow || i == forwardLeftNameColorRow ||
                    i == sendColorRow || i == editTextColorRow || i == editTextBGColorRow || i == editTextBGGradientColorRow || i == editTextIconsColorRow ||  i == attachBGColorRow ||  i == attachBGGradientColorRow || i == attachTextColorRow ||
                    i == emojiViewBGColorRow || i == emojiViewBGGradientColorRow || i == emojiViewTabIconColorRow || i == emojiViewTabColorRow || i == selectedMessageBGColorRow ||
                    i == quickBarColorRow || i == quickBarNamesColorRow) {
                return 3;
            } else if (i == solidBGColorCheckRow || i == commandColorCheckRow || i == memberColorCheckRow || i == showUsernameCheckRow || i == avatarAlignTopRow || i == ownAvatarAlignTopRow || i == showContactAvatar || i == showOwnAvatar || i == showOwnAvatarGroup || i == hideStatusIndicatorCheckRow) {
                return 4;
            } else if (i == headerGradientRow || i == gradientBGRow || i == editTextBGGradientRow || i == attachBGGradientRow || i == emojiViewBGGradientRow) {
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
