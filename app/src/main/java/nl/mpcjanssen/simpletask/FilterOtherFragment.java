package nl.mpcjanssen.simpletask;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioButton;


public class FilterOtherFragment extends Fragment {

    final static String TAG = FilterOtherFragment.class.getSimpleName();
    private CheckBox cbHideCompleted;
    private CheckBox cbHideFuture;
    private CheckBox cbHideLists;
    private CheckBox cbHideTags;
    private CheckBox cbHideCreateDate;
    private CheckBox cbHideHidden;
    private CheckBox cbCreateAsThreshold;
    private RadioGroup rgTaskGroup2;
    private RadioButton rbTaskGroup2;
//    private RadioButton rbTaskGroup2Due;
//    private RadioButton rbTaskGroup2None;
//    private RadioButton rbTaskGroup2Prio;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() this:" + this);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.d(TAG, "onDestroy() this:" + this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState() this:" + this);
        outState.putBoolean(Query.INTENT_HIDE_COMPLETED_FILTER, getHideCompleted());
        outState.putBoolean(Query.INTENT_HIDE_FUTURE_FILTER, getHideFuture());
        outState.putBoolean(Query.INTENT_HIDE_LISTS_FILTER, getHideLists());
        outState.putBoolean(Query.INTENT_HIDE_TAGS_FILTER, getHideTags());
        outState.putBoolean(Query.INTENT_HIDE_CREATE_DATE_FILTER, getHideCreateDate());
        outState.putBoolean(Query.INTENT_CREATE_AS_THRESHOLD, getCreateAsThreshold());
        outState.putString(Query.INTENT_TASKGROUP2_KEY, getTaskGroup2By());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() this:" + this + " savedInstance:" + savedInstanceState);

        Bundle arguments = getArguments();

        Log.d(TAG, "Fragment bundle:" + this + " arguments:" + arguments);
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.other_filter,
                container, false);

        cbHideCompleted = layout.findViewById(R.id.cb_show_completed);
        cbHideFuture = layout.findViewById(R.id.cb_show_future);
        cbHideLists = layout.findViewById(R.id.cb_show_lists);
        cbHideTags = layout.findViewById(R.id.cb_show_tags);
        cbHideCreateDate = layout.findViewById(R.id.cb_show_create_date);
        cbHideHidden = layout.findViewById(R.id.cb_show_hidden);
        cbCreateAsThreshold = layout.findViewById(R.id.cb_create_is_threshold);
        rgTaskGroup2 = layout.findViewById(R.id.rg_task_g2);
        rbTaskGroup2 = layout.findViewById(rgTaskGroup2.getCheckedRadioButtonId());
