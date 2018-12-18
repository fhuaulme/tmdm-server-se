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

import com.amalto.xmlserver.interfaces.WhereCondition;
import org.apache.commons.lang.NotImplementedException;
import org.talend.mdm.commmon.metadata.MetadataRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.amalto.core.query.user.UserQueryBuilder.in;

public class TQLPredicateToMDMPredicate implements IASTVisitor<UserQueryBuilder> {

    private String leftPath;
    private List<String> values = new ArrayList<>();

    private MetadataRepository metadataRepository;

    private UserQueryBuilder userQueryBuilder = new UserQueryBuilder();

    public TQLPredicateToMDMPredicate(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    @Override
    public UserQueryBuilder visit(TqlElement tqlElement) {
        throw new NotImplementedException("TqlElement not implemented.");
    }

    @Override
    public UserQueryBuilder visit(ComparisonOperator comparisonOperator) {
        this.userQueryBuilder = UserQueryBuilder.where(this.userQueryBuilder, UserQueryHelper.buildCondition(this.userQueryBuilder, new WhereCondition(this.leftPath, this.getOperatorFromComparisonOperator(comparisonOperator.getOperator().name()), this.values.get(0), null), this.metadataRepository), null);
        return null;
    }

    @Override
    public UserQueryBuilder visit(LiteralValue literalValue) {
        this.values.add(literalValue.getValue());
        return null;
    }

    @Override
    public UserQueryBuilder visit(FieldReference fieldReference) {
        this.leftPath = fieldReference.getPath().substring(1, fieldReference.getPath().length()-1);
        return null;
    }

    @Override
    public UserQueryBuilder visit(org.talend.tql.model.Expression expression) {
        throw new NotImplementedException("Expression not implemented.");
    }

    @Override
    public UserQueryBuilder visit(AndExpression andExpression) {
        if (andExpression.getExpressions().length == 1) {
            andExpression.getExpressions()[0].accept(this);
            return this.userQueryBuilder;
        } else {
            //Arrays.stream(andExpression.getExpressions()).forEach(expression -> expression.accept(this));
            throw new NotImplementedException("And expression not implemented.");
        }
    }

    @Override
    public UserQueryBuilder visit(OrExpression orExpression) {
        if (orExpression.getExpressions().length == 1) {
            orExpression.getExpressions()[0].accept(this);
            return this.userQueryBuilder;
        } else {
            //Arrays.stream(orExpression.getExpressions()).forEach(expression -> expression.accept(this));
            throw new NotImplementedException("Or expression not implemented.");
        }
    }

    @Override
    public UserQueryBuilder visit(ComparisonExpression comparisonExpression) {
        comparisonExpression.getField().accept(this);
        comparisonExpression.getValueOrField().accept(this);
        comparisonExpression.getOperator().accept(this);
        return null;
    }

    @Override
    public UserQueryBuilder visit(FieldInExpression fieldInExpression) {
        fieldInExpression.getField().accept(this);
        Stream.of(fieldInExpression.getValues()).forEach(value -> this.values.add(value.getValue()));
        this.userQueryBuilder.where(in(this.metadataRepository.getComplexType(this.getEntity(this.leftPath)).getField(getField(this.leftPath)), this.values));
        return null;
    }

    @Override
    public UserQueryBuilder visit(FieldIsEmptyExpression fieldIsEmptyExpression) {
        throw new NotImplementedException("FieldIsEmpty expression not implemented.");
    }

    @Override
    public UserQueryBuilder visit(FieldIsValidExpression fieldIsValidExpression) {
        throw new NotImplementedException("FieldIsEmpty expression not implemented.");
    }

    @Override
    public UserQueryBuilder visit(FieldIsInvalidExpression fieldIsInvalidExpression) {
        throw new NotImplementedException("FieldIsInvalid expression not implemented.");
    }

    @Override
    public UserQueryBuilder visit(FieldMatchesRegex fieldMatchesRegex) {
        throw new NotImplementedException("FieldMatches regex not implemented.");
    }

    @Override
    public UserQueryBuilder visit(FieldCompliesPattern fieldCompliesPattern) {
        throw new NotImplementedException("FieldComplies pattern not implemented.");
    }

    @Override
    public UserQueryBuilder visit(FieldWordCompliesPattern fieldWordCompliesPattern) {
        throw new NotImplementedException("FieldWordComplies pattern not implemented.");
    }

    @Override
    public UserQueryBuilder visit(FieldBetweenExpression fieldBetweenExpression) {
        throw new NotImplementedException("FieldBetween expression not implemented.");
    }

    @Override
    public UserQueryBuilder visit(NotExpression notExpression) {
        throw new NotImplementedException("NotExpression not implemented.");
    }

    @Override
    public UserQueryBuilder visit(FieldContainsExpression fieldContainsExpression) {
        fieldContainsExpression.getField().accept(this);
        this.userQueryBuilder = UserQueryBuilder.where(this.userQueryBuilder, UserQueryHelper.buildCondition(userQueryBuilder, new WhereCondition(leftPath, "CONTAINS", fieldContainsExpression.getValue(), null), this.metadataRepository), null);
        return null;
    }

    @Override
    public UserQueryBuilder visit(AllFields allFields) {
        throw new NotImplementedException("AllFields not implemented.");
    }

    private String getEntity(String leftPath) {
        return leftPath.split("/")[0];
    }

    private String getField(String leftPath) {
        return leftPath.split("/")[1];
    }

    private String getOperatorFromComparisonOperator(String name) {
        String operator;
        switch (name) {
            case "EQ" :
                operator = "=";
                break;
            case "NEQ" :
                operator = "!=";
                break;
            case "LT" :
                operator = "<";
                break;
            case "GT" :
                operator = ">";
                break;
            case "LET" :
                operator = "<=";
                break;
            case "GET" :
                operator = ">=";
                break;
            default :
                throw new NotImplementedException("'" + name + "' support not implemented.");
        }
        return operator;
    }
}
