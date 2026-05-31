package com.xkball.let_me_see_see.client.gui.xkwidget;

import com.xkball.let_me_see_see.client.gui.screen.DataBaseScreen;
import com.xkball.let_me_see_see.common.data.ExportsDataManager;
import com.xkball.let_me_see_see.utils.ClassSearcher;
import com.xkball.let_me_see_see.utils.VanillaUtils;
import com.xkball.xklib.ui.widget.Button;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class ClassLabelWidget extends Button implements Comparable<ClassLabelWidget> {

    public final Class<?> clazz;
    public final String className;
    public final String classSimpleName;
    public final int depth;
    public State state;
    protected final DataBaseScreen screen;

    public ClassLabelWidget(Class<?> clazz, DataBaseScreen screen, int depth) {
        super("", () -> {});
        this.clazz = clazz;
        this.screen = screen;
        this.depth = depth;
        this.className = ClassSearcher.className(clazz);
        this.state = State.of(className);
        this.classSimpleName = className.substring(0, className.lastIndexOf('['))
                .substring(className.lastIndexOf('.') + 1);

        var text = "  ".repeat(depth) + classSimpleName;
        this.setText(text);
        this.setTextColor(state.color != -1 ? state.color : -1);
        this.setCallback(() -> {
            screen.lastFocused = this;
            com.xkball.xklib.ui.system.GuiSystem.INSTANCE.get().submitTreeUpdate(screen::refreshPreview);
        });
    }

    public ClassLabelWidget(Class<?> clazz, DataBaseScreen screen) {
        this(clazz, screen, 0);
    }

    public void updateState() {
        this.state = State.of(className);
        this.setTextColor(state.color != -1 ? state.color : -1);
    }

    @Override
    public int compareTo(ClassLabelWidget o) {
        return String.CASE_INSENSITIVE_ORDER.compare(this.className, o.className);
    }

    public enum State {
        Unexport(VanillaUtils.getColor(150, 150, 150, 255),
                Component.translatable("let_me_see_see.gui.data_base.un_export")),
        Normal(-1, Component.empty()),
        Old(VanillaUtils.getColor(240, 230, 0, 255),
                Component.translatable("let_me_see_see.gui.data_base.old").withStyle(ChatFormatting.GOLD)),
        OutOfDate(VanillaUtils.getColor(255, 0, 0, 255),
                Component.translatable("let_me_see_see.gui.data_base.out_of_date").withStyle(ChatFormatting.RED));

        public final int color;
        public final Component message;

        State(int color, Component message) {
            this.color = color;
            this.message = message;
        }

        public static State of(String className) {
            return of(className, ExportsDataManager.recordedClasses.get(className));
        }

        public static State of(String className, ExportsDataManager.ExportEnv exportEnv) {
            if (exportEnv == null || !ExportsDataManager.recordedClasses.containsKey(className))
                return Unexport;
            if (ExportsDataManager.EXPORT_ENV.equals(exportEnv)) return Normal;
            if (ExportsDataManager.EXPORT_ENV.modListMD5().equals(exportEnv.modListMD5())) return Old;
            return OutOfDate;
        }
    }
}
