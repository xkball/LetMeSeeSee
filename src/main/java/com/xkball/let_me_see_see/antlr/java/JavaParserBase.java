package com.xkball.let_me_see_see.antlr.java;

import org.antlr.v4.runtime.*;

import java.util.List;

public abstract class JavaParserBase extends Parser {
	
	public JavaParserBase(TokenStream input){
		super(input);
	}
	
	public boolean DoLastRecordComponent() {
		ParserRuleContext ctx = this.getContext();
		if (!(ctx instanceof com.xkball.let_me_see_see.antlr.java.JavaParser.RecordComponentListContext)) {
			return true; // or throw if this is an unexpected state
		}
		
		com.xkball.let_me_see_see.antlr.java.JavaParser.RecordComponentListContext tctx = (com.xkball.let_me_see_see.antlr.java.JavaParser.RecordComponentListContext) ctx;
		List<com.xkball.let_me_see_see.antlr.java.JavaParser.RecordComponentContext> rcs = tctx.recordComponent();
		if (rcs.isEmpty()) return true;
		
		int count = rcs.size();
		for (int c = 0; c < count; ++c) {
			com.xkball.let_me_see_see.antlr.java.JavaParser.RecordComponentContext rc = rcs.get(c);
			if (rc.ELLIPSIS() != null && c + 1 < count)
				return false;
		}
		return true;
	}
}