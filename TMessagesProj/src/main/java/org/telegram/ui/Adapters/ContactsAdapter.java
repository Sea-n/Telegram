/*
 * This is the source code of Telegram for Android v. 1.3.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package org.telegram.ui.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.tgnet.TLRPC;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Cells.GraySectionCell;
import org.telegram.ui.Cells.LetterSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.HashMap;

public class ContactsAdapter extends RecyclerListView.SectionsAdapter {

    private Context mContext;
    private int onlyUsers;
    private boolean needPhonebook;
    private HashMap<Integer, TLRPC.User> ignoreUsers;
    private HashMap<Integer, ?> checkedMap;
    private boolean scrolling;
    private boolean isAdmin;

    public ContactsAdapter(Context context, int onlyUsersType, boolean arg2, HashMap<Integer, TLRPC.User> arg3, boolean arg4) {
        mContext = context;
        onlyUsers = onlyUsersType;
        needPhonebook = arg2;
        ignoreUsers = arg3;
        isAdmin = arg4;
    }

    public void setCheckedMap(HashMap<Integer, ?> map) {
        checkedMap = map;
    }

    public void setIsScrolling(boolean value) {
        scrolling = value;
    }

    public Object getItem(int section, int position) {
        HashMap<String, ArrayList<TLRPC.TL_contact>> usersSectionsDict = onlyUsers == 2 ? ContactsController.getInstance().usersMutualSectionsDict : ContactsController.getInstance().usersSectionsDict;
        ArrayList<String> sortedUsersSectionsArray = onlyUsers == 2 ? ContactsController.getInstance().sortedUsersMutualSectionsArray : ContactsController.getInstance().sortedUsersSectionsArray;

        if (onlyUsers != 0 && !isAdmin) {
            if (section < sortedUsersSectionsArray.size()) {
                ArrayList<TLRPC.TL_contact> arr = usersSectionsDict.get(sortedUsersSectionsArray.get(section));
                if (position < arr.size()) {
                    return MessagesController.getInstance().getUser(arr.get(position).user_id);
                }
            }
            return null;
        } else {
            if (section == 0) {
                return null;
            } else {
                if (section - 1 < sortedUsersSectionsArray.size()) {
                    ArrayList<TLRPC.TL_contact> arr = usersSectionsDict.get(sortedUsersSectionsArray.get(section - 1));
                    if (position < arr.size()) {
                        return MessagesController.getInstance().getUser(arr.get(position).user_id);
                    }
                    return null;
                }
            }
        }
        if (needPhonebook) {
            return ContactsController.getInstance().phoneBookContacts.get(position);
        }
        return null;
    }

    @Override
    public boolean isEnabled(int section, int row) {
        HashMap<String, ArrayList<TLRPC.TL_contact>> usersSectionsDict = onlyUsers == 2 ? ContactsController.getInstance().usersMutualSectionsDict : ContactsController.getInstance().usersSectionsDict;
        ArrayList<String> sortedUsersSectionsArray = onlyUsers == 2 ? ContactsController.getInstance().sortedUsersMutualSectionsArray : ContactsController.getInstance().sortedUsersSectionsArray;

        if (onlyUsers != 0 && !isAdmin) {
            ArrayList<TLRPC.TL_contact> arr = usersSectionsDict.get(sortedUsersSectionsArray.get(section));
            return row < arr.size();
        } else {
            if (section == 0) {
                if (needPhonebook || isAdmin) {
                    if (row == 1) {
                        return false;
                    }
                } else {
                    if (row == 3) {
                        return false;
                    }
                }
                return true;
            } else if (section - 1 < sortedUsersSectionsArray.size()) {
                ArrayList<TLRPC.TL_contact> arr = usersSectionsDict.get(sortedUsersSectionsArray.get(section - 1));
                return row < arr.size();
            }
        }
        return true;
    }

    @Override
    public int getSectionCount() {
        ArrayList<String> sortedUsersSectionsArray = onlyUsers == 2 ? ContactsController.getInstance().sortedUsersMutualSectionsArray : ContactsController.getInstance().sortedUsersSectionsArray;
        int count = sortedUsersSectionsArray.size();
        if (onlyUsers == 0) {
            count++;
        }
        if (isAdmin) {
            count++;
        }
        if (needPhonebook) {
            count++;
        }
        return count;
    }

    @Override
    public int getCountForSection(int section) {
        HashMap<String, ArrayList<TLRPC.TL_contact>> usersSectionsDict = onlyUsers == 2 ? ContactsController.getInstance().usersMutualSectionsDict : ContactsController.getInstance().usersSectionsDict;
        ArrayList<String> sortedUsersSectionsArray = onlyUsers == 2 ? ContactsController.getInstance().sortedUsersMutualSectionsArray : ContactsController.getInstance().sortedUsersSectionsArray;

        if (onlyUsers != 0 && !isAdmin) {
            if (section < sortedUsersSectionsArray.size()) {
                ArrayList<TLRPC.TL_contact> arr = usersSectionsDict.get(sortedUsersSectionsArray.get(section));
                int count = arr.size();
                if (section != (sortedUsersSectionsArray.size() - 1) || needPhonebook) {
                    count++;
                }
                return count;
            }
        } else {
            if (section == 0) {
                if (needPhonebook || isAdmin) {
                    return 2;
                } else {
                    return 4;
                }
            } else if (section - 1 < sortedUsersSectionsArray.size()) {
                ArrayList<TLRPC.TL_contact> arr = usersSectionsDict.get(sortedUsersSectionsArray.get(section - 1));
                int count = arr.size();
                if (section - 1 != (sortedUsersSectionsArray.size() - 1) || needPhonebook) {
                    count++;
                }
                return count;
            }
        }
        if (needPhonebook) {
            return ContactsController.getInstance().phoneBookContacts.size();
        }
        return 0;
    }

    @Override
    public View getSectionHeaderView(int section, View view) {
        HashMap<String, ArrayList<TLRPC.TL_contact>> usersSectionsDict = onlyUsers == 2 ? ContactsController.getInstance().usersMutualSectionsDict : ContactsController.getInstance().usersSectionsDict;
        ArrayList<String> sortedUsersSectionsArray = onlyUsers == 2 ? ContactsController.getInstance().sortedUsersMutualSectionsArray : ContactsController.getInstance().sortedUsersSectionsArray;

        if (view == null) {
            view = new LetterSectionCell(mContext);
        }
        LetterSectionCell cell = (LetterSectionCell) view;
        if (onlyUsers != 0 && !isAdmin) {
            if (section < sortedUsersSectionsArray.size()) {
                cell.setLetter(sortedUsersSectionsArray.get(section));
            } else {
                cell.setLetter("");
            }
        } else {
            if (section == 0) {
                cell.setLetter("");
            } else if (section - 1 < sortedUsersSectionsArray.size()) {
                cell.setLetter(sortedUsersSectionsArray.get(section - 1));
            } else {
                cell.setLetter("");
            }
        }
		if(Theme.usePlusTheme)cell.setLetterColor(AndroidUtilities.getIntDef("contactsNameColor", 0xff808080)); //Plus
        return view;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		//plus
		SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        int cColorGrey = themePrefs.getInt("contactsNameColor", 0xff737373);
        int cColorBlack = themePrefs.getInt("contactsNameColor", 0xff000000);
        int iconsColor = themePrefs.getInt("contactsIconsColor", 0xff737373);
		//
        View view;
        switch (viewType) {
            case 0:
                view = new UserCell(mContext, 58, 1, false);
                if(Theme.usePlusTheme) {
                    view.setTag("Contacts");
                    updateViewColor(view);
                }
                break;
            case 1:
                view = new TextCell(mContext);
                if(Theme.usePlusTheme) {
                    ((TextCell) view).setTextColor(cColorBlack);
                    ((TextCell) view).setTextSize(themePrefs.getInt("contactsNameSize", 16));
                    ((TextCell) view).setIconColor(iconsColor);
                    updateViewColor(view);
                }
                break;
            case 2:
                view = new GraySectionCell(mContext);
                ((GraySectionCell) view).setText(LocaleController.getString("Contacts", R.string.Contacts).toUpperCase());
                if(Theme.usePlusTheme) {
                    ((GraySectionCell) view).setBackgroundColor(themePrefs.getInt("contactsRowColor", 0xffffffff));
                    ((GraySectionCell) view).setTextColor(cColorGrey);
                    updateViewColor(view);
                }
                break;
            case 3:
            default:
                view = new DividerCell(mContext);
                view.setPadding(AndroidUtilities.dp(LocaleController.isRTL ? 28 : 72), 0, AndroidUtilities.dp(LocaleController.isRTL ? 72 : 28), 0);
                if(Theme.usePlusTheme) {
                    view.setTag("contactsRowColor"); //Plus
                    updateViewColor(view);
                }
                break;
        }
        if(Theme.usePlusTheme)updateListBG(parent);
        return new RecyclerListView.Holder(view);
    }
	//plus
	private void updateListBG(ViewGroup vg){
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        int mainColor = themePrefs.getInt("contactsRowColor", 0xffffffff);
        int value = themePrefs.getInt("contactsRowGradient", 0);
        //boolean b = true;//themePrefs.getBoolean("contactsRowGradientListCheck", false);
        if(value > 0) {
            GradientDrawable.Orientation go;
            switch(value) {
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

            int gradColor = themePrefs.getInt("contactsRowGradientColor", 0xffffffff);
            int[] colors = new int[]{mainColor, gradColor};
            GradientDrawable gd = new GradientDrawable(go, colors);
            vg.setBackgroundDrawable(gd);
        }else{
            vg.setBackgroundColor(mainColor);
        }
    }

    private void updateViewColor(View v){
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        int mainColor = themePrefs.getInt("contactsRowColor", 0xffffffff);
        int value = themePrefs.getInt("contactsRowGradient", 0);
        boolean b = true;//themePrefs.getBoolean("contactsRowGradientListCheck", false);
        if(value > 0 && !b) {
            GradientDrawable.Orientation go;
            switch(value) {
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

            int gradColor = themePrefs.getInt("contactsRowGradientColor", 0xffffffff);
            int[] colors = new int[]{mainColor, gradColor};
            GradientDrawable gd = new GradientDrawable(go, colors);
            v.setBackgroundDrawable(gd);
        } else if(b){
            v.setBackgroundColor(0x00000000);
        }
        if(value > 0)v.setTag("Contacts00");
    }
	//
    @Override
    public void onBindViewHolder(int section, int position, RecyclerView.ViewHolder holder) {
        switch (holder.getItemViewType()) {
            case 0:
                UserCell userCell = (UserCell) holder.itemView;
                HashMap<String, ArrayList<TLRPC.TL_contact>> usersSectionsDict = onlyUsers == 2 ? ContactsController.getInstance().usersMutualSectionsDict : ContactsController.getInstance().usersSectionsDict;
                ArrayList<String> sortedUsersSectionsArray = onlyUsers == 2 ? ContactsController.getInstance().sortedUsersMutualSectionsArray : ContactsController.getInstance().sortedUsersSectionsArray;

                ArrayList<TLRPC.TL_contact> arr = usersSectionsDict.get(sortedUsersSectionsArray.get(section - (onlyUsers != 0 && !isAdmin ? 0 : 1)));
                TLRPC.User user = MessagesController.getInstance().getUser(arr.get(position).user_id);
                userCell.setData(user, null, null, 0);
                if (checkedMap != null) {
                    userCell.setChecked(checkedMap.containsKey(user.id), !scrolling);
                }
                if (ignoreUsers != null) {
                    if (ignoreUsers.containsKey(user.id)) {
                        userCell.setAlpha(0.5f);
                    } else {
                        userCell.setAlpha(1.0f);
                    }
                }
                break;
            case 1:
                TextCell textCell = (TextCell) holder.itemView;
                if (section == 0) {
                    if (needPhonebook) {
                        textCell.setTextAndIcon(LocaleController.getString("InviteFriends", R.string.InviteFriends), R.drawable.menu_invite);
                    } else if (isAdmin) {
                        textCell.setTextAndIcon(LocaleController.getString("InviteToGroupByLink", R.string.InviteToGroupByLink), R.drawable.menu_invite);
                    } else {
                        if (position == 0) {
                            textCell.setTextAndIcon(LocaleController.getString("NewGroup", R.string.NewGroup), R.drawable.menu_newgroup);
                        } else if (position == 1) {
                            textCell.setTextAndIcon(LocaleController.getString("NewSecretChat", R.string.NewSecretChat), R.drawable.menu_secret);
                        } else if (position == 2) {
                            textCell.setTextAndIcon(LocaleController.getString("NewChannel", R.string.NewChannel), R.drawable.menu_broadcast);
                        }
                    }
                } else {
                    ContactsController.Contact contact = ContactsController.getInstance().phoneBookContacts.get(position);
                    if (contact.first_name != null && contact.last_name != null) {
                        textCell.setText(contact.first_name + " " + contact.last_name);
                    } else if (contact.first_name != null && contact.last_name == null) {
                        textCell.setText(contact.first_name);
                    } else {
                        textCell.setText(contact.last_name);
                    }
                }
                break;
        }
    }

    @Override
    public int getItemViewType(int section, int position) {
        HashMap<String, ArrayList<TLRPC.TL_contact>> usersSectionsDict = onlyUsers == 2 ? ContactsController.getInstance().usersMutualSectionsDict : ContactsController.getInstance().usersSectionsDict;
        ArrayList<String> sortedUsersSectionsArray = onlyUsers == 2 ? ContactsController.getInstance().sortedUsersMutualSectionsArray : ContactsController.getInstance().sortedUsersSectionsArray;
        if (onlyUsers != 0 && !isAdmin) {
            ArrayList<TLRPC.TL_contact> arr = usersSectionsDict.get(sortedUsersSectionsArray.get(section));
            return position < arr.size() ? 0 : 3;
        } else {
            if (section == 0) {
                if ((needPhonebook || isAdmin) && position == 1 || position == 3) {
                    return 2;
                }
            } else if (section - 1 < sortedUsersSectionsArray.size()) {
                ArrayList<TLRPC.TL_contact> arr = usersSectionsDict.get(sortedUsersSectionsArray.get(section - 1));
                return position < arr.size() ? 0 : 3;
            }
        }
        return 1;
    }

    @Override
    public String getLetter(int position) {
        ArrayList<String> sortedUsersSectionsArray = onlyUsers == 2 ? ContactsController.getInstance().sortedUsersMutualSectionsArray : ContactsController.getInstance().sortedUsersSectionsArray;
        int section = getSectionForPosition(position);
        if (section == -1) {
            section = sortedUsersSectionsArray.size() - 1;
        }
        if (section > 0 && section <= sortedUsersSectionsArray.size()) {
            return sortedUsersSectionsArray.get(section - 1);
        }
        return null;
    }

    @Override
    public int getPositionForScrollProgress(float progress) {
        return (int) (getItemCount() * progress);
    }
}
