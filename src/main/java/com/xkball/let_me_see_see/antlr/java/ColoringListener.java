package com.xkball.let_me_see_see.antlr.java;

import com.xkball.let_me_see_see.config.ColorMapping;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ColoringListener extends com.xkball.let_me_see_see.antlr.java.JavaParserBaseListener {
    
    public static final Set<String> KEYWORDS = new HashSet<>(Set.of(
            "abstract", "assert", "boolean", "break", "byte",
            "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else",
            "enum", "extends", "final", "finally", "float",
            "for", "goto",
            "if", "implements", "import", "instanceof", "int",
            "interface", "long", "native", "new", "package",
            "private", "protected", "public", "return", "short",
            "static", "strictfp", "super", "switch", "synchronized",
            "this", "throw", "throws", "transient", "try",
            "void", "volatile", "while",
            "module", "open", "opens", "exports", "requires",
            "transitive", "uses", "provides", "with", "to",
            "null", "true", "false"
    ));
    
    private final Int2ObjectMap<ColorMapping> map;
    private final Map<String, String> imports = new HashMap<>();
    private String packageName = "";
    
    public ColoringListener(Int2ObjectMap<ColorMapping> map) {
        this.map = map;
    }

    public Map<String, String> getImports() {
        return imports;
    }

    public String getPackageName() {
        return packageName;
    }
    
    private void checkError(int index, ParserRuleContext ctx){
        if(ctx.exception != null){
            map.put(index, ColorMapping.ERROR);
        }
    }
    
    @Override
    public void visitTerminal(TerminalNode node) {
        super.visitTerminal(node);
        var token = node.getSymbol();
        var index = token.getTokenIndex();
        if (KEYWORDS.contains(token.getText())) {
            map.putIfAbsent(index,ColorMapping.KEY_WORDS);
        }
    }

    @Override
    public void enterImportDeclaration(com.xkball.let_me_see_see.antlr.java.JavaParser.ImportDeclarationContext ctx) {
        super.enterImportDeclaration(ctx);
        if (ctx.STATIC() != null) return;
        if (ctx.getText().endsWith(".*;")) return;
        var qualifiedName = ctx.qualifiedName();
        if (qualifiedName == null) return;
        var fullName = qualifiedName.getText();
        var index = fullName.lastIndexOf('.');
        if (index < 0 || index == fullName.length() - 1) return;
        imports.putIfAbsent(fullName.substring(index + 1), fullName);
    }

    @Override
    public void enterPackageDeclaration(com.xkball.let_me_see_see.antlr.java.JavaParser.PackageDeclarationContext ctx) {
        super.enterPackageDeclaration(ctx);
        var qualifiedName = ctx.qualifiedName();
        if (qualifiedName != null) {
            packageName = qualifiedName.getText();
        }
    }
    
    @Override
    public void enterClassOrInterfaceType(com.xkball.let_me_see_see.antlr.java.JavaParser.ClassOrInterfaceTypeContext ctx) {
        super.enterClassOrInterfaceType(ctx);
        var start = ctx.start.getTokenIndex();
        this.checkError(start, ctx);
        map.putIfAbsent(start, ColorMapping.CLASS_DEF);
    }
    
    @Override
    public void enterRecordDeclaration(com.xkball.let_me_see_see.antlr.java.JavaParser.RecordDeclarationContext ctx) {
        super.enterRecordDeclaration(ctx);
        var identifier = ctx.identifier();
        if (identifier != null) {
            var index = identifier.start.getTokenIndex();
            this.checkError(index, ctx);
            map.putIfAbsent(index,ColorMapping.CLASS_DEF);
        }
    }
    
    @Override
    public void enterClassDeclaration(com.xkball.let_me_see_see.antlr.java.JavaParser.ClassDeclarationContext ctx) {
        super.enterClassDeclaration(ctx);
        var identifier = ctx.identifier();
        if (identifier != null) {
            var index = identifier.start.getTokenIndex();
            this.checkError(index, ctx);
            map.putIfAbsent(index,ColorMapping.CLASS_DEF);
        }
    }
    
    @Override
    public void enterInterfaceDeclaration(com.xkball.let_me_see_see.antlr.java.JavaParser.InterfaceDeclarationContext ctx) {
        super.enterInterfaceDeclaration(ctx);
        var identifier = ctx.identifier();
        if (identifier != null) {
            var index = identifier.start.getTokenIndex();
            this.checkError(index, ctx);
            map.putIfAbsent(index,ColorMapping.CLASS_DEF);
        }
    }
    
    @Override
    public void enterMethodDeclaration(com.xkball.let_me_see_see.antlr.java.JavaParser.MethodDeclarationContext ctx) {
        super.enterMethodDeclaration(ctx);
        var identifier = ctx.identifier();
        if (identifier != null) {
            var index = identifier.start.getTokenIndex();
            this.checkError(index, ctx);
            map.putIfAbsent(index,ColorMapping.METHOD_DEF);
        }
    }
    
    @Override
    public void enterInterfaceMethodDeclaration(com.xkball.let_me_see_see.antlr.java.JavaParser.InterfaceMethodDeclarationContext ctx) {
        super.enterInterfaceMethodDeclaration(ctx);
        var identifier = ctx.interfaceCommonBodyDeclaration().identifier();
        if (identifier != null) {
            var index = identifier.start.getTokenIndex();
            this.checkError(index, ctx);
            map.putIfAbsent(index,ColorMapping.METHOD_DEF);
        }
    }
    
    @Override
    public void enterConstructorDeclaration(com.xkball.let_me_see_see.antlr.java.JavaParser.ConstructorDeclarationContext ctx) {
        super.enterConstructorDeclaration(ctx);
        var identifier = ctx.identifier();
        if(identifier != null){
            var index = identifier.start.getTokenIndex();
            this.checkError(index, ctx);
            map.putIfAbsent(index,ColorMapping.METHOD_DEF);
        }
    }
    
    @Override
    public void enterMethodCall(com.xkball.let_me_see_see.antlr.java.JavaParser.MethodCallContext ctx) {
        super.enterMethodCall(ctx);
        var identifier = ctx.identifier();
        if(identifier != null){
            var index = identifier.start.getTokenIndex();
            this.checkError(index, ctx);
            map.putIfAbsent(index,ColorMapping.METHOD_CALL);
        }
        if(ctx.THIS() != null){
            var index = ctx.THIS().getSymbol().getTokenIndex();
            this.checkError(index, ctx);
            map.putIfAbsent(index,ColorMapping.KEY_WORDS);
        }
        if(ctx.SUPER() != null){
            var index = ctx.SUPER().getSymbol().getTokenIndex();
            this.checkError(index, ctx);
            map.putIfAbsent(index,ColorMapping.KEY_WORDS);
        }
    }
    
    private void enterNumberLiteral(ParserRuleContext ctx){
        var start = ctx.start.getTokenIndex();
        this.checkError(start, ctx);
        map.putIfAbsent(start,ColorMapping.NUMBER_LITERAL);
    }
    
    @Override
    public void enterIntegerLiteral(com.xkball.let_me_see_see.antlr.java.JavaParser.IntegerLiteralContext ctx) {
        super.enterIntegerLiteral(ctx);
        this.enterNumberLiteral(ctx);
    }
    
    @Override
    public void enterFloatLiteral(com.xkball.let_me_see_see.antlr.java.JavaParser.FloatLiteralContext ctx) {
        super.enterFloatLiteral(ctx);
        this.enterNumberLiteral(ctx);
    }
    
    @Override
    public void enterLiteral(com.xkball.let_me_see_see.antlr.java.JavaParser.LiteralContext ctx) {
        super.enterLiteral(ctx);
        var index = -1;
        if(ctx.CHAR_LITERAL() != null){
            index = ctx.CHAR_LITERAL().getSymbol().getTokenIndex();
        }
        if(ctx.STRING_LITERAL() != null){
            index = ctx.STRING_LITERAL().getSymbol().getTokenIndex();
        }
        if(ctx.TEXT_BLOCK() != null){
            index = ctx.TEXT_BLOCK().getSymbol().getTokenIndex();
        }
        if(index != -1){
            this.checkError(index, ctx);
            map.putIfAbsent(index,ColorMapping.STRING_LITERAL);
            return;
        }
        if(ctx.BOOL_LITERAL() != null){
            index = ctx.BOOL_LITERAL().getSymbol().getTokenIndex();
        }
        if(ctx.NULL_LITERAL() != null){
            index = ctx.NULL_LITERAL().getSymbol().getTokenIndex();
        }
        if(index != -1){
            this.checkError(index, ctx);
            map.putIfAbsent(index,ColorMapping.KEY_WORDS);
        }
    }
    
    @Override
    public void enterSwitchLabel(com.xkball.let_me_see_see.antlr.java.JavaParser.SwitchLabelContext ctx) {
        super.enterSwitchLabel(ctx);
        var index = -1;
        if(ctx.IDENTIFIER() != null){
            index = ctx.IDENTIFIER().getSymbol().getTokenIndex();
        }
        if(ctx.CASE() != null){
            index = ctx.CASE().getSymbol().getTokenIndex();
        }
        if(ctx.DEFAULT() != null){
            index = ctx.DEFAULT().getSymbol().getTokenIndex();
        }
        if(index != -1){
            this.checkError(index, ctx);
            map.putIfAbsent(index,ColorMapping.KEY_WORDS);
        }
    }
    
    private void handleFieldDefContext(com.xkball.let_me_see_see.antlr.java.JavaParser.FieldDeclarationContext ctx, ColorMapping type){
        if(ctx.variableDeclarators() != null){
            for(var def : ctx.variableDeclarators().variableDeclarator()){
                var defID = def.variableDeclaratorId();
                if(defID == null) continue;
                var identifier = defID.identifier();
                if(identifier == null) continue;
                var index = identifier.start.getTokenIndex();
                this.checkError(index, ctx);
                map.putIfAbsent(index,type);
            }
        }
    }
    
    @Override
    public void enterClassOrInterfaceModifier(com.xkball.let_me_see_see.antlr.java.JavaParser.ClassOrInterfaceModifierContext ctx) {
        super.enterClassOrInterfaceModifier(ctx);
        if(ctx.STATIC() != null){
            var p = ctx.parent;
            if(!(p instanceof com.xkball.let_me_see_see.antlr.java.JavaParser.ModifierContext modCtx)) return;
            var pp = modCtx.parent;
            if(!(pp instanceof com.xkball.let_me_see_see.antlr.java.JavaParser.ClassBodyDeclarationContext bodyDefCtx)) return;
            bodyDefCtx.children.stream().filter(ctx_ -> ctx_ instanceof com.xkball.let_me_see_see.antlr.java.JavaParser.MemberDeclarationContext).toList().forEach(ctx_ -> {
                var fieldDefCtx = ((com.xkball.let_me_see_see.antlr.java.JavaParser.MemberDeclarationContext)(ctx_)).fieldDeclaration();
                if(fieldDefCtx != null){
                    handleFieldDefContext(fieldDefCtx, ColorMapping.STATIC_FIELD);
                }
            });
        }
    }
    
    @Override
    public void enterFieldDeclaration(com.xkball.let_me_see_see.antlr.java.JavaParser.FieldDeclarationContext ctx) {
        super.enterFieldDeclaration(ctx);
        handleFieldDefContext(ctx, ColorMapping.FIELD);
    }
    
    @Override
    public void enterAnnotation(com.xkball.let_me_see_see.antlr.java.JavaParser.AnnotationContext ctx) {
        super.enterAnnotation(ctx);
        if(ctx.AT() != null){
            var index = ctx.AT().getSymbol().getTokenIndex();
            this.checkError(index, ctx);
            map.putIfAbsent(index, ColorMapping.ANNOTATION);
        }
        if(ctx.qualifiedName() != null){
            var index = ctx.qualifiedName().start.getTokenIndex();
            this.checkError(index, ctx);
            map.putIfAbsent(index, ColorMapping.ANNOTATION);
        }
    }
}
