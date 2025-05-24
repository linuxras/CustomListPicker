package com.thekstudio.customlistpicker;

import android.R;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import java.util.ArrayList;

import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.util.YailList;

@DesignerComponent(
        version = 2,
        description = "Custom List Picker extension to show a custom list picker dialog. Developed by The K Studio.",
        nonVisible = true,
        iconName = "check.png")

public class CustomListPicker extends AndroidNonvisibleComponent {

    private final Context context;
    private YailList listItems;
    private AlertDialog dialog;
    private int dialogBackgroundColor = Color.DKGRAY;
    private int dialogTextColor = Color.WHITE;
    private int dividerColor = Color.DKGRAY;
    private int searchBoxHintColor = Color.LTGRAY;
    private String dialogTitle = "Select Item";
    private Typeface dialogTypeface;
    private String selection = "";
    private int selectionIndex = 0;
    private boolean isSearchBoxVisible = true;
    private EditText searchEditText;
    private float fontSize;
    private ListView listView;
    private float itemHeight = 0.0f;
    private int dialogHeight = 0;
    private boolean cancellable = true;
    private String searchHintText = "Search";
    private String negativeButtonText = "Cancel";
    private int negativeButtonTextColor = Color.WHITE;

    public CustomListPicker(ComponentContainer container) {
        super(container.$form());
        context = container.$context();
        listItems = YailList.makeEmptyList();

    }

    @SuppressWarnings("unused")
    @SimpleFunction(description = "Shows the custom list picker dialog.")
    public void ShowListPicker() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

