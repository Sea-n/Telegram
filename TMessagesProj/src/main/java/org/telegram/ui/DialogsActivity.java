/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package org.telegram.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.VideoEditedInfo;
import org.telegram.messenger.query.SearchQuery;
import org.telegram.messenger.query.StickersQuery;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.MenuDrawable;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Adapters.DialogsAdapter;
import org.telegram.ui.Adapters.DialogsSearchAdapter;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Cells.DrawerActionCell;
import org.telegram.ui.Cells.DrawerProfileCell;
import org.telegram.ui.Cells.GraySectionCell;
import org.telegram.ui.Cells.HashtagSearchCell;
import org.telegram.ui.Cells.HintDialogCell;
import org.telegram.ui.Cells.LoadingCell;
import org.telegram.ui.Cells.ProfileSearchCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.Favorite;
import org.telegram.ui.Components.FragmentContextView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.TabsView;

import java.util.ArrayList;
import java.util.List;

public class DialogsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, PhotoViewer.PhotoViewerProvider {
    
    private RecyclerListView listView;
    private LinearLayoutManager layoutManager;
    private DialogsAdapter dialogsAdapter;
    private DialogsSearchAdapter dialogsSearchAdapter;
    private EmptyTextProgressView searchEmptyView;
    private RadialProgressView progressView;
    private LinearLayout emptyView;
    private ActionBarMenuItem passcodeItem;
    private ImageView floatingButton;
    private RecyclerView sideMenu;
    private FragmentContextView fragmentContextView;

    private TextView emptyTextView1;
    private TextView emptyTextView2;

    private AlertDialog permissionDialog;

    private int prevPosition;
    private int prevTop;
    private boolean scrollUpdated;
    private boolean floatingHidden;
    private final AccelerateDecelerateInterpolator floatingInterpolator = new AccelerateDecelerateInterpolator();

    private boolean checkPermission = true;

    private String selectAlertString;
    private String selectAlertStringGroup;
    private String addToGroupAlertString;
    private int dialogsType;

    public static boolean dialogsLoaded;
    private boolean searching;
    private boolean searchWas;
    private boolean onlySelect;
    private long selectedDialog;
    private String searchString;
    private long openedDialogId;
	private boolean cantSendToChannels;

    private DialogsActivityDelegate delegate;

    private float touchPositionDP;

    private int user_id = 0;
    private int chat_id = 0;
    private BackupImageView avatarImage;
    private boolean updateTabCounters = false;

    private TabsView newTabsView;

    private boolean tabsHidden;

    private DialogsOnTouch onTouchListener = null;

    public interface DialogsActivityDelegate {
        void didSelectDialog(DialogsActivity fragment, long dialog_id, boolean param);
    }

