package com.example.orbshowboard;

import java.util.ArrayList;

public class myGroup {
    private String title;
    private ArrayList<Child> children;
    private boolean isChecked;
    private boolean isHalfselect;

    public myGroup( String title) {
        this.title = title;
        children = new ArrayList<Child>();
        this.isChecked = false;
        this.isHalfselect = false;
    }

    public void setHalfselected(boolean isHalfselect){this.isHalfselect = isHalfselect;}
    public boolean getHalfselected(){
        return this.isHalfselect;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public void toggle() {
        this.isChecked = !this.isChecked;
    }

    public boolean getChecked() {
        return this.isChecked;
    }

   /* //public String getId() {
        return id;
    }*/

    public String getTitle() {
        return title;
    }

    public void addChildrenItem(Child child) {
        children.add(child);
    }

    public int getChildrenCount() {
        return children.size();
    }

    public Child getChildItem(int index) {
        return children.get(index);
    }
}