/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.amalto.commons.core.utils.xpath.ri.compiler;

import java.util.ArrayList;

/**
 * Implementation of {@link Expression} for the operation "&gt;=".
 *
 * @author Dmitri Plotnikov
 * @version $Revision: 652845 $ $Date: 2008-05-02 12:46:46 -0500 (Fri, 02 May 2008) $
 */
public class CoreOperationGreaterThanOrEqual extends
        CoreOperationRelationalExpression {

    /**
     * Create a new CoreOperationGreaterThanOrEqual.
     * @param arg1 operand 1
     * @param arg2 operand 2
     */
    public CoreOperationGreaterThanOrEqual(Expression arg1, Expression arg2) {
        super(new Expression[] { arg1, arg2 });
    }

    protected boolean evaluateCompare(int compare) {
        return compare >= 0;
    }

    public String getSymbol() {
        return ">=";
    }

    public CoreOperationGreaterThanOrEqual clone(boolean deep) {
    	Expression[] arguments;
    	if (! deep) {
    		arguments = this.args;
    	} else {
    		ArrayList<Expression> as = new ArrayList<Expression>();
        	if (this.args != null) {
        		for (int i = 0; i < this.args.length; i++) {
        			as.add(this.args[i].clone(true));
                }
        	}
        	arguments = as.toArray(new Expression[as.size()]);
    	}
    	return new CoreOperationGreaterThanOrEqual(arguments[0], arguments[1]);
    }
}
