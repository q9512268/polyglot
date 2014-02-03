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

import java.util.List;

/**
 * An immutable representation of a Java language {@code for}
 * statement.  Contains a statement to be executed and an expression
 * to be tested indicating whether to reexecute the statement.
 */
public interface For extends Loop {
    /** List of initialization statements.
     * @return A list of {@link polyglot.ast.ForInit ForInit}.
     */
    List<ForInit> inits();

    /** Set the list of initialization statements.
     * @param inits A list of {@link polyglot.ast.ForInit ForInit}.
     */
    For inits(List<ForInit> inits);

    /** Set the loop condition */
    For cond(Expr cond);

    /** List of iterator expressions.
     * @return A list of {@link polyglot.ast.ForUpdate ForUpdate}.
     */
    List<ForUpdate> iters();

    /** Set the list of iterator expressions.
     * @param iters A list of {@link polyglot.ast.ForUpdate ForUpdate}.
     */
    For iters(List<ForUpdate> iters);

    /** Set the loop body */
    @Override
    For body(Stmt body);
}
