package com.xkball.let_me_see_see.utils;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.util.Objects;

public class ClassStaticAnalysis {
    
    public static final String ONLY_IN = "Lnet/neoforged/api/distmarker/OnlyIn;";
    
    static {
        ClassSearcher.init();
    }
    
    public static void scanOnlyIn(String modid){
        var classes = ClassSearcher.ofModid(modid);
        for (var clazz : classes){
            try {
                var byteCode = Objects.requireNonNull(ClassStaticAnalysis.class.getClassLoader().getResourceAsStream(clazz)).readAllBytes();
                var classReader = new ClassReader(byteCode);
                classReader.accept(new OnlyInVisitor(),ClassReader.SKIP_CODE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    public static class OnlyInVisitor extends ClassVisitor {
        
        private String name;
        
        public OnlyInVisitor() {
            super(Opcodes.ASM9);
        }
        
        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            this.name = name;
        }
        
        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if(ONLY_IN.equals(descriptor)){
                System.out.println("Only In Class: " + name);
            }
            return super.visitAnnotation(descriptor, visible);
        }
    }
    
    
    //todo[xkball] 可选字节码来源
    public enum ByteCodeSource{
        RESOURCE,
        TRANSFORMER
    }
    
}
