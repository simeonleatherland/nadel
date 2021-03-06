package graphql.nadel.engine;

import graphql.Assert;
import graphql.language.Argument;
import graphql.language.ArrayValue;
import graphql.language.BooleanValue;
import graphql.language.EnumValue;
import graphql.language.Field;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.Node;
import graphql.language.NullValue;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.language.VariableReference;
import graphql.nadel.util.FpKit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static graphql.Assert.assertNotNull;
import static graphql.Assert.assertTrue;
import static graphql.nadel.util.FpKit.filter;

public class FieldMetadataUtil {

    private static final String NADEL_FIELD_METADATA = "NADEL_FIELD_METADATA";
    private static final String OVERALL_TYPE_INFO = "OVERALL_TYPE_INFO";


    private static class FieldMetadata implements Serializable {
        private final String id;
        private final boolean rootOfTransformation;

        public FieldMetadata(String id, boolean rootOfTransformation) {
            this.id = id;
            this.rootOfTransformation = rootOfTransformation;
        }

        public String getId() {
            return id;
        }

        public boolean isRootOfTransformation() {
            return rootOfTransformation;
        }


    }

    public static List<String> getRootOfTransformationIds(Field field) {
        String serialized = field.getAdditionalData().get(NADEL_FIELD_METADATA);
        if (serialized == null) {
            return Collections.emptyList();
        }
        List<FieldMetadata> fieldMetadata = readMetadata(serialized);

        return FpKit.filterAndMap(fieldMetadata, FieldMetadata::isRootOfTransformation, FieldMetadata::getId);
    }

    public static List<String> getFieldIds(Field field) {
        String serialized = field.getAdditionalData().get(NADEL_FIELD_METADATA);
        if (serialized == null) {
            return Collections.emptyList();
        }
        List<FieldMetadata> fieldMetadata = readMetadata(serialized);
        return graphql.util.FpKit.map(fieldMetadata, FieldMetadata::getId);
    }

    public static Field addFieldMetadata(Field field, String id, boolean rootOfTransformation) {
        assertNotNull(id);
        String serialized = field.getAdditionalData().get(NADEL_FIELD_METADATA);
        List<FieldMetadata> fieldMetadata = new ArrayList<>();
        if (serialized != null) {
            fieldMetadata = readMetadata(serialized);
        }

        FieldMetadata newFieldMetadata = new FieldMetadata(id, rootOfTransformation);
        fieldMetadata.add(newFieldMetadata);
        String newSerializedValue = writeMetadata(fieldMetadata);
        return field.transform(builder -> builder.additionalData(NADEL_FIELD_METADATA, newSerializedValue));
    }

    public static String getUniqueRootFieldId(Field field) {
        String serialized = assertNotNull(field.getAdditionalData().get(NADEL_FIELD_METADATA), "nadel field id expected");
        List<FieldMetadata> fieldMetadata = readMetadata(serialized);
        List<FieldMetadata> rootFieldMetadata = filter(fieldMetadata, FieldMetadata::isRootOfTransformation);
        assertTrue(rootFieldMetadata.size() == 1, "exactly one root nadel infos expected");
        return rootFieldMetadata.get(0).id;
    }

    public static void setFieldMetadata(Field.Builder builder, String id, List<String> additionalIds, boolean rootOfTransformation) {
        assertNotNull(id);
        List<FieldMetadata> fieldMetadata = new ArrayList<>();

        FieldMetadata newFieldMetadata = new FieldMetadata(id, rootOfTransformation);
        fieldMetadata.add(newFieldMetadata);
        for (String additionalId : additionalIds) {
            fieldMetadata.add(new FieldMetadata(additionalId, false));
        }
        String newSerializedValue = writeMetadata(fieldMetadata);
        builder.additionalData(NADEL_FIELD_METADATA, newSerializedValue);

    }

    private static String writeMetadata(List<FieldMetadata> fieldMetadata) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(fieldMetadata);
                return Base64.getEncoder().encodeToString(baos.toByteArray());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static List<FieldMetadata> readMetadata(String serialized) {
        try {
            byte[] decoded = Base64.getDecoder().decode(serialized);
            ByteArrayInputStream bais = new ByteArrayInputStream(decoded);
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                return (List<FieldMetadata>) ois.readObject();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Field setOverallTypeInfoId(Field field, String id) {
        return field.transform(builder -> builder.additionalData(OVERALL_TYPE_INFO, id));
    }

    public static Argument setOverallTypeInfoId(Argument argument, String id) {
        return argument.transform(builder -> builder.additionalData(OVERALL_TYPE_INFO, id));
    }

    public static Value setOverallTypeInfoId(Value node, String id) {
        if (node instanceof VariableReference) {
            VariableReference value = (VariableReference) node;
            return value.transform(builder -> builder.additionalData(OVERALL_TYPE_INFO, id));
        }
        if (node instanceof StringValue) {
            StringValue value = (StringValue) node;
            return value.transform(builder -> builder.additionalData(OVERALL_TYPE_INFO, id));
        }
        if (node instanceof NullValue) {
            NullValue value = (NullValue) node;
            return value.transform(builder -> builder.additionalData(OVERALL_TYPE_INFO, id));
        }
        if (node instanceof ObjectValue) {
            ObjectValue value = (ObjectValue) node;
            return value.transform(builder -> builder.additionalData(OVERALL_TYPE_INFO, id));
        }
        if (node instanceof ArrayValue) {
            ArrayValue value = (ArrayValue) node;
            return value.transform(builder -> builder.additionalData(OVERALL_TYPE_INFO, id));
        }
        if (node instanceof EnumValue) {
            EnumValue value = (EnumValue) node;
            return value.transform(builder -> builder.additionalData(OVERALL_TYPE_INFO, id));
        }
        if (node instanceof BooleanValue) {
            BooleanValue value = (BooleanValue) node;
            return value.transform(builder -> builder.additionalData(OVERALL_TYPE_INFO, id));
        }
        if (node instanceof FloatValue) {
            FloatValue value = (FloatValue) node;
            return value.transform(builder -> builder.additionalData(OVERALL_TYPE_INFO, id));
        }
        if (node instanceof IntValue) {
            IntValue value = (IntValue) node;
            return value.transform(builder -> builder.additionalData(OVERALL_TYPE_INFO, id));
        }
        return Assert.assertShouldNeverHappen();
    }

    public static String getOverallTypeInfoId(Node<?> node) {
        return node.getAdditionalData().get(OVERALL_TYPE_INFO);
    }
}
