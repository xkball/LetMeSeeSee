package com.xkball.let_me_see_see.client.gui.xkwidget;

import com.xkball.let_me_see_see.client.gui.screen.DataBaseScreen;
import com.xkball.let_me_see_see.utils.ThrowableSupplier;
import com.xkball.let_me_see_see.utils.JavaWorkaround;
import com.xkball.xklib.ui.render.IComponent;
import com.xkball.xklib.ui.widget.Button;
import com.xkball.xklib.ui.widget.container.ContainerWidget;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class ClassTreeModel {

    private static final MethodHandle IS_TOP_CLASS = ThrowableSupplier.getOrThrow(
            () -> JavaWorkaround.getTrustedLookup().findVirtual(Class.class, "isTopLevelClass",
                    MethodType.methodType(boolean.class)));

    public final PackageLabel root;

    public ClassTreeModel(Collection<Class<?>> classes) {
        this.root = new PackageLabel("", 0);
        root.open = true;
        for (var clazz : classes) {
            if (!isNormalClass(clazz)) continue;
            var path = clazz.getName().split("\\.");
            var node = root;
            for (var i = 0; i < path.length - 1; i++) {
                PackageLabel finalNode = node;
                node = node.pkg.computeIfAbsent(path[i],
                        str -> new PackageLabel(str, finalNode.depth + 1));
            }
            node.classes.put(path[path.length - 1],
                    new ClassLabelPlaceholder(clazz, node.depth + 1));
        }
    }

    public void copyOpenStates(@Nullable ClassTreeModel source) {
        if (source == null) return;
        copyOpenStates(source.root, this.root);
    }

    private static void copyOpenStates(PackageLabel source, PackageLabel target) {
        target.open = source.open;
        for (var entry : target.pkg.entrySet()) {
            var sourceChild = source.pkg.get(entry.getKey());
            if (sourceChild != null) {
                copyOpenStates(sourceChild, entry.getValue());
            }
        }
    }

    public static boolean isNormalClass(Class<?> clazz) {
        return ThrowableSupplier.getOrElse(
                () -> (boolean) IS_TOP_CLASS.invoke(clazz), false)
                && !clazz.isSynthetic()
                && !clazz.isArray();
    }

    public Map<String, String> collectImplicitImports(String packageName) {
        var result = new TreeMap<String, String>();
        collectPackageClasses(result, "java.lang");
        if (!packageName.isEmpty() && !"java.lang".equals(packageName)) {
            collectPackageClasses(result, packageName);
        }
        return result;
    }

    private void collectPackageClasses(Map<String, String> result, String packageName) {
        var node = findPackage(packageName);
        if (node == null) return;
        for (var entry : node.classes.entrySet()) {
            result.putIfAbsent(entry.getKey(), packageName + "." + entry.getKey());
        }
    }

    @Nullable
    private PackageLabel findPackage(String packageName) {
        var node = root;
        if (packageName.isEmpty()) return node;
        for (var part : packageName.split("\\.")) {
            node = node.pkg.get(part);
            if (node == null) return null;
        }
        return node;
    }

    public void addToContainer(ContainerWidget container, DataBaseScreen screen, Runnable onUpdate) {
        addToContainer(container, root, screen, onUpdate);
    }

    public void addToContainer(ContainerWidget container, DataBaseScreen screen) {
        addToContainer(container, screen, () -> {});
    }

    private void addToContainer(ContainerWidget container, PackageLabel node, DataBaseScreen screen, Runnable onUpdate) {
        var label = createPackageButton(node, onUpdate);
        label.inlineStyle("""
                size: 100% 8rpx;
                flex-shrink: 0;
                text-align: left;
                text-scale: expand-width;
                text-height: 8rpx;
                button-shape: rect;
                button-bg-color: 0x00FFFFFF;
                text-color: -1;
                text-drop-shadow: false;
                """);

        container.addChild(label);

        if (node.open) {
            for (var pk : node.pkg.values()) {
                addToContainer(container, pk, screen, onUpdate);
            }
            for (var clz : node.classes.values()) {
                var widget = clz.toWidget(screen);
                widget.inlineStyle("size: 100% 8rpx; flex-shrink: 0;");
                container.addChild(widget);
            }
        }
    }

    private Button createPackageButton(PackageLabel node, Runnable onUpdate) {
        var depth = node.depth;
        var prefix = "  ".repeat(depth) + (node.open ? "> " : "^ ");
        var text = prefix + node.packageName;
        return new Button(IComponent.literal(text), () -> {
            node.open = !node.open;
            onUpdate.run();
        });
    }

    public static class PackageLabel implements Comparable<PackageLabel> {
        public final int depth;
        public final String packageName;
        public final Map<String, PackageLabel> pkg = new TreeMap<>();
        public final Map<String, ClassLabelPlaceholder> classes = new TreeMap<>();
        public boolean open = false;

        public PackageLabel(String packageName, int depth) {
            this.packageName = packageName;
            this.depth = depth;
        }

        @Override
        public int compareTo(PackageLabel o) {
            return String.CASE_INSENSITIVE_ORDER.compare(packageName, o.packageName);
        }
    }

    public record ClassLabelPlaceholder(Class<?> clazz, int depth) {
        public ClassLabelWidget toWidget(DataBaseScreen screen) {
            return new ClassLabelWidget(clazz, screen, depth + 2);
        }
    }
}