    public DialogsActivity(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        if (getArguments() != null) {
            onlySelect = arguments.getBoolean("onlySelect", false);
            cantSendToChannels = arguments.getBoolean("cantSendToChannels", false);			
            dialogsType = arguments.getInt("dialogsType", 0);
            selectAlertString = arguments.getString("selectAlertString");
            selectAlertStringGroup = arguments.getString("selectAlertStringGroup");
            addToGroupAlertString = arguments.getString("addToGroupAlertString");
        }

        if (searchString == null) {
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.dialogsNeedReload);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.encryptedChatUpdated);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.contactsDidLoaded);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.appDidLogout);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.openedChatChanged);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.notificationsSettingsUpdated);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageReceivedByAck);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageReceivedByServer);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageSendError);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.didSetPasscode);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.needReloadRecentDialogsSearch);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.didLoadedReplyMessages);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.reloadHints);
            //plus
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.refreshTabs);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateDialogsTheme);
        }


        if (!dialogsLoaded) {
            MessagesController.getInstance().loadDialogs(0, Theme.plusChatsToLoad, true);
            ContactsController.getInstance().checkInviteText();
            MessagesController.getInstance().loadPinnedDialogs(0, null);
			StickersQuery.checkFeaturedStickers();
            dialogsLoaded = true;
        }
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (searchString == null) {
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.dialogsNeedReload);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.encryptedChatUpdated);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.contactsDidLoaded);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.appDidLogout);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.openedChatChanged);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.notificationsSettingsUpdated);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageReceivedByAck);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageReceivedByServer);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageSendError);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didSetPasscode);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.needReloadRecentDialogsSearch);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didLoadedReplyMessages);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.reloadHints);
            //plus
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.refreshTabs);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateDialogsTheme);
        }
        delegate = null;
    }
    //plus
    void resetViews(){
        PhotoViewer.getInstance().destroyPhotoViewer();
        if(avatarImage != null){
            avatarImage = null;
        }
    }
    //
    @Override
    public View createView(final Context context) {
        searching = false;
        searchWas = false;

        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                Theme.createChatResources(context, false);
            }
        });


	    SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        int tColor = themePrefs.getInt("chatsHeaderTitleColor", 0xffffffff);
        if(Theme.usePlusTheme){
            avatarImage = new BackupImageView(context);
            avatarImage.setRoundRadius(AndroidUtilities.dp(30));
        }

        ActionBarMenu menu = actionBar.createMenu();
        if (!onlySelect && searchString == null) {
            passcodeItem = menu.addItem(1, R.drawable.lock_close);
            //plus
            passcodeItem.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View view) {
                    if (UserConfig.passcodeHash.length() > 0) {
                        presentFragment(new PasscodeActivity(2));
                    } else {
                        presentFragment(new PasscodeActivity(0));
                    }
                    return true;
                }
            });
            //
            updatePasscodeButton();
         }
         final ActionBarMenuItem item = menu.addItem(0, R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {
            @Override
            public void onSearchExpand() {
                //plus
                refreshTabAndListViews(true);
                //
                searching = true;
                if (listView != null) {
                    if (searchString != null) {
                        listView.setEmptyView(searchEmptyView);
                        progressView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.GONE);
                    }
                    if (!onlySelect) {
                        floatingButton.setVisibility(View.GONE);
                    }
                }
                updatePasscodeButton();
            }

            @Override
            public boolean canCollapseSearch() {
                if (searchString != null) {
                    finishFragment();
                    return false;
                }
                return true;
            }

            @Override
            public void onSearchCollapse() {
                //plus
                refreshTabAndListViews(false);
                //
                searching = false;
                searchWas = false;
                if (listView != null) {
                    searchEmptyView.setVisibility(View.GONE);
                    if (MessagesController.getInstance().loadingDialogs && MessagesController.getInstance().dialogs.isEmpty()) {
                        emptyView.setVisibility(View.GONE);
                        listView.setEmptyView(progressView);
                    } else {
                        progressView.setVisibility(View.GONE);
                        listView.setEmptyView(emptyView);
                    }
                    if (!onlySelect) {
                        floatingButton.setVisibility(View.VISIBLE);
                        floatingHidden = true;
                        floatingButton.setTranslationY(AndroidUtilities.dp(Theme.plusTabsToBottom ? 150 : 100));
                        hideFloatingButton(false);
                    }
                    if (listView.getAdapter() != dialogsAdapter) {
                        listView.setAdapter(dialogsAdapter);
                        dialogsAdapter.notifyDataSetChanged();
                    }
                }
                if (dialogsSearchAdapter != null) {
                    dialogsSearchAdapter.searchDialogs(null);
                }
                updatePasscodeButton();
            }

            @Override
            public void onTextChanged(EditText editText) {
                String text = editText.getText().toString();
                if (text.length() != 0 || dialogsSearchAdapter != null && dialogsSearchAdapter.hasRecentRearch()) {
                    searchWas = true;
                    if (dialogsSearchAdapter != null && listView.getAdapter() != dialogsSearchAdapter) {
                        listView.setAdapter(dialogsSearchAdapter);
                        dialogsSearchAdapter.notifyDataSetChanged();
                    }
                    if (searchEmptyView != null && listView.getEmptyView() != searchEmptyView) {
                        emptyView.setVisibility(View.GONE);
                        progressView.setVisibility(View.GONE);
                        searchEmptyView.showTextView();
                        listView.setEmptyView(searchEmptyView);
                    }
                }
                if (dialogsSearchAdapter != null) {
                     dialogsSearchAdapter.searchDialogs(text);
                 }
             }
         });
	    item.getSearchField().setHint(LocaleController.getString("Search", R.string.Search));
        if (onlySelect) {
                actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            actionBar.setTitle(LocaleController.getString("SelectChat", R.string.SelectChat));
        } else {
            if (searchString != null) {
                actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            } else {
                actionBar.setBackButtonDrawable(new MenuDrawable());
            }
            //Log.e("DialogsActivity", Theme.plusDoNotChangeHeaderTitle + " dialogsType " + dialogsType);
            actionBar.setTitle(Theme.plusDoNotChangeHeaderTitle ? getHeaderTitle() : getTitle(dialogsType, false));
        }
        actionBar.setAllowOverlayTitle(true);
        actionBar.setCastShadows(Theme.usePlusTheme ? !Theme.chatsHideHeaderShadow : Theme.plusHideTabs);
        if(Theme.usePlusTheme)actionBar.setItemsColor(Theme.chatsHeaderIconsColor, false);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    if (onlySelect) {
                        finishFragment();
                    } else if (parentLayout != null) {
                        parentLayout.getDrawerLayoutContainer().openDrawer(false);
                    }
                } else if (id == 1) {
                    UserConfig.appLocked = !UserConfig.appLocked;
                    UserConfig.saveConfig(false);
                    updatePasscodeButton();
                }
            }
        });

        if (sideMenu != null) {
            sideMenu.setBackgroundColor(Theme.getColor(Theme.key_chats_menuBackground));
            sideMenu.setGlowColor(Theme.getColor(Theme.key_chats_menuBackground));
            sideMenu.getAdapter().notifyDataSetChanged();
        }

        FrameLayout frameLayout = new FrameLayout(context);
        fragmentView = frameLayout;
        
        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(true);
        listView.setItemAnimator(null);
        listView.setInstantClick(true);
        listView.setLayoutAnimation(null);
        listView.setTag(4);
        layoutManager = new LinearLayoutManager(context) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listView.setLayoutManager(layoutManager);
        listView.setVerticalScrollbarPosition(LocaleController.isRTL ? RecyclerListView.SCROLLBAR_POSITION_LEFT : RecyclerListView.SCROLLBAR_POSITION_RIGHT);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        //plus
        onTouchListener = new DialogsOnTouch(context);
        listView.setOnTouchListener(onTouchListener);

        listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (listView == null || listView.getAdapter() == null) {
                    return;
                }
                long dialog_id = 0;
                int message_id = 0;
                RecyclerView.Adapter adapter = listView.getAdapter();
                if (adapter == dialogsAdapter) {
                    TLRPC.TL_dialog dialog = dialogsAdapter.getItem(position);
                    if (dialog == null) {
                        return;
                    }
                    dialog_id = dialog.id;
                } else if (adapter == dialogsSearchAdapter) {
                    Object obj = dialogsSearchAdapter.getItem(position);
                    if (obj instanceof TLRPC.User) {
                        dialog_id = ((TLRPC.User) obj).id;
                        if (dialogsSearchAdapter.isGlobalSearch(position)) {
                            ArrayList<TLRPC.User> users = new ArrayList<>();
                            users.add((TLRPC.User) obj);
                            MessagesController.getInstance().putUsers(users, false);
                            MessagesStorage.getInstance().putUsersAndChats(users, null, false, true);
                        }
                        if (!onlySelect) {
                            dialogsSearchAdapter.putRecentSearch(dialog_id, (TLRPC.User) obj);
                        }
                    } else if (obj instanceof TLRPC.Chat) {
                        if (dialogsSearchAdapter.isGlobalSearch(position)) {
                            ArrayList<TLRPC.Chat> chats = new ArrayList<>();
                            chats.add((TLRPC.Chat) obj);
                            MessagesController.getInstance().putChats(chats, false);
                            MessagesStorage.getInstance().putUsersAndChats(null, chats, false, true);
                        }
                        if (((TLRPC.Chat) obj).id > 0) {
                            dialog_id = -((TLRPC.Chat) obj).id;
                        } else {
                            dialog_id = AndroidUtilities.makeBroadcastId(((TLRPC.Chat) obj).id);
                        }
                        if (!onlySelect) {
                            dialogsSearchAdapter.putRecentSearch(dialog_id, (TLRPC.Chat) obj);
                        }
                    } else if (obj instanceof TLRPC.EncryptedChat) {
                        dialog_id = ((long) ((TLRPC.EncryptedChat) obj).id) << 32;
                        if (!onlySelect) {
                            dialogsSearchAdapter.putRecentSearch(dialog_id, (TLRPC.EncryptedChat) obj);
                        }
                    } else if (obj instanceof MessageObject) {
                        MessageObject messageObject = (MessageObject) obj;
                        dialog_id = messageObject.getDialogId();
                        message_id = messageObject.getId();
                        dialogsSearchAdapter.addHashtagsFromMessage(dialogsSearchAdapter.getLastSearchString());
                    } else if (obj instanceof String) {
                        actionBar.openSearchField((String) obj);
                    }
                }

                if (dialog_id == 0) {
                    return;
                }

                if (touchPositionDP < 65) {
                    SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    //if(preferences.getInt("dialogsClickOnGroupPic", 0) == 2)MessagesController.getInstance().loadChatInfo(chat_id, null, false);
                    user_id = 0;
                    chat_id = 0;
                    int lower_part = (int) dialog_id;
                    int high_id = (int) (dialog_id >> 32);

                    if (lower_part != 0) {
                        if (high_id == 1) {
                            chat_id = lower_part;
                        } else {
                            if (lower_part > 0) {
                                user_id = lower_part;
                            } else if (lower_part < 0) {
                                chat_id = -lower_part;
                            }
                        }
                    } else {
                        TLRPC.EncryptedChat chat = MessagesController.getInstance().getEncryptedChat(high_id);
                        user_id = chat.user_id;
                    }

                    if (user_id != 0) {
                        int picClick = plusPreferences.getInt("dialogsClickOnPic", 0);
                        if (picClick == 2) {
                            Bundle args = new Bundle();
                            args.putInt("user_id", user_id);
                            presentFragment(new ProfileActivity(args));
                            return;
                        } else if (picClick == 1) {
                            TLRPC.User user = MessagesController.getInstance().getUser(user_id);
                            if (user.photo != null && user.photo.photo_big != null) {
                                PhotoViewer.getInstance().setParentActivity(getParentActivity());
                                PhotoViewer.getInstance().openPhoto(user.photo.photo_big, DialogsActivity.this);
                            }
                            return;
                        }

                    } else if (chat_id != 0) {
                        int picClick = plusPreferences.getInt("dialogsClickOnGroupPic", 0);
                        if (picClick == 2) {
                            MessagesController.getInstance().loadChatInfo(chat_id, null, false);
                            Bundle args = new Bundle();
                            args.putInt("chat_id", chat_id);
                            ProfileActivity fragment = new ProfileActivity(args);
                            presentFragment(fragment);
                            return;
                        } else if (picClick == 1) {
                            TLRPC.Chat chat = MessagesController.getInstance().getChat(chat_id);
                            if (chat.photo != null && chat.photo.photo_big != null) {
                                PhotoViewer.getInstance().setParentActivity(getParentActivity());
                                PhotoViewer.getInstance().openPhoto(chat.photo.photo_big, DialogsActivity.this);
                            }
                            return;
                        }
                    }
                }

                //
                if (onlySelect) {
                    didSelectResult(dialog_id, true, false);
                } else {
                    Bundle args = new Bundle();
                    int lower_part = (int) dialog_id;
                    int high_id = (int) (dialog_id >> 32);
                    if (lower_part != 0) {
                        if (high_id == 1) {
                            args.putInt("chat_id", lower_part);
                        } else {
                            if (lower_part > 0) {
                                args.putInt("user_id", lower_part);
                            } else if (lower_part < 0) {
                                if (message_id != 0) {
                                    TLRPC.Chat chat = MessagesController.getInstance().getChat(-lower_part);
                                    if (chat != null && chat.migrated_to != null) {
                                        args.putInt("migrated_to", lower_part);
                                        lower_part = -chat.migrated_to.channel_id;
                                    }
                                }
                                args.putInt("chat_id", -lower_part);
                            }
                        }
                    } else {
                        args.putInt("enc_id", high_id);
                    }
                    if (message_id != 0) {
                        args.putInt("message_id", message_id);
                    } else {
                        if (actionBar != null) {
                            actionBar.closeSearchField();
                        }
                    }
                    if (AndroidUtilities.isTablet()) {
                        if (openedDialogId == dialog_id && adapter != dialogsSearchAdapter) {
                            return;
                        }
                        if (dialogsAdapter != null) {
                            dialogsAdapter.setOpenedDialogId(openedDialogId = dialog_id);
                            updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
                        }
                    }
                    if (searchString != null) {
                        if (MessagesController.checkCanOpenChat(args, DialogsActivity.this)) {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
                            presentFragment(new ChatActivity(args));
                        }
                    } else {
                        if (MessagesController.checkCanOpenChat(args, DialogsActivity.this)) {
                            presentFragment(new ChatActivity(args));
                        }
                    }
            }
            }
        });
        listView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListener() {
            @Override
            public boolean onItemClick(View view, int position) {
                if (onlySelect || searching && searchWas || getParentActivity() == null) {
                    if (searchWas && searching || dialogsSearchAdapter.isRecentSearchDisplayed()) {
                        RecyclerView.Adapter adapter = listView.getAdapter();
                        if (adapter == dialogsSearchAdapter) {
                            Object item = dialogsSearchAdapter.getItem(position);
                            if (item instanceof String || dialogsSearchAdapter.isRecentSearchDisplayed()) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                builder.setMessage(LocaleController.getString("ClearSearch", R.string.ClearSearch));
                                builder.setPositiveButton(LocaleController.getString("ClearButton", R.string.ClearButton).toUpperCase(), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (dialogsSearchAdapter.isRecentSearchDisplayed()) {
                                            dialogsSearchAdapter.clearRecentSearch();
                                        } else {
                                            dialogsSearchAdapter.clearRecentHashtags();
                                        }
                                    }
                                });
                                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                                showDialog(builder.create());
                                return true;
                            }
                        }
                    }
                    return false;
                }
                TLRPC.TL_dialog dialog;
                ArrayList<TLRPC.TL_dialog> dialogs = getDialogsArray();
                if (position < 0 || position >= dialogs.size()) {
                    return false;
                }
                dialog = dialogs.get(position);
                selectedDialog = dialog.id;
                final boolean pinned = dialog.pinned;

                BottomSheet.Builder builder = new BottomSheet.Builder(getParentActivity());
                int lower_id = (int) selectedDialog;
                int high_id = (int) (selectedDialog >> 32);
                //plus
                TLRPC.Chat currentChat = MessagesController.getInstance().getChat((int) -selectedDialog);
                TLRPC.User currentUser = MessagesController.getInstance().getUser((int) selectedDialog);
                String title = currentChat != null ? currentChat.title : currentUser != null ? UserObject.getUserName(currentUser) : null;
                if(title != null)builder.setTitle(title);
                //
                if (DialogObject.isChannel(dialog)) {
                    final TLRPC.Chat chat = MessagesController.getInstance().getChat(-lower_id);
                    CharSequence items[];
                    //plus
                    final boolean isFav = Favorite.getInstance().isFavorite(dialog.id);
                    final int unread = dialog.unread_count;
                    SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    final boolean markedAsUnread = plusPreferences.getInt("unread_" + dialog.id, 0) == 1;
                    //CharSequence cs2 = isFav ? LocaleController.getString("DeleteFromFavorites", R.string.DeleteFromFavorites) : LocaleController.getString("AddToFavorites", R.string.AddToFavorites);
                    final boolean isMuted = MessagesController.getInstance().isDialogMuted(selectedDialog);
                    int muted = MessagesController.getInstance().isDialogMuted(selectedDialog) ? R.drawable.chats_mute : 0;
                    //CharSequence cs = muted != 0 ? LocaleController.getString("UnmuteNotifications", R.string.UnmuteNotifications) : LocaleController.getString("MuteNotifications", R.string.MuteNotifications);
                    //CharSequence csa = LocaleController.getString("AddShortcut", R.string.AddShortcut);
                    //
                    int icons[] = new int[]{
                            dialog.pinned ? R.drawable.chats_unpin : R.drawable.chats_pin,
                            R.drawable.chats_clear,
                            R.drawable.chats_leave,
                            isMuted ? R.drawable.notify_members_on : R.drawable.notify_members_off,
                            isFav ? R.drawable.chats_nofavs : R.drawable.chats_favs,
                            unread == 0 && !markedAsUnread ? R.drawable.chats_unread : R.drawable.chats_read,
                            R.drawable.chats_shortcut
                    };
                    if (chat != null && chat.megagroup) {
                        items = new CharSequence[]{
                                dialog.pinned || MessagesController.getInstance().canPinDialog(false) ? (dialog.pinned ? LocaleController.getString("UnpinFromTop", R.string.UnpinFromTop) : LocaleController.getString("PinToTop", R.string.PinToTop)) : null,
                                LocaleController.getString("ClearHistoryCache", R.string.ClearHistoryCache),
                                chat == null || !chat.creator ? LocaleController.getString("LeaveMegaMenu", R.string.LeaveMegaMenu) : LocaleController.getString("DeleteMegaMenu", R.string.DeleteMegaMenu),
                                muted != 0 ? LocaleController.getString("UnmuteNotifications", R.string.UnmuteNotifications) : LocaleController.getString("MuteNotifications", R.string.MuteNotifications),
                                isFav ? LocaleController.getString("DeleteFromFavorites", R.string.DeleteFromFavorites) : LocaleController.getString("AddToFavorites", R.string.AddToFavorites),
                                unread == 0 && !markedAsUnread ? LocaleController.getString("MarkAsUnread", R.string.MarkAsUnread) : LocaleController.getString("MarkAsRead", R.string.MarkAsRead),
                                LocaleController.getString("AddShortcut", R.string.AddShortcut)};
                    } else {
                        items = new CharSequence[]{
                                dialog.pinned || MessagesController.getInstance().canPinDialog(false) ? (dialog.pinned ? LocaleController.getString("UnpinFromTop", R.string.UnpinFromTop) : LocaleController.getString("PinToTop", R.string.PinToTop)) : null,
                                LocaleController.getString("ClearHistoryCache", R.string.ClearHistoryCache),
                                chat == null || !chat.creator ? LocaleController.getString("LeaveChannelMenu", R.string.LeaveChannelMenu) : LocaleController.getString("ChannelDeleteMenu", R.string.ChannelDeleteMenu),
                                muted != 0 ? LocaleController.getString("UnmuteNotifications", R.string.UnmuteNotifications) : LocaleController.getString("MuteNotifications", R.string.MuteNotifications),
                                isFav ? LocaleController.getString("DeleteFromFavorites", R.string.DeleteFromFavorites) : LocaleController.getString("AddToFavorites", R.string.AddToFavorites),
                                unread == 0 && !markedAsUnread ? LocaleController.getString("MarkAsUnread", R.string.MarkAsUnread) : LocaleController.getString("MarkAsRead", R.string.MarkAsRead),
                                LocaleController.getString("AddShortcut", R.string.AddShortcut)};

                    }

                    builder.setItems(items, icons, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, final int which) {
                            if (which == 3) {
                                if(newTabsView != null){
                                    newTabsView.forceUpdateTabCounters();
                                }
                                updateTabCounters = true;
                                boolean muted = MessagesController.getInstance().isDialogMuted(selectedDialog);
                                if (!muted) {
                                    showDialog(AlertsCreator.createMuteAlert(getParentActivity(), selectedDialog));
                                } else {
                                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putInt("notify2_" + selectedDialog, 0);
                                    MessagesStorage.getInstance().setDialogFlags(selectedDialog, 0);
                                    editor.commit();
                                    TLRPC.TL_dialog dialg = MessagesController.getInstance().dialogs_dict.get(selectedDialog);
                                    if (dialg != null) {
                                        dialg.notify_settings = new TLRPC.TL_peerNotifySettings();
                                    }
                                    NotificationsController.updateServerNotificationsSettings(selectedDialog);
                                }
                            } else if (which == 4) {
                                TLRPC.TL_dialog dialg = MessagesController.getInstance().dialogs_dict.get(selectedDialog);
                                if (isFav) {
                                    Favorite.getInstance().deleteFavorite(selectedDialog);
                                    MessagesController.getInstance().dialogsFavs.remove(dialg);
                                } else {
                                    Favorite.getInstance().addFavorite(selectedDialog);
                                    MessagesController.getInstance().dialogsFavs.add(dialg);
                                }
                                if (dialogsType == 8) {
                                    if (dialogsAdapter != null) {
                                        dialogsAdapter.notifyDataSetChanged();
                                    }
                                    if(!Theme.plusHideTabs){
                                        updateTabs();
                                    }
                                }
                                if(!Theme.plusHideTabs){
                                    updateTabCounters = true;
                                }
                                updateVisibleRows(0);
                            } else if (which == 5) {
                                if(unread == 0 && !markedAsUnread){
                                    markDialogAsUnread();
                                } else {
                                    markAsReadDialog(false);
                                }
                            } else if (which == 6) {
                                addShortcut();
                            }
                            //
                            else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                                //builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                builder.setTitle(chat != null ? chat.title : LocaleController.getString("AppName", R.string.AppName));
                                if (which == 0) {
                                    if (MessagesController.getInstance().pinDialog(selectedDialog, !pinned, null, 0) && !pinned) {
                                        listView.smoothScrollToPosition(0);
                                    }
                                } else {
                                    //AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                                    builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                    if (which == 1) {
                                        if (chat != null && chat.megagroup) {
                                            builder.setMessage(LocaleController.getString("AreYouSureClearHistorySuper", R.string.AreYouSureClearHistorySuper));
                                        } else {
                                            builder.setMessage(LocaleController.getString("AreYouSureClearHistoryChannel", R.string.AreYouSureClearHistoryChannel));
                                        }
                                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                MessagesController.getInstance().deleteDialog(selectedDialog, 2);
                                            }
                                        });
                                    } else {
                                        if (chat != null && chat.megagroup) {
                                            if (!chat.creator) {
                                                builder.setMessage(LocaleController.getString("MegaLeaveAlert", R.string.MegaLeaveAlert));
                                            } else {
                                                builder.setMessage(LocaleController.getString("MegaDeleteAlert", R.string.MegaDeleteAlert));
                                            }
                                        } else {
                                            if (chat == null || !chat.creator) {
                                                builder.setMessage(LocaleController.getString("ChannelLeaveAlert", R.string.ChannelLeaveAlert));
                                            } else {
                                                builder.setMessage(LocaleController.getString("ChannelDeleteAlert", R.string.ChannelDeleteAlert));
                                            }
                                        }
                                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                MessagesController.getInstance().deleteUserFromChat((int) -selectedDialog, UserConfig.getCurrentUser(), null);
                                                if (AndroidUtilities.isTablet()) {
                                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, selectedDialog);
                                                }
                                            }
                                        });
                                    }
                                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                                    showDialog(builder.create());
                                }
                        }}
                        });
                        showDialog(builder.create());
                } else {
                    final boolean isChat = lower_id < 0 && high_id != 1;
                    final boolean isMuted = MessagesController.getInstance().isDialogMuted(selectedDialog);
                    int muted = MessagesController.getInstance().isDialogMuted(selectedDialog) ? R.drawable.list_mute : 0;
                    TLRPC.User user = null;
                    if (!isChat && lower_id > 0 && high_id != 1) {
                        user = MessagesController.getInstance().getUser(lower_id);
                    }
                    TLRPC.EncryptedChat encryptedChat = null;
                    if(lower_id == 0){
                        encryptedChat = MessagesController.getInstance().getEncryptedChat(high_id);
                    }
                    final boolean isEncrypted = encryptedChat != null;
                    final boolean isBot = user != null && user.bot;
                    final boolean isFav = Favorite.getInstance().isFavorite(dialog.id);
                    final int unread = dialog.unread_count;
                    SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    final boolean markedAsUnread = plusPreferences.getInt("unread_" + dialog.id, 0) == 1;
                    //CharSequence cs = isFav ? LocaleController.getString("DeleteFromFavorites", R.string.DeleteFromFavorites) : LocaleController.getString("AddToFavorites", R.string.AddToFavorites);
                    //CharSequence csa = LocaleController.getString("AddShortcut", R.string.AddShortcut);

                    /*builder.setItems(new CharSequence[]{LocaleController.getString("ClearHistory", R.string.ClearHistory),
                            isChat ? LocaleController.getString("DeleteChat", R.string.DeleteChat) : 
                            isBot ? LocaleController.getString("DeleteAndStop", R.string.DeleteAndStop) : LocaleController.getString("Delete", R.string.Delete),
                            muted != 0 ? LocaleController.getString("UnmuteNotifications", R.string.UnmuteNotifications) : LocaleController.getString("MuteNotifications", R.string.MuteNotifications),
                            cs,
                            unread == 0 && !markedAsUnread ? LocaleController.getString("MarkAsUnread", R.string.MarkAsUnread) : LocaleController.getString("MarkAsRead", R.string.MarkAsRead),
                            csa}, new DialogInterface.OnClickListener() {*/
                    builder.setItems(new CharSequence[]{
                            dialog.pinned || MessagesController.getInstance().canPinDialog(lower_id == 0) ? (dialog.pinned ? LocaleController.getString("UnpinFromTop", R.string.UnpinFromTop) : LocaleController.getString("PinToTop", R.string.PinToTop)) : null,
                            LocaleController.getString("ClearHistory", R.string.ClearHistory),
                            isChat ? LocaleController.getString("DeleteChat", R.string.DeleteChat) : isBot ? LocaleController.getString("DeleteAndStop", R.string.DeleteAndStop) : LocaleController.getString("Delete", R.string.Delete),
                            muted != 0 ? LocaleController.getString("UnmuteNotifications", R.string.UnmuteNotifications) : LocaleController.getString("MuteNotifications", R.string.MuteNotifications),
                            isFav ? LocaleController.getString("DeleteFromFavorites", R.string.DeleteFromFavorites) : LocaleController.getString("AddToFavorites", R.string.AddToFavorites),
                            unread == 0 && !markedAsUnread ? LocaleController.getString("MarkAsUnread", R.string.MarkAsUnread) : LocaleController.getString("MarkAsRead", R.string.MarkAsRead),
                            LocaleController.getString("AddShortcut", R.string.AddShortcut)
                    }, new int[]{
                            dialog.pinned ? R.drawable.chats_unpin : R.drawable.chats_pin,
                            R.drawable.chats_clear,
                            isChat ? R.drawable.chats_leave : R.drawable.chats_delete,
                            isMuted ? R.drawable.notify_members_on : R.drawable.notify_members_off,
                            isFav ? R.drawable.chats_nofavs : R.drawable.chats_favs,
                            unread == 0 && !markedAsUnread ? R.drawable.chats_unread : R.drawable.chats_read,
                            R.drawable.chats_shortcut
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, final int which) {
                            if (which == 3) {
                                if(newTabsView != null){
                                    newTabsView.forceUpdateTabCounters();
                                }
                                updateTabCounters = true;
                                boolean muted = MessagesController.getInstance().isDialogMuted(selectedDialog);
                                if (!muted) {
                                    showDialog(AlertsCreator.createMuteAlert(getParentActivity(), selectedDialog));
                                } else {
                                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putInt("notify2_" + selectedDialog, 0);
                                    MessagesStorage.getInstance().setDialogFlags(selectedDialog, 0);
                                    editor.commit();
                                    TLRPC.TL_dialog dialg = MessagesController.getInstance().dialogs_dict.get(selectedDialog);
                                    if (dialg != null) {
                                        dialg.notify_settings = new TLRPC.TL_peerNotifySettings();
                                    }
                                    NotificationsController.updateServerNotificationsSettings(selectedDialog);
                                }
                            } else if (which == 4) {
                                if(!isEncrypted) {
                                    TLRPC.TL_dialog dialg = MessagesController.getInstance().dialogs_dict.get(selectedDialog);
                                    if (isFav) {
                                        Favorite.getInstance().deleteFavorite(selectedDialog);
                                        MessagesController.getInstance().dialogsFavs.remove(dialg);
                                    } else {
                                        Favorite.getInstance().addFavorite(selectedDialog);
                                        MessagesController.getInstance().dialogsFavs.add(dialg);
                                    }
                                    if (dialogsType == 8) {
                                        if (dialogsAdapter != null) {
                                            dialogsAdapter.notifyDataSetChanged();
                                        }
                                        if (!Theme.plusHideTabs) {
                                            updateTabs();
                                        }
                                    }
                                    updateVisibleRows(0);
                                } else{
                                    AndroidUtilities.runOnUIThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (getParentActivity() != null) {
                                                Toast toast = Toast.makeText(getParentActivity(), "Secret chats can't be added to favorites", Toast.LENGTH_SHORT);
                                                toast.show();
                                            }
                                        }
                                    });
                                }
                            } else if (which == 5) {
                                if(unread == 0 && !markedAsUnread){
                                    markDialogAsUnread();
                                } else {
                                    markAsReadDialog(false);
                                }
                            } else if (which == 6) {
                                addShortcut();
                            }
                            //
                            else {

                                if (which == 0) {
                                    if (MessagesController.getInstance().pinDialog(selectedDialog, !pinned, null, 0) && !pinned) {
                                        listView.smoothScrollToPosition(0);
                                    }
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                                    builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                    //builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                    TLRPC.Chat currentChat = MessagesController.getInstance().getChat((int) -selectedDialog);
                                    TLRPC.User user = MessagesController.getInstance().getUser((int) selectedDialog);
                                    String title = currentChat != null ? currentChat.title : user != null ? UserObject.getUserName(user) : LocaleController.getString("AppName", R.string.AppName);
                                    builder.setTitle(title);
                                    if (which == 1) {
                                        builder.setMessage(LocaleController.getString("AreYouSureClearHistory", R.string.AreYouSureClearHistory));
                                    } else {
                                        if (isChat) {
                                            builder.setMessage(LocaleController.getString("AreYouSureDeleteAndExit", R.string.AreYouSureDeleteAndExit));
                                        } else {
                                            builder.setMessage(LocaleController.getString("AreYouSureDeleteThisChat", R.string.AreYouSureDeleteThisChat));
                                        }
                                    }
                                    builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (which != 1) {
                                                if (isChat) {
                                                    TLRPC.Chat currentChat = MessagesController.getInstance().getChat((int) -selectedDialog);
                                                    if (currentChat != null && ChatObject.isNotInChat(currentChat)) {
                                                    MessagesController.getInstance().deleteDialog(selectedDialog, 0);
                                                    } else {
                                                        MessagesController.getInstance().deleteUserFromChat((int) -selectedDialog, MessagesController.getInstance().getUser(UserConfig.getClientUserId()), null);
                                                    }
                                                } else {
                                                    MessagesController.getInstance().deleteDialog(selectedDialog, 0);
                                                }
                                                if (isBot) {
                                                    MessagesController.getInstance().blockUser((int) selectedDialog);
                                                }
                                                if (AndroidUtilities.isTablet()) {
                                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, selectedDialog);
                                                }
                                            } else {
                                                MessagesController.getInstance().deleteDialog(selectedDialog, 1);
                                            }
                                        }
                                    });
                                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                                    showDialog(builder.create());
                                }
                            }
                        }
                    });
                    showDialog(builder.create());
                }
                return true;
            }
        });

        searchEmptyView = new EmptyTextProgressView(context);
        searchEmptyView.setVisibility(View.GONE);
        searchEmptyView.setShowAtCenter(true);
        searchEmptyView.setText(LocaleController.getString("NoResult", R.string.NoResult));
        frameLayout.addView(searchEmptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        emptyView = new LinearLayout(context);
        emptyView.setOrientation(LinearLayout.VERTICAL);
        emptyView.setVisibility(View.GONE);
        emptyView.setGravity(Gravity.CENTER);
        frameLayout.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        //if(Theme.usePlusTheme)emptyView.setBackgroundColor(Theme.chatsRowColor);
        /*emptyView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });*/
        emptyView.setOnTouchListener(onTouchListener);
        emptyTextView1 = new TextView(context);
        emptyTextView1.setText(LocaleController.getString("NoChats", R.string.NoChats));
        emptyTextView1.setTextColor(Theme.usePlusTheme ? Theme.chatsNameColor : Theme.getColor(Theme.key_emptyListPlaceholder));
        emptyTextView1.setGravity(Gravity.CENTER);
        emptyTextView1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        emptyView.addView(emptyTextView1, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        emptyTextView2 = new TextView(context);
        String help = LocaleController.getString("NoChatsHelp", R.string.NoChatsHelp);
        if (AndroidUtilities.isTablet() && !AndroidUtilities.isSmallTablet()) {
            help = help.replace('\n', ' ');
        }
        emptyTextView2.setText(help);
        emptyTextView2.setTextColor(Theme.usePlusTheme ? Theme.chatsNameColor : Theme.getColor(Theme.key_emptyListPlaceholder));
        emptyTextView2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        emptyTextView2.setGravity(Gravity.CENTER);
        emptyTextView2.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(6), AndroidUtilities.dp(8), 0);
        emptyTextView2.setLineSpacing(AndroidUtilities.dp(2), 1);
        emptyView.addView(emptyTextView2, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        progressView = new RadialProgressView(context);
        progressView.setVisibility(View.GONE);
        frameLayout.addView(progressView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

        floatingButton = new ImageView(context);
        floatingButton.setVisibility(onlySelect ? View.GONE : View.VISIBLE);
        floatingButton.setScaleType(ImageView.ScaleType.CENTER);

        Drawable drawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56), Theme.getColor(Theme.key_chats_actionBackground), Theme.getColor(Theme.key_chats_actionPressedBackground));
        if (Build.VERSION.SDK_INT < 21) {
            Drawable shadowDrawable = context.getResources().getDrawable(R.drawable.floating_shadow).mutate();
            shadowDrawable.setColorFilter(new PorterDuffColorFilter(0xff000000, PorterDuff.Mode.MULTIPLY));
            CombinedDrawable combinedDrawable = new CombinedDrawable(shadowDrawable, drawable, 0, 0);
            combinedDrawable.setIconSize(AndroidUtilities.dp(56), AndroidUtilities.dp(56));
            drawable = combinedDrawable;
        }
        floatingButton.setBackgroundDrawable(drawable);
        floatingButton.setColorFilter(new PorterDuffColorFilter(Theme.usePlusTheme ? Theme.chatsFloatingPencilColor : Theme.getColor(Theme.key_chats_actionIcon), PorterDuff.Mode.MULTIPLY));
        floatingButton.setImageResource(R.drawable.floating_pencil);
        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(floatingButton, "translationZ", AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(floatingButton, "translationZ", AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            floatingButton.setStateListAnimator(animator);
            floatingButton.setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                }
            });
        }
        frameLayout.addView(floatingButton, LayoutHelper.createFrame(Build.VERSION.SDK_INT >= 21 ? 56 : 60, Build.VERSION.SDK_INT >= 21 ? 56 : 60, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, LocaleController.isRTL ? 14 : 0, 0, LocaleController.isRTL ? 0 : 14, !Theme.plusHideTabs && Theme.plusTabsToBottom ? Theme.plusTabsHeight + 14 : 14));
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putBoolean("destroyAfterSelect", true);
                presentFragment(new ContactsActivity(args));
            }
        });
        // plus
        if(Theme.plusDefaultTab != -1){
            Theme.plusSelectedTab = Theme.plusDefaultTab;
        }
        newTabsView = new  TabsView(context);
        newTabsView.setListener(new TabsView.Listener() {

            @Override
            public void onPageSelected(int position, int type) {
                //Log.e("DialogsActivity", "onPageSelected position " + position + " type " + type);
                if (dialogsType != type) {
                    dialogsType = type;
                    refreshAdapterAndTabs();
                    refreshTabAndListViews(false);
                    if(type > 2){
                        neeLoadMoreChats();
                    }
                }
            }

            @Override
            public void onTabLongClick(int position, int type) {
                int sort = type == 0 ? Theme.plusSortAll : type == 3 ? Theme.plusSortUsers : type == 4 || type == 9 ? Theme.plusSortGroups : type == 5 ? Theme.plusSortChannels : type == 6 ? Theme.plusSortBots : type == 7 ? Theme.plusSortSuperGroups : type == 8 ? Theme.plusSortFavs : type == 10 ? Theme.plusSortAdmin : type == 11 ? Theme.plusSortUnread : 0;
                if(type == 0){
                    showAllTabLongClick(position, type, sort);
                } else {
                    showTabLongClick(position, type, sort);
                }

            }

            @Override
            public void refresh(boolean bool) {
                refreshTabAndListViews(bool);
            }

            @Override
            public void onTabClick() {
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                //Log.e("DialogsActivity", "onTabClick firstVisibleItem " + firstVisibleItem);
                if(firstVisibleItem < 20){
                    listView.smoothScrollToPosition(0);
                } else{
                    listView.scrollToPosition(0);
                }

            }
        });
        frameLayout.addView(newTabsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, Theme.plusTabsHeight, Theme.plusTabsToBottom ? Gravity.BOTTOM : Gravity.TOP));
        refreshTabAndListViews(false);

        if(!Theme.plusHideTabs){
            dialogsType = Theme.plusDialogType;
        }

        //if(Theme.usePlusTheme)Glow.setEdgeGlowColor(listView, Theme.chatsHeaderColor);

        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING && searching && searchWas) {
                    AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                int visibleItemCount = Math.abs(layoutManager.findLastVisibleItemPosition() - firstVisibleItem) + 1;
                int totalItemCount = recyclerView.getAdapter().getItemCount();

                if (searching && searchWas) {
                    if (visibleItemCount > 0 && layoutManager.findLastVisibleItemPosition() == totalItemCount - 1 && !dialogsSearchAdapter.isMessagesSearchEndReached()) {
                        dialogsSearchAdapter.loadMoreSearchMessages();
                    }
                    return;
                }

                if (visibleItemCount > 0) {
                    if (layoutManager.findLastVisibleItemPosition() >= getDialogsArray().size() - 10) {
                        boolean fromCache = !MessagesController.getInstance().dialogsEndReached;
                        if (fromCache || !MessagesController.getInstance().serverDialogsEndReached) {
                            MessagesController.getInstance().loadDialogs(-1, 100, fromCache);
                        }
                        /*if(MessagesController.getInstance().dialogs.size() >= Theme.plusChatsToLoad || !MessagesController.getInstance().dialogsEndReached) {
                            if (dialogsType < 3) {
                                MessagesController.getInstance().loadDialogs(-1, Theme.plusChatsToLoad, !MessagesController.getInstance().dialogsEndReached); // <- This causes bad list scroll
                            } else {
                                int size = getDialogsArray().size();
                                if (!MessagesController.getInstance().dialogsEndReached && !MessagesController.getInstance().loadingDialogs && layoutManager.findLastVisibleItemPosition() >= size - 1 && ((firstVisibleItem <= 0 && size < 10) || firstVisibleItem > prevPosition)) {
                                    
                                    if (Theme.plusChatsToLoad < 5000)
                                        MessagesController.getInstance().loadDialogs(-1, Theme.plusChatsToLoad, true);
                                }
                            }
                        }*/
                    }
                }

                if (floatingButton.getVisibility() != View.GONE) {
                    final View topChild = recyclerView.getChildAt(0);
                    int firstViewTop = 0;
                    if (topChild != null) {
                        firstViewTop = topChild.getTop();
                    }
                    boolean goingDown;
                    boolean changed = true;
                    if (prevPosition == firstVisibleItem) {
                        final int topDelta = prevTop - firstViewTop;
                        goingDown = firstViewTop < prevTop;
                        changed = Math.abs(topDelta) > 1;
                    } else {
                        goingDown = firstVisibleItem > prevPosition;
                    }
                    if (changed && scrollUpdated) {
                        if(!Theme.plusHideTabs && !Theme.plusDisableTabsAnimation || Theme.plusHideTabs)hideFloatingButton(goingDown);
                    }
                    prevPosition = firstVisibleItem;
                    prevTop = firstViewTop;
                    scrollUpdated = true;
                }

                if(!Theme.plusHideTabs && visibleItemCount < totalItemCount) {
                    //Log.e("DialogsActivity", "onScrolled visibleItemCount " + visibleItemCount + " totalItemCount " + totalItemCount + " findLastVisibleItemPosition " + layoutManager.findLastVisibleItemPosition());
                    if (dy > 1) {
                        //Down (HIDE)
                        if (recyclerView.getChildAt(0).getTop() < 0){
                            if(!Theme.plusDisableTabsAnimation) {
                                hideTabsAnimated(true);
                            } else{
                                hideFloatingButton(true);
                            }
                        }

                    }
                    if (dy < -1) {
                        //Up (SHOW)
                        if(!Theme.plusDisableTabsAnimation) {
                            hideTabsAnimated(false);
                            if (firstVisibleItem == 0) {
                                listView.setPadding(0, Theme.plusTabsToBottom ? 0 : AndroidUtilities.dp(Theme.plusTabsHeight), 0, Theme.plusTabsToBottom ? AndroidUtilities.dp(Theme.plusTabsHeight) : 0);
                            }
                        } else{
                            hideFloatingButton(false);
                        }
                    }
                    //}
                }
            }
        });

        //if (!onlySelect && dialogsType == 0) {
        /*if (!onlySelect && (dialogsType == 0 || dialogsType > 2)) {
            frameLayout.addView(new PlayerView(context, this), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 39, Gravity.TOP | Gravity.LEFT, 0, -36, 0, 0));
        }*/

        if (searchString == null) {
            dialogsAdapter = new DialogsAdapter(context, dialogsType);
            if (AndroidUtilities.isTablet() && openedDialogId != 0) {
                dialogsAdapter.setOpenedDialogId(openedDialogId);
            }
            listView.setAdapter(dialogsAdapter);
        }
        int type = 0;
        if (searchString != null) {
            type = 2;
        } else if (!onlySelect) {
            type = 1;
        }
        dialogsSearchAdapter = new DialogsSearchAdapter(context, type, dialogsType);
        dialogsSearchAdapter.setDelegate(new DialogsSearchAdapter.DialogsSearchAdapterDelegate() {
            @Override
            public void searchStateChanged(boolean search) {
                if (searching && searchWas && searchEmptyView != null) {
                    if (search) {
                        searchEmptyView.showProgress();
                    } else {
                        searchEmptyView.showTextView();
                    }
                }
            }

            @Override
            public void didPressedOnSubDialog(int did) {
                if (onlySelect) {
                    didSelectResult(did, true, false);
                } else {
                    Bundle args = new Bundle();
                    if (did > 0) {
                        args.putInt("user_id", did);
                    } else {
                        args.putInt("chat_id", -did);
                    }
                    if (actionBar != null) {
                        actionBar.closeSearchField();
                    }
                    if (AndroidUtilities.isTablet()) {
                        if (dialogsAdapter != null) {
                            dialogsAdapter.setOpenedDialogId(openedDialogId = did);
                            updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
                        }
                    }
                    if (searchString != null) {
                        if (MessagesController.checkCanOpenChat(args, DialogsActivity.this)) {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
                            presentFragment(new ChatActivity(args));
                        }
                    } else {
                        if (MessagesController.checkCanOpenChat(args, DialogsActivity.this)) {
                            presentFragment(new ChatActivity(args));
                        }
                    }
                }
            }

            @Override
            public void needRemoveHint(final int did) {
                if (getParentActivity() == null) {
                    return;
                }
                TLRPC.User user = MessagesController.getInstance().getUser(did);
                if (user == null) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                builder.setMessage(LocaleController.formatString("ChatHintsDelete", R.string.ChatHintsDelete, ContactsController.formatName(user.first_name, user.last_name)));
                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SearchQuery.removePeer(did);
                    }
                });
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                showDialog(builder.create());
            }
        });

        if (MessagesController.getInstance().loadingDialogs && MessagesController.getInstance().dialogs.isEmpty()) {
            searchEmptyView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
            listView.setEmptyView(progressView);
        } else {
            searchEmptyView.setVisibility(View.GONE);
            progressView.setVisibility(View.GONE);
            listView.setEmptyView(emptyView);
        }
        if (searchString != null) {
            actionBar.openSearchField(searchString);
        }

        if (!onlySelect && dialogsType == 0 || dialogsType > 2) {
            frameLayout.addView(fragmentContextView = new FragmentContextView(context, this), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 39, Gravity.TOP | Gravity.LEFT, 0, -36, 0, 0));
        }

        //if(Theme.usePlusTheme)updateTheme();
        if(Theme.usePlusTheme){
            fragmentView.setBackgroundColor(Theme.chatsRowColor);
            if (Theme.chatsRowGradient > 0) {
                GradientDrawable.Orientation go;
                switch (Theme.chatsRowGradient) {
                    case 2:
                        go = GradientDrawable.Orientation.LEFT_RIGHT;
                        break;
                    case 3:
                        go = GradientDrawable.Orientation.TL_BR;
                        break;
                    case 4:
                        go = GradientDrawable.Orientation.BL_TR;
                        break;
                    default:
                        go = GradientDrawable.Orientation.TOP_BOTTOM;
                }
                int[] colors = new int[]{Theme.chatsRowColor, Theme.chatsRowGradientColor};
                GradientDrawable gd = new GradientDrawable(go, colors);
                fragmentView.setBackgroundDrawable(gd);
            }
            updateTheme();
        }
        return fragmentView;
    }

    private void showAllTabLongClick(final int position, final int type, int sort){
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(getTitle(type, true));
        List<CharSequence> array = new ArrayList<>();
        array.add(LocaleController.getString("SortTabs", R.string.SortTabs));
        array.add(sort == 0 ? LocaleController.getString("SortByUnreadCount", R.string.SortByUnreadCount) : LocaleController.getString("SortByLastMessage", R.string.SortByLastMessage));
        array.add(Theme.plusDefaultTab == position ? LocaleController.getString("ResetDefaultTab", R.string.ResetDefaultTab) : LocaleController.getString("SetAsDefaultTab", R.string.SetAsDefaultTab));
        array.add(LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead));
        builder.setItems(array.toArray(new CharSequence[ array.size()]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int which) {
                if (which == 1) {
                    updateSortValue(type);
                } else if (which == 2){
                    updateDefault(position);
                } else if (which == 3){
                    markAsReadDialog(true);
                } else if (which == 0) {
                    presentFragment(new PlusManageTabsActivity());
                }
            }
        });
        showDialog(builder.create());
    }

    private void showTabLongClick(final int position, final int type, int sort){
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(getTitle(type, true));
        List<CharSequence> array = new ArrayList<>();
        array.add(LocaleController.getString("SortTabs", R.string.SortTabs));
        array.add(sort == 0 ? type == 3 ? LocaleController.getString("SortByStatus", R.string.SortByStatus) : LocaleController.getString("SortByUnreadCount", R.string.SortByUnreadCount) : type == 11 && sort == 1 ? LocaleController.getString("SortUnmutedFirst", R.string.SortUnmutedFirst) : LocaleController.getString("SortByLastMessage", R.string.SortByLastMessage));
        array.add(Theme.plusDefaultTab == position ? LocaleController.getString("ResetDefaultTab", R.string.ResetDefaultTab) : LocaleController.getString("SetAsDefaultTab", R.string.SetAsDefaultTab));
        array.add(LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead));
        if(type == 10){
            array.add(Theme.plusShowAllInAdminTab ? LocaleController.getString("ShowCreatedOnly", R.string.ShowCreatedOnly) : LocaleController.getString("ShowAllCreatedAndAdmin", R.string.ShowAllCreatedAndAdmin));
        }
        //if(type == 11){
        //    array.add(Theme.plusShowUnmutedFirst ? LocaleController.getString("DoNotShowUnmutedFirst", R.string.DoNotShowUnmutedFirst) : LocaleController.getString("ShowUnmutedFirst", R.string.ShowUnmutedFirst));
        //}
        builder.setItems(array.toArray(new CharSequence[ array.size()]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int which) {
                if (which == 1) {
                    updateSortValue(type);
                } else if (which == 2) {
                    updateDefault(position);
                } else if (which == 3) {
                    markAsReadDialog(true);
                } else if (which == 0) {
                    presentFragment(new PlusManageTabsActivity());
                } else if (type == 10 && which == 4) {
                    Theme.plusShowAllInAdminTab = !Theme.plusShowAllInAdminTab;
                    MessagesController.getInstance().sortDialogs(null);
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload);
                    SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = plusPreferences.edit();
                    editor.putBoolean("showAllInAdminTab", Theme.plusShowAllInAdminTab).apply();
                } else if (type == 11 && which == 4) {
                    Theme.plusShowUnmutedFirst = !Theme.plusShowUnmutedFirst;
                    MessagesController.getInstance().sortDialogs(null);
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload);
                    SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = plusPreferences.edit();
                    editor.putBoolean("showUnmutedFirst", Theme.plusShowUnmutedFirst).apply();
                }
            }
        });
        showDialog(builder.create());
    }

    private void updateSortValue(int type){
        String title = "";
        int i = 0;
        switch (type) {
            case 0:
                Theme.plusSortAll = Theme.plusSortAll == 0 ? 1 : 0;
                i = Theme.plusSortAll;
                title = "sortAll";
                break;
            case 3:
                Theme.plusSortUsers = Theme.plusSortUsers == 0 ? 1 : 0;
                i = Theme.plusSortUsers;
                title = "sortUsers";
                break;
            case 9:
            case 4:
                Theme.plusSortGroups = Theme.plusSortGroups == 0 ? 1 : 0;
                i = Theme.plusSortGroups;
                title = "sortGroups";
                break;
            case 5:
                Theme.plusSortChannels = Theme.plusSortChannels == 0 ? 1 : 0;
                i = Theme.plusSortChannels;
                title = "sortChannels";
                break;
            case 6:
                Theme.plusSortBots = Theme.plusSortBots == 0 ? 1 : 0;
                i = Theme.plusSortBots;
                title = "sortBots";
                break;
            case 7:
                Theme.plusSortSuperGroups = Theme.plusSortSuperGroups == 0 ? 1 : 0;
                i = Theme.plusSortSuperGroups;
                title = "sortSGroups";
                break;
            case 8:
                Theme.plusSortFavs = Theme.plusSortFavs == 0 ? 1 : 0;
                i = Theme.plusSortFavs;
                title = "sortFavs";
                break;
            case 10:
                Theme.plusSortAdmin = Theme.plusSortAdmin == 0 ? 1 : 0;
                i = Theme.plusSortAdmin;
                title = "sortAdmin";
                break;
            case 11:
                Theme.plusSortUnread = Theme.plusSortUnread == 0 ? 1 : Theme.plusSortUnread == 1 ? 2 : 0;
                i = Theme.plusSortUnread;
                title = "sortUnread";
                break;
        }

        if(!title.isEmpty()) {
            SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = plusPreferences.edit();
            editor.putInt(title, i).apply();
        }
        if (dialogsAdapter != null && dialogsAdapter.getItemCount() > 1) {
            dialogsAdapter.notifyDataSetChanged();
        }
    }

    private void updateDefault(int position){
        Theme.plusDefaultTab = Theme.plusDefaultTab == position ? -1 : position;
        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = plusPreferences.edit();
        editor.putInt("defaultTab", Theme.plusDefaultTab).apply();
    }

    private void markDialogAsUnread(){
        TLRPC.TL_dialog dialg = MessagesController.getInstance().dialogs_dict.get(selectedDialog);
        if(dialg.unread_count == 0) {
            SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = plusPreferences.edit();
            editor.putInt("unread_" + dialg.id, 1);
            editor.commit();
            updateVisibleRows(0);
        }
    }

    private void resetUnread(SharedPreferences plusPreferences, long uid){
        //SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = plusPreferences.edit();
        editor.remove("unread_"  + uid);
        editor.commit();
        updateVisibleRows(0);
    }

    private void markAsReadDialog(final boolean all){
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        TLRPC.Chat currentChat = MessagesController.getInstance().getChat((int) -selectedDialog);
        TLRPC.User user = MessagesController.getInstance().getUser((int) selectedDialog);
        String title = currentChat != null ? currentChat.title : user != null ? UserObject.getUserName(user) : LocaleController.getString("AppName", R.string.AppName);
        builder.setTitle(all ? getTitle(dialogsType, false) : title);
        builder.setMessage((all ? LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead) : LocaleController.getString("MarkAsRead", R.string.MarkAsRead)) + '\n' + LocaleController.getString("AreYouSure", R.string.AreYouSure));
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
                updateTabCounters = true;
                //Log.e("DialogsActivity", "0 markAsReadDialog " + MessagesController.getInstance().dialogsUnread.size());
                if(all){
                    ArrayList<TLRPC.TL_dialog> dialogs = getDialogsArray();
                    if (dialogs != null && !dialogs.isEmpty()) {
                        for (int a = 0; a < dialogs.size(); a++) {
                            TLRPC.TL_dialog dialg = dialogs.get(a);/*getDialogsArray().get(a);*/
                            if(dialg.unread_count > 0){
                                MessagesController.getInstance().markDialogAsRead(dialg.id, Math.max(0, dialg.top_message), Math.max(0, dialg.top_message), dialg.last_message_date, true, false);
                            } else{
                                if(plusPreferences.getInt("unread_" + dialg.id, 0) == 1){
                                    resetUnread(plusPreferences, dialg.id);
                                }
                            }
                        }
                    }
                } else {
                    TLRPC.TL_dialog dialg = MessagesController.getInstance().dialogs_dict.get(selectedDialog);
                    if(dialg.unread_count > 0) {
                        MessagesController.getInstance().markDialogAsRead(dialg.id, Math.max(0, dialg.top_message), Math.max(0, dialg.top_message), dialg.last_message_date, true, false);
                    } else{
                       if(plusPreferences.getInt("unread_" + dialg.id, 0) == 1){
                           resetUnread(plusPreferences, dialg.id);
                       }
                    }
                }
            }
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        showDialog(builder.create());
    }

    private void addShortcut() {
        TLRPC.TL_dialog dialg = MessagesController.getInstance().dialogs_dict.get(selectedDialog);
        try {
            long did = 0;
            long dialog_id = dialg.id;

            int lower_id = (int)dialog_id;
            int high_id = (int)(dialog_id >> 32);
            if (lower_id != 0) {
                did = lower_id;
            } else {
                TLRPC.EncryptedChat encryptedChat = MessagesController.getInstance().getEncryptedChat(high_id);
                if (encryptedChat != null) {
                    did = ((long) encryptedChat.id) << 32;
                }
            }

            if(did != 0) {
                AndroidUtilities.installShortcut(did);
            }
        } catch (Exception e) {
            FileLog.e( e);
        }
    }

    private class DialogsOnTouch implements View.OnTouchListener {

        private DisplayMetrics displayMetrics;

        //private static final int MIN_DISTANCE_HIGH = 40;
        //private static final int MIN_DISTANCE_HIGH_Y = 60;
        //private float downX, downY, upX, upY;
        private float vDPI;
        private boolean changed;
        private float touchPosition;

        private DialogsOnTouch(Context context) {
            displayMetrics = context.getResources().getDisplayMetrics();
            vDPI = displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT;
        }

        public boolean onTouch(View view, MotionEvent event) {
            touchPositionDP = Math.round(event.getX() / vDPI);
            //Log.e("DialogsOnTouch", "onTouch");
            if(Theme.plusHideTabs || searching || Theme.plusDisableTabsScrolling){
                return false;
            }

            //if(testView != null){
            //    testView.getPager().onTouchEvent(event);
            //}

            if(newTabsView != null){
                newTabsView.getPager().onTouchEvent(event);
            }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touchPosition = Math.round(event.getX() / vDPI);
                        //Log.e("DialogsActivity", "DOWN touchPosition " + touchPosition + " changed " + changed);
                        if(touchPosition > 50){
                            parentLayout.getDrawerLayoutContainer().setAllowOpenDrawer(false, false);
                            changed = true;
                        }
                        return view instanceof LinearLayout; // for emptyView
                    case MotionEvent.ACTION_UP:
                        if(changed){
                            //Log.e("DialogsActivity", "UP touchPosition " + touchPosition + " changed " + changed);
                            parentLayout.getDrawerLayoutContainer().setAllowOpenDrawer(true, false);
                            //touchPosition = -1;
                        }
                        changed = false;
                        //return false;
                }

            return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (dialogsAdapter != null) {
            dialogsAdapter.notifyDataSetChanged();
            if(!Theme.plusHideTabs) {
                unreadCount();
            }
        }
        if (dialogsSearchAdapter != null) {
            dialogsSearchAdapter.notifyDataSetChanged();
        }
        if (checkPermission && !onlySelect && Build.VERSION.SDK_INT >= 23) {
            Activity activity = getParentActivity();
            if (activity != null) {
                checkPermission = false;
                if (activity.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED || activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (activity.shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                        builder.setMessage(LocaleController.getString("PermissionContacts", R.string.PermissionContacts));
                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                        showDialog(permissionDialog = builder.create());
                    } else if (activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                        builder.setMessage(LocaleController.getString("PermissionStorage", R.string.PermissionStorage));
                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                        showDialog(permissionDialog = builder.create());
                    } else {
                        askForPermissons();
                    }
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void askForPermissons() {
        Activity activity = getParentActivity();
        if (activity == null) {
            return;
        }
        ArrayList<String> permissons = new ArrayList<>();
        if (activity.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissons.add(Manifest.permission.READ_CONTACTS);
            permissons.add(Manifest.permission.WRITE_CONTACTS);
            permissons.add(Manifest.permission.GET_ACCOUNTS);
        }
        if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissons.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissons.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        String[] items = permissons.toArray(new String[permissons.size()]);
        try{
            activity.requestPermissions(items, 1);
        } catch (Exception ignore) {
        }
    }

    @Override
    protected void onDialogDismiss(Dialog dialog) {
        super.onDialogDismiss(dialog);
        if (permissionDialog != null && dialog == permissionDialog && getParentActivity() != null) {
            askForPermissons();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
        if (!onlySelect && floatingButton != null) {
            floatingButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    floatingButton.setTranslationY(floatingHidden ? AndroidUtilities.dp(!Theme.plusHideTabs && Theme.plusTabsToBottom ? 150 : 100) : 0);
                    floatingButton.setClickable(!floatingHidden);
                    if (floatingButton != null) {
                        floatingButton.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResultFragment(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int a = 0; a < permissions.length; a++) {
                if (grantResults.length <= a || grantResults[a] != PackageManager.PERMISSION_GRANTED) {
                    continue;
                }
                switch (permissions[a]) {
                    case Manifest.permission.READ_CONTACTS:
                        ContactsController.getInstance().readContacts();
                        break;
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        ImageLoader.getInstance().checkMediaPaths();
                        break;
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked") //updateInterfaces = 2 dialogsNeedReload = 3
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.dialogsNeedReload) {
            if (dialogsAdapter != null) {
                //Log.e("DialogsActivity", "didReceivedNotification dialogsNeedReload");
                if (dialogsAdapter.isDataSetChanged()) {
                    dialogsAdapter.notifyDataSetChanged();
                } else {
                    updateVisibleRows(MessagesController.UPDATE_MASK_NEW_MESSAGE);
                }
                if(!Theme.plusHideTabs){
                    updateTabCounters = true;
                }
            }
            if (dialogsSearchAdapter != null) {
                dialogsSearchAdapter.notifyDataSetChanged();
            }
            if (listView != null) {
                try {
                    if (MessagesController.getInstance().loadingDialogs && MessagesController.getInstance().dialogs.isEmpty()) {
                        searchEmptyView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.GONE);
                        listView.setEmptyView(progressView);
                    } else {
                        progressView.setVisibility(View.GONE);
                        if (searching && searchWas) {
                            emptyView.setVisibility(View.GONE);
                            listView.setEmptyView(searchEmptyView);
                        } else {
                            searchEmptyView.setVisibility(View.GONE);
                            listView.setEmptyView(emptyView);
                            // plus
                            checkEmptyView();
                        }
                    }
                } catch (Exception e) {
                    FileLog.e(e); //TODO fix it in other way?
                }
            }
        } else if (id == NotificationCenter.emojiDidLoaded) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.updateInterfaces) {
            //plus
            //Log.e("DialogsActivity", "didReceiveNotification updateInterfaces");
            if(!Theme.plusHideTabs && (Integer) args[0] == MessagesController.UPDATE_MASK_READ_DIALOG_MESSAGE){
                if(dialogsAdapter != null){
                    dialogsAdapter.notifyDataSetChanged();
                }
                updateTabCounters = true;
            }
            //
            updateVisibleRows((Integer) args[0]);
        } else if (id == NotificationCenter.appDidLogout) {
            dialogsLoaded = false;
        } else if (id == NotificationCenter.encryptedChatUpdated) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.contactsDidLoaded) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.openedChatChanged) {
            if (dialogsType == 0 && AndroidUtilities.isTablet()) {
                boolean close = (Boolean) args[1];
                long dialog_id = (Long) args[0];
                if (close) {
                    if (dialog_id == openedDialogId) {
                        openedDialogId = 0;
                    }
                } else {
                    openedDialogId = dialog_id;
                }
                if (dialogsAdapter != null) {
                    dialogsAdapter.setOpenedDialogId(openedDialogId);
                }
                updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
            }
        } else if (id == NotificationCenter.notificationsSettingsUpdated) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.messageReceivedByAck || id == NotificationCenter.messageReceivedByServer || id == NotificationCenter.messageSendError) {
            updateVisibleRows(MessagesController.UPDATE_MASK_SEND_STATE);
        } else if (id == NotificationCenter.didSetPasscode) {
            updatePasscodeButton();
        } else if (id == NotificationCenter.refreshTabs) {
            int i = (int) args[0];
            //Log.e("DialogsActivity", "didReceivedNotification refreshTabs i " + i);
            if(i == 14 || i == 12 || i == 10 || i == 15){
                if(newTabsView != null){
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) newTabsView.getLayoutParams();
                    params.gravity = Theme.plusTabsToBottom ? Gravity.BOTTOM : Gravity.TOP ;
                    newTabsView.setLayoutParams(params);
                }
                if(floatingButton != null){
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) floatingButton.getLayoutParams();
                    layoutParams.bottomMargin = AndroidUtilities.dp(!Theme.plusHideTabs && Theme.plusTabsToBottom ? Theme.plusTabsHeight + 14 : 14);
                    floatingButton.setLayoutParams(layoutParams);
                }
                if(i == 14){
                    if(newTabsView != null){
                        newTabsView.forceUpdateTabCounters();
                    }
                } else {
                    if(newTabsView != null){
                        newTabsView.reloadTabs();
                    }
                }
            } else if(i == 11){
                refreshTabs();
            }
                /*else if(i <= 7){
                if(newTabsView != null) {
                    newTabsView.addRemoveTab(i);
                }
            } else if(i == 15){
                if(newTabsView != null){
                    newTabsView.reloadTabs();
                }
            }*/

            //Log.e("DialogsActivity","x refreshTabs " + Theme.plusHideTabs);
            updateTabs();
            //hideShowTabs((int) args[0]);
        } else if (id == NotificationCenter.updateDialogsTheme) {
            int i = (int) args[0];
            if(i == Theme.UPDATE_DIALOGS_HEADER_COLOR){
                if(Theme.usePlusTheme) {
                    updateTheme();
                    actionBar.setCastShadows(!Theme.chatsHideHeaderShadow);
                    if (newTabsView != null) {
                        newTabsView.updateTabsColors();
                        //newTabsView.reloadTabs();
                    }
                }
            } else if(i == Theme.UPDATE_DIALOGS_ROW_COLOR){
                //if(Theme.usePlusTheme)updateListBG();
            } else if(i == Theme.UPDATE_DIALOGS_ALL_COLOR){
                //if(Theme.usePlusTheme) {
                //    updateTheme();
                //    updateListBG();
                //}
            }
        }

        if (id == NotificationCenter.needReloadRecentDialogsSearch) {
            if (dialogsSearchAdapter != null) {
                dialogsSearchAdapter.loadRecentSearch();
            }
        } else if (id == NotificationCenter.didLoadedReplyMessages) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.reloadHints) {
            if (dialogsSearchAdapter != null) {
                dialogsSearchAdapter.notifyDataSetChanged();
            }
        }
    }

    private ArrayList<TLRPC.TL_dialog> getDialogsArray() {
        if (dialogsAdapter != null) {
            return dialogsAdapter.getDialogsArray();
        }
        return null;
        /*if (dialogsType == 0) {
            return MessagesController.getInstance().dialogs;
        } else if (dialogsType == 1) {
            return MessagesController.getInstance().dialogsServerOnly;
        } else if (dialogsType == 2) {
            return MessagesController.getInstance().dialogsGroupsOnly;
        }
        //plus
        else if (dialogsType == 3) {
            return MessagesController.getInstance().dialogsUsers;
        } else if (dialogsType == 4) {
            return MessagesController.getInstance().dialogsGroups;
        } else if (dialogsType == 5) {
            return MessagesController.getInstance().dialogsChannels;
        } else if (dialogsType == 6) {
            return MessagesController.getInstance().dialogsBots;
        } else if (dialogsType == 7) {
            return MessagesController.getInstance().dialogsMegaGroups;
        } else if (dialogsType == 8) {
            return MessagesController.getInstance().dialogsFavs;
        } else if (dialogsType == 9) {
            return MessagesController.getInstance().dialogsGroupsAll;
        }
        //
        return null;*/
    }

    public void setSideMenu(RecyclerView recyclerView) {
        sideMenu = recyclerView;
        sideMenu.setBackgroundColor(Theme.getColor(Theme.key_chats_menuBackground));
        sideMenu.setGlowColor(Theme.getColor(Theme.key_chats_menuBackground));
    }

    private void updatePasscodeButton() {
        if (passcodeItem == null) {
            return;
        }
        if (UserConfig.passcodeHash.length() != 0 && !searching) {
            passcodeItem.setVisibility(View.VISIBLE);
            if (UserConfig.appLocked) {
                    passcodeItem.setIcon(R.drawable.lock_close);
            } else {
                    passcodeItem.setIcon(R.drawable.lock_open);
            }
        } else {
            passcodeItem.setVisibility(View.GONE);
        }
    }

    private void hideFloatingButton(boolean hide) {
        if (floatingHidden == hide) {
            return;
        }
        floatingHidden = hide;
        ObjectAnimator animator = ObjectAnimator.ofFloat(floatingButton, "translationY", floatingHidden ? AndroidUtilities.dp(!Theme.plusHideTabs && Theme.plusTabsToBottom ? 150 : 100) : 0).setDuration(300);
        animator.setInterpolator(floatingInterpolator);
        floatingButton.setClickable(!hide);
        animator.start();
    }

    private void updateVisibleRows(int mask) {
        if (listView == null) {
            return;
        }
        // plus
        if (AndroidUtilities.playingAGame) {
            return;
        }
        //
        int count = listView.getChildCount();
        for (int a = 0; a < count; a++) {
            View child = listView.getChildAt(a);
            if (child instanceof DialogCell) {
                if (listView.getAdapter() != dialogsSearchAdapter) {
                    DialogCell cell = (DialogCell) child;
                    if ((mask & MessagesController.UPDATE_MASK_NEW_MESSAGE) != 0) {
                        cell.checkCurrentDialogIndex();
                        if (dialogsType == 0 && AndroidUtilities.isTablet()) {
                            cell.setDialogSelected(cell.getDialogId() == openedDialogId);
                        }
                    } else if ((mask & MessagesController.UPDATE_MASK_SELECT_DIALOG) != 0) {
                        if (dialogsType == 0 && AndroidUtilities.isTablet()) {
                            cell.setDialogSelected(cell.getDialogId() == openedDialogId);
                        }
                    } else {
                        cell.update(mask);
                    }
                }
            } else if (child instanceof UserCell) {
                ((UserCell) child).update(mask);
            } else if (child instanceof ProfileSearchCell) {
                ((ProfileSearchCell) child).update(mask);
            } else if (child instanceof RecyclerListView) {
                RecyclerListView innerListView = (RecyclerListView) child;
                int count2 = innerListView.getChildCount();
                for (int b = 0; b < count2; b++) {
                    View child2 = innerListView.getChildAt(b);
                    if (child2 instanceof HintDialogCell) {
                        ((HintDialogCell) child2).checkUnreadCounter(mask);
                    }
                }
            }
        }
        
        if(updateTabCounters){
            unreadCount();
            updateTabCounters = false;
        }
    }

    private void unreadCount(){
        //Log.e("DialogsActivity", "0 unreadCount updateTabCounters " + updateTabCounters + " size " + MessagesController.getInstance().dialogs.size() + " size " + MessagesController.getInstance().dialogs_dict.size());
        if(!Theme.plusHideTabsCounters){
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.refreshTabsCounters);
        }

    }

    /*private void updateListBG(){
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        if(Theme.chatsRowGradient > 0) {
            GradientDrawable.Orientation go;
            switch(Theme.chatsRowGradient) {
                case 2:
                    go = GradientDrawable.Orientation.LEFT_RIGHT;
                    break;
                case 3:
                    go = GradientDrawable.Orientation.TL_BR;
                    break;
                case 4:
                    go = GradientDrawable.Orientation.BL_TR;
                    break;
                default:
                    go = GradientDrawable.Orientation.TOP_BOTTOM;
            }

            int gradColor = themePrefs.getInt("chatsRowGradientColor", 0xffffffff);
            int[] colors = new int[]{Theme.chatsRowColor, gradColor};
            GradientDrawable gd = new GradientDrawable(go, colors);
            listView.setBackgroundDrawable(gd);
        }else{
            listView.setBackgroundColor(Theme.chatsRowColor);
        }
    }*/

    public void setDelegate(DialogsActivityDelegate dialogsActivityDelegate) {
        delegate = dialogsActivityDelegate;
    }

    public void setSearchString(String string) {
        searchString = string;
    }

    public boolean isMainDialogList() {
        return delegate == null && searchString == null;
    }

    private void didSelectResult(final long dialog_id, boolean useAlert, final boolean param) {
        if (addToGroupAlertString == null) {
            if ((int) dialog_id < 0) {
                TLRPC.Chat chat = MessagesController.getInstance().getChat(-(int) dialog_id);
                if (ChatObject.isChannel(chat) && !chat.megagroup && (cantSendToChannels || !ChatObject.isCanWriteToChannel(-(int) dialog_id))) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                builder.setMessage(LocaleController.getString("ChannelCantSendMessage", R.string.ChannelCantSendMessage));
                builder.setNegativeButton(LocaleController.getString("OK", R.string.OK), null);
                showDialog(builder.create());
                return;
            }
        }
        }
        if (useAlert && (selectAlertString != null && selectAlertStringGroup != null || addToGroupAlertString != null)) {
            if (getParentActivity() == null) {
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
            int lower_part = (int) dialog_id;
            int high_id = (int) (dialog_id >> 32);
            if (lower_part != 0) {
                if (high_id == 1) {
                    TLRPC.Chat chat = MessagesController.getInstance().getChat(lower_part);
                    if (chat == null) {
                        return;
                    }
                    builder.setMessage(LocaleController.formatStringSimple(selectAlertStringGroup, chat.title));
                } else {
                    if (lower_part > 0) {
                        TLRPC.User user = MessagesController.getInstance().getUser(lower_part);
                        if (user == null) {
                            return;
                        }
                        builder.setMessage(LocaleController.formatStringSimple(selectAlertString, UserObject.getUserName(user)));
                    } else if (lower_part < 0) {
                        TLRPC.Chat chat = MessagesController.getInstance().getChat(-lower_part);
                        if (chat == null) {
                            return;
                        }
                        if (addToGroupAlertString != null) {
                            builder.setMessage(LocaleController.formatStringSimple(addToGroupAlertString, chat.title));
                        } else {
                            builder.setMessage(LocaleController.formatStringSimple(selectAlertStringGroup, chat.title));
                        }
                    }
                }
            } else {
                TLRPC.EncryptedChat chat = MessagesController.getInstance().getEncryptedChat(high_id);
                TLRPC.User user = MessagesController.getInstance().getUser(chat.user_id);
                if (user == null) {
                    return;
                }
                builder.setMessage(LocaleController.formatStringSimple(selectAlertString, UserObject.getUserName(user)));
            }

            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    didSelectResult(dialog_id, false, false);
                }
            });
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            showDialog(builder.create());
        } else {
            if (delegate != null) {
                delegate.didSelectDialog(DialogsActivity.this, dialog_id, param);
                delegate = null;
            } else {
                finishFragment();
            }
        }
    }

    @Override
    public ThemeDescription[] getThemeDescriptions() {
        ThemeDescription.ThemeDescriptionDelegate cellDelegate = new ThemeDescription.ThemeDescriptionDelegate() {
            @Override
            public void didSetColor(int color) {
                int count = listView.getChildCount();
                for (int a = 0; a < count; a++) {
                    View child = listView.getChildAt(a);
                    if (child instanceof ProfileSearchCell) {
                        ((ProfileSearchCell) child).update(0);
                    } else if (child instanceof DialogCell) {
                        ((DialogCell) child).update(0);
                    }
                }
                RecyclerListView recyclerListView = dialogsSearchAdapter.getInnerListView();
                if (recyclerListView != null) {
                    count = recyclerListView.getChildCount();
                    for (int a = 0; a < count; a++) {
                        View child = recyclerListView.getChildAt(a);
                        if (child instanceof HintDialogCell) {
                            ((HintDialogCell) child).update();
                        }
                    }
                }
            }
        };
        return new ThemeDescription[]{
                new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite),

                new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault),
                new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCH, null, null, null, null, Theme.key_actionBarDefaultSearch),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, null, null, null, null, Theme.key_actionBarDefaultSearchPlaceholder),

                new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector),

                new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider),

                new ThemeDescription(searchEmptyView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_emptyListPlaceholder),
                new ThemeDescription(searchEmptyView, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, Theme.key_progressCircle),

                new ThemeDescription(emptyTextView1, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_emptyListPlaceholder),
                new ThemeDescription(emptyTextView2, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_emptyListPlaceholder),

                new ThemeDescription(floatingButton, ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, Theme.key_chats_actionIcon),
                new ThemeDescription(floatingButton, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_chats_actionBackground),
                new ThemeDescription(floatingButton, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_chats_actionPressedBackground),

                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.avatar_photoDrawable, Theme.avatar_broadcastDrawable}, null, Theme.key_avatar_text),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundRed),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundOrange),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundViolet),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundGreen),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundCyan),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundBlue),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundPink),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_countPaint, null, null, Theme.key_chats_unreadCounter),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_countGrayPaint, null, null, Theme.key_chats_unreadCounterMuted),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_countTextPaint, null, null, Theme.key_chats_unreadCounterText),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, Theme.dialogs_namePaint, null, null, Theme.key_chats_name),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, Theme.dialogs_nameEncryptedPaint, null, null, Theme.key_chats_secretName),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_lockDrawable}, null, Theme.key_chats_secretIcon),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_groupDrawable, Theme.dialogs_broadcastDrawable, Theme.dialogs_botDrawable}, null, Theme.key_chats_nameIcon),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_pinnedDrawable}, null, Theme.key_chats_pinnedIcon),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_messagePaint, null, null, Theme.key_chats_message),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_chats_nameMessage),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_chats_draft),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_chats_attachMessage),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_messagePrintingPaint, null, null, Theme.key_chats_actionMessage),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_timePaint, null, null, Theme.key_chats_date),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_pinnedPaint, null, null, Theme.key_chats_pinnedOverlay),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_tabletSeletedPaint, null, null, Theme.key_chats_tabletSelectedOverlay),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_checkDrawable, Theme.dialogs_halfCheckDrawable}, null, Theme.key_chats_sentCheck),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_clockDrawable}, null, Theme.key_chats_sentClock),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_errorPaint, null, null, Theme.key_chats_sentError),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_errorDrawable}, null, Theme.key_chats_sentErrorIcon),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_verifiedCheckDrawable}, null, Theme.key_chats_verifiedCheck),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_verifiedDrawable}, null, Theme.key_chats_verifiedBackground),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_muteDrawable}, null, Theme.key_chats_muteIcon),

                new ThemeDescription(sideMenu, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_chats_menuBackground),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuName),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuPhone),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuPhoneCats),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuCloudBackgroundCats),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, new String[]{"cloudDrawable"}, null, null, null, Theme.key_chats_menuCloud),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chat_serviceBackground),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuTopShadow),

                new ThemeDescription(sideMenu, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{DrawerActionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_chats_menuItemIcon),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerActionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_chats_menuItemText),

                new ThemeDescription(sideMenu, 0, new Class[]{DividerCell.class}, Theme.dividerPaint, null, null, Theme.key_divider),

                new ThemeDescription(listView, 0, new Class[]{LoadingCell.class}, new String[]{"progressBar"}, null, null, null, Theme.key_progressCircle),

                new ThemeDescription(listView, 0, new Class[]{ProfileSearchCell.class}, Theme.dialogs_offlinePaint, null, null, Theme.key_windowBackgroundWhiteGrayText3),
                new ThemeDescription(listView, 0, new Class[]{ProfileSearchCell.class}, Theme.dialogs_onlinePaint, null, null, Theme.key_windowBackgroundWhiteBlueText3),

                new ThemeDescription(listView, 0, new Class[]{GraySectionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2),
                new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{GraySectionCell.class}, null, null, null, Theme.key_graySection),

                new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{HashtagSearchCell.class}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),

                new ThemeDescription(progressView, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, Theme.key_progressCircle),

                new ThemeDescription(dialogsSearchAdapter.getInnerListView(), 0, new Class[]{HintDialogCell.class}, Theme.dialogs_countPaint, null, null, Theme.key_chats_unreadCounter),
                new ThemeDescription(dialogsSearchAdapter.getInnerListView(), 0, new Class[]{HintDialogCell.class}, Theme.dialogs_countGrayPaint, null, null, Theme.key_chats_unreadCounterMuted),
                new ThemeDescription(dialogsSearchAdapter.getInnerListView(), 0, new Class[]{HintDialogCell.class}, Theme.dialogs_countTextPaint, null, null, Theme.key_chats_unreadCounterText),
                new ThemeDescription(dialogsSearchAdapter.getInnerListView(), 0, new Class[]{HintDialogCell.class}, new String[]{"nameTextView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),

                new ThemeDescription(fragmentContextView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{FragmentContextView.class}, new String[]{"frameLayout"}, null, null, null, Theme.key_inappPlayerBackground),
                new ThemeDescription(fragmentContextView, 0, new Class[]{FragmentContextView.class}, new String[]{"playButton"}, null, null, null, Theme.key_inappPlayerPlayPause),
                new ThemeDescription(fragmentContextView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{FragmentContextView.class}, new String[]{"titleTextView"}, null, null, null, Theme.key_inappPlayerTitle),
                new ThemeDescription(fragmentContextView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{FragmentContextView.class}, new String[]{"frameLayout"}, null, null, null, Theme.key_inappPlayerPerformer),
                new ThemeDescription(fragmentContextView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{FragmentContextView.class}, new String[]{"closeButton"}, null, null, null, Theme.key_inappPlayerClose),

                new ThemeDescription(fragmentContextView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{FragmentContextView.class}, new String[]{"frameLayout"}, null, null, null, Theme.key_returnToCallBackground),
                new ThemeDescription(fragmentContextView, 0, new Class[]{FragmentContextView.class}, new String[]{"titleTextView"}, null, null, null, Theme.key_returnToCallText),

                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogBackgroundGray),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlack),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextLink),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogLinkSelection),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlue),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlue2),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlue3),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlue4),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextRed),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextGray),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextGray2),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextGray3),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextGray4),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogIcon),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextHint),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogInputField),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogInputFieldActivated),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogCheckboxSquareBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogCheckboxSquareCheck),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogCheckboxSquareUnchecked),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogCheckboxSquareDisabled),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRadioBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRadioBackgroundChecked),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogProgressCircle),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogButton),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogButtonSelector),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogScrollGlow),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRoundCheckBox),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRoundCheckBoxCheck),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogBadgeBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogBadgeText),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogLineProgress),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogLineProgressBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogGrayLine),
        };
    }

    private String getHeaderTitle(){
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        int value = themePrefs.getInt("chatsHeaderTitle", 0);
        String title = BuildVars.DEBUG_VERSION ? LocaleController.getString("AppNameBeta", R.string.AppNameBeta) : LocaleController.getString("AppName", R.string.AppName);
        TLRPC.User user = UserConfig.getCurrentUser();
        if( value == 1){
            title = LocaleController.getString("ShortAppName", R.string.ShortAppName);
        } else if( value == 2){
            if (user != null && (user.first_name != null || user.last_name != null)) {
                title = ContactsController.formatName(user.first_name, user.last_name);
            }
        } else if(value == 3){
            if (user != null && user.username != null && user.username.length() != 0) {
                title = "@" + user.username;
            }
        } else if(value == 4){
            title = "";
        }
        //Log.e("DialogsActivity", value + " getHeaderTitle " + title);
        return title;
    }

    private String getTitle(int type, boolean all){
        //Log.e("DialogsActivity", "getTitle type " + type);
        switch(type) {
            case 3:
                return LocaleController.getString("Users", R.string.Users);
            case 4:
            case 9:
                return LocaleController.getString("Groups", R.string.Groups);
            case 5:
                return LocaleController.getString("Channels", R.string.Channels);
            case 6:
                return LocaleController.getString("Bots", R.string.Bots);
            case 7:
                return LocaleController.getString("SuperGroups", R.string.SuperGroups);
            case 8:
                return LocaleController.getString("Favorites", R.string.Favorites);
            case 10:
                return LocaleController.getString("ChannelEditor", R.string.ChannelEditor);
            case 11:
                return LocaleController.getString("Unread", R.string.Unread);
            default:
                return all ? LocaleController.getString("All", R.string.All) : getHeaderTitle();
        }
    }

    private void paintHeader(boolean tabs){
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        actionBar.setTitleColor(themePrefs.getInt("chatsHeaderTitleColor", 0xffffffff));

        if(!tabs)actionBar.setBackgroundColor(Theme.chatsHeaderColor);
        if(tabs){
            newTabsView.setBackgroundColor(Theme.chatsTabsBGColor == Theme.defColor ? Theme.chatsHeaderColor : Theme.chatsTabsBGColor);
        }
        int val = Theme.chatsHeaderGradient;
        if(val > 0) {
            GradientDrawable.Orientation go;
            switch(val) {
                case 2:
                    go = GradientDrawable.Orientation.LEFT_RIGHT;
                    break;
                case 3:
                    go = GradientDrawable.Orientation.TL_BR;
                    break;
                case 4:
                    go = GradientDrawable.Orientation.BL_TR;
                    break;
                default:
                    go = GradientDrawable.Orientation.TOP_BOTTOM;
            }
            int gradColor = Theme.chatsHeaderGradientColor;
            int[] colors = new int[]{Theme.chatsHeaderColor, gradColor};
            GradientDrawable gd = new GradientDrawable(go, colors);
            if(!tabs)actionBar.setBackgroundDrawable(gd);
            if(tabs){
                if(Theme.chatsTabsBGColor == Theme.defColor)newTabsView.setBackgroundDrawable(gd);
            }
        }
    }

	private void updateTheme(){
        paintHeader(false);
        //SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        try{
            //plus
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Bitmap bm = BitmapFactory.decodeResource(getParentActivity().getResources(), R.drawable.ic_launcher);
                ActivityManager.TaskDescription td = new ActivityManager.TaskDescription(getHeaderTitle(), bm, Theme.chatsHeaderColor);
                getParentActivity().setTaskDescription(td);
                bm.recycle();
            }


        } catch (NullPointerException e) {
            FileLog.e( e);
        }
        try{
            Drawable search = getParentActivity().getResources().getDrawable(R.drawable.ic_ab_search);
            if(search != null)search.setColorFilter(Theme.chatsHeaderIconsColor, PorterDuff.Mode.MULTIPLY);
            Drawable lockO = getParentActivity().getResources().getDrawable(R.drawable.lock_close);
            if(lockO != null)lockO.setColorFilter(Theme.chatsHeaderIconsColor, PorterDuff.Mode.MULTIPLY);
            Drawable lockC = getParentActivity().getResources().getDrawable(R.drawable.lock_open);
            if(lockC != null)lockC.setColorFilter(Theme.chatsHeaderIconsColor, PorterDuff.Mode.MULTIPLY);
            Drawable clear = getParentActivity().getResources().getDrawable(R.drawable.ic_close_white);
            if(clear != null)clear.setColorFilter(Theme.chatsHeaderIconsColor, PorterDuff.Mode.MULTIPLY);
        } catch (OutOfMemoryError e) {
            FileLog.e( e);
        }
        refreshTabs();
    }

    private void refreshAdapterAndTabs(){
        if(dialogsAdapter != null) {
            dialogsAdapter.setDialogsType(dialogsType);
            dialogsAdapter.notifyDataSetChanged();
        }
        refreshTabs();
    }

    private void refreshTabs(){
        //Log.e("DialogsActivity", Theme.plusDoNotChangeHeaderTitle + " refreshTabs dialogsType " + dialogsType);
        actionBar.setTitle(Theme.plusDoNotChangeHeaderTitle ? getHeaderTitle() : getTitle(dialogsType, false));
        if(Theme.usePlusTheme)paintHeader(true);
    }

    private void checkEmptyView(){
        ArrayList<TLRPC.TL_dialog> dialogs = getDialogsArray();
        if (dialogs.isEmpty()) {
            if(emptyView.getChildCount() > 0){
                TextView tv = (TextView) emptyView.getChildAt(0);
                if(tv != null){
                    tv.setText(dialogsType < 3 ? LocaleController.getString("NoChats", R.string.NoChats) : dialogsType == 8 ? LocaleController.getString("NoFavoritesHelp", R.string.NoFavoritesHelp) : getTitle(dialogsType, false));
                    tv.setTextColor(Theme.usePlusTheme ? Theme.chatsNameColor : Theme.getColor(Theme.key_emptyListPlaceholder));
                }
                if(emptyView.getChildAt(1) != null)emptyView.getChildAt(1).setVisibility(View.GONE);
            }
            emptyView.setVisibility(View.VISIBLE);
            if(Theme.usePlusTheme){
                emptyView.setBackgroundColor(Theme.chatsRowColor);
            }
        }
    }

    private void updateTabs(){
        refreshTabAndListViews(false);
        if (Theme.plusHideTabs && dialogsType > 2) {
            Theme.plusDialogType = dialogsType = 0;
            if(dialogsAdapter != null) {
                dialogsAdapter.setDialogsType(dialogsType);
            }
            refreshAdapterAndTabs();
        }
    }

    private void refreshTabAndListViews(boolean forceHide){
        if(newTabsView != null) {
            if (Theme.plusHideTabs || forceHide) {
                newTabsView.setVisibility(View.GONE);
                listView.setPadding(0, 0, 0, 0);
            } else {
                newTabsView.setVisibility(View.VISIBLE);
                int h = AndroidUtilities.dp(Theme.plusTabsHeight);
                ViewGroup.LayoutParams params = newTabsView.getLayoutParams();
                if (params != null) {
                    params.height = h;
                    newTabsView.setLayoutParams(params);
                }
                listView.setPadding(0, Theme.plusTabsToBottom ? 0 : h, 0, Theme.plusTabsToBottom ? h : 0);
                hideTabsAnimated(false);
            }
        }
        listView.scrollToPosition(0);
    }

    private void neeLoadMoreChats(){
        int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
        int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
        int visibleItemCount = Math.abs(lastVisibleItem - firstVisibleItem) + 1;
        int totalItemCount = listView.getAdapter().getItemCount();
        //Log.e("DialogsActivity", "neeLoadMoreChats firstVisibleItem " + firstVisibleItem + " lastVisibleItem " + lastVisibleItem + " visibleItemCount " + visibleItemCount + " totalItemCount " + totalItemCount);
        if (!MessagesController.getInstance().dialogsEndReached && !MessagesController.getInstance().loadingDialogs && lastVisibleItem > 0 && totalItemCount == visibleItemCount) {
            //Log.e("DialogsActivity", "2 neeLoadMoreChats " + " dialogsType " + dialogsType + " dialogsEndReached " + MessagesController.getInstance().dialogsEndReached + " nextDialogsCacheOffset " + MessagesController.getInstance().nextDialogsCacheOffset);
            if (Theme.plusChatsToLoad < 5000)
                MessagesController.getInstance().loadDialogs(-1, Theme.plusChatsToLoad, true);
        }

    }

    private void hideTabsAnimated(final boolean hide){
        if (tabsHidden == hide) {
            return;
        }
        tabsHidden = hide;
        if(hide)listView.setPadding(0, 0, 0, 0);

        ObjectAnimator animator = ObjectAnimator.ofFloat(newTabsView, "translationY", hide ? -AndroidUtilities.dp(Theme.plusTabsHeight) * (Theme.plusTabsToBottom ? -1 : 1) : 0).setDuration(300);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if(!tabsHidden)listView.setPadding(0, Theme.plusTabsToBottom ? 0 : AndroidUtilities.dp(Theme.plusTabsHeight), 0, Theme.plusTabsToBottom ? AndroidUtilities.dp(Theme.plusTabsHeight) : 0);
            }
        });
        animator.start();
    }

    @Override
    public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
        if (fileLocation == null) {
            return null;
        }

        TLRPC.FileLocation photoBig = null;
        if (user_id != 0) {
            TLRPC.User user = MessagesController.getInstance().getUser(user_id);
            if (user != null && user.photo != null && user.photo.photo_big != null) {
                photoBig = user.photo.photo_big;
            }
        } else if (chat_id != 0) {
            TLRPC.Chat chat = MessagesController.getInstance().getChat(chat_id);
            if (chat != null && chat.photo != null && chat.photo.photo_big != null) {
                photoBig = chat.photo.photo_big;
            }
        }

        if (avatarImage != null && photoBig != null && photoBig.local_id == fileLocation.local_id && photoBig.volume_id == fileLocation.volume_id && photoBig.dc_id == fileLocation.dc_id) {
            int coords[] = new int[2];
            avatarImage.getLocationInWindow(coords);
            PhotoViewer.PlaceProviderObject object = new PhotoViewer.PlaceProviderObject();
            object.viewX = coords[0];
            object.viewY = coords[1] - AndroidUtilities.statusBarHeight;
            object.parentView = avatarImage;
            object.imageReceiver = avatarImage.getImageReceiver();
            //object.user_id = user_id;
            object.thumb = object.imageReceiver.getBitmap();
            object.size = -1;
            object.radius = avatarImage.getImageReceiver().getRoundRadius();
            return object;
        }
        return null;
    }

    @Override
    public Bitmap getThumbForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
        return null;
    }

    @Override
    public void willSwitchFromPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {

    }

    @Override
    public void willHidePhotoViewer() {

    }

    @Override
    public boolean isPhotoChecked(int index) {
        return false;
    }

    @Override
    public void setPhotoChecked(int index, VideoEditedInfo videoEditedInfo) {

    }

    @Override
    public boolean cancelButtonPressed() {
        return true;
    }

    @Override
    public void sendButtonPressed(int index, VideoEditedInfo videoEditedInfo) {

    }

    @Override
    public int getSelectedCount() {
        return 0;
    }

    @Override
    public void updatePhotoAtIndex(int index) {

    }

    @Override
    public boolean allowCaption() {
        return false;
    }

    @Override
    public boolean scaleToFill() {
        return false;
    }
}
