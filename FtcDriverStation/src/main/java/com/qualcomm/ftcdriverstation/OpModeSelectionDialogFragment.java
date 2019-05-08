/*
 * Copyright (c) 2014-2016 Qualcomm Technologies Inc.
 * Copyright (c) 2018 OpenFTC Team
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of Qualcomm Technologies Inc nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * Qualcomm Technologies Inc., will periodically collect anonymous information
 * about the device this software is installed on such as the make, model, and
 * software versions, but no information that identifies you.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.qualcomm.ftcdriverstation;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta;

public class OpModeSelectionDialogFragment extends DialogFragment
{
    private OpModeSelectionDialogListener listener = null;
    private List<OpModeMeta> opModes = new LinkedList<>();
    @StringRes
    private int title = 0;

    public interface OpModeSelectionDialogListener
    {
        void onOpModeSelectionClick(OpModeMeta opModeMeta);
    }

    public void setOpModes(List<OpModeMeta> opModes)
    {
        this.opModes = new LinkedList<>(opModes);
        Collections.sort(this.opModes, new Comparator<OpModeMeta>()
        {
            public int compare(OpModeMeta lhs, OpModeMeta rhs)
            {
                int result = lhs.group.compareTo(rhs.group);
                if (result == 0)
                {
                    return lhs.name.compareTo(rhs.name);
                }
                return result;
            }
        });
    }

    public void setOnSelectionDialogListener(OpModeSelectionDialogListener listener)
    {
        this.listener = listener;
    }

    public void setTitle(@StringRes int title)
    {
        this.title = title;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        View customTitle = LayoutInflater.from(getActivity()).inflate(R.layout.opmode_dialog_title_bar, null);
        ((TextView) customTitle.findViewById(R.id.opmodeDialogTitle)).setText(title);
        Builder builder = new Builder(getActivity());
        builder.setCustomTitle(customTitle);
        ArrayAdapter<OpModeMeta> arrayAdapter = new ArrayAdapter<OpModeMeta>(getActivity(), R.layout.opmode_dialog_item, R.id.opmodeDialogItemText, opModes)
        {
            public View getView(int position, View convertView, ViewGroup parent)
            {
                View result = super.getView(position, convertView, parent);
                ImageView separator = result.findViewById(R.id.opmodeDialogItemTextSeparator);

                if (position >= opModes.size() -1 || opModes.get(position).group.equals(opModes.get(position + 1).group))
                {
                    separator.setVisibility(View.GONE);
                }
                else
                {
                    separator.setVisibility(View.VISIBLE);
                }
                return result;
            }
        };
        builder.setTitle(title);
        builder.setAdapter(arrayAdapter, new OnClickListener()
        {
            public void onClick(DialogInterface dialogInterface, int selectionIndex)
            {
                if (listener != null)
                {
                    listener.onOpModeSelectionClick(opModes.get(selectionIndex));
                }
            }
        });
        return builder.create();
    }

    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        dialog.findViewById(dialog.getContext().getResources().getIdentifier("android:id/titleDivider", null, null)).setBackground(dialog.findViewById(R.id.opmodeDialogTitleLine).getBackground());
    }
}