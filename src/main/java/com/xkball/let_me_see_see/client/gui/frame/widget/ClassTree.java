package com.xkball.let_me_see_see.client.gui.frame.widget;

import com.xkball.let_me_see_see.client.gui.frame.core.HorizontalAlign;
import com.xkball.let_me_see_see.client.gui.frame.core.IPanel;
import com.xkball.let_me_see_see.client.gui.frame.core.PanelConfig;
import com.xkball.let_me_see_see.client.gui.frame.core.VerticalAlign;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.VerticalPanel;
import com.xkball.let_me_see_see.client.gui.screen.DataBaseScreen;
import com.xkball.let_me_see_see.client.gui.widget.ClassLabel;
import com.xkball.let_me_see_see.utils.JavaWorkaround;
import com.xkball.let_me_see_see.utils.ThrowableSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class ClassTree {
    
    private static final MethodHandle IS_TOP_CLASS = ThrowableSupplier.getOrThrow(() -> JavaWorkaround.getTrustedLookup().findVirtual(Class.class,"isTopLevelClass", MethodType.methodType(boolean.class)));
    
    public final PackageLabel root;
    
    public ClassTree(Collection<Class<?>> classes) {
        this.root = new PackageLabel("",0);
        root.open = true;
        for(var clazz : classes) {
            if(!isNormalClass(clazz)) continue;
            var path = clazz.getName().split("\\.");
            var node = root;
            for(var i = 0; i < path.length - 1; i++) {
                PackageLabel finalNode = node;
                node = node.pkg.computeIfAbsent(path[i], str -> new PackageLabel(str, finalNode.depth+1));
            }
            node.classes.put(path[path.length-1],new ClassLabel.ClassLabel_(clazz,node.depth+1));
        }
    }
    
    public static boolean isNormalClass(Class<?> clazz) {
        return ThrowableSupplier.getOrElse(() -> (boolean)IS_TOP_CLASS.invoke(clazz),false)
                && !clazz.isSynthetic()
                && !clazz.isArray();
    }
    
    public void addToPanel(VerticalPanel widget,DataBaseScreen dataBaseScreen) {
        addToPanel(widget, dataBaseScreen,root);
    }
    
    public void addToPanel(VerticalPanel widget, DataBaseScreen dataBaseScreen, PackageLabel packageLabel){
        var config = PanelConfig.of(1,1)
                .fixWidth(widget.getBoundary().inner().width()-6)
                .align(HorizontalAlign.LEFT,VerticalAlign.CENTER)
                .fixHeight(10);
        widget.addWidget(config.apply(packageLabel.createLabel(dataBaseScreen)));
        if(packageLabel.open){
            for(var pk : packageLabel.pkg.values()){
                this.addToPanel(widget, dataBaseScreen,pk);
            }
            for(var clzLabel : packageLabel.classes.values()){
                widget.addWidget(config.apply(clzLabel.toActualLabel(dataBaseScreen)));
            }
        }
    }
    
    public static class PackageLabel implements Comparable<PackageLabel> {
        
        public final int depth;
        public final String packageName;
        public final Map<String, PackageLabel> pkg = new TreeMap<>();
        public final Map<String, ClassLabel.ClassLabel_> classes = new TreeMap<>();
        
        public boolean open = false;
        
        public PackageLabel(String packageName, int depth) {
            this.packageName = packageName;
            this.depth = depth;
        }
        
        public int elementCount(){
            if(!open) return 0;
            var i = this.pkg.size() + this.classes.size();
            for(var p : this.pkg.values()) {
                i += p.elementCount();
            }
            return i;
        }
        
        public Label createLabel(DataBaseScreen dataBaseScreen) {
            return new Label(Component.literal(" ".repeat(depth) + (open ? "> ":"^ ") + packageName),1,-1,true){
                @Override
                protected boolean isValidClickButton(int button) {
                    return button == 0;
                }
                
                @Override
                public void onClick(double mouseX, double mouseY, int button) {
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    open = !open;
                    IPanel.GLOBAL_UPDATE_MARKER.setNeedUpdate();
                    dataBaseScreen.searchBarUpdateChecker.forceUpdate();
                }
            };
        }
        
        @Override
        public int compareTo(@NotNull ClassTree.PackageLabel o) {
            return String.CASE_INSENSITIVE_ORDER.compare(packageName, o.packageName);
        }
        
    }
}
