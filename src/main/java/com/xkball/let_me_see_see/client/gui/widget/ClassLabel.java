package com.xkball.let_me_see_see.client.gui.widget;

import com.mojang.logging.LogUtils;
import com.xkball.let_me_see_see.LetMeSeeSee;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.AutoResizeWidget;
import com.xkball.let_me_see_see.client.gui.screen.DataBaseScreen;
import com.xkball.let_me_see_see.common.data.ExportsDataManager;
import com.xkball.let_me_see_see.config.LMSConfig;
import com.xkball.let_me_see_see.utils.ClassDecompiler;
import com.xkball.let_me_see_see.utils.ClassSearcher;
import com.xkball.let_me_see_see.utils.VanillaUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class ClassLabel extends AutoResizeWidget implements Comparable<ClassLabel> {
    
    public static final Logger LOGGER = LogUtils.getLogger();
    public final Class<?> clazz;
    public final String className;
    public final String classSimpleName;
    public State state;
    
    public ClassLabel(Class<?> clazz) {
        super(Component.literal(ClassSearcher.className(clazz)));
        this.clazz = clazz;
        this.className = ClassSearcher.className(clazz);
        this.state = State.of(className);
        this.classSimpleName = className.substring(0, className.lastIndexOf('[')).substring(className.lastIndexOf('.') + 1);
        this.setTooltip(Tooltip.create(Component.literal(className + '\n').append(state.message)));
    }
    
    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.pose().pushMatrix();
        var scale = 1.2f;
        guiGraphics.pose().scale(scale, scale);
        var boundary = getBoundary().inner();
        var font = Minecraft.getInstance().font;
        guiGraphics.drawString(font, classSimpleName, (int)((boundary.x() + 4) / scale), (int)((boundary.y() + 4) / scale), state.color, true);
        guiGraphics.pose().popMatrix();
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
        ClassDecompiler.clear(getClassPath());
        LetMeSeeSee.scanClasses(clazz);
    }
    
    public void updateState(){
        this.state = State.of(className);
    }
    
    @Override
    public int compareTo(@NotNull ClassLabel o) {
        return String.CASE_INSENSITIVE_ORDER.compare(this.className, o.className);
    }
    
    public enum State {
        Unexport(VanillaUtils.getColor(150,150,150,255),Component.translatable("let_me_see_see.gui.data_base.un_export")),
        Normal(-1, Component.empty()),
        Old(VanillaUtils.getColor(240, 230, 0, 255), Component.translatable("let_me_see_see.gui.data_base.old").withStyle(ChatFormatting.GOLD)),
        OutOfDate(VanillaUtils.getColor(255, 0, 0, 255), Component.translatable("let_me_see_see.gui.data_base.out_of_date").withStyle(ChatFormatting.RED));
        public final int color;
        public final Component message;
        
        public static State of(String className){
            return of(className,ExportsDataManager.recordedClasses.get(className));
        }
        
        public static State of(String className,@Nullable ExportsDataManager.ExportEnv exportEnv) {
            if (exportEnv == null || !ExportsDataManager.recordedClasses.containsKey(className)) return Unexport;
            if (ExportsDataManager.EXPORT_ENV.equals(exportEnv)) return Normal;
            if (ExportsDataManager.EXPORT_ENV.modListMD5().equals(exportEnv.modListMD5())) return Old;
            return OutOfDate;
        }
        
        State(int color, Component message) {
            this.color = color;
            this.message = message;
        }
    }
    
    public static class ClassLabelInDBS extends ClassLabel {
        
        private final DataBaseScreen dataBaseScreen;
        
        public ClassLabelInDBS(Class<?> clazz, DataBaseScreen dataBaseScreen) {
            super(clazz);
            this.dataBaseScreen = dataBaseScreen;
        }
        
        @Override
        public void setFocused(boolean focused) {
            super.setFocused(focused);
            if (focused) dataBaseScreen.lastFocused = this;
            if (!focused && this.equals(dataBaseScreen.lastFocused)) dataBaseScreen.lastFocused = null;
            dataBaseScreen.setNeedUpdate();
        }
    }
    
    public static class ClassLabelInDBS_ extends ClassLabelInDBS {
        
        public String depth;
        
        public ClassLabelInDBS_(Class<?> clazz, DataBaseScreen dataBaseScreen, String depth) {
            super(clazz, dataBaseScreen);
            this.depth = depth;
        }
        
        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            renderDecoration(guiGraphics, mouseX, mouseY, partialTick);
            var boundary = getBoundary().inner();
            var font = Minecraft.getInstance().font;
            guiGraphics.drawString(font, depth+classSimpleName, boundary.x(), boundary.y(), state.color, true);
        }
    }
    
    public static class ClassLabel_ extends ClassLabel {
        
        public String depth;
        
        public ClassLabel_(Class<?> clazz, int depth) {
            super(clazz);
            this.depth = " ".repeat(depth);
        }
        
        public ClassLabelInDBS_ toActualLabel(DataBaseScreen dataBaseScreen) {
            return new ClassLabelInDBS_(this.clazz, dataBaseScreen, depth );
        }
    }
}
