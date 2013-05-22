/*
 * Copyright (C) 2006-2013 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.query.optimization;

import com.amalto.core.query.user.*;
import com.amalto.core.storage.datasource.RDBMSDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.Types;

public class ConfigurableContainsOptimizer implements Optimizer {

    private static final Logger LOGGER = Logger.getLogger(ConfigurableContainsOptimizer.class);

    private static final Visitor<Condition> FULL_TEXT_CONTAINS_OPTIMIZATION = new FullTextContainsOptimization();

    private static final Visitor<Condition> DISABLED_CONTAINS = new DisabledContains();

    private static final Visitor<Boolean> HAS_CONTAINS = new HasContains();

    private static final Visitor<Boolean> HAS_FORBIDDEN_FULL_TEXT = new HasForbiddenFullTextPredicates();

    private final RDBMSDataSource dataSource;

    public ConfigurableContainsOptimizer(RDBMSDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void optimize(Select select) {
        // TODO Disable when using GT, LT... in query
        if (select.getCondition() != null) {
            Condition condition = select.getCondition();
            RDBMSDataSource.ContainsOptimization containsOptimization = dataSource.getContainsOptimization();
            if (containsOptimization == RDBMSDataSource.ContainsOptimization.LIKE) {
                // Nothing to do
                return;
            }
            if (hasContains(condition)) {
                switch (containsOptimization) {
                    case FULL_TEXT:
                        if (!dataSource.supportFullText()) {
                            // Don't do any optimization if full text is disabled on datasource.
                            LOGGER.error("Cannot use '" + containsOptimization + "': datasource '" + dataSource.getName() + "' does not support full text search.");
                        } else if (!select.getJoins().isEmpty()) {
                            LOGGER.warn("Cannot use '" + containsOptimization + "': query uses join clause.");
                        } else if (hasForbiddenFullTextPredicates(condition)) {
                            LOGGER.warn("Cannot use '" + containsOptimization + "': query uses full text forbidden predicates.");
                        } else {
                            condition = select.getCondition().accept(FULL_TEXT_CONTAINS_OPTIMIZATION);
                        }
                        break;
                    case DISABLED:
                        select.getCondition().accept(DISABLED_CONTAINS);
                        break;
                }
            }
            select.setCondition(condition);
        }
    }

    private static boolean hasForbiddenFullTextPredicates(Condition condition) {
        return condition.accept(HAS_FORBIDDEN_FULL_TEXT);
    }

    private static boolean hasContains(Condition condition) {
        return condition.accept(HAS_CONTAINS);
    }

    private static class DisabledContains extends VisitorAdapter<Condition> {

        @Override
        public Condition visit(Select select) {
            return select.getCondition().accept(this);
        }

        @Override
        public Condition visit(StringConstant constant) {
            return null;
        }

        @Override
        public Condition visit(Isa isa) {
            return isa;
        }

        @Override
        public Condition visit(UnaryLogicOperator condition) {
            return condition;
        }

        @Override
        public Condition visit(IsEmpty isEmpty) {
            return isEmpty;
        }

        @Override
        public Condition visit(IsNull isNull) {
            return isNull;
        }

        @Override
        public Condition visit(NotIsEmpty notIsEmpty) {
            return notIsEmpty;
        }

        @Override
        public Condition visit(NotIsNull notIsNull) {
            return notIsNull;
        }

        @Override
        public Condition visit(BinaryLogicOperator condition) {
            condition.getLeft().accept(this);
            condition.getRight().accept(this);
            return condition;
        }

        @Override
        public Condition visit(Compare condition) {
            Predicate predicate = condition.getPredicate();
            if (predicate == Predicate.CONTAINS) {
                throw new IllegalStateException("Cannot execute query: 'contains' use is disabled.");
            }
            return condition;
        }

        @Override
        public Condition visit(Range range) {
            return range;
        }

        @Override
        public Condition visit(Condition condition) {
            return condition;
        }

        @Override
        public Condition visit(FullText fullText) {
            return fullText;
        }

        @Override
        public Condition visit(FieldFullText fieldFullText) {
            return fieldFullText;
        }
    }

    private static class FullTextContainsOptimization extends VisitorAdapter<Condition> {

        private boolean isContains;

        private String containsValue;

        private Field currentField;

        @Override
        public Condition visit(Select select) {
            return select.getCondition().accept(this);
        }

        @Override
        public Condition visit(StringConstant constant) {
            if (isContains) {
                containsValue = constant.getValue();
            }
            return null;
        }

        @Override
        public Condition visit(Isa isa) {
            return isa;
        }

        @Override
        public Condition visit(UnaryLogicOperator condition) {
            return condition;
        }

        @Override
        public Condition visit(IsEmpty isEmpty) {
            return isEmpty;
        }

        @Override
        public Condition visit(IsNull isNull) {
            return isNull;
        }

        @Override
        public Condition visit(NotIsEmpty notIsEmpty) {
            return notIsEmpty;
        }

        @Override
        public Condition visit(NotIsNull notIsNull) {
            return notIsNull;
        }

        @Override
        public Condition visit(Field field) {
            currentField = field;
            return null;
        }

        @Override
        public Condition visit(BinaryLogicOperator condition) {
            Condition left = condition.getLeft().accept(this);
            Condition right = condition.getRight().accept(this);
            condition.setLeft(left);
            condition.setRight(right);
            return condition;
        }

        @Override
        public Condition visit(Compare condition) {
            Predicate predicate = condition.getPredicate();
            if (predicate == Predicate.CONTAINS) {
                isContains = true;
                containsValue = StringUtils.EMPTY;
                condition.getLeft().accept(this);
                condition.getRight().accept(this);

                ComplexTypeMetadata type = currentField.getFieldMetadata().getContainingType();
                if (type.getKeyFields().size() > 1) {
                    LOGGER.warn("Cannot use contains optimization 'full text' on type '" + type.getName() + "' (use of compound key).");
                } else {
                    if (Types.MULTI_LINGUAL.equals(currentField.getFieldMetadata().getType().getName())) {
                        return condition;
                    } else {
                        return new FieldFullText(currentField, containsValue);
                    }
                }
            }
            return condition;
        }

        @Override
        public Condition visit(Range range) {
            return range;
        }

        @Override
        public Condition visit(Condition condition) {
            return condition;
        }

        @Override
        public Condition visit(FullText fullText) {
            return fullText;
        }

        @Override
        public Condition visit(FieldFullText fieldFullText) {
            return fieldFullText;
        }
    }

    private static class HasContains extends VisitorAdapter<Boolean> {
        @Override
        public Boolean visit(Compare condition) {
            return condition.getPredicate() == Predicate.CONTAINS;
        }

        @Override
        public Boolean visit(Condition condition) {
            return false;
        }

        @Override
        public Boolean visit(BinaryLogicOperator condition) {
            return condition.getLeft().accept(this) || condition.getRight().accept(this);
        }

        @Override
        public Boolean visit(UnaryLogicOperator condition) {
            return condition.getCondition().accept(this);
        }

        @Override
        public Boolean visit(Range range) {
            return false;
        }

        @Override
        public Boolean visit(IsEmpty isEmpty) {
            return false;
        }

        @Override
        public Boolean visit(NotIsEmpty notIsEmpty) {
            return false;
        }

        @Override
        public Boolean visit(IsNull isNull) {
            return false;
        }

        @Override
        public Boolean visit(NotIsNull notIsNull) {
            return false;
        }

        @Override
        public Boolean visit(FullText fullText) {
            return false;
        }

        @Override
        public Boolean visit(FieldFullText fieldFullText) {
            return false;
        }

        @Override
        public Boolean visit(Isa isa) {
            return false;
        }
    }

    private static class HasForbiddenFullTextPredicates extends VisitorAdapter<Boolean> {
            @Override
            public Boolean visit(Compare condition) {
                return condition.getPredicate() == Predicate.GREATER_THAN ||
                        condition.getPredicate() == Predicate.GREATER_THAN_OR_EQUALS ||
                        condition.getPredicate() == Predicate.LOWER_THAN ||
                        condition.getPredicate() == Predicate.LOWER_THAN_OR_EQUALS;
            }

            @Override
            public Boolean visit(Condition condition) {
                return false;
            }

            @Override
            public Boolean visit(BinaryLogicOperator condition) {
                return condition.getLeft().accept(this) || condition.getRight().accept(this);
            }

            @Override
            public Boolean visit(UnaryLogicOperator condition) {
                return condition.getCondition().accept(this);
            }

            @Override
            public Boolean visit(Range range) {
                return false;
            }

            @Override
            public Boolean visit(IsEmpty isEmpty) {
                return false;
            }

            @Override
            public Boolean visit(NotIsEmpty notIsEmpty) {
                return false;
            }

            @Override
            public Boolean visit(IsNull isNull) {
                return false;
            }

            @Override
            public Boolean visit(NotIsNull notIsNull) {
                return false;
            }

            @Override
            public Boolean visit(FullText fullText) {
                return false;
            }

            @Override
            public Boolean visit(FieldFullText fieldFullText) {
                return false;
            }

            @Override
            public Boolean visit(Isa isa) {
                return false;
            }
        }
}