package org.telegram.ui;

/**
 * Created by Sergio on 22/01/2016.
 */
/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberPicker;

import java.io.File;
import java.util.ArrayList;

public class PlusSettingsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private ListView listView;
    private ListAdapter listAdapter;

    //private int overscrollRow;
    //private int emptyRow;

    //private int settingsSectionRow;
    private int settingsSectionRow2;

    private int mediaDownloadSection;
    private int mediaDownloadSection2;

    private int drawerSectionRow;
    private int drawerSectionRow2;
    private int showUsernameRow;

    private int messagesSectionRow;
    private int messagesSectionRow2;

    private int profileSectionRow;
    private int profileSectionRow2;
    private int profileSharedOptionsRow;
    private int profileEnableGoToMsgRow;

    private int notificationSectionRow;
    private int notificationSection2Row;
    private int notificationInvertMessagesOrderRow;
    //private int notificationMuteNewChats;

    private int privacySectionRow;
    private int privacySectionRow2;
    private int hideMobileNumberRow;

    private int rowCount;
    private int disableMessageClickRow;
    private int showAndroidEmojiRow;
    private int useDeviceFontRow;
    private int keepOriginalFilenameRow;
    private int keepOriginalFilenameDetailRow;
    private int emojiPopupSize;
    private int disableAudioStopRow;

    private int dialogsSectionRow;
    private int dialogsSectionRow2;
    private int dialogsPicClickRow;
    private int dialogsGroupPicClickRow;
    private int dialogsHideTabsCheckRow;
    private int dialogsTabsHeightRow;
    private int dialogsTabsRow;
    private int dialogsManageTabsRow;
    private int dialogsTabsTextModeRow;
    private int dialogsTabsTextSizeRow;
    private int dialogsExpandTabsRow;
    private int dialogsDisableTabsAnimationCheckRow;
    private int dialogsInfiniteTabsSwipe;
    private int chatShowDirectShareBtn;
    private int dialogsHideTabsCounters;
    private int dialogsTabsCountersCountChats;
    private int dialogsTabsCountersCountNotMuted;
    private int dialogsTabsToBottomRow;
    private int dialogsHideSelectedTabIndicator;
    private int dialogsDisableTabsScrollingRow;
    private int dialogsDoNotChangeHeaderTitleRow;
    private int dialogsLimitTabsCountersRow;
    private int dialogsShowAllInAdminTabRow;
    private int dialogsShowAllInAdminTabDetailRow;

    private int chatDirectShareToMenu;
    private int chatDirectShareReplies;
    private int chatDirectShareFavsFirst;
    private int chatShowEditedMarkRow;
    private int chatShowDateToastRow;
    private int chatHideLeftGroupRow;
    private int chatHideJoinedGroupRow;
    private int chatHideBotKeyboardRow;
    private int chatSearchUserOnTwitterRow;
    private int chatShowPhotoQualityBarRow;
    private int chatPhotoQualityRow;
    private int chatShowQuickBarRow;
    private int chatVerticalQuickBarRow;
    private int chatAlwaysBackToMainRow;
    private int chatDoNotCloseQuickBarRow;
    private int chatHideQuickBarOnScrollRow;
    private int chatCenterQuickBarBtnRow;
    private int chatShowMembersQuickBarRow;
    private int chatSaveToCloudQuoteRow;
    private int chatSwipeToReplyRow;
    private int chatPhotoViewerHideStatusBarRow;
    private int chatDrawSingleBigEmojiRow;
    private int chatMarkdownRow;
    private int chatShowUserBioRow;

    private int plusSettingsSectionRow;
    private int plusSettingsSectionRow2;
    private int savePlusSettingsRow;
    private int restorePlusSettingsRow;
    private int resetPlusSettingsRow;

    private int showMySettingsRow;

    private int toastNotificationSectionRow;
    private int toastNotificationSection2Row;
    private int showTypingToastNotificationRow;
    private int showOnlineToastNotificationRow;
    private int showOnlineToastNotificationDetailRow;
    private int showToastOnlyIfContactFavRow;
    private int showOfflineToastNotificationRow;
    private int toastNotificationSizeRow;
    private int toastNotificationPaddingRow;
    private int toastNotificationToBottomRow;
    private int toastNotificationPositionRow;
    private int enableDirectReplyRow;
    private int chatHideInstantCameraRow;
    private int chatDoNotHideStickersTabRow;
    private int hideNotificationsIfPlayingRow;
    private int chatsToLoadRow;
    private int moveVersionToSettingsRow;

    private boolean reseting = false;
    private boolean saving = false;

    private boolean showPrefix;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        NotificationCenter.getInstance().addObserver(this, NotificationCenter.refreshTabs);

        rowCount = 0;
        //overscrollRow = -1;
        //emptyRow = -1;

        //settingsSectionRow = rowCount++;
        settingsSectionRow2 = rowCount++;

        if (android.os.Build.VERSION.SDK_INT >= 19) { // Only enable this option for Kitkat and newer android versions
            showAndroidEmojiRow = rowCount++;
        } else {
            showAndroidEmojiRow = -1;
        }
        useDeviceFontRow = rowCount++;
        chatsToLoadRow = BuildConfig.DEBUG ? rowCount++ : -1;

        dialogsSectionRow = rowCount++;
        dialogsSectionRow2 = rowCount++;

        dialogsHideTabsCheckRow = rowCount++;
        dialogsTabsRow = -1;//rowCount++;
        dialogsManageTabsRow = rowCount++;
        dialogsTabsHeightRow = rowCount++;
        dialogsTabsTextModeRow = rowCount++;
        dialogsTabsTextSizeRow = rowCount++;
        dialogsExpandTabsRow = rowCount++;
        dialogsTabsToBottomRow = rowCount++;
        dialogsDisableTabsScrollingRow = rowCount++;
        dialogsDisableTabsAnimationCheckRow = rowCount++;
        dialogsInfiniteTabsSwipe = rowCount++;
        dialogsHideTabsCounters = rowCount++;
        dialogsTabsCountersCountNotMuted = rowCount++;
        dialogsTabsCountersCountChats = rowCount++;
        dialogsLimitTabsCountersRow = rowCount++;
        dialogsHideSelectedTabIndicator = rowCount++;
        dialogsDoNotChangeHeaderTitleRow = rowCount++;
        dialogsShowAllInAdminTabRow = rowCount++;
        dialogsShowAllInAdminTabDetailRow = rowCount++;

        dialogsPicClickRow = rowCount++;
        dialogsGroupPicClickRow = rowCount++;

        messagesSectionRow = rowCount++;
        messagesSectionRow2 = rowCount++;
        emojiPopupSize = rowCount++;
        disableAudioStopRow = rowCount++;
        disableMessageClickRow = rowCount++;
        chatShowDirectShareBtn = rowCount++;
        chatDirectShareReplies = rowCount++;
        chatDirectShareToMenu = rowCount++;
        chatDirectShareFavsFirst = rowCount++;
        chatShowEditedMarkRow = -1;//rowCount++;
        chatHideLeftGroupRow = rowCount++;
        chatHideJoinedGroupRow = -1;
        chatHideBotKeyboardRow = rowCount++;
        chatShowDateToastRow = -1;//rowCount++;
        chatSearchUserOnTwitterRow = rowCount++;
        chatShowPhotoQualityBarRow = rowCount++;
        chatPhotoQualityRow = rowCount++;
        chatShowQuickBarRow = rowCount++;
        chatVerticalQuickBarRow = rowCount++;
        chatAlwaysBackToMainRow = rowCount++;
        chatDoNotCloseQuickBarRow = rowCount++;
        chatHideQuickBarOnScrollRow = rowCount++;
        chatCenterQuickBarBtnRow = rowCount++;
        chatShowMembersQuickBarRow = rowCount++;
        chatSaveToCloudQuoteRow = rowCount++;
        chatSwipeToReplyRow = rowCount++;
        chatHideInstantCameraRow = rowCount++;
        chatDoNotHideStickersTabRow = rowCount++;
        chatMarkdownRow = -1;//rowCount++;
        chatDrawSingleBigEmojiRow = rowCount++;
        chatPhotoViewerHideStatusBarRow = -1;//rowCount++;
        chatShowUserBioRow = rowCount++;

        drawerSectionRow = rowCount++;
        drawerSectionRow2 = rowCount++;
        showUsernameRow = rowCount++;
        moveVersionToSettingsRow = rowCount++;

        profileSectionRow = rowCount++;
        profileSectionRow2 = rowCount++;
        profileSharedOptionsRow = rowCount++;
        profileEnableGoToMsgRow = rowCount++;

        notificationSectionRow = rowCount++;
        notificationSection2Row = rowCount++;
        hideNotificationsIfPlayingRow = rowCount++;
        notificationInvertMessagesOrderRow = -1;//rowCount++;
        enableDirectReplyRow = -1/*rowCount++*/;
        //notificationMuteNewChats--;// = rowCount++;

        toastNotificationSectionRow = rowCount++;
        toastNotificationSection2Row = rowCount++;
        showTypingToastNotificationRow = rowCount++; //BuildConfig.DEBUG ? rowCount++ : -1;
        showOnlineToastNotificationRow = rowCount++; //BuildConfig.DEBUG ? rowCount++ : -1;
        showOnlineToastNotificationDetailRow = rowCount++;
        toastNotificationToBottomRow = rowCount++; //BuildConfig.DEBUG ? rowCount++ : -1;
        toastNotificationPositionRow = rowCount++; //BuildConfig.DEBUG ? rowCount++ : -1;
        toastNotificationSizeRow = rowCount++; //BuildConfig.DEBUG ? rowCount++ : -1;
        toastNotificationPaddingRow = rowCount++; //BuildConfig.DEBUG ? rowCount++ : -1;
        showToastOnlyIfContactFavRow = BuildConfig.DEBUG ? rowCount++ : -1;
        showOfflineToastNotificationRow = BuildConfig.DEBUG ? rowCount++ : -1;

        privacySectionRow = rowCount++;
        privacySectionRow2 = rowCount++;
        hideMobileNumberRow = rowCount++;

        TLRPC.User currentUser = UserConfig.getCurrentUser();
        showMySettingsRow = currentUser.username == null ? -1 : -1/*rowCount++*/;

        mediaDownloadSection = rowCount++;
        mediaDownloadSection2 = rowCount++;
        keepOriginalFilenameRow = rowCount++;
        keepOriginalFilenameDetailRow = rowCount++;

        plusSettingsSectionRow = rowCount++;
        plusSettingsSectionRow2 = rowCount++;
        savePlusSettingsRow = rowCount++;
        restorePlusSettingsRow = rowCount++;
        resetPlusSettingsRow = rowCount++;

        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
        showPrefix = preferences.getBoolean("plusShowPrefix", true);

        MessagesController.getInstance().loadFullUser(UserConfig.getCurrentUser(), classGuid, true);

        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.refreshTabs);
    }

    @Override
    public View createView(Context context) {
        //actionBar.setItemsBackground(AvatarDrawable.getButtonColorForId(5));
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);

        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setTitle(LocaleController.getString("PlusSettings", R.string.PlusSettings));

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
                editor.putBoolean("plusShowPrefix", showPrefix).apply();
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

        listView.setAdapter(listAdapter);

        //int bgColor = preferences.getInt("prefBGColor", 0xffffffff);
        //int def = preferences.getInt("themeColor", AndroidUtilities.defColor);
        //int hColor = preferences.getInt("prefHeaderColor", def);

        AndroidUtilities.setListViewEdgeEffectColor(listView, Theme.prefActionbarColor);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                if (i == emojiPopupSize) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("EmojiPopupSize", R.string.EmojiPopupSize));
                    final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                    numberPicker.setMinValue(60);
                    numberPicker.setMaxValue(100);
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    numberPicker.setValue(preferences.getInt("emojiPopupSize", AndroidUtilities.isTablet() ? 65 : 60));
                    builder.setView(numberPicker);
                    builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt("emojiPopupSize", numberPicker.getValue());
                            editor.apply();
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                    });
                    showDialog(builder.create());
                } else if (i == chatPhotoQualityRow) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("PhotoQuality", R.string.PhotoQuality));
                    final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                    numberPicker.setMinValue(1);
                    numberPicker.setMaxValue(100);
                    numberPicker.setValue(Theme.plusPhotoQuality);
                    builder.setView(numberPicker);
                    builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt("photoQuality", numberPicker.getValue());
                            editor.apply();
                            Theme.plusPhotoQuality = numberPicker.getValue();
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                    });
                    showDialog(builder.create());
                } else if (i == showAndroidEmojiRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    boolean enabled = preferences.getBoolean("showAndroidEmoji", false);
                    editor.putBoolean("showAndroidEmoji", !enabled);

                    ApplicationLoader.SHOW_ANDROID_EMOJI = !enabled;
                    if(ApplicationLoader.SHOW_ANDROID_EMOJI && Theme.plusDrawSingleBigEmoji){
                        Theme.plusDrawSingleBigEmoji = false;
                        editor.putBoolean("drawSingleBigEmoji", false);
                    }
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!enabled);
                    }
                } else if (i == useDeviceFontRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    boolean enabled = preferences.getBoolean("useDeviceFont", false);
                    editor.putBoolean("useDeviceFont", !enabled);
                    editor.apply();
                    ApplicationLoader.USE_DEVICE_FONT = !enabled;
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
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!enabled);
                    }
                } else if (i == disableAudioStopRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    boolean send = preferences.getBoolean("disableAudioStop", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("disableAudioStop", !send);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!send);
                    }
                } else if (i == disableMessageClickRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    boolean send = preferences.getBoolean("disableMessageClick", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("disableMessageClick", !send);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!send);
                    }
                } else if (i == chatDirectShareReplies) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    boolean send = preferences.getBoolean("directShareReplies", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("directShareReplies", !send);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!send);
                    }
                } else if (i == chatDirectShareToMenu) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    boolean send = preferences.getBoolean("directShareToMenu", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("directShareToMenu", !send);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!send);
                    }
                } else if (i == chatDirectShareFavsFirst) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    boolean send = preferences.getBoolean("directShareFavsFirst", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("directShareFavsFirst", !send);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!send);
                    }
                } else if (i == chatShowEditedMarkRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    boolean send = preferences.getBoolean("showEditedMark", true);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("showEditedMark", !send);
                    editor.apply();
                    Theme.plusShowEditedMark = !send;
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!send);
                    }
                } else if (i == chatShowDateToastRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    boolean show = preferences.getBoolean("showDateToast", true);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("showDateToast", !show);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!show);
                    }
                } else if (i == chatHideLeftGroupRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    boolean hide = preferences.getBoolean("hideLeftGroup", false);
                    MessagesController.getInstance().hideLeftGroup = !hide;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("hideLeftGroup", !hide);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!hide);
                    }
                } else if (i == chatHideJoinedGroupRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    boolean hide = preferences.getBoolean("hideJoinedGroup", false);
                    MessagesController.getInstance().hideJoinedGroup = !hide;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("hideJoinedGroup", !hide);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!hide);
                    }
                } else if (i == chatHideBotKeyboardRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    boolean hide = preferences.getBoolean("hideBotKeyboard", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("hideBotKeyboard", !hide);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!hide);
                    }
                } else if (i == dialogsHideTabsCheckRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    Theme.plusHideTabs = !Theme.plusHideTabs;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("hideTabs", Theme.plusHideTabs);
                    editor.apply();

                    if(Theme.plusHideUsersTab && Theme.plusHideGroupsTab && Theme.plusHideSuperGroupsTab && Theme.plusHideChannelsTab && Theme.plusHideBotsTab && Theme.plusHideFavsTab){

                        if (listView != null) {
                            listView.invalidateViews();
                        }
                    }
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 10);
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusHideTabs);
                    }
                } else if (i == dialogsTabsTextModeRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    Theme.plusTabTitlesMode = Theme.chatsTabTitlesMode  = !Theme.plusTabTitlesMode;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("tabTitlesMode", Theme.plusTabTitlesMode);
                    editor.apply();
                    SharedPreferences.Editor editorTheme = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE).edit();
                    editorTheme.putBoolean("chatsTabTitlesMode", Theme.plusTabTitlesMode);
                    editorTheme.apply();
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 15);
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusTabTitlesMode);
                    }
                } else if (i == dialogsExpandTabsRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    Theme.plusTabsShouldExpand = !Theme.plusTabsShouldExpand;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("tabsShouldExpand", Theme.plusTabsShouldExpand);
                    editor.apply();
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 15);
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusTabsShouldExpand);
                    }
                } else if (i == dialogsDoNotChangeHeaderTitleRow) {
                    Theme.plusDoNotChangeHeaderTitle = !Theme.plusDoNotChangeHeaderTitle;
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("doNotChangeHeaderTitle", Theme.plusDoNotChangeHeaderTitle);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusDoNotChangeHeaderTitle);
                    }
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 11);
                } else if (i == dialogsDisableTabsScrollingRow) {
                    Theme.plusDisableTabsScrolling = !Theme.plusDisableTabsScrolling;
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("disableTabsScrolling", Theme.plusDisableTabsScrolling);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusDisableTabsScrolling);
                    }
                } else if (i == dialogsTabsToBottomRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    //boolean disable = preferences.getBoolean("tabsToBottom", false);
                    Theme.plusTabsToBottom = Theme.chatsTabsToBottom = !Theme.plusTabsToBottom;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("tabsToBottom", Theme.plusTabsToBottom);
                    editor.apply();
                    SharedPreferences.Editor editorTheme = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE).edit();
                    editorTheme.putBoolean("chatsTabsToBottom", Theme.plusTabsToBottom);
                    editorTheme.apply();

                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 14);
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusTabsToBottom);
                    }
                } else if (i == dialogsHideSelectedTabIndicator) {
                    Theme.plusHideTabsSelector = !Theme.plusHideTabsSelector;
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("hideSelectedTabIndicator", Theme.plusHideTabsSelector);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusHideTabsSelector);
                    }
                    //NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 17);
                } else if (i == dialogsDisableTabsAnimationCheckRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    //boolean disable = preferences.getBoolean("disableTabsAnimation", false);
                    Theme.plusDisableTabsAnimation = !Theme.plusDisableTabsAnimation;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("disableTabsAnimation", Theme.plusDisableTabsAnimation);
                    editor.apply();
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 11);
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusDisableTabsAnimation);
                    }
                } else if (i == dialogsInfiniteTabsSwipe) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    Theme.plusInfiniteTabsSwipe = !Theme.plusInfiniteTabsSwipe;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("infiniteTabsSwipe", Theme.plusInfiniteTabsSwipe);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusInfiniteTabsSwipe);
                    }
                } else if (i == dialogsHideTabsCounters) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    Theme.plusHideTabsCounters = !Theme.plusHideTabsCounters;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("hideTabsCounters", Theme.plusHideTabsCounters);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusHideTabsCounters);
                    }
                } else if (i == dialogsLimitTabsCountersRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    Theme.plusLimitTabsCounters = !Theme.plusLimitTabsCounters;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("limitTabsCounters", Theme.plusLimitTabsCounters);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusLimitTabsCounters);
                    }
                } else if (i == dialogsShowAllInAdminTabRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    Theme.plusShowAllInAdminTab = !Theme.plusShowAllInAdminTab;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("showAllInAdminTab", Theme.plusShowAllInAdminTab);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusShowAllInAdminTab);
                    }
                    if(!Theme.plusHideAdminTab) {
                        MessagesController.getInstance().sortDialogs(null);
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload);
                    }
                } else if (i == dialogsTabsCountersCountChats) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    //boolean disable = preferences.getBoolean("tabsCountersCountChats", false);
                    Theme.plusTabsCountersCountChats = !Theme.plusTabsCountersCountChats;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("tabsCountersCountChats", Theme.plusTabsCountersCountChats);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusTabsCountersCountChats);
                    }
                } else if (i == dialogsTabsCountersCountNotMuted) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    Theme.plusTabsCountersCountNotMuted = !Theme.plusTabsCountersCountNotMuted;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("tabsCountersCountNotMuted", Theme.plusTabsCountersCountNotMuted);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusTabsCountersCountNotMuted);
                    }
                } else if (i == showUsernameRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    Theme.plusShowUsername = !Theme.plusShowUsername;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("showUsername", Theme.plusShowUsername);
                    /*if(!scr){
                        editor.putBoolean("hideMobile", true);
                        if (listView != null) {
                            listView.invalidateViews();
                        }
                    }*/
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusShowUsername);
                    }
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.mainUserInfoChanged);
                } else if (i == moveVersionToSettingsRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    Theme.plusMoveVersionToSettings = !Theme.plusMoveVersionToSettings;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("moveVersionToSettings", Theme.plusMoveVersionToSettings);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusMoveVersionToSettings);
                    }
                    //NotificationCenter.getInstance().postNotificationName(NotificationCenter.mainUserInfoChanged);
                } else if (i == profileEnableGoToMsgRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    Theme.plusProfileEnableGoToMsg = !Theme.plusProfileEnableGoToMsg;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("profileEnableGoToMsg", Theme.plusProfileEnableGoToMsg);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusProfileEnableGoToMsg);
                    }
                } else if (i == hideMobileNumberRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    Theme.plusHideMobile = !Theme.plusHideMobile;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("hideMobile", Theme.plusHideMobile);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusHideMobile);
                    }
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.mainUserInfoChanged);
                } else if (i == keepOriginalFilenameRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    boolean keep = preferences.getBoolean("keepOriginalFilename", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("keepOriginalFilename", !keep);
                    editor.apply();
                    ApplicationLoader.KEEP_ORIGINAL_FILENAME = !keep;
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!keep);
                    }
                } else if (i == dialogsPicClickRow) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("ClickOnContactPic", R.string.ClickOnContactPic));
                    builder.setItems(new CharSequence[]{
                            LocaleController.getString("RowGradientDisabled", R.string.RowGradientDisabled),
                            LocaleController.getString("ShowPics", R.string.ShowPics),
                            LocaleController.getString("ShowProfile", R.string.ShowProfile)
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt("dialogsClickOnPic", which);
                            editor.apply();
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    showDialog(builder.create());
                } else if (i == dialogsGroupPicClickRow) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("ClickOnGroupPic", R.string.ClickOnGroupPic));
                    builder.setItems(new CharSequence[]{
                            LocaleController.getString("RowGradientDisabled", R.string.RowGradientDisabled),
                            LocaleController.getString("ShowPics", R.string.ShowPics),
                            LocaleController.getString("ShowProfile", R.string.ShowProfile)
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt("dialogsClickOnGroupPic", which);
                            editor.apply();
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    showDialog(builder.create());
                } else if (i == dialogsTabsTextSizeRow) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("TabsTextSize", R.string.TabsTextSize));
                    final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                    numberPicker.setMinValue(8);
                    numberPicker.setMaxValue(18);
                    numberPicker.setValue(Theme.plusTabsTextSize);
                    builder.setView(numberPicker);
                    builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Theme.plusTabsTextSize = Theme.chatsTabsTextSize = numberPicker.getValue();
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt("tabsTextSize", Theme.plusTabsTextSize);
                            editor.apply();
                            SharedPreferences.Editor editorTheme = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE).edit();
                            editorTheme.putInt("chatsTabsTextSize", Theme.plusTabsTextSize);
                            editorTheme.apply();
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 15);
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                    });
                    showDialog(builder.create());
                } else if (i == dialogsTabsHeightRow) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("TabsHeight", R.string.TabsHeight));
                    final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                    numberPicker.setMinValue(30);
                    numberPicker.setMaxValue(48);
                    //SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    numberPicker.setValue(Theme.plusTabsHeight /*preferences.getInt("tabsHeight", AndroidUtilities.isTablet() ? 42 : 40)*/);
                    builder.setView(numberPicker);
                    builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            Theme.plusTabsHeight = numberPicker.getValue();
                            editor.putInt("tabsHeight", Theme.plusTabsHeight);
                            editor.apply();
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 12);
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                    });
                    showDialog(builder.create());
                } else if (i == dialogsManageTabsRow) {
                    presentFragment(new PlusManageTabsActivity());
                } else if (i == dialogsTabsRow) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    createTabsDialog(builder);
                    builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 13);
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                    });
                    showDialog(builder.create());
                } else if (i == profileSharedOptionsRow) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    createSharedOptions(builder);
                    builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, 13);
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                    });
                    showDialog(builder.create());
                } /*else if (i == notificationMuteNewChats) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    createMuteNewChatsOptions(builder);
                    builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                    });
                    showDialog(builder.create());
                }*/ else if (i == showMySettingsRow) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    createMySettingsOptions(builder);
                    final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    final int FLAGS = preferences.getInt("showMySettings", 0);
                    builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int flags = preferences.getInt("showMySettings", 0);
                            if(FLAGS != flags){
                                getUserAbout();
                            }
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                    });
                    showDialog(builder.create());
                } else if (i == chatShowDirectShareBtn) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    createDialog(builder, chatShowDirectShareBtn);
                    builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                    });
                    showDialog(builder.create());
                } else if (i == notificationInvertMessagesOrderRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    boolean scr = preferences.getBoolean("invertMessagesOrder", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("invertMessagesOrder", !scr);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!scr);
                    }
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.mainUserInfoChanged);
                } else if (i == hideNotificationsIfPlayingRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    Theme.plusHideNotificationsIfPlaying = !Theme.plusHideNotificationsIfPlaying;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("hideNotificationsIfPlaying", Theme.plusHideNotificationsIfPlaying);
                    editor.apply();
                    AndroidUtilities.playingAGame = false;
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusHideNotificationsIfPlaying);
                    }
                } else if (i == enableDirectReplyRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    Theme.plusEnableDirectReply = !Theme.plusEnableDirectReply;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("enableDirectReply", Theme.plusEnableDirectReply);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusEnableDirectReply);
                    }
                } else if (i == chatShowQuickBarRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    boolean bol = Theme.plusShowQuickBar;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("showQuickBar", !bol);
                    editor.apply();
                    Theme.plusShowQuickBar = !bol;
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!bol);
                    }
                    if (listView != null) {
                        listView.invalidateViews();
                    }
                } else if (i == chatPhotoViewerHideStatusBarRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    Theme.plusPhotoViewerHideStatusBar = !Theme.plusPhotoViewerHideStatusBar;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("photoViewerHideStatusBar", Theme.plusPhotoViewerHideStatusBar);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusPhotoViewerHideStatusBar);
                    }
                    if (listView != null) {
                        listView.invalidateViews();
                    }
                } else if (i == chatDrawSingleBigEmojiRow) {
                    if((ApplicationLoader.SHOW_ANDROID_EMOJI || MessagesController.getInstance().useSystemEmoji) && !Theme.plusDrawSingleBigEmoji){
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                if (getParentActivity() != null) {
                                    Toast toast = Toast.makeText(getParentActivity(), LocaleController.getString("EmojiBigSizeInfo", R.string.EmojiBigSizeInfo), Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            }
                        });
                    } else {
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                        Theme.plusDrawSingleBigEmoji = !Theme.plusDrawSingleBigEmoji;
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("drawSingleBigEmoji", Theme.plusDrawSingleBigEmoji);
                        editor.apply();
                        MessagesController.getInstance().allowBigEmoji = Theme.plusDrawSingleBigEmoji;
                        SharedPreferences.Editor mainEditor = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE).edit();
                        mainEditor.putBoolean("allowBigEmoji", Theme.plusDrawSingleBigEmoji);
                        mainEditor.apply();
                        if (view instanceof TextCheckCell) {
                            ((TextCheckCell) view).setChecked(Theme.plusDrawSingleBigEmoji);
                        }
                    }
                } else if (i == chatMarkdownRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    Theme.plusEnableMarkdown = !Theme.plusEnableMarkdown;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("enableMarkdown", Theme.plusEnableMarkdown);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusEnableMarkdown);
                    }
                } else if (i == chatShowUserBioRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    Theme.plusShowUserBio = !Theme.plusShowUserBio;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("showUserBio", Theme.plusShowUserBio);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusShowUserBio);
                    }
                } else if (i == chatDoNotHideStickersTabRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    Theme.plusDoNotHideStickersTab = !Theme.plusDoNotHideStickersTab;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("doNotHideStickersTab", Theme.plusDoNotHideStickersTab);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusDoNotHideStickersTab);
                    }
                    if (listView != null) {
                        listView.invalidateViews();
                    }
                } else if (i == chatHideInstantCameraRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    Theme.plusHideInstantCamera = !Theme.plusHideInstantCamera;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("hideInstantCamera", Theme.plusHideInstantCamera);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusHideInstantCamera);
                    }
                    if (listView != null) {
                        listView.invalidateViews();
                    }
                } else if (i == chatSwipeToReplyRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    Theme.plusSwipeToReply = !Theme.plusSwipeToReply;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("plusSwipeToReply", Theme.plusSwipeToReply);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusSwipeToReply);
                    }
                    if (listView != null) {
                        listView.invalidateViews();
                    }
                }  else if (i == chatVerticalQuickBarRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    boolean bol = Theme.plusVerticalQuickBar;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("verticalQuickBar", !bol);
                    editor.apply();
                    Theme.plusVerticalQuickBar = !bol;
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!bol);
                    }
                } else if (i == chatAlwaysBackToMainRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    boolean bol = Theme.plusAlwaysBackToMain;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("alwaysBackToMain", !bol);
                    editor.apply();
                    Theme.plusAlwaysBackToMain = !bol;
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!bol);
                    }
                } else if (i == chatDoNotCloseQuickBarRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    boolean bol = Theme.plusDoNotCloseQuickBar;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("doNotCloseQuickBar", !bol);
                    editor.apply();
                    Theme.plusDoNotCloseQuickBar = !bol;
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!bol);
                    }
                } else if (i == chatHideQuickBarOnScrollRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    boolean bol = Theme.plusHideQuickBarOnScroll;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("hideQuickBarOnScroll", !bol);
                    editor.apply();
                    Theme.plusHideQuickBarOnScroll = !bol;
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!bol);
                    }
                } else if (i == chatCenterQuickBarBtnRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    boolean bol = Theme.plusCenterQuickBarBtn;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("centerQuickBarBtn", !bol);
                    editor.apply();
                    Theme.plusCenterQuickBarBtn = !bol;
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!bol);
                    }
                } else if (i == chatShowMembersQuickBarRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    boolean bol = Theme.plusQuickBarShowMembers;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("quickBarShowMembers", !bol);
                    editor.apply();
                    Theme.plusQuickBarShowMembers = !bol;
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!bol);
                    }
                } else if (i == chatSearchUserOnTwitterRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    boolean hide = preferences.getBoolean("searchOnTwitter", true);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("searchOnTwitter", !hide);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!hide);
                    }
                } else if (i == chatShowPhotoQualityBarRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    //boolean hide = preferences.getBoolean("showPhotoQualityBar", true);
                    Theme.plusShowPhotoQualityBar = !Theme.plusShowPhotoQualityBar;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("showPhotoQualityBar", Theme.plusShowPhotoQualityBar);
                    editor.apply();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(Theme.plusShowPhotoQualityBar);
                    }
                } else if (i == chatSaveToCloudQuoteRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    boolean bol = Theme.plusSaveToCloudQuote;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("saveToCloudQuote", !bol);
                    editor.apply();
                    Theme.plusSaveToCloudQuote = !bol;
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!bol);
                    }
                } else if(i == savePlusSettingsRow){
                    LayoutInflater li = LayoutInflater.from(getParentActivity());
                    View promptsView = li.inflate(R.layout.editbox_dialog, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setView(promptsView);
                    final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);

                    userInput.setHint(LocaleController.getString("EnterName", R.string.EnterName));
                    userInput.setHintTextColor(0xff979797);
                    SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
                    int defColor = themePrefs.getInt("themeColor", AndroidUtilities.defColor);
                    userInput.getBackground().setColorFilter(themePrefs.getInt("dialogColor", defColor), PorterDuff.Mode.SRC_IN);
                    AndroidUtilities.clearCursorDrawable(userInput);
                    //builder.setMessage(LocaleController.getString("EnterName", R.string.EnterName));
                    builder.setTitle(LocaleController.getString("SaveSettings", R.string.SaveSettings));
                    builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (saving) {
                                return;
                            }
                            final String pName = userInput.getText().toString();
                            if(pName.length() < 1){
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
                                        String path = "/Telegram";
                                        Utilities.savePreferencesToSD(getParentActivity(), path, "plusconfig.xml", pName+".xml", true);
                                        Utilities.saveDBToSD(getParentActivity(), path, "favourites", "favorites.db", true);
                                    }
                                }
                            });
                        }
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    showDialog(builder.create());
                } else if (i == restorePlusSettingsRow) {
                    DocumentSelectActivity fragment = new DocumentSelectActivity();
                    fragment.fileFilter = ".xml";
                    fragment.arrayFilter = new String[] {".db"};
                    fragment.setDelegate(new DocumentSelectActivity.DocumentSelectActivityDelegate() {
                        @Override
                        public void didSelectFiles(final DocumentSelectActivity activity, ArrayList<String> files) {
                            restoreSettings(files.get(0));
                        }

                        @Override
                        public void startDocumentSelectActivity() {}
                    });
                    presentFragment(fragment);
                } else if(i == resetPlusSettingsRow){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setMessage(LocaleController.getString("AreYouSure", R.string.AreYouSure));
                    builder.setTitle(LocaleController.getString("ResetSettings", R.string.ResetSettings));
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
                                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.clear();
                                    editor.apply();
                                    if (listView != null) {
                                        listView.invalidateViews();
                                        fixLayout();
                                    }
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
                } else if (i == showTypingToastNotificationRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    boolean disable = Theme.plusShowTypingToast;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("showTypingToast", !disable);
                    editor.apply();
                    Theme.plusShowTypingToast  = !disable;
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!disable);
                    }
                } else if (i == showOnlineToastNotificationRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    boolean disable = Theme.plusShowOnlineToast;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("showOnlineToast", !disable);
                    editor.apply();
                    Theme.plusShowOnlineToast  = !disable;
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!disable);
                    }
                } else if (i == showToastOnlyIfContactFavRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    boolean disable = Theme.plusShowOnlyIfContactFav;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("showOnlyIfContactFav", !disable);
                    editor.apply();
                    Theme.plusShowOnlyIfContactFav  = !disable;
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!disable);
                    }
                } else if (i == showOfflineToastNotificationRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    boolean disable = Theme.plusShowOfflineToast;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("showOfflineToast", !disable);
                    editor.apply();
                    Theme.plusShowOfflineToast = !disable;
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!disable);
                    }
                } else if (i == toastNotificationSizeRow) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("ToastNotificationSize", R.string.ToastNotificationSize));
                    final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                    numberPicker.setMinValue(10);
                    numberPicker.setMaxValue(20);
                    numberPicker.setValue(Theme.plusToastNotificationSize);
                    builder.setView(numberPicker);
                    builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt("toastNotificationSize", numberPicker.getValue());
                            editor.apply();
                            Theme.plusToastNotificationSize = numberPicker.getValue();
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                            //MessagesController.getInstance().mToast = null;
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.showStatusNotifications, null, true);
                        }
                    });
                    showDialog(builder.create());
                } else if (i == toastNotificationPaddingRow) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("ToastNotificationPadding", R.string.ToastNotificationPadding));
                    final NumberPicker numberPicker = new NumberPicker(getParentActivity());
                    numberPicker.setMinValue(0);
                    numberPicker.setMaxValue(200);
                    numberPicker.setValue(Theme.plusToastNotificationPadding);
                    builder.setView(numberPicker);
                    builder.setNegativeButton(LocaleController.getString("Done", R.string.Done), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt("toastNotificationPadding", numberPicker.getValue());
                            editor.apply();
                            Theme.plusToastNotificationPadding = numberPicker.getValue();
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                            //MessagesController.getInstance().mToast = null;
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.showStatusNotifications, null, true);
                        }
                    });
                    showDialog(builder.create());
                } else if (i == toastNotificationToBottomRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    boolean disable = Theme.plusToastNotificationToBottom;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("toastNotificationToBottom", !disable);
                    editor.apply();
                    Theme.plusToastNotificationToBottom = !disable;
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!disable);
                    }
                    //MessagesController.getInstance().mToast = null;
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.showStatusNotifications, null, true);
                } else if (i == toastNotificationPositionRow) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("ToastNotificationPosition", R.string.ToastNotificationPosition));
                    builder.setItems(new CharSequence[]{
                            LocaleController.getString("Left", R.string.Left),
                            LocaleController.getString("Center", R.string.Center),
                            LocaleController.getString("Right", R.string.Right)
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt("toastNotificationPosition", which);
                            editor.apply();
                            Theme.plusToastNotificationPosition = which;
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                            //MessagesController.getInstance().mToast = null;
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.showStatusNotifications, null, true);
                        }
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    showDialog(builder.create());
                } else if (i == chatsToLoadRow) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle("Chats to load");
                    final int v0 = 50;
                    final int v1 = 100;
                    final int v2 = 200;
                    final int v3 = 300;
                    final int v4 = 400;
                    final int v5 = 500;
                    final int v6 = 750;
                    final int v7 = 1000;
                    final int v8 = 1500;
                    final int v9 = 2000;
                    final int v10 = 1000000;
                    builder.setItems(new CharSequence[]{v0+"", v1+"", v2+"", v3+"", v4+"", v5+"", v6+"", v7+"", v8+"", v9+"", "All"
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            String param = "chatsToLoad";
                            int value = 100;
                            if (which == 0) {
                                value = v0;
                            } else if (which == 1) {
                                value = v1;
                            } else if (which == 2) {
                                value = v2;
                            } else if (which == 3) {
                                value = v3;
                            } else if (which == 4) {
                                value = v4;
                            } else if (which == 5) {
                                value = v5;
                            } else if (which == 6) {
                                value = v6;
                            } else if (which == 7) {
                                value = v7;
                            } else if (which == 8) {
                                value = v8;
                            } else if (which == 9) {
                                value = v9;
                            } else if (which == 10) {
                                value = v10;
                            }
                            Theme.plusChatsToLoad = value;
                            editor.putInt(param, value);
                            editor.commit();
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
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
                //resetPref(view.getTag().toString());
                return true;
            }
        });

        frameLayout.addView(actionBar);

        return fragmentView;
    }

    private void restoreSettings(final String xmlFile){
        File file = new File(xmlFile);
        File favFile = null;
        final String favFilePath = file.getParentFile().toString()+ "/favorites.db";
        if(!file.getName().contains("favorites.db")) {
            favFile = new File(favFilePath);
        }
        final boolean favExists = favFile != null && favFile.exists();
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(LocaleController.getString("RestoreSettings", R.string.RestoreSettings));
        final String name = file.getName();
        builder.setMessage(file.getName());
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        String ext = name.split("\\.")[1];
                        if(ext != null) {
                            if (ext.contains("xml")) {
                                if (Utilities.loadPrefFromSD(getParentActivity(), xmlFile, "plusconfig") == 4) {
                                    if(favExists){
                                        restoreSettings(favFilePath);
                                    } else {
                                        Utilities.restartApp();
                                    }
                                }
                            } else if (ext.contains("db")) {
                                if (Utilities.loadDBFromSD(getParentActivity(), xmlFile, "favourites") == 4) {
                                    Utilities.restartApp();
                                }
                            }
                        }
                    }
                });
            }
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        showDialog(builder.create());
    }

    private AlertDialog.Builder createTabsDialog(AlertDialog.Builder builder){
        builder.setTitle(LocaleController.getString("HideShowTabs", R.string.HideShowTabs));

        builder.setMultiChoiceItems(
                new CharSequence[]{LocaleController.getString("All", R.string.All), LocaleController.getString("Users", R.string.Users), LocaleController.getString("Groups", R.string.Groups), LocaleController.getString("SuperGroups", R.string.SuperGroups), LocaleController.getString("Channels", R.string.Channels), LocaleController.getString("Bots", R.string.Bots), LocaleController.getString("Favorites", R.string.Favorites)},
                new boolean[]{!Theme.plusHideAllTab, !Theme.plusHideUsersTab, !Theme.plusHideGroupsTab, !Theme.plusHideSuperGroupsTab, !Theme.plusHideChannelsTab, !Theme.plusHideBotsTab, !Theme.plusHideFavsTab},
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        /*SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        if (which == 0) {
                            Theme.plusHideAllTab = !isChecked;
                            editor.putBoolean("hideUsers", Theme.plusHideAllTab);
                        } else if (which == 1) {
                            Theme.plusHideUsersTab = !isChecked;
                            editor.putBoolean("hideUsers", Theme.plusHideUsersTab);
                        } else if (which == 2) {
                            Theme.plusHideGroupsTab = !isChecked;
                            editor.putBoolean("hideGroups", Theme.plusHideGroupsTab);
                        } else if (which == 3) {
                            Theme.plusHideSuperGroupsTab = !isChecked;
                            editor.putBoolean("hideSGroups", Theme.plusHideSuperGroupsTab);
                        } else if (which == 4) {
                            Theme.plusHideChannelsTab = !isChecked;
                            editor.putBoolean("hideChannels", Theme.plusHideChannelsTab);
                        } else if (which == 5) {
                            Theme.plusHideBotsTab = !isChecked;
                            editor.putBoolean("hideBots", Theme.plusHideBotsTab);
                        } else if (which == 6) {
                            Theme.plusHideFavsTab = !isChecked;
                            editor.putBoolean("hideFavs", Theme.plusHideFavsTab);
                        }
                        editor.apply();

                        if(Theme.plusHideUsersTab && Theme.plusHideGroupsTab && Theme.plusHideSuperGroupsTab && Theme.plusHideChannelsTab && Theme.plusHideBotsTab && Theme.plusHideFavsTab){
                            Theme.plusHideTabs = true;
                            editor.putBoolean("hideTabs", Theme.plusHideTabs);
                            editor.apply();

                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }*/
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabs, which);
                    }
                });
        return builder;
    }

    private AlertDialog.Builder createSharedOptions(AlertDialog.Builder builder){
        builder.setTitle(LocaleController.getString("SharedMedia", R.string.SharedMedia));

        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
        boolean hideMedia = preferences.getBoolean("hideSharedMedia", false);
        boolean hideFiles = preferences.getBoolean("hideSharedFiles", false);
        boolean hideMusic = preferences.getBoolean("hideSharedMusic", false);
        boolean hideLinks = preferences.getBoolean("hideSharedLinks", false);
        CharSequence[] cs = /*BuildVars.DEBUG_VERSION ?*/   new CharSequence[]{LocaleController.getString("SharedMediaTitle", R.string.SharedMediaTitle), LocaleController.getString("DocumentsTitle", R.string.DocumentsTitle), LocaleController.getString("AudioTitle", R.string.AudioTitle), LocaleController.getString("LinksTitle", R.string.LinksTitle)} /*:
                                                        new CharSequence[]{LocaleController.getString("SharedMediaTitle", R.string.SharedMediaTitle), LocaleController.getString("DocumentsTitle", R.string.DocumentsTitle), LocaleController.getString("AudioTitle", R.string.AudioTitle)}*/;
        boolean[] b = /*BuildVars.DEBUG_VERSION ?*/ new boolean[]{!hideMedia, !hideFiles, !hideMusic, !hideLinks} /*:
                                                new boolean[]{!hideMedia, !hideFiles, !hideMusic}*/;
        builder.setMultiChoiceItems(cs, b,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();

                        if (which == 0) {
                            editor.putBoolean("hideSharedMedia", !isChecked);
                        } else if (which == 1) {
                            editor.putBoolean("hideSharedFiles", !isChecked);
                        } else if (which == 2) {
                            editor.putBoolean("hideSharedMusic", !isChecked);
                        } else if (which == 3) {
                            editor.putBoolean("hideSharedLinks", !isChecked);
                        }
                        editor.apply();
                    }
                });
        return builder;
    }

    /*private AlertDialog.Builder createMuteNewChatsOptions(AlertDialog.Builder builder){
        builder.setTitle(LocaleController.getString("MuteNewChats", R.string.MuteNewChats));

        final int USER = 1;         // 000001
        final int ENCRYPTED = 2;    // 000010
        final int GROUP = 4;        // 000100
        final int SUPERGROUP = 8;   // 001000
        final int CHANNEL = 16;     // 010000
        final int BOT = 32;         // 100000

        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
        int MUTE_FLAGS = preferences.getInt("muteIfNew", 0);
        boolean muteUser = (MUTE_FLAGS & USER) == USER;
        boolean muteEncrypted = (MUTE_FLAGS & ENCRYPTED) == ENCRYPTED;
        boolean muteGroup = (MUTE_FLAGS & GROUP) == GROUP;
        boolean muteSGroup = (MUTE_FLAGS & SUPERGROUP) == SUPERGROUP;
        boolean muteChannel = (MUTE_FLAGS & CHANNEL) == CHANNEL;
        boolean muteBot = (MUTE_FLAGS & BOT) == BOT;

        builder.setMultiChoiceItems(new CharSequence[]{LocaleController.getString("Users", R.string.Users), LocaleController.getString("EncryptedChat", R.string.EncryptedChat), LocaleController.getString("Groups", R.string.Groups), LocaleController.getString("SuperGroups", R.string.SuperGroups), LocaleController.getString("Channels", R.string.Channels), LocaleController.getString("Bots", R.string.Bots)},
                new boolean[]{muteUser, muteEncrypted, muteGroup, muteSGroup, muteChannel, muteBot},
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                        int FLAGS = preferences.getInt("muteIfNew", 0);
                        int val = 0;
                        if (which == 0) {
                            val = USER;
                        } else if (which == 1) {
                            val = ENCRYPTED;
                        } else if (which == 2) {
                            val = GROUP;
                        } else if (which == 3) {
                            val = SUPERGROUP;
                        } else if (which == 4) {
                            val = CHANNEL;
                        } else if (which == 5) {
                            val = BOT;
                        }
                        FLAGS = isChecked ? FLAGS + val : FLAGS - val;
                        MessagesController.getInstance().muteIfNew = FLAGS;
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt("muteIfNew", FLAGS);
                        editor.apply();
                    }
                });
        return builder;
    }*/

    private AlertDialog.Builder createMySettingsOptions(AlertDialog.Builder builder){
        builder.setTitle(LocaleController.getString("ShowMySettings", R.string.ShowMySettings));

        final int VERSION = 1;         // 000001
        final int LANGUAGE = 2;    // 000010

        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
        int FLAGS = preferences.getInt("showMySettings", 0);
        boolean showVersion = (FLAGS & VERSION) == VERSION;
        boolean showLanguage = (FLAGS & LANGUAGE) == LANGUAGE;

        builder.setMultiChoiceItems(new CharSequence[]{LocaleController.getString("PlusVersion", R.string.PlusVersion), LocaleController.getString("Language", R.string.Language)/*, LocaleController.getString("Groups", R.string.Groups), LocaleController.getString("SuperGroups", R.string.SuperGroups), LocaleController.getString("Channels", R.string.Channels), LocaleController.getString("Bots", R.string.Bots)*/},
                new boolean[]{showVersion, showLanguage/*, muteGroup, muteSGroup, muteChannel, muteBot*/},
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                        int FLAGS = preferences.getInt("showMySettings", 0);
                        int val = 0;
                        if (which == 0) {
                            val = VERSION;
                        } else if (which == 1) {
                            val = LANGUAGE;
                        }
                        FLAGS = isChecked ? FLAGS + val : FLAGS - val;
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt("showMySettings", FLAGS);
                        editor.apply();
                    }
                });
        return builder;
    }

    private String userAbout;
    private int linkSearchRequestId;
    private TLRPC.WebPage foundWebPage;
    private int pass;

    public void getUserAbout() {
        final TLRPC.User currentUser = UserConfig.getCurrentUser();
        //final int uid = UserConfig.getClientUserId();
        //final TLRPC.User currentUser = MessagesController.getInstance().getUser(uid);
        //if(currentUser == null || currentUser.username == null){
        //    return;
        //}
        String link = String.format("https://telegram.me/%s", currentUser.username);
        //Log.e("SettingsActivity", "getUserAbout link "+link);
        userAbout = null;
        final TLRPC.TL_messages_getWebPagePreview req = new TLRPC.TL_messages_getWebPagePreview();
        req.message = link;
        linkSearchRequestId = ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
            @Override
            public void run(final TLObject response, final TLRPC.TL_error error) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        linkSearchRequestId = 0;
                        if (error == null) {
                            if (response instanceof TLRPC.TL_messageMediaWebPage) {
                                foundWebPage = ((TLRPC.TL_messageMediaWebPage) response).webpage;
                                if(foundWebPage.description != null){
                                    userAbout = foundWebPage.description;
                                    //Log.e("SettingsActivity", "userAbout " + userAbout);
                                    //NotificationCenter.getInstance().postNotificationName(NotificationCenter.userInfoDidLoaded, uid, null);

                                    setUserAbout();
                                } else{
                                    if(pass != 1){
                                        pass = 1;
                                        AndroidUtilities.runOnUIThread(new Runnable() {
                                        //final Handler handler = new Handler();
                                        //handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                getUserAbout();
                                            }
                                        }, 500);
                                    }
                                }
                            }
                        }
                    }
                });
            }
        });
        ConnectionsManager.getInstance().bindRequestToGuid(linkSearchRequestId, classGuid);
    }

    public void setUserAbout() {
        //String status = userAbout;
        // get the index of last new line character
        int startIndex = userAbout.lastIndexOf("\n");

        String result = null;

        if(startIndex != -1 && startIndex != userAbout.length()){
            result = userAbout.substring(startIndex + 1);
        }
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
        int FLAGS = preferences.getInt("showMySettings", 0);
        boolean showVersion = (FLAGS & 1) == 1;
        boolean showLanguage = (FLAGS & 2) == 2;
        String version = AndroidUtilities.getVersion();
        //boolean newLine = false;
        String status = null;
        if(showVersion){
            status = version;
            //newLine = true;
        }
        SharedPreferences mainPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        String lang = mainPreferences.getString("language", null);
        if(lang != null && showLanguage){
            status = (status != null ? status + " " : "") + lang.toUpperCase();
        }
        if(status == null){
            return;
        }
        if(result != null && status.equals(result)){
            return;
        }
        //Log.e("setUserAbout","status " + status);
        TLRPC.TL_account_updateProfile req = new TLRPC.TL_account_updateProfile();
        req.flags |= 4;
        req.about = (result == null ? userAbout : userAbout.substring(0, userAbout.lastIndexOf("\n"))) + "\n" + status;
        //Log.e("setUserAbout","req.about " + req.about);
        ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
            @Override
            public void run(TLObject response, TLRPC.TL_error error) {
                if(error != null){
                    //Log.e("setUserAbout","error " + error.toString());
                }
                if(response != null){
                    //Log.e("setUserAbout","response " + response.toString());
                    MessagesController.getInstance().loadFullUser(UserConfig.getCurrentUser(), classGuid, true);
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            //NotificationCenter.getInstance().postNotificationName(NotificationCenter.userInfoDidLoaded, UserConfig.getCurrentUser().id, null);
                            UserConfig.saveConfig(true);
                        }
                    });
                }
            }
        });
    }

    private AlertDialog.Builder createDialog(AlertDialog.Builder builder, int i){
        if (i == chatShowDirectShareBtn) {
            builder.setTitle(LocaleController.getString("ShowDirectShareButton", R.string.ShowDirectShareButton));

            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
            //SharedPreferences mainPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
            boolean showDSBtnUsers = preferences.getBoolean("showDSBtnUsers", false);
            boolean showDSBtnGroups = preferences.getBoolean("showDSBtnGroups", true);
            boolean showDSBtnSGroups = preferences.getBoolean("showDSBtnSGroups", true);
            boolean showDSBtnChannels = preferences.getBoolean("showDSBtnChannels", true);
            boolean showDSBtnBots = preferences.getBoolean("showDSBtnBots", true);

            builder.setMultiChoiceItems(
                    new CharSequence[]{LocaleController.getString("Users", R.string.Users), LocaleController.getString("Groups", R.string.Groups), LocaleController.getString("SuperGroups", R.string.SuperGroups), LocaleController.getString("Channels", R.string.Channels), LocaleController.getString("Bots", R.string.Bots)},
                    new boolean[]{showDSBtnUsers, showDSBtnGroups, showDSBtnSGroups, showDSBtnChannels, showDSBtnBots},
                    new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            if (which == 0) {
                                editor.putBoolean("showDSBtnUsers", isChecked);
                                Theme.plusShowDSBtnUsers = isChecked;
                            } else if (which == 1) {
                                editor.putBoolean("showDSBtnGroups", isChecked);
                                Theme.plusShowDSBtnGroups = isChecked;
                            } else if (which == 2) {
                                editor.putBoolean("showDSBtnSGroups", isChecked);
                                Theme.plusShowDSBtnSGroups = isChecked;
                            } else if (which == 3) {
                                editor.putBoolean("showDSBtnChannels", isChecked);
                                Theme.plusShowDSBtnChannels = isChecked;
                            } else if (which == 4) {
                                editor.putBoolean("showDSBtnBots", isChecked);
                                Theme.plusShowDSBtnBots = isChecked;
                            }
                            editor.apply();

                        }
                    });
        }

        return builder;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }

        if(Theme.usePlusTheme)updateTheme();
        fixLayout();
    }

    private void updateTheme(){
        actionBar.setBackgroundColor(Theme.prefActionbarColor);
        actionBar.setTitleColor(Theme.prefActionbarTitleColor);
        Drawable back = getParentActivity().getResources().getDrawable(R.drawable.ic_ab_back);
        back.setColorFilter(Theme.prefActionbarIconsColor, PorterDuff.Mode.MULTIPLY);
        actionBar.setBackButtonDrawable(back);

        Drawable other = getParentActivity().getResources().getDrawable(R.drawable.ic_ab_other);
        other.setColorFilter(Theme.prefActionbarIconsColor, PorterDuff.Mode.MULTIPLY);
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
                    fragmentView.getViewTreeObserver().removeOnPreDrawListener(this);
                }
                return true;
            }
        });
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.refreshTabs) {
            int i = (int) args[0];
            //Log.e("PlusSettings", "didReceivedNotification refreshTabs i " + i);
            if(i == 15){
                if (listView != null) {
                    listView.invalidateViews();
                }
            }
        }
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
            return  i == showAndroidEmojiRow || i == useDeviceFontRow || i == emojiPopupSize || i == dialogsTabsTextSizeRow || i == dialogsTabsHeightRow || i == dialogsTabsRow || i == dialogsManageTabsRow || i == chatShowDirectShareBtn || i == profileSharedOptionsRow ||
                    i == disableAudioStopRow || i == disableMessageClickRow || i == chatDirectShareToMenu || i == chatDirectShareReplies || i == chatDirectShareFavsFirst || i == chatShowEditedMarkRow ||
                    i == chatShowDateToastRow || i == chatHideLeftGroupRow || i == chatHideJoinedGroupRow || i == chatHideBotKeyboardRow || i == dialogsHideTabsCheckRow || i == dialogsDisableTabsAnimationCheckRow ||
                    i == dialogsInfiniteTabsSwipe || i == dialogsHideTabsCounters || i == dialogsTabsCountersCountChats || i == dialogsTabsCountersCountNotMuted || i == chatSearchUserOnTwitterRow ||
                    i == keepOriginalFilenameRow || i == dialogsPicClickRow || i == dialogsGroupPicClickRow || i == hideMobileNumberRow || i == showUsernameRow ||
                    i == notificationInvertMessagesOrderRow || i == savePlusSettingsRow || i == restorePlusSettingsRow || i == resetPlusSettingsRow || i == chatPhotoQualityRow ||
                    i == chatShowPhotoQualityBarRow || i == dialogsTabsTextModeRow || i == dialogsExpandTabsRow || i == dialogsDisableTabsScrollingRow || i == dialogsDoNotChangeHeaderTitleRow  || i == dialogsTabsToBottomRow || i == dialogsHideSelectedTabIndicator || i == showMySettingsRow || i == showTypingToastNotificationRow ||
                    i == toastNotificationSizeRow || i == toastNotificationPaddingRow || i == toastNotificationToBottomRow || i == toastNotificationPositionRow || i == showOnlineToastNotificationRow ||
                    i == showOfflineToastNotificationRow || i == showToastOnlyIfContactFavRow || i == enableDirectReplyRow || i == chatShowQuickBarRow || (i == chatVerticalQuickBarRow /*&& Theme.plusShowQuickBar*/) ||
                    (i == chatAlwaysBackToMainRow /*&& Theme.plusShowQuickBar*/) || (i == chatDoNotCloseQuickBarRow /*&& Theme.plusShowQuickBar*/) || (i == chatHideQuickBarOnScrollRow /*&& Theme.plusShowQuickBar*/) ||
                    (i == chatCenterQuickBarBtnRow /*&& Theme.plusShowQuickBar*/) || (i == chatShowMembersQuickBarRow /*&& Theme.plusShowQuickBar*/) || i == chatSaveToCloudQuoteRow || i == chatSwipeToReplyRow ||
                    i == hideNotificationsIfPlayingRow || i == chatHideInstantCameraRow || i == chatDoNotHideStickersTabRow || i == chatPhotoViewerHideStatusBarRow || i == chatsToLoadRow ||
                    i == profileEnableGoToMsgRow || i == chatDrawSingleBigEmojiRow || i == dialogsLimitTabsCountersRow || i == chatMarkdownRow || i == moveVersionToSettingsRow ||
                    i == dialogsShowAllInAdminTabRow || i == chatShowUserBioRow;
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
                prefix = "P";
                if (i == settingsSectionRow2) {
                    prefix = prefix + "0 ";
                } else if (i < dialogsSectionRow2) {
                    prefix = prefix + "0." + i + " ";
                } else if (i == dialogsSectionRow2) {
                    prefix = prefix + "1 ";
                } else if (i > dialogsSectionRow2 && i < messagesSectionRow2){
                    prefix = prefix + "1." + (i - dialogsSectionRow2) + " ";
                } else if (i == messagesSectionRow2) {
                    prefix = prefix + "2 ";
                } else if (i > messagesSectionRow2 && i < drawerSectionRow2){
                    prefix = prefix + "2." + (i - messagesSectionRow2) + " ";
                } else if (i == drawerSectionRow2) {
                    prefix = prefix + "3 ";
                } else if (i > drawerSectionRow2 && i < profileSectionRow2){
                    prefix = prefix + "3." + (i - drawerSectionRow2) + " ";
                } else if (i == profileSectionRow2) {
                    prefix = prefix + "4 ";
                } else if (i > profileSectionRow2 && i < notificationSection2Row){
                    prefix = prefix + "4." + (i - profileSectionRow2) + " ";
                } else if (i == notificationSection2Row) {
                    prefix = prefix + "5 ";
                } else if (i > notificationSection2Row && i < toastNotificationSection2Row){
                    prefix = prefix + "5." + (i - notificationSection2Row) + " ";
                } else if (i == toastNotificationSection2Row) {
                    prefix = prefix + "6 ";
                } else if (i > toastNotificationSection2Row && i < privacySectionRow2){
                    prefix = prefix + "6." + (i - toastNotificationSection2Row - (i <= showOnlineToastNotificationDetailRow ? 0 : 1)) + " ";
                } else if (i == privacySectionRow2) {
                    prefix = prefix + "7 ";
                } else if (i > privacySectionRow2 && i < mediaDownloadSection2){
                    prefix = prefix + "7." + (i - privacySectionRow2) + " ";
                }  else if (i == mediaDownloadSection2) {
                    prefix = prefix + "8 ";
                } else if (i > mediaDownloadSection2 && i < plusSettingsSectionRow2){
                    prefix = prefix + "8." + (i - mediaDownloadSection2) + " ";
                }
            }

            if (type == 0) {
                if (view == null) {
                    view = new ShadowSectionCell(mContext);
                }
            } else if (type == 1) {
                if (view == null) {
                    view = new HeaderCell(mContext);
                }
                if (i == settingsSectionRow2) {
                    ((HeaderCell) view).setText(prefix + LocaleController.getString("General", R.string.General));
                } else if (i == messagesSectionRow2) {
                    ((HeaderCell) view).setText(prefix + LocaleController.getString("MessagesSettings", R.string.MessagesSettings));
                } else if (i == profileSectionRow2) {
                    ((HeaderCell) view).setText(prefix + LocaleController.getString("ProfileScreen", R.string.ProfileScreen));
                } else if (i == drawerSectionRow2) {
                    ((HeaderCell) view).setText(prefix + LocaleController.getString("NavigationDrawer", R.string.NavigationDrawer));
                } else if (i == privacySectionRow2) {
                    ((HeaderCell) view).setText(prefix + LocaleController.getString("PrivacySettings", R.string.PrivacySettings));
                } else if (i == mediaDownloadSection2) {
                    ((HeaderCell) view).setText(prefix + LocaleController.getString("SharedMedia", R.string.SharedMedia));
                } else if (i == dialogsSectionRow2) {
                    ((HeaderCell) view).setText(prefix + LocaleController.getString("DialogsSettings", R.string.DialogsSettings));
                } else if (i == notificationSection2Row) {
                    ((HeaderCell) view).setText(prefix + LocaleController.getString("Notifications", R.string.Notifications));
                } else if (i == toastNotificationSection2Row) {
                    ((HeaderCell) view).setText(prefix + LocaleController.getString("ToastNotification", R.string.ToastNotification));
                } else if (i == plusSettingsSectionRow2) {
                    ((HeaderCell) view).setText(LocaleController.getString("PlusSettings", R.string.PlusSettings));
                }
            } else if (type == 2) {
                if (view == null) {
                    view = new TextSettingsCell(mContext);
                }
                TextSettingsCell textCell = (TextSettingsCell) view;
                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                if (i == emojiPopupSize) {
                    textCell.setTag("emojiPopupSize");
                    int size = preferences.getInt("emojiPopupSize", AndroidUtilities.isTablet() ? 65 : 60);
                    textCell.setTextAndValue(prefix + LocaleController.getString("EmojiPopupSize", R.string.EmojiPopupSize), String.format("%d", size), true);
                } else if (i == chatPhotoQualityRow) {
                    textCell.setTag("photoQuality");
                    textCell.setTextAndValue(prefix + LocaleController.getString("PhotoQuality", R.string.PhotoQuality), String.format("%d", Theme.plusPhotoQuality), true);
                } else if (i == dialogsTabsTextSizeRow) {
                    textCell.setTag("chatsTabsTextSize");
                    textCell.setTextAndValue(prefix + LocaleController.getString("TabsTextSize", R.string.TabsTextSize), String.format("%d", Theme.plusTabsTextSize), true);
                } else if (i == dialogsTabsHeightRow) {
                    textCell.setTag("tabsHeight");
                    //int size = preferences.getInt("tabsHeight", AndroidUtilities.isTablet() ? 42 : 40);
                    textCell.setTextAndValue(prefix + LocaleController.getString("TabsHeight", R.string.TabsHeight), String.format("%d", Theme.plusTabsHeight), true);
                } else if (i == dialogsPicClickRow) {
                    textCell.setTag("dialogsClickOnPic");
                    String value;
                    int sort = preferences.getInt("dialogsClickOnPic", 0);
                    if (sort == 0) {
                        value = LocaleController.getString("RowGradientDisabled", R.string.RowGradientDisabled);
                    } else if (sort == 1) {
                        value = LocaleController.getString("ShowPics", R.string.ShowPics);
                    } else {
                        value = LocaleController.getString("ShowProfile", R.string.ShowProfile);
                    }
                    textCell.setTextAndValue(prefix + LocaleController.getString("ClickOnContactPic", R.string.ClickOnContactPic), value, true);
                } else if (i == dialogsGroupPicClickRow) {
                    textCell.setTag("dialogsClickOnGroupPic");
                    String value;
                    int sort = preferences.getInt("dialogsClickOnGroupPic", 0);
                    if (sort == 0) {
                        value = LocaleController.getString("RowGradientDisabled", R.string.RowGradientDisabled);
                    } else if (sort == 1) {
                        value = LocaleController.getString("ShowPics", R.string.ShowPics);
                    } else {
                        value = LocaleController.getString("ShowProfile", R.string.ShowProfile);
                    }
                    textCell.setTextAndValue(prefix + LocaleController.getString("ClickOnGroupPic", R.string.ClickOnGroupPic), value, true);
                } else if (i == toastNotificationSizeRow) {
                    textCell.setTag("toastNotificationSize");
                    textCell.setTextAndValue(prefix + LocaleController.getString("ToastNotificationSize", R.string.ToastNotificationSize), String.format("%d", Theme.plusToastNotificationSize), true);
                } else if (i == toastNotificationPaddingRow) {
                    textCell.setTag("toastNotificationPadding");
                    textCell.setTextAndValue(prefix + LocaleController.getString("ToastNotificationPadding", R.string.ToastNotificationPadding), String.format("%d", Theme.plusToastNotificationPadding), true);
                } else if (i == toastNotificationPositionRow) {
                    textCell.setTag("toastNotificationPosition");
                    String value;
                    int sort = Theme.plusToastNotificationPosition;
                    if (sort == 0) {
                        value = LocaleController.getString("Left", R.string.Left);
                    } else if (sort == 1) {
                        value = LocaleController.getString("Center", R.string.Center);
                    } else {
                        value = LocaleController.getString("Right", R.string.Right);
                    }
                    textCell.setTextAndValue(prefix + LocaleController.getString("ToastNotificationPosition", R.string.ToastNotificationPosition), value, true);
                } else if (i == chatsToLoadRow) {
                    textCell.setTag("chatsToLoad");
                    String title = "Chats to load";
                    int value = preferences.getInt("chatsToLoad", 100);
                    if (value == 50) {
                        textCell.setTextAndValue(title, "50", true);
                    } else if (value == 100) {
                        textCell.setTextAndValue(title, "100", true);
                    } else if (value == 200) {
                        textCell.setTextAndValue(title, "200", true);
                    } else if (value == 300) {
                        textCell.setTextAndValue(title, "300", true);
                    } else if (value == 400) {
                        textCell.setTextAndValue(title, "400", true);
                    } else if (value == 500) {
                        textCell.setTextAndValue(title, "500", true);
                    } else if (value == 750) {
                        textCell.setTextAndValue(title, "750", true);
                    } else if (value == 1000) {
                        textCell.setTextAndValue(title, "1000", true);
                    } else if (value == 1500) {
                        textCell.setTextAndValue(title, "1500", true);
                    } else if (value == 2000) {
                        textCell.setTextAndValue(title, "2000", true);
                    } else if (value == 1000000) {
                        textCell.setTextAndValue(title, "All", true);
                    }
                } if (i == dialogsManageTabsRow) {
                    textCell.setText(prefix + LocaleController.getString("SortTabs", R.string.SortTabs), true);
                }
            } else if (type == 3) {
                if (view == null) {
                    view = new TextCheckCell(mContext);
                }
                TextCheckCell textCell = (TextCheckCell) view;

                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                if (i == disableAudioStopRow) {
                    textCell.setTag("disableAudioStop");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("DisableAudioStop", R.string.DisableAudioStop), preferences.getBoolean("disableAudioStop", false), true);
                } else if (i == disableMessageClickRow) {
                    textCell.setTag("disableMessageClick");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("DisableMessageClick", R.string.DisableMessageClick), preferences.getBoolean("disableMessageClick", false), true);
                } else if (i == chatDirectShareReplies) {
                    textCell.setTag("directShareReplies");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("DirectShareReplies", R.string.DirectShareReplies), preferences.getBoolean("directShareReplies", false), true);
                } else if (i == chatDirectShareToMenu) {
                    textCell.setTag("directShareToMenu");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("DirectShareToMenu", R.string.DirectShareToMenu), preferences.getBoolean("directShareToMenu", false), true);
                } else if (i == chatDirectShareFavsFirst) {
                    textCell.setTag("directShareFavsFirst");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("DirectShareShowFavsFirst", R.string.DirectShareShowFavsFirst), preferences.getBoolean("directShareFavsFirst", false), true);
                } else if (i == chatShowEditedMarkRow) {
                    textCell.setTag("showEditedMark");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("ShowEditedMark", R.string.ShowEditedMark), preferences.getBoolean("showEditedMark", true), true);
                } else if (i == chatShowDateToastRow) {
                    textCell.setTag("showDateToast");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("ShowDateToast", R.string.ShowDateToast), preferences.getBoolean("showDateToast", true), true);
                } else if (i == chatHideLeftGroupRow) {
                    textCell.setTag("hideLeftGroup");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("HideLeftGroup", R.string.HideLeftGroup), preferences.getBoolean("hideLeftGroup", false), true);
                } else if (i == chatHideJoinedGroupRow) {
                    textCell.setTag("hideJoinedGroup");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("HideJoinedGroup", R.string.HideJoinedGroup), preferences.getBoolean("hideJoinedGroup", false), true);
                } else if (i == chatHideBotKeyboardRow) {
                    textCell.setTag("hideBotKeyboard");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("HideBotKeyboard", R.string.HideBotKeyboard), preferences.getBoolean("hideBotKeyboard", false), true);
                } else if (i == keepOriginalFilenameRow) {
                    textCell.setTag("keepOriginalFilename");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("KeepOriginalFilename", R.string.KeepOriginalFilename), preferences.getBoolean("keepOriginalFilename", false), false);
                } else if (i == showAndroidEmojiRow) {
                    textCell.setTag("showAndroidEmoji");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("ShowAndroidEmoji", R.string.ShowAndroidEmoji), preferences.getBoolean("showAndroidEmoji", false), true);
                } else if (i == useDeviceFontRow) {
                    textCell.setTag("useDeviceFont");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("UseDeviceFont", R.string.UseDeviceFont), preferences.getBoolean("useDeviceFont", false), true);
                } else if (i == dialogsHideTabsCheckRow) {
                    textCell.setTag("hideTabs");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("HideTabs", R.string.HideTabs), Theme.plusHideTabs, true);
                } else if (i == dialogsDisableTabsAnimationCheckRow) {
                    textCell.setTag("disableTabsAnimation");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("DisableTabsAnimation", R.string.DisableTabsAnimation), Theme.plusDisableTabsAnimation, true);
                } else if (i == dialogsTabsTextModeRow) {
                    textCell.setTag("chatsTabTitlesMode");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("ShowTabTitle", R.string.ShowTabTitle), Theme.plusTabTitlesMode, true);
                } else if (i == dialogsExpandTabsRow) {
                    textCell.setTag("tabsShouldExpand");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("FitTabsToScreen", R.string.FitTabsToScreen), Theme.plusTabsShouldExpand, true);
                } else if (i == dialogsTabsToBottomRow) {
                    textCell.setTag("tabsToBottom");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("TabsToBottom", R.string.TabsToBottom), preferences.getBoolean("tabsToBottom", false), true);
                } else if (i == dialogsDisableTabsScrollingRow) {
                    textCell.setTag("disableTabsScrolling");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("DisableTabsScrolling", R.string.DisableTabsScrolling), Theme.plusDisableTabsScrolling, true);
                } else if (i == dialogsHideSelectedTabIndicator) {
                    textCell.setTag("hideSelectedTabIndicator");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("HideSelectedTabIndicator", R.string.HideSelectedTabIndicator), preferences.getBoolean("hideSelectedTabIndicator", false), true);
                } else if (i == dialogsInfiniteTabsSwipe) {
                    textCell.setTag("infiniteTabsSwipe");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("InfiniteSwipe", R.string.InfiniteSwipe), Theme.plusInfiniteTabsSwipe, true);
                } else if (i == dialogsHideTabsCounters) {
                    textCell.setTag("hideTabsCounters");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("HideTabsCounters", R.string.HideTabsCounters), Theme.plusHideTabsCounters, true);
                } else if (i == dialogsTabsCountersCountChats) {
                    textCell.setTag("tabsCountersCountChats");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("HeaderTabCounterCountChats", R.string.HeaderTabCounterCountChats), Theme.plusTabsCountersCountChats, true);
                } else if (i == dialogsTabsCountersCountNotMuted) {
                    textCell.setTag("tabsCountersCountNotMuted");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("HeaderTabCounterCountNotMuted", R.string.HeaderTabCounterCountNotMuted), Theme.plusTabsCountersCountNotMuted, true);
                } else if (i == hideMobileNumberRow) {
                    textCell.setTag("hideMobile");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("HideMobile", R.string.HideMobile), Theme.plusHideMobile, true);
                } else if (i == showUsernameRow) {
                    textCell.setTag("showUsername");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("ShowUsernameInMenu", R.string.ShowUsernameInMenu), Theme.plusShowUsername, true);
                } else if (i == notificationInvertMessagesOrderRow) {
                    textCell.setTag("invertMessagesOrder");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("InvertMessageOrder", R.string.InvertMessageOrder), preferences.getBoolean("invertMessagesOrder", false), false);
                } else if (i == chatSearchUserOnTwitterRow) {
                    textCell.setTag("searchOnTwitter");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("SearchUserOnTwitter", R.string.SearchUserOnTwitter), preferences.getBoolean("searchOnTwitter", true), true);
                } else if (i == chatShowPhotoQualityBarRow) {
                    textCell.setTag("showPhotoQualityBar");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("ShowPhotoQualityBar", R.string.ShowPhotoQualityBar), Theme.plusShowPhotoQualityBar, false);
                } else if (i == showTypingToastNotificationRow) {
                    textCell.setTag("showTypingToast");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("ShowTypingToast", R.string.ShowTypingToast), Theme.plusShowTypingToast, true);
                } else if (i == toastNotificationToBottomRow) {
                    textCell.setTag("toastNotificationToBottom");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("ToastNotificationToBottom", R.string.ToastNotificationToBottom), Theme.plusToastNotificationToBottom, true);
                } else if (i == showOnlineToastNotificationRow) {
                    textCell.setTag("plusShowOnlineToast");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("ShowOnlineToast", R.string.ShowOnlineToast), Theme.plusShowOnlineToast, false);
                } else if (i == showToastOnlyIfContactFavRow) {
                    textCell.setTag("showOnlyIfContactFav");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("ShowOnlyIfContactFav", R.string.ShowOnlyIfContactFav), Theme.plusShowOnlyIfContactFav, true);
                } else if (i == showOfflineToastNotificationRow) {
                    textCell.setTag("showOfflineToast");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("ShowOfflineToast", R.string.ShowOfflineToast), Theme.plusShowOfflineToast, true);
                } else if (i == hideNotificationsIfPlayingRow) {
                    textCell.setTag("plusHideNotificationsIfPlaying");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("OverrideNotificationsIfPlaying", R.string.OverrideNotificationsIfPlaying), Theme.plusHideNotificationsIfPlaying, true);
                } else if (i == enableDirectReplyRow) {
                    textCell.setTag("enableDirectReply");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("EnableDirectReply", R.string.EnableDirectReply), Theme.plusEnableDirectReply, true);
                } else if (i == chatShowQuickBarRow) {
                    textCell.setTag("showQuickBar");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("ShowQuickBar", R.string.ShowQuickBar), Theme.plusShowQuickBar, false);
                } else if (i == chatVerticalQuickBarRow) {
                    textCell.setTag("verticalQuickBar");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("VerticalQuickBar", R.string.VerticalQuickBar), Theme.plusVerticalQuickBar, false);
                    //textCell.setEnabled(Theme.plusShowQuickBar);
                } else if (i == chatAlwaysBackToMainRow) {
                    textCell.setTag("alwaysBackToMain");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("AlwaysBackToMain", R.string.AlwaysBackToMain), Theme.plusAlwaysBackToMain, false);
                    //textCell.setEnabled(Theme.plusShowQuickBar);
                } else if (i == chatDoNotCloseQuickBarRow) {
                    textCell.setTag("doNotCloseQuickBar");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("DoNotCloseQuickBar", R.string.DoNotCloseQuickBar), Theme.plusDoNotCloseQuickBar, false);
                    //textCell.setEnabled(Theme.plusShowQuickBar);
                } else if (i == chatHideQuickBarOnScrollRow) {
                    textCell.setTag("hideQuickBarOnScroll");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("HideQuickBarOnScroll", R.string.HideQuickBarOnScroll), Theme.plusHideQuickBarOnScroll, false);
                    //textCell.setEnabled(Theme.plusShowQuickBar);
                } else if (i == chatCenterQuickBarBtnRow) {
                    textCell.setTag("centerQuickBarBtn");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("CenterQuickBarButton", R.string.CenterQuickBarButton), Theme.plusCenterQuickBarBtn, false);
                    //textCell.setEnabled(Theme.plusShowQuickBar);
                } else if (i == chatShowMembersQuickBarRow) {
                    textCell.setTag("quickBarShowMembers");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("ShowMembersOnQuickBar", R.string.ShowMembersOnQuickBar), Theme.plusQuickBarShowMembers, true);
                    //textCell.setEnabled(Theme.plusShowQuickBar);
                } else if (i == chatSaveToCloudQuoteRow) {
                    textCell.setTag("saveToCloudQuote");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("SaveToCloudQuote", R.string.SaveToCloudQuote), Theme.plusSaveToCloudQuote, true);
                } else if (i == chatSwipeToReplyRow) {
                    textCell.setTag("plusSwipeToReply");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("SwipeToReply", R.string.SwipeToReply), Theme.plusSwipeToReply, true);
                } else if (i == chatHideInstantCameraRow) {
                    textCell.setTag("hideInstantCamera");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("HideInstantCamera", R.string.HideInstantCamera), Theme.plusHideInstantCamera, true);
                } else if (i == chatDoNotHideStickersTabRow) {
                    textCell.setTag("doNotHideStickersTab");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("DoNotHideStickersTab", R.string.DoNotHideStickersTab), Theme.plusDoNotHideStickersTab, true);
                } else if (i == chatPhotoViewerHideStatusBarRow) {
                    textCell.setTag("photoViewerHideStatusBar");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("HideStatusBar", R.string.HideStatusBar), Theme.plusPhotoViewerHideStatusBar, false);
                } else if (i == profileEnableGoToMsgRow) {
                    textCell.setTag("profileEnableGoToMsg");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("EnableGoToMessage", R.string.EnableGoToMessage), Theme.plusProfileEnableGoToMsg, true);
                } else if (i == dialogsDoNotChangeHeaderTitleRow) {
                    textCell.setTag("doNotChangeHeaderTitle");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("DoNotChangeHeaderTitle", R.string.DoNotChangeHeaderTitle), Theme.plusDoNotChangeHeaderTitle, true);
                } else if (i == chatDrawSingleBigEmojiRow) {
                    textCell.setTag("plusDrawSingleBigEmoji");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("EmojiBigSize", R.string.EmojiBigSize), Theme.plusDrawSingleBigEmoji, true);
                } else if (i == dialogsLimitTabsCountersRow) {
                    textCell.setTag("plusLimitTabsCounters");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("LimitTabsCounter", R.string.LimitTabsCounter), Theme.plusLimitTabsCounters, true);
                } else if (i == chatMarkdownRow) {
                    textCell.setTag("enableMarkdown");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("Markdown", R.string.Markdown), Theme.plusEnableMarkdown, true);
                } else if (i == moveVersionToSettingsRow) {
                    textCell.setTag("moveVersionToSettings");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("MoveVersionToSettings", R.string.MoveVersionToSettings), Theme.plusMoveVersionToSettings, true);
                } else if (i == dialogsShowAllInAdminTabRow) {
                    textCell.setTag("showAllInAdminTab");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("ShowAllInAdminTab", R.string.ShowAllInAdminTab), Theme.plusShowAllInAdminTab, true);
                } else if (i == chatShowUserBioRow) {
                    textCell.setTag("showUserBio");
                    textCell.setTextAndCheck(prefix + LocaleController.getString("ShowUserBio", R.string.ShowUserBio), Theme.plusShowUserBio, false);
                }
            } else if (type == 6) {
                if (view == null) {
                    view = new TextDetailSettingsCell(mContext);
                }
                TextDetailSettingsCell textCell = (TextDetailSettingsCell) view;

                if (i == dialogsTabsRow) {
                    String value;
                    //SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);

                    //boolean hideUsers = preferences.getBoolean("hideUsers", false);
                    //boolean hideGroups = preferences.getBoolean("hideGroups", false);
                    //boolean hideSGroups = preferences.getBoolean("hideSGroups", false);
                    //boolean hideChannels = preferences.getBoolean("hideChannels", false);
                    //boolean hideBots = preferences.getBoolean("hideBots", false);
                    //boolean hideFavs = preferences.getBoolean("hideFavs", false);

                    value = prefix + LocaleController.getString("HideShowTabs", R.string.HideShowTabs);

                    String text = "";
                    if (!Theme.plusHideAllTab) {
                        text += LocaleController.getString("All", R.string.All);
                    }
                    if (!Theme.plusHideUsersTab) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("Users", R.string.Users);
                    }
                    if (!Theme.plusHideGroupsTab) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("Groups", R.string.Groups);
                    }
                    if (!Theme.plusHideSuperGroupsTab) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("SuperGroups", R.string.SuperGroups);
                    }
                    if (!Theme.plusHideChannelsTab) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("Channels", R.string.Channels);
                    }
                    if (!Theme.plusHideBotsTab) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("Bots", R.string.Bots);
                    }
                    if (!Theme.plusHideFavsTab) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("Favorites", R.string.Favorites);
                    }
                    if (text.length() == 0) {
                        text = "";
                    }
                    textCell.setTextAndValue(value, text, true);
                } else if (i == chatShowDirectShareBtn) {
                    String value;
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    boolean showDSBtnUsers = preferences.getBoolean("showDSBtnUsers", false);
                    boolean showDSBtnGroups = preferences.getBoolean("showDSBtnGroups", true);
                    boolean showDSBtnSGroups = preferences.getBoolean("showDSBtnSGroups", true);
                    boolean showDSBtnChannels = preferences.getBoolean("showDSBtnChannels", true);
                    boolean showDSBtnBots = preferences.getBoolean("showDSBtnBots", true);

                    value = prefix + LocaleController.getString("ShowDirectShareButton", R.string.ShowDirectShareButton);

                    String text = "";
                    if (showDSBtnUsers) {
                        text += LocaleController.getString("Users", R.string.Users);
                    }
                    if (showDSBtnGroups) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("Groups", R.string.Groups);
                    }
                    if (showDSBtnSGroups) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("SuperGroups", R.string.SuperGroups);
                    }
                    if (showDSBtnChannels) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("Channels", R.string.Channels);
                    }
                    if (showDSBtnBots) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("Bots", R.string.Bots);
                    }

                    if (text.length() == 0) {
                        text = LocaleController.getString("Channels", R.string.UsernameEmpty);
                    }
                    textCell.setTextAndValue(value, text, true);
                } else if (i == profileSharedOptionsRow) {
                    String value;
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);

                    boolean hideMedia = preferences.getBoolean("hideSharedMedia", false);
                    boolean hideFiles = preferences.getBoolean("hideSharedFiles", false);
                    boolean hideMusic = preferences.getBoolean("hideSharedMusic", false);
                    boolean hideLinks = preferences.getBoolean("hideSharedLinks", false);

                    value = prefix + LocaleController.getString("SharedMedia", R.string.SharedMedia);

                    String text = "";
                    if (!hideMedia) {
                        text += LocaleController.getString("SharedMediaTitle", R.string.SharedMediaTitle);
                    }
                    if (!hideFiles) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("DocumentsTitle", R.string.DocumentsTitle);
                    }
                    if (!hideMusic) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("AudioTitle", R.string.AudioTitle);
                    }
                    if (!hideLinks /*&& BuildVars.DEBUG_VERSION*/) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("LinksTitle", R.string.LinksTitle);
                    }

                    if (text.length() == 0) {
                        text = "";
                    }
                    textCell.setTextAndValue(value, text, true);
                } /*else if (i == notificationMuteNewChats) {
                    String value;
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);

                    final int USER = 1;         // 000001
                    final int ENCRYPTED = 2;    // 000010
                    final int GROUP = 4;        // 000100
                    final int SUPERGROUP = 8;   // 001000
                    final int CHANNEL = 16;     // 010000
                    final int BOT = 32;         // 100000

                    final int MUTE_FLAGS = preferences.getInt("muteIfNew", 0);
                    boolean muteUser = (MUTE_FLAGS & USER) == USER;
                    boolean muteEncrypted = (MUTE_FLAGS & ENCRYPTED) == ENCRYPTED;
                    boolean muteGroup = (MUTE_FLAGS & GROUP) == GROUP;
                    boolean muteSGroup = (MUTE_FLAGS & SUPERGROUP) == SUPERGROUP;
                    boolean muteChannel = (MUTE_FLAGS & CHANNEL) == CHANNEL;
                    boolean muteBot = (MUTE_FLAGS & BOT) == BOT;

                    value = LocaleController.getString("MuteNewChats", R.string.MuteNewChats);

                    String text = "";
                    if (muteUser) {
                        text += LocaleController.getString("Users", R.string.Users);
                    }
                    if (muteEncrypted) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("EncryptedChat", R.string.EncryptedChat);
                    }
                    if (muteGroup) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("Groups", R.string.Groups);
                    }
                    if (muteSGroup) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("SuperGroups", R.string.SuperGroups);
                    }
                    if (muteChannel) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("Channels", R.string.Channels);
                    }
                    if (muteBot) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("Bots", R.string.Bots);
                    }

                    if (text.length() == 0) {
                        text = "";
                    }
                    textCell.setTextAndValue(value, text, true);
                }*/ else if (i == showMySettingsRow) {
                    String value;
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);

                    final int VERSION = 1;         // 000001
                    final int LANGUAGE = 2;    // 000010
                    //final int GROUP = 4;        // 000100
                    //final int SUPERGROUP = 8;   // 001000
                    //final int CHANNEL = 16;     // 010000
                    //final int BOT = 32;         // 100000

                    final int FLAGS = preferences.getInt("showMySettings", 0);
                    boolean showVersion = (FLAGS & VERSION) == VERSION;
                    boolean showLanguage = (FLAGS & LANGUAGE) == LANGUAGE;
                    //boolean muteGroup = (MUTE_FLAGS & GROUP) == GROUP;
                    //boolean muteSGroup = (MUTE_FLAGS & SUPERGROUP) == SUPERGROUP;
                    //boolean muteChannel = (MUTE_FLAGS & CHANNEL) == CHANNEL;
                    //boolean muteBot = (MUTE_FLAGS & BOT) == BOT;

                    value = prefix + LocaleController.getString("ShowMySettings", R.string.ShowMySettings);

                    String text = "";
                    if (showVersion) {
                        text += LocaleController.getString("PlusVersion", R.string.PlusVersion);
                    }
                    if (showLanguage) {
                        if (text.length() != 0) {
                            text += ", ";
                        }
                        text += LocaleController.getString("Language", R.string.Language);
                    }

                    if (text.length() == 0) {
                        text = "";
                    }
                    textCell.setTextAndValue(value, text, true);
                } else if (i == savePlusSettingsRow) {
                    textCell.setMultilineDetail(true);
                    textCell.setTextAndValue(LocaleController.getString("SaveSettings", R.string.SaveSettings), LocaleController.getString("SaveSettingsSum", R.string.SaveSettingsSum)
                            + " (" + LocaleController.getString("AlsoFavorites", R.string.AlsoFavorites)+")", true);
                } else if (i == restorePlusSettingsRow) {
                    textCell.setMultilineDetail(true);
                    textCell.setTextAndValue(LocaleController.getString("RestoreSettings", R.string.RestoreSettings), LocaleController.getString("RestoreSettingsSum", R.string.RestoreSettingsSum), true);
                } else if (i == resetPlusSettingsRow) {
                    textCell.setMultilineDetail(true);
                    textCell.setTextAndValue(LocaleController.getString("ResetSettings", R.string.ResetSettings), LocaleController.getString("ResetSettingsSum", R.string.ResetSettingsSum), false);
                }
            } else if (type == 7) {
                if (view == null) {
                    view = new TextInfoPrivacyCell(mContext);
                }
                if (i == keepOriginalFilenameDetailRow) {
                    ((TextInfoPrivacyCell) view).setText(LocaleController.getString("KeepOriginalFilenameHelp", R.string.KeepOriginalFilenameHelp));
                } else if (i == showOnlineToastNotificationDetailRow) {
                    ((TextInfoPrivacyCell) view).setText(LocaleController.getString("OnlineToastHelp", R.string.OnlineToastHelp));
                } else if (i == dialogsShowAllInAdminTabDetailRow) {
                    ((TextInfoPrivacyCell) view).setText(LocaleController.getString("ShowAllInAdminTabHelp", R.string.ShowAllInAdminTabHelp));
                }
                view.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                if(Theme.usePlusTheme)view.getBackground().setColorFilter(Theme.prefBGColor, PorterDuff.Mode.SRC_IN);
            }
            if(view != null && type != 7){
                view.setBackgroundColor(Theme.usePlusTheme ? Theme.prefBGColor : Theme.getColor(Theme.key_windowBackgroundWhite));
            }
            return view;
        }

        @Override
        public int getItemViewType(int i) {
            if (i == messagesSectionRow || i == profileSectionRow || i == drawerSectionRow || i == privacySectionRow ||
                    i == mediaDownloadSection || i == dialogsSectionRow || i == notificationSectionRow || i == toastNotificationSectionRow || i == plusSettingsSectionRow) {
                return 0;
            } else if (i == settingsSectionRow2 || i == messagesSectionRow2 || i == profileSectionRow2 || i == drawerSectionRow2 ||
                    i == privacySectionRow2 || i == mediaDownloadSection2 || i == dialogsSectionRow2 || i == notificationSection2Row || i == toastNotificationSection2Row ||
                    i == plusSettingsSectionRow2) {
                return 1;
            } else if (i == disableAudioStopRow || i == disableMessageClickRow || i == dialogsHideTabsCheckRow || i == dialogsDisableTabsAnimationCheckRow || i == dialogsInfiniteTabsSwipe ||
                    i == dialogsHideTabsCounters || i == dialogsTabsCountersCountChats || i == dialogsTabsCountersCountNotMuted || i == showAndroidEmojiRow || i == useDeviceFontRow ||
                    i == keepOriginalFilenameRow || i == hideMobileNumberRow || i == showUsernameRow || i == chatDirectShareToMenu || i == chatDirectShareReplies || i == chatDirectShareFavsFirst ||
                    i == chatShowEditedMarkRow || i == chatShowDateToastRow || i == chatHideLeftGroupRow || i == chatHideJoinedGroupRow || i == chatHideBotKeyboardRow || i == notificationInvertMessagesOrderRow ||
                    i == chatSearchUserOnTwitterRow || i == chatShowPhotoQualityBarRow || i == dialogsTabsTextModeRow || i == dialogsExpandTabsRow || i == dialogsDisableTabsScrollingRow || i == dialogsTabsToBottomRow || i == dialogsHideSelectedTabIndicator ||
                    i == showTypingToastNotificationRow || i == toastNotificationToBottomRow || i == showOnlineToastNotificationRow || i == showOfflineToastNotificationRow ||
                    i == showToastOnlyIfContactFavRow || i == enableDirectReplyRow || i == chatShowQuickBarRow || i == chatVerticalQuickBarRow || i == chatAlwaysBackToMainRow || i == chatDoNotCloseQuickBarRow ||
                    i == chatHideQuickBarOnScrollRow || i == chatCenterQuickBarBtnRow || i == chatShowMembersQuickBarRow || i == chatSaveToCloudQuoteRow || i == chatSwipeToReplyRow ||
                    i == hideNotificationsIfPlayingRow || i == chatHideInstantCameraRow || i == chatDoNotHideStickersTabRow || i == chatPhotoViewerHideStatusBarRow ||
                    i == profileEnableGoToMsgRow || i == dialogsDoNotChangeHeaderTitleRow || i == chatDrawSingleBigEmojiRow || i == dialogsLimitTabsCountersRow || i == chatMarkdownRow ||
                    i == moveVersionToSettingsRow || i == dialogsShowAllInAdminTabRow || i == chatShowUserBioRow) {
                return 3;
            } else if (i == emojiPopupSize || i == dialogsTabsTextSizeRow || i == dialogsTabsHeightRow || i == dialogsPicClickRow || i == dialogsGroupPicClickRow || i == chatPhotoQualityRow
                    || i == toastNotificationSizeRow || i == toastNotificationPaddingRow || i == toastNotificationPositionRow || i == chatsToLoadRow || i == dialogsManageTabsRow) {
                return 2;
            } else if (i == dialogsTabsRow || i == chatShowDirectShareBtn || i == profileSharedOptionsRow || i == savePlusSettingsRow ||
                    i == restorePlusSettingsRow || i == resetPlusSettingsRow || /*i == notificationMuteNewChats ||*/ i == showMySettingsRow) {
                return 6;
            } else if (i == keepOriginalFilenameDetailRow || i == showOnlineToastNotificationDetailRow || i == dialogsShowAllInAdminTabDetailRow) {
                return 7;
            } else {
                return 2;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 8;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}

