package com.thekstudio.customlistpicker;

import android.content.DialogInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;

@DesignerComponent(
        version = 2,
        description = "Choice Picker extension to show single and multi choice picker dialogs. Developed by The K Studio.",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "aiwebres/check.png")

@SimpleObject(external = true)
//Libraries
@UsesLibraries(libraries = "")
//Permissions
@UsesPermissions(permissionNames = "")

public class ChoicePicker extends AndroidNonvisibleComponent {

    private final List<Object> listItems;
    private AlertDialog dialog;
    private boolean cancellable = true;
    private String dialogTitle = "Select Item";
    private String selection = "";
    private int selectionIndex = 0;
    private List<Object> selections = new ArrayList<>();
    private List<Integer> indices = new ArrayList<>();
    private String positiveButtonText = "OK";
    private String negativeButtonText = "CANCEL";
    private boolean showSelectAllButton = true;
    private String selectAllButtonText = "Select All";
    private String selectNoneButtonText = "Select None";
    private Object preSelectedChoice;
    private final List<Object> preSelectedChoices = new ArrayList<>();

    public ChoicePicker(ComponentContainer container){
        super(container.$form());
        listItems = new ArrayList<>();
    }

    @SuppressWarnings("unused")
    @SimpleFunction(description = "Shows a single choice list picker dialog")
    public void ShowSingleChoicePicker() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(form);
        dialogBuilder.setTitle(dialogTitle);

        // Find the index of the pre-selected choice
        int preSelectedIndex = -1;
        if (preSelectedChoice != null) {
            preSelectedIndex = listItems.indexOf(preSelectedChoice);
        }

        final int[] checkedItem = { preSelectedIndex };

