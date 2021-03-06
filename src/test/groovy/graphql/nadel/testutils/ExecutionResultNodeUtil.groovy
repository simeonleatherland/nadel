package graphql.nadel.testutils


import graphql.execution.ExecutionPath
import graphql.execution.ExecutionStepInfo
import graphql.execution.nextgen.FetchedValueAnalysis
import graphql.execution.nextgen.result.ExecutionResultNode
import graphql.execution.nextgen.result.LeafExecutionResultNode
import graphql.execution.nextgen.result.ListExecutionResultNode
import graphql.execution.nextgen.result.ObjectExecutionResultNode
import graphql.execution.nextgen.result.ResolvedValue
import graphql.execution.nextgen.result.ResultNodesUtil
import graphql.execution.nextgen.result.RootExecutionResultNode

import static graphql.Scalars.GraphQLString
import static graphql.execution.ExecutionStepInfo.newExecutionStepInfo
import static graphql.execution.FetchedValue.newFetchedValue
import static graphql.execution.MergedField.newMergedField
import static graphql.execution.nextgen.FetchedValueAnalysis.newFetchedValueAnalysis
import static graphql.language.Field.newField

/**
 * A helper for tests around {@link graphql.execution.nextgen.result.ExecutionResultNode}s
 *
 * The convention is that the field name is the field value with "Val" appended
 *
 * You can use paths instead of field names and the field name will be the last segment of path
 * eg "/a/b" will result in a field name "b" and value of "bVal"
 */
class ExecutionResultNodeUtil {

    static ExecutionStepInfo esi(String pathName) {
        return esi(pathName, null)
    }

    static ExecutionStepInfo esi(String pathName, String alias) {
        if (pathName == null || pathName.isAllWhitespace()) {
            return newExecutionStepInfo().type(GraphQLString).path(ExecutionPath.rootPath()).build()
        }
        if (!pathName.contains("/")) {
            pathName = "/" + pathName
        }
        def path = ExecutionPath.parse(pathName)
        def fieldName = path.getSegmentName()

        def field = newMergedField(newField(fieldName).alias(alias).build()).build()
        newExecutionStepInfo().type(GraphQLString).field(field).path(path).build()
    }


    static ResolvedValue resolvedValue(String value) {
        return ResolvedValue.newResolvedValue().completedValue(value).build();
    }

    static LeafExecutionResultNode leaf(String name, String alias) {
        new LeafExecutionResultNode(esi(name, alias), resolvedValue(name + "Val"), null)
    }

    static LeafExecutionResultNode leaf(String name) {
        new LeafExecutionResultNode(esi(name), resolvedValue(name + "Val"), null)
    }

    static ObjectExecutionResultNode object(String name, List<ExecutionResultNode> children) {
        new ObjectExecutionResultNode(esi(name), resolvedValue(name + "Val"), children)
    }

    static ListExecutionResultNode list(String name, List<ExecutionResultNode> children) {
        new ListExecutionResultNode(esi(name), resolvedValue(name + "Val"), children)
    }

    static RootExecutionResultNode root(List<ExecutionResultNode> children) {
        new RootExecutionResultNode(children)
    }

    static Map toData(ExecutionResultNode resultNode) {
        def executionResult = ResultNodesUtil.toExecutionResult(resultNode)
        def specification = executionResult.toSpecification()
        return specification["data"]
    }

}
