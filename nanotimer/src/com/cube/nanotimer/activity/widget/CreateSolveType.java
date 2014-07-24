package com.cube.nanotimer.activity.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.cube.nanotimer.Options;
import com.cube.nanotimer.R;
import com.cube.nanotimer.util.Utils;
import com.cube.nanotimer.vo.SolveType;
import com.cube.nanotimer.vo.SolveTypeStep;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;

import java.util.ArrayList;
import java.util.List;

public class CreateSolveType extends DialogFragment {

  private static final String ARG_CUBETYPEID = "cubetypeId";

  private int cubeTypeId;
  private Dialog dialog;

  private EditText tfName;
  private List<String> liSteps;

  public static CreateSolveType newInstance(int cubeTypeId) {
    CreateSolveType frag = new CreateSolveType();
    Bundle args = new Bundle();
    args.putInt(ARG_CUBETYPEID, cubeTypeId);
    frag.setArguments(args);
    return frag;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    cubeTypeId = getArguments().getInt(ARG_CUBETYPEID);
    dialog = getCreationDialog();
    return dialog;
  }

  private Dialog getCreationDialog() {
    LayoutInflater factory = LayoutInflater.from(getActivity());
    View view = factory.inflate(R.layout.create_solvetype_dialog, null);
    final AlertDialog d;

    d = new AlertDialog.Builder(getActivity())
        .setView(view)
        .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            onConfirm();
          }
        })
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
          }
        })
        .create();

    tfName = (EditText) view.findViewById(R.id.tfName);
    final LinearLayout detailsLayout = (LinearLayout) view.findViewById(R.id.detailsLayout);
    detailsLayout.setVisibility(View.GONE);

    // Steps ListView
    liSteps = new ArrayList<String>();
    final StepsListAdapter stepsAdapter = new StepsListAdapter(getActivity(), R.layout.simple_list_item, liSteps);
    final DragSortListView lvSteps = (DragSortListView) view.findViewById(R.id.lvSteps);
    lvSteps.setAdapter(stepsAdapter);

    lvSteps.setDropListener(new DropListener() {
      @Override
      public void drop(int from, int to) {
        if (from != to) {
          String step = stepsAdapter.getItem(from);
          liSteps.remove(step);
          liSteps.add(to, step);
          stepsAdapter.notifyDataSetChanged();
        }
      }
    });

    DragSortController controller = new DragSortController(lvSteps);
    controller.setDragHandleId(R.id.imgMove);
    lvSteps.setFloatViewManager(controller);
    lvSteps.setOnTouchListener(controller);

    // Steps header
    final EditText tfStepName = (EditText) view.findViewById(R.id.tfStepName);
    Button buAddStep = (Button) view.findViewById(R.id.buAddStep);
    buAddStep.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        String stepName = tfStepName.getText().toString().trim();
        if (!"".equals(stepName)) {
          if (liSteps.size() < Options.INSTANCE.getMaxStepsCount()) {
            if (!liSteps.contains(stepName)) {
              liSteps.add(stepName);
              tfStepName.setText("");
              stepsAdapter.notifyDataSetChanged();
            } else {
              Utils.showInfoMessage(R.string.step_already_exists);
            }
          } else {
            Utils.showInfoMessage(R.string.max_steps_count_reached);
          }
        }
      }
    });

    View buExpand = view.findViewById(R.id.buExpand);
    buExpand.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        if (detailsLayout.getVisibility() == View.GONE) {
          detailsLayout.setVisibility(View.VISIBLE);
//          Utils.expandView(detailsLayout);
        } else {
          detailsLayout.setVisibility(View.GONE);
//          Utils.collapseView(detailsLayout);
        }
      }
    });

    return d;
  }

  private void onConfirm() {
    String solveTypeName = tfName.getText().toString().trim();
    if ("".equals(solveTypeName)) {
      // TODO : this does not prevent the dialog from closing. see how to handle that
      // TODO : also check and do the same thing in other similar fragments (like FieldEditDialog)
      return;
    }

    SolveType solveType = new SolveType(solveTypeName, cubeTypeId);
    List<SolveTypeStep> steps = new ArrayList<SolveTypeStep>();
    for (String stepName : liSteps) {
      SolveTypeStep step = new SolveTypeStep();
      step.setName(stepName);
      steps.add(step);
    }
    solveType.setSteps(steps.toArray(new SolveTypeStep[0]));

    // TODO : call creator

    dialog.dismiss();
  }

  @Override
  public void show(FragmentManager manager, String tag) {
    if (manager.findFragmentByTag(tag) == null) {
      super.show(manager, tag);
    }
  }

  private class StepsListAdapter extends ArrayAdapter<String> {
    public StepsListAdapter(Context context, int id, List<String> list) {
      super(context, id, list);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
      View view = convertView;
      if (view == null) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.steps_list_item, null);
      }

      if (position >= 0 && position < liSteps.size()) {
        String item = liSteps.get(position);
        if (item != null) {
          TextView tvName = (TextView) view.findViewById(R.id.tvStepName);
          tvName.setText(item);
        }
      }
      return view;
    }
  }

}
