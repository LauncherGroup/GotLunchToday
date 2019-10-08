package de.schkola.kitchenscanner.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import de.schkola.kitchenscanner.R;
import de.schkola.kitchenscanner.task.StatTask;

public class StatsListFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_stats_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setCancelable(false);
        dialog.setTitle(getString(R.string.collecting_data));
        dialog.setMessage(getString(R.string.collecting_data_lunch));
        new StatTask(dialog, (result) -> {
            ((TextView) view.findViewById(R.id.orderedA)).setText(result.getLunchA());
            ((TextView) view.findViewById(R.id.gotA)).setText(result.getDispensedA());
            ((TextView) view.findViewById(R.id.getA)).setText(result.getToDispenseA().size());
            ((TextView) view.findViewById(R.id.orderedB)).setText(result.getLunchA());
            ((TextView) view.findViewById(R.id.gotB)).setText(result.getDispensedB());
            ((TextView) view.findViewById(R.id.getB)).setText(result.getToDispenseB().size());
            ((TextView) view.findViewById(R.id.orderedS)).setText(result.getLunchS());
            ((TextView) view.findViewById(R.id.gotS)).setText(result.getDispensedS());
            ((TextView) view.findViewById(R.id.getS)).setText(result.getToDispenseS().size());
        }).execute();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_stats_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_chart) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new StatsChartFragment())
                    .commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}