        dialogBuilder.setSingleChoiceItems((CharSequence[]) listItems.toArray(new Object[0]), checkedItem[0],
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)  {
                        // Store the selected item index
                        checkedItem[0] = which;
                    }
                });

        // Set the positive button text
        if (positiveButtonText != null && !positiveButtonText.isEmpty()) {
            dialogBuilder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (checkedItem[0] >= 0 && checkedItem[0] < listItems.size()) {
                        selection = listItems.get(checkedItem[0]).toString();
                        selectionIndex = checkedItem[0] + 1;
                        AfterSinglePicking(selection, selectionIndex);
                    } else {
                        SinglePickerCancelled();
                    }
                    dialog.dismiss();
                }
            });
        }

        // Set the negative button text
        if (negativeButtonText != null && !negativeButtonText.isEmpty()) {
            dialogBuilder.setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)  {
                    dialog.dismiss();
                    SinglePickerCancelled();
                }
            });
        }

        dialog = dialogBuilder.create();
        dialog.setCancelable(cancellable);
        dialog.show();
    }

    // Single choice picker properties

    @SimpleEvent(description = "Event triggered after picking a single item")
    public void AfterSinglePicking(Object selection, int index) {
        EventDispatcher.dispatchEvent(this, "AfterSinglePicking", selection, index);
    }

    @SimpleEvent(description = "Event triggered when the single-picker is cancelled or dismissed without selecting an item")
    public void SinglePickerCancelled() {
        EventDispatcher.dispatchEvent(this, "SinglePickerCancelled");
    }

    @SimpleProperty(description = "Returns the selected item from the custom list picker.")
    public String Selection() {
        return selection != null ? selection : "";
    }

    @SimpleProperty(description = "Returns the index of the selected item from the custom list picker. Index starts from 1.")
    public int SelectionIndex() {
        return selectionIndex;
    }

    @SimpleProperty(description = "Sets the choice to be pre-selected in the single-choice picker dialog.")
    public void PreSelectedChoice(Object choice) {
        preSelectedChoice = choice;
    }

    //@SuppressWarnings("unused")
    @SimpleFunction(description = "Shows a multiple choice list picker dialog.")
    public void ShowMultiChoicePicker() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(form);
        dialogBuilder.setTitle(dialogTitle);

        final boolean[] checkedItems = new boolean[listItems.size()];

        // Set the initial checked state based on the pre-selected choices
        for (int i = 0; i < listItems.size(); i++) {
            if (preSelectedChoices.contains(listItems.get(i))) {
                checkedItems[i] = true;
            }
        }

        // Create a CharSequence array from the listItems
        CharSequence[] itemsArray = new CharSequence[listItems.size()];
        for (int i = 0; i < listItems.size(); i++) {
            itemsArray[i] = listItems.get(i).toString();
        }

        // Set the custom adapter with multi-choice listener
        dialogBuilder.setMultiChoiceItems(itemsArray, checkedItems,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedItems[which] = isChecked;
                    }
                });

        // Set the positive button text
        if (positiveButtonText != null && !positiveButtonText.isEmpty()) {
            dialogBuilder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Get the selected items and indices
                    selections = new ArrayList<>();
                    indices = new ArrayList<>();
                    for (int i = 0; i < checkedItems.length; i++) {
                        if (checkedItems[i]) {
                            selections.add(listItems.get(i));
                            indices.add(i + 1); // Add 1 to the index value
                        }
                    }

                    // Dispatch the event with selections and indices
                    AfterMultiPicking(selections, indices);
                }
            });
        }

        // Set the negative button text
        if (negativeButtonText != null && !negativeButtonText.isEmpty()) {
            dialogBuilder.setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    // Raise the MultiPickerCancelled event
                    MultiPickerCancelled();
                }
            });
        }

        final AlertDialog alertDialog = dialogBuilder.create();
        if (showSelectAllButton) {

            // Add neutral button manually
            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, selectAllButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Toggle all items
                    boolean allChecked = true;
                    for (boolean checkedItem : checkedItems) {
                        if (!checkedItem) {
                            allChecked = false;
                            break;
                        }
                    }
                    Arrays.fill(checkedItems, !allChecked);
                    ((AlertDialog) dialog).getListView().clearChoices();
                    ((AlertDialog) dialog).getListView().invalidateViews();

                    // Update neutral button text
                    updateNeutralButtonText(alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL), checkedItems);
                }
            });

            // Set the onShowListener for the dialog
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) { // Declare dialog as final
                    Button allButton = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                    allButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Toggle all items
                            boolean allChecked = true;
                            for (boolean checkedItem : checkedItems) {
                                if (!checkedItem) {
                                    allChecked = false;
                                    break;
                                }
                            }
                            Arrays.fill(checkedItems, !allChecked);
                            ((AlertDialog) dialog).getListView().clearChoices();
                            ((AlertDialog) dialog).getListView().invalidateViews();

                            // Update neutral button text
                            updateNeutralButtonText(alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL), checkedItems);
                        }
                    });
                }
            });

        }

        // Show the dialog
        alertDialog.setCancelable(cancellable);
        alertDialog.show();
    }

    // Method to update neutral button text based on the current selection state
    private void updateNeutralButtonText(Button neutralButton, boolean[] checkedItems) {
        if (areAllItemsChecked(checkedItems)) {
            neutralButton.setText(selectNoneButtonText);
        } else {
            neutralButton.setText(selectAllButtonText);
        }
    }

    // Method to check if all items are checked
    private boolean areAllItemsChecked(boolean[] checkedItems) {
        for (boolean checkedItem : checkedItems) {
            if (!checkedItem) {
                return false;
            }
        }
        return true;
    }


    @SimpleEvent(description = "Event triggered after picking multiple items from multi-picker dialog.")
    public void AfterMultiPicking(List<Object> selections, List<Integer> indices) {
        EventDispatcher.dispatchEvent(this, "AfterMultiPicking", selections, indices);
    }

    @SimpleEvent(description = "Event triggered when the multi-picker is cancelled or dismissed without selecting items")
    public void MultiPickerCancelled() {
        EventDispatcher.dispatchEvent(this, "MultiPickerCancelled");
    }

    @SimpleProperty(description = "Returns the selected items from multi list picker dialog.")
    public List<Object> Selections() {
        return selections;
    }

    @SimpleProperty(description = "Returns the indices of the selected items from multi list picker dialog.")
    public List<Integer> SelectionIndices() {
        return indices;
    }

    @SimpleProperty(description = "Sets the choices to be pre-selected in the multi-choice picker dialog.")
    public void PreSelectedChoices(List<Object> choices) {
        preSelectedChoices.clear();
        preSelectedChoices.addAll(choices);
    }

    @DesignerProperty(editorType = "boolean", defaultValue = "True")
    @SimpleProperty(description = "Set whether to show or hide the Select All button for Multi Choice Picker Dialog.", category = PropertyCategory.APPEARANCE)
    public void ShowSelectAllButton(boolean show) {
        showSelectAllButton = show;
    }

    @DesignerProperty(editorType = "string", defaultValue = "Select All")
    @SimpleProperty(description = "Set the text for the Select All button for Multi Choice Picker Dialog.", category = PropertyCategory.APPEARANCE)
    public void SelectAllButtonText(String text) {
        selectAllButtonText = text;
    }

    @DesignerProperty(editorType = "string", defaultValue = "Select None")
    @SimpleProperty(description = "Set the text for the Deselect All button for Multi Choice Picker Dialog.", category = PropertyCategory.APPEARANCE)
    public void SelectNoneButtonText(String text) {
        selectNoneButtonText = text;
    }

    @DesignerProperty(editorType = "string", defaultValue = "OK")
    @SimpleProperty(description = "Sets the positive button text for dialog.", category = PropertyCategory.APPEARANCE)
    public void PositiveButtonText(String text) {
        positiveButtonText = text;
    }

    @DesignerProperty(editorType = "string", defaultValue = "Cancel")
    @SimpleProperty(description = "Sets the negative button text for dialog.", category = PropertyCategory.APPEARANCE)
    public void NegativeButtonText(String text) {
        negativeButtonText = text;
    }

    @DesignerProperty(editorType = "boolean", defaultValue = "True")
    @SimpleProperty(description = "Sets whether the dialog is cancellable or not", category = PropertyCategory.BEHAVIOR)
    public void Cancellable(boolean cancellable) {
        this.cancellable = cancellable;
    }

    @SimpleFunction(description = "Dismisses the custom list picker dialog.")
    public void DismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @DesignerProperty(editorType = "string", defaultValue = "Select Item")
    @SimpleProperty(description = "Sets the title of the custom list picker dialog.", category = PropertyCategory.APPEARANCE)
    public void Title(String title) {
        dialogTitle = title;
    }

    @SimpleProperty(description = "Sets the list items for the custom list picker dialog.")
    public void Elements(List<Object> items) {
        listItems.clear();
        listItems.addAll(items);
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
    @SimpleProperty(description = "Sets the list items for the custom list picker dialog from a comma-separated string value.", category = PropertyCategory.APPEARANCE)
    public void ElementsFromString(String elements) {
        String[] items = elements.split(",");
        List<Object> itemList = new ArrayList<>();
        for (String item : items) {
            itemList.add(item.trim());
        }
        Elements(itemList);
    }

    @SimpleProperty(description = "Returns the list items for the custom list picker dialog.")
    public List<Object> Elements() {
        return listItems;
    }
}
