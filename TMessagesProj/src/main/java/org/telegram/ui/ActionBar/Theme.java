/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package org.telegram.ui.ActionBar;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.util.StateSet;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.ThemeEditorView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Theme {

    public static class ThemeInfo {
        public String name;
        public String pathToFile;
        public String assetName;

        public JSONObject getSaveJson() {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", name);
                jsonObject.put("path", pathToFile);
                return jsonObject;
            } catch (Exception e) {
                FileLog.e(e);
            }
            return null;
        }

        public String getName() {
            if ("Default".equals(name)) {
                return LocaleController.getString("Default", R.string.Default);
            } else if ("Blue".equals(name)) {
                return LocaleController.getString("ThemeBlue", R.string.ThemeBlue);
            } else if ("Dark".equals(name)) {
                return LocaleController.getString("ThemeDark", R.string.ThemeDark);
            }
            return name;
        }

        public static ThemeInfo createWithJson(JSONObject object) {
            if (object == null) {
                return null;
            }
            try {
                ThemeInfo themeInfo = new ThemeInfo();
                themeInfo.name = object.getString("name");
                themeInfo.pathToFile = object.getString("path");
                return themeInfo;
            } catch (Exception e) {
                FileLog.e(e);
            }
            return null;
        }

        public static ThemeInfo createWithString(String string) {
            if (TextUtils.isEmpty(string)) {
                return null;
            }
            String[] args = string.split("\\|");
            if (args.length != 2) {
                return null;
            }
            ThemeInfo themeInfo = new ThemeInfo();
            themeInfo.name = args[0];
            themeInfo.pathToFile = args[1];
            return themeInfo;
        }
    }

    private static final Object sync = new Object();
    private static final Object wallpaperSync = new Object();

    public static final int ACTION_BAR_PHOTO_VIEWER_COLOR = 0x7f000000;
    public static final int ACTION_BAR_MEDIA_PICKER_COLOR = 0xff333333;
    public static final int ACTION_BAR_VIDEO_EDIT_COLOR = 0xff000000;
    public static final int ACTION_BAR_PLAYER_COLOR = 0xffffffff;
    public static final int ACTION_BAR_PICKER_SELECTOR_COLOR = 0xff3d3d3d;
    public static final int ACTION_BAR_WHITE_SELECTOR_COLOR = 0x40ffffff;
    public static final int ACTION_BAR_AUDIO_SELECTOR_COLOR = 0x2f000000;
    public static final int ARTICLE_VIEWER_MEDIA_PROGRESS_COLOR = 0xffffffff;
    //public static final int INPUT_FIELD_SELECTOR_COLOR = 0xffd6d6d6;

    private static Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public static ArrayList<ThemeInfo> themes;
    private static ArrayList<ThemeInfo> otherThemes;
    private static HashMap<String, ThemeInfo> themesDict;
    private static ThemeInfo currentTheme;
    private static ThemeInfo defaultTheme;
    private static ThemeInfo previousTheme;

    public static PorterDuffColorFilter colorFilter;
    public static PorterDuffColorFilter colorPressedFilter;
    private static int selectedColor;
    private static boolean isCustomTheme;
    private static int serviceMessageColor;
    private static int serviceSelectedMessageColor;
    private static int currentColor;
    private static int currentSelectedColor;
    private static Drawable wallpaper;
    private static Drawable themedWallpaper;
    private static int themedWallpaperFileOffset;

    public static Paint dividerPaint;
    public static Paint linkSelectionPaint;
    public static Paint checkboxSquare_eraserPaint;
    public static Paint checkboxSquare_checkPaint;
    public static Paint checkboxSquare_backgroundPaint;
    public static Paint avatar_backgroundPaint;

    public static Drawable listSelector;
    public static Drawable avatar_broadcastDrawable;
    public static Drawable avatar_photoDrawable;

    public static Paint dialogs_tabletSeletedPaint;
    public static Paint dialogs_pinnedPaint;
    public static Paint dialogs_countPaint;
    public static Paint dialogs_errorPaint;
    public static Paint dialogs_countGrayPaint;
    public static TextPaint dialogs_namePaint;
    public static TextPaint dialogs_nameEncryptedPaint;
    public static TextPaint dialogs_messagePaint;
    public static TextPaint dialogs_messagePrintingPaint;
    public static TextPaint dialogs_timePaint;
    public static TextPaint dialogs_countTextPaint;
    public static TextPaint dialogs_onlinePaint;
    public static TextPaint dialogs_offlinePaint;
    public static Drawable dialogs_checkDrawable;
    public static Drawable dialogs_halfCheckDrawable;
    public static Drawable dialogs_clockDrawable;
    public static Drawable dialogs_errorDrawable;
    public static Drawable dialogs_lockDrawable;
    public static Drawable dialogs_groupDrawable;
    public static Drawable dialogs_broadcastDrawable;
    public static Drawable dialogs_botDrawable;
    public static Drawable dialogs_muteDrawable;
    public static Drawable dialogs_verifiedDrawable;
    public static Drawable dialogs_verifiedCheckDrawable;
    public static Drawable dialogs_pinnedDrawable;
    public static Drawable dialogs_errorIconDrawable;
    //plus
    public static boolean usePlusTheme;
    public static TextPaint dialogs_groupPaint;
    public static TextPaint dialogs_nameUnknownPaint;
    public static TextPaint dialogs_messageTypingPaint;
    public static TextPaint dialogs_mediaPaint;

    public static Drawable dialogs_superGroupDrawable;
    public static Drawable dialogs_FavDrawable;
    //public static GradientDrawable dialogs_StatusBGDrawable;
    //
    public static TextPaint profile_aboutTextPaint;
    public static Drawable profile_verifiedDrawable;
    public static Drawable profile_verifiedCheckDrawable;

    public static Paint chat_docBackPaint;
    public static Paint chat_deleteProgressPaint;
    public static Paint chat_botProgressPaint;
    public static Paint chat_urlPaint;
    public static Paint chat_textSearchSelectionPaint;
    public static Paint chat_instantViewRectPaint;
    public static Paint chat_replyLinePaint;
    public static Paint chat_msgErrorPaint;
    public static Paint chat_statusPaint;
    public static Paint chat_statusRecordPaint;
    public static Paint chat_actionBackgroundPaint;
    public static Paint chat_timeBackgroundPaint;
    public static Paint chat_composeBackgroundPaint;
    public static Paint chat_radialProgressPaint;
    public static TextPaint chat_msgTextPaint;
    public static TextPaint chat_actionTextPaint;
    public static TextPaint chat_msgBotButtonPaint;
    public static TextPaint chat_msgGameTextPaint;
    public static TextPaint chat_msgTextPaintOneEmoji;
    public static TextPaint chat_msgTextPaintTwoEmoji;
    public static TextPaint chat_msgTextPaintThreeEmoji;
    public static TextPaint chat_infoPaint;
    public static TextPaint chat_docNamePaint;
    public static TextPaint chat_locationTitlePaint;
    public static TextPaint chat_locationAddressPaint;
    public static TextPaint chat_durationPaint;
    public static TextPaint chat_gamePaint;
    public static TextPaint chat_shipmentPaint;
    public static TextPaint chat_instantViewPaint;
    public static TextPaint chat_audioTimePaint;
    public static TextPaint chat_audioTitlePaint;
    public static TextPaint chat_audioPerformerPaint;
    public static TextPaint chat_botButtonPaint;
    public static TextPaint chat_contactNamePaint;
    public static TextPaint chat_contactPhonePaint;
    public static TextPaint chat_timePaint;
    public static TextPaint chat_namePaint;
    public static TextPaint chat_forwardNamePaint;
    public static TextPaint chat_replyNamePaint;
    public static TextPaint chat_replyTextPaint;
    public static TextPaint chat_contextResult_titleTextPaint;
    public static TextPaint chat_contextResult_descriptionTextPaint;

    public static Drawable chat_composeShadowDrawable;
    public static Drawable chat_roundVideoShadow;
    public static Drawable chat_msgInDrawable;
    public static Drawable chat_msgInSelectedDrawable;
    public static Drawable chat_msgInShadowDrawable;
    public static Drawable chat_msgOutDrawable;
    public static Drawable chat_msgOutSelectedDrawable;
    public static Drawable chat_msgOutShadowDrawable;
    public static Drawable chat_msgInMediaDrawable;
    public static Drawable chat_msgInMediaSelectedDrawable;
    public static Drawable chat_msgInMediaShadowDrawable;
    public static Drawable chat_msgOutMediaDrawable;
    public static Drawable chat_msgOutMediaSelectedDrawable;
    public static Drawable chat_msgOutMediaShadowDrawable;
    public static Drawable chat_msgOutCheckDrawable;
    public static Drawable chat_msgOutCheckSelectedDrawable;
    public static Drawable chat_msgOutHalfCheckDrawable;
    public static Drawable chat_msgOutHalfCheckSelectedDrawable;
    public static Drawable chat_msgOutClockDrawable;
    public static Drawable chat_msgOutSelectedClockDrawable;
    public static Drawable chat_msgInClockDrawable;
    public static Drawable chat_msgInSelectedClockDrawable;
    public static Drawable chat_msgMediaCheckDrawable;
    public static Drawable chat_msgMediaHalfCheckDrawable;
    public static Drawable chat_msgMediaClockDrawable;
    public static Drawable chat_msgStickerCheckDrawable;
    public static Drawable chat_msgStickerHalfCheckDrawable;
    public static Drawable chat_msgStickerClockDrawable;
    public static Drawable chat_msgStickerViewsDrawable;
    public static Drawable chat_msgInViewsDrawable;
    public static Drawable chat_msgInViewsSelectedDrawable;
    public static Drawable chat_msgOutViewsDrawable;
    public static Drawable chat_msgOutViewsSelectedDrawable;
    public static Drawable chat_msgMediaViewsDrawable;
    public static Drawable chat_msgInMenuDrawable;
    public static Drawable chat_msgInMenuSelectedDrawable;
    public static Drawable chat_msgOutMenuDrawable;
    public static Drawable chat_msgOutMenuSelectedDrawable;
    public static Drawable chat_msgMediaMenuDrawable;
    public static Drawable chat_msgInInstantDrawable;
    public static Drawable chat_msgOutInstantDrawable;
    public static Drawable chat_msgErrorDrawable;
    public static Drawable chat_muteIconDrawable;
    public static Drawable chat_lockIconDrawable;
    public static Drawable chat_inlineResultFile;
    public static Drawable chat_inlineResultAudio;
    public static Drawable chat_inlineResultLocation;
    public static Drawable chat_msgOutBroadcastDrawable;
    public static Drawable chat_msgMediaBroadcastDrawable;
    public static Drawable chat_msgOutLocationDrawable;
    public static Drawable chat_msgBroadcastDrawable;
    public static Drawable chat_msgBroadcastMediaDrawable;
    public static Drawable chat_contextResult_shadowUnderSwitchDrawable;
    public static Drawable chat_shareDrawable;
    public static Drawable chat_shareIconDrawable;
    public static Drawable chat_botLinkDrawalbe;
    public static Drawable chat_botInlineDrawable;
    public static Drawable chat_systemDrawable;
    public static Drawable chat_msgInCallDrawable;
    public static Drawable chat_msgInCallSelectedDrawable;
    public static Drawable chat_msgOutCallDrawable;
    public static Drawable chat_msgOutCallSelectedDrawable;
    public static Drawable chat_msgCallUpRedDrawable;
    public static Drawable chat_msgCallUpGreenDrawable;
    public static Drawable chat_msgCallDownRedDrawable;
    public static Drawable chat_msgCallDownGreenDrawable;
    public static Drawable[] chat_attachButtonDrawables = new Drawable[8];
    public static Drawable[] chat_locationDrawable = new Drawable[2];
    public static Drawable[] chat_contactDrawable = new Drawable[2];
    public static Drawable[] chat_cornerOuter = new Drawable[4];
    public static Drawable[] chat_cornerInner = new Drawable[4];
    public static Drawable[][] chat_fileStatesDrawable = new Drawable[10][2];
    public static Drawable[][] chat_ivStatesDrawable = new Drawable[4][2];
    public static Drawable[][] chat_photoStatesDrawables = new Drawable[13][2];
    //plus
    public static int defColor;
    public static int dialogColor;
    public static int darkColor;
    public static int lightColor;
    // CHATS
    public static int chatsTabsBGColor;
    public static int chatsFavIndicatorColor;
    public static int FAV_INDICATOR_COLOR_DEF = 0xffffd54f;
    public static boolean chatsTabsToBottom;
    public static int chatsHeaderColor;
    public static int chatsHeaderGradient;
    public static int chatsHeaderGradientColor;
    public static int chatsHeaderTitleColor;
    public static int chatsChecksColor;
    //
    public static int chatsNameColor;
    public static int chatsGroupNameColor;
    public static int chatsDividerColor;

    public static int chatsUnknownNameColor;
    public static int chatsNameSize;
    public static int chatsAvatarRadius;
    public static int chatsAvatarSize;
    public static int chatsAvatarMarginLeft;
    public static int chatsRowColor;
    public static int chatsRowGradient;
    public static int chatsRowGradientColor;
    public static int chatsPinnedMsgBGColor;
    public static int chatsHeaderIconsColor;
    public static int chatsHeaderTabIconColor;
    public static int chatsHeaderTabUnselectedIconColor;
    public static int chatsTabsIndicatorColor;
    public static int chatsCountSize;
    public static boolean chatsHideStatusIndicator;
    public static boolean chatsHideHeaderShadow;
    public static int chatsTabCounterSilentBGColor;
    public static int chatsTabCounterBGColor;
    public static int chatsTabCounterColor;
    public static int chatsTabCounterSize;
    public static boolean chatsTabTitlesMode;
    public static int chatsTabsTextSize;
    public static int chatsFloatingBGColor;
    public static int chatsFloatingPencilColor;
    public static int chatsMessageColor;
    public static int chatsMemberColor;
    public static int chatsMediaColor;
    //
    //
    // CHATS Notifications
    public static final int UPDATE_DIALOGS_ALL_COLOR = 0;
    public static final int UPDATE_DIALOGS_HEADER_COLOR = 1;
    public static final int UPDATE_DIALOGS_ROW_COLOR = 2;
    // CHAT
    public static int chatStatusColor;
    public static int chatStatusSize;
    public static int chatOnlineColor;
    public static int chatTypingColor;
    public static int chatSelectedMsgBGColor;
    public static final int SELECTED_MDG_BACKGROUND_COLOR_DEF = 0x6626A69A;
    public static int chatQuickBarColor;
    public static int chatQuickBarNamesColor;
    public static int chatAvatarRadius;
    public static int chatAvatarSize;
    public static int chatAvatarMarginLeft;
    public static boolean chatAvatarAlignTop;
    public static boolean chatOwnAvatarAlignTop;
    public static boolean chatShowOwnAvatar;
    public static boolean chatShowOwnAvatarGroup;
    public static boolean chatShowContactAvatar;
    public static int chatRTextColor;
    public static int chatRLinkColor;
    public static int chatRBubbleColor;
    public static int chatLTextColor;
    public static int chatLLinkColor;
    public static int chatLBubbleColor;
    public static int chatDateColor;
    public static int chatDateSize;
    public static int chatDateBubbleColor;
    public static int chatChecksColor;
    public static int chatRTimeColor;
    public static int chatLTimeColor;
    public static int chatContactNameColor;
    public static int chatForwardRColor;
    public static int chatForwardLColor;
    public static int chatMemberColor;
    public static boolean chatMemberColorCheck;
    public static boolean chatHideStatusIndicator;
    public static boolean chatShowUsernameCheck;
    public static boolean chatSolidBGColorCheck;
    public static int chatHeaderColor;
    public static int chatHeaderIconsColor;
    public static String chatBubbleStyle;
    public static int chatBubbleStyleVal;
    public static String chatCheckStyle;
    public static int chatCheckStyleVal;
    public static int chatTimeSize;
    public static int chatEditTextIconsColor;
    public static int chatAttachTextColor;
    public static int chatAttachBGColor;
    // CONTACTS
    public static int contactsHeaderColor;
    public static int contactsHeaderTitleColor;
    public static int contactsHeaderIconsColor;
    public static int contactsIconsColor;
    public static int contactsRowColor;
    public static int contactsNameColor;
    public static int contactsStatusColor;
    public static int contactsOnlineColor;
    public static int contactsAvatarRadius;
    public static int contactsNameSize;
    public static int contactsStatusSize;

    // DRAWER
    public static int drawerHeaderColor;
    public static int drawerOptionColor;
    public static int drawerNameColor;
    public static int drawerPhoneColor;
    public static int drawerAvatarSize;
    public static boolean drawerCenterAvatarCheck;
    public static boolean drawerHeaderBGCheck;
    public static boolean drawerHideBGShadowCheck;
    public static int drawerIconColor;
    public static int drawerOptionSize;
    // PROFILE
    public static int profileActionbarColor;
    public static int profileActionbarGradientList;
    public static int profileActionbarGradientColor;
    public static int profileActionbarIconsColor;
    public static int profileActionbarAvatarRadius;
    public static int profileActionbarNameSize;
    public static int profileActionbarNameColor;
    public static int profileActionbarStatusColor;
    public static int profileActionbarStatusSize;
    public static int profileRowColor;
    public static int profileRowGradientList;
    public static int profileRowGradientColor;
    public static int profileRowAvatarRadius;
    public static int profileRowTitleColor;
    public static int profileRowStatusColor;
    public static int profileRowOnlineColor;
    public static int profileRowIconsColor;
    public static int profileRowCreatorStarColor;
    public static int profileRowAdminStarColor;
    //public static int profileSummaryColor;
    // PREFERENCES
    public static int prefActionbarColor;
    public static int prefActionbarTitleColor;
    public static int prefActionbarStatusColor;
    public static int prefActionbarIconsColor;
    public static int prefAvatarColor;
    public static int prefAvatarRadius;
    public static int prefAvatarSize;
    public static int prefBGColor;
    public static int prefShadowColor;
    public static int prefSectionColor;
    public static int prefTitleColor;
    public static int prefSummaryColor;
    public static int prefDividerColor;
    // PLUS
    // Chat
    public static boolean plusShowDSBtnChannels;
    public static boolean plusShowDSBtnUsers;
    public static boolean plusShowDSBtnBots;
    public static boolean plusShowDSBtnGroups;
    public static boolean plusShowDSBtnSGroups;
    public static boolean plusShowEditedMark;
    public static boolean plusShowPhotoQualityBar;
    public static int plusPhotoQuality;
    //public static int plusPhotoMaxSize;
    public static boolean plusPhotoViewerHideStatusBar;

    public static boolean plusShowTypingToast;
    public static boolean plusShowOnlineToast;
    public static boolean plusShowOnlyIfContactFav;
    public static boolean plusShowOfflineToast;
    public static int plusToastNotificationSize;
    public static int plusToastNotificationPadding;
    public static boolean plusToastNotificationToBottom;
    public static int plusToastNotificationPosition;
    public static boolean plusEnableDirectReply;
    public static boolean plusShowQuickBar;
    public static boolean plusVerticalQuickBar;
    public static boolean plusAlwaysBackToMain;
    public static boolean plusDoNotCloseQuickBar;
    public static boolean plusHideQuickBarOnScroll;
    public static boolean plusCenterQuickBarBtn;
    public static int plusQuickBarDialogType;
    public static boolean plusQuickBarShowMembers;
    public static boolean plusSaveToCloudQuote;
    public static boolean plusSwipeToReply;
    public static boolean plusHideNotificationsIfPlaying;
    public static boolean plusHideInstantCamera;
    public static int plusSortAll;
    public static int plusSortUsers;
    public static int plusSortGroups;
    public static int plusSortSuperGroups;
    public static int plusSortChannels;
    public static int plusSortBots;
    public static int plusSortFavs;
    public static int plusSortAdmin;
    public static int plusSortUnread;
    public static boolean plusShowUnmutedFirst;
    public static boolean plusDoNotHideStickersTab;
    public static int plusTabsHeight;
    public static int plusDefaultTab;
    public static int plusSelectedTab;
    public static int plusDialogType;
    public static boolean plusHideAllTab;
    public static boolean plusHideUsersTab;
    public static boolean plusHideGroupsTab;
    public static boolean plusHideSuperGroupsTab;
    public static boolean plusHideChannelsTab;
    public static boolean plusHideBotsTab;
    public static boolean plusHideFavsTab;
    public static boolean plusHideAdminTab;
    public static boolean plusHideUnreadTab;
    public static boolean plusShowAllInAdminTab;
    public static boolean plusTabTitlesMode;
    public static boolean plusTabsShouldExpand;
    public static boolean plusDisableTabsAnimation;
    public static boolean plusDisableTabsScrolling;
    public static int plusTabsTextSize;
    public static boolean plusDrawSingleBigEmoji;
    public static boolean plusEnableMarkdown;
    public static boolean plusLimitTabsCounters;
    // PLUS Dialogs
    public static boolean plusHideTabs;
    public static boolean plusTabsToBottom;
    public static boolean plusHideTabsSelector;
    public static boolean plusHideTabsCounters;
    public static boolean plusTabsCountersCountChats;
    public static boolean plusTabsCountersCountNotMuted;
    public static boolean plusInfiniteTabsSwipe;
    public static boolean plusDoNotChangeHeaderTitle;
    // Plus Profile
    public static boolean plusProfileEnableGoToMsg;
    public static int plusChatsToLoad;
    // Plus Drawer
    public static boolean plusHideMobile;
    public static boolean plusShowUsername;
    public static boolean plusMoveVersionToSettings;
    public static boolean plusShowUserBio;
    //
    public static final String pkey_themeColor = "themeColor";
    public static final String pkey_dialogColor = "dialogColor";
    //chat
    //HEADER
    public static final String pkey_chatHeaderColor = "chatHeaderColor";
    public static final String pkey_chatHeaderGradient = "chatHeaderGradient";
    public static final String pkey_chatHeaderGradientColor = "chatHeaderGradientColor";
    public static final String pkey_chatHeaderIconsColor = "chatHeaderIconsColor";
    public static final String pkey_chatHeaderAvatarRadius = "chatHeaderAvatarRadius";
    public static final String pkey_chatNameSize = "chatNameSize";
    public static final String pkey_chatNameColor = "chatNameColor";
    public static final String pkey_chatStatusSize = "chatStatusSize";
    public static final String pkey_chatStatusColor = "chatStatusColor";
    public static final String pkey_chatOnlineColor = "chatOnlineColor";
    public static final String pkey_chatTypingColor = "chatTypingColor";
    //LIST
    public static final String pkey_chatSolidBGColor = "chatSolidBGColor";
    public static final String pkey_chatGradientBGColor = "chatGradientBGColor";
    public static final String pkey_chatGradientBG = "chatGradientBG";
    public static final String pkey_chatMemberColor = "chatMemberColor";
    public static final String pkey_chatContactNameColor = "chatContactNameColor";
    public static final String pkey_chatRTextColor = "chatRTextColor";
    public static final String pkey_chatRLinkColor = "chatRLinkColor";
    public static final String pkey_chatLTextColor = "chatLTextColor";
    public static final String pkey_chatLLinkColor = "chatLLinkColor";
    public static final String pkey_chatSelectedMsgBGColor = "chatSelectedMsgBGColor";
    public static final String pkey_chatRTimeColor = "chatRTimeColor";
    public static final String pkey_chatLTimeColor = "chatLTimeColor";
    public static final String pkey_chatCommandColor = "chatCommandColor";
    public static final String pkey_chatDateColor = "chatDateColor";
    public static final String pkey_chatChecksColor = "chatChecksColor";
    public static final String pkey_chatRBubbleColor = "chatRBubbleColor";
    public static final String pkey_chatLBubbleColor = "chatLBubbleColor";
    public static final String pkey_chatForwardRColor = "chatForwardRColor";
    public static final String pkey_chatForwardLColor = "chatForwardLColor";
    public static final String pkey_chatTextSize = "chatTextSize";
    public static final String pkey_chatTimeSize = "chatTimeSize";
    public static final String pkey_chatDateSize = "chatDateSize";
    public static final String pkey_chatDateBubbleColor = "chatDateBubbleColor";
    public static final String pkey_chatSendIconColor = "chatSendIconColor";
    public static final String pkey_chatEditTextColor = "chatEditTextColor";
    public static final String pkey_chatEditTextSize = "chatEditTextSize";
    public static final String pkey_chatEditTextBGColor = "chatEditTextBGColor";
    public static final String pkey_chatEditTextBGGradientColor = "chatEditTextBGGradientColor";
    public static final String pkey_chatEditTextBGGradient = "chatEditTextBGGradient";
    public static final String pkey_chatEditTextIconsColor = "chatEditTextIconsColor";
    public static final String pkey_chatAttachBGColor = "chatAttachBGColor";
    public static final String pkey_chatAttachBGGradient = "chatAttachBGGradient";
    public static final String pkey_chatAttachBGGradientColor = "chatAttachBGGradientColor";
    public static final String pkey_chatAttachTextColor = "chatAttachTextColor";
    public static final String pkey_chatEmojiViewBGColor = "chatEmojiViewBGColor";
    public static final String pkey_chatEmojiViewBGGradient = "chatEmojiViewBGGradient";
    public static final String pkey_chatEmojiViewBGGradientColor = "chatEmojiViewBGGradientColor";
    public static final String pkey_chatEmojiViewTabIconColor = "chatEmojiViewTabIconColor";
    public static final String pkey_chatEmojiViewTabColor = "chatEmojiViewTabColor";
    public static final String pkey_chatQuickBarColor = "chatQuickBarColor";
    public static final String pkey_chatQuickBarNamesColor = "chatQuickBarNamesColor";
    public static final String pkey_chatAvatarRadius = "chatAvatarRadius";
    public static final String pkey_chatAvatarSize = "chatAvatarSize";
    public static final String pkey_chatAvatarMarginLeft = "chatAvatarMarginLeft";
    //CHATS
    //
    public static final String pkey_chatsHeaderTitleColor = "chatsHeaderTitleColor";
    public static final String pkey_chatsChecksColor = "chatsChecksColor";
    public static final String pkey_chatsGroupNameColor = "chatsGroupNameColor";
    public static final String pkey_chatsNameColor = "chatsNameColor";
    public static final String pkey_chatsUnknownNameColor = "chatsUnknownNameColor";
    public static final String pkey_chatsFloatingBGColor = "chatsFloatingBGColor";
    public static final String pkey_chatsFloatingPencilColor = "chatsFloatingPencilColor";
    public static final String pkey_chatsMessageColor = "chatsMessageColor";
    public static final String pkey_chatsMemberColor = "chatsMemberColor";
    public static final String pkey_chatsMediaColor = "chatsMediaColor";
    //
    //PREFS
    public static final String pkey_prefActionbarColor = "prefActionbarColor";
    public static final String pkey_prefActionbarTitleColor = "prefActionbarTitleColor";
    public static final String pkey_prefActionbarStatusColor = "prefActionbarStatusColor";
    public static final String pkey_prefAvatarColor = "prefAvatarColor";
    public static final String pkey_prefBGColor = "prefBGColor";
    //prefBGColor
    //DRAWER
    public static final String pkey_drawerOptionColor = "drawerOptionColor";
    public static final String pkey_drawerIconColor = "drawerIconColor";
    public static final String pkey_drawerPhoneColor = "drawerPhoneColor";
    public static final String pkey_drawerNameColor = "drawerNameColor";
    //drawerPhoneColor
    //PROFILE
    public static final String pkey_profileActionbarIconsColor = "profileActionbarIconsColor";
    //

    private static HashMap<String, Integer> defaultPlusColors = new HashMap<>();
    private static HashMap<String, Integer> currentPlusColors;
    private static Map<String, ?> currentPlusTheme;

    private static HashMap<String, String> telegramToPlus = new HashMap<>();

    static {
        //defColor = 0xff009688;
        //lightColor = AndroidUtilities.getIntDarkerColor("themeColor", -0x40); //0xff40d6c8 -12527928
        //darkColor = AndroidUtilities.getIntDarkerColor("themeColor", 0x15); //0xff008173 -16744077
        // AndroidUtilities.getDefBubbleColor() 0xffb2dfdb -5054501
        //serviceMessageColor 1711934734 0x660a0d0e
        //serviceSelectedMessageColor -2012607218 880A0D0E
        //chatDateBubbleColor
        //Log.e("Theme", "defColor: " + defColor + " lightColor: " + lightColor + " darkColor: " + darkColor + " AndroidUtilities.getDefBubbleColor() " + AndroidUtilities.getDefBubbleColor());
        //GENERAL
        defaultPlusColors.put(pkey_themeColor, 0xff009688);
        defaultPlusColors.put(pkey_dialogColor, 0xff009688);
        //CHATS
        defaultPlusColors.put(pkey_chatsHeaderTitleColor, 0xffffffff);
        defaultPlusColors.put(pkey_chatsChecksColor, 0xff009688);
        defaultPlusColors.put(pkey_chatsNameColor, 0xff212121);
        defaultPlusColors.put(pkey_chatsGroupNameColor , 0xff212121);
        defaultPlusColors.put(pkey_chatsUnknownNameColor , 0xff212121);
        defaultPlusColors.put(pkey_chatsFloatingBGColor, 0xff009688);
        defaultPlusColors.put(pkey_chatsFloatingPencilColor, 0xffffffff);
        defaultPlusColors.put(pkey_chatsMessageColor, 0xff808080);
        defaultPlusColors.put(pkey_chatsMemberColor, 0xff008173);
        defaultPlusColors.put(pkey_chatsMediaColor, 0xff008173);
        //CHAT HEADER
        defaultPlusColors.put(pkey_chatHeaderColor, 0xff009688);                //2.1.1
        defaultPlusColors.put(pkey_chatHeaderGradient, 0);                      //2.1.2
        defaultPlusColors.put(pkey_chatHeaderGradientColor, 0xff009688);        //2.1.3
        defaultPlusColors.put(pkey_chatHeaderIconsColor, 0xffffffff);           //2.1.4
        defaultPlusColors.put(pkey_chatHeaderAvatarRadius, 32);                 //2.1.5
        defaultPlusColors.put(pkey_chatNameSize, 18);                           //2.1.6
        defaultPlusColors.put(pkey_chatNameColor, 0xffffffff);                  //2.1.7
        defaultPlusColors.put(pkey_chatStatusSize, 14);                         //2.1.8
        defaultPlusColors.put(pkey_chatStatusColor, 0xff40d6c8);          //2.1.9
        defaultPlusColors.put(pkey_chatOnlineColor, 0xff40d6c8);          //2.1.10
        defaultPlusColors.put(pkey_chatTypingColor, 0xff40d6c8);          //2.1.11
        //CHAT LIST
        //chatSolidBGColorCheck                                                 //2.2.1
        defaultPlusColors.put(pkey_chatSolidBGColor, 0xffffffff);               //2.2.2
        defaultPlusColors.put(pkey_chatGradientBG, 0);                          //2.2.3
        defaultPlusColors.put(pkey_chatGradientBGColor, 0xffffffff);            //2.2.4
        //chatShowContactAvatar                                                 //2.2.5
        //chatAvatarAlignTop                                                    //2.2.6
        //chatShowOwnAvatar                                                     //2.2.7
        //chatShowOwnAvatarGroup                                                //2.2.8
        //chatOwnAvatarAlignTop                                                 //2.2.9
        defaultPlusColors.put(pkey_chatAvatarRadius, 32);                       //2.2.10
        defaultPlusColors.put(pkey_chatAvatarSize, 42);                         //2.2.11
        defaultPlusColors.put(pkey_chatAvatarMarginLeft, 6);                    //2.2.12
        //chatHideStatusIndicator                                               //2.2.13
        defaultPlusColors.put(pkey_chatTextSize, 16);                           //2.2.14
        defaultPlusColors.put(pkey_chatRTextColor, 0xff000000);                 //2.2.15
        defaultPlusColors.put(pkey_chatRLinkColor, 0xff009688);                 //2.2.16
        defaultPlusColors.put(pkey_chatLTextColor, 0xff000000);                 //2.2.17
        defaultPlusColors.put(pkey_chatLLinkColor, 0xff009688);                 //2.2.18
        defaultPlusColors.put(pkey_chatSelectedMsgBGColor, 0x6626a69a);         //2.2.19
        //chatCommandColorCheck                                                 //2.2.20
        defaultPlusColors.put(pkey_chatCommandColor, 0xff009688);               //2.2.21
        defaultPlusColors.put(pkey_chatTimeSize, 12);                           //2.2.22
        defaultPlusColors.put(pkey_chatRTimeColor, 0xff008173);                   //2.2.23
        defaultPlusColors.put(pkey_chatLTimeColor, 0xffa1aab3);                 //2.2.24
        defaultPlusColors.put(pkey_chatChecksColor, 0xff009688);                //2.2.25
        defaultPlusColors.put(pkey_chatDateSize, 16);                           //2.2.26
        defaultPlusColors.put(pkey_chatDateColor, 0xffffffff);                  //2.2.27
        //chatBubbleStyle                                                       //2.2.28
        //chatCheckStyle                                                        //2.2.29
        defaultPlusColors.put(pkey_chatRBubbleColor, 0xffb2dfdb);               //2.2.30
        defaultPlusColors.put(pkey_chatLBubbleColor, 0xffffffff);               //2.2.31
        defaultPlusColors.put(pkey_chatDateBubbleColor, 0x660a0d0e); //2.2.32
        //chatMemeberColorCheck                                                 //2.2.33
        defaultPlusColors.put(pkey_chatMemberColor, 0xff008173);           //2.2.34
        defaultPlusColors.put(pkey_chatContactNameColor, 0xff009688);           //2.2.35
        defaultPlusColors.put(pkey_chatForwardRColor, 0xff008173);         //2.2.36
        defaultPlusColors.put(pkey_chatForwardLColor, 0xff008173);         //2.2.37
        //chatShowUsernameCheck                                                 //2.2.38
        //CHAT BOTTOM
        defaultPlusColors.put(pkey_chatSendIconColor, 0xff009688);              //2.2.39
        defaultPlusColors.put(pkey_chatEditTextSize, 18);                       //2.2.40
        defaultPlusColors.put(pkey_chatEditTextColor, 0xff000000);              //2.2.41
        defaultPlusColors.put(pkey_chatEditTextBGColor, 0xffffffff);            //2.2.42
        defaultPlusColors.put(pkey_chatEditTextBGGradient, 0);                  //2.2.43
        defaultPlusColors.put(pkey_chatEditTextBGGradientColor, 0xffffffff);    //2.2.44
        defaultPlusColors.put(pkey_chatEditTextIconsColor, 0xffadadad);         //2.2.45
        defaultPlusColors.put(pkey_chatAttachBGColor, 0xffffffff);              //2.2.46
        defaultPlusColors.put(pkey_chatAttachBGGradient, 0);                    //2.2.47
        defaultPlusColors.put(pkey_chatAttachBGGradientColor, 0xffffffff);      //2.2.48
        defaultPlusColors.put(pkey_chatAttachTextColor, 0xff009688);            //2.2.49
        defaultPlusColors.put(pkey_chatEmojiViewBGColor, 0xfff5f6f7);           //2.2.50
        defaultPlusColors.put(pkey_chatEmojiViewBGGradient, 0);                 //2.2.51
        defaultPlusColors.put(pkey_chatEmojiViewBGGradientColor, 0xfff5f6f7);   //2.2.52
        defaultPlusColors.put(pkey_chatEmojiViewTabIconColor, 0xffa8a8a8);      //2.2.53
        defaultPlusColors.put(pkey_chatEmojiViewTabColor, 0xff008173);            //2.2.54
        defaultPlusColors.put(pkey_chatQuickBarColor, 0xffffffff);              //2.2.55
        defaultPlusColors.put(pkey_chatQuickBarNamesColor, 0xff212121);         //2.2.56
        //DRAWER
        defaultPlusColors.put(pkey_drawerOptionColor, 0xff444444);
        defaultPlusColors.put(pkey_drawerIconColor, 0xff737373);
        defaultPlusColors.put(pkey_drawerPhoneColor, 0xff40d6c8);
        defaultPlusColors.put(pkey_drawerNameColor, 0xffffffff);
        //PREFS
        defaultPlusColors.put(pkey_prefActionbarColor, 0xff009688);
        defaultPlusColors.put(pkey_prefActionbarTitleColor, 0xffffffff);
        defaultPlusColors.put(pkey_prefActionbarStatusColor, 0xff40d6c8);
        defaultPlusColors.put(pkey_prefAvatarColor, 0xff008173);
        defaultPlusColors.put(pkey_prefBGColor, 0xffffffff);
        //PROFILE
        defaultPlusColors.put(pkey_profileActionbarIconsColor, 0xffffffff);

        currentPlusColors = new HashMap<>();
    }
    //
    public static final String key_dialogBackground = "dialogBackground";
    public static final String key_dialogBackgroundGray = "dialogBackgroundGray";
    public static final String key_dialogTextBlack = "dialogTextBlack";
    public static final String key_dialogTextLink = "dialogTextLink";
    public static final String key_dialogLinkSelection = "dialogLinkSelection";
    public static final String key_dialogTextRed = "dialogTextRed";
    public static final String key_dialogTextBlue = "dialogTextBlue";
    public static final String key_dialogTextBlue2 = "dialogTextBlue2";
    public static final String key_dialogTextBlue3 = "dialogTextBlue3";
    public static final String key_dialogTextBlue4 = "dialogTextBlue4";
    public static final String key_dialogTextGray = "dialogTextGray";
    public static final String key_dialogTextGray2 = "dialogTextGray2";
    public static final String key_dialogTextGray3 = "dialogTextGray3";
    public static final String key_dialogTextGray4 = "dialogTextGray4";
    public static final String key_dialogTextHint = "dialogTextHint";
    public static final String key_dialogInputField = "dialogInputField";
    public static final String key_dialogInputFieldActivated = "dialogInputFieldActivated";
    public static final String key_dialogCheckboxSquareBackground = "dialogCheckboxSquareBackground";
    public static final String key_dialogCheckboxSquareCheck = "dialogCheckboxSquareCheck";
    public static final String key_dialogCheckboxSquareUnchecked = "dialogCheckboxSquareUnchecked";
    public static final String key_dialogCheckboxSquareDisabled = "dialogCheckboxSquareDisabled";
    public static final String key_dialogScrollGlow = "dialogScrollGlow";
    public static final String key_dialogRoundCheckBox = "dialogRoundCheckBox";
    public static final String key_dialogRoundCheckBoxCheck = "dialogRoundCheckBoxCheck";
    public static final String key_dialogBadgeBackground = "dialogBadgeBackground";
    public static final String key_dialogBadgeText = "dialogBadgeText";
    public static final String key_dialogRadioBackground = "dialogRadioBackground";
    public static final String key_dialogRadioBackgroundChecked = "dialogRadioBackgroundChecked";
    public static final String key_dialogProgressCircle = "dialogProgressCircle";
    public static final String key_dialogLineProgress = "dialogLineProgress";
    public static final String key_dialogLineProgressBackground = "dialogLineProgressBackground";
    public static final String key_dialogButton = "dialogButton";
    public static final String key_dialogButtonSelector = "dialogButtonSelector";
    public static final String key_dialogIcon = "dialogIcon";
    public static final String key_dialogGrayLine = "dialogGrayLine";

    public static final String key_windowBackgroundWhite = "windowBackgroundWhite";
    public static final String key_progressCircle = "progressCircle";
    public static final String key_listSelector = "listSelectorSDK21";
    public static final String key_windowBackgroundWhiteInputField = "windowBackgroundWhiteInputField";
    public static final String key_windowBackgroundWhiteInputFieldActivated = "windowBackgroundWhiteInputFieldActivated";
    public static final String key_windowBackgroundWhiteGrayIcon = "windowBackgroundWhiteGrayIcon";
    public static final String key_windowBackgroundWhiteBlueText = "windowBackgroundWhiteBlueText";
    public static final String key_windowBackgroundWhiteBlueText2 = "windowBackgroundWhiteBlueText2";
    public static final String key_windowBackgroundWhiteBlueText3 = "windowBackgroundWhiteBlueText3";
    public static final String key_windowBackgroundWhiteBlueText4 = "windowBackgroundWhiteBlueText4";
    public static final String key_windowBackgroundWhiteBlueText5 = "windowBackgroundWhiteBlueText5";
    public static final String key_windowBackgroundWhiteBlueText6 = "windowBackgroundWhiteBlueText6";
    public static final String key_windowBackgroundWhiteBlueText7 = "windowBackgroundWhiteBlueText7";
    public static final String key_windowBackgroundWhiteGreenText = "windowBackgroundWhiteGreenText";
    public static final String key_windowBackgroundWhiteGreenText2 = "windowBackgroundWhiteGreenText2";
    public static final String key_windowBackgroundWhiteRedText = "windowBackgroundWhiteRedText";
    public static final String key_windowBackgroundWhiteRedText2 = "windowBackgroundWhiteRedText2";
    public static final String key_windowBackgroundWhiteRedText3 = "windowBackgroundWhiteRedText3";
    public static final String key_windowBackgroundWhiteRedText4 = "windowBackgroundWhiteRedText4";
    public static final String key_windowBackgroundWhiteRedText5 = "windowBackgroundWhiteRedText5";
    public static final String key_windowBackgroundWhiteRedText6 = "windowBackgroundWhiteRedText6";
    public static final String key_windowBackgroundWhiteGrayText = "windowBackgroundWhiteGrayText";
    public static final String key_windowBackgroundWhiteGrayText2 = "windowBackgroundWhiteGrayText2";
    public static final String key_windowBackgroundWhiteGrayText3 = "windowBackgroundWhiteGrayText3";
    public static final String key_windowBackgroundWhiteGrayText4 = "windowBackgroundWhiteGrayText4";
    public static final String key_windowBackgroundWhiteGrayText5 = "windowBackgroundWhiteGrayText5";
    public static final String key_windowBackgroundWhiteGrayText6 = "windowBackgroundWhiteGrayText6";
    public static final String key_windowBackgroundWhiteGrayText7 = "windowBackgroundWhiteGrayText7";
    public static final String key_windowBackgroundWhiteGrayText8 = "windowBackgroundWhiteGrayText8";
    public static final String key_windowBackgroundWhiteGrayLine = "windowBackgroundWhiteGrayLine";
    public static final String key_windowBackgroundWhiteBlackText = "windowBackgroundWhiteBlackText";
    public static final String key_windowBackgroundWhiteHintText = "windowBackgroundWhiteHintText";
    public static final String key_windowBackgroundWhiteValueText = "windowBackgroundWhiteValueText";
    public static final String key_windowBackgroundWhiteLinkText = "windowBackgroundWhiteLinkText";
    public static final String key_windowBackgroundWhiteLinkSelection = "windowBackgroundWhiteLinkSelection";
    public static final String key_windowBackgroundWhiteBlueHeader = "windowBackgroundWhiteBlueHeader";
    public static final String key_switchThumb = "switchThumb";
    public static final String key_switchTrack = "switchTrack";
    public static final String key_switchThumbChecked = "switchThumbChecked";
    public static final String key_switchTrackChecked = "switchTrackChecked";
    public static final String key_checkboxSquareBackground = "checkboxSquareBackground";
    public static final String key_checkboxSquareCheck = "checkboxSquareCheck";
    public static final String key_checkboxSquareUnchecked = "checkboxSquareUnchecked";
    public static final String key_checkboxSquareDisabled = "checkboxSquareDisabled";
    public static final String key_windowBackgroundGray = "windowBackgroundGray";
    public static final String key_windowBackgroundGrayShadow = "windowBackgroundGrayShadow";
    public static final String key_emptyListPlaceholder = "emptyListPlaceholder";
    public static final String key_divider = "divider";
    public static final String key_graySection = "graySection";
    public static final String key_radioBackground = "radioBackground";
    public static final String key_radioBackgroundChecked = "radioBackgroundChecked";
    public static final String key_checkbox = "checkbox";
    public static final String key_checkboxCheck = "checkboxCheck";
    public static final String key_fastScrollActive = "fastScrollActive";
    public static final String key_fastScrollInactive = "fastScrollInactive";
    public static final String key_fastScrollText = "fastScrollText";

    public static final String key_inappPlayerPerformer = "inappPlayerPerformer";
    public static final String key_inappPlayerTitle = "inappPlayerTitle";
    public static final String key_inappPlayerBackground = "inappPlayerBackground";
    public static final String key_inappPlayerPlayPause = "inappPlayerPlayPause";
    public static final String key_inappPlayerClose = "inappPlayerClose";

    public static final String key_returnToCallBackground = "returnToCallBackground";
    public static final String key_returnToCallText = "returnToCallText";

    public static final String key_contextProgressInner1 = "contextProgressInner1";
    public static final String key_contextProgressOuter1 = "contextProgressOuter1";
    public static final String key_contextProgressInner2 = "contextProgressInner2";
    public static final String key_contextProgressOuter2 = "contextProgressOuter2";
    public static final String key_contextProgressInner3 = "contextProgressInner3";
    public static final String key_contextProgressOuter3 = "contextProgressOuter3";

    public static final String key_avatar_text = "avatar_text";
    public static final String key_avatar_backgroundRed = "avatar_backgroundRed";
    public static final String key_avatar_backgroundOrange = "avatar_backgroundOrange";
    public static final String key_avatar_backgroundViolet = "avatar_backgroundViolet";
    public static final String key_avatar_backgroundGreen = "avatar_backgroundGreen";
    public static final String key_avatar_backgroundCyan = "avatar_backgroundCyan";
    public static final String key_avatar_backgroundBlue = "avatar_backgroundBlue";
    public static final String key_avatar_backgroundPink = "avatar_backgroundPink";
    public static final String key_avatar_backgroundGroupCreateSpanBlue = "avatar_backgroundGroupCreateSpanBlue";
    public static final String key_avatar_backgroundInProfileRed = "avatar_backgroundInProfileRed";
    public static final String key_avatar_backgroundInProfileOrange = "avatar_backgroundInProfileOrange";
    public static final String key_avatar_backgroundInProfileViolet = "avatar_backgroundInProfileViolet";
    public static final String key_avatar_backgroundInProfileGreen = "avatar_backgroundInProfileGreen";
    public static final String key_avatar_backgroundInProfileCyan = "avatar_backgroundInProfileCyan";
    public static final String key_avatar_backgroundInProfileBlue = "avatar_backgroundInProfileBlue";
    public static final String key_avatar_backgroundInProfilePink = "avatar_backgroundInProfilePink";
    public static final String key_avatar_backgroundActionBarRed = "avatar_backgroundActionBarRed";
    public static final String key_avatar_backgroundActionBarOrange = "avatar_backgroundActionBarOrange";
    public static final String key_avatar_backgroundActionBarViolet = "avatar_backgroundActionBarViolet";
    public static final String key_avatar_backgroundActionBarGreen = "avatar_backgroundActionBarGreen";
    public static final String key_avatar_backgroundActionBarCyan = "avatar_backgroundActionBarCyan";
    public static final String key_avatar_backgroundActionBarBlue = "avatar_backgroundActionBarBlue";
    public static final String key_avatar_backgroundActionBarPink = "avatar_backgroundActionBarPink";
    public static final String key_avatar_subtitleInProfileRed = "avatar_subtitleInProfileRed";
    public static final String key_avatar_subtitleInProfileOrange = "avatar_subtitleInProfileOrange";
    public static final String key_avatar_subtitleInProfileViolet = "avatar_subtitleInProfileViolet";
    public static final String key_avatar_subtitleInProfileGreen = "avatar_subtitleInProfileGreen";
    public static final String key_avatar_subtitleInProfileCyan = "avatar_subtitleInProfileCyan";
    public static final String key_avatar_subtitleInProfileBlue = "avatar_subtitleInProfileBlue";
    public static final String key_avatar_subtitleInProfilePink = "avatar_subtitleInProfilePink";
    public static final String key_avatar_nameInMessageRed = "avatar_nameInMessageRed";
    public static final String key_avatar_nameInMessageOrange = "avatar_nameInMessageOrange";
    public static final String key_avatar_nameInMessageViolet = "avatar_nameInMessageViolet";
    public static final String key_avatar_nameInMessageGreen = "avatar_nameInMessageGreen";
    public static final String key_avatar_nameInMessageCyan = "avatar_nameInMessageCyan";
    public static final String key_avatar_nameInMessageBlue = "avatar_nameInMessageBlue";
    public static final String key_avatar_nameInMessagePink = "avatar_nameInMessagePink";
    public static final String key_avatar_actionBarSelectorRed = "avatar_actionBarSelectorRed";
    public static final String key_avatar_actionBarSelectorOrange = "avatar_actionBarSelectorOrange";
    public static final String key_avatar_actionBarSelectorViolet = "avatar_actionBarSelectorViolet";
    public static final String key_avatar_actionBarSelectorGreen = "avatar_actionBarSelectorGreen";
    public static final String key_avatar_actionBarSelectorCyan = "avatar_actionBarSelectorCyan";
    public static final String key_avatar_actionBarSelectorBlue = "avatar_actionBarSelectorBlue";
    public static final String key_avatar_actionBarSelectorPink = "avatar_actionBarSelectorPink";
    public static final String key_avatar_actionBarIconRed = "avatar_actionBarIconRed";
    public static final String key_avatar_actionBarIconOrange = "avatar_actionBarIconOrange";
    public static final String key_avatar_actionBarIconViolet = "avatar_actionBarIconViolet";
    public static final String key_avatar_actionBarIconGreen = "avatar_actionBarIconGreen";
    public static final String key_avatar_actionBarIconCyan = "avatar_actionBarIconCyan";
    public static final String key_avatar_actionBarIconBlue = "avatar_actionBarIconBlue";
    public static final String key_avatar_actionBarIconPink = "avatar_actionBarIconPink";

    public static String[] keys_avatar_background = {key_avatar_backgroundRed, key_avatar_backgroundOrange, key_avatar_backgroundViolet, key_avatar_backgroundGreen, key_avatar_backgroundCyan, key_avatar_backgroundBlue, key_avatar_backgroundPink};
    public static String[] keys_avatar_backgroundInProfile = {key_avatar_backgroundInProfileRed, key_avatar_backgroundInProfileOrange, key_avatar_backgroundInProfileViolet, key_avatar_backgroundInProfileGreen, key_avatar_backgroundInProfileCyan, key_avatar_backgroundInProfileBlue, key_avatar_backgroundInProfilePink};
    public static String[] keys_avatar_backgroundActionBar = {key_avatar_backgroundActionBarRed, key_avatar_backgroundActionBarOrange, key_avatar_backgroundActionBarViolet, key_avatar_backgroundActionBarGreen, key_avatar_backgroundActionBarCyan, key_avatar_backgroundActionBarBlue, key_avatar_backgroundActionBarPink};
    public static String[] keys_avatar_subtitleInProfile = {key_avatar_subtitleInProfileRed, key_avatar_subtitleInProfileOrange, key_avatar_subtitleInProfileViolet, key_avatar_subtitleInProfileGreen, key_avatar_subtitleInProfileCyan, key_avatar_subtitleInProfileBlue, key_avatar_subtitleInProfilePink};
    public static String[] keys_avatar_nameInMessage = {key_avatar_nameInMessageRed, key_avatar_nameInMessageOrange, key_avatar_nameInMessageViolet, key_avatar_nameInMessageGreen, key_avatar_nameInMessageCyan, key_avatar_nameInMessageBlue, key_avatar_nameInMessagePink};
    public static String[] keys_avatar_actionBarSelector = {key_avatar_actionBarSelectorRed, key_avatar_actionBarSelectorOrange, key_avatar_actionBarSelectorViolet, key_avatar_actionBarSelectorGreen, key_avatar_actionBarSelectorCyan, key_avatar_actionBarSelectorBlue, key_avatar_actionBarSelectorPink};
    public static String[] keys_avatar_actionBarIcon = {key_avatar_actionBarIconRed, key_avatar_actionBarIconOrange, key_avatar_actionBarIconViolet, key_avatar_actionBarIconGreen, key_avatar_actionBarIconCyan, key_avatar_actionBarIconBlue, key_avatar_actionBarIconPink};

    public static final String key_actionBarDefault = "actionBarDefault";
    public static final String key_actionBarDefaultSelector = "actionBarDefaultSelector";
    public static final String key_actionBarWhiteSelector = "actionBarWhiteSelector";
    public static final String key_actionBarDefaultIcon = "actionBarDefaultIcon";
    public static final String key_actionBarActionModeDefault = "actionBarActionModeDefault";
    public static final String key_actionBarActionModeDefaultTop = "actionBarActionModeDefaultTop";
    public static final String key_actionBarActionModeDefaultIcon = "actionBarActionModeDefaultIcon";
    public static final String key_actionBarActionModeDefaultSelector = "actionBarActionModeDefaultSelector";
    public static final String key_actionBarDefaultTitle = "actionBarDefaultTitle";
    public static final String key_actionBarDefaultSubtitle = "actionBarDefaultSubtitle";
    public static final String key_actionBarDefaultSearch = "actionBarDefaultSearch";
    public static final String key_actionBarDefaultSearchPlaceholder = "actionBarDefaultSearchPlaceholder";
    public static final String key_actionBarDefaultSubmenuItem = "actionBarDefaultSubmenuItem";
    public static final String key_actionBarDefaultSubmenuBackground = "actionBarDefaultSubmenuBackground";
    public static final String key_chats_unreadCounter = "chats_unreadCounter";
    public static final String key_chats_unreadCounterMuted = "chats_unreadCounterMuted";
    public static final String key_chats_unreadCounterText = "chats_unreadCounterText";
    public static final String key_chats_name = "chats_name";
    public static final String key_chats_secretName = "chats_secretName";
    public static final String key_chats_secretIcon = "chats_secretIcon";
    public static final String key_chats_nameIcon = "chats_nameIcon";
    public static final String key_chats_pinnedIcon = "chats_pinnedIcon";
    public static final String key_chats_message = "chats_message";
    public static final String key_chats_draft = "chats_draft";
    public static final String key_chats_nameMessage = "chats_nameMessage";
    public static final String key_chats_attachMessage = "chats_attachMessage";
    public static final String key_chats_actionMessage = "chats_actionMessage";
    public static final String key_chats_date = "chats_date";
    public static final String key_chats_pinnedOverlay = "chats_pinnedOverlay";
    public static final String key_chats_tabletSelectedOverlay = "chats_tabletSelectedOverlay";
    public static final String key_chats_sentCheck = "chats_sentCheck";
    public static final String key_chats_sentClock = "chats_sentClock";
    public static final String key_chats_sentError = "chats_sentError";
    public static final String key_chats_sentErrorIcon = "chats_sentErrorIcon";
    public static final String key_chats_verifiedBackground = "chats_verifiedBackground";
    public static final String key_chats_verifiedCheck = "chats_verifiedCheck";
    public static final String key_chats_muteIcon = "chats_muteIcon";
    public static final String key_chats_menuTopShadow = "chats_menuTopShadow";
    public static final String key_chats_menuBackground = "chats_menuBackground";
    public static final String key_chats_menuItemText = "chats_menuItemText";
    public static final String key_chats_menuItemIcon = "chats_menuItemIcon";
    public static final String key_chats_menuName = "chats_menuName";
    public static final String key_chats_menuPhone = "chats_menuPhone";
    public static final String key_chats_menuPhoneCats = "chats_menuPhoneCats";
    public static final String key_chats_menuCloud = "chats_menuCloud";
    public static final String key_chats_menuCloudBackgroundCats = "chats_menuCloudBackgroundCats";
    public static final String key_chats_actionIcon = "chats_actionIcon";
    public static final String key_chats_actionBackground = "chats_actionBackground";
    public static final String key_chats_actionPressedBackground = "chats_actionPressedBackground";

    public static final String key_chat_inBubble = "chat_inBubble";
    public static final String key_chat_inBubbleSelected = "chat_inBubbleSelected";
    public static final String key_chat_inBubbleShadow = "chat_inBubbleShadow";
    public static final String key_chat_outBubble = "chat_outBubble";
    public static final String key_chat_outBubbleSelected = "chat_outBubbleSelected";
    public static final String key_chat_outBubbleShadow = "chat_outBubbleShadow";
    public static final String key_chat_messageTextIn = "chat_messageTextIn";
    public static final String key_chat_messageTextOut = "chat_messageTextOut";
    public static final String key_chat_messageLinkIn = "chat_messageLinkIn";
    public static final String key_chat_messageLinkOut = "chat_messageLinkOut";
    public static final String key_chat_serviceText = "chat_serviceText";
    public static final String key_chat_serviceLink = "chat_serviceLink";
    public static final String key_chat_serviceIcon = "chat_serviceIcon";
    public static final String key_chat_serviceBackground = "chat_serviceBackground";
    public static final String key_chat_serviceBackgroundSelected = "chat_serviceBackgroundSelected";
    public static final String key_chat_muteIcon = "chat_muteIcon";
    public static final String key_chat_lockIcon = "chat_lockIcon";
    public static final String key_chat_outSentCheck = "chat_outSentCheck";
    public static final String key_chat_outSentCheckSelected = "chat_outSentCheckSelected";
    public static final String key_chat_outSentClock = "chat_outSentClock";
    public static final String key_chat_outSentClockSelected = "chat_outSentClockSelected";
    public static final String key_chat_inSentClock = "chat_inSentClock";
    public static final String key_chat_inSentClockSelected = "chat_inSentClockSelected";
    public static final String key_chat_mediaSentCheck = "chat_mediaSentCheck";
    public static final String key_chat_mediaSentClock = "chat_mediaSentClock";
    public static final String key_chat_mediaTimeBackground = "chat_mediaTimeBackground";
    public static final String key_chat_outViews = "chat_outViews";
    public static final String key_chat_outViewsSelected = "chat_outViewsSelected";
    public static final String key_chat_inViews = "chat_inViews";
    public static final String key_chat_inViewsSelected = "chat_inViewsSelected";
    public static final String key_chat_mediaViews = "chat_mediaViews";
    public static final String key_chat_outMenu = "chat_outMenu";
    public static final String key_chat_outMenuSelected = "chat_outMenuSelected";
    public static final String key_chat_inMenu = "chat_inMenu";
    public static final String key_chat_inMenuSelected = "chat_inMenuSelected";
    public static final String key_chat_mediaMenu = "chat_mediaMenu";
    public static final String key_chat_outInstant = "chat_outInstant";
    public static final String key_chat_outInstantSelected = "chat_outInstantSelected";
    public static final String key_chat_inInstant = "chat_inInstant";
    public static final String key_chat_inInstantSelected = "chat_inInstantSelected";
    public static final String key_chat_sentError = "chat_sentError";
    public static final String key_chat_sentErrorIcon = "chat_sentErrorIcon";
    public static final String key_chat_selectedBackground = "chat_selectedBackground";
    public static final String key_chat_previewDurationText = "chat_previewDurationText";
    public static final String key_chat_previewGameText = "chat_previewGameText";
    public static final String key_chat_inPreviewInstantText = "chat_inPreviewInstantText";
    public static final String key_chat_outPreviewInstantText = "chat_outPreviewInstantText";
    public static final String key_chat_inPreviewInstantSelectedText = "chat_inPreviewInstantSelectedText";
    public static final String key_chat_outPreviewInstantSelectedText = "chat_outPreviewInstantSelectedText";
    public static final String key_chat_secretTimeText = "chat_secretTimeText";
    public static final String key_chat_stickerNameText = "chat_stickerNameText";
    public static final String key_chat_botButtonText = "chat_botButtonText";
    public static final String key_chat_botProgress = "chat_botProgress";
    public static final String key_chat_inForwardedNameText = "chat_inForwardedNameText";
    public static final String key_chat_outForwardedNameText = "chat_outForwardedNameText";
    public static final String key_chat_inViaBotNameText = "chat_inViaBotNameText";
    public static final String key_chat_outViaBotNameText = "chat_outViaBotNameText";
    public static final String key_chat_stickerViaBotNameText = "chat_stickerViaBotNameText";
    public static final String key_chat_inReplyLine = "chat_inReplyLine";
    public static final String key_chat_outReplyLine = "chat_outReplyLine";
    public static final String key_chat_stickerReplyLine = "chat_stickerReplyLine";
    public static final String key_chat_inReplyNameText = "chat_inReplyNameText";
    public static final String key_chat_outReplyNameText = "chat_outReplyNameText";
    public static final String key_chat_stickerReplyNameText = "chat_stickerReplyNameText";
    public static final String key_chat_inReplyMessageText = "chat_inReplyMessageText";
    public static final String key_chat_outReplyMessageText = "chat_outReplyMessageText";
    public static final String key_chat_inReplyMediaMessageText = "chat_inReplyMediaMessageText";
    public static final String key_chat_outReplyMediaMessageText = "chat_outReplyMediaMessageText";
    public static final String key_chat_inReplyMediaMessageSelectedText = "chat_inReplyMediaMessageSelectedText";
    public static final String key_chat_outReplyMediaMessageSelectedText = "chat_outReplyMediaMessageSelectedText";
    public static final String key_chat_stickerReplyMessageText = "chat_stickerReplyMessageText";
    public static final String key_chat_inPreviewLine = "chat_inPreviewLine";
    public static final String key_chat_outPreviewLine = "chat_outPreviewLine";
    public static final String key_chat_inSiteNameText = "chat_inSiteNameText";
    public static final String key_chat_outSiteNameText = "chat_outSiteNameText";
    public static final String key_chat_inContactNameText = "chat_inContactNameText";
    public static final String key_chat_outContactNameText = "chat_outContactNameText";
    public static final String key_chat_inContactPhoneText = "chat_inContactPhoneText";
    public static final String key_chat_outContactPhoneText = "chat_outContactPhoneText";
    public static final String key_chat_mediaProgress = "chat_mediaProgress";
    public static final String key_chat_inAudioProgress = "chat_inAudioProgress";
    public static final String key_chat_outAudioProgress = "chat_outAudioProgress";
    public static final String key_chat_inAudioSelectedProgress = "chat_inAudioSelectedProgress";
    public static final String key_chat_outAudioSelectedProgress = "chat_outAudioSelectedProgress";
    public static final String key_chat_mediaTimeText = "chat_mediaTimeText";
    public static final String key_chat_inTimeText = "chat_inTimeText";
    public static final String key_chat_outTimeText = "chat_outTimeText";
    public static final String key_chat_inTimeSelectedText = "chat_inTimeSelectedText";
    public static final String key_chat_outTimeSelectedText = "chat_outTimeSelectedText";
    public static final String key_chat_inAudioPerfomerText = "chat_inAudioPerfomerText";
    public static final String key_chat_outAudioPerfomerText = "chat_outAudioPerfomerText";
    public static final String key_chat_inAudioTitleText = "chat_inAudioTitleText";
    public static final String key_chat_outAudioTitleText = "chat_outAudioTitleText";
    public static final String key_chat_inAudioDurationText = "chat_inAudioDurationText";
    public static final String key_chat_outAudioDurationText = "chat_outAudioDurationText";
    public static final String key_chat_inAudioDurationSelectedText = "chat_inAudioDurationSelectedText";
    public static final String key_chat_outAudioDurationSelectedText = "chat_outAudioDurationSelectedText";
    public static final String key_chat_inAudioSeekbar = "chat_inAudioSeekbar";
    public static final String key_chat_outAudioSeekbar = "chat_outAudioSeekbar";
    public static final String key_chat_inAudioSeekbarSelected = "chat_inAudioSeekbarSelected";
    public static final String key_chat_outAudioSeekbarSelected = "chat_outAudioSeekbarSelected";
    public static final String key_chat_inAudioSeekbarFill = "chat_inAudioSeekbarFill";
    public static final String key_chat_outAudioSeekbarFill = "chat_outAudioSeekbarFill";
    public static final String key_chat_inVoiceSeekbar = "chat_inVoiceSeekbar";
    public static final String key_chat_outVoiceSeekbar = "chat_outVoiceSeekbar";
    public static final String key_chat_inVoiceSeekbarSelected = "chat_inVoiceSeekbarSelected";
    public static final String key_chat_outVoiceSeekbarSelected = "chat_outVoiceSeekbarSelected";
    public static final String key_chat_inVoiceSeekbarFill = "chat_inVoiceSeekbarFill";
    public static final String key_chat_outVoiceSeekbarFill = "chat_outVoiceSeekbarFill";
    public static final String key_chat_inFileProgress = "chat_inFileProgress";
    public static final String key_chat_outFileProgress = "chat_outFileProgress";
    public static final String key_chat_inFileProgressSelected = "chat_inFileProgressSelected";
    public static final String key_chat_outFileProgressSelected = "chat_outFileProgressSelected";
    public static final String key_chat_inFileNameText = "chat_inFileNameText";
    public static final String key_chat_outFileNameText = "chat_outFileNameText";
    public static final String key_chat_inFileInfoText = "chat_inFileInfoText";
    public static final String key_chat_outFileInfoText = "chat_outFileInfoText";
    public static final String key_chat_inFileInfoSelectedText = "chat_inFileInfoSelectedText";
    public static final String key_chat_outFileInfoSelectedText = "chat_outFileInfoSelectedText";
    public static final String key_chat_inFileBackground = "chat_inFileBackground";
    public static final String key_chat_outFileBackground = "chat_outFileBackground";
    public static final String key_chat_inFileBackgroundSelected = "chat_inFileBackgroundSelected";
    public static final String key_chat_outFileBackgroundSelected = "chat_outFileBackgroundSelected";
    public static final String key_chat_inVenueNameText = "chat_inVenueNameText";
    public static final String key_chat_outVenueNameText = "chat_outVenueNameText";
    public static final String key_chat_inVenueInfoText = "chat_inVenueInfoText";
    public static final String key_chat_outVenueInfoText = "chat_outVenueInfoText";
    public static final String key_chat_inVenueInfoSelectedText = "chat_inVenueInfoSelectedText";
    public static final String key_chat_outVenueInfoSelectedText = "chat_outVenueInfoSelectedText";
    public static final String key_chat_mediaInfoText = "chat_mediaInfoText";
    public static final String key_chat_linkSelectBackground = "chat_linkSelectBackground";
    public static final String key_chat_textSelectBackground = "chat_textSelectBackground";
    public static final String key_chat_wallpaper = "chat_wallpaper";
    public static final String key_chat_messagePanelBackground = "chat_messagePanelBackground";
    public static final String key_chat_messagePanelShadow = "chat_messagePanelShadow";
    public static final String key_chat_messagePanelText = "chat_messagePanelText";
    public static final String key_chat_messagePanelHint = "chat_messagePanelHint";
    public static final String key_chat_messagePanelIcons = "chat_messagePanelIcons";
    public static final String key_chat_messagePanelSend = "chat_messagePanelSend";
    public static final String key_chat_messagePanelVoiceLock = "key_chat_messagePanelVoiceLock";
    public static final String key_chat_messagePanelVoiceLockBackground = "key_chat_messagePanelVoiceLockBackground";
    public static final String key_chat_messagePanelVoiceLockShadow = "key_chat_messagePanelVoiceLockShadow";
    public static final String key_chat_topPanelBackground = "chat_topPanelBackground";
    public static final String key_chat_topPanelClose = "chat_topPanelClose";
    public static final String key_chat_topPanelLine = "chat_topPanelLine";
    public static final String key_chat_topPanelTitle = "chat_topPanelTitle";
    public static final String key_chat_topPanelMessage = "chat_topPanelMessage";
    public static final String key_chat_reportSpam = "chat_reportSpam";
    public static final String key_chat_addContact = "chat_addContact";
    public static final String key_chat_inLoader = "chat_inLoader";
    public static final String key_chat_inLoaderSelected = "chat_inLoaderSelected";
    public static final String key_chat_outLoader = "chat_outLoader";
    public static final String key_chat_outLoaderSelected = "chat_outLoaderSelected";
    public static final String key_chat_inLoaderPhoto = "chat_inLoaderPhoto";
    public static final String key_chat_inLoaderPhotoSelected = "chat_inLoaderPhotoSelected";
    public static final String key_chat_inLoaderPhotoIcon = "chat_inLoaderPhotoIcon";
    public static final String key_chat_inLoaderPhotoIconSelected = "chat_inLoaderPhotoIconSelected";
    public static final String key_chat_outLoaderPhoto = "chat_outLoaderPhoto";
    public static final String key_chat_outLoaderPhotoSelected = "chat_outLoaderPhotoSelected";
    public static final String key_chat_outLoaderPhotoIcon = "chat_outLoaderPhotoIcon";
    public static final String key_chat_outLoaderPhotoIconSelected = "chat_outLoaderPhotoIconSelected";
    public static final String key_chat_mediaLoaderPhoto = "chat_mediaLoaderPhoto";
    public static final String key_chat_mediaLoaderPhotoSelected = "chat_mediaLoaderPhotoSelected";
    public static final String key_chat_mediaLoaderPhotoIcon = "chat_mediaLoaderPhotoIcon";
    public static final String key_chat_mediaLoaderPhotoIconSelected = "chat_mediaLoaderPhotoIconSelected";
    public static final String key_chat_inLocationBackground = "chat_inLocationBackground";
    public static final String key_chat_inLocationIcon = "chat_inLocationIcon";
    public static final String key_chat_outLocationBackground = "chat_outLocationBackground";
    public static final String key_chat_outLocationIcon = "chat_outLocationIcon";
    public static final String key_chat_inContactBackground = "chat_inContactBackground";
    public static final String key_chat_inContactIcon = "chat_inContactIcon";
    public static final String key_chat_outContactBackground = "chat_outContactBackground";
    public static final String key_chat_outContactIcon = "chat_outContactIcon";
    public static final String key_chat_inFileIcon = "chat_inFileIcon";
    public static final String key_chat_inFileSelectedIcon = "chat_inFileSelectedIcon";
    public static final String key_chat_outFileIcon = "chat_outFileIcon";
    public static final String key_chat_outFileSelectedIcon = "chat_outFileSelectedIcon";
    public static final String key_chat_replyPanelIcons = "chat_replyPanelIcons";
    public static final String key_chat_replyPanelClose = "chat_replyPanelClose";
    public static final String key_chat_replyPanelName = "chat_replyPanelName";
    public static final String key_chat_replyPanelMessage = "chat_replyPanelMessage";
    public static final String key_chat_replyPanelLine = "chat_replyPanelLine";
    public static final String key_chat_searchPanelIcons = "chat_searchPanelIcons";
    public static final String key_chat_searchPanelText = "chat_searchPanelText";
    public static final String key_chat_secretChatStatusText = "chat_secretChatStatusText";
    public static final String key_chat_fieldOverlayText = "chat_fieldOverlayText";
    public static final String key_chat_stickersHintPanel = "chat_stickersHintPanel";
    public static final String key_chat_botSwitchToInlineText = "chat_botSwitchToInlineText";
    public static final String key_chat_unreadMessagesStartArrowIcon = "chat_unreadMessagesStartArrowIcon";
    public static final String key_chat_unreadMessagesStartText = "chat_unreadMessagesStartText";
    public static final String key_chat_unreadMessagesStartBackground = "chat_unreadMessagesStartBackground";
    public static final String key_chat_inlineResultIcon = "chat_inlineResultIcon";
    public static final String key_chat_emojiPanelBackground = "chat_emojiPanelBackground";
    public static final String key_chat_emojiPanelShadowLine = "chat_emojiPanelShadowLine";
    public static final String key_chat_emojiPanelEmptyText = "chat_emojiPanelEmptyText";
    public static final String key_chat_emojiPanelIcon = "chat_emojiPanelIcon";
    public static final String key_chat_emojiPanelIconSelected = "chat_emojiPanelIconSelected";
    public static final String key_chat_emojiPanelStickerPackSelector = "chat_emojiPanelStickerPackSelector";
    public static final String key_chat_emojiPanelIconSelector = "chat_emojiPanelIconSelector";
    public static final String key_chat_emojiPanelBackspace = "chat_emojiPanelBackspace";
    public static final String key_chat_emojiPanelMasksIcon = "chat_emojiPanelMasksIcon";
    public static final String key_chat_emojiPanelMasksIconSelected = "chat_emojiPanelMasksIconSelected";
    public static final String key_chat_emojiPanelTrendingTitle = "chat_emojiPanelTrendingTitle";
    public static final String key_chat_emojiPanelTrendingDescription = "chat_emojiPanelTrendingDescription";
    public static final String key_chat_botKeyboardButtonText = "chat_botKeyboardButtonText";
    public static final String key_chat_botKeyboardButtonBackground = "chat_botKeyboardButtonBackground";
    public static final String key_chat_botKeyboardButtonBackgroundPressed = "chat_botKeyboardButtonBackgroundPressed";
    public static final String key_chat_emojiPanelNewTrending = "chat_emojiPanelNewTrending";
    public static final String key_chat_editDoneIcon = "chat_editDoneIcon";
    public static final String key_chat_messagePanelVoicePressed = "chat_messagePanelVoicePressed";
    public static final String key_chat_messagePanelVoiceBackground = "chat_messagePanelVoiceBackground";
    public static final String key_chat_messagePanelVoiceShadow = "chat_messagePanelVoiceShadow";
    public static final String key_chat_messagePanelVoiceDelete = "chat_messagePanelVoiceDelete";
    public static final String key_chat_messagePanelVoiceDuration = "chat_messagePanelVoiceDuration";
    public static final String key_chat_recordedVoicePlayPause = "chat_recordedVoicePlayPause";
    public static final String key_chat_recordedVoicePlayPausePressed = "chat_recordedVoicePlayPausePressed";
    public static final String key_chat_recordedVoiceProgress = "chat_recordedVoiceProgress";
    public static final String key_chat_recordedVoiceProgressInner = "chat_recordedVoiceProgressInner";
    public static final String key_chat_recordedVoiceDot = "chat_recordedVoiceDot";
    public static final String key_chat_recordedVoiceBackground = "chat_recordedVoiceBackground";
    public static final String key_chat_recordVoiceCancel = "chat_recordVoiceCancel";
    public static final String key_chat_recordTime = "chat_recordTime";
    public static final String key_chat_messagePanelCancelInlineBot = "chat_messagePanelCancelInlineBot";
    public static final String key_chat_gifSaveHintText = "chat_gifSaveHintText";
    public static final String key_chat_gifSaveHintBackground = "chat_gifSaveHintBackground";
    public static final String key_chat_goDownButton = "chat_goDownButton";
    public static final String key_chat_goDownButtonShadow = "chat_goDownButtonShadow";
    public static final String key_chat_goDownButtonIcon = "chat_goDownButtonIcon";
    public static final String key_chat_goDownButtonCounter = "chat_goDownButtonCounter";
    public static final String key_chat_goDownButtonCounterBackground = "chat_goDownButtonCounterBackground";
    public static final String key_chat_secretTimerBackground = "chat_secretTimerBackground";
    public static final String key_chat_secretTimerText = "chat_secretTimerText";

    public static final String key_profile_creatorIcon = "profile_creatorIcon";
    public static final String key_profile_adminIcon = "profile_adminIcon";
    public static final String key_profile_title = "profile_title";
    public static final String key_profile_actionIcon = "profile_actionIcon";
    public static final String key_profile_actionBackground = "profile_actionBackground";
    public static final String key_profile_actionPressedBackground = "profile_actionPressedBackground";
    public static final String key_profile_verifiedBackground = "profile_verifiedBackground";
    public static final String key_profile_verifiedCheck = "profile_verifiedCheck";

    public static final String key_sharedMedia_startStopLoadIcon = "sharedMedia_startStopLoadIcon";
    public static final String key_sharedMedia_linkPlaceholder = "sharedMedia_linkPlaceholder";
    public static final String key_sharedMedia_linkPlaceholderText = "sharedMedia_linkPlaceholderText";

    public static final String key_featuredStickers_addedIcon = "featuredStickers_addedIcon";
    public static final String key_featuredStickers_buttonProgress = "featuredStickers_buttonProgress";
    public static final String key_featuredStickers_addButton = "featuredStickers_addButton";
    public static final String key_featuredStickers_addButtonPressed = "featuredStickers_addButtonPressed";
    public static final String key_featuredStickers_delButton = "featuredStickers_delButton";
    public static final String key_featuredStickers_delButtonPressed = "featuredStickers_delButtonPressed";
    public static final String key_featuredStickers_buttonText = "featuredStickers_buttonText";
    public static final String key_featuredStickers_unread = "featuredStickers_unread";

    public static final String key_stickers_menu = "stickers_menu";
    public static final String key_stickers_menuSelector = "stickers_menuSelector";

    public static final String key_changephoneinfo_image = "changephoneinfo_image";

    public static final String key_groupcreate_hintText = "groupcreate_hintText";
    public static final String key_groupcreate_cursor = "groupcreate_cursor";
    public static final String key_groupcreate_sectionShadow = "groupcreate_sectionShadow";
    public static final String key_groupcreate_sectionText = "groupcreate_sectionText";
    public static final String key_groupcreate_onlineText = "groupcreate_onlineText";
    public static final String key_groupcreate_offlineText = "groupcreate_offlineText";
    public static final String key_groupcreate_checkbox = "groupcreate_checkbox";
    public static final String key_groupcreate_checkboxCheck = "groupcreate_checkboxCheck";
    public static final String key_groupcreate_spanText = "groupcreate_spanText";
    public static final String key_groupcreate_spanBackground = "groupcreate_spanBackground";

    public static final String key_login_progressInner = "login_progressInner";
    public static final String key_login_progressOuter = "login_progressOuter";

    public static final String key_musicPicker_checkbox = "musicPicker_checkbox";
    public static final String key_musicPicker_checkboxCheck = "musicPicker_checkboxCheck";
    public static final String key_musicPicker_buttonBackground = "musicPicker_buttonBackground";
    public static final String key_musicPicker_buttonIcon = "musicPicker_buttonIcon";

    public static final String key_picker_enabledButton = "picker_enabledButton";
    public static final String key_picker_disabledButton = "picker_disabledButton";
    public static final String key_picker_badge = "picker_badge";
    public static final String key_picker_badgeText = "picker_badgeText";

    public static final String key_location_markerX = "location_markerX";
    public static final String key_location_sendLocationBackground = "location_sendLocationBackground";
    public static final String key_location_sendLocationIcon = "location_sendLocationIcon";

    public static final String key_files_folderIcon = "files_folderIcon";
    public static final String key_files_folderIconBackground = "files_folderIconBackground";
    public static final String key_files_iconText = "files_iconText";

    public static final String key_sessions_devicesImage = "sessions_devicesImage";

    public static final String key_calls_callReceivedGreenIcon = "calls_callReceivedGreenIcon";
    public static final String key_calls_callReceivedRedIcon = "calls_callReceivedRedIcon";

    public static final String key_calls_ratingStar = "calls_ratingStar";
    public static final String key_calls_ratingStarSelected = "calls_ratingStarSelected";

    //ununsed
    public static final String key_chat_outBroadcast = "chat_outBroadcast";
    public static final String key_chat_mediaBroadcast = "chat_mediaBroadcast";

    public static final String key_player_actionBar = "player_actionBar";
    public static final String key_player_actionBarSelector = "player_actionBarSelector";
    public static final String key_player_actionBarTitle = "player_actionBarTitle";
    public static final String key_player_actionBarTop = "player_actionBarTop";
    public static final String key_player_actionBarSubtitle = "player_actionBarSubtitle";
    public static final String key_player_actionBarItems = "player_actionBarItems";
    public static final String key_player_seekBarBackground = "player_seekBarBackground";
    public static final String key_player_time = "player_time";
    public static final String key_player_duration = "player_duration";
    public static final String key_player_progressBackground = "player_progressBackground";
    public static final String key_player_progress = "player_progress";
    public static final String key_player_placeholder = "player_placeholder";
    public static final String key_player_button = "player_button";
    public static final String key_player_buttonActive = "player_buttonActive";

    private static HashMap<String, Integer> defaultColors = new HashMap<>();
    private static HashMap<String, Integer> currentColors;
    //plus
    static {
        telegramToPlus.put(key_actionBarDefault, pkey_themeColor); //0xff527da3
        telegramToPlus.put(key_actionBarDefaultTitle, pkey_chatsHeaderTitleColor); //0xffffffff default title using main screen
        telegramToPlus.put(key_windowBackgroundWhiteBlueHeader, pkey_themeColor); //0xff3e90cf

        telegramToPlus.put(key_actionBarDefaultIcon, pkey_chatHeaderIconsColor); //0xffffffff
        //key_actionBarDefaultSelector 0xff406d94 actionBar.setItemsBackgroundColor
        //key_actionBarActionModeDefaultSelector 0xfff0f0f0 actionBar.setItemsBackground
        //
        //key_chat_messageTextOut
        //key_chat_messageLinkOut
        telegramToPlus.put(key_chat_messageTextOut, pkey_chatRTextColor);
        telegramToPlus.put(key_chat_outFileNameText, pkey_chatRTextColor);
        telegramToPlus.put(key_chat_outFileInfoText, pkey_chatRTextColor);
        telegramToPlus.put(key_chat_inFileNameText, pkey_chatLTextColor);
        telegramToPlus.put(key_chat_inFileInfoText, pkey_chatLTextColor);
        telegramToPlus.put(key_chat_outMenu, pkey_chatRTimeColor);
        telegramToPlus.put(key_chat_inMenu, pkey_chatLTimeColor);
        //
        telegramToPlus.put(key_chat_outSentCheck, pkey_chatChecksColor);    //0xff5db050
        telegramToPlus.put(key_chat_outSentCheckSelected, pkey_chatChecksColor);    //0xff5db050
        telegramToPlus.put(key_chat_outSentClock, pkey_chatChecksColor);    //0xff75bd5e
        telegramToPlus.put(key_chat_outSentClockSelected, pkey_chatChecksColor);    //0xff75bd5e
        telegramToPlus.put(key_chat_mediaSentCheck, pkey_chatChecksColor);    //0xffffffff
        /*
            setDrawableColorByKey(chat_msgInClockDrawable, key_chat_inSentClock);
            setDrawableColorByKey(chat_msgInSelectedClockDrawable, key_chat_inSentClockSelected);

         */

        telegramToPlus.put(key_chat_messageLinkOut, pkey_chatRLinkColor);
        telegramToPlus.put(key_chat_messageTextIn, pkey_chatLTextColor);
        telegramToPlus.put(key_chat_messageLinkIn, pkey_chatLLinkColor);
        telegramToPlus.put(key_chat_outTimeText, pkey_chatRTimeColor);
        telegramToPlus.put(key_chat_inTimeText, pkey_chatLTimeColor);
        telegramToPlus.put(key_chat_outForwardedNameText, pkey_chatForwardRColor);
        telegramToPlus.put(key_chat_inForwardedNameText, pkey_chatForwardLColor);
        telegramToPlus.put(key_chat_outReplyLine, pkey_chatForwardRColor);
        telegramToPlus.put(key_chat_outReplyNameText, pkey_chatForwardRColor);
        telegramToPlus.put(key_chat_outReplyMediaMessageText, pkey_chatForwardRColor);
        telegramToPlus.put(key_chat_outReplyMessageText, pkey_chatRTextColor);

        telegramToPlus.put(key_chat_inReplyLine, pkey_chatForwardLColor);
        telegramToPlus.put(key_chat_inReplyNameText, pkey_chatForwardLColor);
        telegramToPlus.put(key_chat_inReplyMediaMessageText, pkey_chatForwardLColor);
        telegramToPlus.put(key_chat_inReplyMessageText, pkey_chatLTextColor);

        telegramToPlus.put(key_chat_inBubble, pkey_chatLBubbleColor);
        telegramToPlus.put(key_chat_inBubbleShadow, pkey_chatLBubbleColor);
        telegramToPlus.put(key_chat_outBubble, pkey_chatRBubbleColor);
        telegramToPlus.put(key_chat_outBubbleShadow, pkey_chatRBubbleColor);
        telegramToPlus.put(key_actionBarActionModeDefaultIcon, /*pkey_chatEditTextIconsColor*/pkey_chatHeaderIconsColor); //0xff737373
        telegramToPlus.put(key_chat_serviceText, pkey_chatDateColor);   //0xffffffff
        telegramToPlus.put(key_chat_messagePanelVoiceBackground, pkey_themeColor);      //0xff5795cc
        telegramToPlus.put(key_chat_emojiPanelNewTrending, pkey_chatEditTextIconsColor);   //0xff4da6ea
        telegramToPlus.put(key_chat_messagePanelIcons, pkey_chatEditTextIconsColor);   //0xffa8a8a8
        telegramToPlus.put(key_chat_messagePanelVoiceDelete, pkey_chatEditTextIconsColor);   //0xff737373
        telegramToPlus.put(key_chat_messagePanelSend, pkey_chatSendIconColor);   //0xff62b0eb
        telegramToPlus.put(key_inappPlayerBackground, pkey_chatHeaderColor); //0xffffffff
        telegramToPlus.put(key_chat_messagePanelBackground, pkey_chatEditTextBGColor); //0xffffffff
        telegramToPlus.put(key_chat_topPanelBackground, pkey_chatHeaderColor); //0xffffffff
        telegramToPlus.put(key_chat_topPanelLine, pkey_chatNameColor); //0xff6c9fd2
        telegramToPlus.put(key_chat_topPanelTitle, pkey_chatNameColor); //0xff3a8ccf
        telegramToPlus.put(key_chat_topPanelMessage, pkey_chatStatusColor); //0xff999999
        telegramToPlus.put(key_chat_topPanelClose, pkey_chatStatusColor); //0xffa8a8a8
        telegramToPlus.put(key_chat_goDownButton, pkey_chatEditTextBGColor); //0xffffffff
        telegramToPlus.put(key_chat_goDownButtonIcon, pkey_chatEditTextIconsColor); //0xffa8a8a8
        //telegramToPlus.put(key_chat_goDownButtonCounter, pkey_chatHeaderIconsColor); //0xffffffff
        //telegramToPlus.put(key_chat_goDownButtonCounterBackground, pkey_chatHeaderColor); //0xff4da2e8
        //

        //key_chat_messagePanelIcons 0xffa8a8a8
        //key_chat_messagePanelVoiceShadow 0x0d000000
        //key_chat_messagePanelVoicePressed 0xffffffff

        //key_chat_topPanelTitle 0xff3a8ccf alertNameTextView
        //key_chat_topPanelMessage 0xff999999
        //key_chat_goDownButtonCounter 0xffffffff pagedownButtonCounter.setTextColor chatNameColor ??
        //key_chat_goDownButtonCounterBackground 0xff4da2e8 pagedownButtonCounter.setBackgroundDrawable
        //key_chat_replyPanelLine 0xffe8e8e8 replyLineView.setBackgroundColor chatEditTextBGColor ??
        //key_chat_replyPanelIcons 0xff57a8e6 replyIconImageView.setColorFilter
        //key_chat_replyPanelClose 0xffa8a8a8
        //key_chat_replyPanelName 0xff3a8ccf
        //key_chat_replyPanelMessage 0xff222222
        //key_chat_searchPanelIcons 0xff5da5dc
        //key_chat_searchPanelText 0xff4e9ad4
        //key_chat_selectedBackground 0x6633b5e5
        //key_chat_emojiPanelNewTrending 0xff4da6ea
        //
        //CHATS
        //
        telegramToPlus.put(key_chats_sentCheck, pkey_chatsChecksColor);   //0xff46aa36
        telegramToPlus.put(key_chats_sentClock, pkey_chatsChecksColor);   //0xff75bd5e

        telegramToPlus.put(key_chats_name, pkey_chatsNameColor);   //0xff212121
        telegramToPlus.put(key_chats_secretName, pkey_chatsNameColor);   //0xff00a60e
        telegramToPlus.put(key_chats_actionBackground, pkey_chatsFloatingBGColor);   //0xff6aa1ce
        //telegramToPlus.put(key_chats_actionIcon, pkey_chatsFloatingPencilColor);   //0xffffffff
        telegramToPlus.put(key_chats_attachMessage, pkey_chatsMessageColor);   //0xff4d83b3
        telegramToPlus.put(key_chats_nameMessage, pkey_chatsMemberColor);   //0xff4d83b3
        //key_chats_draft 0xffdd4b39
        //SETTINGS
        telegramToPlus.put(key_avatar_backgroundActionBarBlue, pkey_prefActionbarColor);   //0xff598fba << Cambia Drawer y Settings
        telegramToPlus.put(key_profile_title, pkey_prefActionbarTitleColor);   //0xffffffff
        telegramToPlus.put(key_avatar_subtitleInProfileBlue, pkey_prefActionbarStatusColor);   //0xffd7eafa
        telegramToPlus.put(key_avatar_backgroundInProfileBlue, pkey_prefAvatarColor);   //0xff5085b1
        telegramToPlus.put(key_windowBackgroundWhite, pkey_prefBGColor);   //0xffffffff
        //
        //key_inappPlayerTitle 0xff2f3438
        //DRAWER
        telegramToPlus.put(key_chats_menuItemText, pkey_drawerOptionColor);   //0xff444444
        telegramToPlus.put(key_chats_menuItemIcon, pkey_drawerIconColor);   //0xff737373
        telegramToPlus.put(key_chats_menuPhone, pkey_drawerPhoneColor);   //0xffffffff
        telegramToPlus.put(key_chats_menuCloud, pkey_drawerNameColor);   //0xffffffff
        //key_chats_menuCloudBackgroundCats 0xff427ba9 darkColor
        //key_chats_menuCloud 0xffffffff
        //

    }
    //
    static {
        defaultColors.put(key_dialogBackground, 0xffffffff);
        defaultColors.put(key_dialogBackgroundGray, 0xfff0f0f0);
        defaultColors.put(key_dialogTextBlack, 0xff212121);
        defaultColors.put(key_dialogTextLink, 0xff2678b6);
        defaultColors.put(key_dialogLinkSelection, 0x3362a9e3);
        defaultColors.put(key_dialogTextRed, 0xffcd5a5a);
        defaultColors.put(key_dialogTextBlue, 0xff2f8cc9);
        defaultColors.put(key_dialogTextBlue2, 0xff3a8ccf);
        defaultColors.put(key_dialogTextBlue3, 0xff3ec1f9);
        defaultColors.put(key_dialogTextBlue4, 0xff19a7e8);
        defaultColors.put(key_dialogTextGray, 0xff348bc1);
        defaultColors.put(key_dialogTextGray2, 0xff757575);
        defaultColors.put(key_dialogTextGray3, 0xff999999);
        defaultColors.put(key_dialogTextGray4, 0xffb3b3b3);
        defaultColors.put(key_dialogTextHint, 0xff979797);
        defaultColors.put(key_dialogIcon, 0xff8a8a8a);
        defaultColors.put(key_dialogGrayLine, 0xffd2d2d2);
        defaultColors.put(key_dialogInputField, 0xffdbdbdb);
        defaultColors.put(key_dialogInputFieldActivated, 0xff37a9f0);
        defaultColors.put(key_dialogCheckboxSquareBackground, 0xff43a0df);
        defaultColors.put(key_dialogCheckboxSquareCheck, 0xffffffff);
        defaultColors.put(key_dialogCheckboxSquareUnchecked, 0xff737373);
        defaultColors.put(key_dialogCheckboxSquareDisabled, 0xffb0b0b0);
        defaultColors.put(key_dialogRadioBackground, 0xffb3b3b3);
        defaultColors.put(key_dialogRadioBackgroundChecked, 0xff37a9f0);
        defaultColors.put(key_dialogProgressCircle, 0xff527da3);
        defaultColors.put(key_dialogLineProgress, 0xff527da3);
        defaultColors.put(key_dialogLineProgressBackground, 0xffdbdbdb);
        defaultColors.put(key_dialogButton, 0xff4991cc);
        defaultColors.put(key_dialogButtonSelector, 0x0f000000);
        defaultColors.put(key_dialogScrollGlow, 0xfff5f6f7);
        defaultColors.put(key_dialogRoundCheckBox, 0xff3ec1f9);
        defaultColors.put(key_dialogRoundCheckBoxCheck, 0xffffffff);
        defaultColors.put(key_dialogBadgeBackground, 0xff3ec1f9);
        defaultColors.put(key_dialogBadgeText, 0xffffffff);

        defaultColors.put(key_windowBackgroundWhite, 0xffffffff);
        defaultColors.put(key_progressCircle, 0xff527da3);
        defaultColors.put(key_windowBackgroundWhiteGrayIcon, 0xff737373);
        defaultColors.put(key_windowBackgroundWhiteBlueText, 0xff3b84c0);
        defaultColors.put(key_windowBackgroundWhiteBlueText2, 0xff348bc1);
        defaultColors.put(key_windowBackgroundWhiteBlueText3, 0xff2678b6);
        defaultColors.put(key_windowBackgroundWhiteBlueText4, 0xff4d83b3);
        defaultColors.put(key_windowBackgroundWhiteBlueText5, 0xff4c8eca);
        defaultColors.put(key_windowBackgroundWhiteBlueText6, 0xff3a8ccf);
        defaultColors.put(key_windowBackgroundWhiteBlueText7, 0xff377aae);
        defaultColors.put(key_windowBackgroundWhiteGreenText, 0xff26972c);
        defaultColors.put(key_windowBackgroundWhiteGreenText2, 0xff37a919);
        defaultColors.put(key_windowBackgroundWhiteRedText, 0xffcd5a5a);
        defaultColors.put(key_windowBackgroundWhiteRedText2, 0xffdb5151);
        defaultColors.put(key_windowBackgroundWhiteRedText3, 0xffd24949);
        defaultColors.put(key_windowBackgroundWhiteRedText4, 0xffcf3030);
        defaultColors.put(key_windowBackgroundWhiteRedText5, 0xffed3d39);
        defaultColors.put(key_windowBackgroundWhiteRedText6, 0xffff6666);
        defaultColors.put(key_windowBackgroundWhiteGrayText, 0xffa8a8a8);
        defaultColors.put(key_windowBackgroundWhiteGrayText2, 0xff8a8a8a);
        defaultColors.put(key_windowBackgroundWhiteGrayText3, 0xff999999);
        defaultColors.put(key_windowBackgroundWhiteGrayText4, 0xff808080);
        defaultColors.put(key_windowBackgroundWhiteGrayText5, 0xffa3a3a3);
        defaultColors.put(key_windowBackgroundWhiteGrayText6, 0xff757575);
        defaultColors.put(key_windowBackgroundWhiteGrayText7, 0xffc6c6c6);
        defaultColors.put(key_windowBackgroundWhiteGrayText8, 0xff6d6d72);
        defaultColors.put(key_windowBackgroundWhiteGrayLine, 0xffdbdbdb);
        defaultColors.put(key_windowBackgroundWhiteBlackText, 0xff212121);
        defaultColors.put(key_windowBackgroundWhiteHintText, 0xff979797);
        defaultColors.put(key_windowBackgroundWhiteValueText, 0xff2f8cc9);
        defaultColors.put(key_windowBackgroundWhiteLinkText, 0xff2678b6);
        defaultColors.put(key_windowBackgroundWhiteLinkSelection, 0x3362a9e3);
        defaultColors.put(key_windowBackgroundWhiteBlueHeader, 0xff3e90cf);
        defaultColors.put(key_windowBackgroundWhiteInputField, 0xffdbdbdb);
        defaultColors.put(key_windowBackgroundWhiteInputFieldActivated, 0xff37a9f0);
        defaultColors.put(key_switchThumb, 0xffededed);
        defaultColors.put(key_switchTrack, 0xffc7c7c7);
        defaultColors.put(key_switchThumbChecked, 0xff45abef);
        defaultColors.put(key_switchTrackChecked, 0xffa0d6fa);
        defaultColors.put(key_checkboxSquareBackground, 0xff43a0df);
        defaultColors.put(key_checkboxSquareCheck, 0xffffffff);
        defaultColors.put(key_checkboxSquareUnchecked, 0xff737373);
        defaultColors.put(key_checkboxSquareDisabled, 0xffb0b0b0);
        defaultColors.put(key_listSelector, 0x0f000000);
        defaultColors.put(key_radioBackground, 0xffb3b3b3);
        defaultColors.put(key_radioBackgroundChecked, 0xff37a9f0);
        defaultColors.put(key_windowBackgroundGray, 0xfff0f0f0);
        defaultColors.put(key_windowBackgroundGrayShadow, 0xff000000);
        defaultColors.put(key_emptyListPlaceholder, 0xff959595);
        defaultColors.put(key_divider, 0xffd9d9d9);
        defaultColors.put(key_graySection, 0xfff2f2f2);
        defaultColors.put(key_contextProgressInner1, 0xffbfdff6);
        defaultColors.put(key_contextProgressOuter1, 0xff2b96e2);
        defaultColors.put(key_contextProgressInner2, 0xffbfdff6);
        defaultColors.put(key_contextProgressOuter2, 0xffffffff);
        defaultColors.put(key_contextProgressInner3, 0xffb3b3b3);
        defaultColors.put(key_contextProgressOuter3, 0xffffffff);
        defaultColors.put(key_fastScrollActive, 0xff52a3db);
        defaultColors.put(key_fastScrollInactive, 0xff636363);
        defaultColors.put(key_fastScrollText, 0xffffffff);

        defaultColors.put(key_avatar_text, 0xffffffff);

        defaultColors.put(key_avatar_backgroundRed, 0xffe56555);
        defaultColors.put(key_avatar_backgroundOrange, 0xfff28c48);
        defaultColors.put(key_avatar_backgroundViolet, 0xff8e85ee);
        defaultColors.put(key_avatar_backgroundGreen, 0xff76c84d);
        defaultColors.put(key_avatar_backgroundCyan, 0xff5fbed5);
        defaultColors.put(key_avatar_backgroundBlue, 0xff549cdd);
        defaultColors.put(key_avatar_backgroundPink, 0xfff2749a);
        defaultColors.put(key_avatar_backgroundGroupCreateSpanBlue, 0xffbfd6ea);
        defaultColors.put(key_avatar_backgroundInProfileRed, 0xffd86f65);
        defaultColors.put(key_avatar_backgroundInProfileOrange, 0xfff69d61);
        defaultColors.put(key_avatar_backgroundInProfileViolet, 0xff8c79d2);
        defaultColors.put(key_avatar_backgroundInProfileGreen, 0xff67b35d);
        defaultColors.put(key_avatar_backgroundInProfileCyan, 0xff56a2bb);
        defaultColors.put(key_avatar_backgroundInProfileBlue, 0xff5085b1);
        defaultColors.put(key_avatar_backgroundInProfilePink, 0xfff37fa6);
        defaultColors.put(key_avatar_backgroundActionBarRed, 0xffca6056);
        defaultColors.put(key_avatar_backgroundActionBarOrange, 0xfff18944);
        defaultColors.put(key_avatar_backgroundActionBarViolet, 0xff7d6ac4);
        defaultColors.put(key_avatar_backgroundActionBarGreen, 0xff56a14c);
        defaultColors.put(key_avatar_backgroundActionBarCyan, 0xff4492ac);
        defaultColors.put(key_avatar_backgroundActionBarBlue, 0xff598fba);
        defaultColors.put(key_avatar_backgroundActionBarPink, 0xff598fba);
        defaultColors.put(key_avatar_subtitleInProfileRed, 0xfff9cbc5);
        defaultColors.put(key_avatar_subtitleInProfileOrange, 0xfffdddc8);
        defaultColors.put(key_avatar_subtitleInProfileViolet, 0xffcdc4ed);
        defaultColors.put(key_avatar_subtitleInProfileGreen, 0xffc0edba);
        defaultColors.put(key_avatar_subtitleInProfileCyan, 0xffb8e2f0);
        defaultColors.put(key_avatar_subtitleInProfileBlue, 0xffd7eafa);
        defaultColors.put(key_avatar_subtitleInProfilePink, 0xffd7eafa);
        defaultColors.put(key_avatar_nameInMessageRed, 0xffca5650);
        defaultColors.put(key_avatar_nameInMessageOrange, 0xffd87b29);
        defaultColors.put(key_avatar_nameInMessageViolet, 0xff4e92cc);
        defaultColors.put(key_avatar_nameInMessageGreen, 0xff50b232);
        defaultColors.put(key_avatar_nameInMessageCyan, 0xff42b1a8);
        defaultColors.put(key_avatar_nameInMessageBlue, 0xff4e92cc);
        defaultColors.put(key_avatar_nameInMessagePink, 0xff4e92cc);
        defaultColors.put(key_avatar_actionBarSelectorRed, 0xffbc4b41);
        defaultColors.put(key_avatar_actionBarSelectorOrange, 0xffe67429);
        defaultColors.put(key_avatar_actionBarSelectorViolet, 0xff735fbe);
        defaultColors.put(key_avatar_actionBarSelectorGreen, 0xff48953d);
        defaultColors.put(key_avatar_actionBarSelectorCyan, 0xff39849d);
        defaultColors.put(key_avatar_actionBarSelectorBlue, 0xff4981ad);
        defaultColors.put(key_avatar_actionBarSelectorPink, 0xff4981ad);
        defaultColors.put(key_avatar_actionBarIconRed, 0xffffffff);
        defaultColors.put(key_avatar_actionBarIconOrange, 0xffffffff);
        defaultColors.put(key_avatar_actionBarIconViolet, 0xffffffff);
        defaultColors.put(key_avatar_actionBarIconGreen, 0xffffffff);
        defaultColors.put(key_avatar_actionBarIconCyan, 0xffffffff);
        defaultColors.put(key_avatar_actionBarIconBlue, 0xffffffff);
        defaultColors.put(key_avatar_actionBarIconPink, 0xffffffff);

        defaultColors.put(key_actionBarDefault, 0xff527da3);
        defaultColors.put(key_actionBarDefaultIcon, 0xffffffff);
        defaultColors.put(key_actionBarActionModeDefault, 0xffffffff);
        defaultColors.put(key_actionBarActionModeDefaultTop, 0x99000000);
        defaultColors.put(key_actionBarActionModeDefaultIcon, 0xff737373);
        defaultColors.put(key_actionBarDefaultTitle, 0xffffffff);
        defaultColors.put(key_actionBarDefaultSubtitle, 0xffd5e8f7);
        defaultColors.put(key_actionBarDefaultSelector, 0xff406d94);
        defaultColors.put(key_actionBarWhiteSelector, 0x2f000000);
        defaultColors.put(key_actionBarDefaultSearch, 0xffffffff);
        defaultColors.put(key_actionBarDefaultSearchPlaceholder, 0x88ffffff);
        defaultColors.put(key_actionBarDefaultSubmenuItem, 0xff212121);
        defaultColors.put(key_actionBarDefaultSubmenuBackground, 0xffffffff);
        defaultColors.put(key_actionBarActionModeDefaultSelector, 0xfff0f0f0);

        defaultColors.put(key_chats_unreadCounter, 0xff4ecc5e);
        defaultColors.put(key_chats_unreadCounterMuted, 0xffc7c7c7);
        defaultColors.put(key_chats_unreadCounterText, 0xffffffff);
        defaultColors.put(key_chats_name, 0xff212121);
        defaultColors.put(key_chats_secretName, 0xff00a60e);
        defaultColors.put(key_chats_secretIcon, 0xff19b126);
        defaultColors.put(key_chats_nameIcon, 0xff242424);
        defaultColors.put(key_chats_pinnedIcon, 0xffa8a8a8);
        defaultColors.put(key_chats_message, 0xff8f8f8f);
        defaultColors.put(key_chats_draft, 0xffdd4b39);
        defaultColors.put(key_chats_nameMessage, 0xff4d83b3);
        defaultColors.put(key_chats_attachMessage, 0xff4d83b3);
        defaultColors.put(key_chats_actionMessage, 0xff4d83b3);
        defaultColors.put(key_chats_date, 0xff999999);
        defaultColors.put(key_chats_pinnedOverlay, 0x08000000);
        defaultColors.put(key_chats_tabletSelectedOverlay, 0x0f000000);
        defaultColors.put(key_chats_sentCheck, 0xff46aa36);
        defaultColors.put(key_chats_sentClock, 0xff75bd5e);
        defaultColors.put(key_chats_sentError, 0xffd55252);
        defaultColors.put(key_chats_sentErrorIcon, 0xffffffff);
        defaultColors.put(key_chats_verifiedBackground, 0xff33a8e6);
        defaultColors.put(key_chats_verifiedCheck, 0xffffffff);
        defaultColors.put(key_chats_muteIcon, 0xffa8a8a8);
        defaultColors.put(key_chats_menuBackground, 0xffffffff);
        defaultColors.put(key_chats_menuItemText, 0xff444444);
        defaultColors.put(key_chats_menuItemIcon, 0xff737373);
        defaultColors.put(key_chats_menuName, 0xffffffff);
        defaultColors.put(key_chats_menuPhone, 0xffffffff);
        defaultColors.put(key_chats_menuPhoneCats, 0xffc2e5ff);
        defaultColors.put(key_chats_menuCloud, 0xffffffff);
        defaultColors.put(key_chats_menuCloudBackgroundCats, 0xff427ba9);
        defaultColors.put(key_chats_actionIcon, 0xffffffff);
        defaultColors.put(key_chats_actionBackground, 0xff6aa1ce);
        defaultColors.put(key_chats_actionPressedBackground, 0xff5792c2);

        defaultColors.put(key_chat_lockIcon, 0xffffffff);
        defaultColors.put(key_chat_muteIcon, 0xffb1cce3);
        defaultColors.put(key_chat_inBubble, 0xffffffff);
        defaultColors.put(key_chat_inBubbleSelected, 0xffe2f8ff);
        defaultColors.put(key_chat_inBubbleShadow, 0xff1d3753);
        defaultColors.put(key_chat_outBubble, 0xffefffde);
        defaultColors.put(key_chat_outBubbleSelected, 0xffd4f5bc);
        defaultColors.put(key_chat_outBubbleShadow, 0xff1e750c);
        defaultColors.put(key_chat_messageTextIn, 0xff000000);
        defaultColors.put(key_chat_messageTextOut, 0xff000000);
        defaultColors.put(key_chat_messageLinkIn, 0xff2678b6);
        defaultColors.put(key_chat_messageLinkOut, 0xff2678b6);
        defaultColors.put(key_chat_serviceText, 0xffffffff);
        defaultColors.put(key_chat_serviceLink, 0xffffffff);
        defaultColors.put(key_chat_serviceIcon, 0xffffffff);
        defaultColors.put(key_chat_mediaTimeBackground, 0x66000000);
        defaultColors.put(key_chat_outSentCheck, 0xff5db050);
        defaultColors.put(key_chat_outSentCheckSelected, 0xff5db050);
        defaultColors.put(key_chat_outSentClock, 0xff75bd5e);
        defaultColors.put(key_chat_outSentClockSelected, 0xff75bd5e);
        defaultColors.put(key_chat_inSentClock, 0xffa1aab3);
        defaultColors.put(key_chat_inSentClockSelected, 0xff93bdca);
        defaultColors.put(key_chat_mediaSentCheck, 0xffffffff);
        defaultColors.put(key_chat_mediaSentClock, 0xffffffff);
        defaultColors.put(key_chat_inViews, 0xffa1aab3);
        defaultColors.put(key_chat_inViewsSelected, 0xff93bdca);
        defaultColors.put(key_chat_outViews, 0xff6eb257);
        defaultColors.put(key_chat_outViewsSelected, 0xff6eb257);
        defaultColors.put(key_chat_mediaViews, 0xffffffff);
        defaultColors.put(key_chat_inMenu, 0xffb6bdc5);
        defaultColors.put(key_chat_inMenuSelected, 0xff98c1ce);
        defaultColors.put(key_chat_outMenu, 0xff91ce7e);
        defaultColors.put(key_chat_outMenuSelected, 0xff91ce7e);
        defaultColors.put(key_chat_mediaMenu, 0xffffffff);
        defaultColors.put(key_chat_outInstant, 0xff55ab4f);
        defaultColors.put(key_chat_outInstantSelected, 0xff489943);
        defaultColors.put(key_chat_inInstant, 0xff3a8ccf);
        defaultColors.put(key_chat_inInstantSelected, 0xff3079b5);
        defaultColors.put(key_chat_sentError, 0xffdb3535);
        defaultColors.put(key_chat_sentErrorIcon, 0xffffffff);
        defaultColors.put(key_chat_selectedBackground, 0x6633b5e5);
        defaultColors.put(key_chat_previewDurationText, 0xffffffff);
        defaultColors.put(key_chat_previewGameText, 0xffffffff);
        defaultColors.put(key_chat_inPreviewInstantText, 0xff3a8ccf);
        defaultColors.put(key_chat_outPreviewInstantText, 0xff55ab4f);
        defaultColors.put(key_chat_inPreviewInstantSelectedText, 0xff3079b5);
        defaultColors.put(key_chat_outPreviewInstantSelectedText, 0xff489943);
        defaultColors.put(key_chat_secretTimeText, 0xffe4e2e0);
        defaultColors.put(key_chat_stickerNameText, 0xffffffff);
        defaultColors.put(key_chat_botButtonText, 0xffffffff);
        defaultColors.put(key_chat_botProgress, 0xffffffff);
        defaultColors.put(key_chat_inForwardedNameText, 0xff3886c7);
        defaultColors.put(key_chat_outForwardedNameText, 0xff55ab4f);
        defaultColors.put(key_chat_inViaBotNameText, 0xff3a8ccf);
        defaultColors.put(key_chat_outViaBotNameText, 0xff55ab4f);
        defaultColors.put(key_chat_stickerViaBotNameText, 0xffffffff);
        defaultColors.put(key_chat_inReplyLine, 0xff599fd8);
        defaultColors.put(key_chat_outReplyLine, 0xff6eb969);
        defaultColors.put(key_chat_stickerReplyLine, 0xffffffff);
        defaultColors.put(key_chat_inReplyNameText, 0xff3a8ccf);
        defaultColors.put(key_chat_outReplyNameText, 0xff55ab4f);
        defaultColors.put(key_chat_stickerReplyNameText, 0xffffffff);
        defaultColors.put(key_chat_inReplyMessageText, 0xff000000);
        defaultColors.put(key_chat_outReplyMessageText, 0xff000000);
        defaultColors.put(key_chat_inReplyMediaMessageText, 0xffa1aab3);
        defaultColors.put(key_chat_outReplyMediaMessageText, 0xff65b05b);
        defaultColors.put(key_chat_inReplyMediaMessageSelectedText, 0xff89b4c1);
        defaultColors.put(key_chat_outReplyMediaMessageSelectedText, 0xff65b05b);
        defaultColors.put(key_chat_stickerReplyMessageText, 0xffffffff);
        defaultColors.put(key_chat_inPreviewLine, 0xff70b4e8);
        defaultColors.put(key_chat_outPreviewLine, 0xff88c97b);
        defaultColors.put(key_chat_inSiteNameText, 0xff3a8ccf);
        defaultColors.put(key_chat_outSiteNameText, 0xff55ab4f);
        defaultColors.put(key_chat_inContactNameText, 0xff4e9ad4);
        defaultColors.put(key_chat_outContactNameText, 0xff55ab4f);
        defaultColors.put(key_chat_inContactPhoneText, 0xff2f3438);
        defaultColors.put(key_chat_outContactPhoneText, 0xff354234);
        defaultColors.put(key_chat_mediaProgress, 0xffffffff);
        defaultColors.put(key_chat_inAudioProgress, 0xffffffff);
        defaultColors.put(key_chat_outAudioProgress, 0xffefffde);
        defaultColors.put(key_chat_inAudioSelectedProgress, 0xffe2f8ff);
        defaultColors.put(key_chat_outAudioSelectedProgress, 0xffd4f5bc);
        defaultColors.put(key_chat_mediaTimeText, 0xffffffff);
        defaultColors.put(key_chat_inTimeText, 0xffa1aab3);
        defaultColors.put(key_chat_outTimeText, 0xff70b15c);
        defaultColors.put(key_chat_inTimeSelectedText, 0xff89b4c1);
        defaultColors.put(key_chat_outTimeSelectedText, 0xff70b15c);
        defaultColors.put(key_chat_inAudioPerfomerText, 0xff2f3438);
        defaultColors.put(key_chat_outAudioPerfomerText, 0xff354234);
        defaultColors.put(key_chat_inAudioTitleText, 0xff4e9ad4);
        defaultColors.put(key_chat_outAudioTitleText, 0xff55ab4f);
        defaultColors.put(key_chat_inAudioDurationText, 0xffa1aab3);
        defaultColors.put(key_chat_outAudioDurationText, 0xff65b05b);
        defaultColors.put(key_chat_inAudioDurationSelectedText, 0xff89b4c1);
        defaultColors.put(key_chat_outAudioDurationSelectedText, 0xff65b05b);
        defaultColors.put(key_chat_inAudioSeekbar, 0xffe4eaf0);
        defaultColors.put(key_chat_outAudioSeekbar, 0xffbbe3ac);
        defaultColors.put(key_chat_inAudioSeekbarSelected, 0xffbcdee8);
        defaultColors.put(key_chat_outAudioSeekbarSelected, 0xffa9dd96);
        defaultColors.put(key_chat_inAudioSeekbarFill, 0xff72b5e8);
        defaultColors.put(key_chat_outAudioSeekbarFill, 0xff78c272);
        defaultColors.put(key_chat_inVoiceSeekbar, 0xffdee5eb);
        defaultColors.put(key_chat_outVoiceSeekbar, 0xffbbe3ac);
        defaultColors.put(key_chat_inVoiceSeekbarSelected, 0xffbcdee8);
        defaultColors.put(key_chat_outVoiceSeekbarSelected, 0xffa9dd96);
        defaultColors.put(key_chat_inVoiceSeekbarFill, 0xff72b5e8);
        defaultColors.put(key_chat_outVoiceSeekbarFill, 0xff78c272);
        defaultColors.put(key_chat_inFileProgress, 0xffebf0f5);
        defaultColors.put(key_chat_outFileProgress, 0xffdaf5c3);
        defaultColors.put(key_chat_inFileProgressSelected, 0xffcbeaf6);
        defaultColors.put(key_chat_outFileProgressSelected, 0xffc5eca7);
        defaultColors.put(key_chat_inFileNameText, 0xff4e9ad4);
        defaultColors.put(key_chat_outFileNameText, 0xff55ab4f);
        defaultColors.put(key_chat_inFileInfoText, 0xffa1aab3);
        defaultColors.put(key_chat_outFileInfoText, 0xff65b05b);
        defaultColors.put(key_chat_inFileInfoSelectedText, 0xff89b4c1);
        defaultColors.put(key_chat_outFileInfoSelectedText, 0xff65b05b);
        defaultColors.put(key_chat_inFileBackground, 0xffebf0f5);
        defaultColors.put(key_chat_outFileBackground, 0xffdaf5c3);
        defaultColors.put(key_chat_inFileBackgroundSelected, 0xffcbeaf6);
        defaultColors.put(key_chat_outFileBackgroundSelected, 0xffc5eca7);
        defaultColors.put(key_chat_inVenueNameText, 0xff4e9ad4);
        defaultColors.put(key_chat_outVenueNameText, 0xff55ab4f);
        defaultColors.put(key_chat_inVenueInfoText, 0xffa1aab3);
        defaultColors.put(key_chat_outVenueInfoText, 0xff65b05b);
        defaultColors.put(key_chat_inVenueInfoSelectedText, 0xff89b4c1);
        defaultColors.put(key_chat_outVenueInfoSelectedText, 0xff65b05b);
        defaultColors.put(key_chat_mediaInfoText, 0xffffffff);
        defaultColors.put(key_chat_linkSelectBackground, 0x3362a9e3);
        defaultColors.put(key_chat_textSelectBackground, 0x6662a9e3);
        defaultColors.put(key_chat_emojiPanelBackground, 0xfff5f6f7);
        defaultColors.put(key_chat_emojiPanelShadowLine, 0xffe2e5e7);
        defaultColors.put(key_chat_emojiPanelEmptyText, 0xff888888);
        defaultColors.put(key_chat_emojiPanelIcon, 0xffa8a8a8);
        defaultColors.put(key_chat_emojiPanelIconSelected, 0xff2b96e2);
        defaultColors.put(key_chat_emojiPanelStickerPackSelector, 0xffe2e5e7);
        defaultColors.put(key_chat_emojiPanelIconSelector, 0xff2b96e2);
        defaultColors.put(key_chat_emojiPanelBackspace, 0xffa8a8a8);
        defaultColors.put(key_chat_emojiPanelMasksIcon, 0xffffffff);
        defaultColors.put(key_chat_emojiPanelMasksIconSelected, 0xff62bfe8);
        defaultColors.put(key_chat_emojiPanelTrendingTitle, 0xff212121);
        defaultColors.put(key_chat_emojiPanelTrendingDescription, 0xff8a8a8a);
        defaultColors.put(key_chat_botKeyboardButtonText, 0xff36474f);
        defaultColors.put(key_chat_botKeyboardButtonBackground, 0xffe4e7e9);
        defaultColors.put(key_chat_botKeyboardButtonBackgroundPressed, 0xffccd1d4);
        defaultColors.put(key_chat_unreadMessagesStartArrowIcon, 0xffa2b5c7);
        defaultColors.put(key_chat_unreadMessagesStartText, 0xff5695cc);
        defaultColors.put(key_chat_unreadMessagesStartBackground, 0xffffffff);
        defaultColors.put(key_chat_editDoneIcon, 0xff51bdf3);
        defaultColors.put(key_chat_inFileIcon, 0xffa2b5c7);
        defaultColors.put(key_chat_inFileSelectedIcon, 0xff87b6c5);
        defaultColors.put(key_chat_outFileIcon, 0xff85bf78);
        defaultColors.put(key_chat_outFileSelectedIcon, 0xff85bf78);
        defaultColors.put(key_chat_inLocationBackground, 0xffebf0f5);
        defaultColors.put(key_chat_inLocationIcon, 0xffa2b5c7);
        defaultColors.put(key_chat_outLocationBackground, 0xffdaf5c3);
        defaultColors.put(key_chat_outLocationIcon, 0xff87bf78);
        defaultColors.put(key_chat_inContactBackground, 0xff72b5e8);
        defaultColors.put(key_chat_inContactIcon, 0xffffffff);
        defaultColors.put(key_chat_outContactBackground, 0xff78c272);
        defaultColors.put(key_chat_outContactIcon, 0xffefffde);
        defaultColors.put(key_chat_outBroadcast, 0xff46aa36);
        defaultColors.put(key_chat_mediaBroadcast, 0xffffffff);
        defaultColors.put(key_chat_searchPanelIcons, 0xff5da5dc);
        defaultColors.put(key_chat_searchPanelText, 0xff4e9ad4);
        defaultColors.put(key_chat_secretChatStatusText, 0xff7f7f7f);
        defaultColors.put(key_chat_fieldOverlayText, 0xff3a8ccf);
        defaultColors.put(key_chat_stickersHintPanel, 0xffffffff);
        defaultColors.put(key_chat_replyPanelIcons, 0xff57a8e6);
        defaultColors.put(key_chat_replyPanelClose, 0xffa8a8a8);
        defaultColors.put(key_chat_replyPanelName, 0xff3a8ccf);
        defaultColors.put(key_chat_replyPanelMessage, 0xff222222);
        defaultColors.put(key_chat_replyPanelLine, 0xffe8e8e8);
        defaultColors.put(key_chat_messagePanelBackground, 0xffffffff);
        defaultColors.put(key_chat_messagePanelText, 0xff000000);
        defaultColors.put(key_chat_messagePanelHint, 0xffb2b2b2);
        defaultColors.put(key_chat_messagePanelShadow, 0xff000000);
        defaultColors.put(key_chat_messagePanelIcons, 0xffa8a8a8);
        defaultColors.put(key_chat_recordedVoicePlayPause, 0xffffffff);
        defaultColors.put(key_chat_recordedVoicePlayPausePressed, 0xffd9eafb);
        defaultColors.put(key_chat_recordedVoiceDot, 0xffda564d);
        defaultColors.put(key_chat_recordedVoiceBackground, 0xff559ee3);
        defaultColors.put(key_chat_recordedVoiceProgress, 0xffa2cef8);
        defaultColors.put(key_chat_recordedVoiceProgressInner, 0xffffffff);
        defaultColors.put(key_chat_recordVoiceCancel, 0xff999999);
        defaultColors.put(key_chat_messagePanelSend, 0xff62b0eb);
        defaultColors.put(key_chat_messagePanelVoiceLock, 0xffa4a4a4);
        defaultColors.put(key_chat_messagePanelVoiceLockBackground, 0xffffffff);
        defaultColors.put(key_chat_messagePanelVoiceLockShadow, 0xff000000);
        defaultColors.put(key_chat_recordTime, 0xff4d4c4b);
        defaultColors.put(key_chat_emojiPanelNewTrending, 0xff4da6ea);
        defaultColors.put(key_chat_gifSaveHintText, 0xffffffff);
        defaultColors.put(key_chat_gifSaveHintBackground, 0xcc111111);
        defaultColors.put(key_chat_goDownButton, 0xffffffff);
        defaultColors.put(key_chat_goDownButtonShadow, 0xff000000);
        defaultColors.put(key_chat_goDownButtonIcon, 0xffa8a8a8);
        defaultColors.put(key_chat_goDownButtonCounter, 0xffffffff);
        defaultColors.put(key_chat_goDownButtonCounterBackground, 0xff4da2e8);
        defaultColors.put(key_chat_messagePanelCancelInlineBot, 0xffadadad);
        defaultColors.put(key_chat_messagePanelVoicePressed, 0xffffffff);
        defaultColors.put(key_chat_messagePanelVoiceBackground, 0xff5795cc);
        defaultColors.put(key_chat_messagePanelVoiceShadow, 0x0d000000);
        defaultColors.put(key_chat_messagePanelVoiceDelete, 0xff737373);
        defaultColors.put(key_chat_messagePanelVoiceDuration, 0xffffffff);
        defaultColors.put(key_chat_inlineResultIcon, 0xff5795cc);
        defaultColors.put(key_chat_topPanelBackground, 0xffffffff);
        defaultColors.put(key_chat_topPanelClose, 0xffa8a8a8);
        defaultColors.put(key_chat_topPanelLine, 0xff6c9fd2);
        defaultColors.put(key_chat_topPanelTitle, 0xff3a8ccf);
        defaultColors.put(key_chat_topPanelMessage, 0xff999999);
        defaultColors.put(key_chat_reportSpam, 0xffcf5957);
        defaultColors.put(key_chat_addContact, 0xff4a82b5);
        defaultColors.put(key_chat_inLoader, 0xff72b5e8);
        defaultColors.put(key_chat_inLoaderSelected, 0xff65abe0);
        defaultColors.put(key_chat_outLoader, 0xff78c272);
        defaultColors.put(key_chat_outLoaderSelected, 0xff6ab564);
        defaultColors.put(key_chat_inLoaderPhoto, 0xffa2b8c8);
        defaultColors.put(key_chat_inLoaderPhotoSelected, 0xffa2b5c7);
        defaultColors.put(key_chat_inLoaderPhotoIcon, 0xfffcfcfc);
        defaultColors.put(key_chat_inLoaderPhotoIconSelected, 0xffebf0f5);
        defaultColors.put(key_chat_outLoaderPhoto, 0xff85bf78);
        defaultColors.put(key_chat_outLoaderPhotoSelected, 0xff7db870);
        defaultColors.put(key_chat_outLoaderPhotoIcon, 0xffdaf5c3);
        defaultColors.put(key_chat_outLoaderPhotoIconSelected, 0xffc0e8a4);
        defaultColors.put(key_chat_mediaLoaderPhoto, 0x66000000);
        defaultColors.put(key_chat_mediaLoaderPhotoSelected, 0x7f000000);
        defaultColors.put(key_chat_mediaLoaderPhotoIcon, 0xffffffff);
        defaultColors.put(key_chat_mediaLoaderPhotoIconSelected, 0xffd9d9d9);
        defaultColors.put(key_chat_secretTimerBackground, 0xcc3e648e);
        defaultColors.put(key_chat_secretTimerText, 0xffffffff);

        defaultColors.put(key_profile_creatorIcon, 0xff4a97d6);
        defaultColors.put(key_profile_adminIcon, 0xff858585);
        defaultColors.put(key_profile_actionIcon, 0xff737373);
        defaultColors.put(key_profile_actionBackground, 0xffffffff);
        defaultColors.put(key_profile_actionPressedBackground, 0xfff2f2f2);
        defaultColors.put(key_profile_verifiedBackground, 0xffb2d6f8);
        defaultColors.put(key_profile_verifiedCheck, 0xff4983b8);
        defaultColors.put(key_profile_title, 0xffffffff);

        defaultColors.put(key_player_actionBar, 0xffffffff);
        defaultColors.put(key_player_actionBarSelector, 0x2f000000);
        defaultColors.put(key_player_actionBarTitle, 0xff212121);
        defaultColors.put(key_player_actionBarTop, 0x99000000);
        defaultColors.put(key_player_actionBarSubtitle, 0xff8a8a8a);
        defaultColors.put(key_player_actionBarItems, 0xff8a8a8a);
        defaultColors.put(key_player_seekBarBackground, 0xe5ffffff);
        defaultColors.put(key_player_time, 0xff19a7e8);
        defaultColors.put(key_player_duration, 0xff8a8a8a);
        defaultColors.put(key_player_progressBackground, 0x19000000);
        defaultColors.put(key_player_progress, 0xff23afef);
        defaultColors.put(key_player_placeholder, 0xffd9d9d9);
        defaultColors.put(key_player_button, 0xff8a8a8a);
        defaultColors.put(key_player_buttonActive, 0xff23afef);

        defaultColors.put(key_files_folderIcon, 0xff999999);
        defaultColors.put(key_files_folderIconBackground, 0xfff0f0f0);
        defaultColors.put(key_files_iconText, 0xffffffff);

        defaultColors.put(key_sessions_devicesImage, 0xff969696);

        defaultColors.put(key_location_markerX, 0xff808080);
        defaultColors.put(key_location_sendLocationBackground, 0xff6da0d4);
        defaultColors.put(key_location_sendLocationIcon, 0xffffffff);

        defaultColors.put(key_calls_callReceivedGreenIcon, 0xff00c853);
        defaultColors.put(key_calls_callReceivedRedIcon, 0xffff4848);

        defaultColors.put(key_featuredStickers_addedIcon, 0xff50a8eb);
        defaultColors.put(key_featuredStickers_buttonProgress, 0xffffffff);
        defaultColors.put(key_featuredStickers_addButton, 0xff50a8eb);
        defaultColors.put(key_featuredStickers_addButtonPressed, 0xff439bde);
        defaultColors.put(key_featuredStickers_delButton, 0xffd95757);
        defaultColors.put(key_featuredStickers_delButtonPressed, 0xffc64949);
        defaultColors.put(key_featuredStickers_buttonText, 0xffffffff);
        defaultColors.put(key_featuredStickers_unread, 0xff4da6ea);

        defaultColors.put(key_inappPlayerPerformer, 0xff2f3438);
        defaultColors.put(key_inappPlayerTitle, 0xff2f3438);
        defaultColors.put(key_inappPlayerBackground, 0xffffffff);
        defaultColors.put(key_inappPlayerPlayPause, 0xff62b0eb);
        defaultColors.put(key_inappPlayerClose, 0xffa8a8a8);

        defaultColors.put(key_returnToCallBackground, 0xff44a1e3);
        defaultColors.put(key_returnToCallText, 0xffffffff);

        defaultColors.put(key_sharedMedia_startStopLoadIcon, 0xff36a2ee);
        defaultColors.put(key_sharedMedia_linkPlaceholder, 0xfff0f0f0);
        defaultColors.put(key_sharedMedia_linkPlaceholderText, 0xffffffff);
        defaultColors.put(key_checkbox, 0xff5ec245);
        defaultColors.put(key_checkboxCheck, 0xffffffff);

        defaultColors.put(key_stickers_menu, 0xffb6bdc5);
        defaultColors.put(key_stickers_menuSelector, 0x2f000000);

        defaultColors.put(key_changephoneinfo_image, 0xffa8a8a8);

        defaultColors.put(key_groupcreate_hintText, 0xffa1aab3);
        defaultColors.put(key_groupcreate_cursor, 0xff52a3db);
        defaultColors.put(key_groupcreate_sectionShadow, 0xff000000);
        defaultColors.put(key_groupcreate_sectionText, 0xff7c8288);
        defaultColors.put(key_groupcreate_onlineText, 0xff4092cd);
        defaultColors.put(key_groupcreate_offlineText, 0xff838c96);
        defaultColors.put(key_groupcreate_checkbox, 0xff5ec245);
        defaultColors.put(key_groupcreate_checkboxCheck, 0xffffffff);
        defaultColors.put(key_groupcreate_spanText, 0xff212121);
        defaultColors.put(key_groupcreate_spanBackground, 0xfff2f2f2);

        defaultColors.put(key_login_progressInner, 0xffe1eaf2);
        defaultColors.put(key_login_progressOuter, 0xff62a0d0);

        defaultColors.put(key_musicPicker_checkbox, 0xff29b6f7);
        defaultColors.put(key_musicPicker_checkboxCheck, 0xffffffff);
        defaultColors.put(key_musicPicker_buttonBackground, 0xff5cafea);
        defaultColors.put(key_musicPicker_buttonIcon, 0xffffffff);
        defaultColors.put(key_picker_enabledButton, 0xff19a7e8);
        defaultColors.put(key_picker_disabledButton, 0xff999999);
        defaultColors.put(key_picker_badge, 0xff29b6f7);
        defaultColors.put(key_picker_badgeText, 0xffffffff);

        defaultColors.put(key_chat_botSwitchToInlineText, 0xff4391cc);

        defaultColors.put(key_calls_ratingStar, 0x80000000);
        defaultColors.put(key_calls_ratingStarSelected, 0xFF4a97d6);

        themes = new ArrayList<>();
        otherThemes = new ArrayList<>();
        themesDict = new HashMap<>();
        currentColors = new HashMap<>();

        ThemeInfo themeInfo = new ThemeInfo();
        themeInfo.name = "Default";
        themes.add(currentTheme = defaultTheme = themeInfo);
        themesDict.put("Default", defaultTheme);

        themeInfo = new ThemeInfo();
        themeInfo.name = "Dark";
        themeInfo.assetName = "dark.attheme";
        themes.add(themeInfo);
        themesDict.put("Dark", themeInfo);

        themeInfo = new ThemeInfo();
        themeInfo.name = "Blue";
        themeInfo.assetName = "bluebubbles.attheme";
        themes.add(themeInfo);
        themesDict.put("Blue", themeInfo);

        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("themeconfig", Activity.MODE_PRIVATE);
        String themesString = preferences.getString("themes2", null);
        if (!TextUtils.isEmpty(themesString)) {
            try {
                JSONArray jsonArray = new JSONArray(themesString);
                for (int a = 0; a < jsonArray.length(); a++) {
                    themeInfo = ThemeInfo.createWithJson(jsonArray.getJSONObject(a));
                    if (themeInfo != null) {
                        otherThemes.add(themeInfo);
                        themes.add(themeInfo);
                        themesDict.put(themeInfo.name, themeInfo);
                    }
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        } else {
            themesString = preferences.getString("themes", null);
            if (!TextUtils.isEmpty(themesString)) {
                String[] themesArr = themesString.split("&");
                for (int a = 0; a < themesArr.length; a++) {
                    themeInfo = ThemeInfo.createWithString(themesArr[a]);
                    if (themeInfo != null) {
                        otherThemes.add(themeInfo);
                        themes.add(themeInfo);
                        themesDict.put(themeInfo.name, themeInfo);
                    }
                }
            }
            saveOtherThemes();
            preferences.edit().remove("themes").commit();
        }

        sortThemes();

        ThemeInfo applyingTheme = null;
        try {
            preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
            String theme = preferences.getString("theme", null);
            if (theme != null) {
                applyingTheme = themesDict.get(theme);
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        if (applyingTheme == null) {
            applyingTheme = defaultTheme;
        }
        applyTheme(applyingTheme, false, false);
    }

    private static Method StateListDrawable_getStateDrawableMethod;
    private static Field BitmapDrawable_mColorFilter;

    private static Drawable getStateDrawable(Drawable drawable, int index) {
        if (StateListDrawable_getStateDrawableMethod == null) {
            try {
                StateListDrawable_getStateDrawableMethod = StateListDrawable.class.getDeclaredMethod("getStateDrawable", int.class);
            } catch (Throwable ignore) {

            }
        }
        if (StateListDrawable_getStateDrawableMethod == null) {
            return null;
        }
        try {
            return (Drawable) StateListDrawable_getStateDrawableMethod.invoke(drawable, index);
        } catch (Exception ignore) {

        }
        return null;
    }

    public static Drawable createEmojiIconSelectorDrawable(Context context, int resource, int defaultColor, int pressedColor) {
        Resources resources = context.getResources();
        Drawable defaultDrawable = resources.getDrawable(resource).mutate();
        if (defaultColor != 0) {
            defaultDrawable.setColorFilter(new PorterDuffColorFilter(defaultColor, PorterDuff.Mode.MULTIPLY));
        }
        Drawable pressedDrawable = resources.getDrawable(resource).mutate();
        if (pressedColor != 0) {
            pressedDrawable.setColorFilter(new PorterDuffColorFilter(pressedColor, PorterDuff.Mode.MULTIPLY));
        }
        StateListDrawable stateListDrawable = new StateListDrawable() {
            @Override
            public boolean selectDrawable(int index) {
                if (Build.VERSION.SDK_INT < 21) {
                    Drawable drawable = getStateDrawable(this, index);
                    ColorFilter colorFilter = null;
                    if (drawable instanceof BitmapDrawable) {
                        colorFilter = ((BitmapDrawable) drawable).getPaint().getColorFilter();
                    } else if (drawable instanceof NinePatchDrawable) {
                        colorFilter = ((NinePatchDrawable) drawable).getPaint().getColorFilter();
                    }
                    boolean result = super.selectDrawable(index);
                    if (colorFilter != null) {
                        drawable.setColorFilter(colorFilter);
                    }
                    return result;
                }
                return super.selectDrawable(index);
            }
        };
        stateListDrawable.setEnterFadeDuration(1);
        stateListDrawable.setExitFadeDuration(200);
        stateListDrawable.addState(new int[]{android.R.attr.state_selected}, pressedDrawable);
        stateListDrawable.addState(new int[]{}, defaultDrawable);
        return stateListDrawable;
    }

    public static Drawable createEditTextDrawable(Context context, boolean alert) {
        Resources resources = context.getResources();
        Drawable defaultDrawable = resources.getDrawable(R.drawable.search_dark).mutate();
        defaultDrawable.setColorFilter(new PorterDuffColorFilter(getColor(alert ? Theme.key_dialogInputField : Theme.key_windowBackgroundWhiteInputField), PorterDuff.Mode.MULTIPLY));
        Drawable pressedDrawable = resources.getDrawable(R.drawable.search_dark_activated).mutate();
        pressedDrawable.setColorFilter(new PorterDuffColorFilter(getColor(alert ? Theme.key_dialogInputFieldActivated : Theme.key_windowBackgroundWhiteInputFieldActivated), PorterDuff.Mode.MULTIPLY));
        StateListDrawable stateListDrawable = new StateListDrawable() {
            @Override
            public boolean selectDrawable(int index) {
                if (Build.VERSION.SDK_INT < 21) {
                    Drawable drawable = getStateDrawable(this, index);
                    ColorFilter colorFilter = null;
                    if (drawable instanceof BitmapDrawable) {
                        colorFilter = ((BitmapDrawable) drawable).getPaint().getColorFilter();
                    } else if (drawable instanceof NinePatchDrawable) {
                        colorFilter = ((NinePatchDrawable) drawable).getPaint().getColorFilter();
                    }
                    boolean result = super.selectDrawable(index);
                    if (colorFilter != null) {
                        drawable.setColorFilter(colorFilter);
                    }
                    return result;
                }
                return super.selectDrawable(index);
            }
        };
        stateListDrawable.addState(new int[]{android.R.attr.state_enabled, android.R.attr.state_focused}, pressedDrawable);
        stateListDrawable.addState(new int[]{android.R.attr.state_focused}, pressedDrawable);
        stateListDrawable.addState(StateSet.WILD_CARD, defaultDrawable);
        return stateListDrawable;
    }

    public static Drawable createSimpleSelectorDrawable(Context context, int resource, int defaultColor, int pressedColor) {
        Resources resources = context.getResources();
        Drawable defaultDrawable = resources.getDrawable(resource).mutate();
        if (defaultColor != 0) {
            defaultDrawable.setColorFilter(new PorterDuffColorFilter(defaultColor, PorterDuff.Mode.MULTIPLY));
        }
        Drawable pressedDrawable = resources.getDrawable(resource).mutate();
        if (pressedColor != 0) {
            pressedDrawable.setColorFilter(new PorterDuffColorFilter(pressedColor, PorterDuff.Mode.MULTIPLY));
        }
        StateListDrawable stateListDrawable = new StateListDrawable() {
            @Override
            public boolean selectDrawable(int index) {
                if (Build.VERSION.SDK_INT < 21) {
                    Drawable drawable = getStateDrawable(this, index);
                    ColorFilter colorFilter = null;
                    if (drawable instanceof BitmapDrawable) {
                        colorFilter = ((BitmapDrawable) drawable).getPaint().getColorFilter();
                    } else if (drawable instanceof NinePatchDrawable) {
                        colorFilter = ((NinePatchDrawable) drawable).getPaint().getColorFilter();
                    }
                    boolean result = super.selectDrawable(index);
                    if (colorFilter != null) {
                        drawable.setColorFilter(colorFilter);
                    }
                    return result;
                }
                return super.selectDrawable(index);
            }
        };
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
        stateListDrawable.addState(new int[]{android.R.attr.state_selected}, pressedDrawable);
        stateListDrawable.addState(StateSet.WILD_CARD, defaultDrawable);
        return stateListDrawable;
    }

    public static Drawable createCircleDrawable(int size, int color) {
        OvalShape ovalShape = new OvalShape();
        ovalShape.resize(size, size);
        ShapeDrawable defaultDrawable = new ShapeDrawable(ovalShape);
        defaultDrawable.getPaint().setColor(color);
        return defaultDrawable;
    }

    public static Drawable createCircleDrawableWithIcon(int size, int iconRes) {
        return createCircleDrawableWithIcon(size, iconRes, 0);
    }

    public static Drawable createCircleDrawableWithIcon(int size, int iconRes, int stroke) {
        OvalShape ovalShape = new OvalShape();
        ovalShape.resize(size, size);
        ShapeDrawable defaultDrawable = new ShapeDrawable(ovalShape);
        Paint paint = defaultDrawable.getPaint();
        paint.setColor(0xffffffff);
        if (stroke == 1) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(AndroidUtilities.dp(2));
        } else if (stroke == 2) {
            paint.setAlpha(0);
        }
        Drawable drawable = ApplicationLoader.applicationContext.getResources().getDrawable(iconRes).mutate();
        CombinedDrawable combinedDrawable = new CombinedDrawable(defaultDrawable, drawable);
        combinedDrawable.setCustomSize(size, size);
        return combinedDrawable;
    }

    public static Drawable createRoundRectDrawableWithIcon(int rad, int iconRes) {
        ShapeDrawable defaultDrawable = new ShapeDrawable(new RoundRectShape(new float[]{rad, rad, rad, rad, rad, rad, rad, rad}, null, null));
        defaultDrawable.getPaint().setColor(0xffffffff);
        Drawable drawable = ApplicationLoader.applicationContext.getResources().getDrawable(iconRes).mutate();
        return new CombinedDrawable(defaultDrawable, drawable);
    }

    public static void setCombinedDrawableColor(Drawable combinedDrawable, int color, boolean isIcon) {
        if (!(combinedDrawable instanceof CombinedDrawable)) {
            return;
        }
        Drawable drawable;
        if (isIcon) {
            drawable = ((CombinedDrawable) combinedDrawable).getIcon();
        } else {
            drawable = ((CombinedDrawable) combinedDrawable).getBackground();
        }
        drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
    }

    public static Drawable createSimpleSelectorCircleDrawable(int size, int defaultColor, int pressedColor) {
        OvalShape ovalShape = new OvalShape();
        ovalShape.resize(size, size);
        ShapeDrawable defaultDrawable = new ShapeDrawable(ovalShape);
        defaultDrawable.getPaint().setColor(defaultColor);
        ShapeDrawable pressedDrawable = new ShapeDrawable(ovalShape);
        if (Build.VERSION.SDK_INT >= 21) {
            pressedDrawable.getPaint().setColor(0xffffffff);
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{StateSet.WILD_CARD},
                    new int[]{pressedColor}
            );
            return new RippleDrawable(colorStateList, defaultDrawable, pressedDrawable);
        } else {
            pressedDrawable.getPaint().setColor(pressedColor);
            StateListDrawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
            stateListDrawable.addState(new int[]{android.R.attr.state_focused}, pressedDrawable);
            stateListDrawable.addState(StateSet.WILD_CARD, defaultDrawable);
            return stateListDrawable;
        }
    }

    public static Drawable createRoundRectDrawable(int rad, int defaultColor) {
        ShapeDrawable defaultDrawable = new ShapeDrawable(new RoundRectShape(new float[]{rad, rad, rad, rad, rad, rad, rad, rad}, null, null));
        defaultDrawable.getPaint().setColor(defaultColor);
        return defaultDrawable;
    }

    public static Drawable createSimpleSelectorRoundRectDrawable(int rad, int defaultColor, int pressedColor) {
        ShapeDrawable defaultDrawable = new ShapeDrawable(new RoundRectShape(new float[]{rad, rad, rad, rad, rad, rad, rad, rad}, null, null));
        defaultDrawable.getPaint().setColor(defaultColor);
        ShapeDrawable pressedDrawable = new ShapeDrawable(new RoundRectShape(new float[]{rad, rad, rad, rad, rad, rad, rad, rad}, null, null));
        pressedDrawable.getPaint().setColor(pressedColor);
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);
        stateListDrawable.addState(new int[]{android.R.attr.state_selected}, pressedDrawable);
        stateListDrawable.addState(StateSet.WILD_CARD, defaultDrawable);
        return stateListDrawable;
    }

    public static Drawable getRoundRectSelectorDrawable() {
        if (Build.VERSION.SDK_INT >= 21) {
            Drawable maskDrawable = createRoundRectDrawable(AndroidUtilities.dp(3), 0xffffffff);
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{StateSet.WILD_CARD},
                    new int[]{getColor(key_dialogButtonSelector)}
            );
            return new RippleDrawable(colorStateList, null, maskDrawable);
        } else {
            StateListDrawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, createRoundRectDrawable(AndroidUtilities.dp(3), getColor(key_dialogButtonSelector)));
            stateListDrawable.addState(new int[]{android.R.attr.state_selected}, createRoundRectDrawable(AndroidUtilities.dp(3), getColor(key_dialogButtonSelector)));
            stateListDrawable.addState(StateSet.WILD_CARD, new ColorDrawable(0x00000000));
            return stateListDrawable;
        }
    }

    public static Drawable getSelectorDrawable(boolean whiteBackground) {
        if (whiteBackground) {
            if (Build.VERSION.SDK_INT >= 21) {
                Drawable maskDrawable = new ColorDrawable(0xffffffff);
                ColorStateList colorStateList = new ColorStateList(
                        new int[][]{StateSet.WILD_CARD},
                        new int[]{getColor(key_listSelector)}
                );
                return new RippleDrawable(colorStateList, new ColorDrawable(getColor(key_windowBackgroundWhite)), maskDrawable);
            } else {
                int color = getColor(key_listSelector);
                StateListDrawable stateListDrawable = new StateListDrawable();
                stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(color));
                stateListDrawable.addState(new int[]{android.R.attr.state_selected}, new ColorDrawable(color));
                stateListDrawable.addState(StateSet.WILD_CARD, new ColorDrawable(getColor(key_windowBackgroundWhite)));
                return stateListDrawable;
            }
        } else {
            return createSelectorDrawable(getColor(key_listSelector), 2);
        }
    }

    public static Drawable createSelectorDrawable(int color) {
        return createSelectorDrawable(color, 1);
    }

    public static Drawable createSelectorDrawable(int color, int maskType) {
        Drawable drawable;
        if (Build.VERSION.SDK_INT >= 21) {
            Drawable maskDrawable = null;
            if (maskType == 1) {
                maskPaint.setColor(0xffffffff);
                maskDrawable = new Drawable() {
                    @Override
                    public void draw(Canvas canvas) {
                        android.graphics.Rect bounds = getBounds();
                        canvas.drawCircle(bounds.centerX(), bounds.centerY(), AndroidUtilities.dp(18), maskPaint);
                    }

                    @Override
                    public void setAlpha(int alpha) {

                    }

                    @Override
                    public void setColorFilter(ColorFilter colorFilter) {

                    }

                    @Override
                    public int getOpacity() {
                        return 0;
                    }
                };
            } else if (maskType == 2) {
                maskDrawable = new ColorDrawable(0xffffffff);
            }
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{StateSet.WILD_CARD},
                    new int[]{color}
            );
            return new RippleDrawable(colorStateList, null, maskDrawable);
        } else {
            StateListDrawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(color));
            stateListDrawable.addState(new int[]{android.R.attr.state_selected}, new ColorDrawable(color));
            stateListDrawable.addState(StateSet.WILD_CARD, new ColorDrawable(0x00000000));
            return stateListDrawable;
        }
    }

    public static void applyPreviousTheme() {
        if (previousTheme == null) {
            return;
        }
        applyTheme(previousTheme, true, false);
        previousTheme = null;
    }

    private static void sortThemes() {
        Collections.sort(themes, new Comparator<ThemeInfo>() {
            @Override
            public int compare(ThemeInfo o1, ThemeInfo o2) {
                if (o1.pathToFile == null && o1.assetName == null) {
                    return -1;
                } else if (o2.pathToFile == null && o2.assetName == null) {
                    return 1;
                }
                return o1.name.compareTo(o2.name);
            }
        });
    }

    public static ThemeInfo applyThemeFile(File file, String themeName, boolean temporary) {
        try {
            if (themeName.equals("Default") || themeName.equals("Dark") || themeName.equals("Blue")) {
                return null;
            }
            File finalFile = new File(ApplicationLoader.getFilesDirFixed(), themeName);
            if (!AndroidUtilities.copyFile(file, finalFile)) {
                return null;
            }

            boolean newTheme = false;
            ThemeInfo themeInfo = themesDict.get(themeName);
            if (themeInfo == null) {
                newTheme = true;
                themeInfo = new ThemeInfo();
                themeInfo.name = themeName;
                themeInfo.pathToFile = finalFile.getAbsolutePath();
            }
            if (!temporary) {
                if (newTheme) {
                    themes.add(themeInfo);
                    themesDict.put(themeInfo.name, themeInfo);
                    otherThemes.add(themeInfo);
                    sortThemes();
                    saveOtherThemes();
                }
            } else {
                previousTheme = currentTheme;
            }

            applyTheme(themeInfo, !temporary, true);
            return themeInfo;
        } catch (Exception e) {
            FileLog.e(e);
        }
        return null;
    }

    public static void setUsePlusThemeKey(boolean value){
        usePlusTheme = value;
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        SharedPreferences.Editor editor = themePrefs.edit();
        editor.putBoolean("usePlusTheme", value);
        editor.apply();
    }

    public static void applyTheme(ThemeInfo themeInfo) {
        //
        setUsePlusThemeKey(false);
        //
        applyTheme(themeInfo, true, true);
    }

    public static void applyTheme(ThemeInfo themeInfo, boolean save, boolean removeWallpaperOverride) {
        if (themeInfo == null) {
            return;
        }
        ThemeEditorView editorView = ThemeEditorView.getInstance();
        if (editorView != null) {
            editorView.destroy();
        }
        try {
            if (themeInfo.pathToFile != null || themeInfo.assetName != null) {
                if (save) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("theme", themeInfo.name);
                    if (removeWallpaperOverride) {
                        editor.remove("overrideThemeWallpaper");
                    }
                    editor.commit();
                }
                if (themeInfo.assetName != null) {
                    currentColors = getThemeFileValues(null, themeInfo.assetName);
                } else {
                    currentColors = getThemeFileValues(new File(themeInfo.pathToFile), null);
                }
            } else {
                if (save) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.remove("theme");
                    if (removeWallpaperOverride) {
                        editor.remove("overrideThemeWallpaper");
                    }
                    editor.commit();
                }
                currentColors.clear();
                wallpaper = null;
                themedWallpaper = null;
            }
            //plus
            getPlusThemeFileValues();
            //
            currentTheme = themeInfo;
            reloadWallpaper();
            applyCommonTheme();
            applyDialogsTheme();
            applyProfileTheme();
            applyChatTheme(false);
        } catch (Exception e) {
            FileLog.e(e);
        }
    }
    //plus
    public static boolean needRebuild;

    public static void applyPlusTheme(boolean plus) {
        try {
            //Log.e("Theme", "applyPlusTheme");
            getPlusThemeFileValues();
            reloadWallpaper();
            applyCommonTheme();
            applyDialogsTheme();
            applyProfileTheme();
            applyChatTheme(false);
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public static void applyPlusTheme() {
        try {
            //Log.e("Theme", "applyPlusTheme");
            if(!Theme.usePlusTheme){
                Theme.usePlusTheme = true;
                SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
                SharedPreferences.Editor editor = themePrefs.edit();
                editor.putBoolean("usePlusTheme", true);
                editor.commit();
            }
            getPlusThemeFileValues();
            reloadWallpaper();
            applyCommonTheme();
            applyDialogsTheme();
            applyProfileTheme();
            applyChatTheme(false);
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    private static void getPlusThemeFileValues() {
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        usePlusTheme = themePrefs.getBoolean("usePlusTheme", true);
        SharedPreferences prefs = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        String theme = prefs.getString("prevTheme", null);
        if (usePlusTheme && theme == null) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("prevTheme", Theme.getCurrentTheme().name);
            edit.commit();
            applyTheme(Theme.getDefaultTheme());
        }
        currentPlusTheme = themePrefs.getAll();
        //Log.d("Theme", "getPlusThemeFileValues " + currentPlusTheme.size());
        try {
            for(Map.Entry<String,?> entry : currentPlusTheme.entrySet()){
                //Log.d("Theme", "map values: " + entry.getKey() + ": " + entry.getValue().toString() + " " + (entry.getValue() instanceof Integer ? "INTEGER" : entry.getValue() instanceof Boolean ? "BOOLEAN" : "STRING"));
                if(entry.getValue() instanceof Integer){
                    currentPlusColors.put(entry.getKey(), ((Integer) entry.getValue()).intValue());
                }
            }
        } catch (Throwable e) {
            FileLog.e(e);
        }
    }
    //

    private static void saveOtherThemes() {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("themeconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        JSONArray array = new JSONArray();
        for (int a = 0; a < otherThemes.size(); a++) {
            JSONObject jsonObject = otherThemes.get(a).getSaveJson();
            if (jsonObject != null) {
                array.put(jsonObject);
            }
        }
        editor.putString("themes2", array.toString());
        editor.commit();
    }

    public static HashMap<String, Integer> getDefaultColors() {
        return defaultColors;
    }

    public static String getCurrentThemeName() {
        String text = currentTheme.getName();
        if (text.endsWith(".attheme")) {
            text = text.substring(0, text.lastIndexOf('.'));
        }
        return text;
    }

    public static ThemeInfo getCurrentTheme() {
        return currentTheme != null ? currentTheme : defaultTheme;
    }

    public static ThemeInfo getDefaultTheme() {
        return defaultTheme;
    }

    public static HashMap<String, ThemeInfo> getThemeList() {
        return themesDict;
    }

    public static boolean deleteTheme(ThemeInfo themeInfo) {
        if (themeInfo.pathToFile == null) {
            return false;
        }
        boolean currentThemeDeleted = false;
        if (currentTheme == themeInfo) {
            applyTheme(defaultTheme, true, false);
            currentThemeDeleted = true;
        }

        otherThemes.remove(themeInfo);
        themesDict.remove(themeInfo.name);
        themes.remove(themeInfo);
        File file = new File(themeInfo.pathToFile);
        file.delete();
        saveOtherThemes();
        return currentThemeDeleted;
    }

    public static void saveCurrentTheme(String name, boolean finalSave) {
        StringBuilder result = new StringBuilder();
        for (HashMap.Entry<String, Integer> entry : currentColors.entrySet()) {
            result.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        File file = new File(ApplicationLoader.getFilesDirFixed(), name);
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            stream.write(result.toString().getBytes());
            if (themedWallpaper instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) themedWallpaper).getBitmap();
                if (bitmap != null) {
                    stream.write(new byte[]{'W', 'P', 'S', '\n'});
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 87, stream);
                    stream.write(new byte[]{'\n', 'W', 'P', 'E', '\n'});
                }
                if (finalSave) {
                    wallpaper = themedWallpaper;
                    calcBackgroundColor(wallpaper, 2);
                }
            }
            ThemeInfo newTheme;
            if ((newTheme = themesDict.get(name)) == null) {
                newTheme = new ThemeInfo();
                newTheme.pathToFile = file.getAbsolutePath();
                newTheme.name = name;
                themes.add(newTheme);
                themesDict.put(newTheme.name, newTheme);
                otherThemes.add(newTheme);
                saveOtherThemes();
                sortThemes();
            }
            currentTheme = newTheme;
            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("theme", currentTheme.name);
            editor.commit();
        } catch (Exception e) {
            FileLog.e(e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
                FileLog.e("tmessage", e);
            }
        }
    }

    public static File getAssetFile(String assetName) {
        File file = new File(ApplicationLoader.getFilesDirFixed(), assetName);
        if (!file.exists()) {
            InputStream in = null;
            try {
                in = ApplicationLoader.applicationContext.getAssets().open(assetName);
                AndroidUtilities.copyFile(in, file);
            } catch (Exception e) {
                FileLog.e(e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception ignore) {

                    }
                }
            }
        }
        return file;
    }

    private static HashMap<String, Integer> getThemeFileValues(File file, String assetName) {
        FileInputStream stream = null;
        HashMap<String, Integer> stringMap = new HashMap<>();
        try {
            byte[] bytes = new byte[1024];
            int currentPosition = 0;
            if (assetName != null) {
                file = getAssetFile(assetName);
            }
            stream = new FileInputStream(file);
            int idx;
            int read;
            boolean finished = false;
            themedWallpaperFileOffset = -1;
            while ((read = stream.read(bytes)) != -1) {
                int previousPosition = currentPosition;
                int start = 0;
                for (int a = 0; a < read; a++) {
                    if (bytes[a] == '\n') {
                        int len = a - start + 1;
                        String line = new String(bytes, start, len - 1, "UTF-8");
                        if (line.startsWith("WPS")) {
                            themedWallpaperFileOffset = currentPosition + len;
                            finished = true;
                            break;
                        } else {
                            if ((idx = line.indexOf('=')) != -1) {
                                String key = line.substring(0, idx);
                                String param = line.substring(idx + 1);
                                int value;
                                if (param.length() > 0 && param.charAt(0) == '#') {
                                    try {
                                        value = Color.parseColor(param);
                                    } catch (Exception ignore) {
                                        value = Utilities.parseInt(param);
                                    }
                                } else {
                                    value = Utilities.parseInt(param);
                                }
                                stringMap.put(key, value);
                            }
                        }
                        start += len;
                        currentPosition += len;
                    }
                }
                if (previousPosition == currentPosition) {
                    break;
                }
                stream.getChannel().position(currentPosition);
                if (finished) {
                    break;
                }
            }
        } catch (Throwable e) {
            FileLog.e(e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
        return stringMap;
    }

    public static void createCommonResources(Context context) {
        if (dividerPaint == null) {
            dividerPaint = new Paint();
            dividerPaint.setStrokeWidth(1);

            avatar_backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

            checkboxSquare_checkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            checkboxSquare_checkPaint.setStyle(Paint.Style.STROKE);
            checkboxSquare_checkPaint.setStrokeWidth(AndroidUtilities.dp(2));
            checkboxSquare_eraserPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            checkboxSquare_eraserPaint.setColor(0);
            checkboxSquare_eraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            checkboxSquare_backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

            linkSelectionPaint = new Paint();

            Resources resources = context.getResources();

            avatar_broadcastDrawable = resources.getDrawable(R.drawable.broadcast_w);
            avatar_photoDrawable = resources.getDrawable(R.drawable.photo_w);

            applyCommonTheme();
        }
    }

    public static void applyCommonTheme() {
        if (dividerPaint == null) {
            return;
        }
        dividerPaint.setColor(getColor(key_divider));
        linkSelectionPaint.setColor(getColor(key_windowBackgroundWhiteLinkSelection));

        setDrawableColorByKey(avatar_broadcastDrawable, key_avatar_text);
        setDrawableColorByKey(avatar_photoDrawable, key_avatar_text);
    }

    public static void createDialogsResources(Context context) {
        createCommonResources(context);
        if (dialogs_namePaint == null) {
            Resources resources = context.getResources();

            dialogs_namePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            dialogs_namePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            dialogs_nameEncryptedPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            dialogs_nameEncryptedPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            dialogs_messagePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            dialogs_messagePrintingPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            dialogs_timePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            dialogs_countTextPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            dialogs_countTextPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            dialogs_onlinePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            dialogs_offlinePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);

            dialogs_tabletSeletedPaint = new Paint();
            dialogs_pinnedPaint = new Paint();
            dialogs_countPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            dialogs_countGrayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            dialogs_errorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

            dialogs_lockDrawable = resources.getDrawable(R.drawable.list_secret);
            dialogs_checkDrawable = resources.getDrawable(R.drawable.list_check);
            dialogs_halfCheckDrawable = resources.getDrawable(R.drawable.list_halfcheck);
            dialogs_clockDrawable = resources.getDrawable(R.drawable.msg_clock).mutate();
            dialogs_errorDrawable = resources.getDrawable(R.drawable.list_warning_sign);
            dialogs_groupDrawable = resources.getDrawable(R.drawable.list_group);
            dialogs_broadcastDrawable = resources.getDrawable(R.drawable.list_broadcast);
            dialogs_muteDrawable = resources.getDrawable(R.drawable.list_mute).mutate();
            dialogs_verifiedDrawable = resources.getDrawable(R.drawable.verified_area);
            dialogs_verifiedCheckDrawable = resources.getDrawable(R.drawable.verified_check);
            dialogs_botDrawable = resources.getDrawable(R.drawable.list_bot);
            dialogs_pinnedDrawable = resources.getDrawable(R.drawable.list_pin);

            //plus
            dialogs_superGroupDrawable = resources.getDrawable(R.drawable.list_supergroup);
            dialogs_FavDrawable = resources.getDrawable(R.drawable.admin_star);
            //dialogs_StatusBGDrawable = new GradientDrawable();
            //dialogs_StatusBGDrawable.setCornerRadius(AndroidUtilities.dp(16));
            //dialogs_StatusBGDrawable.setStroke(AndroidUtilities.dp(2), Color.WHITE);

            dialogs_groupPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            dialogs_groupPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));

            dialogs_nameUnknownPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            dialogs_nameUnknownPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));

            dialogs_messageTypingPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);

            dialogs_mediaPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);

            //if(checkVal != Theme.chatCheckStyleVal){
                setDialogsChecks(resources);
            //}
            //
            applyDialogsTheme();
        }

        dialogs_namePaint.setTextSize(AndroidUtilities.dp(17));
        dialogs_nameEncryptedPaint.setTextSize(AndroidUtilities.dp(17));
        dialogs_messagePaint.setTextSize(AndroidUtilities.dp(16));
        dialogs_messagePrintingPaint.setTextSize(AndroidUtilities.dp(16));
        dialogs_timePaint.setTextSize(AndroidUtilities.dp(13));
        dialogs_countTextPaint.setTextSize(AndroidUtilities.dp(13));
        dialogs_onlinePaint.setTextSize(AndroidUtilities.dp(16));
        dialogs_offlinePaint.setTextSize(AndroidUtilities.dp(16));
        //
        dialogs_namePaint.setTextSize(AndroidUtilities.dp(Theme.chatsNameSize));
        dialogs_nameEncryptedPaint.setTextSize(AndroidUtilities.dp(Theme.chatsNameSize));
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        dialogs_messagePaint.setTextSize(AndroidUtilities.dp(themePrefs.getInt("chatsMessageSize", 16)));
        dialogs_messagePrintingPaint.setTextSize(AndroidUtilities.dp(themePrefs.getInt("chatsMessageSize", 16)));
        dialogs_messageTypingPaint.setTextSize(AndroidUtilities.dp(themePrefs.getInt("chatsMessageSize", 16)));
        dialogs_timePaint.setTextSize(AndroidUtilities.dp(themePrefs.getInt("chatsTimeSize", 13)));
        dialogs_countTextPaint.setTextSize(AndroidUtilities.dp(Theme.chatsCountSize));
        dialogs_groupPaint.setTextSize(AndroidUtilities.dp(themePrefs.getInt("chatsGroupNameSize", Theme.chatsNameSize)));
        dialogs_nameUnknownPaint.setTextSize(AndroidUtilities.dp(Theme.chatsNameSize));

        //dialogs_nameUnknownPaint.setTextSize(AndroidUtilities.dp(17));
        //dialogs_groupPaint.setTextSize(AndroidUtilities.dp(17));
        //dialogs_messageTypingPaint.setTextSize(AndroidUtilities.dp(16));
        dialogs_mediaPaint.setTextSize(AndroidUtilities.dp(16));
        //
    }

    public static void applyDialogsTheme() {
        if (dialogs_namePaint == null) {
            return;
        }
        dialogs_namePaint.setColor(getColor(key_chats_name));
        dialogs_nameEncryptedPaint.setColor(getColor(key_chats_secretName));
        dialogs_messagePaint.setColor(dialogs_messagePaint.linkColor = getColor(key_chats_message));
        dialogs_tabletSeletedPaint.setColor(getColor(key_chats_tabletSelectedOverlay));
        dialogs_pinnedPaint.setColor(getColor(key_chats_pinnedOverlay));
        dialogs_timePaint.setColor(getColor(key_chats_date));
        dialogs_countTextPaint.setColor(getColor(key_chats_unreadCounterText));
        dialogs_messagePrintingPaint.setColor(getColor(key_chats_actionMessage));
        dialogs_countPaint.setColor(getColor(key_chats_unreadCounter));
        dialogs_countGrayPaint.setColor(getColor(key_chats_unreadCounterMuted));
        dialogs_errorPaint.setColor(getColor(key_chats_sentError));
        dialogs_onlinePaint.setColor(getColor(key_windowBackgroundWhiteBlueText3));
        dialogs_offlinePaint.setColor(getColor(key_windowBackgroundWhiteGrayText3));

        setDrawableColorByKey(dialogs_lockDrawable, key_chats_secretIcon);
        setDrawableColorByKey(dialogs_checkDrawable, key_chats_sentCheck);
        setDrawableColorByKey(dialogs_halfCheckDrawable, key_chats_sentCheck);
        setDrawableColorByKey(dialogs_clockDrawable, key_chats_sentClock);
        setDrawableColorByKey(dialogs_errorDrawable, key_chats_sentErrorIcon);
        setDrawableColorByKey(dialogs_groupDrawable, key_chats_nameIcon);
        setDrawableColorByKey(dialogs_broadcastDrawable, key_chats_nameIcon);
        setDrawableColorByKey(dialogs_botDrawable, key_chats_nameIcon);
        setDrawableColorByKey(dialogs_pinnedDrawable, key_chats_pinnedIcon);
        setDrawableColorByKey(dialogs_muteDrawable, key_chats_muteIcon);
        setDrawableColorByKey(dialogs_verifiedDrawable, key_chats_verifiedBackground);
        setDrawableColorByKey(dialogs_verifiedCheckDrawable, key_chats_verifiedCheck);
        //plus
        setDrawableColorByKey(dialogs_superGroupDrawable, key_chats_nameIcon);
        setDrawableColorByKey(dialogs_FavDrawable, key_chats_nameIcon);
        //
        //plus
        if(usePlusTheme) {
            SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
            //dialogs_nameEncryptedPaint.setColor(themePrefs.getInt("chatsNameColor", Theme.darkColor));//0xff00a60e
            dialogs_groupPaint.setColor(themePrefs.getInt("chatsGroupNameColor", Theme.chatsNameColor));
            dialogs_nameUnknownPaint.setColor(themePrefs.getInt("chatsUnknownNameColor", Theme.chatsNameColor));//0xff4d83b3
            dialogs_messagePaint.setColor(chatsMessageColor);
            dialogs_messagePrintingPaint.setColor(themePrefs.getInt("chatsMessageColor", Theme.defColor));
            dialogs_mediaPaint.setColor(themePrefs.getInt("chatsMediaColor", Theme.darkColor));
            dialogs_messageTypingPaint.setColor(themePrefs.getInt("chatsTypingColor", Theme.defColor));
            dialogs_timePaint.setColor(themePrefs.getInt("chatsTimeColor", 0xff999999));
            dialogs_countTextPaint.setColor(themePrefs.getInt("chatsCountColor", 0xffffffff));

            //dialogs_groupPaint.setColor(0xff212121);
            //dialogs_nameUnknownPaint.setColor(0xff4d83b3);
            //dialogs_messageTypingPaint.setColor(0xff4d83b3);
            dialogs_mediaPaint.setColor(0xff00ff00);

            dialogs_lockDrawable.setColorFilter(themePrefs.getInt("chatsGroupIconColor", themePrefs.getInt("chatsNameColor", Theme.defColor)), PorterDuff.Mode.SRC_IN);
            //dialogs_checkDrawable.setColorFilter(chatsChecksColor, PorterDuff.Mode.SRC_IN);
            //dialogs_halfCheckDrawable.setColorFilter(chatsChecksColor, PorterDuff.Mode.SRC_IN);
            //dialogs_clockDrawable.setColorFilter(chatsChecksColor, PorterDuff.Mode.SRC_IN);

            int nColor = themePrefs.getInt("chatsGroupIconColor", themePrefs.getInt("chatsGroupNameColor", 0xff000000));
            dialogs_groupDrawable.setColorFilter(nColor, PorterDuff.Mode.SRC_IN);
            dialogs_superGroupDrawable.setColorFilter(nColor, PorterDuff.Mode.SRC_IN);
            dialogs_broadcastDrawable.setColorFilter(nColor, PorterDuff.Mode.SRC_IN);
            dialogs_botDrawable.setColorFilter(nColor, PorterDuff.Mode.SRC_IN);
            int mColor = themePrefs.getInt("chatsMuteColor", 0xffa8a8a8);
            dialogs_muteDrawable.setColorFilter(mColor, PorterDuff.Mode.SRC_IN);
            //dividerPaint.setColor(chatsDividerColor);
            dialogs_pinnedDrawable.setColorFilter(nColor, PorterDuff.Mode.SRC_IN);
            dialogs_countPaint.setColor(themePrefs.getInt("chatsCountBGColor", Theme.defColor));
            dialogs_countGrayPaint.setColor(themePrefs.getInt("chatsCountSilentBGColor", themePrefs.getInt("chatsCountBGColor", 0xffb9b9b9)));

            dialogs_pinnedPaint.setColor(/*0x20000000*//*AndroidUtilities.setDarkColor(chatsRowColor, 0x15)*/chatsPinnedMsgBGColor);
            dialogs_FavDrawable.setColorFilter(Theme.chatsFavIndicatorColor, PorterDuff.Mode.SRC_IN);
        }
        //dialogs_StatusBGDrawable.setColor(Color.GRAY);

        //
    }
    //plus
    private static int checkVal;

    private static void setDialogsChecks(Resources resources) {
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        String check = themePrefs.getString("chatCheckStyle", Theme.checksNamesArray[0]);
        //if(checkVal != Theme.chatCheckStyleVal) {
            checkVal = Theme.chatCheckStyleVal;
            //Log.e("Theme", "setDialogsChecks check:" + check);
            if (check.equals(Theme.checksNamesArray[1])) {
                dialogs_checkDrawable = resources.getDrawable(R.drawable.dialogs_check_2);
                dialogs_halfCheckDrawable = resources.getDrawable(R.drawable.dialogs_halfcheck_2);
            } else if (check.equals(Theme.checksNamesArray[2])) {
                dialogs_checkDrawable = resources.getDrawable(R.drawable.dialogs_check_3);
                dialogs_halfCheckDrawable = resources.getDrawable(R.drawable.dialogs_halfcheck_3);
            } else if (check.equals(Theme.checksNamesArray[3])) {
                dialogs_checkDrawable = resources.getDrawable(R.drawable.dialogs_check_4);
                dialogs_halfCheckDrawable = resources.getDrawable(R.drawable.dialogs_halfcheck_4);
            } else if (check.equals(Theme.checksNamesArray[4])) {
                dialogs_checkDrawable = resources.getDrawable(R.drawable.dialogs_check_5);
                dialogs_halfCheckDrawable = resources.getDrawable(R.drawable.dialogs_halfcheck_5);
            } else if (check.equals(Theme.checksNamesArray[5])) {
                dialogs_checkDrawable = resources.getDrawable(R.drawable.dialogs_check_6);
                dialogs_halfCheckDrawable = resources.getDrawable(R.drawable.dialogs_halfcheck_6);
            } else if (check.equals(Theme.checksNamesArray[6])) {
                dialogs_checkDrawable = resources.getDrawable(R.drawable.dialogs_check_7);
                dialogs_halfCheckDrawable = resources.getDrawable(R.drawable.dialogs_halfcheck_7);
            } else if (check.equals(Theme.checksNamesArray[7])) {
                dialogs_checkDrawable = resources.getDrawable(R.drawable.dialogs_check_8);
                dialogs_halfCheckDrawable = resources.getDrawable(R.drawable.dialogs_halfcheck_8);
            } else if (check.equals(Theme.checksNamesArray[8])) {
                dialogs_checkDrawable = resources.getDrawable(R.drawable.dialogs_check_9);
                dialogs_halfCheckDrawable = resources.getDrawable(R.drawable.dialogs_halfcheck_9);
            } else if (check.equals(Theme.checksNamesArray[9])) {
                dialogs_checkDrawable = resources.getDrawable(R.drawable.dialogs_check_10);
                dialogs_halfCheckDrawable = resources.getDrawable(R.drawable.dialogs_halfcheck_10);
            } else if (check.equals(Theme.checksNamesArray[10])) {
                dialogs_checkDrawable = resources.getDrawable(R.drawable.dialogs_check_11);
                dialogs_halfCheckDrawable = resources.getDrawable(R.drawable.dialogs_halfcheck_11);
            } else if (check.equals(Theme.checksNamesArray[11])) {
                dialogs_checkDrawable = resources.getDrawable(R.drawable.dialogs_check_12);
                dialogs_halfCheckDrawable = resources.getDrawable(R.drawable.dialogs_halfcheck_12);
            } else {
                dialogs_checkDrawable = resources.getDrawable(R.drawable.list_check);
                dialogs_halfCheckDrawable = resources.getDrawable(R.drawable.list_halfcheck);
            }
        //}
    }
    //
    public static void destroyResources() {
        for (int a = 0; a < chat_attachButtonDrawables.length; a++) {
            if (chat_attachButtonDrawables[a] != null) {
                chat_attachButtonDrawables[a].setCallback(null);
            }
        }
    }

    public static void createChatResources(Context context, boolean fontsOnly) {
        synchronized (sync) {
            if (chat_msgTextPaint == null) {
                chat_msgTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
                chat_msgGameTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
                chat_msgTextPaintOneEmoji = new TextPaint(Paint.ANTI_ALIAS_FLAG);
                chat_msgTextPaintTwoEmoji = new TextPaint(Paint.ANTI_ALIAS_FLAG);
                chat_msgTextPaintThreeEmoji = new TextPaint(Paint.ANTI_ALIAS_FLAG);
                chat_msgBotButtonPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
                chat_msgBotButtonPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            }
        }

        if (!fontsOnly && chat_msgInDrawable == null) {
            chat_infoPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            chat_docNamePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            chat_docNamePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            chat_docBackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            chat_deleteProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            chat_botProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            chat_botProgressPaint.setStrokeCap(Paint.Cap.ROUND);
            chat_botProgressPaint.setStyle(Paint.Style.STROKE);
            chat_locationTitlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            chat_locationTitlePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            chat_locationAddressPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            chat_urlPaint = new Paint();
            chat_textSearchSelectionPaint = new Paint();
            chat_radialProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            chat_radialProgressPaint.setStrokeCap(Paint.Cap.ROUND);
            chat_radialProgressPaint.setStyle(Paint.Style.STROKE);
            chat_radialProgressPaint.setColor(0x9fffffff);
            chat_audioTimePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            chat_audioTitlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            chat_audioTitlePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            chat_audioPerformerPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            chat_botButtonPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            chat_botButtonPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            chat_contactNamePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            chat_contactNamePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            chat_contactPhonePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            chat_durationPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            chat_gamePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            chat_gamePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            chat_shipmentPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            chat_timePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            chat_namePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            chat_namePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            chat_forwardNamePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            chat_replyNamePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            chat_replyNamePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            chat_replyTextPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            chat_instantViewPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            chat_instantViewPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            chat_instantViewRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            chat_instantViewRectPaint.setStyle(Paint.Style.STROKE);
            chat_replyLinePaint = new Paint();
            chat_msgErrorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            chat_statusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            chat_statusRecordPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            chat_statusRecordPaint.setStyle(Paint.Style.STROKE);
            chat_statusRecordPaint.setStrokeCap(Paint.Cap.ROUND);
            chat_actionTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            chat_actionTextPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            chat_actionBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            chat_timeBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            chat_contextResult_titleTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            chat_contextResult_titleTextPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            chat_contextResult_descriptionTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            chat_composeBackgroundPaint = new Paint();

            Resources resources = context.getResources();
			updateAllColors();//plus
            chat_msgInDrawable = resources.getDrawable(R.drawable.msg_in).mutate();
            chat_msgInSelectedDrawable = resources.getDrawable(R.drawable.msg_in).mutate();

            chat_msgOutDrawable = resources.getDrawable(R.drawable.msg_out).mutate();
            chat_msgOutSelectedDrawable = resources.getDrawable(R.drawable.msg_out).mutate();

            chat_msgInMediaDrawable = resources.getDrawable(R.drawable.msg_photo).mutate();
            chat_msgInMediaSelectedDrawable = resources.getDrawable(R.drawable.msg_photo).mutate();
            chat_msgOutMediaDrawable = resources.getDrawable(R.drawable.msg_photo).mutate();
            chat_msgOutMediaSelectedDrawable = resources.getDrawable(R.drawable.msg_photo).mutate();

            chat_msgOutCheckDrawable = resources.getDrawable(R.drawable.msg_check).mutate();
            chat_msgOutCheckSelectedDrawable = resources.getDrawable(R.drawable.msg_check).mutate();
            chat_msgMediaCheckDrawable = resources.getDrawable(R.drawable.msg_check).mutate();
            chat_msgStickerCheckDrawable = resources.getDrawable(R.drawable.msg_check).mutate();
            chat_msgOutHalfCheckDrawable = resources.getDrawable(R.drawable.msg_halfcheck).mutate();
            chat_msgOutHalfCheckSelectedDrawable = resources.getDrawable(R.drawable.msg_halfcheck).mutate();
            chat_msgMediaHalfCheckDrawable = resources.getDrawable(R.drawable.msg_halfcheck).mutate();
            chat_msgStickerHalfCheckDrawable = resources.getDrawable(R.drawable.msg_halfcheck).mutate();
            chat_msgOutClockDrawable = resources.getDrawable(R.drawable.msg_clock).mutate();
            chat_msgOutSelectedClockDrawable = resources.getDrawable(R.drawable.msg_clock).mutate();
            chat_msgInClockDrawable = resources.getDrawable(R.drawable.msg_clock).mutate();
            chat_msgInSelectedClockDrawable = resources.getDrawable(R.drawable.msg_clock).mutate();
            chat_msgMediaClockDrawable = resources.getDrawable(R.drawable.msg_clock).mutate();
            chat_msgStickerClockDrawable = resources.getDrawable(R.drawable.msg_clock).mutate();
            chat_msgInViewsDrawable = resources.getDrawable(R.drawable.msg_views).mutate();
            chat_msgInViewsSelectedDrawable = resources.getDrawable(R.drawable.msg_views).mutate();
            chat_msgOutViewsDrawable = resources.getDrawable(R.drawable.msg_views).mutate();
            chat_msgOutViewsSelectedDrawable = resources.getDrawable(R.drawable.msg_views).mutate();
            chat_msgMediaViewsDrawable = resources.getDrawable(R.drawable.msg_views).mutate();
            chat_msgStickerViewsDrawable = resources.getDrawable(R.drawable.msg_views).mutate();
            chat_msgInMenuDrawable = resources.getDrawable(R.drawable.msg_actions).mutate();
            chat_msgInMenuSelectedDrawable = resources.getDrawable(R.drawable.msg_actions).mutate();
            chat_msgOutMenuDrawable = resources.getDrawable(R.drawable.msg_actions).mutate();
            chat_msgOutMenuSelectedDrawable = resources.getDrawable(R.drawable.msg_actions).mutate();
            chat_msgMediaMenuDrawable = resources.getDrawable(R.drawable.video_actions);
            chat_msgInInstantDrawable = resources.getDrawable(R.drawable.msg_instant).mutate();
            chat_msgOutInstantDrawable = resources.getDrawable(R.drawable.msg_instant).mutate();
            chat_msgErrorDrawable = resources.getDrawable(R.drawable.msg_warning);
            chat_muteIconDrawable = resources.getDrawable(R.drawable.list_mute).mutate();
            chat_lockIconDrawable = resources.getDrawable(R.drawable.ic_lock_header);
            chat_msgBroadcastDrawable = resources.getDrawable(R.drawable.broadcast3).mutate();
            chat_msgBroadcastMediaDrawable = resources.getDrawable(R.drawable.broadcast3).mutate();
            chat_msgInCallDrawable = resources.getDrawable(R.drawable.ic_call_white_24dp).mutate();
            chat_msgInCallSelectedDrawable = resources.getDrawable(R.drawable.ic_call_white_24dp).mutate();
            chat_msgOutCallDrawable = resources.getDrawable(R.drawable.ic_call_white_24dp).mutate();
            chat_msgOutCallSelectedDrawable = resources.getDrawable(R.drawable.ic_call_white_24dp).mutate();
            chat_msgCallUpRedDrawable = resources.getDrawable(R.drawable.ic_call_made_green_18dp).mutate();
            chat_msgCallUpGreenDrawable = resources.getDrawable(R.drawable.ic_call_made_green_18dp).mutate();
            chat_msgCallDownRedDrawable = resources.getDrawable(R.drawable.ic_call_received_green_18dp).mutate();
            chat_msgCallDownGreenDrawable = resources.getDrawable(R.drawable.ic_call_received_green_18dp).mutate();

            chat_inlineResultFile = resources.getDrawable(R.drawable.bot_file);
            chat_inlineResultAudio = resources.getDrawable(R.drawable.bot_music);
            chat_inlineResultLocation = resources.getDrawable(R.drawable.bot_location);

            chat_msgInShadowDrawable = resources.getDrawable(R.drawable.msg_in_shadow);
            chat_msgOutShadowDrawable = resources.getDrawable(R.drawable.msg_out_shadow);
            chat_msgInMediaShadowDrawable = resources.getDrawable(R.drawable.msg_photo_shadow);
            chat_msgOutMediaShadowDrawable = resources.getDrawable(R.drawable.msg_photo_shadow);

            chat_botLinkDrawalbe = resources.getDrawable(R.drawable.bot_link);
            chat_botInlineDrawable = resources.getDrawable(R.drawable.bot_lines);

            chat_systemDrawable = resources.getDrawable(R.drawable.system);

            chat_contextResult_shadowUnderSwitchDrawable = resources.getDrawable(R.drawable.header_shadow).mutate();

            chat_attachButtonDrawables[0] = resources.getDrawable(R.drawable.attach_camera_states);
            chat_attachButtonDrawables[1] = resources.getDrawable(R.drawable.attach_gallery_states);
            chat_attachButtonDrawables[2] = resources.getDrawable(R.drawable.attach_video_states);
            chat_attachButtonDrawables[3] = resources.getDrawable(R.drawable.attach_audio_states);
            chat_attachButtonDrawables[4] = resources.getDrawable(R.drawable.attach_file_states);
            chat_attachButtonDrawables[5] = resources.getDrawable(R.drawable.attach_contact_states);
            chat_attachButtonDrawables[6] = resources.getDrawable(R.drawable.attach_location_states);
            chat_attachButtonDrawables[7] = resources.getDrawable(R.drawable.attach_hide_states);

            chat_cornerOuter[0] = resources.getDrawable(R.drawable.corner_out_tl);
            chat_cornerOuter[1] = resources.getDrawable(R.drawable.corner_out_tr);
            chat_cornerOuter[2] = resources.getDrawable(R.drawable.corner_out_br);
            chat_cornerOuter[3] = resources.getDrawable(R.drawable.corner_out_bl);

            chat_cornerInner[0] = resources.getDrawable(R.drawable.corner_in_tr);
            chat_cornerInner[1] = resources.getDrawable(R.drawable.corner_in_tl);
            chat_cornerInner[2] = resources.getDrawable(R.drawable.corner_in_br);
            chat_cornerInner[3] = resources.getDrawable(R.drawable.corner_in_bl);

            chat_shareDrawable = resources.getDrawable(R.drawable.share_round);
            chat_shareIconDrawable = resources.getDrawable(R.drawable.share_arrow);

            chat_ivStatesDrawable[0][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(40), R.drawable.msg_round_play_m, 1);
            chat_ivStatesDrawable[0][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(40), R.drawable.msg_round_play_m, 1);
            chat_ivStatesDrawable[1][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(40), R.drawable.msg_round_pause_m, 1);
            chat_ivStatesDrawable[1][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(40), R.drawable.msg_round_pause_m, 1);
            chat_ivStatesDrawable[2][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(40), R.drawable.msg_round_load_m, 1);
            chat_ivStatesDrawable[2][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(40), R.drawable.msg_round_load_m, 1);
            chat_ivStatesDrawable[3][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(40), R.drawable.msg_round_cancel_m, 2);
            chat_ivStatesDrawable[3][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(40), R.drawable.msg_round_cancel_m, 2);

            chat_fileStatesDrawable[0][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44), R.drawable.msg_round_play_m);
            chat_fileStatesDrawable[0][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44), R.drawable.msg_round_play_m);
            chat_fileStatesDrawable[1][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44), R.drawable.msg_round_pause_m);
            chat_fileStatesDrawable[1][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44), R.drawable.msg_round_pause_m);
            chat_fileStatesDrawable[2][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44), R.drawable.msg_round_load_m);
            chat_fileStatesDrawable[2][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44), R.drawable.msg_round_load_m);
            chat_fileStatesDrawable[3][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44), R.drawable.msg_round_file_s);
            chat_fileStatesDrawable[3][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44), R.drawable.msg_round_file_s);
            chat_fileStatesDrawable[4][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44), R.drawable.msg_round_cancel_m);
            chat_fileStatesDrawable[4][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44), R.drawable.msg_round_cancel_m);
            chat_fileStatesDrawable[5][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44), R.drawable.msg_round_play_m);
            chat_fileStatesDrawable[5][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44), R.drawable.msg_round_play_m);
            chat_fileStatesDrawable[6][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44), R.drawable.msg_round_pause_m);
            chat_fileStatesDrawable[6][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44), R.drawable.msg_round_pause_m);
            chat_fileStatesDrawable[7][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44), R.drawable.msg_round_load_m);
            chat_fileStatesDrawable[7][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44), R.drawable.msg_round_load_m);
            chat_fileStatesDrawable[8][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44), R.drawable.msg_round_file_s);
            chat_fileStatesDrawable[8][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44), R.drawable.msg_round_file_s);
            chat_fileStatesDrawable[9][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44), R.drawable.msg_round_cancel_m);
            chat_fileStatesDrawable[9][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44), R.drawable.msg_round_cancel_m);

            chat_photoStatesDrawables[0][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(48), R.drawable.msg_round_load_m);
            chat_photoStatesDrawables[0][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(48), R.drawable.msg_round_load_m);
            chat_photoStatesDrawables[1][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(48), R.drawable.msg_round_cancel_m);
            chat_photoStatesDrawables[1][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(48), R.drawable.msg_round_cancel_m);
            chat_photoStatesDrawables[2][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(48), R.drawable.msg_round_gif_m);
            chat_photoStatesDrawables[2][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(48), R.drawable.msg_round_gif_m);
            chat_photoStatesDrawables[3][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(48), R.drawable.msg_round_play_m);
            chat_photoStatesDrawables[3][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(48), R.drawable.msg_round_play_m);

            chat_photoStatesDrawables[4][0] = chat_photoStatesDrawables[4][1] = resources.getDrawable(R.drawable.burn);
            chat_photoStatesDrawables[5][0] = chat_photoStatesDrawables[5][1] = resources.getDrawable(R.drawable.circle);
            chat_photoStatesDrawables[6][0] = chat_photoStatesDrawables[6][1] = resources.getDrawable(R.drawable.photocheck);

            chat_photoStatesDrawables[7][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(48), R.drawable.msg_round_load_m);
            chat_photoStatesDrawables[7][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(48), R.drawable.msg_round_load_m);
            chat_photoStatesDrawables[8][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(48), R.drawable.msg_round_cancel_m);
            chat_photoStatesDrawables[8][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(48), R.drawable.msg_round_cancel_m);
            chat_photoStatesDrawables[9][0] = resources.getDrawable(R.drawable.doc_big).mutate();
            chat_photoStatesDrawables[9][1] = resources.getDrawable(R.drawable.doc_big).mutate();
            chat_photoStatesDrawables[10][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(48), R.drawable.msg_round_load_m);
            chat_photoStatesDrawables[10][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(48), R.drawable.msg_round_load_m);
            chat_photoStatesDrawables[11][0] = createCircleDrawableWithIcon(AndroidUtilities.dp(48), R.drawable.msg_round_cancel_m);
            chat_photoStatesDrawables[11][1] = createCircleDrawableWithIcon(AndroidUtilities.dp(48), R.drawable.msg_round_cancel_m);
            chat_photoStatesDrawables[12][0] = resources.getDrawable(R.drawable.doc_big).mutate();
            chat_photoStatesDrawables[12][1] = resources.getDrawable(R.drawable.doc_big).mutate();

            chat_contactDrawable[0] = createCircleDrawableWithIcon(AndroidUtilities.dp(44), R.drawable.msg_contact);
            chat_contactDrawable[1] = createCircleDrawableWithIcon(AndroidUtilities.dp(44), R.drawable.msg_contact);

            chat_locationDrawable[0] = createRoundRectDrawableWithIcon(AndroidUtilities.dp(2), R.drawable.msg_location);
            chat_locationDrawable[1] = createRoundRectDrawableWithIcon(AndroidUtilities.dp(2), R.drawable.msg_location);

            chat_composeShadowDrawable = context.getResources().getDrawable(R.drawable.compose_panel_shadow);

                try {
                    int bitmapSize = AndroidUtilities.roundMessageSize + AndroidUtilities.dp(6);
                    Bitmap bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    paint.setShadowLayer(AndroidUtilities.dp(4), 0, 0, 0x5f000000);
                    canvas.drawCircle(bitmapSize / 2, bitmapSize / 2, AndroidUtilities.roundMessageSize / 2 - AndroidUtilities.dp(1), paint);
                    try {
                        canvas.setBitmap(null);
                    } catch (Exception ignore) {

                    }
                    chat_roundVideoShadow = new BitmapDrawable(bitmap);
                } catch (Throwable ignore) {

                }
            
