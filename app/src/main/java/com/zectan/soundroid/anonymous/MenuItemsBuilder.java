package com.zectan.soundroid.anonymous;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.MenuRes;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.PopupMenu;

import com.zectan.soundroid.R;

public class MenuItemsBuilder {

    public static <T> void createMenu(View v, @MenuRes int menu_id, T object, MenuItemCallback<T> callback) {
        Context context = v.getContext();
        PopupMenu popup = new PopupMenu(context, v);
        popup.getMenuInflater().inflate(menu_id, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> callback.onMenuItemClicked(object, item));
        inflateIcons(popup, context);
        popup.show();
    }

    @SuppressLint("RestrictedApi")
    private static void inflateIcons(PopupMenu popup, Context context) {
        if (popup.getMenu() instanceof MenuBuilder) {
            MenuBuilder menuBuilder = (MenuBuilder) popup.getMenu();
            menuBuilder.setOptionalIconsVisible(true);
            for (int i = 0; i < menuBuilder.getVisibleItems().size(); i++) {
                MenuItemImpl item = menuBuilder.getVisibleItems().get(i);
                int iconMarginPX = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 10f, context.getResources().getDisplayMetrics()
                );

                if (item.getIcon() != null) {
                    Drawable icon = item.getIcon();
                    TypedValue typedValue = new TypedValue();
                    context.getTheme().resolveAttribute(R.attr.colorOnBackground, typedValue, true);
                    icon.setTint(typedValue.data);
                    item.setIcon(new InsetDrawable(icon, iconMarginPX, 0, iconMarginPX, 0));
                }
            }
        }
    }

    public interface MenuItemCallback<T> {
        boolean onMenuItemClicked(T object, MenuItem item);
    }
}
