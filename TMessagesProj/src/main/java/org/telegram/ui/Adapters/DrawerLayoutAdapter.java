/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package org.telegram.ui.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DrawerActionCell;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.DrawerProfileCell;
import org.telegram.ui.Cells.TextInfoCell;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.Locale;

import static android.R.attr.type;

public class DrawerLayoutAdapter extends RecyclerListView.SelectionAdapter {

    private Context mContext;
    private ArrayList<Item> items = new ArrayList<>(11);
	//plus
	private SharedPreferences themePrefs;
	private static final int versionType = 4;
    private static final int contactsRow = 6;
    private static final int themesRow = 7;
    private static final int themingRow = 8;
    private static final int settingsRow = 9;
    private static final int callsRow = 10;
    private static final int plusSettingsRow = 11;
    private static final int channelRow = 12;
    private static final int communityRow = 13;
    private static final int chatsStatsRow = 14;
    private static final int versionRow = 15;
    //private static final int faqRow = 16;
    //private static final int inviteFriendsRow = 17;
	//
    public DrawerLayoutAdapter(Context context) {
        mContext = context;
		//plus
		themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
		//
        Theme.createDialogsResources(context);
        resetItems();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void notifyDataSetChanged() {
        resetItems();
        super.notifyDataSetChanged();
    }

    @Override
    public boolean isEnabled(RecyclerView.ViewHolder holder) {
        return holder.getItemViewType() == 3;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case 0:
                view = new DrawerProfileCell(mContext);
                break;
            case 1:
            default:
                view = new EmptyCell(mContext, AndroidUtilities.dp(8));
                break;
            case 2:
                view = new DividerCell(mContext);
                break;
            case 3:
                view = new DrawerActionCell(mContext);
                break;
            case 4:
                view = new TextInfoCell(mContext);
                break;
        }
        view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new RecyclerListView.Holder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case 0:
                ((DrawerProfileCell) holder.itemView).setUser(MessagesController.getInstance().getUser(UserConfig.getClientUserId()));
                holder.itemView.setBackgroundColor(Theme.usePlusTheme ? Theme.drawerHeaderColor : Theme.getColor(Theme.key_avatar_backgroundActionBarBlue));
				//plus
				if(Theme.usePlusTheme)((DrawerProfileCell) holder.itemView).refreshAvatar(themePrefs.getInt("drawerAvatarSize", 64), themePrefs.getInt("drawerAvatarRadius", 32));
				//
                break;
			//plus
			case 1:
                updateViewColor(holder.itemView);
                break;
			case 2:
				holder.itemView.setTag("drawerListDividerColor");
                updateViewColor(holder.itemView);
                break;
			//
            case 3:
                items.get(position).bind((DrawerActionCell) holder.itemView);
				updateViewColor(holder.itemView);
                if(Theme.usePlusTheme)((DrawerActionCell) holder.itemView).setTextSize(Theme.drawerOptionSize);
                break;
            case 4:
                updateViewColor(holder.itemView);
                    try {
                        PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                        int code = pInfo.versionCode;// / 10;
                        String abi = "";
                        switch (pInfo.versionCode % 10) {
                            case 0:
                                abi = "arm";
                                break;
                            case 1:
                                abi = "arm-v7a";
                                break;
                            case 2:
                                abi = "x86";
                                break;
                            case 3:
                                abi = "arm-v7a_SDK23";
                                break;
                            case 4:
                                abi = "x86_SDK23";
                                break;
                            case 5:
                                abi = "universal";
                                break;
                        }
                        ((TextInfoCell) holder.itemView).setText(String.format(Locale.US, LocaleController.getString("TelegramForAndroid", R.string.TelegramForAndroid)+ "\nv%s (%d) %s", pInfo.versionName, code, abi));
                        ((TextInfoCell) holder.itemView).setTextColor(Theme.usePlusTheme ? themePrefs.getInt("drawerVersionColor", 0xffa3a3a3) : Theme.getColor(Theme.key_chats_menuItemText));
                        ((TextInfoCell) holder.itemView).setTextSize(themePrefs.getInt("drawerVersionSize", 13));
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                break;
        }
    }

