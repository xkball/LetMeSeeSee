package com.xkball.let_me_see_see.client.gui.widget;

import com.mojang.logging.LogUtils;
import com.xkball.let_me_see_see.LetMeSeeSee;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.AutoResizeWidget;
import com.xkball.let_me_see_see.common.data.ExportsDataManager;
import com.xkball.let_me_see_see.config.LMSConfig;
import com.xkball.let_me_see_see.utils.ClassSearcher;
import com.xkball.let_me_see_see.utils.VanillaUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@OnlyIn(Dist.CLIENT)
public class ClassLabel extends AutoResizeWidget {
    
    public static final Logger LOGGER = LogUtils.getLogger();
    public final String className;
    public final String classSimpleName;
    public final State state;
    
    public ClassLabel(String formattedClassName, ExportsDataManager.ExportEnv exportEnv) {
        super(Component.literal(formattedClassName));
        this.className = formattedClassName;
        this.state = State.of(exportEnv);
        this.classSimpleName = className.substring(0, className.lastIndexOf('[')).substring(className.lastIndexOf('.') + 1);
        this.setTooltip(Tooltip.create(Component.literal(formattedClassName + '\n').append(state.message)));
    }
    
    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.pose().pushPose();
        var scale = 1.2f;
        guiGraphics.pose().scale(scale, scale, 1);
        var boundary = getBoundary().inner();
        var font = Minecraft.getInstance().font;
        guiGraphics.drawString(font, classSimpleName, (boundary.x() + 4) / scale, (boundary.y() + 4) / scale, state.color, true);
        guiGraphics.pose().popPose();
    }
    
    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    
    }
    
    public Path getClassPath(){
        return Path.of(LetMeSeeSee.EXPORT_DIR_PATH, className.substring(0, className.lastIndexOf('[')).replace('.', File.separatorChar) + ".class");
    }
    
    public void openInIDEA() {
        var ideaPath = LMSConfig.IDEA_PATH;
        if (ideaPath.isEmpty()) return;
        var classPath = getClassPath().toString();
        var pb = new ProcessBuilder('\"' + ideaPath + '\"', classPath);
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        try {
            pb.start();
        } catch (IOException e) {
            LOGGER.error("Failed to open file {}", classPath, e);
        }
    }
    
    public void reExport() {
        var clazz = ClassSearcher.classMap.get(className);
        if (clazz == null) return;
        LetMeSeeSee.scanClasses(clazz);
    }
    
    public enum State {
        Normal(-1, Component.empty()),
        Old(VanillaUtils.getColor(240, 230, 0, 255), Component.translatable("let_me_see_see.gui.data_base.old").withStyle(ChatFormatting.GOLD)),
        OutOfDate(VanillaUtils.getColor(255, 0, 0, 255), Component.translatable("let_me_see_see.gui.data_base.out_of_date").withStyle(ChatFormatting.RED));
        public final int color;
        public final Component message;
        
        public static State of(ExportsDataManager.ExportEnv exportEnv) {
            if (ExportsDataManager.EXPORT_ENV.equals(exportEnv)) return Normal;
            if (ExportsDataManager.EXPORT_ENV.modListMD5().equals(exportEnv.modListMD5())) return Old;
            return OutOfDate;
        }
        
        State(int color, Component message) {
            this.color = color;
            this.message = message;
        }
    }
}
