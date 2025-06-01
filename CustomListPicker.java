package com.thekstudio.customlistpicker;

import android.R;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.util.Log;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.io.InputStream;
import java.io.IOException;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TableRow;
import androidx.appcompat.app.AlertDialog;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.FileScope;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.HorizontalArrangement;
import com.google.appinventor.components.runtime.ButtonBase;
import com.google.appinventor.components.runtime.Label;
import com.google.appinventor.components.runtime.util.ElementsUtil;
import com.google.appinventor.components.runtime.util.YailList;
import com.google.appinventor.components.runtime.util.AsyncCallbackPair;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.FileUtil;
import com.google.appinventor.components.runtime.util.ViewUtil;


@DesignerComponent(
        version = 3,
        description = "Custom List Picker extension to show a custom list picker dialog. Developed by The K Studio.",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "aiwebres/icon.png")

@SimpleObject(external = true)
//Libraries
@UsesLibraries(libraries = "")
//Permissions
@UsesPermissions(permissionNames = "")

public class CustomListPicker extends AndroidNonvisibleComponent {

    private final Activity context;
    //private YailList listItems;
    private List<Object> listItems;
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
    private boolean hasIcons = false;
    private String searchHintText = "Search";
    private String negativeButtonText = "Cancel";
    private int negativeButtonTextColor = Color.WHITE;
    private String filePrefix;
    private static final String LOG_TAG = "CustomListPicker";

    //From ColinTreeListView
    private HashMap<String, CachedImage> iconMap = new HashMap<String, CachedImage>();
    private boolean asyncImageLoad = false;
    private boolean cacheImage = false;
    //END

    private final Form form;

