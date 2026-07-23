package com.xkball.let_me_see_see;

import com.mojang.logging.LogUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.xkball.let_me_see_see.client.ScreenProviders;
import com.xkball.let_me_see_see.common.data.ExportsDataManager;
import com.xkball.let_me_see_see.client.gui.screen.ExplorerScreen;
import com.xkball.let_me_see_see.config.LMSConfig;
import com.xkball.let_me_see_see.utils.ClassSearcher;
import com.xkball.let_me_see_see.utils.ClassStaticAnalysis;
import com.xkball.let_me_see_see.utils.JavaWorkaround;
import com.xkball.let_me_see_see.utils.ThrowableSupplier;
import com.xkball.let_me_see_see.utils.VanillaUtils;
import com.xkball.xklibmc.annotation.NonNullByDefault;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mod(value = LetMeSeeSeeClient.MODID, dist = Dist.CLIENT)
//@ModMeta(useLanguages = {"en_us","zh_cn"})
@NonNullByDefault
public class LetMeSeeSeeClient {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    private static final MethodHandle LOAD_AGENT = ThrowableSupplier.getOrNull(() -> Objects.requireNonNull(JavaWorkaround.TRUSTED_LOOKUP)
            .unreflect(Class.forName("sun.instrument.InstrumentationImpl").getMethod("loadAgent", String.class)),
            (e) -> LOGGER.error("Can not get MethodHandle: sun.instrument.InstrumentationImpl.loadAgent",e));
    
    public static final String MODID = "let_me_see_see";
    public static final String JAR_PATH_KEY = "LET_ME_SEE_AGENT_JAR_PATH";
    public static final String EXPORT_PATH_KEY = "LET_ME_SEE_EXPORT_PATH";
    public static final boolean IS_DEBUG = SharedConstants.IS_RUNNING_WITH_JDWP;
    public static final UUID GAME_INSTANCE_UUID = UUID.randomUUID();
    public static String MOD_LIST_MD5;
    public static String JAR_PATH = "";
    public static String EXPORT_DIR_PATH;
    public static String[] CLASS_PATH;
    public static Instrumentation INST;
    private static boolean agentLoadFailed = false;

    public LetMeSeeSeeClient(IEventBus modEventBus, ModContainer modContainer) {
        CLASS_PATH = System.getProperty("java.class.path").split(File.pathSeparator);
        EXPORT_DIR_PATH = FMLPaths.getOrCreateGameRelativePath(Path.of(MODID)).toString();
        MOD_LIST_MD5 = VanillaUtils.md5(ModList.get().getMods().stream()
                .flatMap(mif -> Stream.of(mif.getModId(), mif.getVersion().toString()))
                .collect(Collectors.joining()));
        ExportsDataManager.EXPORT_ENV = new ExportsDataManager.ExportEnv(GAME_INSTANCE_UUID, MOD_LIST_MD5);
        var jar = modContainer.getModInfo().getOwningFile().getFile().getFilePath().toFile();
        if (jar.isDirectory()) {
            JAR_PATH = System.getProperty(JAR_PATH_KEY);
            if (JAR_PATH == null || JAR_PATH.isEmpty()) {
                LOGGER.error("This mod require it's jar path to work! Missing system property: " + JAR_PATH_KEY);
            }
        } else {
            JAR_PATH = jar.getAbsolutePath();
        }
        LOGGER.info("{}: {}", JAR_PATH_KEY, JAR_PATH);
        LOGGER.info("{}: {}", EXPORT_PATH_KEY, EXPORT_DIR_PATH);
        LOGGER.info("{}: {}", "MOD_LIST_MD5", MOD_LIST_MD5);
        ClassStaticAnalysis.scanOnlyIn(MODID);
        modContainer.registerConfig(ModConfig.Type.CLIENT, LMSConfig.SPEC);
    }
    
    public static void scanClasses(Class<?>... classes) {
        scanClasses(List.of(classes));
    }
    
    public static void scanClasses(List<Class<?>> classes) {
        ExportsDataManager.addExportClass(classes);
        classes.forEach(LetMeSeeSeeClient::runExportClass);
        ExportsDataManager.sentMessages();
    }
    
    private static void tryGetInst(){
        try {
            INST = (Instrumentation) Class.forName("com.xkball.let_me_see_see.LMSAgent", true, ClassLoader.getSystemClassLoader())
                    .getField("INST").get(null);
        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
            LOGGER.error("Failed to get instrumentation", e);
        }
    }
    
    private static Path createTempJar(){
        return VanillaUtils.copyToTempDir(JAR_PATH, "let_me_see_see_temp.jar");
    }
    