    @Override
    public int getItemViewType(int i) {

        if (i == 0) {
            return 0;
        } else if (i == 1) {
            return 1;
        } else if (i == 5) {
            return 2;
        }
		//plus
        else if (i == items.size() - 1) {
            return Theme.plusMoveVersionToSettings ? -1 : 4;
        }
        //
        return 3;
    }

    private void resetItems() {
        items.clear();
        if (!UserConfig.isClientActivated()) {
            return;
        }
        items.add(null); // profile
        items.add(null); // padding
        items.add(new Item(2, LocaleController.getString("NewGroup", R.string.NewGroup), R.drawable.menu_newgroup));
        items.add(new Item(3, LocaleController.getString("NewSecretChat", R.string.NewSecretChat), R.drawable.menu_secret));
        items.add(new Item(4, LocaleController.getString("NewChannel", R.string.NewChannel), R.drawable.menu_broadcast));
        items.add(null); // divider
        items.add(new Item(/*6*/contactsRow, LocaleController.getString("Contacts", R.string.Contacts), R.drawable.menu_contacts));
        if (MessagesController.getInstance().callsEnabled) {
            items.add(new Item(/*10*/callsRow, LocaleController.getString("Calls", R.string.Calls), R.drawable.menu_calls));
        }
        //plus
        items.add(new Item(themesRow, LocaleController.getString("DownloadThemes", R.string.DownloadThemes), R.drawable.menu_themes));
        items.add(new Item(themingRow, LocaleController.getString("Theming", R.string.Theming), R.drawable.menu_theming));
        //
        //items.add(new Item(inviteFriendsRow, LocaleController.getString("InviteFriends", R.string.InviteFriends), R.drawable.menu_invite));
        items.add(new Item(/*8*/settingsRow, LocaleController.getString("Settings", R.string.Settings), R.drawable.menu_settings));
        //plus
        items.add(new Item(plusSettingsRow, LocaleController.getString("PlusSettings", R.string.PlusSettings), R.drawable.menu_plus));
        items.add(new Item(channelRow, LocaleController.getString("OfficialChannel", R.string.OfficialChannel), R.drawable.menu_broadcast));
        items.add(new Item(communityRow, LocaleController.getString("Community", R.string.Community), R.drawable.menu_forum));
        items.add(new Item(chatsStatsRow, LocaleController.getString("ChatsCounters", R.string.ChatsCounters), R.drawable.profile_list));
        items.add(null);
        //
        //items.add(new Item(faqRow, LocaleController.getString("TelegramFaq", R.string.TelegramFaq), R.drawable.menu_help));
    }

    public int getId(int position) {
        if (position < 0 || position >= items.size()) {
            return -1;
        }
        Item item = items.get(position);
        return item != null ? item.id : -1;
    }

    private class Item {
        public int icon;
        public String text;
        public int id;

        public Item(int id, String text, int icon) {
            this.icon = icon;
            this.id = id;
            this.text = text;
        }

        public void bind(DrawerActionCell actionCell) {
            actionCell.setTextAndIcon(text, icon);
        }
    }
	//plus
	private void updateViewColor(View v){
        if(Theme.usePlusTheme) {
            SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
            int mainColor = themePrefs.getInt("drawerListColor", 0xffffffff);
            int value = themePrefs.getInt("drawerRowGradient", 0);
            boolean b = true;//themePrefs.getBoolean("drawerRowGradientListCheck", false);
            if (value > 0 && !b) {
                GradientDrawable.Orientation go;
                switch (value) {
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

                int gradColor = themePrefs.getInt("drawerRowGradientColor", 0xffffffff);
                int[] colors = new int[]{mainColor, gradColor};
                GradientDrawable gd = new GradientDrawable(go, colors);
                v.setBackgroundDrawable(gd);
            }
        }
    }
	//
}
