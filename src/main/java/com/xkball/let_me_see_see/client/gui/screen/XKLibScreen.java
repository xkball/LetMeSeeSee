package com.xkball.let_me_see_see.client.gui.screen;

import com.xkball.xklib.ui.render.IComponent;
import com.xkball.xklib.ui.widget.container.ContainerWidget;
import com.xkball.xklibmc.ui.XKLibBaseScreen;
import net.minecraft.network.chat.Component;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class XKLibScreen extends XKLibBaseScreen {

    protected final Queue<Runnable> renderTasks = new ConcurrentLinkedQueue<>();
    private boolean uiBuilt = false;

    public XKLibScreen() {
        super(Component.empty());
    }

    protected abstract String getTitleKey();

    @Override
    protected void init() {
        super.init();
        if (!uiBuilt) {
            uiBuilt = true;
            setupFrame();
        }
    }

    protected void setupFrame() {
        var root = new ContainerWidget();
        root.inlineStyle("size: 100% 100%; flex-direction: column;");
        this.addScreenLayer(XKLibBaseScreen.frame(IComponent.translatable(getTitleKey()), root));
        buildUI(root);
    }

    protected abstract void buildUI(ContainerWidget root);

    public void submitRenderTask(Runnable runnable) {
        renderTasks.add(runnable);
    }
}
