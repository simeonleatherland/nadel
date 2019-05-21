package graphql.nadel.engine.transformation;

import graphql.analysis.QueryVisitorFieldEnvironment;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.MergedField;
import graphql.execution.nextgen.result.ExecutionResultNode;
import graphql.language.Field;
import graphql.nadel.engine.UnapplyEnvironment;
import graphql.schema.GraphQLOutputType;
import graphql.util.TraversalControl;

import java.util.ArrayList;
import java.util.List;

import static graphql.Assert.assertTrue;
import static graphql.nadel.engine.transformation.FieldUtils.resultKeyForField;

public abstract class FieldTransformation {

    public static final String NADEL_FIELD_ID = "FIELD_ID";


    /**
     * This is a bit strange method because n FieldTransformations map to one unapply method and we don't know the mapping until
     * this method is called. So we actually give all relevant transformations as a List
     */
    public abstract ExecutionResultNode unapplyResultNode(ExecutionResultNode executionResultNode,
                                                          List<FieldTransformation> allTransformations,
                                                          UnapplyEnvironment environment);


    private String resultKey;
    private QueryVisitorFieldEnvironment environment;

    public TraversalControl apply(QueryVisitorFieldEnvironment environment) {
        this.environment = environment;
        resultKey = resultKeyForField(environment.getField());
        // Not changing node means it will be preserved as is
        return TraversalControl.CONTINUE;
    }


    public QueryVisitorFieldEnvironment getOriginalFieldEnvironment() {
        return environment;
    }

    public Field getOriginalField() {
        return getOriginalFieldEnvironment().getField();
    }

    public GraphQLOutputType getOriginalFieldType() {
        return getOriginalFieldEnvironment().getFieldDefinition().getType();
    }


    protected ExecutionStepInfo replaceFieldsWithOriginalFields(List<FieldTransformation> allTransformations, ExecutionStepInfo esi) {
        MergedField underlyingMergedField = esi.getField();
        List<Field> underlyingFields = underlyingMergedField.getFields();
        assertTrue(allTransformations.size() == underlyingFields.size());

        // core objective: replacing the fields with the original fields
        List<Field> newFields = new ArrayList<>();
        for (FieldTransformation fieldTransformation : allTransformations) {
            newFields.add(fieldTransformation.getOriginalField());
        }
        MergedField newMergedField = MergedField.newMergedField(newFields).build();
        ExecutionStepInfo esiWithMappedField = esi.transform(builder -> builder.field(newMergedField));
        return esiWithMappedField;
    }


}
