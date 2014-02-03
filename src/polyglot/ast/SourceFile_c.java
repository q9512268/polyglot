/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 * 
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan 
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.ast;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import polyglot.frontend.Source;
import polyglot.translate.ExtensionRewriter;
import polyglot.types.Context;
import polyglot.types.ImportTable;
import polyglot.types.Package;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.util.CodeWriter;
import polyglot.util.CollectionUtil;
import polyglot.util.ListUtil;
import polyglot.util.Position;
import polyglot.util.SerialVersionUID;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.NodeVisitor;
import polyglot.visit.PrettyPrinter;
import polyglot.visit.TypeBuilder;
import polyglot.visit.TypeChecker;

/**
 * A {@code SourceFile} is an immutable representations of a Java
 * language source file.  It consists of a package name, a list of 
 * {@code Import}s, and a list of {@code GlobalDecl}s.
 */
public class SourceFile_c extends Node_c implements SourceFile {
    private static final long serialVersionUID = SerialVersionUID.generate();

    protected PackageNode package_;
    protected List<Import> imports;
    protected List<TopLevelDecl> decls;
    protected ImportTable importTable;
    protected Source source;

    public SourceFile_c(Position pos, PackageNode package_,
            List<Import> imports, List<TopLevelDecl> decls) {
        super(pos);
        assert (imports != null && decls != null && !decls.isEmpty()); // package_ may be null, imports empty
        this.package_ = package_;
        this.imports = ListUtil.copy(imports, true);
        this.decls = ListUtil.copy(decls, true);
    }

    @Override
    public boolean isDisambiguated() {
        return super.isDisambiguated() && this.importTable != null;
    }

    @Override
    public Source source() {
        return this.source;
    }

    @Override
    public SourceFile source(Source source) {
        SourceFile_c n = (SourceFile_c) copy();
        n.source = source;
        return n;
    }

    @Override
    public PackageNode package_() {
        return this.package_;
    }

    @Override
    public SourceFile package_(PackageNode package_) {
        SourceFile_c n = (SourceFile_c) copy();
        n.package_ = package_;
        return n;
    }

    @Override
    public List<Import> imports() {
        return Collections.unmodifiableList(this.imports);
    }

    @Override
    public SourceFile imports(List<Import> imports) {
        SourceFile_c n = (SourceFile_c) copy();
        n.imports = ListUtil.copy(imports, true);
        return n;
    }

    @Override
    public List<TopLevelDecl> decls() {
        return Collections.unmodifiableList(this.decls);
    }

    @Override
    public SourceFile decls(List<TopLevelDecl> decls) {
        SourceFile_c n = (SourceFile_c) copy();
        n.decls = ListUtil.copy(decls, true);
        return n;
    }

    @Override
    public ImportTable importTable() {
        return this.importTable;
    }

    @Override
    public SourceFile importTable(ImportTable importTable) {
        SourceFile_c n = (SourceFile_c) copy();
        n.importTable = importTable;
        return n;
    }

    /** Reconstruct the source file. */
    protected SourceFile_c reconstruct(PackageNode package_,
            List<Import> imports, List<TopLevelDecl> decls) {
        if (package_ != this.package_
                || !CollectionUtil.equals(imports, this.imports)
                || !CollectionUtil.equals(decls, this.decls)) {
            SourceFile_c n = (SourceFile_c) copy();
            n.package_ = package_;
            n.imports = ListUtil.copy(imports, true);
            n.decls = ListUtil.copy(decls, true);
            return n;
        }

        return this;
    }

    @Override
    public Node visitChildren(NodeVisitor v) {
        PackageNode package_ = visitChild(this.package_, v);
        List<Import> imports = visitList(this.imports, v);
        List<TopLevelDecl> decls = visitList(this.decls, v);
        return reconstruct(package_, imports, decls);
    }

    /**
     * Build type objects for the source file.  Set the visitor's import table
     * field before we recurse into the declarations.
     */
    @Override
    public NodeVisitor buildTypesEnter(TypeBuilder tb) throws SemanticException {
        TypeSystem ts = tb.typeSystem();
        Package pkg = null;
        if (package_ != null) {
            pkg = package_.package_();
        }

        ImportTable it = ts.importTable(source.name(), pkg);
        tb = tb.pushPackage(pkg);
        tb.setImportTable(it);
        return tb;
    }

    @Override
    public Context enterScope(Context c) {
        return c.pushSource(importTable);
    }

    @Override
    public Node typeCheck(TypeChecker tc) throws SemanticException {
        Set<String> names = new LinkedHashSet<String>();
        boolean hasPublic = false;

        for (TopLevelDecl d : decls) {
            String s = d.name();

            if (names.contains(s)) {
                throw new SemanticException("Duplicate declaration: \"" + s
                        + "\".", d.position());
            }

            names.add(s);

            if (d.flags().isPublic()) {
                if (hasPublic) {
                    throw new SemanticException("The source contains more than one public declaration.",
                                                d.position());
                }

                hasPublic = true;
            }
        }

        return this;
    }

    @Override
    public Node extRewrite(ExtensionRewriter rw) throws SemanticException {
        SourceFile n = (SourceFile) super.extRewrite(rw);
        return n.importTable(null);
    }

    @Override
    public String toString() {
        return "<<<< " + source + " >>>>";
    }

    @Override
    public void prettyPrint(CodeWriter w, PrettyPrinter tr) {
        w.write("<<<< " + source + " >>>>");
        w.newline(0);

        if (package_ != null) {
            w.write("package ");
            print(package_, w, tr);
            w.write(";");
            w.newline(0);
            w.newline(0);
        }

        for (Import im : imports) {
            print(im, w, tr);
        }

        if (!imports.isEmpty()) {
            w.newline(0);
        }

        for (TopLevelDecl d : decls) {
            print(d, w, tr);
        }
    }

    @Override
    public Node disambiguateOverride(Node parent, AmbiguityRemover ar)
            throws SemanticException {
        /*
        SourceFile n = this;
        
        // Disambiguate imports and package declarations.
        OuterScopeDisambiguator osd = new OuterScopeDisambiguator(ar);
        n = (SourceFile) osd.visitEdgeNoOverride(parent, n);
        if (osd.hasErrors()) throw new SemanticException();

        // Ensure supertyperts and signatures are disambiguated for all
        // classes visible from the outer scope. 
        SupertypeDisambiguator sud = new SupertypeDisambiguator(ar);
        n = (SourceFile) sud.visitEdgeNoOverride(parent, n);
        if (sud.hasErrors()) throw new SemanticException();

        SignatureDisambiguator sid = new SignatureDisambiguator(ar);
        n = (SourceFile) sid.visitEdgeNoOverride(parent, n);
        if (sid.hasErrors()) throw new SemanticException();
        
        // Now type check the children.
        n = (SourceFile) ar.visitEdgeNoOverride(parent, n);
        if (ar.hasErrors()) throw new SemanticException();
        
        return n.disambiguate(ar);
         */
        return null;
    }

    @Override
    public void dump(CodeWriter w) {
        super.dump(w);
        w.begin(0);
        w.allowBreak(4, " ");
        w.write("(import-table " + importTable + ")");
        w.end();
    }

    @Override
    public Node copy(NodeFactory nf) {
        return nf.SourceFile(this.position,
                             this.package_,
                             this.imports,
                             this.decls).source(this.source);
    }

}
