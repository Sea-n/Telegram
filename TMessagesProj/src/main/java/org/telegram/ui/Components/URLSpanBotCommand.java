/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package org.telegram.ui.Components;

import android.content.SharedPreferences;
import android.text.TextPaint;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.ui.ActionBar.Theme;


public class URLSpanBotCommand extends URLSpanNoUnderline {

    public static boolean enabled = true;
    public boolean isOut;

    public URLSpanBotCommand(String url, boolean isOutOwner) {
        super(url);
        isOut = isOutOwner;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        if (isOut) {
            ds.setColor(Theme.usePlusTheme ? enabled ? Theme.chatRLinkColor : Theme.chatRTextColor : Theme.getColor(enabled ? Theme.key_chat_messageLinkOut : Theme.key_chat_messageTextOut));
        } else {
            ds.setColor(Theme.usePlusTheme ? enabled ? Theme.chatLLinkColor : Theme.chatLTextColor : Theme.getColor(enabled ? Theme.key_chat_messageLinkIn : Theme.key_chat_messageTextIn));
        }
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        int def = themePrefs.getInt("chatCommandColor", themePrefs.getInt("themeColor", AndroidUtilities.defColor));
        boolean check = themePrefs.getBoolean("chatCommandColorCheck", false);
        if(enabled && check)ds.setColor(def);
        ds.setUnderlineText(false);
    }
}