    public static Instrumentation getInst() {
        if (INST != null) return INST;
        if (agentLoadFailed) return null;
        var jar = createTempJar().toFile().getAbsolutePath();
        LOGGER.info("Start get instrumentation via MethodHandle.");
        if(LOAD_AGENT != null) {
            try {
                LOAD_AGENT.invokeExact(jar);
            } catch (Throwable e) {
                LOGGER.error("Failed invoke loadAgent", e);
            }
            tryGetInst();
        }
        if(INST == null){
            LOGGER.info("Start get instrumentation via Attach JVM.");
            var pid = ProcessHandle.current().pid();
            var javaHome = System.getProperty("java.home");
            var process = new ProcessBuilder("java", "-jar", jar, String.valueOf(pid), jar);
            process.directory(new File(javaHome, "bin"));
            process.redirectErrorStream(true);
            process.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            try {
                process.start().waitFor();
            } catch (IOException | InterruptedException e) {
                LOGGER.error("Failed to load java agent", e);
            }
            tryGetInst();
        }
        if (INST == null) {
            agentLoadFailed = true;
            LOGGER.warn("Failed to get instrumentation after all. Class browser features will be unavailable.");
            return null;
        }
        INST.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                return recordTransformer(classBeingRedefined, classfileBuffer);
            }
        }, true);
        
        return INST;
    }

    public static boolean isAgentAvailable() {
        return getInst() != null;
    }

    public static void runExportClass(Class<?> clazz) {
        var inst = getInst();
        if (inst == null) {
            LOGGER.warn("Cannot export class, instrumentation not available");
            return;
        }
        var className = getClassName(clazz);
        try {
            LOGGER.info(className);
            inst.retransformClasses(clazz);
        } catch (UnmodifiableClassException e) {
            LOGGER.warn("class not support retransform {}", className);
        } catch (ClassFormatError e) {
            LOGGER.warn("class format error {}", className);
        } catch (Throwable e) {
            LOGGER.error("class transform error {}", className, e);
        }
    }
    
    public static byte[] recordTransformer(Class<?> clazz, byte[] src) {
        if (!ExportsDataManager.canExport(clazz)) {
            return src;
        }
        if (writeClassCode(getClassName(clazz), src)) {
            ExportsDataManager.finishClassExport(clazz);
            ExportsDataManager.resultQueue.add(createExportResultMessage(clazz, true));
        } else {
            ExportsDataManager.resultQueue.add(createExportResultMessage(clazz, false));
        }
        return src;
    }

    private static Component createExportResultMessage(Class<?> clazz, boolean success) {
        var topLevelClass = clazz;
        while (topLevelClass.getEnclosingClass() != null) {
            topLevelClass = topLevelClass.getEnclosingClass();
        }
        var className = Component.literal(topLevelClass.getSimpleName())
                .withStyle(success ? ChatFormatting.AQUA : ChatFormatting.RED)
                .withStyle(style -> style
                        .withClickEvent(new ClickEvent.RunCommand("/letmeseesee " + clazz.getName()))
                        .withHoverEvent(new HoverEvent.ShowText(Component.translatable("let_me_see_see.message.export.open_class"))));
        return Component.translatable(success
                ? "let_me_see_see.message.export.success"
                : "let_me_see_see.message.export.failure", className);
    }
    
    public static String getClassName(Class<?> clazz) {
        return clazz.getName().replace('.', File.separatorChar) + ".class";
    }
    
    public static boolean writeClassCode(String className, byte[] code) {
        var filePath = Path.of(EXPORT_DIR_PATH, className);
        try {
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, code);
        } catch (IOException e) {
            LOGGER.error("cannot write class code to file", e);
            return false;
        }
        return true;
    }
    
    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerClientCommands(RegisterClientCommandsEvent event) {
            var command = Commands.literal("letmeseesee")
                    .then(Commands.argument("className", StringArgumentType.greedyString())
                            .executes(context -> openClassBrowser(StringArgumentType.getString(context, "className"))));
            event.getDispatcher().register(command);
            event.getDispatcher().register(Commands.literal("lms")
                    .then(Commands.argument("className", StringArgumentType.greedyString())
                            .executes(context -> openClassBrowser(StringArgumentType.getString(context, "className")))));
        }

        private static int openClassBrowser(String className) {
            ClassSearcher.buildClassMap();
            if (ClassSearcher.ofClassName(className).isEmpty()) {
                var player = Minecraft.getInstance().player;
                if (player != null) {
                    player.sendSystemMessage(Component.translatable("let_me_see_see.command.class_not_found", className));
                }
                return 0;
            }
            Minecraft.getInstance().setScreen(new ExplorerScreen(className));
            return 1;
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            ScreenProviders.init();
        }
        
        @SubscribeEvent
        public static void onRegGuiLayerDef(RegisterGuiLayersEvent event) {
        
        }
    }
    
}
