package be.khlim.student.cmee.cmee;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;

/**
 * Created by Elsen on 28/12/2014.
 */
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

public class NumberPickerPreference extends DialogPreference {

    NumberPicker picker;
    Integer initialValue;

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.number_pref);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        this.picker = (NumberPicker)view.findViewById(R.id.pref_num_picker);

        SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
        int maxPoints = Integer.parseInt(preferences.getString("radius", "10"));

        if (maxPoints < 100){
            maxPoints = maxPoints/2;
        }else
        {
            maxPoints = 75;
        }

        picker.setMaxValue(maxPoints);
        picker.setMinValue(1);
        if ( this.initialValue != null ) picker.setValue(initialValue);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        if ( which == DialogInterface.BUTTON_POSITIVE ) {
            this.initialValue = picker.getValue();
            persistString(String.valueOf(picker.getValue()));
            callChangeListener( picker.getValue() );
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue,
                                     Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
            initialValue = Integer.parseInt(this.getPersistedString("1"));
        } else {
            // Set default state from the XML attribute
            initialValue = (Integer) defaultValue;
            persistString(initialValue.toString());
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 1);
    }
}