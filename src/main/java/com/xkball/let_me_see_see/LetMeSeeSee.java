package com.xkball.let_me_see_see;

import com.xkball.let_me_see_see.common.data.ExportsDataManager;
import com.xkball.let_me_see_see.common.item.LMSItems;
import com.xkball.let_me_see_see.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProtectionDomain;
import java.util.List;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(LetMeSeeSee.MODID)
public class LetMeSeeSee {

    public static final String MODID = "let_me_see_see";
    public static final String JAR_PATH_KEY = "LET_ME_SEE_AGENT_JAR_PATH";
    public static final String EXPORT_PATH_KEY = "LET_ME_SEE_EXPORT_PATH";
    public static final boolean IS_DEBUG = SharedConstants.IS_RUNNING_WITH_JDWP;
    public static String JAR_PATH;
    public static String EXPORT_PATH;
    private static final Logger LOGGER = LogUtils.getLogger();
    public static Instrumentation INST;
    
    public LetMeSeeSee(IEventBus modEventBus, ModContainer modContainer) {
        LMSItems.init(modEventBus);
        EXPORT_PATH = FMLPaths.getOrCreateGameRelativePath(Path.of(MODID)).toString();
        if(IS_DEBUG){
            JAR_PATH = System.getProperty(JAR_PATH_KEY);
        }
        else {
            JAR_PATH = modContainer.getModInfo().getOwningFile().getFile().getFilePath().toString();
        }
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
    
    public static void scanClasses(Class<?>... classes){
        scanClasses(List.of(classes));
    }
    
    public static void scanClasses(List<Class<?>> classes){
        ExportsDataManager.addExportClass(classes);
        if(INST == null) getInst();
        classes.forEach(LetMeSeeSee::runExportClass);
        ExportsDataManager.sentMessages();
    }
    
    public static void getInst(){
        var pid = ProcessHandle.current().pid();
        var javaHome = System.getProperty("java.home");
        var process = new ProcessBuilder("java", "-jar",JAR_PATH,String.valueOf(pid),JAR_PATH);
        process.directory(new File(javaHome, "bin"));
        process.redirectErrorStream(true);
        process.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        try {
            process.start().waitFor();
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Failed to load java agent",e);
            throw new RuntimeException(e);
        }
        try {
            INST = (Instrumentation) Class.forName("com.xkball.let_me_see_see.LMSAgent", true, ClassLoader.getSystemClassLoader())
                    .getField("INST").get(null);
        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
            LOGGER.error("Failed to get instrumentation",e);
            throw new RuntimeException(e);
        }
        INST.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                return recordTransformer(classBeingRedefined, classfileBuffer);
            }
        },true);
    }
    
    public static void runExportClass(Class<?> clazz){
        var className = getClassName(clazz);
        //不太需要classloader检查? 毕竟lambda什么的classloader都是null
//            var classLoader = clazz.getClassLoader();
//            if(classLoader == null) {
//                LOGGER.warn("Class loader for {} is null.", className);
//            }
        try {
            LOGGER.info(className);
            //不使用clazz.getClassLoader().getResourceAsStream(classname) 因为只能获取未transform类
            INST.retransformClasses(clazz);
        }catch (UnmodifiableClassException e) {
            LOGGER.warn("class not support retransform {}",className);
        }catch (ClassFormatError e){
            LOGGER.warn("class format error {}",className);
        }catch (Throwable e){
            LOGGER.error("class transform error {}",className,e);
        }
    }
    
    public static byte[] recordTransformer(Class<?> clazz, byte[] src){
        if(!ExportsDataManager.canExport(clazz)){
            return src;
        }
        if(writeClassCode(getClassName(clazz),src)){
            ExportsDataManager.finishClassExport(clazz);
            ExportsDataManager.resultQueue.add(Component.literal("Successfully export class " + getClassName(clazz)));
        }
        else {
            ExportsDataManager.resultQueue.add(Component.literal("Failed to export class " + getClassName(clazz)).withStyle(ChatFormatting.RED));
        }
        return src;
    }
    
    public static String getClassName(Class<?> clazz){
        return clazz.getName().replace('.', File.separatorChar)+".class";
    }
    
    public static boolean writeClassCode(String className,byte[] code){
        var filePath = Path.of(EXPORT_PATH,className);
        try {
            Files.createDirectories(filePath.getParent());
            Files.write(filePath,code);
        } catch (IOException e) {
            LOGGER.error("cannot write class code to file",e);
            return false;
        }
        return true;
    }
    
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }
    }
    
}