//        rbTaskGroup2Due = layout.findViewById(R.id.rg_task_g2_due);
//        rbTaskGroup2None = layout.findViewById(R.id.rg_task_g2_none);
//        rbTaskGroup2Prio = layout.findViewById(R.id.rg_task_g2_prio);

        rgTaskGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup arg0, int arg1) {
                rbTaskGroup2 = arg0.findViewById(arg0.getCheckedRadioButtonId());
            }
        });

        if (savedInstanceState != null) {
            cbHideCompleted.setChecked(!savedInstanceState.getBoolean(Query.INTENT_HIDE_COMPLETED_FILTER, false));
            cbHideFuture.setChecked(!savedInstanceState.getBoolean(Query.INTENT_HIDE_FUTURE_FILTER, false));
            cbHideLists.setChecked(!savedInstanceState.getBoolean(Query.INTENT_HIDE_LISTS_FILTER, false));
            cbHideTags.setChecked(!savedInstanceState.getBoolean(Query.INTENT_HIDE_TAGS_FILTER, false));
            cbHideCreateDate.setChecked(!savedInstanceState.getBoolean(Query.INTENT_HIDE_CREATE_DATE_FILTER, false));
            cbHideHidden.setChecked(!savedInstanceState.getBoolean(Query.INTENT_HIDE_HIDDEN_FILTER, true));
            cbCreateAsThreshold.setChecked(savedInstanceState.getBoolean(Query.INTENT_CREATE_AS_THRESHOLD, false));
//            rbTaskGroup2Due.setChecked(savedInstanceState.getString(Query.INTENT_TASKGROUP2_KEY) == "限期");
//            rbTaskGroup2None.setChecked(savedInstanceState.getString(Query.INTENT_TASKGROUP2_KEY) == "优先级");
//            rbTaskGroup2Prio.setChecked(savedInstanceState.getString(Query.INTENT_TASKGROUP2_KEY) == "无");
        } else {
            cbHideCompleted.setChecked(!arguments.getBoolean(Query.INTENT_HIDE_COMPLETED_FILTER, false));
            cbHideFuture.setChecked(!arguments.getBoolean(Query.INTENT_HIDE_FUTURE_FILTER, false));
            cbHideLists.setChecked(!arguments.getBoolean(Query.INTENT_HIDE_LISTS_FILTER, false));
            cbHideTags.setChecked(!arguments.getBoolean(Query.INTENT_HIDE_TAGS_FILTER, false));
            cbHideCreateDate.setChecked(!arguments.getBoolean(Query.INTENT_HIDE_CREATE_DATE_FILTER, false));
            cbHideHidden.setChecked(!arguments.getBoolean(Query.INTENT_HIDE_HIDDEN_FILTER, true));
            cbCreateAsThreshold.setChecked(arguments.getBoolean(Query.INTENT_CREATE_AS_THRESHOLD, true));
//            rbTaskGroup2Due.setChecked(arguments.getString(Query.INTENT_TASKGROUP2_KEY, "限期") == "限期");
//            rbTaskGroup2None.setChecked(arguments.getString(Query.INTENT_TASKGROUP2_KEY, "限期") == "优先级");
//            rbTaskGroup2Prio.setChecked(arguments.getString(Query.INTENT_TASKGROUP2_KEY, "限期") == "无");
        }

        return layout;
    }

    public boolean getHideCompleted() {
        Bundle arguments = getArguments();
        if (cbHideCompleted == null) {
            return arguments.getBoolean(Query.INTENT_HIDE_COMPLETED_FILTER, false);
        } else {
            return !cbHideCompleted.isChecked();
        }
    }

    public boolean getHideFuture() {
        Bundle arguments = getArguments();
        if (cbHideCompleted == null) {
            return arguments.getBoolean(Query.INTENT_HIDE_FUTURE_FILTER, false);
        } else {
            return !cbHideFuture.isChecked();
        }
    }

    public boolean getHideHidden() {
        Bundle arguments = getArguments();
        if (cbHideHidden == null) {
            return arguments.getBoolean(Query.INTENT_HIDE_HIDDEN_FILTER, true);
        } else {
            return !cbHideHidden.isChecked();
        }
    }

    public boolean getHideLists() {
        Bundle arguments = getArguments();
        if (cbHideCompleted == null) {
            return arguments.getBoolean(Query.INTENT_HIDE_LISTS_FILTER, false);
        } else {
            return !cbHideLists.isChecked();
        }
    }
    public boolean getHideTags() {
        Bundle arguments = getArguments();
        if (cbHideCompleted == null) {
            return arguments.getBoolean(Query.INTENT_HIDE_TAGS_FILTER, false);
        } else {
            return !cbHideTags.isChecked();
        }
    }
    public boolean getHideCreateDate() {
        Bundle arguments = getArguments();
        if (cbHideCreateDate == null) {
            return arguments.getBoolean(Query.INTENT_HIDE_CREATE_DATE_FILTER, false);
        } else {
            return !cbHideCreateDate.isChecked();
        }
    }

    public boolean getCreateAsThreshold() {
        Bundle arguments = getArguments();
        if (cbCreateAsThreshold == null) {
            return arguments.getBoolean(Query.INTENT_CREATE_AS_THRESHOLD, false);
        } else {
            return cbCreateAsThreshold.isChecked();
        }
    }

    public String getTaskGroup2By() {
        Bundle arguments = getArguments();
        if (rbTaskGroup2 == null) {
            return arguments.getString(Query.INTENT_TASKGROUP2_KEY, "限期");
        } else {
            return rbTaskGroup2.getText().toString();
        }
    }


}
