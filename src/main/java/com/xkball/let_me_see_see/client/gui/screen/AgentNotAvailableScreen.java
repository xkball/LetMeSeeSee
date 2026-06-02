package com.xkball.let_me_see_see.client.gui.screen;

import com.xkball.xklib.ui.render.IComponent;
import com.xkball.xklib.ui.widget.Label;
import com.xkball.xklib.ui.widget.container.ContainerWidget;

public class AgentNotAvailableScreen extends XKLibScreen {

    @Override
    protected String getTitleKey() {
        return "let_me_see_see.gui.agent_not_available";
    }

    @Override
    protected void buildUI(ContainerWidget root) {
        var desc = new Label(IComponent.translatable("let_me_see_see.gui.agent_not_available.desc"));
        desc.inlineStyle("text-color: -1; size: 100% auto; text-align: center; margin: 10rpx;");
        root.addChild(desc);
    }
}
