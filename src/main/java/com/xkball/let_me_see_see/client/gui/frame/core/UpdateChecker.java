package com.xkball.let_me_see_see.client.gui.frame.core;

import org.jetbrains.annotations.Nullable;

public class UpdateChecker {
    
    @Nullable private Object obj = null;
    private boolean forceUpdate = false;
    
    public UpdateChecker(){}
    
    public void forceUpdate(){
        this.forceUpdate = true;
    }
    
    public boolean checkUpdate(@Nullable Object obj) {
        if(forceUpdate){
            forceUpdate = false;
            return true;
        }
        if(obj == null) return this.obj != null;
        if(obj.equals(this.obj)) return false;
        this.obj = obj;
        return true;
    }
}