    public CustomListPicker(ComponentContainer container) {
        super(container.$form());
        context = container.$context();
        form = container.$form();
        listItems = new ArrayList<>();
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

        final CustomAdapter adapter2 = new CustomAdapter(context, listItems);

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
        if(hasIcons) {
            listView.setAdapter(adapter2);
        } else {
            listView.setAdapter(adapter);
        }

        // Add a text watcher to filter the list view items based on search input
        if (isSearchBoxVisible) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(hasIcons)
                        adapter2.getFilter().filter(s);
                    else
                        adapter.getFilter().filter(s);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        // Handle item click event
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(hasIcons) {
                    selection = String.valueOf(adapter2.getItem(position));
                } else {
                    selection = String.valueOf(adapter.getItem(position));
                }
                selectionIndex = position + 1;
                AfterPicking(selection, position);
                dialog.dismiss();
            } 
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
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                // Raise the ListPickerCancelled event
                ListPickerCancelled();
            }
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

    @SimpleFunction
    public void ClearAllCache() {
        for (HashMap.Entry<String, CachedImage> entry : iconMap.entrySet()) {
            entry.getValue().releaseMemory();
            iconMap.remove(entry.getKey());
        }
        System.gc();
    }

    @SimpleFunction
    public void ClearCache(String path) {
        CachedImage ci = iconMap.get(path);
        if (ci != null) {
            ci.releaseMemory();
            iconMap.remove(path);
        }
        System.gc();
    }

    @SimpleFunction(description = "Dismisses the custom list picker dialog.")
    public void DismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @SimpleEvent(description = "Event indicating that the user has picked an item from the custom list picker.")
    public void AfterPicking(Object selection, int index) {
        EventDispatcher.dispatchEvent(this, "AfterPicking", selection, index + 1);
    }

    @SimpleEvent(description = "Event indicating that the user has cancelled the list picker.")
    public void ListPickerCancelled() {
        EventDispatcher.dispatchEvent(this, "ListPickerCancelled");
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public boolean AsyncImageLoad() {
        return asyncImageLoad;
    }
    @SimpleProperty
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    public void AsyncImageLoad(boolean async) {
        asyncImageLoad = async;
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR)
    public boolean CacheImage() {
        return cacheImage;
    }
    @SimpleProperty
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    public void CacheImage(boolean cache) {
        cacheImage = cache;
    }

    @SimpleProperty(description = "Returns the selected item from the custom list picker.")
    public String Selection() {
        return selection != null ? selection : "";
    }

    @SimpleProperty(description = "Returns the index of the selected item from the custom list picker. Index starts from 1.")
    public int SelectionIndex() {
        return selectionIndex;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_DKGRAY)
    @SimpleProperty(description = "Sets the background color of the custom list picker dialog.",
        category = PropertyCategory.APPEARANCE)
    public void BackgroundColor(int color) {
        dialogBackgroundColor = color;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_WHITE)
    @SimpleProperty(description = "Sets the text color of the custom list picker dialog text.",
        category = PropertyCategory.APPEARANCE)
    public void TextColor(int color) {
        dialogTextColor = color;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_DKGRAY)
    @SimpleProperty(description = "Sets the color of the divider line between list items.", category = PropertyCategory.APPEARANCE)
    public void DividerColor(int color) {
        dividerColor = color;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_LTGRAY)
    @SimpleProperty(description = "Sets the hint text color of the filter bar.", category = PropertyCategory.APPEARANCE)
    public void HintTextColor(int color) {
        searchBoxHintColor = color;
    }

    @DesignerProperty(editorType = "string", defaultValue = "Select Item")
    @SimpleProperty(description = "Sets the title of the custom list picker dialog.", category = PropertyCategory.APPEARANCE)
    public void Title(String title) {
        dialogTitle = title;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET)
    @SimpleProperty(description = "Sets the typeface for the custom list picker dialog text.", category = PropertyCategory.APPEARANCE)
    public void FontTypeface(String typefaceName) {
        AssetManager assetManager = context.getAssets();
        try {
            dialogTypeface = Typeface.createFromAsset(assetManager, typefaceName);
        } catch (RuntimeException e) {
            dialogTypeface = Typeface.DEFAULT;
        }
    }

    @SimpleProperty(description = "Sets the list items for the custom list picker dialog.")
    public void Elements(List<Object> items) {
        listItems = new ArrayList<Object>(items);
        //Test for list in list and set hasIcon flag
        if(listItems.size() > 0) {
            Object first = listItems.get(0);
            if (first instanceof String) {
                hasIcons = false;
            } else if (first instanceof List) {
                hasIcons = true;
            } else {
                hasIcons = false; //default to false
            }
        } else {
            hasIcons = false;
        }
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
    @SimpleProperty(description = "Sets the list items for the custom list picker dialog from a comma-separated string value.", category = PropertyCategory.APPEARANCE)
    public void ElementsFromString(String elements) {
        String[] items = elements.split(",");
        List<Object> itemList = new ArrayList<Object>();
        String first = items[0];
        if(first.contains("||")) {
            for (String item : items) {
                String[] subItems = item.split("\\|\\|", 2);
                List<String> sublist = new ArrayList<String>();
                sublist.add(subItems[0]);
                sublist.add(subItems[1]);
                itemList.add(sublist);
            }
        } else {
            for (String item : items) {
                itemList.add(item.trim());
            }
        }
        Elements(itemList);
    }

    @SimpleProperty(description = "Returns the list items for the custom list picker dialog.")
    public List<Object> Elements() {
        return listItems;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty(description = "Sets the visibility of the filter bar in the custom list picker dialog.", category = PropertyCategory.BEHAVIOR)
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
    @SimpleProperty(description = "Sets the font size of the list items in the custom list picker dialog.", category = PropertyCategory.APPEARANCE)
    public void FontSize(int size) {
        fontSize = size;
        if (listView != null && listView.getAdapter() != null) {
            ArrayAdapter<?> adapter = (ArrayAdapter<?>) listView.getAdapter();
            adapter.notifyDataSetChanged();
        }
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "0.0")
    @SimpleProperty(description = "Sets the height of the list items in the custom list picker dialog. Use 0 for automatic height.", category = PropertyCategory.APPEARANCE)
    public void ItemHeight(int height) {
        itemHeight = height;
        if (listView != null && listView.getAdapter() != null) {
            ArrayAdapter<?> adapter = (ArrayAdapter<?>) listView.getAdapter();
            adapter.notifyDataSetChanged();
        }
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty(description = "Sets the bold style for the dialog font.", category = PropertyCategory.APPEARANCE)
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
    @SimpleProperty(description = "Sets the height of the dialog in pixels. Set 0 for automatic.", category = PropertyCategory.APPEARANCE)
    public void DialogHeight(int height) {
        dialogHeight = height;
        if (listView != null) {
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = dialogHeight;
            listView.setLayoutParams(params);
        }
    }

    @DesignerProperty(editorType = "boolean", defaultValue = "True")
    @SimpleProperty(description = "Sets whether the dialog is cancellable or not", category = PropertyCategory.BEHAVIOR)
    public void Cancellable(boolean cancellable) {
        this.cancellable = cancellable;
    }

    @DesignerProperty(editorType = "string", defaultValue = "Search")
    @SimpleProperty(description = "Sets the hint text for the search box in the custom list picker dialog.", category = PropertyCategory.APPEARANCE)
    public void SearchHintText(String hint) {
        searchHintText = hint;
    }

    @DesignerProperty(editorType = "string", defaultValue = "Cancel")
    @SimpleProperty(description = "Sets the text for the negative button.", category = PropertyCategory.APPEARANCE)
    public void NegativeButtonText(String text) {
        negativeButtonText = text;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR, defaultValue = Component.DEFAULT_VALUE_COLOR_WHITE)
    @SimpleProperty(description = "Sets the text color of the negative button.", category = PropertyCategory.APPEARANCE)
    public void NegativeButtonTextColor(int textColor) {
        negativeButtonTextColor = textColor;
    }

    private class CustomAdapter extends BaseAdapter implements Filterable{

        private final Activity context;
        private List<Object> items;
        private List<Object> filterHolder;
        private ValueFilter valueFilter;
    
        public CustomAdapter(Activity context, List<Object> list) {
            this.context = context;
            this.items = list;
            this.filterHolder = list;
        }
    
        @Override
        public int getCount() {
            return items.size();
        }
    
        @Override
        public Object getItem(int position) {
            List<String> realItems = (List<String>)items.get(position);
            return realItems.get(2);
        }
    
        @Override
        public long getItemId(int position) {
            return position;
        }

        // ViewHolder class to reuse views efficiently
        private class ViewHolder {
            ImageView imageView;
            TextView textView;
        }

        private void setImageThroughCache(ImageView view, String path) {
            CachedImage ci = iconMap.get(path); // Cache all the time
            if (ci != null) {
                ci.setImage(view);
            } else {
                iconMap.put(path, new CachedImage(path, view));
            }
        }
    
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            List<String> rowData = (List<String>)items.get(position);
            TableLayout tableLayout;
            ViewHolder holder;
            if(convertView == null) {
                tableLayout = new TableLayout(context);
                TableRow tableRow = new TableRow(context);
                TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    itemHeight > 0 ? (int) itemHeight * 10 : ViewGroup.LayoutParams.WRAP_CONTENT
                );
                tableRow.setLayoutParams(rowParams);
                tableRow.setPadding(5, 5, 5, 5);
                tableRow.setGravity(Gravity.CENTER_VERTICAL);
    
                ImageView imageView = new ImageView(context);
                imageView.setLayoutParams(new TableRow.LayoutParams(32, 32));
    
                TextView textView = new TextView(context);
                textView.setPadding(40, 0, 0, 0);
                textView.setTextColor(dialogTextColor != 0 ? dialogTextColor : Color.WHITE); // Set default text color to white
                
                // Adjust the font size
                float adjustedFontSize = fontSize - 0.01f;
                textView.setTextSize(adjustedFontSize);
                
                // Apply the font to the TextView
                 if (dialogTypeface != null) {
                     textView.setTypeface(dialogTypeface);
                 }
                TableRow.LayoutParams textParams = new TableRow.LayoutParams(
                     ViewGroup.LayoutParams.WRAP_CONTENT,
                     ViewGroup.LayoutParams.WRAP_CONTENT
                );
                textParams.gravity = Gravity.CENTER_VERTICAL;
                textView.setLayoutParams(textParams);

                tableRow.addView(imageView);
                tableRow.addView(textView);
    
                tableLayout.addView(tableRow);
                holder = new ViewHolder();
                holder.imageView = imageView;
                holder.textView = textView;
                tableLayout.setTag(holder);
            } else {
                tableLayout = (TableLayout)convertView;
                holder = (ViewHolder)tableLayout.getTag();
            }
            String path = String.valueOf(rowData.get(1));
            if (cacheImage) { 
                setImageThroughCache(holder.imageView, path);
            } else { 
                if (asyncImageLoad) {
                    MediaUtil.getBitmapDrawableAsync(form, path, new AsyncCallbackPair<BitmapDrawable>() {
                        @Override
                        public void onFailure(String message) {}

                        @Override
                        public void onSuccess(final BitmapDrawable result) {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    holder.imageView.setImageDrawable(new BitmapDrawable(form.getResources(), result.getBitmap()));
                                    holder.imageView.setAdjustViewBounds(true);
                                    TableRow.LayoutParams imgParams = new TableRow.LayoutParams(
                                            result.getIntrinsicWidth(),
                                            result.getIntrinsicHeight());
                                    imgParams.gravity = Gravity.CENTER_VERTICAL;
                                    holder.imageView.setLayoutParams(imgParams);
                                }
                            });
                        }
                    });
                } else { // No cache and no async, use default.
                    try {
                        BitmapDrawable bd = MediaUtil.getBitmapDrawable(form, path);
                        holder.imageView.setImageDrawable(bd);
                        if(bd != null) {
                            Log.d(LOG_TAG, "Drawable found loading into imageView directly");
                            holder.imageView.setAdjustViewBounds(true);
                            TableRow.LayoutParams imgParams = new TableRow.LayoutParams(
                                    bd.getIntrinsicWidth(),
                                    bd.getIntrinsicHeight());
                            imgParams.gravity = Gravity.CENTER_VERTICAL;
                            holder.imageView.setLayoutParams(imgParams);
                        }
                    }
                    catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            holder.textView.setText(rowData.get(2));
    
            return tableLayout;
        }

        @Override
        public Filter getFilter() {
            if(valueFilter == null) {
        
                valueFilter = new ValueFilter();
            }
        
            return valueFilter;
        }

        private class ValueFilter extends Filter {

            //Invoked in a worker thread to filter the data according to the constraint.
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if(constraint != null && constraint.length()>0){
                    String filterSeq = constraint.toString().toLowerCase();
                    List<Object> filterList = new ArrayList<Object>();
                    for(Object item : filterHolder) {
                        String txt = String.valueOf(((List<Object>)item).get(2));
                        if(txt.toLowerCase().contains(filterSeq)) {
                            filterList.add(item);
                        }
                    }
                    results.count = filterList.size();
                    results.values = filterList;
                }else{
                    results.count = filterHolder.size();
                    results.values = filterHolder;
                }
                return results;
            }
        
        
            //Invoked in the UI thread to publish the filtering results in the user interface.
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                items = (List<Object>) results.values;
                notifyDataSetChanged();
            }
        }
    }

    //Borrowed from ColinTreeListView
    private class CachedImage {

        public final String path;
        private BitmapDrawable bd = null;
        private ArrayList<ImageView> callback = new ArrayList<ImageView>();

        public CachedImage(String path, ImageView view) {
            this.path = path;
            addCallback(view);
            if (asyncImageLoad) {
                MediaUtil.getBitmapDrawableAsync(form, path, new AsyncCallbackPair<BitmapDrawable>() {
                    @Override
                    public void onFailure(String message) {
                    }

                    @Override
                    public void onSuccess(BitmapDrawable result) {
                        gotBitmapDrawable(result);
                    }
                });
            } else { // Use built-in Synchronizer to make this operation sync
                try {
                    gotBitmapDrawable(MediaUtil.getBitmapDrawable(form, path));
                } catch(IOException ok) {
                    ok.printStackTrace();
                }
            }
        }

        public void setImage(ImageView view) {
            if (bd != null) {
                view.setImageDrawable(new BitmapDrawable(form.getResources(), bd.getBitmap()));
                view.setAdjustViewBounds(true);
                TableRow.LayoutParams imgParams = new TableRow.LayoutParams(bd.getIntrinsicWidth(), bd.getIntrinsicHeight());
                imgParams.gravity = Gravity.CENTER_VERTICAL;
                view.setLayoutParams(imgParams);
            } else {
                addCallback(view);
            }
        }

        public void addCallback(ImageView view) {
            callback.add(view);
        }

        private void gotBitmapDrawable(final BitmapDrawable bd) {
            if (this.bd != null) {
                return;
            }
            this.bd = bd;
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (ImageView view : callback) {
                        setImage(view);
                    }
                }
            });
        }

        public void releaseMemory() {
            this.bd = null;
            this.callback = null;
            // After bd is set to null, all request are not going to triger startLoading()
        }
    }
}