//plus
            setBubbles(context);
            setChecks(context);
            updatePlusPrefs();
            //
            applyChatTheme(fontsOnly);
        }

        chat_msgTextPaintOneEmoji.setTextSize(AndroidUtilities.dp(28));
        chat_msgTextPaintTwoEmoji.setTextSize(AndroidUtilities.dp(24));
        chat_msgTextPaintThreeEmoji.setTextSize(AndroidUtilities.dp(20));
        chat_msgTextPaint.setTextSize(AndroidUtilities.dp(MessagesController.getInstance().fontSize));
        chat_msgGameTextPaint.setTextSize(AndroidUtilities.dp(14));
        chat_msgBotButtonPaint.setTextSize(AndroidUtilities.dp(15));

        if (!fontsOnly && chat_botProgressPaint != null) {
            chat_botProgressPaint.setStrokeWidth(AndroidUtilities.dp(2));
            chat_infoPaint.setTextSize(AndroidUtilities.dp(12));
            chat_docNamePaint.setTextSize(AndroidUtilities.dp(15));
            chat_locationTitlePaint.setTextSize(AndroidUtilities.dp(15));
            chat_locationAddressPaint.setTextSize(AndroidUtilities.dp(13));
            chat_audioTimePaint.setTextSize(AndroidUtilities.dp(12));
            chat_audioTitlePaint.setTextSize(AndroidUtilities.dp(16));
            chat_audioPerformerPaint.setTextSize(AndroidUtilities.dp(15));
            chat_botButtonPaint.setTextSize(AndroidUtilities.dp(15));
            chat_contactNamePaint.setTextSize(AndroidUtilities.dp(15));
            chat_contactPhonePaint.setTextSize(AndroidUtilities.dp(13));
            chat_durationPaint.setTextSize(AndroidUtilities.dp(12));
            chat_timePaint.setTextSize(AndroidUtilities.dp(12));
            chat_namePaint.setTextSize(AndroidUtilities.dp(14));
            chat_forwardNamePaint.setTextSize(AndroidUtilities.dp(14));
            chat_replyNamePaint.setTextSize(AndroidUtilities.dp(14));
            chat_replyTextPaint.setTextSize(AndroidUtilities.dp(14));
            chat_gamePaint.setTextSize(AndroidUtilities.dp(13));
            chat_shipmentPaint.setTextSize(AndroidUtilities.dp(13));
            chat_instantViewPaint.setTextSize(AndroidUtilities.dp(13));
            chat_instantViewRectPaint.setStrokeWidth(AndroidUtilities.dp(1));
            chat_statusRecordPaint.setStrokeWidth(AndroidUtilities.dp(2));
            chat_actionTextPaint.setTextSize(AndroidUtilities.dp(Math.max(16, MessagesController.getInstance().fontSize) - 2));
            chat_contextResult_titleTextPaint.setTextSize(AndroidUtilities.dp(15));
            chat_contextResult_descriptionTextPaint.setTextSize(AndroidUtilities.dp(13));
            chat_radialProgressPaint.setStrokeWidth(AndroidUtilities.dp(3));

        }
    }

    public static void applyChatTheme(boolean fontsOnly) {
        if (chat_msgTextPaint == null) {
            return;
        }

        if (chat_msgInDrawable != null && !fontsOnly) {
            chat_gamePaint.setColor(getColor(key_chat_previewGameText));
            chat_durationPaint.setColor(getColor(key_chat_previewDurationText));
            chat_botButtonPaint.setColor(getColor(key_chat_botButtonText));
            chat_urlPaint.setColor(getColor(key_chat_linkSelectBackground));
            chat_botProgressPaint.setColor(getColor(key_chat_botProgress));
            chat_deleteProgressPaint.setColor(getColor(key_chat_secretTimeText));
            chat_textSearchSelectionPaint.setColor(getColor(key_chat_textSelectBackground));
            chat_msgErrorPaint.setColor(getColor(key_chat_sentError));
            chat_statusPaint.setColor(getColor(key_actionBarDefaultSubtitle));
            chat_statusRecordPaint.setColor(getColor(key_actionBarDefaultSubtitle));
            chat_actionTextPaint.setColor(getColor(key_chat_serviceText));
            chat_actionTextPaint.linkColor = getColor(key_chat_serviceLink);
            chat_contextResult_titleTextPaint.setColor(getColor(key_windowBackgroundWhiteBlackText));
            chat_composeBackgroundPaint.setColor(getColor(key_chat_messagePanelBackground));
            chat_timeBackgroundPaint.setColor(getColor(key_chat_mediaTimeBackground));
            //Log.e("Theme", "applyChatTheme chatBubbleStyleVal " + chatBubbleStyleVal);
            setDrawableColorByKey(chat_msgInDrawable, key_chat_inBubble);
            setDrawableColorByKey(chat_msgInSelectedDrawable, key_chat_inBubbleSelected);
            setDrawableColorByKey(chat_msgInShadowDrawable, chatBubbleStyleVal == 0 ? key_chat_inBubbleShadow : key_chat_inBubble);
            //setDrawableColorByKey(chat_msgOutDrawable, key_chat_outBubble);
            if(chatBubbleStyleVal == 0){
                setDrawableColorByKey(chat_msgOutDrawable, key_chat_outBubble);
                setDrawableColorByKey(chat_msgOutShadowDrawable, key_chat_outBubbleShadow);
                setDrawableColorByKey(chat_msgOutMediaDrawable, key_chat_outBubble);
                setDrawableColorByKey(chat_msgOutMediaShadowDrawable, key_chat_outBubbleShadow);
                setDrawableColorByKey(chat_msgOutMediaSelectedDrawable, key_chat_outBubbleSelected);
            } else{
                setDrawableColorByKeyIN(chat_msgOutDrawable, key_chat_outBubble);
                setDrawableColorByKeyIN(chat_msgOutShadowDrawable, key_chat_outBubble);
                setDrawableColorByKeyIN(chat_msgOutMediaDrawable, key_chat_outBubble);
                setDrawableColorByKeyIN(chat_msgOutMediaShadowDrawable, key_chat_outBubble);
                setDrawableColorByKeyIN(chat_msgOutMediaSelectedDrawable, key_chat_outBubbleSelected);
            }
            setDrawableColorByKey(chat_msgOutSelectedDrawable, key_chat_outBubbleSelected);
            //setDrawableColorByKey(chat_msgOutShadowDrawable, chatBubbleStyleVal == 0 ? key_chat_outBubbleShadow : key_chat_outBubble);
            setDrawableColorByKey(chat_msgInMediaDrawable, key_chat_inBubble);
            setDrawableColorByKey(chat_msgInMediaSelectedDrawable, key_chat_inBubbleSelected);
            setDrawableColorByKey(chat_msgInMediaShadowDrawable, key_chat_inBubbleShadow);
            //setDrawableColorByKey(chat_msgOutMediaDrawable, key_chat_outBubble);
            //setDrawableColorByKey(chat_msgOutMediaSelectedDrawable, key_chat_outBubbleSelected);
            //setDrawableColorByKey(chat_msgOutMediaShadowDrawable, key_chat_outBubbleShadow);
            setDrawableColorByKey(chat_msgOutCheckDrawable, key_chat_outSentCheck);
            setDrawableColorByKey(chat_msgOutCheckSelectedDrawable, key_chat_outSentCheckSelected);
            setDrawableColorByKey(chat_msgOutHalfCheckDrawable, key_chat_outSentCheck);
            setDrawableColorByKey(chat_msgOutHalfCheckSelectedDrawable, key_chat_outSentCheckSelected);
            setDrawableColorByKey(chat_msgOutClockDrawable, key_chat_outSentClock);
            setDrawableColorByKey(chat_msgOutSelectedClockDrawable, key_chat_outSentClockSelected);
            setDrawableColorByKey(chat_msgInClockDrawable, key_chat_inSentClock);
            setDrawableColorByKey(chat_msgInSelectedClockDrawable, key_chat_inSentClockSelected);
            setDrawableColorByKey(chat_msgMediaCheckDrawable, key_chat_mediaSentCheck);
            setDrawableColorByKey(chat_msgMediaHalfCheckDrawable, key_chat_mediaSentCheck);
            setDrawableColorByKey(chat_msgMediaClockDrawable, key_chat_mediaSentClock);
            setDrawableColorByKey(chat_msgStickerCheckDrawable, key_chat_serviceText);
            setDrawableColorByKey(chat_msgStickerHalfCheckDrawable, key_chat_serviceText);
            setDrawableColorByKey(chat_msgStickerClockDrawable, key_chat_serviceText);
            setDrawableColorByKey(chat_msgStickerViewsDrawable, key_chat_serviceText);
            setDrawableColorByKey(chat_shareIconDrawable, key_chat_serviceIcon);
            setDrawableColorByKey(chat_botInlineDrawable, key_chat_serviceIcon);
            setDrawableColorByKey(chat_botLinkDrawalbe, key_chat_serviceIcon);
            setDrawableColorByKey(chat_msgInViewsDrawable, key_chat_inViews);
            setDrawableColorByKey(chat_msgInViewsSelectedDrawable, key_chat_inViewsSelected);
            setDrawableColorByKey(chat_msgOutViewsDrawable, key_chat_outViews);
            setDrawableColorByKey(chat_msgOutViewsSelectedDrawable, key_chat_outViewsSelected);
            setDrawableColorByKey(chat_msgMediaViewsDrawable, key_chat_mediaViews);
            setDrawableColorByKey(chat_msgInMenuDrawable, key_chat_inMenu);
            setDrawableColorByKey(chat_msgInMenuSelectedDrawable, key_chat_inMenuSelected);
            setDrawableColorByKey(chat_msgOutMenuDrawable, key_chat_outMenu);
            setDrawableColorByKey(chat_msgOutMenuSelectedDrawable, key_chat_outMenuSelected);
            setDrawableColorByKey(chat_msgMediaMenuDrawable, key_chat_mediaMenu);
            setDrawableColorByKey(chat_msgOutInstantDrawable, key_chat_outInstant);
            setDrawableColorByKey(chat_msgInInstantDrawable, key_chat_inInstant);
            setDrawableColorByKey(chat_msgErrorDrawable, key_chat_sentErrorIcon);
            setDrawableColorByKey(chat_muteIconDrawable, key_chat_muteIcon);
            setDrawableColorByKey(chat_lockIconDrawable, key_chat_lockIcon);
            setDrawableColorByKey(chat_msgBroadcastDrawable, key_chat_outBroadcast);
            setDrawableColorByKey(chat_msgBroadcastMediaDrawable, key_chat_mediaBroadcast);
            setDrawableColorByKey(chat_inlineResultFile, key_chat_inlineResultIcon);
            setDrawableColorByKey(chat_inlineResultAudio, key_chat_inlineResultIcon);
            setDrawableColorByKey(chat_inlineResultLocation, key_chat_inlineResultIcon);
            setDrawableColorByKey(chat_msgInCallDrawable, key_chat_inInstant);
            setDrawableColorByKey(chat_msgInCallSelectedDrawable, key_chat_inInstantSelected);
            setDrawableColorByKey(chat_msgOutCallDrawable, key_chat_outInstant);
            setDrawableColorByKey(chat_msgOutCallSelectedDrawable, key_chat_outInstantSelected);
            setDrawableColorByKey(chat_msgCallUpRedDrawable, key_calls_callReceivedRedIcon);
            setDrawableColorByKey(chat_msgCallUpGreenDrawable, key_calls_callReceivedGreenIcon);
            setDrawableColorByKey(chat_msgCallDownRedDrawable, key_calls_callReceivedRedIcon);
            setDrawableColorByKey(chat_msgCallDownGreenDrawable, key_calls_callReceivedGreenIcon);

            for (int a = 0; a < 5; a++) {
                setCombinedDrawableColor(chat_fileStatesDrawable[a][0], getColor(key_chat_outLoader), false);
                setCombinedDrawableColor(chat_fileStatesDrawable[a][0], getColor(key_chat_outBubble), true);
                setCombinedDrawableColor(chat_fileStatesDrawable[a][1], getColor(key_chat_outLoaderSelected), false);
                setCombinedDrawableColor(chat_fileStatesDrawable[a][1], getColor(key_chat_outBubbleSelected), true);
                setCombinedDrawableColor(chat_fileStatesDrawable[5 + a][0], getColor(key_chat_inLoader), false);
                setCombinedDrawableColor(chat_fileStatesDrawable[5 + a][0], getColor(key_chat_inBubble), true);
                setCombinedDrawableColor(chat_fileStatesDrawable[5 + a][1], getColor(key_chat_inLoaderSelected), false);
                setCombinedDrawableColor(chat_fileStatesDrawable[5 + a][1], getColor(key_chat_inBubbleSelected), true);
            }
            for (int a = 0; a < 4; a++) {
                setCombinedDrawableColor(chat_photoStatesDrawables[a][0], getColor(key_chat_mediaLoaderPhoto), false);
                setCombinedDrawableColor(chat_photoStatesDrawables[a][0], getColor(key_chat_mediaLoaderPhotoIcon), true);
                setCombinedDrawableColor(chat_photoStatesDrawables[a][1], getColor(key_chat_mediaLoaderPhotoSelected), false);
                setCombinedDrawableColor(chat_photoStatesDrawables[a][1], getColor(key_chat_mediaLoaderPhotoIconSelected), true);
            }
            for (int a = 0; a < 2; a++) {
                setCombinedDrawableColor(chat_photoStatesDrawables[7 + a][0], getColor(key_chat_outLoaderPhoto), false);
                setCombinedDrawableColor(chat_photoStatesDrawables[7 + a][0], getColor(key_chat_outLoaderPhotoIcon), true);
                setCombinedDrawableColor(chat_photoStatesDrawables[7 + a][1], getColor(key_chat_outLoaderPhotoSelected), false);
                setCombinedDrawableColor(chat_photoStatesDrawables[7 + a][1], getColor(key_chat_outLoaderPhotoIconSelected), true);
                setCombinedDrawableColor(chat_photoStatesDrawables[10 + a][0], getColor(key_chat_inLoaderPhoto), false);
                setCombinedDrawableColor(chat_photoStatesDrawables[10 + a][0], getColor(key_chat_inLoaderPhotoIcon), true);
                setCombinedDrawableColor(chat_photoStatesDrawables[10 + a][1], getColor(key_chat_inLoaderPhotoSelected), false);
                setCombinedDrawableColor(chat_photoStatesDrawables[10 + a][1], getColor(key_chat_inLoaderPhotoIconSelected), true);
            }

            setDrawableColorByKey(chat_photoStatesDrawables[9][0], key_chat_outFileIcon);
            setDrawableColorByKey(chat_photoStatesDrawables[9][1], key_chat_outFileSelectedIcon);
            setDrawableColorByKey(chat_photoStatesDrawables[12][0], key_chat_inFileIcon);
            setDrawableColorByKey(chat_photoStatesDrawables[12][1], key_chat_inFileSelectedIcon);

            setCombinedDrawableColor(chat_contactDrawable[0], getColor(key_chat_inContactBackground), false);
            setCombinedDrawableColor(chat_contactDrawable[0], getColor(key_chat_inContactIcon), true);
            setCombinedDrawableColor(chat_contactDrawable[1], getColor(key_chat_outContactBackground), false);
            setCombinedDrawableColor(chat_contactDrawable[1], getColor(key_chat_outContactIcon), true);

            setCombinedDrawableColor(chat_locationDrawable[0], getColor(key_chat_inLocationBackground), false);
            setCombinedDrawableColor(chat_locationDrawable[0], getColor(key_chat_inLocationIcon), true);
            setCombinedDrawableColor(chat_locationDrawable[1], getColor(key_chat_outLocationBackground), false);
            setCombinedDrawableColor(chat_locationDrawable[1], getColor(key_chat_outLocationIcon), true);

            setDrawableColorByKey(chat_composeShadowDrawable, key_chat_messagePanelShadow);

            applyChatServiceMessageColor();

            if(usePlusTheme){
                chat_msgInViewsDrawable.setColorFilter(new PorterDuffColorFilter(chatLTimeColor, PorterDuff.Mode.MULTIPLY));
                chat_msgInViewsSelectedDrawable.setColorFilter(new PorterDuffColorFilter(chatLTimeColor, PorterDuff.Mode.MULTIPLY));
                chat_msgMediaViewsDrawable.setColorFilter(new PorterDuffColorFilter(chatLTimeColor, PorterDuff.Mode.MULTIPLY));
                //chat_msgStickerViewsDrawable.setColorFilter(new PorterDuffColorFilter(chatLTimeColor, PorterDuff.Mode.MULTIPLY));
                chat_msgOutViewsDrawable.setColorFilter(new PorterDuffColorFilter(chatRTimeColor, PorterDuff.Mode.MULTIPLY));
                chat_msgOutViewsSelectedDrawable.setColorFilter(new PorterDuffColorFilter(chatRTimeColor, PorterDuff.Mode.MULTIPLY));

                chat_muteIconDrawable.setColorFilter(new PorterDuffColorFilter(chatHeaderIconsColor, PorterDuff.Mode.MULTIPLY));
                chat_lockIconDrawable.setColorFilter(new PorterDuffColorFilter(chatHeaderIconsColor, PorterDuff.Mode.MULTIPLY));

                chat_actionTextPaint.setColor(Theme.chatDateColor);
                if(chatDateColor != 0xffffffff){
                    chat_actionTextPaint.linkColor = AndroidUtilities.getIntDarkerColor("chatDateColor", -0x50);
                }
                chat_textSearchSelectionPaint.setColor(chatSelectedMsgBGColor);
                updateChatDrawablesColor();
            } else{

            }

        }
    }

    public static void applyChatServiceMessageColor() {
        if (chat_actionBackgroundPaint == null) {
            return;
        }
        Integer serviceColor = currentColors.get(key_chat_serviceBackground);
        Integer servicePressedColor = currentColors.get(key_chat_serviceBackgroundSelected);
        boolean override;
        if (serviceColor == null) {
            serviceColor = serviceMessageColor;
        }
        if (servicePressedColor == null) {
            servicePressedColor = serviceSelectedMessageColor;
        }
        if (currentColor != serviceColor) {
            chat_actionBackgroundPaint.setColor(usePlusTheme ? chatDateBubbleColor : serviceColor);
            //Log.e("Theme", "applyChatServiceMessageColor serviceColor " + serviceColor);
            colorFilter = new PorterDuffColorFilter(serviceColor, PorterDuff.Mode.MULTIPLY);
            currentColor = serviceColor;
            if (chat_cornerOuter[0] != null) {
                for (int a = 0; a < 4; a++) {
                    chat_cornerOuter[a].setColorFilter(usePlusTheme ? new PorterDuffColorFilter(chatDateBubbleColor, PorterDuff.Mode.MULTIPLY) : colorFilter);
                    chat_cornerInner[a].setColorFilter(usePlusTheme ? new PorterDuffColorFilter(chatDateBubbleColor, PorterDuff.Mode.MULTIPLY) : colorFilter);
                }
            }
        }
        if (currentSelectedColor != servicePressedColor) {
            currentSelectedColor = servicePressedColor;
            colorPressedFilter = new PorterDuffColorFilter(servicePressedColor, PorterDuff.Mode.MULTIPLY);
        }
    }

    public static void createProfileResources(Context context) {
        if (profile_verifiedDrawable == null) {
            profile_aboutTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

            Resources resources = context.getResources();

            profile_verifiedDrawable = resources.getDrawable(R.drawable.verified_area).mutate();
            profile_verifiedCheckDrawable = resources.getDrawable(R.drawable.verified_check).mutate();

            applyProfileTheme();
        }

        profile_aboutTextPaint.setTextSize(AndroidUtilities.dp(16));
    }

    public static void applyProfileTheme() {
        if (profile_verifiedDrawable == null) {
            return;
        }

        profile_aboutTextPaint.setColor(usePlusTheme ? profileRowTitleColor : getColor(key_windowBackgroundWhiteBlackText));
        profile_aboutTextPaint.linkColor = usePlusTheme ? profileRowStatusColor : getColor(key_windowBackgroundWhiteLinkText);

        setDrawableColorByKey(profile_verifiedDrawable, key_profile_verifiedBackground);
        setDrawableColorByKey(profile_verifiedCheckDrawable, key_profile_verifiedCheck);
    }

    public static Drawable getThemedDrawable(Context context, int resId, String key) {
        Drawable drawable = context.getResources().getDrawable(resId).mutate();
        drawable.setColorFilter(new PorterDuffColorFilter(getColor(key), PorterDuff.Mode.MULTIPLY));
        return drawable;
    }

    public static int getDefaultColor(String key) {
        Integer value = usePlusTheme && telegramToPlus.containsKey(key) ? defaultPlusColors.get(telegramToPlus.get(key)) : defaultColors.get(key);
        if (value == null) {
            if (key.equals(key_chats_menuTopShadow)) {
                return 0;
            }
            return 0xffff0000;
        }
        return value;
    }

    public static boolean hasThemeKey(String key) {
        return currentColors.containsKey(key);
    }

    public static Integer getColorOrNull(String key) {
        Integer color = currentColors.get(key);
        if (color == null) {
            color = defaultColors.get(key);
        }
        return color;
    }

    public static int getColor(String key) {
        return getColor(key, null);
    }

    public static int getColor(String key, boolean[] isDefault) {
        Integer color = currentColors.get(key);
        //plus
        if(usePlusTheme){
            if(currentPlusColors.containsKey(key)){
                color = currentPlusColors.get(key);
            } else if(telegramToPlus.containsKey(key)){
                color = currentPlusColors.get(telegramToPlus.get(key));
            }
        }
        //
        if (color == null) {
            if (isDefault != null) {
                isDefault[0] = true;
            }
            if (key.equals(key_chat_serviceBackground)) {
                return serviceMessageColor;
            } else if (key.equals(key_chat_serviceBackgroundSelected)) {
                return serviceSelectedMessageColor;
            }
            return getDefaultColor(key);
        }
        return color;
    }

    public static void setColor(String key, int color, boolean useDefault) {
        if (key.equals(key_chat_wallpaper)) {
            color = 0xff000000 | color;
        }

        if (useDefault) {
            currentColors.remove(key);
        } else {
            currentColors.put(key, color);
        }

        if (key.equals(key_chat_serviceBackground) || key.equals(key_chat_serviceBackgroundSelected)) {
            applyChatServiceMessageColor();
        } else if (key.equals(key_chat_wallpaper)) {
            reloadWallpaper();
        }
    }
    //plus
    public static void setPlusColor(String key, int color, boolean useDefault) {
        if (key.equals(key_chat_wallpaper)) {
            color = 0xff000000 | color;
        }

        if (useDefault) {
            currentPlusColors.remove(key);
        } else {
            currentPlusColors.put(key, color);
        }

        if (key.equals(key_chat_serviceBackground) || key.equals(key_chat_serviceBackgroundSelected)) {
            applyChatServiceMessageColor();
        } else if (key.equals(key_chat_wallpaper)) {
            reloadWallpaper();
        }
    }

    /*public static void refreshTheme(){
        reloadWallpaper();
        applyCommonTheme();
        applyDialogsTheme();
        applyProfileTheme();
        applyChatTheme(false);
    }*/
    //
    public static void setThemeWallpaper(String themeName, Bitmap bitmap, File path) {
        currentColors.remove(key_chat_wallpaper);
        ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE).edit().remove("overrideThemeWallpaper").commit();
        if (bitmap != null) {
            themedWallpaper = new BitmapDrawable(bitmap);
            saveCurrentTheme(themeName, false);
            calcBackgroundColor(themedWallpaper, 0);
            applyChatServiceMessageColor();
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.didSetNewWallpapper);
        } else {
            themedWallpaper = null;
            wallpaper = null;
            saveCurrentTheme(themeName, false);
            reloadWallpaper();
        }
    }

    public static void setDrawableColor(Drawable drawable, int color) {
        drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
    }

    public static void setDrawableColorByKey(Drawable drawable, String key) {
        drawable.setColorFilter(new PorterDuffColorFilter(getColor(key), PorterDuff.Mode.MULTIPLY));
    }
    //plus
    public static void setDrawableColorByKeyIN(Drawable drawable, String key) {
        drawable.setColorFilter(new PorterDuffColorFilter(getColor(key), PorterDuff.Mode.SRC_IN));
    }
    //
    public static void setSelectorDrawableColor(Drawable drawable, int color, boolean selected) {
        if (drawable instanceof StateListDrawable) {
            try {
                if (selected) {
                    Drawable state = getStateDrawable(drawable, 0);
                    if (state instanceof ShapeDrawable) {
                        ((ShapeDrawable) state).getPaint().setColor(color);
                    } else {
                        state.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
                    }
                    state = getStateDrawable(drawable, 1);
                    if (state instanceof ShapeDrawable) {
                        ((ShapeDrawable) state).getPaint().setColor(color);
                    } else {
                        state.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
                    }
                } else {
                    Drawable state = getStateDrawable(drawable, 2);
                    if (state instanceof ShapeDrawable) {
                        ((ShapeDrawable) state).getPaint().setColor(color);
                    } else {
                        state.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
                    }
                }
            } catch (Throwable ignore) {

            }
        } else if (Build.VERSION.SDK_INT >= 21 && drawable instanceof RippleDrawable) {
            RippleDrawable rippleDrawable = (RippleDrawable) drawable;
            if (selected) {
                rippleDrawable.setColor(new ColorStateList(
                        new int[][]{StateSet.WILD_CARD},
                        new int[]{color}
                ));
            } else {
                if (rippleDrawable.getNumberOfLayers() > 0) {
                    Drawable drawable1 = rippleDrawable.getDrawable(0);
                    if (drawable1 instanceof ShapeDrawable) {
                        ((ShapeDrawable) drawable1).getPaint().setColor(color);
                    } else {
                        drawable1.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
                    }
                }
            }
        }
    }

    public static boolean hasWallpaperFromTheme() {
        return currentColors.containsKey(key_chat_wallpaper) || themedWallpaperFileOffset > 0;
    }

    public static boolean isCustomTheme() {
        return isCustomTheme;
    }

    public static int getSelectedColor() {
        return selectedColor;
    }

    public static void reloadWallpaper() {
        wallpaper = null;
        themedWallpaper = null;
        loadWallpaper();
    }

    private static void calcBackgroundColor(Drawable drawable, int save) {
        if (save != 2) {
            int result[] = AndroidUtilities.calcDrawableColor(drawable);
            serviceMessageColor = result[0];
            serviceSelectedMessageColor = result[1];
        }
    }

    public static int getServiceMessageColor() {
        Integer serviceColor = currentColors.get(key_chat_serviceBackground);
        return serviceColor == null ? serviceMessageColor : serviceColor;
    }

    public static void loadWallpaper() {
        if (wallpaper != null) {
            return;
        }
        Utilities.searchQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                synchronized (wallpaperSync) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                    boolean overrideTheme = preferences.getBoolean("overrideThemeWallpaper", false);
                    if (!overrideTheme) {
                        Integer backgroundColor = currentColors.get(key_chat_wallpaper);
                        if (backgroundColor != null) {
                            wallpaper = new ColorDrawable(backgroundColor);
                            isCustomTheme = true;
                        } else if (themedWallpaperFileOffset > 0 && (currentTheme.pathToFile != null || currentTheme.assetName != null)) {
                            FileInputStream stream = null;
                            try {
                                int currentPosition = 0;
                                File file;
                                if (currentTheme.assetName != null) {
                                    file = Theme.getAssetFile(currentTheme.assetName);
                                } else {
                                    file = new File(currentTheme.pathToFile);
                                }
                                stream = new FileInputStream(file);
                                stream.getChannel().position(themedWallpaperFileOffset);
                                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                                if (bitmap != null) {
                                    themedWallpaper = wallpaper = new BitmapDrawable(bitmap);
                                    isCustomTheme = true;
                                }
                            } catch (Throwable e) {
                                FileLog.e(e);
                            } finally {
                                try {
                                    if (stream != null) {
                                        stream.close();
                                    }
                                } catch (Exception e) {
                                    FileLog.e(e);
                                }
                            }
                        }
                    }
                    if (wallpaper == null) {
                        int selectedColor = 0;
                        try {
                            preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                            int selectedBackground = preferences.getInt("selectedBackground", 1000001);
                            selectedColor = preferences.getInt("selectedColor", 0);
                            if (selectedColor == 0) {
                                if (selectedBackground == 1000001) {
                                    wallpaper = ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.background_hd);
                                    isCustomTheme = false;
                                } else {
                                    File toFile = new File(ApplicationLoader.getFilesDirFixed(), "wallpaper.jpg");
                                    if (toFile.exists()) {
                                        wallpaper = Drawable.createFromPath(toFile.getAbsolutePath());
                                        isCustomTheme = true;
                                    } else {
                                        wallpaper = ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.background_hd);
                                        isCustomTheme = false;
                                    }
                                }
                            }
                        } catch (Throwable throwable) {
                            //ignore
                        }
                        if (wallpaper == null) {
                            if (selectedColor == 0) {
                                selectedColor = -2693905;
                            }
                            wallpaper = new ColorDrawable(selectedColor);
                        }
                    }
                    calcBackgroundColor(wallpaper, 1);
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            applyChatServiceMessageColor();
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.didSetNewWallpapper);
                        }
                    });
                }
            }
        });
    }

    public static Drawable getThemedWallpaper(boolean thumb) {
        Integer backgroundColor = currentColors.get(key_chat_wallpaper);
        if (backgroundColor != null) {
            return new ColorDrawable(backgroundColor);
        } else if (themedWallpaperFileOffset > 0 && (currentTheme.pathToFile != null || currentTheme.assetName != null)) {
            FileInputStream stream = null;
            try {
                int currentPosition = 0;
                File file;
                if (currentTheme.assetName != null) {
                    file = Theme.getAssetFile(currentTheme.assetName);
                } else {
                    file = new File(currentTheme.pathToFile);
                }
                stream = new FileInputStream(file);
                stream.getChannel().position(themedWallpaperFileOffset);
                BitmapFactory.Options opts = new BitmapFactory.Options();
                int scaleFactor = 1;
                if (thumb) {
                    opts.inJustDecodeBounds = true;
                    float photoW = opts.outWidth;
                    float photoH = opts.outHeight;
                    int maxWidth = AndroidUtilities.dp(100);
                    while (photoW > maxWidth || photoH > maxWidth) {
                        scaleFactor *= 2;
                        photoW /= 2;
                        photoH /= 2;
                    }
                }
                opts.inJustDecodeBounds = false;
                opts.inSampleSize = scaleFactor;
                Bitmap bitmap = BitmapFactory.decodeStream(stream, null, opts);
                if (bitmap != null) {
                    return new BitmapDrawable(bitmap);
                }
            } catch (Throwable e) {
                FileLog.e(e);
            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
        }
        return null;
    }

	public static Drawable getCachedWallpaper() {
        synchronized (wallpaperSync) {
            if (themedWallpaper != null) {
                return themedWallpaper;
            } else {
                return wallpaper;
            }
        }
    }
	//plus
    public static void updateAllColors(){
        updateMainColors();
        updateDrawerColors();
        updateChatColors();
        updateChatsColors();
        updateContactsColors();
        updateProfileColors();
        updatePrefsColors();
    }

    public static void updateMainColors(){
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        usePlusTheme = themePrefs.getBoolean("usePlusTheme", true);
        defColor = themePrefs.getInt("themeColor", AndroidUtilities.defColor);
        dialogColor = themePrefs.getInt("dialogColor", defColor);
        lightColor = AndroidUtilities.getIntDarkerColor("themeColor", -0x40);
        darkColor = AndroidUtilities.getIntDarkerColor("themeColor", 0x15);
    }

    public static void updateChatColors(){
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        chatStatusColor = themePrefs.getInt("chatStatusColor", lightColor);
        chatStatusSize = themePrefs.getInt("chatStatusSize", AndroidUtilities.isTablet() ? 16 : 14);
        chatOnlineColor = themePrefs.getInt("chatOnlineColor", chatStatusColor);
        chatTypingColor = themePrefs.getInt("chatTypingColor", chatStatusColor);
        chatSelectedMsgBGColor = themePrefs.getInt("chatSelectedMsgBGColor", Theme.SELECTED_MDG_BACKGROUND_COLOR_DEF);
        chatQuickBarColor = themePrefs.getInt("chatQuickBarColor", 0xffffffff);
        chatQuickBarNamesColor = themePrefs.getInt("chatQuickBarNamesColor", 0xff212121);
        chatAvatarRadius = themePrefs.getInt("chatAvatarRadius", AndroidUtilities.isTablet() ? 35 : 32);
        chatAvatarSize = themePrefs.getInt("chatAvatarSize", AndroidUtilities.isTablet() ? 45 : 42);
        chatAvatarMarginLeft = themePrefs.getInt("chatAvatarMarginLeft", 6);
        chatAvatarAlignTop = themePrefs.getBoolean("chatAvatarAlignTop", false);
        chatOwnAvatarAlignTop = themePrefs.getBoolean("chatOwnAvatarAlignTop", false);
        chatShowOwnAvatar = themePrefs.getBoolean("chatShowOwnAvatar", false);
        chatShowOwnAvatarGroup = themePrefs.getBoolean("chatShowOwnAvatarGroup", false);
        chatShowContactAvatar = themePrefs.getBoolean("chatShowContactAvatar", false);
        chatDateBubbleColor = themePrefs.getInt("chatDateBubbleColor", 0x66768993);
        chatRTextColor = themePrefs.getInt("chatRTextColor", 0xff000000);
        chatRLinkColor = themePrefs.getInt("chatRLinkColor", defColor);
        chatRBubbleColor = themePrefs.getInt("chatRBubbleColor", AndroidUtilities.getDefBubbleColor());
        chatLTextColor = themePrefs.getInt("chatLTextColor", 0xff000000);
        chatLLinkColor = themePrefs.getInt("chatLLinkColor", defColor);
        chatLBubbleColor = themePrefs.getInt("chatLBubbleColor", 0xffffffff);
        chatDateColor = themePrefs.getInt("chatDateColor", 0xffffffff);
        chatDateSize = themePrefs.getInt("chatDateSize", AndroidUtilities.isTablet() ? 18 : MessagesController.getInstance().fontSize - 2);
        chatChecksColor = themePrefs.getInt("chatChecksColor", defColor);
        chatRTimeColor = themePrefs.getInt("chatRTimeColor", Theme.darkColor);
        chatLTimeColor = themePrefs.getInt("chatLTimeColor", 0xffa1aab3);
        chatContactNameColor = themePrefs.getInt("chatContactNameColor", defColor);
        chatForwardRColor = themePrefs.getInt("chatForwardRColor", darkColor);
        chatForwardLColor = themePrefs.getInt("chatForwardLColor", darkColor);
        chatMemberColor = themePrefs.getInt("chatMemberColor", darkColor);
        chatMemberColorCheck = themePrefs.getBoolean("chatMemberColorCheck", false);
        chatHideStatusIndicator = themePrefs.getBoolean("chatHideStatusIndicator", false);
        chatShowUsernameCheck = themePrefs.getBoolean("chatShowUsernameCheck", false);
        chatSolidBGColorCheck = themePrefs.getBoolean("chatSolidBGColorCheck", false);
        chatHeaderColor = themePrefs.getInt("chatHeaderColor", defColor);
        chatHeaderIconsColor = themePrefs.getInt("chatHeaderIconsColor", 0xffffffff);
        chatTimeSize = themePrefs.getInt("chatTimeSize", AndroidUtilities.isTablet() ? 14 : 12);
        chatEditTextIconsColor = themePrefs.getInt("chatEditTextIconsColor", 0xffadadad);
        chatAttachTextColor = themePrefs.getInt("chatAttachTextColor", defColor);
        chatAttachBGColor = themePrefs.getInt("chatAttachBGColor", 0xffffffff);
    }

    public static void updateChatDrawablesColor(){
        //Log.e("Theme", "PLUS updateChatDrawablesColor");
        int rBubbleSColor = AndroidUtilities.setDarkColor(chatRBubbleColor, 0x15);
        int lBubbleSColor = AndroidUtilities.setDarkColor(chatLBubbleColor, 0x15);

        chat_msgOutDrawable.setColorFilter(chatRBubbleColor, PorterDuff.Mode.SRC_IN);
        chat_msgOutMediaDrawable.setColorFilter(chatRBubbleColor, PorterDuff.Mode.SRC_IN);
        chat_msgOutSelectedDrawable.setColorFilter(rBubbleSColor, PorterDuff.Mode.SRC_IN);
        chat_msgOutMediaSelectedDrawable.setColorFilter(rBubbleSColor, PorterDuff.Mode.SRC_IN);

        chat_msgInDrawable.setColorFilter(chatLBubbleColor, /*PorterDuff.Mode.SRC_IN*/ PorterDuff.Mode.MULTIPLY);
        chat_msgInMediaDrawable.setColorFilter(chatLBubbleColor, /*PorterDuff.Mode.SRC_IN*/ PorterDuff.Mode.MULTIPLY);
        chat_msgInSelectedDrawable.setColorFilter(lBubbleSColor, PorterDuff.Mode.SRC_IN);
        chat_msgInMediaSelectedDrawable.setColorFilter(lBubbleSColor, PorterDuff.Mode.SRC_IN);

        //if(chatBubbleStyleVal > 0)chat_msgOutShadowDrawable.setColorFilter(chatRBubbleColor, PorterDuff.Mode.SRC_IN);
        //chat_msgOutMediaShadowDrawable.setColorFilter(chatRBubbleColor, PorterDuff.Mode.SRC_IN);
        //chat_msgInShadowDrawable.setColorFilter(chatLBubbleColor, PorterDuff.Mode.SRC_IN);
        //chat_msgInMediaShadowDrawable.setColorFilter(chatLBubbleColor, PorterDuff.Mode.SRC_IN);
    }

    /*public static void updateChatChecksColor(){
        chat_msgOutCheckDrawable.setColorFilter(chatChecksColor, PorterDuff.Mode.SRC_IN);
        chat_msgOutHalfCheckDrawable.setColorFilter(chatChecksColor, PorterDuff.Mode.SRC_IN);
        chat_msgOutClockDrawable.setColorFilter(chatChecksColor, PorterDuff.Mode.SRC_IN);
        chat_msgMediaCheckDrawable.setColorFilter(chatChecksColor, PorterDuff.Mode.MULTIPLY);
        chat_msgMediaHalfCheckDrawable.setColorFilter(chatChecksColor, PorterDuff.Mode.MULTIPLY);
    }*/

    public static void updateChatsColors(){
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        chatsTabsBGColor = themePrefs.getInt("chatsTabsBGColor", defColor);
        chatsFavIndicatorColor = themePrefs.getInt("chatsFavIndicatorColor", FAV_INDICATOR_COLOR_DEF);
        chatsTabsToBottom = themePrefs.getBoolean("chatsTabsToBottom", false);
        chatsHeaderColor = themePrefs.getInt("chatsHeaderColor", defColor);
        chatsHeaderGradient = themePrefs.getInt("chatsHeaderGradient", 0);
        chatsHeaderGradientColor = Theme.chatsHeaderGradient == 0 ? 0x00000000 : themePrefs.getInt("chatsHeaderGradientColor", Theme.defColor);
        chatsHeaderTitleColor = themePrefs.getInt("chatsHeaderTitleColor", 0xffffffff);
        chatsChecksColor = themePrefs.getInt("chatsChecksColor", defColor);
        chatsNameColor = themePrefs.getInt("chatsNameColor", 0xff212121);
        chatsDividerColor = themePrefs.getInt("chatsDividerColor", 0xffdcdcdc);
        chatsUnknownNameColor = themePrefs.getInt("chatsUnknownNameColor", chatsNameColor);
        chatsNameSize = themePrefs.getInt("chatsNameSize", AndroidUtilities.isTablet() ? 19 : 17);
        chatsAvatarRadius = themePrefs.getInt("chatsAvatarRadius", AndroidUtilities.isTablet() ? 35 : 32);
        chatsAvatarSize = themePrefs.getInt("chatsAvatarSize", AndroidUtilities.isTablet() ? 55 : 52);
        chatsAvatarMarginLeft = themePrefs.getInt("chatsAvatarMarginLeft", AndroidUtilities.isTablet() ? 13 : 9);
        chatsRowColor = themePrefs.getInt("chatsRowColor", 0xffffffff);
        chatsRowGradient = themePrefs.getInt("chatsRowGradient", 0);
        chatsRowGradientColor = themePrefs.getInt("chatsRowGradientColor", 0xffffffff);
        chatsPinnedMsgBGColor = themePrefs.getInt("chatsPinnedMsgBGColor", chatsRowColor);
        chatsHeaderIconsColor = themePrefs.getInt("chatsHeaderIconsColor", 0xffffffff);
        chatsHeaderTabIconColor = themePrefs.getInt("chatsHeaderTabIconColor", chatsHeaderIconsColor);
        chatsTabsIndicatorColor = chatsHeaderTabIconColor;
        chatsHeaderTabUnselectedIconColor = themePrefs.getInt("chatsHeaderTabUnselectedIconColor", AndroidUtilities.getIntAlphaColor("chatsHeaderTabIconColor", chatsHeaderIconsColor, 0.35f));

        chatsCountSize = themePrefs.getInt("chatsCountSize", AndroidUtilities.isTablet() ? 15 : 13);
        chatsHideStatusIndicator = themePrefs.getBoolean("chatsHideStatusIndicator", false);
        chatsHideHeaderShadow = themePrefs.getBoolean("chatsHideHeaderShadow", true);

        chatsTabCounterSilentBGColor = themePrefs.getInt("chatsHeaderTabCounterSilentBGColor", 0xffb9b9b9);
        chatsTabCounterBGColor = themePrefs.getInt("chatsHeaderTabCounterBGColor", 0xffd32f2f);
        chatsTabCounterColor = themePrefs.getInt("chatsHeaderTabCounterColor", 0xffffffff);
        chatsTabCounterSize = themePrefs.getInt("chatsHeaderTabCounterSize", AndroidUtilities.isTablet() ? 13 : 11);

        chatsTabTitlesMode = themePrefs.getBoolean("chatsTabTitlesMode", false);
        chatsTabsTextSize = themePrefs.getInt("chatsTabsTextSize", 14);
        chatsFloatingBGColor = themePrefs.getInt("chatsFloatingBGColor", defColor);
        chatsFloatingPencilColor = themePrefs.getInt("chatsFloatingPencilColor", 0xffffffff);
        chatsMessageColor = themePrefs.getInt("chatsMessageColor", 0xff808080);
        chatsMemberColor = themePrefs.getInt("chatsMemberColor", Theme.darkColor);
        chatsMediaColor = themePrefs.getInt("chatsMediaColor", chatsMemberColor);
    }

    public static void updateContactsColors() {
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        contactsHeaderColor = themePrefs.getInt("contactsHeaderColor", Theme.defColor);
        contactsHeaderTitleColor = themePrefs.getInt( "contactsHeaderTitleColor", 0xffffffff);
        contactsHeaderIconsColor = themePrefs.getInt("contactsHeaderIconsColor", 0xffffffff);
        contactsIconsColor = themePrefs.getInt("contactsIconsColor", 0xff737373);
        contactsRowColor = themePrefs.getInt("contactsRowColor", 0xffffffff);
        contactsNameColor = themePrefs.getInt("contactsNameColor", 0xff000000);
        contactsStatusColor = themePrefs.getInt("contactsStatusColor", 0xffa8a8a8);
        contactsOnlineColor = themePrefs.getInt("contactsOnlineColor", AndroidUtilities.getIntDarkerColor("themeColor",0x15));
        contactsAvatarRadius = themePrefs.getInt("contactsAvatarRadius", AndroidUtilities.isTablet() ? 35 : 32);
        contactsNameSize = themePrefs.getInt("contactsNameSize", AndroidUtilities.isTablet() ? 19 : 17);
        contactsStatusSize = themePrefs.getInt("contactsStatusSize", AndroidUtilities.isTablet() ? 16 : 14);
    }

    public static void updateDrawerColors() {
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        drawerHeaderColor = themePrefs.getInt("drawerHeaderColor", defColor);
        drawerOptionColor = themePrefs.getInt("drawerOptionColor", 0xff444444);
        drawerNameColor = themePrefs.getInt("drawerNameColor", 0xffffffff);
        drawerPhoneColor = themePrefs.getInt("drawerPhoneColor", lightColor);
        drawerAvatarSize = themePrefs.getInt("drawerAvatarSize", 64);
        drawerCenterAvatarCheck = themePrefs.getBoolean("drawerCenterAvatarCheck", false);
        drawerHeaderBGCheck = themePrefs.getBoolean("drawerHeaderBGCheck", false);
        drawerHideBGShadowCheck = themePrefs.getBoolean("drawerHideBGShadowCheck", false);
        drawerIconColor = themePrefs.getInt("drawerIconColor", 0xff737373);
        drawerOptionSize = themePrefs.getInt("drawerOptionSize", AndroidUtilities.isTablet() ? 17 : 15);
    }

    public static void updateProfileColors(){
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        //Header
        profileActionbarColor = themePrefs.getInt("profileHeaderColor", defColor);
        profileActionbarGradientList = themePrefs.getInt("profileHeaderGradient", 0);
        profileActionbarGradientColor = themePrefs.getInt("profileHeaderGradientColor", defColor);
        profileActionbarIconsColor = themePrefs.getInt("profileHeaderIconsColor", 0xffffffff);
        profileActionbarAvatarRadius = themePrefs.getInt("profileAvatarRadius", AndroidUtilities.isTablet() ? 35 : 32);
        profileActionbarNameSize = themePrefs.getInt("profileNameSize", AndroidUtilities.isTablet() ? 20 : 18);
        profileActionbarNameColor = themePrefs.getInt("profileNameColor", 0xffffffff);
        profileActionbarStatusColor = themePrefs.getInt("profileStatusColor", lightColor);
        profileActionbarStatusSize = themePrefs.getInt("profileStatusSize", AndroidUtilities.isTablet() ? 16 : 14);
        //List
        profileRowColor = themePrefs.getInt("profileRowColor", 0xffffffff);
        profileRowGradientList = themePrefs.getInt("profileRowGradient", 0);
        profileRowGradientColor = themePrefs.getInt("profileRowGradientColor", defColor);
        profileRowAvatarRadius = themePrefs.getInt("profileRowAvatarRadius", AndroidUtilities.isTablet() ? 35 : 32);
        profileRowTitleColor = themePrefs.getInt("profileTitleColor", 0xff000000);
        profileRowStatusColor = themePrefs.getInt("profileSummaryColor", 0xff8a8a8a);
        profileRowOnlineColor = themePrefs.getInt("profileOnlineColor", lightColor);
        profileRowIconsColor = themePrefs.getInt("profileIconsColor", 0xff737373);
        profileRowCreatorStarColor = themePrefs.getInt("profileCreatorStarColor", profileRowColor == defColor ? darkColor : defColor);
        profileRowAdminStarColor = themePrefs.getInt("profileAdminStarColor", profileRowColor == 0xff858585 ? 0xffbbbbbb : 0xff858585);
        //profileSummaryColor = themePrefs.getInt("profileSummaryColor", Theme.defColor);
        //
    }

    public static void updatePrefsColors(){
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        //Header
        prefActionbarColor = themePrefs.getInt("prefHeaderColor", defColor);
        prefActionbarTitleColor = themePrefs.getInt("prefHeaderTitleColor", 0xffffffff);
        prefActionbarStatusColor = themePrefs.getInt("prefHeaderStatusColor", lightColor);
        prefActionbarIconsColor = themePrefs.getInt( "prefHeaderIconsColor", 0xffffffff);
        prefAvatarColor = themePrefs.getInt( "prefAvatarColor", darkColor);
        prefAvatarRadius = themePrefs.getInt("prefAvatarRadius", AndroidUtilities.isTablet() ? 35 : 32);
        prefAvatarSize = themePrefs.getInt("prefAvatarSize", AndroidUtilities.isTablet() ? 45 : 42);
        //List
        prefBGColor = themePrefs.getInt( "prefBGColor", 0xffffffff);
        prefShadowColor = themePrefs.getInt("prefShadowColor", 0xfff0f0f0);
        prefSectionColor = themePrefs.getInt("prefSectionColor", defColor);
        prefTitleColor = themePrefs.getInt("prefTitleColor", 0xff212121);
        prefSummaryColor = themePrefs.getInt("prefSummaryColor", 0xff8a8a8a);
        prefDividerColor = themePrefs.getInt("prefDividerColor", 0xffd9d9d9);
    }

    public static void setBubbles(Context context){
        //Log.e("Theme", "setBubbles");
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        String bubble = themePrefs.getString("chatBubbleStyle", bubblesNamesArray[0]);
        if(bubble.equals(bubblesNamesArray[0])){
            chat_msgInDrawable = context.getResources().getDrawable(R.drawable.msg_in).mutate();
            chat_msgInSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_in).mutate();
            chat_msgOutDrawable = context.getResources().getDrawable(R.drawable.msg_out).mutate();
            chat_msgOutSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_out).mutate();
            chat_msgInShadowDrawable = context.getResources().getDrawable(R.drawable.msg_in_shadow).mutate();
            chat_msgOutShadowDrawable = context.getResources().getDrawable(R.drawable.msg_out_shadow).mutate();
            chat_msgInMediaDrawable = context.getResources().getDrawable(R.drawable.msg_photo).mutate();
            chat_msgInMediaSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_photo).mutate();
            chat_msgOutMediaDrawable = context.getResources().getDrawable(R.drawable.msg_photo).mutate();
            chat_msgOutMediaSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_photo).mutate();
            chatBubbleStyleVal = 0;
        } else if(bubble.equals(bubblesNamesArray[1])){
            chat_msgInDrawable = context.getResources().getDrawable(R.drawable.msg_in_2).mutate();
            chat_msgInSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_in_2).mutate();
            chat_msgOutDrawable = context.getResources().getDrawable(R.drawable.msg_out_2).mutate();
            chat_msgOutSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_out_2).mutate();
            chat_msgInMediaDrawable = context.getResources().getDrawable(R.drawable.msg_in_2_photo).mutate();
            chat_msgInMediaSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_in_2_photo).mutate();
            chat_msgOutMediaDrawable = context.getResources().getDrawable(R.drawable.msg_out_2_photo).mutate();
            chat_msgOutMediaSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_out_2_photo).mutate();

            chat_msgInShadowDrawable = context.getResources().getDrawable(R.drawable.msg_in_2).mutate();
            chat_msgOutShadowDrawable = context.getResources().getDrawable(R.drawable.msg_out_2).mutate();

            chatBubbleStyleVal = 1;
        } else if(bubble.equals(bubblesNamesArray[2])){
            chat_msgInDrawable = context.getResources().getDrawable(R.drawable.msg_in_3).mutate();
            chat_msgInSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_in_3).mutate();
            chat_msgOutDrawable = context.getResources().getDrawable(R.drawable.msg_out_3).mutate();
            chat_msgOutSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_out_3).mutate();
            chat_msgInMediaDrawable = context.getResources().getDrawable(R.drawable.msg_in_3_photo).mutate();
            chat_msgInMediaSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_in_3_photo).mutate();
            chat_msgOutMediaDrawable = context.getResources().getDrawable(R.drawable.msg_out_3_photo).mutate();
            chat_msgOutMediaSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_out_3_photo).mutate();

            chat_msgInShadowDrawable = context.getResources().getDrawable(R.drawable.msg_in_3).mutate();
            chat_msgOutShadowDrawable = context.getResources().getDrawable(R.drawable.msg_out_3).mutate();

            chatBubbleStyleVal = 2;
        } else if(bubble.equals(bubblesNamesArray[3])){
            chat_msgInDrawable = context.getResources().getDrawable(R.drawable.msg_in_4).mutate();
            chat_msgInSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_in_4).mutate();
            chat_msgOutDrawable = context.getResources().getDrawable(R.drawable.msg_out_4).mutate();
            chat_msgOutSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_out_4).mutate();
            chat_msgInMediaDrawable = context.getResources().getDrawable(R.drawable.msg_in_4_photo).mutate();
            chat_msgInMediaSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_in_4_photo).mutate();
            chat_msgOutMediaDrawable = context.getResources().getDrawable(R.drawable.msg_out_4_photo).mutate();
            chat_msgOutMediaSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_out_4_photo).mutate();

            chat_msgInShadowDrawable = context.getResources().getDrawable(R.drawable.msg_in_4).mutate();
            chat_msgOutShadowDrawable = context.getResources().getDrawable(R.drawable.msg_out_4).mutate();

            chatBubbleStyleVal = 3;
        } else if(bubble.equals(bubblesNamesArray[4])){
            chat_msgInDrawable = context.getResources().getDrawable(R.drawable.msg_in_5).mutate();
            chat_msgInSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_in_5).mutate();
            chat_msgOutDrawable = context.getResources().getDrawable(R.drawable.msg_out_5).mutate();
            chat_msgOutSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_out_5).mutate();
            chat_msgInMediaDrawable = context.getResources().getDrawable(R.drawable.msg_in_5_photo).mutate();
            chat_msgInMediaSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_in_5_photo).mutate();
            chat_msgOutMediaDrawable = context.getResources().getDrawable(R.drawable.msg_out_5_photo).mutate();
            chat_msgOutMediaSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_out_5_photo).mutate();

            chat_msgInShadowDrawable = context.getResources().getDrawable(R.drawable.msg_in_5).mutate();
            chat_msgOutShadowDrawable = context.getResources().getDrawable(R.drawable.msg_out_5).mutate();

            chatBubbleStyleVal = 4;
        } else if(bubble.equals(bubblesNamesArray[5])){
            chat_msgInDrawable = context.getResources().getDrawable(R.drawable.msg_in_6).mutate();
            chat_msgInSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_in_6).mutate();
            chat_msgOutDrawable = context.getResources().getDrawable(R.drawable.msg_out_6).mutate();
            chat_msgOutSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_out_6).mutate();
            chat_msgInMediaDrawable = context.getResources().getDrawable(R.drawable.msg_in_6_photo).mutate();
            chat_msgInMediaSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_in_6_photo).mutate();
            chat_msgOutMediaDrawable = context.getResources().getDrawable(R.drawable.msg_out_6_photo).mutate();
            chat_msgOutMediaSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_out_6_photo).mutate();

            chat_msgInShadowDrawable = context.getResources().getDrawable(R.drawable.msg_in_6).mutate();
            chat_msgOutShadowDrawable = context.getResources().getDrawable(R.drawable.msg_out_6).mutate();

            chatBubbleStyleVal = 5;
        } else if(bubble.equals(bubblesNamesArray[6])){
            chat_msgInDrawable = context.getResources().getDrawable(R.drawable.msg_in_7).mutate();
            chat_msgInSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_in_7).mutate();
            chat_msgOutDrawable = context.getResources().getDrawable(R.drawable.msg_out_7).mutate();
            chat_msgOutSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_out_7).mutate();
            chat_msgInMediaDrawable = context.getResources().getDrawable(R.drawable.msg_in_7_photo).mutate();
            chat_msgInMediaSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_in_7_photo).mutate();
            chat_msgOutMediaDrawable = context.getResources().getDrawable(R.drawable.msg_out_7_photo).mutate();
            chat_msgOutMediaSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_out_7_photo).mutate();

            chat_msgInShadowDrawable = context.getResources().getDrawable(R.drawable.msg_in_7).mutate();
            chat_msgOutShadowDrawable = context.getResources().getDrawable(R.drawable.msg_out_7).mutate();

            chatBubbleStyleVal = 6;
        } else if(bubble.equals(bubblesNamesArray[7])){
            chat_msgInDrawable = context.getResources().getDrawable(R.drawable.msg_in_8).mutate();
            chat_msgInSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_in_8).mutate();
            chat_msgOutDrawable = context.getResources().getDrawable(R.drawable.msg_out_8).mutate();
            chat_msgOutSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_out_8).mutate();
            chat_msgInMediaDrawable = context.getResources().getDrawable(R.drawable.msg_in_8_photo).mutate();
            chat_msgInMediaSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_in_8_photo).mutate();
            chat_msgOutMediaDrawable = context.getResources().getDrawable(R.drawable.msg_out_8_photo).mutate();
            chat_msgOutMediaSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_out_8_photo).mutate();

            chat_msgInShadowDrawable = context.getResources().getDrawable(R.drawable.msg_in_8).mutate();
            chat_msgOutShadowDrawable = context.getResources().getDrawable(R.drawable.msg_out_8).mutate();

            chatBubbleStyleVal = 7;
        } else if(bubble.equals(bubblesNamesArray[8])){
            chat_msgInDrawable = context.getResources().getDrawable(R.drawable.msg_in_9).mutate();
            chat_msgInSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_in_9).mutate();
            chat_msgOutDrawable = context.getResources().getDrawable(R.drawable.msg_out_9).mutate();
            chat_msgOutSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_out_9).mutate();
            chat_msgInMediaDrawable = context.getResources().getDrawable(R.drawable.msg_in_9_photo).mutate();
            chat_msgInMediaSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_in_9_photo).mutate();
            chat_msgOutMediaDrawable = context.getResources().getDrawable(R.drawable.msg_out_9_photo).mutate();
            chat_msgOutMediaSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_out_9_photo).mutate();

            chat_msgInShadowDrawable = context.getResources().getDrawable(R.drawable.msg_in_9).mutate();
            chat_msgOutShadowDrawable = context.getResources().getDrawable(R.drawable.msg_out_9).mutate();

            chatBubbleStyleVal = 8;
        } else if(bubble.equals(bubblesNamesArray[9])){
            chat_msgInDrawable = context.getResources().getDrawable(R.drawable.msg_in_10).mutate();
            chat_msgInSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_in_10).mutate();
            chat_msgOutDrawable = context.getResources().getDrawable(R.drawable.msg_out_10).mutate();
            chat_msgOutSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_out_10).mutate();
            chat_msgInMediaDrawable = context.getResources().getDrawable(R.drawable.msg_in_10_photo).mutate();
            chat_msgInMediaSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_in_10_photo).mutate();
            chat_msgOutMediaDrawable = context.getResources().getDrawable(R.drawable.msg_out_10_photo).mutate();
            chat_msgOutMediaSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_out_10_photo).mutate();

            chat_msgInShadowDrawable = context.getResources().getDrawable(R.drawable.msg_in_10).mutate();
            chat_msgOutShadowDrawable = context.getResources().getDrawable(R.drawable.msg_out_10).mutate();

            chatBubbleStyleVal = 9;
        } else if(bubble.equals(bubblesNamesArray[10])){
            chat_msgInDrawable = context.getResources().getDrawable(R.drawable.msg_in_11).mutate();
            chat_msgInSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_in_11).mutate();
            chat_msgOutDrawable = context.getResources().getDrawable(R.drawable.msg_out_11).mutate();
            chat_msgOutSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_out_11).mutate();
            chat_msgInMediaDrawable = context.getResources().getDrawable(R.drawable.msg_in_11_photo).mutate();
            chat_msgInMediaSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_in_11_photo).mutate();
            chat_msgOutMediaDrawable = context.getResources().getDrawable(R.drawable.msg_out_11_photo).mutate();
            chat_msgOutMediaSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_out_11_photo).mutate();

            chat_msgInShadowDrawable = context.getResources().getDrawable(R.drawable.msg_in_11).mutate();
            chat_msgOutShadowDrawable = context.getResources().getDrawable(R.drawable.msg_out_11).mutate();

            chatBubbleStyleVal = 10;
        } else if(bubble.equals(bubblesNamesArray[11])){
            chat_msgInDrawable = context.getResources().getDrawable(R.drawable.msg_in_12).mutate();
            chat_msgInSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_in_12).mutate();
            chat_msgOutDrawable = context.getResources().getDrawable(R.drawable.msg_out_12).mutate();
            chat_msgOutSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_out_12).mutate();
            chat_msgInMediaDrawable = context.getResources().getDrawable(R.drawable.msg_in_12_photo).mutate();
            chat_msgInMediaSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_in_12_photo).mutate();
            chat_msgOutMediaDrawable = context.getResources().getDrawable(R.drawable.msg_out_12_photo).mutate();
            chat_msgOutMediaSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_out_12_photo).mutate();

            chat_msgInShadowDrawable = context.getResources().getDrawable(R.drawable.msg_in_12).mutate();
            chat_msgOutShadowDrawable = context.getResources().getDrawable(R.drawable.msg_out_12).mutate();

            chatBubbleStyleVal = 11;
        } else if(bubble.equals(bubblesNamesArray[12])){
            chat_msgInDrawable = context.getResources().getDrawable(R.drawable.msg_in_13).mutate();
            chat_msgInSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_in_13).mutate();
            chat_msgOutDrawable = context.getResources().getDrawable(R.drawable.msg_out_13).mutate();
            chat_msgOutSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_out_13).mutate();
            chat_msgInMediaDrawable = context.getResources().getDrawable(R.drawable.msg_in_13_photo).mutate();
            chat_msgInMediaSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_in_13_photo).mutate();
            chat_msgOutMediaDrawable = context.getResources().getDrawable(R.drawable.msg_out_13_photo).mutate();
            chat_msgOutMediaSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_out_13_photo).mutate();

            chat_msgInShadowDrawable = context.getResources().getDrawable(R.drawable.msg_in_13).mutate();
            chat_msgOutShadowDrawable = context.getResources().getDrawable(R.drawable.msg_out_13).mutate();

            chatBubbleStyleVal = 12;
        }  else{
            chat_msgInDrawable = context.getResources().getDrawable(R.drawable.msg_in).mutate();
            chat_msgInSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_in).mutate();
            chat_msgOutDrawable = context.getResources().getDrawable(R.drawable.msg_out).mutate();
            chat_msgOutSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_out).mutate();
            chat_msgInMediaDrawable = context.getResources().getDrawable(R.drawable.msg_photo).mutate();
            chat_msgInMediaSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_photo).mutate();
            chat_msgOutMediaDrawable = context.getResources().getDrawable(R.drawable.msg_photo).mutate();
            chat_msgOutMediaSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_photo).mutate();

            chat_msgInShadowDrawable = context.getResources().getDrawable(R.drawable.msg_in_shadow).mutate();
            chat_msgOutShadowDrawable = context.getResources().getDrawable(R.drawable.msg_out_shadow).mutate();

            chatBubbleStyleVal = 0;
        }
        chatBubbleStyle = bubblesNamesArray[chatBubbleStyleVal];
        //updateChatDrawablesColor();
    }

    public static void setChecks(Context context) {
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        String check = themePrefs.getString("chatCheckStyle", checksNamesArray[0]);
        //Log.e("Theme", "setChecks check:" + check);
        if (check.equals(checksNamesArray[1])) {

            chat_msgOutCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_2).mutate();
            chat_msgOutCheckSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_2).mutate();
            chat_msgMediaCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_2).mutate();
            chat_msgStickerCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_2).mutate();

            chat_msgOutHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_2).mutate();
            chat_msgOutHalfCheckSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_2).mutate();
            chat_msgMediaHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_2).mutate();
            chat_msgStickerHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_2).mutate();

            chatCheckStyleVal = 1;
        } else if (check.equals(checksNamesArray[2])) {

            chat_msgOutCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_3).mutate();
            chat_msgOutCheckSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_3).mutate();
            chat_msgMediaCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_3).mutate();
            chat_msgStickerCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_3).mutate();

            chat_msgOutHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_3).mutate();
            chat_msgOutHalfCheckSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_3).mutate();
            chat_msgMediaHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_3).mutate();
            chat_msgStickerHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_3).mutate();

            chatCheckStyleVal = 2;
        } else if (check.equals(checksNamesArray[3])) {

            chat_msgOutCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_4).mutate();
            chat_msgOutCheckSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_4).mutate();
            chat_msgMediaCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_4).mutate();
            chat_msgStickerCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_4).mutate();

            chat_msgOutHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_4).mutate();
            chat_msgOutHalfCheckSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_4).mutate();
            chat_msgMediaHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_4).mutate();
            chat_msgStickerHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_4).mutate();

            chatCheckStyleVal = 3;
        } else if (check.equals(checksNamesArray[4])) {

            chat_msgOutCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_5).mutate();
            chat_msgOutCheckSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_5).mutate();
            chat_msgMediaCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_5).mutate();
            chat_msgStickerCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_5).mutate();

            chat_msgOutHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_5).mutate();
            chat_msgOutHalfCheckSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_5).mutate();
            chat_msgMediaHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_5).mutate();
            chat_msgStickerHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_5).mutate();

            chatCheckStyleVal = 4;
        } else if (check.equals(checksNamesArray[5])) {

            chat_msgOutCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_6).mutate();
            chat_msgOutCheckSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_6).mutate();
            chat_msgMediaCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_6).mutate();
            chat_msgStickerCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_6).mutate();

            chat_msgOutHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_6).mutate();
            chat_msgOutHalfCheckSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_6).mutate();
            chat_msgMediaHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_6).mutate();
            chat_msgStickerHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_6).mutate();

            chatCheckStyleVal = 5;
        } else if (check.equals(checksNamesArray[6])) {


            chat_msgOutCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_7).mutate();
            chat_msgOutCheckSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_7).mutate();
            chat_msgMediaCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_7).mutate();
            chat_msgStickerCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_7).mutate();

            chat_msgOutHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_7).mutate();
            chat_msgOutHalfCheckSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_7).mutate();
            chat_msgMediaHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_7).mutate();
            chat_msgStickerHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_7).mutate();

            chatCheckStyleVal = 6;
        } else if (check.equals(checksNamesArray[7])) {


            chat_msgOutCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_8).mutate();
            chat_msgOutCheckSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_8).mutate();
            chat_msgMediaCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_8).mutate();
            chat_msgStickerCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_8).mutate();

            chat_msgOutHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_8).mutate();
            chat_msgOutHalfCheckSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_8).mutate();
            chat_msgMediaHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_8).mutate();
            chat_msgStickerHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_8).mutate();

            chatCheckStyleVal = 7;
        } else if (check.equals(checksNamesArray[8])) {


            chat_msgOutCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_9).mutate();
            chat_msgOutCheckSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_9).mutate();
            chat_msgMediaCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_9).mutate();
            chat_msgStickerCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_9).mutate();

            chat_msgOutHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_9).mutate();
            chat_msgOutHalfCheckSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_9).mutate();
            chat_msgMediaHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_9).mutate();
            chat_msgStickerHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_9).mutate();

            chatCheckStyleVal = 8;
        } else if (check.equals(checksNamesArray[9])) {


            chat_msgOutCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_10).mutate();
            chat_msgOutCheckSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_10).mutate();
            chat_msgMediaCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_10).mutate();
            chat_msgStickerCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_10).mutate();

            chat_msgOutHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_10).mutate();
            chat_msgOutHalfCheckSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_10).mutate();
            chat_msgMediaHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_10).mutate();
            chat_msgStickerHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_10).mutate();

            chatCheckStyleVal = 9;
        } else if (check.equals(checksNamesArray[10])) {


            chat_msgOutCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_11).mutate();
            chat_msgOutCheckSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_11).mutate();
            chat_msgMediaCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_11).mutate();
            chat_msgStickerCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_11).mutate();

            chat_msgOutHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_11).mutate();
            chat_msgOutHalfCheckSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_11).mutate();
            chat_msgMediaHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_11).mutate();
            chat_msgStickerHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_11).mutate();

            chatCheckStyleVal = 10;
        } else if (check.equals(checksNamesArray[11])) {
            chat_msgOutCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_12).mutate();
            chat_msgOutCheckSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_12).mutate();
            chat_msgMediaCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_12).mutate();
            chat_msgStickerCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check_w_12).mutate();

            chat_msgOutHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_12).mutate();
            chat_msgOutHalfCheckSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_12).mutate();
            chat_msgMediaHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_12).mutate();
            chat_msgStickerHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck_w_12).mutate();

            chatCheckStyleVal = 11;
        } else {

            chat_msgOutCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check).mutate();
            chat_msgOutCheckSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_check).mutate();
            chat_msgMediaCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check).mutate();
            chat_msgStickerCheckDrawable = context.getResources().getDrawable(R.drawable.msg_check).mutate();

            chat_msgOutHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck).mutate();
            chat_msgOutHalfCheckSelectedDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck).mutate();
            chat_msgMediaHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck).mutate();
            chat_msgStickerHalfCheckDrawable = context.getResources().getDrawable(R.drawable.msg_halfcheck).mutate();

            chatCheckStyleVal = 0;
        }
        setDialogsChecks(context.getResources());
        chatCheckStyle = checksNamesArray[chatCheckStyleVal];
        //updateChatChecksColor();
    }

    public static void updatePlusPrefs(){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
        //Chat
        plusShowDSBtnUsers = preferences.getBoolean("showDSBtnUsers", false);
        plusShowDSBtnGroups = preferences.getBoolean("showDSBtnGroups", true);
        plusShowDSBtnSGroups = preferences.getBoolean("showDSBtnSGroups", true);
        plusShowDSBtnChannels = preferences.getBoolean("showDSBtnChannels", true);
        plusShowDSBtnBots = preferences.getBoolean("showDSBtnBots", true);
        plusShowEditedMark = preferences.getBoolean("showEditedMark", true);
        plusShowPhotoQualityBar = preferences.getBoolean("showPhotoQualityBar", true);
        plusPhotoQuality = preferences.getInt("photoQuality", 80);
        //plusPhotoMaxSize = preferences.getInt("photoMaxSize", 1280);
        plusPhotoViewerHideStatusBar = preferences.getBoolean("photoViewerHideStatusBar", false);

        plusShowTypingToast = preferences.getBoolean("showTypingToast", false);
        plusShowOnlineToast = preferences.getBoolean("showOnlineToast", false);
        plusShowOnlyIfContactFav = preferences.getBoolean("showOnlyIfContactFav", true);
        plusShowOfflineToast = preferences.getBoolean("showOfflineToast", false);
        plusToastNotificationSize = preferences.getInt("toastNotificationSize", 13);
        plusToastNotificationPadding = preferences.getInt("toastNotificationPadding", 0);
        plusToastNotificationToBottom = preferences.getBoolean("toastNotificationToBottom", false);
        plusToastNotificationPosition = preferences.getInt("toastNotificationPosition", 1);
        plusEnableDirectReply = preferences.getBoolean("enableDirectReply", true);
        plusShowQuickBar = preferences.getBoolean("showQuickBar", true);
        plusVerticalQuickBar = preferences.getBoolean("verticalQuickBar", true);
        plusAlwaysBackToMain = preferences.getBoolean("alwaysBackToMain", false);
        plusDoNotCloseQuickBar = preferences.getBoolean("doNotCloseQuickBar", false);
        plusHideQuickBarOnScroll = preferences.getBoolean("hideQuickBarOnScroll", true);
        plusCenterQuickBarBtn = preferences.getBoolean("centerQuickBarBtn", false);
        plusQuickBarDialogType = preferences.getInt("quickBarDialogType", 0);
        plusQuickBarShowMembers = preferences.getBoolean("quickBarShowMembers", false);
        plusSaveToCloudQuote = preferences.getBoolean("saveToCloudQuote", true);
        plusSwipeToReply = preferences.getBoolean("plusSwipeToReply", true);
        plusHideNotificationsIfPlaying = preferences.getBoolean("hideNotificationsIfPlaying", false);
        AndroidUtilities.playingAGame = false;
        plusHideInstantCamera = preferences.getBoolean("hideInstantCamera", false);
        plusDoNotHideStickersTab = preferences.getBoolean("doNotHideStickersTab", false);

        plusSortAll = preferences.getInt("sortAll", 0);
        plusSortUsers = preferences.getInt("sortUsers", 0);
        plusSortGroups = preferences.getInt("sortGroups", 0);
        plusSortSuperGroups = preferences.getInt("sortSGroups", 0);
        plusSortChannels = preferences.getInt("sortChannels", 0);
        plusSortBots = preferences.getInt("sortBots", 0);
        plusSortFavs = preferences.getInt("sortFavs", 0);
        plusSortAdmin = preferences.getInt("sortAdmin", 0);
        plusSortUnread = preferences.getInt("sortUnread", 0);
        plusDrawSingleBigEmoji = preferences.getBoolean("drawSingleBigEmoji", false);
        plusEnableMarkdown = preferences.getBoolean("enableMarkdown", false);
        plusShowUnmutedFirst = preferences.getBoolean("showUnmutedFirst", false);
        //Dialogs
        plusHideTabs = preferences.getBoolean("hideTabs", false);
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        /*if(themePrefs.contains("chatsTabsToBottom")){
            plusTabsToBottom = chatsTabsToBottom;
        } else{
            plusTabsToBottom = preferences.getBoolean("tabsToBottom", false);
        }*/
        plusTabsToBottom = themePrefs.contains("chatsTabsToBottom") ? chatsTabsToBottom : preferences.getBoolean("tabsToBottom", false);
        plusHideTabsSelector = preferences.getBoolean("hideSelectedTabIndicator", false);
        plusHideTabsCounters = preferences.getBoolean("hideTabsCounters", false);

        plusProfileEnableGoToMsg = preferences.getBoolean("profileEnableGoToMsg", false);
        plusChatsToLoad = preferences.getInt("chatsToLoad", 100);
        plusTabsHeight = preferences.getInt("tabsHeight", AndroidUtilities.isTablet() ? 42 : 40);
        plusDialogType = preferences.getInt("dialogType", 0);
        plusDefaultTab = preferences.getInt("defaultTab", -1);
        plusSelectedTab = plusDefaultTab != -1 ? plusDefaultTab : preferences.getInt("selectedTab", 0);
        plusHideAllTab = preferences.getBoolean("hideAllTab", false);
        plusHideUsersTab = preferences.getBoolean("hideUsers", false);
        plusHideGroupsTab = preferences.getBoolean("hideGroups", false);
        plusHideSuperGroupsTab = preferences.getBoolean("hideSGroups", false);
        plusHideChannelsTab = preferences.getBoolean("hideChannels", false);
        plusHideBotsTab = preferences.getBoolean("hideBots", false);
        plusHideFavsTab = preferences.getBoolean("hideFavs", false);
        plusHideAdminTab = preferences.getBoolean("hideAdmin", false);
        plusHideUnreadTab = preferences.getBoolean("hideUnread", false);
        plusShowAllInAdminTab = preferences.getBoolean("showAllInAdminTab", false);
        //plusTabTitlesMode = preferences.getBoolean("tabTitlesMode", false);
        plusTabTitlesMode = themePrefs.contains("chatsTabTitlesMode") ? chatsTabTitlesMode : preferences.getBoolean("tabTitlesMode", false);
        //plusTabsTextSize = preferences.getInt("tabsTextSize", 14);
        plusTabsTextSize = themePrefs.contains("chatsTabsTextSize") ? chatsTabsTextSize : preferences.getInt("tabsTextSize", 14);
        plusTabsShouldExpand = preferences.getBoolean("tabsShouldExpand", true);
        plusDisableTabsAnimation = preferences.getBoolean("disableTabsAnimation", true);
        plusDisableTabsScrolling = preferences.getBoolean("disableTabsScrolling", false);
        plusTabsCountersCountChats = preferences.getBoolean("tabsCountersCountChats", false);
        plusTabsCountersCountNotMuted = preferences.getBoolean("tabsCountersCountNotMuted", false);
        plusInfiniteTabsSwipe = preferences.getBoolean("infiniteTabsSwipe", false);
        plusDoNotChangeHeaderTitle = preferences.getBoolean("doNotChangeHeaderTitle", false);
        plusLimitTabsCounters = preferences.getBoolean("limitTabsCounters", true);
        plusHideMobile = preferences.getBoolean("hideMobile", false);
        plusShowUsername = preferences.getBoolean("showUsername", false);
        plusMoveVersionToSettings = preferences.getBoolean("moveVersionToSettings", false);
        plusShowUserBio = preferences.getBoolean("showUserBio", true);
    }

    public static String[] bubblesNamesArray ={
            "Telegram",
            "Lex",
            "Hangouts",
            "Notepad",
            "Ed",
            "Edge",
            "iOS",
            "Telegram_old",
            "OvaLex",
            "MaxSquare",
            "MaxLines",
            "LineFineLex",
            "PictuLineLex"
    };

    public static Integer[] imgid ={
            R.drawable.msg_in,
            R.drawable.msg_in_2,
            R.drawable.msg_in_3,
            R.drawable.msg_in_4,
            R.drawable.msg_in_5,
            R.drawable.msg_in_6,
            R.drawable.msg_in_7,
            R.drawable.msg_in_8,
            R.drawable.msg_in_9_photo,
            R.drawable.msg_in_10,
            R.drawable.msg_in_11,
            R.drawable.msg_in_12,
            R.drawable.msg_in_13,
            R.drawable.msg_out,
            R.drawable.msg_out_2,
            R.drawable.msg_out_3,
            R.drawable.msg_out_4,
            R.drawable.msg_out_5,
            R.drawable.msg_out_6,
            R.drawable.msg_out_7,
            R.drawable.msg_out_8,
            R.drawable.msg_out_9_photo,
            R.drawable.msg_out_10,
            R.drawable.msg_out_11,
            R.drawable.msg_out_12,
            R.drawable.msg_out_13
    };

    public static String[] checksNamesArray ={
            "Stock",
            "EdCheck",
            "Lex",
            "Gladiator",
            "MaxChecks",
            "ElipLex",
            "CubeLex",
            "MaxLines",
            "RLex",
            "MaxLinesPro",
            "ReadLex",
            "MaxHeart"
    };

    public static Integer[] checkid ={
            R.drawable.list_check,
            R.drawable.dialogs_check_2,
            R.drawable.dialogs_check_3,
            R.drawable.dialogs_check_4,
            R.drawable.dialogs_check_5,
            R.drawable.dialogs_check_6,
            R.drawable.dialogs_check_7,
            R.drawable.dialogs_check_8,
            R.drawable.dialogs_check_9,
            R.drawable.dialogs_check_10,
            R.drawable.dialogs_check_11,
            R.drawable.dialogs_check_12,
            R.drawable.list_halfcheck,
            R.drawable.dialogs_halfcheck_2,
            R.drawable.dialogs_halfcheck_3,
            R.drawable.dialogs_halfcheck_4,
            R.drawable.dialogs_halfcheck_5,
            R.drawable.dialogs_halfcheck_6,
            R.drawable.dialogs_halfcheck_7,
            R.drawable.dialogs_halfcheck_8,
            R.drawable.dialogs_halfcheck_9,
            R.drawable.dialogs_halfcheck_10,
            R.drawable.dialogs_halfcheck_11,
            R.drawable.dialogs_halfcheck_12,
            R.drawable.msg_check,
            R.drawable.msg_check_w_2,
            R.drawable.msg_check_w_3,
            R.drawable.msg_check_w_4,
            R.drawable.msg_check_w_5,
            R.drawable.msg_check_w_6,
            R.drawable.msg_check_w_7,
            R.drawable.msg_check_w_8,
            R.drawable.msg_check_w_9,
            R.drawable.msg_check_w_10,
            R.drawable.msg_check_w_11,
            R.drawable.msg_check_w_12,
            R.drawable.msg_halfcheck,
            R.drawable.msg_halfcheck_w_2,
            R.drawable.msg_halfcheck_w_3,
            R.drawable.msg_halfcheck_w_4,
            R.drawable.msg_halfcheck_w_5,
            R.drawable.msg_halfcheck_w_6,
            R.drawable.msg_halfcheck_w_7,
            R.drawable.msg_halfcheck_w_8,
            R.drawable.msg_halfcheck_w_9,
            R.drawable.msg_halfcheck_w_10,
            R.drawable.msg_halfcheck_w_11,
            R.drawable.msg_halfcheck_w_12,
            R.drawable.msg_check_w,
            R.drawable.msg_check_w_2,
            R.drawable.msg_check_w_3,
            R.drawable.msg_check_w_4,
            R.drawable.msg_check_w_5,
            R.drawable.msg_check_w_6,
            R.drawable.msg_check_w_7,
            R.drawable.msg_check_w_8,
            R.drawable.msg_check_w_9,
            R.drawable.msg_check_w_10,
            R.drawable.msg_check_w_11,
            R.drawable.msg_check_w_12,
            R.drawable.msg_halfcheck_w,
            R.drawable.msg_halfcheck_w_2,
            R.drawable.msg_halfcheck_w_3,
            R.drawable.msg_halfcheck_w_4,
            R.drawable.msg_halfcheck_w_5,
            R.drawable.msg_halfcheck_w_6,
            R.drawable.msg_halfcheck_w_7,
            R.drawable.msg_halfcheck_w_8,
            R.drawable.msg_halfcheck_w_9,
            R.drawable.msg_halfcheck_w_10,
            R.drawable.msg_halfcheck_w_11,
            R.drawable.msg_halfcheck_w_12
    };

    public static final int[] tabIcons = {
            R.drawable.tab_all,
            R.drawable.tab_user,
            R.drawable.tab_group,
            R.drawable.tab_supergroup,
            R.drawable.tab_channel,
            R.drawable.tab_bot,
            R.drawable.tab_favs,
            R.drawable.tab_admin,
            R.drawable.tab_unread};

    public static final String[] tabTitles = {
            LocaleController.getString("All", R.string.All),
            LocaleController.getString("Users", R.string.Users),
            LocaleController.getString("Groups", R.string.Groups),
            LocaleController.getString("SuperGroups", R.string.SuperGroups),
            LocaleController.getString("Channels", R.string.Channels),
            LocaleController.getString("Bots", R.string.Bots),
            LocaleController.getString("Favorites", R.string.Favorites),
            LocaleController.getString("ChannelEditor", R.string.ChannelEditor),
            LocaleController.getString("Unread", R.string.Unread)};

    public static int[] tabType = {0, 3, 4, 7, 5, 6, 8, 10, 11};
	//
}
