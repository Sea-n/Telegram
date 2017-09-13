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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlusChatsStatsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, DialogInterface.OnCancelListener {

    //private ListView listView;
    private ListAdapter listAdapter;
    private ProgressDialog pDialog;
    private List<Integer> arrayIds;
    private List<CharSequence> arrayType;
    private ArrayList<TLRPC.TL_dialog> other = new ArrayList<>();

    private int rowCount;
    private static boolean dialogsDidLoad;
    private int loadSize;
    private int count;
    private int loadChatQ = 100;

    private int totalHeaderRow;
    private int totalRow;
    private int totalUsersRow;
    private int totalGroupsRow;
    private int totalSuperGroupsRow;
    private int totalChannelsRow;
    private int totalBotsRow;
    private int totalFavsRow;
    private int totalSecretsRow;
    private int totalOtherRow;

    private int ownHeaderDividerRow;
    private int ownHeaderRow;
    private int ownGroupsRow;
    private int ownSuperGroupsRow;
    private int ownChannelsRow;

    private int adminHeaderDividerRow;
    private int adminHeaderRow;
    private int adminGroupsRow;
    private int adminSuperGroupsRow;
    private int adminChannelsRow;

    private int totalChatsCount;
    private int usersCount;
    private int groupsCount;
    private int superGroupsCount;
    private int channelsCount;
    private int botsCount;
    private int favsCount;
    private int secretsCount;
    private int otherCount;
    private int ownGroupsCount;
    private int ownSuperGroupsCount;
    private int ownChannelsCount;
    private int adminGroupsCount;
    private int adminSuperGroupsCount;
    private int adminChannelsCount;
    private int otherPosition;
    //private boolean loaded;
    private boolean progressCancelled;
    private Runnable dismissProgressRunnable;

    //private int temp;
    //private int counter;
    //private int counter2;
    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.dialogsNeedReload) {
            //counter2++;
            /*if(temp != loadSize) {

            }*/
            //Log.e("ChatsStats", "didReceivedNotification dialogsNeedReload loadingDialogs " + MessagesController.getInstance().loadingDialogs + " dialogsEndReached " + MessagesController.getInstance().dialogsEndReached);
            if(!MessagesController.getInstance().dialogs.isEmpty()){
                if(!MessagesController.getInstance().loadingDialogs){
                    if(!MessagesController.getInstance().dialogsEndReached && !progressCancelled){
                        int size = MessagesController.getInstance().dialogs.size();
                        count = (int) (loadChatQ * Math.ceil(size / loadChatQ));
                        //Log.e("ChatsStats", "didReceivedNotification dialogsNeedReload loadingDialogs " + size + " count " + count + " loadSize " + loadSize);
                        if(loadSize < size) {
                            loadSize = size;
                            CharSequence title = LocaleController.getString("Loading", R.string.Loading) + " " + count;
                            if(pDialog != null) {
                                pDialog.setMessage(title);
                            }
                            MessagesController.getInstance().loadDialogs(-1, loadChatQ, true);
                            if (dismissProgressRunnable != null) {
                                AndroidUtilities.cancelRunOnUIThread(dismissProgressRunnable);
                            }
                            AndroidUtilities.runOnUIThread(dismissProgressRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    if (dismissProgressRunnable != this) {
                                        return;
                                    }
                                    completeTask();
                                }
                            }, 3000);
                        }
                    } else {
                        completeTask();
                    }
                } /*else{
                    if(temp != loadSize) {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                if (getParentActivity() != null) {
                                    temp = loadSize;
                                    Toast toast = Toast.makeText(getParentActivity(), counter2 + " / " + counter +
                                            "\ncount: " + count + " / loadSize: " + loadSize +
                                            "\ndialogsNeedReload loadSize " + MessagesController.getInstance().dialogs.size() +
                                            "\nloadingDialogs " + MessagesController.getInstance().loadingDialogs +
                                            "\ndialogsEndReached " + MessagesController.getInstance().dialogsEndReached, Toast.LENGTH_SHORT);
                                    toast.show();
                                    //cancelProgress();
                                }
                            }
                        });
                    } else{
                        counter++;
                        if(counter == 2){
                            postDismissProgress();
                        }
                    }
                }*/
            } else{
                completeTask();
            }
        }
    }

    private void completeTask(){
        dismissProgress();
        loadAll();
    }

    /*private void postDismissProgress(){
        Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                dismissProgress();

            }
        };
        Handler pdCanceller = new Handler();
        pdCanceller.postDelayed(progressRunnable, 2000);
    }*/

    private void dismissProgress(){
        if(dismissProgressRunnable != null){
            dismissProgressRunnable = null;
        }
        if(pDialog != null){
            //if(pDialog.isShowing())
                pDialog.dismiss();
            pDialog = null;
        }
        //Log.e("ChatsStats", "dismissProgress " + MessagesController.getInstance().dialogs.size());
    }

    private void loadAll(){
        totalChatsCount = MessagesController.getInstance().dialogs.size();
        //Log.e("ChatsStats", "loadAll totalChatsCount " + totalChatsCount);
        usersCount = MessagesController.getInstance().dialogsUsers.size();
        groupsCount = MessagesController.getInstance().dialogsGroups.size();
        superGroupsCount = MessagesController.getInstance().dialogsMegaGroups.size();
        channelsCount = MessagesController.getInstance().dialogsChannels.size();
        botsCount = MessagesController.getInstance().dialogsBots.size();
        favsCount = MessagesController.getInstance().dialogsFavs.size();

        /*if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }*/
        loadAdminChats();
        //loaded = true;
    }

    private void loadAdminChats(){
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if(loadSize <= totalChatsCount) {
                    other.clear();
                    MessagesController.getInstance().dialogsSecrets.clear();
                    MessagesController.getInstance().dialogsOwnGroups.clear();
                    MessagesController.getInstance().dialogsOwnSuperGroups.clear();
                    MessagesController.getInstance().dialogsOwnChannels.clear();
                    MessagesController.getInstance().dialogsAdminGroups.clear();
                    MessagesController.getInstance().dialogsAdminSuperGroups.clear();
                    MessagesController.getInstance().dialogsAdminChannels.clear();
                    for (int a = 0; a < MessagesController.getInstance().dialogs.size(); a++) {
                        TLRPC.TL_dialog d = MessagesController.getInstance().dialogs.get(a);
                        int high_id = (int) (d.id >> 32);
                        int lower_id = (int) d.id;
                        if (lower_id != 0 && high_id != 1) {
                            if (DialogObject.isChannel(d)) {
                                TLRPC.Chat chat = MessagesController.getInstance().getChat(-lower_id);
                                if (chat != null) {
                                    if (chat.megagroup) {
                                        if (chat.creator) {
                                            MessagesController.getInstance().dialogsOwnSuperGroups.add(d);
                                        } else {
                                            if (/*chat.editor*/ChatObject.hasAdminRights(chat)) {
                                                MessagesController.getInstance().dialogsAdminSuperGroups.add(d);
                                            }
                                        }
                                    } else {
                                        if (chat.creator) {
                                            MessagesController.getInstance().dialogsOwnChannels.add(d);
                                        } else {
                                            if (/*chat.editor*/ChatObject.hasAdminRights(chat)) {
                                                MessagesController.getInstance().dialogsAdminChannels.add(d);
                                            }
                                        }
                                    }
                                }
                            } else if (lower_id < 0) {
                                TLRPC.Chat chat = MessagesController.getInstance().getChat(-lower_id);
                                if (chat != null) {
                                    if (chat.creator) {
                                        MessagesController.getInstance().dialogsOwnGroups.add(d);
                                    } else {
                                        if (chat.admins_enabled && chat.admin) {
                                            MessagesController.getInstance().dialogsAdminGroups.add(d);
                                        }
                                    }
                                }
                            } /*else {
                            TLRPC.User user = MessagesController.getInstance().getUser((int) d.id);
                            if(user != null){
                                if(user.bot){
                                    //dialogsBots.add(d);
                                }else{
                                    //dialogsUsers.add(d);
                                }
                            }
                        }*/
                        } else{
                            TLRPC.EncryptedChat encryptedChat = MessagesController.getInstance().getEncryptedChat(high_id);
                            //if(chat instanceof TLRPC.TL_encryptedChat) {
                            if(encryptedChat != null) {
                                MessagesController.getInstance().dialogsSecrets.add(d);
                            }
                        }
                    }
                }
                secretsCount = MessagesController.getInstance().dialogsSecrets.size();
                ownGroupsCount = MessagesController.getInstance().dialogsOwnGroups.size();
                ownSuperGroupsCount = MessagesController.getInstance().dialogsOwnSuperGroups.size();
                ownChannelsCount = MessagesController.getInstance().dialogsOwnChannels.size();

                adminGroupsCount = MessagesController.getInstance().dialogsAdminGroups.size();
                adminSuperGroupsCount = MessagesController.getInstance().dialogsAdminSuperGroups.size();
                adminChannelsCount = MessagesController.getInstance().dialogsAdminChannels.size();
                //Log.e("ChatsStats", loadSize + "/" +totalChatsCount + " secretsCount " + secretsCount + " ownGroupsCount " + ownGroupsCount + " ownSuperGroupsCount " + ownSuperGroupsCount + " ownSuperGroupsCount " + ownChannelsCount + " adminGroupsCount " + adminGroupsCount + " adminSuperGroupsCount " + adminSuperGroupsCount + " adminChannelsCount " + adminChannelsCount);
                updateOther();
                if (listAdapter != null) {
                    listAdapter.notifyDataSetChanged();
                    //Log.e("ChatsStats", "loadAdminChats notifyDataSetChanged");
                }
                //updateCounters();
            }
        });

    }

    private void updateCounters(){
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("chatsstats", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("totalChatsCount", totalChatsCount);
        editor.putInt("usersCount", usersCount);
        editor.putInt("groupsCount", groupsCount);
        editor.putInt("superGroupsCount", superGroupsCount);
        editor.putInt("channelsCount", channelsCount);
        editor.putInt("botsCount", botsCount);
        editor.putInt("favsCount", favsCount);
        editor.putInt("secretsCount", secretsCount);
        editor.putInt("ownGroupsCount", ownGroupsCount);
        editor.putInt("ownSuperGroupsCount", ownSuperGroupsCount);
        editor.putInt("ownChannelsCount", ownChannelsCount);
        editor.putInt("adminGroupsCount", adminGroupsCount);
        editor.putInt("adminSuperGroupsCount", adminSuperGroupsCount);
        editor.putInt("adminChannelsCount", adminChannelsCount);
        editor.putLong("time", System.currentTimeMillis());
        editor.apply();
    }

    private void updateOther(){
        other = new ArrayList<>(MessagesController.getInstance().dialogs);
        other.removeAll(MessagesController.getInstance().dialogsUsers);
        //other.removeAll(MessagesController.getInstance().dialogsSecrets);
        other.removeAll(MessagesController.getInstance().dialogsGroups);
        other.removeAll(MessagesController.getInstance().dialogsMegaGroups);
        other.removeAll(MessagesController.getInstance().dialogsChannels);
        other.removeAll(MessagesController.getInstance().dialogsBots);
        otherCount = other.size();
        totalChatsCount = usersCount /*+ secretsCount + otherCount*/ + groupsCount + superGroupsCount + channelsCount + botsCount;
        if(otherCount <= 0){
            totalOtherRow = -1;
        } else{
            totalOtherRow = otherPosition;
        }
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        NotificationCenter.getInstance().addObserver(this, NotificationCenter.dialogsNeedReload);
        //counter = counter2 = 0;
        rowCount = 0;
        totalHeaderRow = rowCount++;
        totalRow = -1;//rowCount++;
        totalUsersRow = rowCount++;
        totalGroupsRow = rowCount++;
        totalSuperGroupsRow = rowCount++;
        totalChannelsRow = rowCount++;
        totalBotsRow = rowCount++;
        totalSecretsRow = rowCount++;
        otherPosition = rowCount++;
        totalOtherRow = -1;
        totalFavsRow = rowCount++;

        ownHeaderDividerRow = rowCount++;
        ownHeaderRow = rowCount++;
        ownGroupsRow = rowCount++;
        ownSuperGroupsRow = rowCount++;
        ownChannelsRow = rowCount++;

        adminHeaderDividerRow = rowCount++;
        adminHeaderRow = rowCount++;
        adminGroupsRow = rowCount++;
        adminSuperGroupsRow = rowCount++;
        adminChannelsRow = rowCount++;

        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        dismissProgress();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.dialogsNeedReload);
        updateCounters();

        //avatarImage = null;
        //avatarDrawable = null;
        //loaded = false;
        //Log.e("ChatsStats", "onFragmentDestroy loaded " + loaded);
    }

    @Override
    public View createView(final Context context) {
        //actionBar.setItemsBackground(AvatarDrawable.getButtonColorForId(5));
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);

        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setTitle(LocaleController.getString("ChatsCounters", R.string.ChatsCounters));
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("chatsstats", Activity.MODE_PRIVATE);
        long t = preferences.getLong("time", -1);
        if(t != -1) {
            try{
                actionBar.setSubtitle(Html.fromHtml("<small>" + LocaleController.getString("LastAccess", R.string.LastAccess) + " " +new SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale.US).format(new Date(t)) + "</small>"));
            } catch (Exception e) {
                FileLog.e( e);
            }
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        ListView listView = new ListView(context);

        if(Theme.usePlusTheme)listView.setBackgroundColor(Theme.prefBGColor);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setVerticalScrollBarEnabled(false);

        listView.setAdapter(listAdapter);

        //AndroidUtilities.setListViewEdgeEffectColor(listView, Theme.prefActionbarColor);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));

        pDialog = new ProgressDialog(context);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.setCancelable(true);
        pDialog.setOnCancelListener(this);

        /*Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                pDialog.cancel();
            }
        };
        Handler pDialogCanceller = new Handler();
        pDialogCanceller.postDelayed(progressRunnable, 3000);*/

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                if (getParentActivity() == null) {
                    return;
                }
                if (i == ownGroupsRow) {
                    if(ownGroupsCount > 0)showChats(LocaleController.getString("Groups", R.string.Groups), MessagesController.getInstance().dialogsOwnGroups);
                } else if (i == ownSuperGroupsRow) {
                    if(ownSuperGroupsCount > 0)showChats(LocaleController.getString("SuperGroups", R.string.SuperGroups), MessagesController.getInstance().dialogsOwnSuperGroups);
                } else if (i == ownChannelsRow) {
                    if(ownChannelsCount > 0)showChats(LocaleController.getString("Channels", R.string.Channels), MessagesController.getInstance().dialogsOwnChannels);
                } else if (i == adminGroupsRow) {
                    if(adminGroupsCount > 0)showChats(LocaleController.getString("Groups", R.string.Groups), MessagesController.getInstance().dialogsAdminGroups);
                } else if (i == adminSuperGroupsRow) {
                    if(adminSuperGroupsCount > 0)showChats(LocaleController.getString("SuperGroups", R.string.SuperGroups), MessagesController.getInstance().dialogsAdminSuperGroups);
                } else if (i == adminChannelsRow) {
                    if(adminChannelsCount > 0)showChats(LocaleController.getString("Channels", R.string.Channels), MessagesController.getInstance().dialogsAdminChannels);
                } else if (i == totalSecretsRow) {
                    if(secretsCount > 0)showSecrets(LocaleController.getString("SecretChat", R.string.SecretChat), MessagesController.getInstance().dialogsSecrets);
                } else if (i == totalOtherRow){
                    if(otherCount > 0)showOther(LocaleController.getString("ReportChatOther", R.string.ReportChatOther), other);
                }
            }
        });
        //Log.e("ChatsStats", "Create dialogsNeedReload loadingDialogs " + MessagesController.getInstance().loadingDialogs + " dialogsEndReached " + MessagesController.getInstance().dialogsEndReached);

        /*AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (getParentActivity() != null) {
                    Toast toast = Toast.makeText(getParentActivity(),
                            "dialogsEndReached " + MessagesController.getInstance().dialogsEndReached +
                            "\ndialogsDidLoad " + dialogsDidLoad +
                            "\nloadingDialogs " + MessagesController.getInstance().loadingDialogs +
                            "\nsize " + MessagesController.getInstance().dialogs.size(), Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });*/
        if(!MessagesController.getInstance().dialogsEndReached && !dialogsDidLoad) {
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    //if (!dialogsDidLoad) {
                        pDialog.setMessage(LocaleController.getString("Loading", R.string.Loading));
                        pDialog.show();
                        MessagesController.getInstance().loadDialogs(0, loadChatQ, true);
                        dialogsDidLoad = true;
                    //}
                }
            }, 200);
        } else{
            loadAll();
        }

        frameLayout.addView(actionBar);

        return fragmentView;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        progressCancelled = true;
        dismissProgress();
        loadAll();
    }

    private void showChats(String title, ArrayList<TLRPC.TL_dialog> dlgs){
        //showChatsWithAvatars(title, dlgs);
        //if(true)return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(title);
        List<CharSequence> array = new ArrayList<>();
        arrayIds = new ArrayList<>();
        for (int a = 0; a < dlgs.size(); a++) {
            TLRPC.TL_dialog d = dlgs.get(a);
            int high_id = (int) (d.id >> 32);
            int lower_id = (int) d.id;
            TLRPC.Chat chat = null;
            boolean isPublic = false;
            if (lower_id != 0 && high_id != 1) {
                chat = MessagesController.getInstance().getChat(-lower_id);
                isPublic = ChatObject.isChannel(chat) && chat.username != null && chat.username.length() > 0;
            }
            if (chat != null) {
                array.add(chat.title + (!ChatObject.isChannel(chat) ? "" : isPublic ? " (" + LocaleController.getString("ChannelTypePublic", R.string.ChannelTypePublic) + ")" : ""));
                arrayIds.add(chat.id);
            }
        }

        //String[] simpleArray = new String[ array.size() ];
        //array.toArray( new String[ array.size() ]);
        builder.setItems(array.toArray( new CharSequence[array.size()]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int id = arrayIds.get(which);
                Bundle args = new Bundle();
                args.putInt("chat_id", id);
                presentFragment(new ChatActivity(args));
            }
        });

        builder.setNegativeButton(LocaleController.getString("OK", R.string.OK), null);
        showDialog(builder.create());
    }

    /*private ArrayList<ImageReceiver> imageArray;
    private ArrayList<Drawable> avatarArray;
    private ArrayList<BackupImageView> backupImageArray;

    private ImageReceiver avatarImage;
    private AvatarDrawable avatarDrawable;
    List<Item> items;
    //private ArrayList<Item> items;

    private static class Item{
        private final CharSequence text;
        private Drawable drawable;
        private Item(CharSequence text, Drawable drawable) {
            this.text = text;
            this.drawable = drawable;
        }
    }

    private void showChatsWithAvatars(String title, ArrayList<TLRPC.TL_dialog> dlgs){
        //AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        //builder.setTitle(title);
        List<CharSequence> array = new ArrayList<>();
        items = new ArrayList<>();
        arrayIds = new ArrayList<>();
        avatarArray = new ArrayList<>();
        imageArray = new ArrayList<>();
        backupImageArray = new ArrayList<>();
        for (int a = 0; a < dlgs.size(); a++) {
            TLRPC.TL_dialog d = dlgs.get(a);
            int high_id = (int) (d.id >> 32);
            int lower_id = (int) d.id;
            TLRPC.Chat chat = null;
            boolean isPublic = false;
            if (lower_id != 0 && high_id != 1) {
                chat = MessagesController.getInstance().getChat(-lower_id);
                isPublic = ChatObject.isChannel(chat) && chat.username != null && chat.username.length() > 0;
            }
            if (chat != null) {
                array.add(chat.title + (!ChatObject.isChannel(chat) ? "" : isPublic ? " (" + LocaleController.getString("ChannelTypePublic", R.string.ChannelTypePublic) + ")" : ""));
                arrayIds.add(chat.id);
                avatarImage = new ImageReceiver();
                avatarImage.setRoundRadius(AndroidUtilities.dp(Theme.chatsAvatarRadius));
                avatarDrawable = new AvatarDrawable();
                avatarDrawable.setRadius(AndroidUtilities.dp(Theme.chatsAvatarRadius));
                int avatarSize = AndroidUtilities.dp(12);

                avatarImage.setImageCoords(0, 0, avatarSize, avatarSize);
                TLRPC.FileLocation photo = null;
                if (chat.photo != null) {
                    photo = chat.photo.photo_small;
                }
                avatarDrawable.setInfo(chat);
                avatarImage.setImage(photo, "50_50", avatarDrawable, null, false);

                BackupImageView avatarImageView = new BackupImageView(getParentActivity());
                avatarImageView.setRoundRadius(AndroidUtilities.dp(35));
                avatarImageView.setImage(photo, "50_50", avatarDrawable);
                backupImageArray.add(avatarImageView);
                imageArray.add(avatarImage);

                avatarArray.add(avatarDrawable);
                items.add(new Item(array.get(a), new BitmapDrawable(avatarImage.getBitmap())));
                avatarImage = null;
                avatarDrawable = null;
            }
        }


        ArrayAdapter adapter = new ArrayAdapter<Item>(getParentActivity(), android.R.layout.select_dialog_item, android.R.id.text1, items){
            public View getView(int position, View convertView, ViewGroup parent) {

                //parent.addView(backupImageArray.get(position), LayoutHelper.createLinear(15, 15, Gravity.TOP | Gravity.LEFT, 0, 12, 0, 0));
                View view = backupImageArray.get(position);
                View v = super.getView(position, convertView, parent);
                //TextView tv = (TextView)v.findViewById(android.R.id.text1);
                //tv.setText(items.get(position).text);
                //tv.setCompoundDrawablesWithIntrinsicBounds(avatarArray.get(position), null, null, null);
                //tv.setCompoundDrawablePadding(AndroidUtilities.dp(5));
                return view;
            }
        };

        AlertDialog.Builder builder2 = new AlertDialog.Builder(getParentActivity());
        builder2.setTitle(title);

        builder2.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        int id = arrayIds.get(item);
                        Bundle args = new Bundle();
                        args.putInt("chat_id", id);
                        presentFragment(new ChatActivity(args));
                    }
                });

        builder2.setNegativeButton(LocaleController.getString("OK", R.string.OK), null);
        showDialog(builder2.create());
    }*/


    private void showSecrets(String title, ArrayList<TLRPC.TL_dialog> dlgs){
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(title);
        List<CharSequence> array = new ArrayList<>();
        arrayIds = new ArrayList<>();
        for (int a = 0; a < dlgs.size(); a++) {
            TLRPC.TL_dialog d = dlgs.get(a);
            int lower_id = (int) d.id;
            if (lower_id == 0){
                int high_id = (int) (d.id >> 32);
                TLRPC.EncryptedChat encryptedChat = MessagesController.getInstance().getEncryptedChat(high_id);
                if(encryptedChat != null) {
                    TLRPC.User user = MessagesController.getInstance().getUser(encryptedChat.user_id);
                    if(user != null){
                        array.add(UserObject.getUserName(user) + (encryptedChat instanceof TLRPC.TL_encryptedChat ? "" : " (" + LocaleController.getString("Cancelled", R.string.Cancelled) + ")"));
                        arrayIds.add(high_id);
                    }
                }
            }
        }

        //String[] simpleArray = new String[ array.size() ];
        //array.toArray( new CharSequence[ array.size() ]);
        builder.setItems(array.toArray( new CharSequence[array.size()]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int id = arrayIds.get(which);
                Bundle args = new Bundle();
                args.putInt("enc_id", id);
                presentFragment(new ChatActivity(args));
            }
        });

        builder.setNegativeButton(LocaleController.getString("OK", R.string.OK), null);
        showDialog(builder.create());
    }

    private void showOther(String title, ArrayList<TLRPC.TL_dialog> dlgs){
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(title);
        List<CharSequence> array = new ArrayList<>();
        arrayIds = new ArrayList<>();
        arrayType = new ArrayList<>();
        for (int a = 0; a < dlgs.size(); a++) {
            TLRPC.TL_dialog d = dlgs.get(a);
            int high_id = (int) (d.id >> 32);
            int lower_id = (int) d.id;

            if (lower_id != 0 && high_id != 1) {
                array.add(lower_id+"");
                arrayIds.add(lower_id);
                arrayType.add(lower_id < 0 ? "chat_id" : "user_id");
            } else{
                array.add(high_id+"");
                arrayIds.add(high_id);
                arrayType.add("enc_id");
            }
        }

        //String[] simpleArray = new String[ array.size() ];
        //array.toArray( new String[ array.size() ]);
        builder.setItems(array.toArray( new CharSequence[array.size()]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int id = arrayIds.get(which);
                Bundle args = new Bundle();
                args.putInt(arrayType.get(which).toString(), id);
                presentFragment(new ChatActivity(args));
            }
        });

        builder.setNegativeButton(LocaleController.getString("OK", R.string.OK), null);
        showDialog(builder.create());
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

    private class ListAdapter extends /*BaseAdapter*/BaseAdapter {
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
            return  i == totalHeaderRow || i == totalRow || i == totalUsersRow || i == totalSecretsRow || i == totalGroupsRow || i == totalSuperGroupsRow ||
                    i == totalChannelsRow || i == totalBotsRow || i == totalOtherRow || i == totalFavsRow || i == ownHeaderDividerRow || i == ownHeaderRow || i == ownGroupsRow || i == ownSuperGroupsRow ||
                    i == ownChannelsRow || i == adminHeaderDividerRow || i == adminHeaderRow || i == adminGroupsRow || i == adminSuperGroupsRow || i == adminChannelsRow;
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
            //Log.e("ChatsStats", "i " + i + " type " + type);
            if (type == 0) {
                if (view == null) {
                    view = new ShadowSectionCell(mContext);
                }
            } else if (type == 1) {
                if (view == null) {
                    view = new HeaderCell(mContext);
                }
                ((HeaderCell) view).setTextSize(16);
                int k;
                if (i == totalHeaderRow) {
                    k = totalChatsCount;
                    ((HeaderCell) view).setText(LocaleController.getString("Total", R.string.Total) + (k == 0 ? "" : ": " + k) + (progressCancelled ? " / " +LocaleController.getString("Cancelled", R.string.Cancelled) : ""));
                } else if (i == ownHeaderRow) {
                    k = ownGroupsCount + ownSuperGroupsCount + ownChannelsCount;
                    ((HeaderCell) view).setText(LocaleController.getString("Created", R.string.Created) + (k == 0 ? "" : ": " + k));
                } else if (i == adminHeaderRow) {
                    k = adminGroupsCount + adminSuperGroupsCount + adminChannelsCount;
                    ((HeaderCell) view).setText(LocaleController.getString("Administrator", R.string.Administrator) + (k == 0 ? "" : ": " + k));
                }
            } else if (type == 2) {
                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("chatsstats", Activity.MODE_PRIVATE);

                if (view == null) {
                    view = new TextSettingsCell(mContext);
                }
                TextSettingsCell textCell = (TextSettingsCell) view;

                int counter = 0;
                String title = "";
                String key = "";
                boolean div = false;
                if (i == totalRow) {
                    counter = totalChatsCount;
                    key = "totalChatsCount";
                    title = LocaleController.getString("Total", R.string.Total);
                } else if (i == totalUsersRow) {
                    counter = usersCount;
                    key = "usersCount";
                    title = LocaleController.getString("Users", R.string.Users);
                } else if (i == totalGroupsRow) {
                    counter = groupsCount;
                    key = "groupsCount";
                    title = LocaleController.getString("Groups", R.string.Groups);
                } else if (i == totalSuperGroupsRow) {
                    counter = superGroupsCount;
                    key = "superGroupsCount";
                    title = LocaleController.getString("SuperGroups", R.string.SuperGroups);
                } else if (i == totalChannelsRow) {
                    counter = channelsCount;
                    key = "channelsCount";
                    title = LocaleController.getString("Channels", R.string.Channels);
                } else if (i == totalBotsRow) {
                    counter = botsCount;
                    key = "botsCount";
                    div = true;//totalChatsCount > 0 && otherCount < 1;
                    title = LocaleController.getString("Bots", R.string.Bots);
                } else if (i == totalSecretsRow) {
                    counter = secretsCount;
                    key = "secretsCount";
                    //div = totalChatsCount > 0 && secretsCount < 1;
                    title = LocaleController.getString("SecretChat", R.string.SecretChat);
                } else if (i == totalOtherRow) {
                    counter = otherCount;
                    key = "otherCount";
                    //div = true;
                    title = LocaleController.getString("ReportChatOther", R.string.ReportChatOther);
                } else if (i == totalFavsRow) {
                    counter = favsCount;
                    key = "favsCount";
                    title = LocaleController.getString("Favorites", R.string.Favorites);
                } else if (i == ownGroupsRow) {
                    counter = ownGroupsCount;
                    key = "ownGroupsCount";
                    title = LocaleController.getString("Groups", R.string.Groups);
                } else if (i == ownSuperGroupsRow) {
                    counter = ownSuperGroupsCount;
                    key = "ownSuperGroupsCount";
                    title = LocaleController.getString("SuperGroups", R.string.SuperGroups);
                } else if (i == ownChannelsRow) {
                    counter = ownChannelsCount;
                    key = "ownChannelsCount";
                    title = LocaleController.getString("Channels", R.string.Channels);
                } else if (i == adminGroupsRow) {
                    counter = adminGroupsCount;
                    key = "adminGroupsCount";
                    title = LocaleController.getString("Groups", R.string.Groups);
                } else if (i == adminSuperGroupsRow) {
                    counter = adminSuperGroupsCount;
                    key = "adminSuperGroupsCount";
                    title = LocaleController.getString("SuperGroups", R.string.SuperGroups);
                } else if (i == adminChannelsRow) {
                    counter = adminChannelsCount;
                    key = "adminChannelsCount";
                    title = LocaleController.getString("Channels", R.string.Channels);
                }
                if(!title.isEmpty()){
                    int c = 0;
                    if(totalChatsCount > 0)c = counter - preferences.getInt(key, counter);
                    textCell.setTextAndValue(title + (c != 0 ? " ("+c+")" : ""), String.format("%d", counter), div);
                }
            } else{
                if (view == null) {
                    view = new EmptyCell(mContext);
                }
                EmptyCell emptyCell = (EmptyCell) view;
                emptyCell.setHeight(0);
            }
            return view;
        }

        @Override
        public int getItemViewType(int i) {
            if (i == totalHeaderRow || i == ownHeaderRow || i == adminHeaderRow){
                return 1;
            } else if(i == totalRow || i == totalUsersRow || i == totalSecretsRow || i == totalGroupsRow || i == totalSuperGroupsRow || i == totalChannelsRow ||
                    i == totalBotsRow || i == totalOtherRow || i == totalFavsRow || i == ownGroupsRow || i == ownSuperGroupsRow || i == ownChannelsRow ||
                    i == adminGroupsRow || i == adminSuperGroupsRow || i == adminChannelsRow){
                return 2;
            } else if(i == ownHeaderDividerRow || i == adminHeaderDividerRow){
                return 0;
            } else {
                return -1;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}

