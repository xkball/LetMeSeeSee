package com.xkball.let_me_see_see.client.gui.screen;

import com.xkball.xklib.ui.render.IComponent;
import com.xkball.xklib.ui.widget.container.ContainerWidget;
import com.xkball.xklibmc.ui.XKLibBaseScreen;
import net.minecraft.network.chat.Component;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class XKLibScreen extends XKLibBaseScreen {

    protected final Queue<Runnable> renderTasks = new ConcurrentLinkedQueue<>();
    protected final ContainerWidget root;
    private boolean uiBuilt = false;

    public XKLibScreen() {
        super(Component.empty());
        this.root = new ContainerWidget();
        this.root.inlineStyle("size: 100% 100%; flex-direction: column;");
    }

    protected abstract String getTitleKey();

    protected abstract void buildUI();

    @Override
    protected void init() {
        super.init();
        if (!uiBuilt) {
            uiBuilt = true;
            this.addScreenLayer(XKLibBaseScreen.frame(IComponent.translatable(getTitleKey()), this.root));
            buildUI();
        }
    }

    public void submitRenderTask(Runnable runnable) {
        renderTasks.add(runnable);
    }
}
