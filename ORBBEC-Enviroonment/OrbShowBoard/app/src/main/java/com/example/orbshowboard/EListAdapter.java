package com.example.orbshowboard;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class EListAdapter extends BaseExpandableListAdapter implements ExpandableListView.OnChildClickListener {

    private Context context;
    private ArrayList<myGroup> groups;
    private Set<String> list_selectID = new LinkedHashSet<>();
    private Set<DeviceInfo> list_selectDeviceInfos = new LinkedHashSet<>();


    public EListAdapter(Context context, ArrayList<myGroup> groups) {
        this.context = context;
        this.groups = groups;
        list_selectID.clear();
    }

    public Object getChild(int groupPosition, int childPosition) {
        return groups.get(groupPosition).getChildItem(childPosition);
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    public int getChildrenCount(int groupPosition) {
        return groups.get(groupPosition).getChildrenCount();
    }

    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    public int getGroupCount() {
        return groups.size();
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public boolean hasStableIds() {
        return true;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    /** 設定 Group 資料 */
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        myGroup group = (myGroup) getGroup(groupPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.group_layout, null);
        }

        TextView tv = (TextView) convertView.findViewById(R.id.tvGroup);
        tv.setText(group.getTitle());

        // 重新產生 CheckBox 時，將存起來的 isChecked 狀態重新設定
        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.chbGroup);
        checkBox.setChecked(group.getChecked());
        if (group.getHalfselected())
            checkBox.setBackgroundColor(Color.LTGRAY);
        else
            checkBox.setBackgroundColor(Color.WHITE);
        //checkBox.setButtonDrawable(R.drawable.btn_select);

        // 點擊 CheckBox 時，將狀態存起來
        checkBox.setOnClickListener(new Group_CheckBox_Click(groupPosition));

        return convertView;
    }

    public void UpdateSelectIDArray(boolean state,String optStr,DeviceInfo selectDeviceInfos){
        if (state){
            list_selectID.add(String.valueOf(selectDeviceInfos.ID));
            list_selectDeviceInfos.add(selectDeviceInfos);
        }else {
            list_selectID.remove(String.valueOf(selectDeviceInfos.ID));
            list_selectDeviceInfos.remove(selectDeviceInfos);
        }
    }

    public Set<String> GetSelectIDArray(){
        return list_selectID;
    }

    public Set<DeviceInfo> GetSelectDeviceInfo(){
        return list_selectDeviceInfos;
    }

    /** 勾選 Group CheckBox 時，存 Group CheckBox 的狀態，以及改變 Child CheckBox 的狀態 */
    class Group_CheckBox_Click implements OnClickListener {
        private int groupPosition;

        Group_CheckBox_Click(int groupPosition) {
            this.groupPosition = groupPosition;
        }

        public void onClick(View v) {
            groups.get(groupPosition).toggle();

            // 將 Children 的 isChecked 全面設成跟 Group 一樣
            int childrenCount = groups.get(groupPosition).getChildrenCount();
            boolean groupIsChecked = groups.get(groupPosition).getChecked();
            for (int i = 0; i < childrenCount; i++){
                groups.get(groupPosition).getChildItem(i).setChecked(groupIsChecked);
                //更新选中的设备列表
                UpdateSelectIDArray(true,groups.get(groupPosition).getChildItem(i).getFullname().trim(),
                        groups.get(groupPosition).getChildItem(i).getDeviceInfo());
            }

            // 注意，一定要通知 ExpandableListView 資料已經改變，ExpandableListView 會重新產生畫面
            notifyDataSetChanged();
        }
    }

    /** 設定 Children 資料 */
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        Child child = groups.get(groupPosition).getChildItem(childPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.child_layout, null);
        }

        TextView tv = (TextView) convertView.findViewById(R.id.tvChild);
        tv.setText(child.getFullname());

        // 重新產生 CheckBox 時，將存起來的 isChecked 狀態重新設定
        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.chbChild);
        checkBox.setChecked(child.getChecked());

        //更新选中的设备列表
        UpdateSelectIDArray(checkBox.isChecked(),tv.getText().toString().trim(),child.getDeviceInfo());

        // 點擊 CheckBox 時，將狀態存起來
        checkBox.setOnClickListener(new Child_CheckBox_Click(groupPosition, childPosition));

        return convertView;
    }

    /** 勾選 Child CheckBox 時，存 Child CheckBox 的狀態 */
    class Child_CheckBox_Click implements OnClickListener {
        private int groupPosition;
        private int childPosition;

        Child_CheckBox_Click(int groupPosition, int childPosition) {
            this.groupPosition = groupPosition;
            this.childPosition = childPosition;
        }

        public void onClick(View v) {
            handleClick(childPosition, groupPosition);
        }
    }

    public void handleClick(int childPosition, int groupPosition) {
        groups.get(groupPosition).getChildItem(childPosition).toggle();

        // 檢查 Child CheckBox 是否有全部勾選，以控制 Group CheckBox
        int childrenCount = groups.get(groupPosition).getChildrenCount();
        int childrenCheckedCount=0;
        boolean childrenAllIsChecked = true,childrenHalfIsChecked = false;
        for (int i = 0; i < childrenCount; i++) {
            if (!groups.get(groupPosition).getChildItem(i).getChecked())
                childrenAllIsChecked = false;
            else{
                childrenCheckedCount++;
            }
        }
        if (0< childrenCheckedCount && childrenCheckedCount< childrenCount)
            childrenHalfIsChecked = true;
        groups.get(groupPosition).setChecked(childrenAllIsChecked);
        groups.get(groupPosition).setHalfselected(childrenHalfIsChecked);
        // 注意，一定要通知 ExpandableListView 資料已經改變，ExpandableListView 會重新產生畫面
        notifyDataSetChanged();
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        handleClick(childPosition, groupPosition);
        return true;
    }

}
