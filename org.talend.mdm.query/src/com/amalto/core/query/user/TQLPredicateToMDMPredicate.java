/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */
package com.amalto.core.query.user;

import com.amalto.xmlserver.interfaces.IWhereItem;
import com.amalto.xmlserver.interfaces.WhereCondition;
import org.apache.commons.lang.NotImplementedException;
import org.talend.tql.model.AllFields;
import org.talend.tql.model.AndExpression;
import org.talend.tql.model.ComparisonExpression;
import org.talend.tql.model.ComparisonOperator;
import org.talend.tql.model.FieldBetweenExpression;
import org.talend.tql.model.FieldCompliesPattern;
import org.talend.tql.model.FieldContainsExpression;
import org.talend.tql.model.FieldInExpression;
import org.talend.tql.model.FieldIsEmptyExpression;
import org.talend.tql.model.FieldIsInvalidExpression;
import org.talend.tql.model.FieldIsValidExpression;
import org.talend.tql.model.FieldMatchesRegex;
import org.talend.tql.model.FieldReference;
import org.talend.tql.model.FieldWordCompliesPattern;
import org.talend.tql.model.LiteralValue;
import org.talend.tql.model.NotExpression;
import org.talend.tql.model.OrExpression;
import org.talend.tql.model.TqlElement;
import org.talend.tql.visitor.IASTVisitor;

import java.util.Arrays;

public class TQLPredicateToMDMPredicate implements IASTVisitor<IWhereItem> {

    private WhereCondition whereCondition = new WhereCondition();

    @Override
    public IWhereItem visit(TqlElement tqlElement) {
        throw new NotImplementedException("TqlElement not implemented.");
    }

    @Override
    public IWhereItem visit(ComparisonOperator comparisonOperator) {
        switch (comparisonOperator.getOperator().name()) {
            case "EQ" :
                whereCondition.setOperator("=");
                break;
            case "NEQ" :
                whereCondition.setOperator("!=");
                break;
            case "LT" :
                whereCondition.setOperator("<");
                break;
            case "GT" :
                whereCondition.setOperator(">");
                break;
            case "LET" :
                whereCondition.setOperator("<=");
                break;
            case "GET" :
                whereCondition.setOperator(">=");
                break;
            default :
                throw new NotImplementedException("'" + comparisonOperator.getOperator().name() + "' support not implemented.");
        }
        return null;
    }

    @Override
    public IWhereItem visit(LiteralValue literalValue) {
        whereCondition.setRightValueOrPath(literalValue.getValue());
        return null;
    }

    @Override
    public IWhereItem visit(FieldReference fieldReference) {
        whereCondition.setLeftPath(fieldReference.getPath().substring(1, fieldReference.getPath().length()-1));
        return null;
    }

    @Override
    public IWhereItem visit(org.talend.tql.model.Expression expression) {
        throw new NotImplementedException("Expression not implemented.");
    }

    @Override
    public IWhereItem visit(AndExpression andExpression) {
        Arrays.stream(andExpression.getExpressions()).forEach(expression -> expression.accept(this));
        return null;
    }

    @Override
    public IWhereItem visit(OrExpression orExpression) {
        if (orExpression.getExpressions().length == 1) {
            orExpression.getExpressions()[0].accept(this);
        } else {
            //Arrays.stream(orExpression.getExpressions()).forEach(expression -> expression.accept(this));
            throw new NotImplementedException("Or expression not implemented.");
        }
        return whereCondition;
    }

    @Override
    public IWhereItem visit(ComparisonExpression comparisonExpression) {
        comparisonExpression.getField().accept(this);
        comparisonExpression.getValueOrField().accept(this);
        comparisonExpression.getOperator().accept(this);
        return whereCondition;
    }

    @Override
    public IWhereItem visit(FieldInExpression fieldInExpression) {
        throw new NotImplementedException("FieldIn expression not implemented.");
    }

    @Override
    public IWhereItem visit(FieldIsEmptyExpression fieldIsEmptyExpression) {
        throw new NotImplementedException("FieldIsEmpty expression not implemented.");
    }

    @Override
    public IWhereItem visit(FieldIsValidExpression fieldIsValidExpression) {
        throw new NotImplementedException("FieldIsEmpty expression not implemented.");
    }

    @Override
    public IWhereItem visit(FieldIsInvalidExpression fieldIsInvalidExpression) {
        throw new NotImplementedException("FieldIsInvalid expression not implemented.");
    }

    @Override
    public IWhereItem visit(FieldMatchesRegex fieldMatchesRegex) {
        throw new NotImplementedException("FieldMatches regex not implemented.");
    }

    @Override
    public IWhereItem visit(FieldCompliesPattern fieldCompliesPattern) {
        throw new NotImplementedException("FieldComplies pattern not implemented.");
    }

    @Override
    public IWhereItem visit(FieldWordCompliesPattern fieldWordCompliesPattern) {
        throw new NotImplementedException("FieldWordComplies pattern not implemented.");
    }

    @Override
    public IWhereItem visit(FieldBetweenExpression fieldBetweenExpression) {
        throw new NotImplementedException("FieldBetween expression not implemented.");
    }

    @Override
    public IWhereItem visit(NotExpression notExpression) {
        throw new NotImplementedException("NotExpression not implemented.");
    }

    @Override
    public IWhereItem visit(FieldContainsExpression fieldContainsExpression) {
        fieldContainsExpression.getField().accept(this);
        whereCondition.setOperator("CONTAINS");
        whereCondition.setRightValueOrPath(fieldContainsExpression.getValue());
        return null;
    }

    @Override
    public IWhereItem visit(AllFields allFields) {
        throw new NotImplementedException("AllFields not implemented.");
    }
}