        // Create the dialog layout programmatically
        LinearLayout dialogLayout = new LinearLayout(context);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);

        // Set the background color of the dialog layout
        if (dialogBackgroundColor != 0) {
            dialogLayout.setBackgroundColor(dialogBackgroundColor);
        }

        // Create the dialog title TextView
        TextView dialogTitleTextView = new TextView(context);
        dialogTitleTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        dialogTitleTextView.setText(dialogTitle != null ? dialogTitle : "Select Item");
        dialogTitleTextView.setTextColor(dialogTextColor);
        dialogTitleTextView.setTextSize(20);
        dialogTitleTextView.setPadding(38, 28, 28, 28);
        dialogLayout.addView(dialogTitleTextView);

        // Set the text color of the dialog title
        dialogTitleTextView.setTextColor(dialogTextColor != 0 ? dialogTextColor : Color.WHITE);

        // Apply the font typeface to the dialog title TextView
        if (dialogTypeface != null) {
            dialogTitleTextView.setTypeface(dialogTypeface);
        }

        // Create the search EditText if the filter bar is visible
        if (isSearchBoxVisible) {
            searchEditText = new EditText(context);
            searchEditText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            searchEditText.setHint(searchHintText);


            // Set the hint color of the search EditText
            searchEditText.setHintTextColor(searchBoxHintColor != 0 ? searchBoxHintColor : Color.WHITE);
            searchEditText.setTextColor(dialogTextColor != 0 ? dialogTextColor : Color.WHITE);
            searchEditText.setTextSize(16);
            searchEditText.setPadding(38, 28, 28, 28);

            // Apply the font typeface to the search EditText
            if (dialogTypeface != null) {
                searchEditText.setTypeface(dialogTypeface);
            }

            dialogLayout.addView(searchEditText);
        }

        // Create the list view for displaying items
        listView = new ListView(context);

        // Set the background color of the ListView
        if (dialogBackgroundColor != 0) {
            listView.setBackgroundColor(dialogBackgroundColor);
        }

        listView.setDivider(new ColorDrawable(dividerColor));
        listView.setDividerHeight(1);
        listView.setPadding(10, 10, 10, 10);

        listView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        dialogLayout.addView(listView);

        // Create the custom adapter for the list view
        final ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(context, 0, listItems) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView;
                if (convertView == null) {
                    // Inflate a new view if convertView is null
                    convertView = LayoutInflater.from(context).inflate(R.layout.simple_list_item_1, parent, false);
                    textView = convertView.findViewById(R.id.text1);
                    convertView.setTag(textView);
                } else {
                    // Reuse the existing view if convertView is not null
                    textView = (TextView) convertView.getTag();
                }

                textView.setText(String.valueOf(getItem(position)));
                textView.setTextColor(dialogTextColor != 0 ? dialogTextColor : Color.WHITE); // Set default text color to white

                // Adjust the font size
                float adjustedFontSize = fontSize - 0.01f;
                textView.setTextSize(adjustedFontSize);

                // Apply the font to the TextView
                if (dialogTypeface != null) {
                    textView.setTypeface(dialogTypeface);
                }

                // Adjust the height of the TextView
                ViewGroup.LayoutParams layoutParams = textView.getLayoutParams();
                layoutParams.height = itemHeight > 0 ? (int) itemHeight * 10 : ViewGroup.LayoutParams.WRAP_CONTENT;
                textView.setLayoutParams(layoutParams);

                Object item = getItem(position);
                if (item instanceof Integer) {
                    textView.setText(String.valueOf((int) item));
                } else if (item instanceof String) {
                    textView.setText((String) item);
                }
                return convertView;
            }
        };
        listView.setAdapter(adapter);

        // Add a text watcher to filter the list view items based on search input
        if (isSearchBoxVisible) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    adapter.getFilter().filter(s);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        // Handle item click event
        listView.setOnItemClickListener((parent, view, position, id) -> {
            selection = String.valueOf(adapter.getItem(position));
            selectionIndex = position + 1;
            AfterPicking(selection, position);
            dialog.dismiss();
        });

        // Set the custom layout to the dialog
        dialogBuilder.setView(dialogLayout);

        // Create and show the dialog
        dialog = dialogBuilder.create();

        // Programmatically add negative button to the layout
        Button negativeButton = new Button(context);
        negativeButton.setText(negativeButtonText);
        negativeButton.setTextColor(negativeButtonTextColor);
        negativeButton.setBackgroundColor(Color.TRANSPARENT);
        negativeButton.setOnClickListener(v -> {
            dialog.dismiss();
            // Raise the ListPickerCancelled event
            ListPickerCancelled();
        });
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.gravity = Gravity.END | Gravity.BOTTOM; // Align to bottom right corner
        negativeButton.setLayoutParams(buttonParams);
        dialogLayout.addView(negativeButton);

        // Check if the height should be set automatically
        if (dialogHeight == 0) {
            // Set the height to match parent
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            // Set the custom height
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = dialogHeight;
            listView.setLayoutParams(params);
        }
        dialog.setCancelable(cancellable);
        dialog.show();
    }

    @SimpleEvent(description = "Event indicating that the user has picked an item from the custom list picker.")
    public void AfterPicking(Object selection, int index) {
        EventDispatcher.dispatchEvent(this, "AfterPicking", selection, index + 1);
    }

    @SimpleEvent(description = "Event indicating that the user has cancelled the list picker.")
    public void ListPickerCancelled() {
        EventDispatcher.dispatchEvent(this, "ListPickerCancelled");
    }

    @SimpleProperty(description = "Returns the selected item from the custom list picker.")
    public String Selection() {
        return selection != null ? selection : "";
    }

    @SimpleProperty(description = "Returns the index of the selected item from the custom list picker. Index starts from 1.")
    public int SelectionIndex() {
        return selectionIndex;
    }

    @SimpleFunction(description = "Dismisses the custom list picker dialog.")
    public void DismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_DKGRAY)
    @SimpleProperty(description = "Sets the background color of the custom list picker dialog.")
    public void BackgroundColor(int color) {
        dialogBackgroundColor = color;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_WHITE)
    @SimpleProperty(description = "Sets the text color of the custom list picker dialog text.")
    public void TextColor(int color) {
        dialogTextColor = color;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_DKGRAY)
    @SimpleProperty(description = "Sets the color of the divider line between list items.")
    public void DividerColor(int color) {
        dividerColor = color;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_LTGRAY)
    @SimpleProperty(description = "Sets the hint text color of the filter bar.")
    public void HintTextColor(int color) {
        searchBoxHintColor = color;
    }

    @DesignerProperty(editorType = "string", defaultValue = "Select Item")
    @SimpleProperty(description = "Sets the title of the custom list picker dialog.")
    public void Title(String title) {
        dialogTitle = title;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET)
    @SimpleProperty(description = "Sets the typeface for the custom list picker dialog text.")
    public void FontTypeface(String typefaceName) {
        AssetManager assetManager = context.getAssets();
        try {
            dialogTypeface = Typeface.createFromAsset(assetManager, typefaceName);
        } catch (RuntimeException e) {
            dialogTypeface = Typeface.DEFAULT;
        }
    }

    @SimpleProperty(description = "Sets the list items for the custom list picker dialog.")
    public void Elements(YailList items) {
        listItems.clear();
        listItems = YailList.makeList(new ArrayList<>());
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
    @SimpleProperty(description = "Sets the list items for the custom list picker dialog from a comma-separated string value.")
    public void ElementsFromString(String elements) {
        String[] items = elements.split(",");
        YailList itemList = YailList.makeList(new ArrayList<>());
        for (String item : items) {
            itemList.add(item.trim());
        }
        Elements(itemList);
    }

    @SimpleProperty(description = "Returns the list items for the custom list picker dialog.")
    public YailList Elements() {
        return listItems;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "true")
    @SimpleProperty(description = "Sets the visibility of the filter bar in the custom list picker dialog.")
    public void ShowFilterBar(boolean visible) {
        isSearchBoxVisible = visible;
        if (searchEditText != null) {
            if (visible) {
                searchEditText.setVisibility(View.VISIBLE);
            } else {
                searchEditText.setVisibility(View.GONE);
            }
        }
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "16.0")
    @SimpleProperty(description = "Sets the font size of the list items in the custom list picker dialog.")
    public void FontSize(int size) {
        fontSize = size;
        if (listView != null && listView.getAdapter() != null) {
            ArrayAdapter<?> adapter = (ArrayAdapter<?>) listView.getAdapter();
            adapter.notifyDataSetChanged();
        }
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "0.0")
    @SimpleProperty(description = "Sets the height of the list items in the custom list picker dialog. Use 0 for automatic height.")
    public void ItemHeight(int height) {
        itemHeight = height;
        if (listView != null && listView.getAdapter() != null) {
            ArrayAdapter<?> adapter = (ArrayAdapter<?>) listView.getAdapter();
            adapter.notifyDataSetChanged();
        }
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "false")
    @SimpleProperty(description = "Sets the bold style for the dialog font.")
    public void FontBold(boolean bold) {
        FontTypefaceBold(bold);
    }

    @SimpleProperty(category = PropertyCategory.APPEARANCE, userVisible = false)
    public void FontTypefaceBold(boolean isBold) {
        if (isBold) {
            dialogTypeface = Typeface.create(dialogTypeface, Typeface.BOLD);
        } else {
            dialogTypeface = Typeface.create(dialogTypeface, Typeface.NORMAL);
        }
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "0")
    @SimpleProperty(description = "Sets the height of the dialog in pixels. Set 0 for automatic.")
    public void DialogHeight(int height) {
        dialogHeight = height;
        if (listView != null) {
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = dialogHeight;
            listView.setLayoutParams(params);
        }
    }

    @DesignerProperty(editorType = "boolean", defaultValue = "True")
    @SimpleProperty(description = "Sets whether the dialog is cancellable or not")
    public void Cancellable(boolean cancellable) {
        this.cancellable = cancellable;
    }

    @DesignerProperty(editorType = "string", defaultValue = "Search")
    @SimpleProperty(description = "Sets the hint text for the search box in the custom list picker dialog.")
    public void SearchHintText(String hint) {
        searchHintText = hint;
    }

    @DesignerProperty(editorType = "string", defaultValue = "Cancel")
    @SimpleProperty(description = "Sets the text for the negative button.")
    public void NegativeButtonText(String text) {
        negativeButtonText = text;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_WHITE)
    @SimpleProperty(description = "Sets the text color of the negative button.")
    public void NegativeButtonTextColor(int textColor) {
        negativeButtonTextColor = textColor;
    }
}